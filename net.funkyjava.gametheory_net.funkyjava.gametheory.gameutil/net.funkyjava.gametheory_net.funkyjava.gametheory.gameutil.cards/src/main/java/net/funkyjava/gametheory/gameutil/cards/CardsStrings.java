/**
 * 
 */
package net.funkyjava.gametheory.gameutil.cards;

/**
 * This class and {@link IntCardsSpec} intend to standardize the string
 * representation of int cards.
 * 
 * @author Pierre Mardon
 * 
 */
public class CardsStrings {

	/**
	 * The ranks strings
	 */
	public static final String[] ranks = { "2", "3", "4", "5", "6", "7", "8",
			"9", "T", "J", "Q", "K", "A" };
	/**
	 * The colors strings
	 */
	public static final String[] colors = { "c", "d", "h", "s" };

	/**
	 * The cards specifications
	 */
	private final IntCardsSpec spec;

	/**
	 * @param spec
	 *            The cards specifications
	 * 
	 */
	public CardsStrings(IntCardsSpec spec) {
		this.spec = spec;
	}

	/**
	 * Gets the card's rank string.
	 * 
	 * @param card
	 *            the int card compliant with the spec
	 * @return the rank string
	 */
	public String getRankStr(int card) {
		return ranks[spec.getStandardRank(card)];
	}

	/**
	 * Gets the card's color string.
	 * 
	 * @param card
	 *            the int card compliant with the spec
	 * @return the color string
	 */
	public String getColorStr(int card) {
		return colors[spec.getStandardColor(card)];
	}

	/**
	 * Gets the card's string.
	 * 
	 * @param card
	 *            the int card compliant with the spec
	 * @return the card's string
	 */
	public String getStr(int card) {
		return ranks[spec.getStandardRank(card)]
				+ colors[spec.getStandardColor(card)];
	}
}
