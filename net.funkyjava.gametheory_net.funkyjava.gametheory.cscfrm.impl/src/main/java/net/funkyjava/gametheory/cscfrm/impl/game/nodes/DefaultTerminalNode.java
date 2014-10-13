package net.funkyjava.gametheory.cscfrm.impl.game.nodes;

import net.funkyjava.gametheory.cscfrm.model.game.nodes.TerminalNode;


/**
 * The DefaultTerminalNode class.
 * 
 * @author Pierre Mardon
 */
public final class DefaultTerminalNode extends TerminalNode {

	/**
	 * The constructor for a classic terminal node, providing only payoffs.
	 * 
	 * @param payoffs
	 *            the payoffs
	 */
	public DefaultTerminalNode(double[] payoffs) {
		super(payoffs);
	}

	/**
	 * The constructor for a terminal node that can provide both direct payoffs
	 * or indirect by providing an id referring to some custom payoffs
	 * computing.
	 * 
	 * @param payoffs
	 *            the payoffs
	 * @param id
	 *            the id
	 */
	public DefaultTerminalNode(double[] payoffs, int id) {
		super(payoffs, id);
	}

	/**
	 * The constructor for a terminal node that provides only an id referring to
	 * a custom payoffs computing.
	 * 
	 * @param id
	 *            the id
	 */
	public DefaultTerminalNode(int id) {
		super(id);
	}

}
