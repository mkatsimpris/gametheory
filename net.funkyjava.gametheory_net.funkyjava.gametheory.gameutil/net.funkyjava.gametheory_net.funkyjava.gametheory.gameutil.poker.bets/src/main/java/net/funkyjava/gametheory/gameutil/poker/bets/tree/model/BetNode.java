package net.funkyjava.gametheory.gameutil.poker.bets.tree.model;

import lombok.Data;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLHandBetTreeBuilder;

/**
 * 
 * Abstraction of a bet node to build a bet tree with {@link NLHandBetTreeBuilder}
 * .
 * 
 * @author Pierre Mardon
 * 
 */
@Data
public class BetNode {
	private final int player;
	private final int[] bets;
	private final int[] nextNodes;

	/**
	 * Constructor
	 * 
	 * @param player
	 *            the player's index
	 * @param bets
	 *            the possible bets
	 */
	public BetNode(int player, int[] bets) {
		this.player = player;
		this.bets = bets.clone();
		nextNodes = new int[bets.length];
	}
}
