package net.funkyjava.gametheory.cscfrm.model.game;

import java.util.Iterator;

import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;


/**
 * The Interface CSCFRMGame is a {@link CSCFRMBaseGame} that provides an
 * iterator over its player nodes, making it available to sharing them or
 * loading and saving their states. For better performance, player nodes should
 * be stored in arrays. Look at ArraysIterators to easily build iterators over
 * arrays or multiple dimensions arrays.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player nodes type
 */
public interface CSCFRMGame<PNode extends PlayerNode> extends CSCFRMBaseGame {

	/**
	 * Gets the player nodes iterator.
	 * 
	 * @return the player nodes iterator
	 */
	Iterator<PNode> getPlayerNodesIterator();

}
