package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import java.util.LinkedList;
import java.util.List;

import net.funkyjava.gametheory.gameutil.poker.bets.pots.Pot;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.model.PotsNodes;

public class TerminalNodesBuilder {
	private final List<PotsNodes> showDownNodes = new LinkedList<>();
	private final List<PotsNodes> noShowDownNodes = new LinkedList<>();

	public int findOrCreateShowDownNode(PotsNodes node) {
		int index = showDownNodes.indexOf(node);
		if (index >= 0)
			return index;
		showDownNodes.add(node);
		return showDownNodes.size() - 1;
	}

	public int findOrCreateNoShowDownNode(PotsNodes node) {
		int index = noShowDownNodes.indexOf(node);
		if (index >= 0)
			return index;
		noShowDownNodes.add(node);
		return noShowDownNodes.size() - 1;
	}

	public int[][][] getShowDownNodes() {
		return getNodes(showDownNodes);
	}

	public int[][][] getNoShowDownNodes() {
		return getNodes(noShowDownNodes);
	}

	private static int[][][] getNodes(List<PotsNodes> nodes) {
		int[][][] res = new int[nodes.size()][][];
		for (int i = 0; i < res.length; i++) {
			PotsNodes node = nodes.get(i);
			int nbPots = node.getPots().size();
			res[i] = new int[nbPots][];
			for (int j = 0; j < nbPots; j++) {
				Pot<Integer> pot = node.getPots().get(j);
				final int nbPlayers = pot.getPlayers().size();
				final int[] arr = res[i][j] = new int[nbPlayers + 1];
				for (int k = 0; k < nbPlayers; k++)
					arr[k] = pot.getPlayers().get(k);
				arr[nbPlayers] = pot.getValue();
			}
		}
		return res;
	}

	public TerminalNodes build() {
		return new TerminalNodes(getShowDownNodes(), getNoShowDownNodes());
	}
}
