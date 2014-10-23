package net.funkyjava.gametheory.cscfrm.impl.game.nodes;

import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;

/**
 * The DefaultPlayerNode class.
 * 
 * @author Pierre Mardon
 */
public final class DefaultPlayerNode extends PlayerNode {
	/**
	 * Locked boolean for default {@link Node#lock()} and {@link Node#unlock()}
	 * methods.
	 */
	private boolean locked = false;

	/**
	 * Constructor.
	 * 
	 * @param player
	 *            the player index
	 * @param nbPlayerActions
	 *            the number of player actions
	 */
	public DefaultPlayerNode(int player, int nbPlayerActions) {
		super(player, nbPlayerActions);
	}

	@Override
	public synchronized void lock() throws InterruptedException {
		while (locked)
			this.wait();
		locked = true;
	}

	@Override
	public synchronized void unlock() {
		locked = false;
		this.notify();
	}
}
