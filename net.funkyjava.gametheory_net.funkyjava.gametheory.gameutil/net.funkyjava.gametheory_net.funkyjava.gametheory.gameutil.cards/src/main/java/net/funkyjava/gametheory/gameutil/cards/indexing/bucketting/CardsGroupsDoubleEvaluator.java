package net.funkyjava.gametheory.gameutil.cards.indexing.bucketting;

import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;

/**
 * 
 * Cards evaluator that intends to be used for bucketing.
 * 
 * @author Pierre Mardon
 * 
 */
public interface CardsGroupsDoubleEvaluator {

	/**
	 * Get the value of a cards groups set
	 * 
	 * @param cardsGroups
	 *            the cards groups set
	 * @return its value
	 */
	public double getValue(int[][] cardsGroups);

	/**
	 * Gets the int cards specifications
	 * 
	 * @return the int cards specifications
	 */
	IntCardsSpec getCardsSpec();

	/**
	 * Check if this evaluator can handle those groups sizes specifications.
	 * 
	 * @param groupsSizes
	 *            sizes of the cards groups to be indexed
	 * @return compatibility boolean
	 */
	public boolean canHandleGroups(int[] groupsSizes);

	/**
	 * Check if this evaluator is compatible with a game
	 * 
	 * @return compatibility boolean
	 */
	public boolean isCompatible(String gameId);
}
