package net.funkyjava.gametheory.cscfrm.games.kuhnpoker;

import java.util.LinkedList;
import java.util.List;

import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.cyclic.CSCFRMCyclicStepsGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;


/**
 * The cyclic steps game builder for kuhn poker SNG.
 * 
 * @author Pierre Mardon
 * @param <PNode>
 *            the player node type
 */
public class SNGKuhnPokerBuilder<PNode extends PlayerNode> extends
		SNGKuhnPokerPositionMapper implements
		CSCFRMCyclicStepsGameBuilder<PNode, KuhnPoker<PNode>> {

	/** The builders. */
	private List<CSCFRMGameBuilder<PNode, KuhnPoker<PNode>>> builders = new LinkedList<>();

	/** The uid. */
	private final String uid;

	/**
	 * The Constructor.
	 * 
	 * @param nbBlindsStack
	 *            the number of blinds in players stacks when starting the SNG
	 */
	public SNGKuhnPokerBuilder(int nbBlindsStack) {
		if (nbBlindsStack < 3)
			throw new IllegalArgumentException(
					"Players must have at least 3 blinds each to start a SNG");
		uid = "SNGKuhnPoker-" + nbBlindsStack;
		for (int i = 3; i < nbBlindsStack * 2 - 2; i++) {
			builders.add(new KuhnPokerBuilder<PNode>(i, 2 * nbBlindsStack - i));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.cyclicsteps.CSCFRMCyclicStepsGameBuilder
	 * #getNbPlayers()
	 */
	@Override
	public int getNbPlayers() {
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.cyclicsteps.CSCFRMCyclicStepsGameBuilder
	 * #getBuilders()
	 */
	@Override
	public List<CSCFRMGameBuilder<PNode, KuhnPoker<PNode>>> getBuilders() {
		return builders;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.cyclicsteps.CSCFRMCyclicStepsGameBuilder
	 * #getUId()
	 */
	@Override
	public String getUId() {
		return uid;
	}

}
