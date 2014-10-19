package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TerminalNodes {
	/**
	 * {@link #showDownNodes}[showDownNodeIndex][potIndex][0] = potValue;<br>
	 * {@link #showDownNodes}[showDownNodeIndex][potIndex][i > 0] = id of the
	 * player in pot of index i;
	 */
	public final int[][][] showDownNodes;

	/**
	 * {@link #noShowDownNodes}[noShowDownNodeIndex][potIndex][0] = potValue;<br>
	 * {@link #noShowDownNodes}[noShowDownNodeIndex][potIndex][1] = id of the
	 * pot winning player;
	 */
	public final int[][][] noShowDownNodes;

	public int getNbShowdownNodes() {
		return showDownNodes.length;
	}

	public int getNbNoShowdownNodes() {
		return noShowDownNodes.length;
	}

	public int getNbNodes() {
		return showDownNodes.length + noShowDownNodes.length;
	}
}
