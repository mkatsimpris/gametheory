/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.data;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The players data in a round. Contains in-hand state, stacks and bets.
 * 
 * @author Pierre Mardon
 * 
 */
@Data
@AllArgsConstructor
public class PlayersData {
	private boolean[] inHand;
	private int[] stacks;
	private int[] bets;

	/**
	 * Get the data without bets
	 * 
	 * @return the data without bets
	 */
	public NoBetPlayersData getNoBetData() {
		return new NoBetPlayersData(stacks, inHand);
	}

	/**
	 * Constructor based on data without bets
	 * 
	 * @param data
	 *            the no-bet data
	 */
	public PlayersData(NoBetPlayersData data) {
		this(data.getInHand(), data.getStacks(),
				new int[data.getStacks().length]);
	}
}
