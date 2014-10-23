package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A {@link BettingTree} contains every {@link RoundBetTree} of a hand and its
 * {@link TerminalNodes}
 * 
 * @author Pierre Mardon
 * 
 */
@Data
@AllArgsConstructor
public class BettingTree {
	private RoundBetTree[] roundBetTrees;
	private TerminalNodes termNodes;
}
