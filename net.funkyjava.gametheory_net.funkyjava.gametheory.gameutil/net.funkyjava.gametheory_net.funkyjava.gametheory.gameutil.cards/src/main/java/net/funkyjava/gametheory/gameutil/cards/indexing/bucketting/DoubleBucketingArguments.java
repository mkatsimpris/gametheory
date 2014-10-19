package net.funkyjava.gametheory.gameutil.cards.indexing.bucketting;

import static com.google.common.base.Preconditions.checkArgument;
import lombok.Data;
import lombok.NonNull;
import net.funkyjava.gametheory.gameutil.cards.CardsGroupsDrawingTask;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.CardsGroupsIndexer;

@Data
public class DoubleBucketingArguments {
	private final double[][] values;
	private final int[] occurences;
	private final int nbBuckets;

	/**
	 * Constructor
	 * 
	 * @param values
	 *            the values for which we want to create buckets
	 * @param occurences
	 *            the absolute frequency of each value
	 * @param nbBuckets
	 *            the number of desired buckets. They will be represented by an
	 *            an integer between 0 and nbBuckets - 1
	 */
	public DoubleBucketingArguments(@NonNull double[][] values,
			@NonNull int[] occurences, int nbBuckets) {
		this.values = values;
		this.occurences = occurences;
		this.nbBuckets = nbBuckets;
		checkArgument(nbBuckets > 0, "Nb of buckets must be > 0");
		checkArgument(values.length == occurences.length,
				"The values and occurences array must have the same length");
	}

	public static DoubleBucketingArguments buildFor52CardsDeck(
			@NonNull int[] groupsSizes,
			@NonNull final CardsGroupsIndexer indexer, int nbBuckets,
			final boolean meanEvaluations, String gameId,
			@NonNull final CardsGroupsDoubleEvaluator... evaluators) {
		checkArgument(indexer.canHandleGroups(groupsSizes),
				"Indexer and groups sizes are not compatible");
		checkArgument(nbBuckets > 0, "Nb of buckets must be > 0");
		checkArgument(evaluators.length > 0,
				"There must be at least one evaluator");
		checkArgument(groupsSizes.length > 0, "Nb of groups must be > 0");
		checkArgument(indexer.isCompatible(gameId),
				"The provided indexer is not compatible with this game");

		for (int gs : groupsSizes)
			checkArgument(gs > 0, "Groups sizes must be strictly positive");
		final IntCardsSpec specs = indexer.getCardsSpec();
		for (CardsGroupsDoubleEvaluator eval : evaluators) {
			checkArgument(
					Deck52Cards.areEquivalent(specs, eval.getCardsSpec()),
					"Cards specification are not consistant between indexer and one of the evaluators");
			checkArgument(eval.isCompatible(gameId),
					"One of the evaluators is not compatible with this game");
		}
		final Deck52Cards deck = new Deck52Cards(indexer.getCardsSpec());
		final int nbIndexes = indexer.getIndexSize();
		final int nbEvaluators = evaluators.length;
		final double[][] values = new double[nbIndexes][nbEvaluators];
		final int[] occurences = new int[nbIndexes];
		deck.drawAllGroupsCombinations(groupsSizes,
				new CardsGroupsDrawingTask() {
					private int index, i;
					private int[] evalOffsets = new int[nbEvaluators];

					{
						for (int i = 0; i < nbEvaluators; i++)
							evalOffsets[i] = evaluators[i].getCardsSpec()
									.getOffset();
					}

					@Override
					public void doTask(int[][] cardsGroups) {
						if (!meanEvaluations
								&& occurences[index = indexer
										.indexOf(cardsGroups)]++ > 0)
							return;
						for (i = 0; i < nbEvaluators; i++) {
							values[index][i] += evaluators[i]
									.getValue(cardsGroups);
						}
					}
				});
		if (meanEvaluations) {
			for (int index = 0; index < nbIndexes; index++)
				for (int i = 0; i < nbEvaluators; i++)
					values[index][i] /= occurences[index];
		}
		return new DoubleBucketingArguments(values, occurences, nbBuckets);
	}
}
