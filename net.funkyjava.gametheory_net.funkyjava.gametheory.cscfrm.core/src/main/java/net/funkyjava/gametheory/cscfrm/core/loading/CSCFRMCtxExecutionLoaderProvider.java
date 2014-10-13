package net.funkyjava.gametheory.cscfrm.core.loading;

import java.io.IOException;

import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;


/**
 * A CSCFRMCtxExecutionLoaderProvider is a contextual
 * {@link CSCFRMExecutionLoader} builder where context is structured like an OS
 * path. It must provide access to any sub-context given an id string via
 * {@link #getSubCtxProvider(String)}. It must also provide a method
 * {@link #clear()} to enable clearing all previously saved data in the current
 * context.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the generic type
 */
public interface CSCFRMCtxExecutionLoaderProvider<PNode extends PlayerNode> {

	/**
	 * Gets the loader. It is recommended that the game record location strictly
	 * depends on the configuration
	 * 
	 * @param gameId
	 *            the game id
	 * @param config
	 *            the loader's configuration
	 * @return the loader
	 * @throws IOException
	 *             the IO exception
	 */
	CSCFRMExecutionLoader<PNode> getLoader(String gameId,
			CSCFRMExecutionLoaderConfig config) throws IOException;

	/**
	 * Gets the sub-context provider.
	 * 
	 * @param subCtxId
	 *            the sub context id
	 * @return the sub context provider
	 * @throws IOException
	 *             the IO exception
	 */
	CSCFRMCtxExecutionLoaderProvider<PNode> getSubCtxProvider(String subCtxId)
			throws IOException;

	/**
	 * Clear the current context's data.
	 * 
	 * @throws IOException
	 *             the IO exception
	 */
	void clear() throws IOException;
}
