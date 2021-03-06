/**
 * 
 */
package net.funkyjava.gametheory.gameutil.cards;

import org.apache.commons.math3.random.ISAACRandom;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.CombinatoricsUtils;

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
 * The cards are integers between 0 and 51 with an optional offset. The number
 * of cards to draw is determined by the destination array where thoses bytes
 * will be written.
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

	/** The offset of the cards indexes */
	private final int offset;
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
		this(new DefaultIntCardsSpecs(offset));
	}

	/**
	 * The constructor
	 * 
	 * @param cardsSpec
	 *            cards specification defining the cards indexes offset
	 */
	public Deck52Cards(IntCardsSpec cardsSpec) {
		this.offset = cardsSpec.getOffset();
		for (i = 0; i < 52; i++)
			oneShotDeck[i] = deck[i] = (i + offset);
	}

	/**
	 * The constructor for offset zero.
	 * 
	 */
	public Deck52Cards() {
		this(new DefaultIntCardsSpecs(0));
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

	/**
	 * Recursively and exhaustively draw all possible cards groups combination
	 * 
	 * @param groupsSizes
	 *            sizes fo the groups of cards
	 * @param task
	 *            task to execute for each draw
	 */
	public void drawAllGroupsCombinations(int[] groupsSizes,
			CardsGroupsDrawingTask task) {
		final int nbGroups = groupsSizes.length;
		final int[][] cardsGroups = new int[nbGroups][];
		final boolean[] inUse = new boolean[52];
		for (int g = 0; g < nbGroups; g++)
			cardsGroups[g] = new int[groupsSizes[g]];
		enumCards(0, 0, 0, cardsGroups, inUse, task);
	}

	private boolean enumCards(int minCard, int g, int c, int[][] cardsGroups,
			boolean[] inUse, CardsGroupsDrawingTask task) {
		boolean res = false;
		if (c < cardsGroups[g].length - 1) {
			for (int card = minCard; card < 52; card++) {
				if (inUse[card])
					continue;
				inUse[card] = true;
				cardsGroups[g][c] = card + offset;
				if (!enumCards(card + 1, g, c + 1, cardsGroups, inUse, task)) {
					inUse[card] = false;
					break;
				}
				res = true;
				inUse[card] = false;
			}
		} else if (g < cardsGroups.length - 1) {
			for (int card = minCard; card < 52; card++) {
				if (inUse[card])
					continue;
				inUse[card] = true;
				cardsGroups[g][c] = card + offset;
				if (!enumCards(0, g + 1, 0, cardsGroups, inUse, task)) {
					inUse[card] = false;
					break;
				}
				res = true;
				inUse[card] = false;
			}
		} else {
			for (int card = minCard; card < 52; card++) {
				if (inUse[card])
					continue;
				res = true;
				cardsGroups[g][c] = card + offset;
				if (!task.doTask(cardsGroups))
					return false;
			}
		}
		return res;
	}

	/**
	 * Computes the number of all possible cards groups combinations.
	 * 
	 * @param groupsSizes
	 *            the cards groups sizes
	 * @return the number of combinations
	 */
	public static long getCardsGroupsCombinationsCount(int[] groupsSizes) {
		return getCardsGroupsCombinationsCount(groupsSizes, 0);
	}

	/**
	 * Computes the number of all possible cards groups combinations.
	 * 
	 * @param groupsSizes
	 *            the cards groups sizes
	 * @param nbDeadCards
	 *            number of dead cards in the deck
	 * @return the number of combinations
	 */
	public static long getCardsGroupsCombinationsCount(int[] groupsSizes,
			int nbDeadCards) {
		long tot = 1;
		int deckSize = 52 - nbDeadCards;
		for (int gs : groupsSizes) {
			tot *= CombinatoricsUtils.binomialCoefficient(deckSize, gs);
			deckSize -= gs;
		}
		return tot;
	}
}
