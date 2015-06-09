package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class NLRoundStartingNodesTreeReducer implements
		NLBetTreeBuildingReducer {

	private HashMap<Integer, List<NLBetTreeNode>> roundsStartingNodes = new HashMap<Integer, List<NLBetTreeNode>>();

	private List<NLBetTreeNode> showdownNodes = new LinkedList<>();
	private List<NLBetTreeNode> noShowdownNodes = new LinkedList<>();

	@Override
	public NLBetTreeNode findEquivalent(NLBetTreeNode sourceNode) {
		checkNotNull(sourceNode, "Provided NLBetTreeNode is null");
		if (sourceNode.isBetNode())
			return findBetNodeEquivalent(sourceNode);
		if (sourceNode.isShowDownNode())
			return findShowdownNodeEquivalent(sourceNode);
		if (sourceNode.isNoShowDownNode())
			return findNoShowdownNodeEquivalent(sourceNode);
		throw new IllegalArgumentException(
				"The provided NLBetTreeNode is not a bet node, not a showdown node and not a \"no-showdown\" node");
	}

	private NLBetTreeNode findNoShowdownNodeEquivalent(NLBetTreeNode sourceNode) {
		for (NLBetTreeNode node : noShowdownNodes) {
			if (sourceNode.player == node.player
					&& Arrays.equals(sourceNode.getValues(), node.getValues())
					&& Arrays.equals(sourceNode.getStacks(), node.getStacks()))
				return node;
		}
		noShowdownNodes.add(sourceNode);
		return sourceNode;
	}

	private NLBetTreeNode findShowdownNodeEquivalent(NLBetTreeNode sourceNode) {
		for (NLBetTreeNode node : showdownNodes) {
			if (Arrays.deepEquals(sourceNode.getShowdownPlayers(),
					node.getShowdownPlayers())
					&& Arrays.equals(sourceNode.getValues(), node.getValues()))
				return node;
		}
		showdownNodes.add(sourceNode);
		return sourceNode;
	}

	private NLBetTreeNode findBetNodeEquivalent(NLBetTreeNode sourceNode) {
		// We only check the first bet nodes of each round
		if (sourceNode.getHand().getCurrentRoundBetMoves().isEmpty())
			return sourceNode;
		List<NLBetTreeNode> startingNodes = roundsStartingNodes.get(sourceNode
				.getBetRoundIndex());
		if (startingNodes == null) {
			roundsStartingNodes.put(sourceNode.getBetRoundIndex(),
					startingNodes = new LinkedList<>());
		}
		for (NLBetTreeNode node : startingNodes) {
			if (areBetNodeEquivalent(node, sourceNode))
				return node;
		}
		startingNodes.add(sourceNode);
		return sourceNode;
	}

	private static boolean areBetNodeEquivalent(NLBetTreeNode node,
			NLBetTreeNode sourceNode) {
		// We are guaranteed to be in the same round
		return node.getPlayer() == sourceNode.getPlayer()
				&& Arrays.equals(node.getValues(), sourceNode.getValues());
	}

}
