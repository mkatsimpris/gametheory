package net.funkyjava.gametheory.gameutil.poker.bets.tree;

/**
 * A tree reducer has an unique method to find an equivalent to a given node.
 * This method will be called by {@link NLHandBetTreeBuilder} while building the
 * tree recursively. It is guaranteed that the provide source node has its
 * children filled.
 * 
 * The implementation must always return a not-null {@link NLBetTreeNode} that
 * has matching characteristics (number of possible bets and their values,
 * active player, pots and pots player if any). Also it is required to have its
 * children recursively built. This is important as the coherence will not be
 * checked and can result in an exception.
 * 
 * The basic expected behavior is to remember source nodes that are eligible to
 * be an equivalent to potential future nodes, and check if they are when other
 * source nodes are provided.
 * 
 * @author Pierre Mardon
 * 
 */
public interface NLBetTreeBuildingReducer {

	/**
	 * Returns a not-null and coherent equivalent to the provided source node.
	 * 
	 * @param sourceNode
	 *            the source node
	 * @return a not null equivalent, by default should be the source node
	 */
	NLBetTreeNode findEquivalent(NLBetTreeNode sourceNode);

}
