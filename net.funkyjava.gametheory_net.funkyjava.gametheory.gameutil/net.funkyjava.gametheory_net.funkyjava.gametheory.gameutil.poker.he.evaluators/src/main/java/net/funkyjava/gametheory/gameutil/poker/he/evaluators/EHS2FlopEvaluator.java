package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import lombok.NonNull;
import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.CardsGroupsDoubleEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.HoldemFullEvaluator;

/**
 * Estimated Hand Strength squared evaluator. Monothread use only.
 * 
 * @author Pierre Mardon
 * 
 */
public class EHS2FlopEvaluator implements CardsGroupsDoubleEvaluator {

	private final IntCardsSpec cardsSpec;
	private final HoldemFullEvaluator eval;
	private final Cards52SpecTranslator translator;
	private final int destOffset;
	private final boolean[] usedCards = new boolean[52];
	private final int[] pRiver = new int[7];
	private final int[] opRiver = new int[7];
	private int pRiverEval, opRiverEval;
	private int o1, o2, t, r, i, j;
	private double ehs, ehs2;
	private long win, tie, total, bigTotal;

	/**
	 * Constructor.
	 * 
	 * @param cardsSpec
	 *            the cards specifications we want this evaluator to use
	 * @param eval
	 *            an holdem evaluator to do the job
	 */
	public EHS2FlopEvaluator(@NonNull IntCardsSpec cardsSpec,
			@NonNull HoldemFullEvaluator eval) {
		this.cardsSpec = cardsSpec;
		this.eval = eval;
		translator = new Cards52SpecTranslator(cardsSpec, eval.getCardsSpec());
		destOffset = eval.getCardsSpec().getOffset();
	}

	@Override
	public double getValue(int[][] cardsGroups) {
		ehs2 = 0;
		bigTotal = 0;
		translator.translate(cardsGroups);
		for (i = 0; i < cardsGroups.length; i++)
			for (j = 0; j < cardsGroups[i].length; j++)
				usedCards[cardsGroups[i][j] - destOffset] = true;
		System.arraycopy(cardsGroups[0], 0, pRiver, 0, 2);
		System.arraycopy(cardsGroups[1], 0, pRiver, 2, 3);
		System.arraycopy(cardsGroups[1], 0, opRiver, 2, 3);

		for (t = destOffset; t < destOffset + 52; t++) {
			if (usedCards[t - destOffset])
				continue;
			usedCards[t - destOffset] = true;
			pRiver[5] = opRiver[5] = t;
			for (r = t + 1; r < destOffset + 52; r++) {
				if (usedCards[r - destOffset])
					continue;
				pRiver[6] = opRiver[6] = r;
				usedCards[r - destOffset] = true;
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
				ehs2 += (ehs = (win + tie / 2.0) / total) * ehs;
				bigTotal++;
				usedCards[r - destOffset] = false;
			}
			usedCards[t - destOffset] = false;
		}
		for (i = 0; i < cardsGroups.length; i++)
			for (j = 0; j < cardsGroups[i].length; j++)
				usedCards[cardsGroups[i][j] - destOffset] = false;
		translator.reverse(cardsGroups);
		return ehs2 / bigTotal;
	}

	@Override
	public IntCardsSpec getCardsSpec() {
		return cardsSpec;
	}

	@Override
	public boolean canHandleGroups(int[] groupsSizes) {
		return groupsSizes != null && groupsSizes.length == 2
				&& groupsSizes[0] == 2 && groupsSizes[1] == 3;
	}

	@Override
	public boolean isCompatible(String gameId) {
		return "HE_POKER_FLOP".equals(gameId);
	}

}
