/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * @author Pierre Mardon
 * 
 */
@Data
@AllArgsConstructor
public class NoBetPlayersData {
	@NonNull
	private int[] stacks;
	@NonNull
	private boolean[] inHand;
}
