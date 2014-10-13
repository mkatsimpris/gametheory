/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.games.poker.nlhe.pushfold;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.HoldemEvaluatorProvider;

/**
 * Game builder for {@link NLHEHUPushFold}
 * 
 * @author Pierre Mardon
 * @param <PNode>
 *            the player nodes type
 * 
 */
public class NLHEHUPushFoldBuilder<PNode extends PlayerNode> implements
		CSCFRMGameBuilder<PNode, NLHEHUPushFold<PNode>> {

	private final int sb, bb, stackSb, stackBb, granularity;
	private final boolean isSng;
	private final HoldemEvaluatorProvider evalProvider;

	/**
	 * Constructor for not-sng game
	 * 
	 * @param evalProvider
	 *            the hands evaluators provider
	 * @param sb
	 *            the small blind
	 * @param bb
	 *            the big blind
	 * @param stackSb
	 *            the stack of the sb player
	 * @param stackBb
	 *            the stack of the bb player
	 * 
	 */
	public NLHEHUPushFoldBuilder(HoldemEvaluatorProvider evalProvider, int sb,
			int bb, int stackSb, int stackBb) {
		checkArgument(sb >= 0, "Small blind must be >= 0");
		checkArgument(bb >= 0, "Big blind must be >= 0");
		checkArgument(stackSb > 0, "The small blind player's stack must be > 0");
		checkArgument(stackBb > 0, "The big blind player's stack must be > 0");
		checkArgument(sb <= bb, "Small blind must be <= big blind");
		this.evalProvider = checkNotNull(evalProvider,
				"Holdem evaluatorprovider is null");
		this.sb = sb;
		this.bb = bb;
		this.stackSb = stackSb;
		this.stackBb = stackBb;
		this.granularity = 1;
		isSng = false;
	}

	/**
	 * Constructor for sng game
	 * 
	 * @param evalProvider
	 *            the hands evaluators provider
	 * @param sb
	 *            the small blind
	 * @param bb
	 *            the big blind
	 * @param stackSb
	 *            the stack of the sb player
	 * @param stackBb
	 *            the stack of the bb player
	 * @param granularity
	 *            the terminal nodes ids are indexed by (next game's small blind
	 *            stack / granularity) - 1. Sb, bb, and stacks must be multiple
	 *            of the granularity
	 * 
	 */
	public NLHEHUPushFoldBuilder(HoldemEvaluatorProvider evalProvider, int sb,
			int bb, int stackSb, int stackBb, int granularity) {
		this.evalProvider = checkNotNull(evalProvider,
				"Holdem evaluatorprovider is null");
		checkArgument(sb >= 0, "Small blind must be >= 0");
		checkArgument(bb >= 0, "Big blind must be >= 0");
		checkArgument(stackSb > 0, "The small blind player's stack must be > 0");
		checkArgument(stackBb > 0, "The big blind player's stack must be > 0");
		checkArgument(granularity > 0, "The granularity must be > 0");
		checkArgument(sb <= bb, "Small blind must be <= big blind");
		checkArgument(sb % granularity == 0 && bb % granularity == 0
				&& stackSb % granularity == 0 && stackBb % granularity == 0,
				"Small blind, big blind and stacks must be multiples of the granularity");
		this.sb = sb;
		this.bb = bb;
		this.stackSb = stackSb;
		this.stackBb = stackBb;
		this.granularity = granularity;
		isSng = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.CSCFRMGameBuilder#getGame(net.funkyjava
	 * .cscfrm.game.nodes.provider.itf.NodesProvider)
	 */
	@Override
	public NLHEHUPushFold<PNode> getGame(NodesProvider<PNode> nodesProvider) {
		return isSng ? new NLHEHUPushFold<PNode>(nodesProvider, sb, bb,
				stackSb, stackBb, granularity, evalProvider.getEvaluator())
				: new NLHEHUPushFold<PNode>(nodesProvider, sb, bb, stackSb,
						stackBb, evalProvider.getEvaluator());
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
	public NLHEHUPushFold<PNode> getSharingGame(
			NodesProvider<PNode> nodesProvider, NLHEHUPushFold<PNode> source) {
		return new NLHEHUPushFold<PNode>(source, evalProvider.getEvaluator());
	}

}
