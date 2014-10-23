package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import lombok.NonNull;
import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.CardsGroupsDoubleEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.HoldemFullEvaluator;

/**
 * Estimated Hand Strength evaluator
 * 
 * @author Pierre Mardon
 * 
 */
public class EHSFlopEvaluator implements CardsGroupsDoubleEvaluator {

	private final IntCardsSpec cardsSpec;
	private final HoldemFullEvaluator eval;
	private final Cards52SpecTranslator translator;
	private final int destOffset;
	private static final int aheadIndex = 0, tiedIndex = 1, behindIndex = 2;
	private final boolean[] usedCards = new boolean[52];
	private final int[] pFlop = new int[5];
	private final int[] opFlop = new int[5];
	private final int[] pRiver = new int[7];
	private final int[] opRiver = new int[7];
	private int pFlopEval, opFlopEval, pRiverEval, opRiverEval;
	private final int[] handPot = new int[3];
	private final int[][] handPotFuture = new int[3][3];
	private int o1, o2, t, r, index, i, j;
	private double pPot, nPot, handStrength;

	/**
	 * Constructor.
	 * 
	 * @param cardsSpec
	 *            the cards specifications we want this evaluator to use
	 * @param eval
	 *            an holdem evaluator to do the job
	 */
	public EHSFlopEvaluator(@NonNull IntCardsSpec cardsSpec,
			@NonNull HoldemFullEvaluator eval) {
		this.cardsSpec = cardsSpec;
		this.eval = eval;
		translator = new Cards52SpecTranslator(cardsSpec, eval.getCardsSpec());
		destOffset = eval.getCardsSpec().getOffset();
	}

	@Override
	public double getValue(int[][] cardsGroups) {
		translator.translate(cardsGroups);
		for (i = 0; i < cardsGroups.length; i++)
			for (j = 0; j < cardsGroups[i].length; j++)
				usedCards[cardsGroups[i][j] - destOffset] = true;
		System.arraycopy(cardsGroups[0], 0, pFlop, 0, 2);
		System.arraycopy(cardsGroups[0], 0, pRiver, 0, 2);
		System.arraycopy(cardsGroups[1], 0, pFlop, 2, 3);
		System.arraycopy(cardsGroups[1], 0, pRiver, 2, 3);
		System.arraycopy(cardsGroups[1], 0, opFlop, 2, 3);
		System.arraycopy(cardsGroups[1], 0, opRiver, 2, 3);
		pFlopEval = eval.get5CardsEval(pFlop);

		for (o1 = destOffset; o1 < destOffset + 52; o1++)
			if (usedCards[o1 - destOffset])
				continue;
			else {
				usedCards[o1 - destOffset] = true;
				opRiver[0] = opFlop[0] = o1;
				for (o2 = o1 + 1; o2 < destOffset + 52; o2++)
					if (usedCards[o2 - destOffset])
						continue;
					else {
						usedCards[o2 - destOffset] = true;
						opRiver[1] = opFlop[1] = o2;
						opFlopEval = eval.get5CardsEval(opFlop);
						if (opFlopEval < pFlopEval)
							index = aheadIndex;
						else if (opFlopEval > pFlopEval)
							index = behindIndex;
						else
							index = tiedIndex;
						for (t = destOffset; t < destOffset + 52; t++)
							if (usedCards[t - destOffset])
								continue;
							else {
								usedCards[t - destOffset] = true;
								pRiver[5] = opRiver[5] = t;
								for (r = t + 1; r < destOffset + 52; r++)
									if (usedCards[r - destOffset])
										continue;
									else {
										pRiver[6] = opRiver[6] = r;
										pRiverEval = eval.get7CardsEval(pRiver);
										opRiverEval = eval
												.get7CardsEval(opRiver);
										if (pRiverEval > opRiverEval)
											handPotFuture[index][aheadIndex]++;
										else if (pRiverEval < opRiverEval)
											handPotFuture[index][behindIndex]++;
										else
											handPotFuture[index][tiedIndex]++;
										handPot[index]++;
									}
								usedCards[t - destOffset] = false;
							}
						usedCards[o2 - destOffset] = false;
					}
				usedCards[o1 - destOffset] = false;
			}
		for (i = 0; i < cardsGroups.length; i++)
			for (j = 0; j < cardsGroups[i].length; j++)
				usedCards[cardsGroups[i][j] - destOffset] = false;
		translator.reverse(cardsGroups);
		pPot = (handPotFuture[behindIndex][aheadIndex]
				+ handPotFuture[behindIndex][tiedIndex] / 2.0 + handPotFuture[tiedIndex][aheadIndex] / 2.0)
				/ (handPot[behindIndex] + handPot[tiedIndex] / 2.0);
		nPot = (handPotFuture[aheadIndex][behindIndex]
				+ handPotFuture[tiedIndex][behindIndex] / 2.0 + handPotFuture[aheadIndex][tiedIndex] / 2.0)
				/ (handPot[aheadIndex] + handPot[tiedIndex] / 2.0);
		handStrength = (handPot[aheadIndex] + handPot[tiedIndex] / 2.0)
				/ (handPot[aheadIndex] + handPot[tiedIndex] + handPot[behindIndex]);
		for (i = 0; i < 3; i++) {
			handPot[i] = 0;
			for (j = 0; j < 3; j++)
				handPotFuture[i][j] = 0;
		}
		return handStrength * (1 - nPot) + (1 - handStrength) * pPot;
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
