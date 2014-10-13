/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.data;

import net.funkyjava.gametheory.gameutil.poker.bets.BlindsAnteSpec;
import lombok.AllArgsConstructor;
import lombok.Delegate;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Pierre Mardon
 * 
 */
@AllArgsConstructor
public class BlindsAnteParameters {

	@Getter
	@NonNull
	@Delegate
	private final NoBetPlayersData playersData;
	@Getter
	@NonNull
	@Delegate
	private final BlindsAnteSpec specs;
}
