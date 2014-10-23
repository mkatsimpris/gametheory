package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import lombok.AllArgsConstructor;

/**
 * Terminal nodes compact representation
 * 
 * @see TerminalNodes#showDownNodes
 * @see TerminalNodes#noShowDownNodes
 * @author Pierre Mardon
 * 
 */
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

	/**
	 * Get the number of showdown nodes
	 * 
	 * @return the number of showdown nodes
	 */
	public int getNbShowdownNodes() {
		return showDownNodes.length;
	}

	/**
	 * Get the number of no-showdown nodes
	 * 
	 * @return the number of no-showdown nodes
	 */
	public int getNbNoShowdownNodes() {
		return noShowDownNodes.length;
	}

	/**
	 * Get the total number of nodes
	 * 
	 * @return the total number of nodes
	 */
	public int getNbNodes() {
		return showDownNodes.length + noShowDownNodes.length;
	}
}
