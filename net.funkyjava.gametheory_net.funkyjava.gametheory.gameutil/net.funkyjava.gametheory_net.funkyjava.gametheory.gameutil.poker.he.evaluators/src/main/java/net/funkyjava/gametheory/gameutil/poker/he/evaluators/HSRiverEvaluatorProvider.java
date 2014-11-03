package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.CardsGroupsDoubleEvaluatorProvider;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluatorProvider;

/**
 * Provider for {@link HSRiverEvaluator}
 * 
 * @author Pierre Mardon
 * 
 */
public class HSRiverEvaluatorProvider implements
		CardsGroupsDoubleEvaluatorProvider {

	private final IntCardsSpec specs;
	private final Holdem7CardsEvaluatorProvider evalProvider;

	/**
	 * Constructor
	 * 
	 * @param cardsSpec
	 *            the cards specifications we want the created evaluators to use
	 * @param evalProvider
	 *            an holdem evaluator provider
	 */
	public HSRiverEvaluatorProvider(IntCardsSpec cardsSpec,
			Holdem7CardsEvaluatorProvider evalProvider) {
		this.specs = cardsSpec;
		this.evalProvider = evalProvider;
	}

	@Override
	public HSRiverEvaluator get() {
		return new HSRiverEvaluator(specs, evalProvider.getEvaluator());
	}

}
