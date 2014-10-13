package net.funkyjava.gametheory.cscfrm.impl.loading.none;

import java.io.IOException;
import java.util.Iterator;

import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMState;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoader;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;


/**
 * The NoneGameLoader is a void implementation of {@link CSCFRMExecutionLoader}.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the generic type
 */
public class NoneGameLoader<PNode extends PlayerNode> implements
		CSCFRMExecutionLoader<PNode> {

	/**
	 * The Constructor.
	 */
	public NoneGameLoader() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.loading.itf.CSCFRMExecutionLoader#canLoad()
	 */
	@Override
	public boolean canLoad() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.loading.itf.CSCFRMExecutionLoader#loadPlayerNodes
	 * (java.util.Enumeration)
	 */
	@Override
	public void loadPlayerNodes(Iterator<PNode> nodes) throws IOException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.loading.itf.CSCFRMExecutionLoader#loadState()
	 */
	@Override
	public CSCFRMState loadState() throws IOException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.loading.itf.CSCFRMExecutionLoader#save(java.util
	 * .Enumeration, net.funkyjava.cscfrm.engine.CSCFRMState)
	 */
	@Override
	public void save(Iterator<PNode> nodes, CSCFRMState state)
			throws IOException {

	}

}
