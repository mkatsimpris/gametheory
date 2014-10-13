/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.he.handeval;

/**
 * Provider for {@link HoldemEvaluator}
 * 
 * @author Pierre Mardon
 * 
 */
public interface HoldemEvaluatorProvider {

	/**
	 * Gets an evaluator. When the evaluator implementation is not thread-safe,
	 * should create a new one for each call.
	 * 
	 * @return the evaluator
	 */
	HoldemEvaluator getEvaluator();
}
