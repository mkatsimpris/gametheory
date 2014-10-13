package net.funkyjava.gametheory.cscfrm.core.loading;

import java.io.IOException;
import java.util.Iterator;

import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMState;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMUtilityManager;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;


/**
 * A CSCFRMExecutionLoader provides methods to both load and save an CFRM
 * process state : the {@link CSCFRMState} of an {@link CSCFRMUtilityManager}
 * and an enumeration of player nodes.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player nodes type
 */
public interface CSCFRMExecutionLoader<PNode extends PlayerNode> {

	/**
	 * Can load. No loading method should be called when returning false.
	 * 
	 * @return true, if the loader is able to load both the state and the player
	 *         nodes.
	 */
	boolean canLoad();

	/**
	 * Load the player nodes.
	 * 
	 * @param nodes
	 *            the player nodes iterator
	 * @throws IOException
	 *             the IO exception
	 */
	void loadPlayerNodes(Iterator<PNode> nodes) throws IOException;

	/**
	 * Load CSCFRM state.
	 * 
	 * @return the CSCFRM state
	 * @throws IOException
	 *             the IO exception
	 */
	CSCFRMState loadState() throws IOException;

	/**
	 * Save both the player nodes and the CSCFRM state.
	 * 
	 * @param nodes
	 *            the nodes
	 * @param state
	 *            the state
	 * @throws IOException
	 *             the IO exception
	 */
	void save(Iterator<PNode> nodes, CSCFRMState state) throws IOException;
}
