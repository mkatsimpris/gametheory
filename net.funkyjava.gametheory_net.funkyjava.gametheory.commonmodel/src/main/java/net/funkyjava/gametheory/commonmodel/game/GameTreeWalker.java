package net.funkyjava.gametheory.commonmodel.game;

import net.funkyjava.gametheory.commonmodel.game.nodes.MinPublicNode;

/**
 * A {@link GameTreeWalker} is a {@link GameObserver} that provides a minimal
 * description of the current game tree node at any time.
 * 
 * @author Pierre Mardon
 */
public interface GameTreeWalker extends GameObserver {

	/**
	 * Gets the current node.
	 * 
	 * @return the current node
	 */
	MinPublicNode getCurrentNode();
}
