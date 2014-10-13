package net.funkyjava.gametheory.cscfrm.games.kuhnpoker;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;

/**
 * The KuhnPoker game Builder.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player nodes type
 */
public class KuhnPokerBuilder<PNode extends PlayerNode> implements
		CSCFRMGameBuilder<PNode, KuhnPoker<PNode>> {

	/** Is sng. */
	private final boolean isSng;

	/** The players stacks number of blinds. */
	private final int nbBlindsP1, nbBlindsP2;

	/**
	 * The Constructor for classic payoffs (not SNG).
	 */
	public KuhnPokerBuilder() {
		isSng = false;
		this.nbBlindsP1 = 0;
		this.nbBlindsP2 = 0;
	}

	/**
	 * The Constructor for SNG
	 * 
	 * @param nbBlindsP1
	 *            the first player's number of blinds
	 * @param nbBlindsP2
	 *            the second player's number of blinds
	 */
	public KuhnPokerBuilder(int nbBlindsP1, int nbBlindsP2) {
		checkArgument(nbBlindsP1 > 2 && nbBlindsP2 > 2,
				"Every player must have at least 3 blinds to play Kuhn poker SNG");
		isSng = true;
		this.nbBlindsP1 = nbBlindsP1;
		this.nbBlindsP2 = nbBlindsP2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.CSCFRMGameBuilder#getGame(net.funkyjava
	 * .cscfrm.game.nodes.provider.itf.NodesProvider)
	 */
	@Override
	public KuhnPoker<PNode> getGame(NodesProvider<PNode> nodesProvider) {
		checkNotNull(nodesProvider, "The nodes provider is null");
		if (isSng)
			return new KuhnPoker<>(nodesProvider, nbBlindsP1, nbBlindsP2);
		return new KuhnPoker<>(nodesProvider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.CSCFRMGameBuilder#getSharingGame(net.funkyjava
	 * .cscfrm.game.nodes.provider.itf.NodesProvider,
	 * net.funkyjava.cscfrm.game.itf.CSCFRMGame)
	 */
	@Override
	public KuhnPoker<PNode> getSharingGame(NodesProvider<PNode> nodesProvider,
			KuhnPoker<PNode> source) {
		checkNotNull(nodesProvider, "The nodes provider is null");
		checkNotNull(source, "The source game is null");
		if (isSng)
			return new KuhnPoker<>(nodesProvider, source, nbBlindsP1,
					nbBlindsP2);
		return new KuhnPoker<>(nodesProvider, source);
	}
}
