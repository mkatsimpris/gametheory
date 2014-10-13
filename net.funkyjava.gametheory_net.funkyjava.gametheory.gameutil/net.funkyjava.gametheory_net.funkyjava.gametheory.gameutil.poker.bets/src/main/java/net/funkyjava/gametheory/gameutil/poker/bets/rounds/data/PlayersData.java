/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.data;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Pierre Mardon
 * 
 */
@Data
@AllArgsConstructor
public class PlayersData {
	private boolean[] inHand;
	private int[] stacks;
	private int[] bets;

	public NoBetPlayersData getNoBetData() {
		return new NoBetPlayersData(stacks, inHand);
	}

	public PlayersData(NoBetPlayersData data) {
		this(data.getInHand(), data.getStacks(),
				new int[data.getStacks().length]);
	}
}
