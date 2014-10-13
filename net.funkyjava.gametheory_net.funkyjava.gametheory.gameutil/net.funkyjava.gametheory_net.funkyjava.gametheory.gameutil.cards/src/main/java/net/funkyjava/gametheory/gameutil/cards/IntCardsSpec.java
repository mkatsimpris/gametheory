/**
 * 
 */
package net.funkyjava.gametheory.gameutil.cards;

/**
 * This interface is a bridge from a particular integer cards implementation and
 * an arbitrary standard. The specifications define an offset with
 * {@link #getOffset()} that indicates that valid values will be between this
 * offset (inclusive) and the offset + 52 (exclusive).
 * 
 * @author Pierre Mardon
 * 
 */
public interface IntCardsSpec {
	/**
	 * Gets the cards indexing offset
	 * 
	 * @return the offset for cards indexing
	 */
	int getOffset();

	/**
	 * Gets the rank of a card with 0 = deuce, ..., 12 = ace
	 * 
	 * @param card
	 *            the int card, >= offset && < offset + 52
	 * @return the standard rank of the card
	 */
	int getStandardRank(int card);

	/**
	 * Gets consistent "arbitrary" color of a card. In fact it could be not
	 * arbitrary as it can be used to distribute odd chips when splitting pots.
	 * For that use only, we state that club = 0, diamond = 1, heart = 2 and
	 * spade = 3, as exposed by {@link CardsStrings#getColorStr(int)}
	 * 
	 * @param card
	 *            the int card, >= offset && < offset + 52
	 * @return the standard color of the card
	 */
	int getStandardColor(int card);

	/**
	 * Checks if two cards have the same color
	 * 
	 * @param card1
	 *            the first card
	 * @param card2
	 *            the second card
	 * @return true when the cards have the same color
	 */
	boolean sameColor(int card1, int card2);

	/**
	 * Checks if two cards have the same rank
	 * 
	 * @param card1
	 *            the first card
	 * @param card2
	 *            the second card
	 * @return true when the cards have the same rank
	 */
	boolean sameRank(int card1, int card2);
}