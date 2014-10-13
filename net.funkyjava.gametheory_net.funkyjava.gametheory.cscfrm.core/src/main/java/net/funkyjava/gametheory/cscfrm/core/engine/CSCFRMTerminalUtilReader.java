package net.funkyjava.gametheory.cscfrm.core.engine;

import net.funkyjava.gametheory.commonmodel.game.nodes.MinPublicNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;


/**
 * Interface to bind terminal states of a game to payoffs. See
 * {@link MinPublicNode#getId()} and {@link Node#getId()}. When provided to the
 * {@link CSCFRMEngine} via {@link CSCFRMConfig} using
 * {@link CSCFRMEngine#setConfig(CSCFRMConfig)}, the engine will call
 * {@link #read(int, double[])} with the terminal node id as first argument when
 * it's >= 0.
 * 
 * @author Pierre Mardon
 */
public interface CSCFRMTerminalUtilReader {

	/**
	 * Read.
	 * 
	 * @param id
	 *            the id
	 * @param dest
	 *            the dest
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	void read(int id, double[] dest) throws InterruptedException;
}
