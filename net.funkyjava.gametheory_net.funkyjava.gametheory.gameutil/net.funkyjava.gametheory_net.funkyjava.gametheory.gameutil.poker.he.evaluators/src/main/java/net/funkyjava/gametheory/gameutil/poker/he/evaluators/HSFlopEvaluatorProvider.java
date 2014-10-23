package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.CardsGroupsDoubleEvaluatorProvider;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem5CardsEvaluatorProvider;

/**
 * A provider for {@link HSFlopEvaluator}
 * 
 * @author Pierre Mardon
 * 
 */
public class HSFlopEvaluatorProvider implements
		CardsGroupsDoubleEvaluatorProvider<HSFlopEvaluator> {

	private final IntCardsSpec specs;
	private final Holdem5CardsEvaluatorProvider evalProvider;

	/**
	 * Constructor
	 * 
	 * @param specs
	 *            the cards specs we want the evaluators to speak
	 * @param evalProvider
	 *            5 cards holdem evaluator provider
	 */
	public HSFlopEvaluatorProvider(IntCardsSpec specs,
			Holdem5CardsEvaluatorProvider evalProvider) {
		this.specs = specs;
		this.evalProvider = evalProvider;
	}

	@Override
	public HSFlopEvaluator get() {
		return new HSFlopEvaluator(specs, evalProvider.getEvaluator());
	}

}
