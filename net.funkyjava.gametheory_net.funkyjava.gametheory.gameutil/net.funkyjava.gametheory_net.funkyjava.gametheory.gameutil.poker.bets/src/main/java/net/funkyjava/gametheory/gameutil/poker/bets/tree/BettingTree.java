package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BettingTree {
	private RoundBetTree[] roundBetTrees;
	private TerminalNodes termNodes;
}
