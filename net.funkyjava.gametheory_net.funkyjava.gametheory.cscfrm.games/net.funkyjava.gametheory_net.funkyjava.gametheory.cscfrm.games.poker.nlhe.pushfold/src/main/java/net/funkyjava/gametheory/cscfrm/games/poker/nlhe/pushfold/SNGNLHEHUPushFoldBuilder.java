/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.games.poker.nlhe.pushfold;

import java.util.LinkedList;
import java.util.List;

import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.cyclic.CSCFRMCyclicStepsGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluatorProvider;

/**
 * Builder for NLHE HU Push/Fold constant blinds SNG
 * 
 * @author Pierre Mardon
 * @param <PNode>
 *            the player node type
 * 
 */
public class SNGNLHEHUPushFoldBuilder<PNode extends PlayerNode> implements
		CSCFRMCyclicStepsGameBuilder<PNode, NLHEHUPushFold<PNode>> {

	private final String uid;
	private final List<CSCFRMGameBuilder<PNode, NLHEHUPushFold<PNode>>> builders = new LinkedList<>();

	/**
	 * @param evalProvider
	 *            the holdem evaluator provider
	 * @param sb
	 *            the small blind
	 * @param bb
	 *            the big blind
	 * @param startingStack
	 *            the stack players have when starting
	 * @param granularity
	 *            the terminal nodes ids are indexed by (next game's small blind
	 *            stack / granularity) - 1. Sb, bb, and stacks must be multiple
	 *            of the granularity
	 * 
	 */
	public SNGNLHEHUPushFoldBuilder(Holdem7CardsEvaluatorProvider evalProvider,
			int sb, int bb, int startingStack, int granularity) {
		this.uid = "SNG-NLHEHUPushFold-" + startingStack + "-" + granularity;
		for (int sbStack = granularity; sbStack < startingStack * 2; sbStack += granularity)
			builders.add(new NLHEHUPushFoldBuilder<PNode>(evalProvider, sb, bb,
					sbStack, 2 * startingStack - sbStack, granularity));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.generic.CyclicStepsGamePositionMapper#
	 * getNextPlayerPosition(int, int, int)
	 */
	@Override
	public int getNextPlayerPosition(int fromStep, int toStep, int fromPos) {
		return fromPos == 0 ? 1 : 0;
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
	 * #getUId()
	 */
	@Override
	public String getUId() {
		return uid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.cyclicsteps.CSCFRMCyclicStepsGameBuilder
	 * #getBuilders()
	 */
	@Override
	public List<CSCFRMGameBuilder<PNode, NLHEHUPushFold<PNode>>> getBuilders() {
		return builders;
	}

}
