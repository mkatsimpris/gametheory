package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import lombok.AllArgsConstructor;

/**
 * Compact representation of a round bet tree. Look at {@link #offset} and
 * {@link #betNodes} doc to understand the expected structure
 * 
 * @author Pierre Mardon
 * 
 */
@AllArgsConstructor
public class RoundBetTree {

	/**
	 * Standard offset to differentiate nodes indexes as explained in
	 * {@link #betNodes} doc
	 */
	public static final int offset = Integer.MAX_VALUE / 2;

	/**
	 * int nbActions = ({@link #betNodes}[nodeIndex].length - 1) /2;<br>
	 * int betPlayer = {@link #betNodes}[nodeIndex][2 * nbActions];<br>
	 * int betValue = {@link #betNodes}[nodeIndex][nbActions + betIndex];<br>
	 * int nextNode = {@link #betNodes}[nodeIndex][betIndex];<br>
	 * <ul>
	 * <li>
	 * nextNode < -{@link #offset} : int showDownNodeIndex = nextNode + 2
	 * {@link #offset};</li>
	 * <li>
	 * -{@link #offset} <= nextNode < 0 : int noShowDownNodeIndex = nextNode +
	 * {@link #offset};</li>
	 * <li>
	 * 0 <= nextNode < {@link #offset} : int nextBetNode = nextNode;</li>
	 * <li>
	 * {@link #offset} <= nextNode : int nextRoundBetNodeIndex = nextNode -
	 * {@link #offset};
	 * <li/>
	 * </ul>
	 */
	public final int[][] betNodes;

	/**
	 * Get the number of bet nodes
	 * 
	 * @return the number of bet nodes
	 */
	public int getNbNodes() {
		return betNodes.length;
	}

	/**
	 * Get the number of edges
	 * 
	 * @return the number of edges
	 */
	public int getNbEdges() {
		int res = 0;
		for (int i = 0; i < betNodes.length; i++)
			res += betNodes[i].length / 2;
		return res;
	}

}
