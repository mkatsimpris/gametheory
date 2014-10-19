package net.funkyjava.gametheory.gameutil.poker.bets.tree.model;

import lombok.Data;

@Data
public class BetNode {
	private final int player;
	private final int[] bets;
	private final int[] nextNodes;

	public BetNode(int player, int[] bets) {
		this.player = player;
		this.bets = bets.clone();
		nextNodes = new int[bets.length];
	}
}
