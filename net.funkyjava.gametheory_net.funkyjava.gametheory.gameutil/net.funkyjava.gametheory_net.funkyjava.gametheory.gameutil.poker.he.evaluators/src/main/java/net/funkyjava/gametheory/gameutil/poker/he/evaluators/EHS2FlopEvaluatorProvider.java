package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.CardsGroupsDoubleEvaluatorProvider;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.HoldemFullEvaluatorProvider;

/**
 * Provider for the {@link EHS2FlopEvaluator} to work in multithreading context
 * 
 * @author Pierre Mardon
 * 
 */
public class EHS2FlopEvaluatorProvider implements
		CardsGroupsDoubleEvaluatorProvider<EHS2FlopEvaluator> {

	private final IntCardsSpec specs;
	private final HoldemFullEvaluatorProvider evalProvider;

	/**
	 * Constructor
	 * 
	 * @param specs
	 *            the cards specs we want the evaluators to speak
	 * @param evalProvider
	 *            an holdem evaluator provider
	 */
	public EHS2FlopEvaluatorProvider(IntCardsSpec specs,
			HoldemFullEvaluatorProvider evalProvider) {
		this.specs = specs;
		this.evalProvider = evalProvider;
	}

	@Override
	public EHS2FlopEvaluator get() {
		return new EHS2FlopEvaluator(specs, evalProvider.getEvaluator());
	}

}
