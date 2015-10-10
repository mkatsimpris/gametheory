package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.CardsGroupsIndexer;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

@Slf4j
public class HUPreflopEquityTables {

	private static final String fileName = "HE_HU_EQUITY.dat";

	private static final int nbHoleCards = new WaughIndexer(new int[] { 2, 2 })
			.getIndexSize();
	private static final int nbFlop2 = (int) new WaughIndexer(new int[] { 2, 2,
			2 }).getIndexSize();

	private final WaughIndexer preflopIndexer = new WaughIndexer(new int[] { 2,
			2 });
	private final WaughIndexer flop2Indexer = new WaughIndexer(new int[] { 2,
			2, 2 });

	public HUPreflopEquityTables() {

	}

	private static Object waitObject = new Object();
	private static final int[][] preflopCounts = new int[nbHoleCards][3];
	private static int[][] flop2Counts;
	private static boolean[] flop2Hits;
	private static long total = 0;
	private static long done = 0;
	private static long start;

	public static synchronized void compute() {
		checkState(flop2Counts == null, "Tabels have already been computed");
		synchronized (waitObject) {
			start = System.currentTimeMillis();
			final ExecutorService exe = Executors.newFixedThreadPool(Math.max(
					1, Runtime.getRuntime().availableProcessors() - 1));
			final WaughIndexer holeCardsIndexer = new WaughIndexer(new int[] {
					2, 2 });

			final WaughIndexer flop2Indexer = new WaughIndexer(new int[] { 2,
					2, 2 });
			final TwoPlusTwoEvaluator eval = new TwoPlusTwoEvaluator();
			final int nbHoleCards = holeCardsIndexer.getIndexSize();
			final int nbFlop2 = (int) flop2Indexer.getIndexSize();

			final Cards52SpecTranslator translateToEval = new Cards52SpecTranslator(
					holeCardsIndexer.getCardsSpec(), eval.getCardsSpec());

			final boolean[] preflopHits = new boolean[nbHoleCards];

			int h1, h2, o1, o2;

			final int[] hole = new int[2];
			final int[] opponent = new int[2];
			final int[] flop2 = new int[2];

			// To index
			final int[][] holeCards = { hole, opponent };
			final int[][] oHoleCards = { opponent, hole };

			flop2Hits = new boolean[nbFlop2];
			flop2Counts = new int[nbFlop2][3];

			// To evaluate hands with 2+2 evaluator
			final int[] hCards = new int[7];
			final int[] oCards = new int[7];
			long deck = 0l;
			for (h1 = 0; h1 < 51; h1++) {
				hCards[0] = translateToEval.translate(hole[0] = h1);
				for (h2 = h1 + 1; h2 < 52; h2++) {
					hCards[1] = translateToEval.translate(hole[1] = h2);

					deck |= (0x1l << h1) | (0x1l << h2);

					for (o1 = 0; o1 < 51; o1++) {
						if (((0x1l << o1) & deck) != 0l)
							continue;
						oCards[0] = translateToEval.translate(opponent[0] = o1);
						for (o2 = o1 + 1; o2 < 52; o2++) {
							if (((0x1l << o2) & deck) != 0l)
								continue;
							oCards[1] = translateToEval
									.translate(opponent[1] = o2);
							final int holeIndex = holeCardsIndexer
									.indexOf(holeCards);
							if (preflopHits[holeIndex])
								// Already done for those hole cards
								continue;
							final int oHoleIndex = holeCardsIndexer
									.indexOf(oHoleCards);
							preflopHits[holeIndex] = true;
							preflopHits[oHoleIndex] = true;

							deck |= (0x1l << o2) | (0x1l << o1);
							final int[][] hFlop2CardsCopy = { hole.clone(),
									opponent.clone(), flop2.clone() };
							final int[] flop2Copy = hFlop2CardsCopy[2];
							final int[][] oFlop2CardsCopy = {
									hFlop2CardsCopy[1], hFlop2CardsCopy[0],
									flop2Copy };
							final long currentDeck = deck;
							exe.execute(new Runnable() {

								@Override
								public void run() {
									HUPreflopEquityTables.computeForHoleCards(
											currentDeck, holeIndex, oHoleIndex,
											hCards.clone(), oCards.clone(),
											flop2Copy, hFlop2CardsCopy,
											oFlop2CardsCopy, new WaughIndexer(
													new int[] { 2, 2, 2 }),
											translateToEval, eval);

								}
							});

							total++;
							deck ^= (0x1l << o1) | (0x1l << o2);
						}
					}
					deck ^= (0x1l << h1) | (0x1l << h2);
				}
			}
			log.info("Put {} runnables", total);
			exe.shutdown();
			while (!exe.isTerminated()) {
				try {
					waitObject.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		flop2Hits = null;
	}

	private static final void computeForHoleCards(final long oldDeck,
			final int holeIndex, final int oHoleIndex, final int[] hCards,
			final int[] oCards, final int[] flop2, final int[][] hFlop2Cards,
			final int[][] oFlop2Cards, CardsGroupsIndexer flop2Indexer,
			Cards52SpecTranslator translateToEval, Holdem7CardsEvaluator eval) {
		long deck = oldDeck;
		int f2, f3, t, r, preflopWin = 0, preflopLose = 0, preflopTie = 0;
		final int[][] flop2Counts = HUPreflopEquityTables.flop2Counts;
		final boolean[] flop2Hits = HUPreflopEquityTables.flop2Hits;
		final int[][] preflopCounts = HUPreflopEquityTables.preflopCounts;
		for (int f1 = 0; f1 < 50; f1++) {
			if (((0x1l << f1) & deck) != 0l)
				continue;
			hCards[2] = oCards[2] = translateToEval.translate(flop2[0] = f1);
			for (f2 = f1 + 1; f2 < 51; f2++) {
				if (((0x1l << f2) & deck) != 0l)
					continue;
				hCards[3] = oCards[3] = translateToEval
						.translate(flop2[1] = f2);
				final int flop2Index = flop2Indexer.indexOf(hFlop2Cards);
				if (!flop2Hits[flop2Index]) {
					final int oFlop2Index = (int) flop2Indexer
							.indexOf(oFlop2Cards);
					flop2Hits[flop2Index] = true;
					flop2Hits[oFlop2Index] = true;

					int f2Win = 0, f2Lose = 0, f2Tie = 0;
					for (f3 = f2 + 1; f3 < 52; f3++) {
						if (((0x1l << f3) & deck) != 0l)
							continue;
						hCards[4] = oCards[4] = translateToEval.translate(f3);
						deck |= (0x1l << f1) | (0x1l << f2) | (0x1l << f3);
						for (t = 0; t < 52; t++) {
							if (((0x1l << t) & deck) != 0l)
								continue;
							hCards[5] = oCards[5] = translateToEval
									.translate(t);
							deck |= (0x1l << t);

							for (r = 0; r < 52; r++) {
								if (((0x1l << r) & deck) != 0l)
									continue;
								hCards[6] = oCards[6] = translateToEval
										.translate(r);

								final int hVal = eval.get7CardsEval(hCards);

								final int oVal = eval.get7CardsEval(oCards);
								if (hVal > oVal) {
									// Win
									f2Win++;
								} else if (hVal < oVal) {
									// Lose
									f2Lose++;
								} else {
									// Tie
									f2Tie++;
								}
							} // End computing for all
								// rivers
							deck ^= (0x1l << t);
						} // End computing for all turns
						deck ^= (0x1l << f1) | (0x1l << f2) | (0x1l << f3);
					}
					final int[] fc = flop2Counts[flop2Index];
					final int[] ofc = flop2Counts[oFlop2Index];
					ofc[1] = fc[0] = f2Win;
					ofc[0] = fc[1] = f2Lose;
					ofc[2] = fc[2] = f2Tie;
				}
				final int[] fc = flop2Counts[flop2Index];
				preflopWin += fc[0];
				preflopLose += fc[1];
				preflopTie += fc[2];
			}
		}
		final int[] pc = preflopCounts[holeIndex];
		final int[] opc = preflopCounts[oHoleIndex];
		opc[1] = pc[0] = preflopWin;
		opc[0] = pc[1] = preflopLose;
		opc[2] = pc[2] = preflopTie;
		synchronized (waitObject) {
			done++;
			final long elapsed = System.currentTimeMillis() - start;
			final double ratio = ((double) done) / ((double) total);
			final double remaining = (elapsed / (double) done)
					* (double) (total - done);
			log.info(
					"Done : {}/{}, {}% | elapsed {} seconds | remaining {} seconds",
					done, total, ((int) (ratio * 10000)) / 100.0,
					elapsed / 1000, (long) remaining / 1000);
			waitObject.notifyAll();
		}
	}

	public static int[][] getPreflopCounts() {
		return preflopCounts;
	}

	public static int[][] getFlop2Counts() {
		return flop2Counts;
	}

	public int[] getPreflopWinLoseTie(int[] heroCards, int[] opponentCards) {
		return preflopCounts[preflopIndexer.indexOf(new int[][] { heroCards,
				opponentCards })];
	}

	public int[] getFlop2WinLoseTie(int[] heroCards, int[] opponentCards,
			int[] flop2Cards) {
		return flop2Counts[flop2Indexer.indexOf(new int[][] { heroCards,
				opponentCards, flop2Cards })];
	}

	public IntCardsSpec getCardsSpec() {
		return DefaultIntCardsSpecs.getDefault();
	}

	public static synchronized void writeTo(Path path) throws IOException {
		checkNotNull(flop2Counts,
				"Table have not be computed, call compute method first.");
		checkArgument(!Files.exists(path), "File "
				+ path.toAbsolutePath().toString() + " already exists");
		checkArgument(
				Files.exists(path.getParent())
						&& Files.isDirectory(path.getParent()), "File "
						+ path.toAbsolutePath().toString()
						+ " parent folder doesn't exist");
		final Set<OpenOption> options = new HashSet<OpenOption>();
		options.add(StandardOpenOption.WRITE);
		options.add(StandardOpenOption.READ);
		options.add(StandardOpenOption.CREATE);

		final File f = path.toFile();
		try (final ZipOutputStream out = new ZipOutputStream(
				new FileOutputStream(f))) {
			final ZipEntry e = new ZipEntry(fileName);
			out.putNextEntry(e);
			write(out, preflopCounts);
			write(out, flop2Counts);
			out.closeEntry();
		}
	}

	public static synchronized void readFrom(Path path) throws IOException {
		checkArgument(Files.exists(path), "File "
				+ path.toAbsolutePath().toString() + " doesn't exists");
		checkArgument(!Files.isDirectory(path), "File "
				+ path.toAbsolutePath().toString() + " is a folder");
		checkState(flop2Counts == null,
				"Tables have already been loaded or computed");
		flop2Counts = new int[nbFlop2][3];
		final Set<OpenOption> options = new HashSet<OpenOption>();
		options.add(StandardOpenOption.WRITE);
		options.add(StandardOpenOption.READ);
		options.add(StandardOpenOption.CREATE);

		try (final ZipFile zipFile = new ZipFile(path.toFile())) {
			final Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();
				if (!fileName.equals(entry.getName()))
					continue;
				final InputStream stream = zipFile.getInputStream(entry);
				read(stream, preflopCounts);
				read(stream, flop2Counts);
				return;
			}
		}
		throw new IllegalArgumentException(
				"The zip file doesn't contain a file named " + fileName);
	}

	private static void write(final OutputStream out, final int[][] src)
			throws IOException {
		final ByteBuffer buf = ByteBuffer.allocate(12);
		final IntBuffer intBuf = buf.asIntBuffer();
		for (int i = 0; i < src.length; i++) {
			intBuf.put(src[i]);
			out.write(buf.array());
			buf.clear();
		}
	}

	private static void read(final InputStream stream, final int[][] dest)
			throws IOException {
		final ByteBuffer buf = ByteBuffer.allocate(12);
		final byte[] array = buf.array();
		final IntBuffer intBuf = buf.asIntBuffer();
		for (int i = 0; i < dest.length; i++) {
			int read = 0;
			int lastRead;
			while (read < 12) {
				read += lastRead = stream.read(array, read, 12 - read);
				if (lastRead < 0)
					throw new IOException(
							"Couldn't read the input stream, file may be too short or corrupted");
			}
			intBuf.get(dest[i]);
			intBuf.clear();
		}

	}
}
