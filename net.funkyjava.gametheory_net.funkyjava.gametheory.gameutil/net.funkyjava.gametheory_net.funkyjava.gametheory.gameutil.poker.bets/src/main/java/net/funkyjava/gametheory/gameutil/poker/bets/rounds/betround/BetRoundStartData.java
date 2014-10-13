/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround;

import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayersData;
import lombok.AllArgsConstructor;
import lombok.Delegate;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Pierre Mardon
 * 
 */
@AllArgsConstructor
public class BetRoundStartData {
	@NonNull
	@Delegate
	private final PlayersData playersData;
	@Getter
	private final int firstPlayerIndex;
	@Getter
	private final int bigBlind;
}
