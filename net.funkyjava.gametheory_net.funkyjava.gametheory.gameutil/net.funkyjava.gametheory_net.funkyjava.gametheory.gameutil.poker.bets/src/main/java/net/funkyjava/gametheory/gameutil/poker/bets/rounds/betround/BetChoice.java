/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents a choice a player has to make, containing all options : bet, raise
 * call but fold (always available)
 * 
 * @author Pierre Mardon
 * 
 */
@AllArgsConstructor
public class BetChoice {

	/**
	 * Constructor
	 */
	public BetChoice() {
		this.betRange = BetRange.getNoRange();
		this.raiseRange = RaiseRange.getNoRange();
		this.callValue = CallValue.getNoCall();
		this.player = -1;
	}

	@Getter
	@NonNull
	private final BetRange betRange;

	@Getter
	@NonNull
	private final CallValue callValue;

	@Getter
	@NonNull
	private final RaiseRange raiseRange;

	@Getter
	private final int player;

	/**
	 * Check if this choice is valid
	 * 
	 * @return true when valid
	 */
	public boolean exists() {
		return player >= 0 && callValue.isValid();
	}
}
