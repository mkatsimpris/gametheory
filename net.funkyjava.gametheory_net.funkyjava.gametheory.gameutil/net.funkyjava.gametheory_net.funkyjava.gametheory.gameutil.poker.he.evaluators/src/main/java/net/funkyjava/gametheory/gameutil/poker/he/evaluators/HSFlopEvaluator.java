package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import lombok.NonNull;
import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.CardsGroupsDoubleEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem5CardsEvaluator;

/**
 * Hand Strength flop evaluator
 * 
 * @author Pierre Mardon
 * 
 */
public class HSFlopEvaluator implements CardsGroupsDoubleEvaluator {

	private final IntCardsSpec cardsSpec;
	private final Holdem5CardsEvaluator eval;
	private final Cards52SpecTranslator translator;
	private final int destOffset;
	private static final int aheadIndex = 0, tiedIndex = 1, behindIndex = 2;

	/**
	 * Constructor
	 * 
	 * @param cardsSpec
	 *            the cards specs we want this evaluator to use
	 * @param eval
	 *            a holdem 5 cards evaluator
	 */
	public HSFlopEvaluator(@NonNull IntCardsSpec cardsSpec,
			@NonNull Holdem5CardsEvaluator eval) {
		this.cardsSpec = cardsSpec;
		this.eval = eval;
		translator = new Cards52SpecTranslator(cardsSpec, eval.getCardsSpec());
		destOffset = eval.getCardsSpec().getOffset();
	}

	@Override
	public double getValue(int[][] cardsGroups) {
		final boolean[] usedCards = new boolean[52];
		translator.translate(cardsGroups);
		for (int i = 0; i < cardsGroups.length; i++)
			for (int j = 0; j < cardsGroups[i].length; j++)
				usedCards[cardsGroups[i][j] - destOffset] = true;
		final int[] pFlop = new int[5];
		final int[] opFlop = new int[5];
		System.arraycopy(cardsGroups[0], 0, pFlop, 0, 2);
		System.arraycopy(cardsGroups[1], 2, pFlop, 0, 3);
		System.arraycopy(cardsGroups[1], 2, opFlop, 0, 3);
		final int pFlopEval = eval.get5CardsEval(pFlop);
		int opFlopEval;
		final int[] handPot = new int[3];
		int o1, o2, index;
		for (o1 = destOffset; o1 < destOffset + 52; o1++)
			if (usedCards[o1 - destOffset])
				continue;
			else {
				usedCards[o1] = true;
				opFlop[0] = o1;
				for (o2 = o1 + 1; o2 < destOffset + 52; o2++)
					if (usedCards[o2 - destOffset])
						continue;
					else {
						opFlop[1] = o2;
						opFlopEval = eval.get5CardsEval(opFlop);
						if (opFlopEval < pFlopEval)
							index = aheadIndex;
						else if (opFlopEval > pFlopEval)
							index = behindIndex;
						else
							index = tiedIndex;
						handPot[index]++;
					}
				usedCards[o1 - destOffset] = false;
			}
		translator.reverse(cardsGroups);
		return (handPot[aheadIndex] + handPot[tiedIndex] / 2.0)
				/ (handPot[aheadIndex] + handPot[tiedIndex] + handPot[behindIndex]);
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
