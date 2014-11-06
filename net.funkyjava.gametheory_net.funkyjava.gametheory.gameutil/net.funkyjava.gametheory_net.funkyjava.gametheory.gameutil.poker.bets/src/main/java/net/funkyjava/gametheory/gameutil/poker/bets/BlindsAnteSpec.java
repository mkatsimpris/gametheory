/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets;

import lombok.Getter;
import lombok.experimental.Builder;

/**
 * @author Pierre Mardon
 * 
 */
@Builder
public class BlindsAnteSpec {

	/**
	 * Enable ante
	 */
	@Getter
	private final boolean enableAnte;

	/**
	 * Enable blinds
	 */
	@Getter
	private final boolean enableBlinds;

	/**
	 * Is cash game
	 */
	@Getter
	private final boolean isCash;

	/**
	 * Index of the small blind player
	 */
	@Getter
	private final int sbIndex;

	/**
	 * Index of the big blind player
	 */
	@Getter
	private final int bbIndex;

	/**
	 * Small blind value
	 */
	@Getter
	private final int sbValue;

	/**
	 * Big blind value
	 */
	@Getter
	private final int bbValue;

	/**
	 * Ante value
	 */
	@Getter
	private final int anteValue;

	/**
	 * First player after blinds have been payed
	 */
	@Getter
	private final int firstPlayerAfterBlinds;

	/**
	 * For each player index, specifies if this player has to pay a big blind to
	 * enter the game
	 */
	@Getter
	private final boolean[] shouldPostEnteringBb;

}
