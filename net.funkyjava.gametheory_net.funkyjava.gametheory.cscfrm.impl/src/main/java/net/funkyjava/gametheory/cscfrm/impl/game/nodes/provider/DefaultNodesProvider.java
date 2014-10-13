package net.funkyjava.gametheory.cscfrm.impl.game.nodes.provider;

import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultChanceNode;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultTerminalNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.ChanceNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.TerminalNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;


/**
 * The default {@link NodesProvider} implementation making games using it
 * available to mono and multi-threading CSCFRM.
 * 
 * @author Pierre Mardon
 */
public class DefaultNodesProvider implements NodesProvider<DefaultPlayerNode> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.nodes.provider.itf.NodesProvider#getChanceNode
	 * ()
	 */
	@Override
	public ChanceNode getChanceNode() {
		return new DefaultChanceNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.nodes.provider.itf.NodesProvider#getPlayerNode
	 * (int, int)
	 */
	@Override
	public DefaultPlayerNode getPlayerNode(int player, int nbPlayerActions) {
		return new DefaultPlayerNode(player, nbPlayerActions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.nodes.provider.itf.NodesProvider#getTerminalNode
	 * (double[], int)
	 */
	@Override
	public TerminalNode getTerminalNode(double[] payoffs, int id) {
		return new DefaultTerminalNode(payoffs, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.nodes.provider.itf.NodesProvider#getTerminalNode
	 * (double[])
	 */
	@Override
	public TerminalNode getTerminalNode(double[] payoffs) {
		return new DefaultTerminalNode(payoffs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.nodes.provider.itf.NodesProvider#getTerminalNode
	 * (int)
	 */
	@Override
	public TerminalNode getTerminalNode(int id) {
		return new DefaultTerminalNode(id);
	}

}
