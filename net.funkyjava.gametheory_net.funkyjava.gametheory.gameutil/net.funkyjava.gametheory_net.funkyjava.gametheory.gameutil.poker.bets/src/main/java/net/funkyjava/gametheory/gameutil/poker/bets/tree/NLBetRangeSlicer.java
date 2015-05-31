package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import net.funkyjava.gametheory.gameutil.poker.bets.rounds.NLHandRounds;

/**
 * A {@link NLBetRangeSlicer} should be able to provide a subset of possible
 * player bets to build a {@link RoundBetTree}
 * 
 * @author Pierre Mardon
 * 
 */
public interface NLBetRangeSlicer {

	/**
	 * The int fold representation for building bet trees
	 */
	public static final int fold = Integer.MIN_VALUE;

	/**
	 * Get all possible move values. When one is equal to {@link #fold}, means
	 * fold. Otherwise, means call/bet/raise according to the context
	 * 
	 * @param hand
	 *            representing current hand state. No action perform on this
	 *            object will have any effect.
	 * @return the authorized bet values, call and fold (bet of {@link #fold}
	 *         value) included
	 */
	int[] slice(NLHandRounds hand);
}
