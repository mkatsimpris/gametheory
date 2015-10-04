package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.CardsGroupsDoubleEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluator;

public class HUPreflopEquityRawEvaluator implements CardsGroupsDoubleEvaluator {

	private final IntCardsSpec specs;
	private final Holdem7CardsEvaluator eval;
	private final int offset;
	private final Cards52SpecTranslator specsTranslator;

	public HUPreflopEquityRawEvaluator(IntCardsSpec specs,
			Holdem7CardsEvaluator eval) {
		this.specs = specs;
		this.eval = eval;
		this.offset = specs.getOffset();
		specsTranslator = new Cards52SpecTranslator(specs, eval.getCardsSpec());
	}

	@Override
	public double getValue(int[][] cardsGroups) {
		final Cards52SpecTranslator specsTranslator = this.specsTranslator;
		final int ca1 = cardsGroups[0][0];
		final int ca2 = cardsGroups[0][1];
		final int h1 = cardsGroups[1][0];
		final int h2 = cardsGroups[1][1];
		final int[] pCards = { specsTranslator.translate(ca1),
				specsTranslator.translate(ca2), 0, 0, 0, 0, 0 };
		final int[] p2Cards = { specsTranslator.translate(h1),
				specsTranslator.translate(h2), 0, 0, 0, 0, 0 };
		int b1, b2, b3, b4, b5, p1Eval, p2Eval;
		final int offset = this.offset;
		long win = 0;
		long lose = 0;
		long tie = 0;
		final Holdem7CardsEvaluator eval = this.eval;
		for (b1 = offset; b1 < offset + 52; b1++) {
			if (b1 == ca1 || b1 == ca2 || b1 == h1 || b1 == h2)
				continue;
			pCards[2] = p2Cards[2] = specsTranslator.translate(b1);

			for (b2 = b1 + 1; b2 < offset + 52; b2++) {
				if (b2 == ca1 || b2 == ca2 || b2 == h1 || b2 == h2)
					continue;
				pCards[3] = p2Cards[3] = specsTranslator.translate(b2);

				for (b3 = b2 + 1; b3 < offset + 52; b3++) {
					if (b3 == ca1 || b3 == ca2 || b3 == h1 || b3 == h2)
						continue;
					pCards[4] = p2Cards[4] = specsTranslator.translate(b3);

					for (b4 = b3 + 1; b4 < offset + 52; b4++) {
						if (b4 == ca1 || b4 == ca2 || b4 == h1 || b4 == h2)
							continue;
						pCards[5] = p2Cards[5] = specsTranslator.translate(b4);

						for (b5 = b4 + 1; b5 < offset + 52; b5++) {
							if (b5 == ca1 || b5 == ca2 || b5 == h1 || b5 == h2)
								continue;
							pCards[6] = p2Cards[6] = specsTranslator
									.translate(b5);
							p1Eval = eval.get7CardsEval(pCards);
							p2Eval = eval.get7CardsEval(p2Cards);
							if (p2Eval > p1Eval)
								lose++;
							else if (p1Eval > p2Eval)
								win++;
							else
								tie++;
						}
					}
				}
			}
		}
		return (((double) win) + ((double) tie) / 2.0)
				/ (double) (win + lose + tie);
	}

	@Override
	public IntCardsSpec getCardsSpec() {
		return specs;
	}

	@Override
	public boolean canHandleGroups(int[] groupsSizes) {
		return groupsSizes != null && groupsSizes.length == 2
				&& groupsSizes[0] == 2 && groupsSizes[1] == 2;
	}

	@Override
	public boolean isCompatible(String gameId) {
		return "HE_POKER_PREFLOP_HU".equals(gameId);
	}

}
