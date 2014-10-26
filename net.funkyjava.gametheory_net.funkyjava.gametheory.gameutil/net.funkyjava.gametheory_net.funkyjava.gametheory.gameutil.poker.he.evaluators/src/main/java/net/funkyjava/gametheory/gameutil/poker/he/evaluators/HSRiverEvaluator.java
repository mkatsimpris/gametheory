package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import lombok.NonNull;
import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.CardsGroupsDoubleEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluator;

/**
 * Estimated Hand Strength squared evaluator. Monothread use only.
 * 
 * @author Pierre Mardon
 * 
 */
public class HSRiverEvaluator implements CardsGroupsDoubleEvaluator {

	private final IntCardsSpec cardsSpec;
	private final Holdem7CardsEvaluator eval;
	private final Cards52SpecTranslator translator;
	private final int destOffset;
	private final boolean[] usedCards = new boolean[52];
	private final int[] pRiver = new int[7];
	private final int[] opRiver = new int[7];
	private int pRiverEval, opRiverEval;
	private int o1, o2, i, j;
	private long win, tie, total;

	/**
	 * Constructor.
	 * 
	 * @param cardsSpec
	 *            the cards specifications we want this evaluator to use
	 * @param eval
	 *            an holdem evaluator to do the job
	 */
	public HSRiverEvaluator(@NonNull IntCardsSpec cardsSpec,
			@NonNull Holdem7CardsEvaluator eval) {
		this.cardsSpec = cardsSpec;
		this.eval = eval;
		translator = new Cards52SpecTranslator(cardsSpec, eval.getCardsSpec());
		destOffset = eval.getCardsSpec().getOffset();
	}

	@Override
	public IntCardsSpec getCardsSpec() {
		return cardsSpec;
	}

	@Override
	public boolean canHandleGroups(int[] groupsSizes) {
		return groupsSizes != null && groupsSizes.length == 2
				&& groupsSizes[0] == 2 && groupsSizes[1] == 5;
	}

	@Override
	public boolean isCompatible(String gameId) {
		return "HE_POKER_RIVER".equals(gameId);
	}

	@Override
	public double getValue(int[][] cardsGroups) {
		translator.translate(cardsGroups);
		for (i = 0; i < cardsGroups.length; i++)
			for (j = 0; j < cardsGroups[i].length; j++)
				usedCards[cardsGroups[i][j] - destOffset] = true;
		System.arraycopy(cardsGroups[0], 0, pRiver, 0, 2);
		System.arraycopy(cardsGroups[1], 0, pRiver, 2, 5);
		System.arraycopy(cardsGroups[1], 0, opRiver, 2, 5);
		win = tie = total = 0;
		for (o1 = destOffset; o1 < destOffset + 52; o1++) {
			if (usedCards[o1 - destOffset])
				continue;
			usedCards[o1 - destOffset] = true;
			opRiver[0] = o1;
			for (o2 = o1 + 1; o2 < destOffset + 52; o2++) {
				if (usedCards[o2 - destOffset])
					continue;
				opRiver[1] = o2;
				pRiverEval = eval.get7CardsEval(pRiver);
				opRiverEval = eval.get7CardsEval(opRiver);
				if (opRiverEval < pRiverEval)
					win++;
				else if (opRiverEval == pRiverEval)
					tie++;
				total++;
			}
			usedCards[o1 - destOffset] = false;
		}
		for (i = 0; i < cardsGroups.length; i++)
			for (j = 0; j < cardsGroups[i].length; j++)
				usedCards[cardsGroups[i][j] - destOffset] = false;
		translator.reverse(cardsGroups);
		return (win + tie / 2.0) / total;
	}

}
