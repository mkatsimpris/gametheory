package net.funkyjava.gametheory.gameutil.cards.indexing.bucketing;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.CardsGroupsDrawingTask;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;
import net.funkyjava.gametheory.gameutil.cards.indexing.CardsGroupsIndexer;

/**
 * 
 * Build a double look-up table (LUT) for a 52 cards game via the method
 * {@link #buildLUT(CardsGroupsIndexer, CardsGroupsDoubleEvaluatorProvider, int[], int, boolean, boolean, String)}
 * or
 * {@link #buildAndWriteLUT(CardsGroupsIndexer, CardsGroupsDoubleEvaluatorProvider, int[], int, boolean, String, Path, boolean)}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class Double52CardsLUTBuilder {
	private final ExecutorService service;
	private static final int nbJobs = 20;
	private final CardsGroupsIndexer indexer;
	private final int indexSize;
	private final CardsGroupsDoubleEvaluator[] evaluators;
	private final int nbThreads;
	private final List<int[][]> toDo = new LinkedList<>();
	private final List<int[][]> freeJobs = new LinkedList<>();
	private final Cards52SpecTranslator translator;
	private final int[] groupsSizes;
	private final boolean meanValues, countOccurrences;
	private final DoubleLUT lut;
	private final boolean[] uniqueValSet;
	private boolean stop = false;

	private long count = 0;
	private long uniqueCount = 0;
	private long start;

	private Double52CardsLUTBuilder(@NonNull CardsGroupsIndexer indexer,
			@NonNull CardsGroupsDoubleEvaluatorProvider<?> provider,
			int[] groupsSizes, int nbThreads, boolean countOccurrences,
			boolean meanValues, String gameId) {
		checkArgument(nbThreads > 0, "Cant run with nbThreads <= 2");
		checkArgument(!meanValues || countOccurrences,
				"Cannot mean values without counting occurrencies");
		this.indexer = indexer;
		indexSize = indexer.getIndexSize();
		this.evaluators = new CardsGroupsDoubleEvaluator[nbThreads - 1];
		this.groupsSizes = groupsSizes;
		this.nbThreads = nbThreads;
		this.meanValues = meanValues;
		this.countOccurrences = countOccurrences;
		uniqueValSet = countOccurrences ? null : new boolean[indexSize];
		service = Executors.newFixedThreadPool(nbThreads + 1);
		for (int i = 0; i < nbThreads - 1; i++) {
			evaluators[i] = provider.get();
			checkArgument(evaluators[i].canHandleGroups(groupsSizes),
					"It seems like the evaluator provider cannot handle those groups sizes");
			checkArgument(evaluators[i].isCompatible(gameId),
					"The evaluator provider isn't compatible with this gameId");
		}
		translator = new Cards52SpecTranslator(indexer.getCardsSpec(),
				evaluators[0].getCardsSpec());
		lut = new DoubleLUT(indexSize, countOccurrences);
		for (int i = 0; i < nbJobs; i++) {
			final int[][] cards = new int[groupsSizes.length][];
			for (int j = 0; j < groupsSizes.length; j++)
				cards[j] = new int[groupsSizes[j]];
			freeJobs.add(cards);
		}
	}

	private DoubleLUT build() throws InterruptedException {
		start = System.currentTimeMillis();
		service.execute(new Feeder());
		for (int i = 0; i < nbThreads - 1; i++) {
			service.execute(new Eater(i));
		}
		service.shutdown();
		service.awaitTermination(Long.MAX_VALUE / 2, TimeUnit.DAYS);
		log.info("Filling LUT complete");
		if (meanValues)
			lut.meanValues();
		return lut;
	}

	private void end() {
		log.info("End walking cards combinations");
		stop = true;
		synchronized (toDo) {
			toDo.notify();
		}
	}

	private class Feeder implements Runnable {
		int i;

		@Override
		public void run() {
			new Deck52Cards(indexer.getCardsSpec()).drawAllGroupsCombinations(
					groupsSizes, new CardsGroupsDrawingTask() {
						private int[][] cards;

						@Override
						public boolean doTask(int[][] cardsGroups) {
							synchronized (toDo) {
								while (freeJobs.isEmpty())
									try {
										toDo.wait();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								if (!countOccurrences
										&& uniqueCount == indexSize) {
									end();
									return false;
								}
								cards = freeJobs.remove(0);
								for (i = 0; i < cards.length; i++)
									System.arraycopy(cardsGroups[i], 0,
											cards[i], 0, cards[i].length);
								toDo.add(cards);
								toDo.notify();
								return true;
							}

						}
					});
			end();
		}
	}

	private class Eater implements Runnable {

		private final int index;

		public Eater(int index) {
			this.index = index;
		}

		@Override
		public void run() {
			long elapsed;
			int[][] cards = null;
			int handIndex;
			double val = 0;
			while (true) {
				synchronized (toDo) {
					try {
						while (!stop && toDo.isEmpty()) {
							toDo.wait();
						}
						if (toDo.isEmpty()) {
							toDo.notifyAll();
							log.info("Lut eater process {} ended", index);
							return;
						}
						cards = toDo.remove(0);
					} catch (InterruptedException e) {
						e.printStackTrace();
						toDo.notifyAll();
						return;
					}
					toDo.notifyAll();

					handIndex = indexer.indexOf(cards);
					if (handIndex < 0 || handIndex >= indexSize)
						throw new IllegalAccessError("Indexer returned index "
								+ handIndex + " for cards "
								+ Arrays.deepToString(cards));
					if (countOccurrences) {
						lut.incrOccurrencesCountFor(handIndex);
						if (meanValues
								|| lut.getOccurencesCountFor(handIndex) == 1) {
						} else {
							freeJobs.add(cards);
							toDo.notifyAll();
							continue;
						}
					} else {
						if (uniqueValSet[handIndex]) {
							freeJobs.add(cards);
							toDo.notifyAll();
							continue;
						}
						uniqueValSet[handIndex] = true;
						uniqueCount++;
					}
					count++;
					if (count % 10000 == 0) {
						// Elapsed time
						elapsed = System.currentTimeMillis() - start;
						log.info("Iter {} iter/s {} elapsed time {}", count,
								count * 1000 / (double) elapsed, elapsed);
					}
				}
				translator.translate(cards);
				val = evaluators[index].getValue(cards);
				translator.reverse(cards);
				synchronized (toDo) {
					lut.addValueFor(handIndex, val);
					freeJobs.add(cards);
					toDo.notifyAll();
				}
			}
		}
	}

	/**
	 * Build a double look-up table
	 * 
	 * @param indexer
	 *            the cards groups indexer
	 * @param provider
	 *            the evaluator provider for cards groups
	 * @param groupsSizes
	 *            the expected cards groups sizes
	 * @param nbThreads
	 *            number of thread to run this build. An additional thread will
	 *            be used to walk all cards groups combinations.
	 * @param countOccurrences
	 *            whether occurrences should be counted
	 * @param meanValues
	 *            true means that the values provided by the evaluators must be
	 *            averaged
	 * @param gameId
	 *            the game id for which the LUT is built
	 * @return the resulting LUT
	 * @throws InterruptedException
	 */
	public static DoubleLUT buildLUT(@NonNull CardsGroupsIndexer indexer,
			@NonNull CardsGroupsDoubleEvaluatorProvider<?> provider,
			int[] groupsSizes, int nbThreads, boolean countOccurrences,
			boolean meanValues, String gameId) throws InterruptedException {
		return new Double52CardsLUTBuilder(indexer, provider, groupsSizes,
				nbThreads, countOccurrences, meanValues, gameId).build();
	}

	/**
	 * Builds a double look-up table and writes it to a non-existing file. The
	 * permission to create the file is checked before the LUT building.
	 * 
	 * @param indexer
	 *            the cards groups indexer
	 * @param provider
	 *            the evaluator provider for cards groups
	 * @param groupsSizes
	 *            the expected cards groups sizes
	 * @param nbThreads
	 *            number of thread to run this build. An additional thread will
	 *            be used to walk all cards groups combinations.
	 * @param meanValues
	 *            true means that the values provided by the evaluators must be
	 *            averaged
	 * @param gameId
	 *            the game id for which the LUT is built
	 * @param filePath
	 *            path of the file where the LUT will be written
	 * @param writeOccurences
	 *            write occurences array to the file
	 * @return the resulting LUT
	 * @throws InterruptedException
	 * @throws IOException
	 *             when writting encounters an error
	 */
	public static DoubleLUT buildAndWriteLUT(
			@NonNull CardsGroupsIndexer indexer,
			@NonNull CardsGroupsDoubleEvaluatorProvider<?> provider,
			int[] groupsSizes, int nbThreads, boolean meanValues,
			String gameId, Path filePath, boolean writeOccurences)
			throws InterruptedException, IOException {
		Files.createFile(filePath);
		Files.delete(filePath);
		final DoubleLUT res = new Double52CardsLUTBuilder(indexer, provider,
				groupsSizes, nbThreads, writeOccurences, meanValues, gameId)
				.build();
		res.writeToFile(filePath, writeOccurences);
		return res;
	}

}
