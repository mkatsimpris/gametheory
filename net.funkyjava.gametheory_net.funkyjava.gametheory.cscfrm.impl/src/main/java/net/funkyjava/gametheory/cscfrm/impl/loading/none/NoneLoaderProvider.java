package net.funkyjava.gametheory.cscfrm.impl.loading.none;

import java.io.IOException;

import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMCtxExecutionLoaderProvider;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoader;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;


/**
 * The NoneLoaderProvider class is a void implementation of
 * {@link CSCFRMCtxExecutionLoaderProvider}.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player node type
 */
public class NoneLoaderProvider<PNode extends PlayerNode> implements
		CSCFRMCtxExecutionLoaderProvider<PNode> {

	/**
	 * The Constructor.
	 */
	public NoneLoaderProvider() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.loading.itf.CSCFRMCtxExecutionLoaderProvider#
	 * getSubCtxProvider(java.lang.String)
	 */
	@Override
	public NoneLoaderProvider<PNode> getSubCtxProvider(String subCtxId)
			throws IOException {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.loading.itf.CSCFRMCtxExecutionLoaderProvider#getLoader
	 * (java.lang.String,
	 * net.funkyjava.cscfrm.loading.itf.CSCFRMExecutionLoaderConfig)
	 */
	@Override
	public CSCFRMExecutionLoader<PNode> getLoader(String gameId,
			CSCFRMExecutionLoaderConfig config) throws IOException {
		return new NoneGameLoader<PNode>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.loading.itf.CSCFRMCtxExecutionLoaderProvider#clear()
	 */
	@Override
	public void clear() throws IOException {

	}

}
