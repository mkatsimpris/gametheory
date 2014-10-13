package net.funkyjava.gametheory.cscfrm.model.game.nodes.provider;

import net.funkyjava.gametheory.cscfrm.model.game.nodes.ChanceNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.TerminalNode;;

/**
 * A NodesProvider is a {@link Node} factory. It is intended to separate game
 * implementation and nodes implementation
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the players nodes type
 */
public interface NodesProvider<PNode extends PlayerNode> {

	/**
	 * Gets a new chance node.
	 * 
	 * @return the chance node
	 */
	ChanceNode getChanceNode();

	/**
	 * Gets a new player node.
	 * 
	 * @param player
	 *            the player
	 * @param nbPlayerActions
	 *            the player actions number
	 * @return the player node
	 */
	PNode getPlayerNode(int player, int nbPlayerActions);

	/**
	 * Gets a new the terminal node that has payoffs AND refers to specific a
	 * terminal state by its id.
	 * 
	 * @param payoffs
	 *            the payoffs
	 * @param id
	 *            the id
	 * @return the terminal node
	 */
	TerminalNode getTerminalNode(double[] payoffs, int id);

	/**
	 * Gets a new terminal node that has payoffs.
	 * 
	 * @param payoffs
	 *            the payoffs
	 * @return the terminal node
	 */
	TerminalNode getTerminalNode(double[] payoffs);

	/**
	 * Gets a new terminal node that refers to specific a terminal state by its
	 * id.
	 * 
	 * @param id
	 *            the id
	 * @return the terminal node
	 */
	TerminalNode getTerminalNode(int id);
}
