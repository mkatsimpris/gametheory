/**
 * 
 */
package net.funkyjava.gametheory.gameutil.cards;

import org.apache.commons.math3.random.ISAACRandom;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * <p>
 * This class has two purposes :
 * <ul>
 * <li>Bring an efficient cards drawing algorithm with the partial Fisher–Yates
 * shuffle</li>
 * <li>Provide a persistent deck from where card can be drawn successively, and
 * that can be reseted. See {@link #draw(int[])} and {@link #reset()}
 * respectively.</li>
 * <li>Provide a one shot drawing method over a fresh deck with no variable
 * creations. See {@link Deck52Cards#oneShotDeckDraw(int[])}.</li>
 * </ul>
 * </p>
 * <p>
 * The cards are integers between 0 and 51. The number of cards to draw is
 * determined by the destination array where thoses bytes will be written.
 * </p>
 * <p>
 * A better {@link RandomGenerator} like {@link ISAACRandom} or
 * {@link MersenneTwister} can be set via {@link #setRandom(RandomGenerator)} to
 * replace the default JDK's random.
 * </p>
 * <p>
 * Not thread safe.
 * </p>
 * 
 * @author Pierre Mardon
 * 
 */
public class Deck52Cards {
	/** The persistent deck */
	private final int[] deck = new int[52];
	/** Deck for one-shot draws */
	private final int[] oneShotDeck = new int[52];
	/** The random generator */
	private RandomGenerator rand = new JDKRandomGenerator();
	/** Temporary int */
	private int tmp;
	/** The i :) */
	private int i;

	/** Number of cards already drawed in the persistent deck */
	private int drawed = 0;

	/**
	 * The constructor
	 * 
	 * @param offset
	 *            The offset of the cards indexes. For example, with offset ==
	 *            1, cards index will be between 1 and 52
	 */
	public Deck52Cards(int offset) {
		for (i = 0; i < 52; i++)
			oneShotDeck[i] = deck[i] = (i + offset);
	}

	/**
	 * Reset persistent deck
	 */
	public void reset() {
		drawed = 0;
	}

	/**
	 * Draw cards from current deck and put them in the destination array. How
	 * many cards are drew is determined by the array's length.
	 * 
	 * @param dest
	 *            destination array for cards
	 */
	public void draw(int[] dest) {
		if (drawed + dest.length > 52)
			throw new IllegalArgumentException("Cannot draw " + dest.length
					+ " cards from deck that has only " + (52 - drawed)
					+ " cards left.");
		for (i = drawed; i < drawed + dest.length; i++) {
			dest[i - drawed] = deck[tmp = (i + rand.nextInt(52 - i))];
			deck[tmp] = deck[i];
			deck[i] = dest[i - drawed];
		}
		drawed += dest.length;
	}

	/**
	 * Draw cards from a fresh deck and put them in the destination array. How
	 * many cards are drew is determined by the array's length. No other calls
	 * can be performed on the same deck.
	 * 
	 * @param dest
	 *            destination array for cards
	 */
	public void oneShotDeckDraw(int[] dest) {
		if (dest.length > 52)
			throw new IllegalArgumentException("Cannot draw more than 52 cards");
		for (i = 0; i < dest.length; i++) {
			dest[i] = oneShotDeck[tmp = (i + rand.nextInt(52 - i))];
			oneShotDeck[tmp] = oneShotDeck[i];
			oneShotDeck[i] = dest[i];
		}

	}

	/**
	 * Sets the random generator
	 * 
	 * @param random
	 *            Random generator to set
	 */
	public void setRandom(RandomGenerator random) {
		this.rand = random;
	}

	/**
	 * Gets the number of cards remaining in the persistent deck.
	 * 
	 * @return the size of the deck
	 */
	public int getSize() {
		return 52 - drawed;
	}
}
