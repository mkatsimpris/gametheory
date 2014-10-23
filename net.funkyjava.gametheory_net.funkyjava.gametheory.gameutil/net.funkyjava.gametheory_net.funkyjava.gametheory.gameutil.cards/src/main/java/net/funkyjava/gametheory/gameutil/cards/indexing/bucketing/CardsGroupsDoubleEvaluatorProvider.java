package net.funkyjava.gametheory.gameutil.cards.indexing.bucketing;

/**
 * Aims to provide evaluators of the same nature that can be run in parallel to
 * work with multithread implementations. Each evaluator furnished has to have
 * the same cards specifications.
 * 
 * @author Pierre Mardon
 * 
 * @param <E>
 *            the evaluator class
 */
public interface CardsGroupsDoubleEvaluatorProvider<E extends CardsGroupsDoubleEvaluator> {

	/**
	 * Get an evaluator. It can always be the same instance as long as it's
	 * thread safe
	 * 
	 * @return the evaluator
	 */
	E get();

}
