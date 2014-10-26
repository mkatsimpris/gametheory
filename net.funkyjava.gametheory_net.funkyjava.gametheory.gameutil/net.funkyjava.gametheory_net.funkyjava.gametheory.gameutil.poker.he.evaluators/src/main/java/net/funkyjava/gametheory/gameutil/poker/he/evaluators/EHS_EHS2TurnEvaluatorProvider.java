package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.CardsGroupsMultiDoubleEvaluatorProvider;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluatorProvider;

/**
 * Provider for the {@link EHS_EHS2FlopEvaluator} to work in multithreading
 * context
 * 
 * @author Pierre Mardon
 * 
 */
public class EHS_EHS2TurnEvaluatorProvider implements
		CardsGroupsMultiDoubleEvaluatorProvider {

	private final IntCardsSpec specs;
	private final Holdem7CardsEvaluatorProvider evalProvider;

	/**
	 * Constructor
	 * 
	 * @param specs
	 *            the cards specs we want the evaluators to speak
	 * @param evalProvider
	 *            an holdem evaluator provider
	 */
	public EHS_EHS2TurnEvaluatorProvider(IntCardsSpec specs,
			Holdem7CardsEvaluatorProvider evalProvider) {
		this.specs = specs;
		this.evalProvider = evalProvider;
	}

	@Override
	public EHS_EHS2TurnEvaluator get() {
		return new EHS_EHS2TurnEvaluator(specs, evalProvider.getEvaluator());
	}

}
