package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

public class NLIndexedBetTree {

	private final NLBetTreeNode rootNode;
	private final int nbOfBetRounds;

	private List<List<NLBetTreeNode>> betNodes = new ArrayList<>();
	private List<List<NLBetTreeNode>> showdownNodes = new ArrayList<>();
	private List<List<NLBetTreeNode>> noShowdownNodes = new ArrayList<>();

	public NLIndexedBetTree(NLBetTreeNode rootNode) {
		this.rootNode = checkNotNull(rootNode);
		nbOfBetRounds = rootNode.getHand().getNbBetRounds();
		checkArgument(nbOfBetRounds > 0, "Number of bet rounds should be > 0");
		for (int i = 0; i < nbOfBetRounds; i++) {
			betNodes.add(new ArrayList<NLBetTreeNode>());
			showdownNodes.add(new ArrayList<NLBetTreeNode>());
			noShowdownNodes.add(new ArrayList<NLBetTreeNode>());
		}
		indexRecursive(rootNode);
	}

	private void indexRecursive(NLBetTreeNode node) {
		checkArgument(
				node.getBetRoundIndex() < nbOfBetRounds
						&& node.getBetRoundIndex() >= 0,
				"Node's bet round index is wrong !");
		// This node was already indexed
		if (node.getRoundNodeIndex() >= 0)
			return;
		if (node.isBetNode()) {
			node.setRoundNodeIndex(betNodes.get(node.getBetRoundIndex()).size());
			betNodes.get(node.getBetRoundIndex()).add(node);
			for (int i = 0; i < node.getChildrenCount(); i++) {
				indexRecursive(node.getChild(i));
			}
			return;
		}
		if (node.isNoShowDownNode()) {
			node.setRoundNodeIndex(noShowdownNodes.get(node.getBetRoundIndex())
					.size());
			noShowdownNodes.get(node.getBetRoundIndex()).add(node);
			return;
		}
		if (node.isShowDownNode()) {
			node.setRoundNodeIndex(showdownNodes.get(node.getBetRoundIndex())
					.size());
			showdownNodes.get(node.getBetRoundIndex()).add(node);
			return;
		}
	}

	public int getNbOfNodesForBetRound(int betRoundIndex) {
		if (betRoundIndex < 0 || betRoundIndex >= nbOfBetRounds) {
			return 0;
		}
		return betNodes.get(betRoundIndex).size()
				+ showdownNodes.get(betRoundIndex).size()
				+ noShowdownNodes.get(betRoundIndex).size();
	}

	public int getNbOfBetNodesForBetRound(int betRoundIndex) {
		if (betRoundIndex < 0 || betRoundIndex >= nbOfBetRounds) {
			return 0;
		}
		return betNodes.get(betRoundIndex).size();
	}

	public int getNbOfShowdownNodesForBetRound(int betRoundIndex) {
		if (betRoundIndex < 0 || betRoundIndex >= nbOfBetRounds) {
			return 0;
		}
		return showdownNodes.get(betRoundIndex).size();
	}

	public int getNbOfNoShowdownNodesForBetRound(int betRoundIndex) {
		if (betRoundIndex < 0 || betRoundIndex >= nbOfBetRounds) {
			return 0;
		}
		return noShowdownNodes.get(betRoundIndex).size();
	}

	public int getNbOfBetNodes() {
		int res = 0;
		for (int i = 0; i < nbOfBetRounds; i++)
			res += betNodes.get(i).size();
		return res;
	}

	public int getNbOfShowdownNodes() {
		int res = 0;
		for (int i = 0; i < nbOfBetRounds; i++)
			res += showdownNodes.get(i).size();
		return res;
	}

	public int getNbOfNoShowdownNodes() {
		int res = 0;
		for (int i = 0; i < nbOfBetRounds; i++)
			res += noShowdownNodes.get(i).size();
		return res;
	}

	public NLBetTreeNode getRootNode() {
		return rootNode;
	}

	public NLBetTreeNode getBetNode(int betRoundIndex, int roundBetNodeIndex) {
		if (betRoundIndex < 0 || betRoundIndex >= nbOfBetRounds) {
			return null;
		}
		if (roundBetNodeIndex < 0
				|| betNodes.get(betRoundIndex).size() <= roundBetNodeIndex) {
			return null;
		}
		return betNodes.get(betRoundIndex).get(roundBetNodeIndex);
	}

	public NLBetTreeNode getShowdownNode(int betRoundIndex,
			int roundBetNodeIndex) {
		if (betRoundIndex < 0 || betRoundIndex >= nbOfBetRounds) {
			return null;
		}
		if (roundBetNodeIndex < 0
				|| showdownNodes.get(betRoundIndex).size() <= roundBetNodeIndex) {
			return null;
		}
		return showdownNodes.get(betRoundIndex).get(roundBetNodeIndex);
	}

	public NLBetTreeNode getNoShowdownNode(int betRoundIndex,
			int roundBetNodeIndex) {
		if (betRoundIndex < 0 || betRoundIndex >= nbOfBetRounds) {
			return null;
		}
		if (roundBetNodeIndex < 0
				|| noShowdownNodes.get(betRoundIndex).size() <= roundBetNodeIndex) {
			return null;
		}
		return noShowdownNodes.get(betRoundIndex).get(roundBetNodeIndex);
	}

}
