package net.funkyjava.gametheory.cscfrm.model.game;

import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;

/**
 * A CSCFRMGameBuilder provides methods to build a game or a copy of a game
 * providing it. A game and all of its copies should be state-independent
 * regarding the others but the access to their player nodes {@link Node#lock()}
 * and {@link Node#unlock()} method should ensure an exclusive write access to
 * them for the CSCFRM algorithm.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the players nodes type
 * @param <GameClass>
 *            the game type
 */
public interface CSCFRMGameBuilder<PNode extends PlayerNode, GameClass extends CSCFRMGame<PNode>> {

	/**
	 * Gets the game.
	 * 
	 * @param nodesProvider
	 *            the nodes provider that should create all nodes needed by the
	 *            game.
	 * @return the game
	 */
	GameClass getGame(NodesProvider<PNode> nodesProvider);

	/**
	 * Gets a sharing game.
	 * 
	 * @param nodesProvider
	 *            the nodes provider that should create all remaining nodes
	 *            needed by the game.
	 * @param source
	 *            the source game whose player nodes must be shared accordingly
	 *            to their {@link Node#lock()} and {@link Node#unlock()}
	 *            methods.
	 * @return the sharing game
	 */
	public GameClass getSharingGame(NodesProvider<PNode> nodesProvider,
			GameClass source);
}
