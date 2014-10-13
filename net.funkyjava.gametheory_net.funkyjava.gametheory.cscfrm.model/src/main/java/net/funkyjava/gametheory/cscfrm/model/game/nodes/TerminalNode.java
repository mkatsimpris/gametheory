package net.funkyjava.gametheory.cscfrm.model.game.nodes;

/**
 * The TerminalNode class.
 * 
 * @author Pierre Mardon
 */
public class TerminalNode extends Node {

	/**
	 * The simple constructor.
	 * 
	 * @param payoffs
	 *            the payoffs
	 */
	protected TerminalNode(double[] payoffs) {
		super(payoffs);
	}

	/**
	 * The constructor to build a terminal node that has payoffs AND refers to
	 * specific a terminal state by its id.
	 * 
	 * @param payoffs
	 *            the payoffs
	 * @param id
	 *            the terminal state id
	 */
	protected TerminalNode(double[] payoffs, int id) {
		super(payoffs, id);
	}

	/**
	 * The constructor to build a terminal node that refers to a specific
	 * terminal state by its id.
	 * 
	 * @param id
	 *            the id
	 */
	protected TerminalNode(int id) {
		super(null, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.nodes.base.Node#getPayoffs()
	 */
	@Override
	public double[] getPayoffs() {
		return payoffs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.nodes.base.Node#getId()
	 */
	@Override
	public int getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.nodes.base.Node#lock()
	 */
	@Override
	public void lock() throws InterruptedException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.nodes.base.Node#unlock()
	 */
	@Override
	public void unlock() {

	}
}
