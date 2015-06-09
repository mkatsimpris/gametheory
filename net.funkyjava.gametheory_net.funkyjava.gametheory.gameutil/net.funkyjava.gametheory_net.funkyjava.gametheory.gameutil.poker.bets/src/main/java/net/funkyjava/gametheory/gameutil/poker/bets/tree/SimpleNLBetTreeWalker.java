package net.funkyjava.gametheory.gameutil.poker.bets.tree;

public interface SimpleNLBetTreeWalker {

	boolean handleCurrentNode(NLBetTreeNode node, int depth, int lastBet);

}
