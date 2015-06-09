package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleNLBetTreePrinter implements SimpleNLBetTreeWalker {

	@Override
	public boolean handleCurrentNode(NLBetTreeNode node, int depth, int lastBet) {
		String str = "";
		for (int i = 0; i < depth - 1; i++)
			str += "\t|";
		if (depth > 0) {
			str += "\t|__>";
			String lastBetStr = lastBet == NLBetRangeSlicer.fold ? "Fold" : ""
					+ lastBet;
			str += lastBetStr + " : ";
		}
		if (node.isBetNode()) {
			str += "Player(" + node.getPlayer() + ")";
		} else if (node.isNoShowDownNode()) {
			str += "Player (" + node.getPlayer() + ") wins "
					+ Arrays.toString(node.getValues()) + " stacks "
					+ Arrays.toString(node.getStacks());
		} else if (node.isShowDownNode()) {
			str += "Showdown. Pots " + Arrays.toString(node.getValues())
					+ " Pots players "
					+ Arrays.deepToString(node.getShowdownPlayers());
		}
		log.info(str);
		return true;
	}

}
