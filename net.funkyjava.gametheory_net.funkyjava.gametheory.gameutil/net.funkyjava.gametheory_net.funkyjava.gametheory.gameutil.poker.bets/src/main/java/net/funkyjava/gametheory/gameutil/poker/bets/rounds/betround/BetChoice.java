/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Pierre Mardon
 * 
 */
@AllArgsConstructor
public class BetChoice {

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

	public boolean exists() {
		return player >= 0 && callValue.isValid();
	}
}
