package net.funkyjava.gametheory.gameutil.cards.indexing.bucketing;

import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;

/**
 * 
 * Cards evaluator that intends to be used for bucketing.
 * 
 * @author Pierre Mardon
 * 
 */
public interface CardsGroupsMultiDoubleEvaluator {

	/**
	 * Get the values of a cards groups set
	 * 
	 * @param cardsGroups
	 *            the cards groups set
	 * @param dest
	 *            the destination where values will be written
	 * @param offset
	 *            index offset for the destination array
	 */
	public void getValues(int[][] cardsGroups, double[] dest, int offset);

	/**
	 * Get the number of values this evaluator calculates
	 * 
	 * @return the number of values this evaluator generates
	 */
	int getNbValues();

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
	 * @param gameId
	 *            the string representation of the game
	 * 
	 * @return compatibility boolean
	 */
	public boolean isCompatible(String gameId);

	/**
	 * Gets the value name for a given index between 0 and
	 * {@link #getNbValues()} - 1
	 * 
	 * @param valueIndex
	 *            index of the value
	 * @return the name of the value
	 */
	public String getValueName(int valueIndex);
}
