/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.games.poker.nlhe.pushfold;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.Random;

import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMFullGame;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.ChanceNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.TerminalNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;
import net.funkyjava.gametheory.cscfrm.util.game.helpers.ArraysIterator;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluator;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Pierre Mardon
 * @param <PNode>
 *            the player node type
 * 
 */
@Slf4j
public class NLHEHUPushFold<PNode extends PlayerNode> implements
		CSCFRMFullGame<PNode> {

	private static String[] holeCardsStr = new String[169];

	static {
		int r1, r2;
		for (int i = 0; i < 169; i++) {
			r1 = i / 13;
			r2 = i % 13;
			if (r1 > r2) {
				holeCardsStr[i] = Cards52Strings.ranks[r1]
						+ Cards52Strings.ranks[r2] + "s";
				continue;
			}
			if (r1 < r2) {
				holeCardsStr[i] = Cards52Strings.ranks[r2]
						+ Cards52Strings.ranks[r1] + "o";
				continue;
			}
			holeCardsStr[i] = Cards52Strings.ranks[r1]
					+ Cards52Strings.ranks[r1] + " ";
		}
	}

	private final int[][] cardsHands = new int[52][52];
	private final String uid;
	private final boolean immediateAllIn;
	private final boolean justSbCall;
	private boolean debug = false;
	private final Deck52Cards deck;
	private final int deckOffset;
	private final Holdem7CardsEvaluator eval;

	private final TerminalNode sbWinsAllIn, bbWinsAllIn, sbFold, bbFold, tie;
	private final ChanceNode distribSbCards, distribBbCards, distribBoard;
	private final PNode[] sbChoice, bbChoice;
	private final Random rand = new Random();

	private static enum Sequence {
		ROOT, DISTRIBUTED_P1, DISTRIBUTED_P2, SB_FOLDED, SB_PUSHED, BB_FOLDED, BB_CALLED, ALL_IN_RESULT
	}

	private boolean ignoreChanceNodes = false;
	final private int[] intCards = new int[9];
	final private int[] p1CardsInt = new int[2];
	final private int[] p2CardsInt = new int[2];
	final private int[] boardCardsInt = new int[5];

	private Sequence seq;
	private int p1Cards, p2Cards;
	private int allInResult;

	/**
	 * Constructor for not-sng game
	 * 
	 * @param nodesProvider
	 *            the nodes provider
	 * @param sb
	 *            small blind value
	 * @param bb
	 *            big blind value
	 * @param stackSb
	 *            first player's stack
	 * @param stackBb
	 *            second player's stack
	 * @param eval
	 *            Holdem hand evaluator
	 */
	public NLHEHUPushFold(NodesProvider<PNode> nodesProvider, int sb, int bb,
			int stackSb, int stackBb, Holdem7CardsEvaluator eval) {
		this(nodesProvider, sb, bb, stackSb, stackBb, 0, false, eval);
	}

	/**
	 * SNG game constructor
	 * 
	 * @param nodesProvider
	 *            the nodes provider
	 * @param sb
	 *            small blind value
	 * @param bb
	 *            big blind value
	 * @param stackSb
	 *            first player's stack
	 * @param stackBb
	 *            second player's stack
	 * @param granularity
	 *            the terminal nodes ids are indexed by (next game's small blind
	 *            stack / granularity) - 1. Sb, bb, and stacks must be multiple
	 *            of the granularity
	 * @param eval
	 *            Holdem hand evaluator
	 * 
	 */
	public NLHEHUPushFold(NodesProvider<PNode> nodesProvider, int sb, int bb,
			int stackSb, int stackBb, int granularity, Holdem7CardsEvaluator eval) {
		this(nodesProvider, sb, bb, stackSb, stackBb, granularity, true, eval);
	}

	/**
	 * Constructor to build a game sharing his resources with a source game.
	 * 
	 * @param source
	 *            the source game
	 * @param eval
	 *            an holdem evaluator
	 */
	public NLHEHUPushFold(NLHEHUPushFold<PNode> source, Holdem7CardsEvaluator eval) {
		this.immediateAllIn = source.immediateAllIn;
		this.justSbCall = source.justSbCall;
		this.bbChoice = source.bbChoice;
		this.sbChoice = source.sbChoice;
		this.bbFold = source.bbFold;
		this.sbFold = source.sbFold;
		this.sbWinsAllIn = source.sbWinsAllIn;
		this.bbWinsAllIn = source.bbWinsAllIn;
		this.tie = source.tie;
		this.distribBbCards = source.distribBbCards;
		this.distribSbCards = source.distribSbCards;
		this.distribBoard = source.distribBoard;
		this.deckOffset = eval.getCardsSpec().getOffset();
		checkArgument(
				deckOffset == source.deckOffset,
				"The game to share resources with has not the same cards "
						+ "specifications as the one provided in this constructor");
		this.deck = new Deck52Cards(deckOffset);
		this.uid = source.uid;
		this.eval = eval;

	}

	/**
	 * Main constructor
	 * 
	 * @param nodesProvider
	 *            the nodes provider
	 * @param sb
	 *            small blind value
	 * @param bb
	 *            big blind value
	 * @param stackSb
	 *            first player's stack
	 * @param stackBb
	 *            second player's stack
	 * @param granularity
	 *            For SNG only : the terminal nodes ids are indexed by (next
	 *            game's small blind stack / granularity) - 1. Sb, bb, and
	 *            stacks must be multiple of the granularity
	 * @param isSng
	 *            true when in SNG
	 * @param eval
	 *            Holdem hand evaluator
	 * 
	 */
	@SuppressWarnings("unchecked")
	private NLHEHUPushFold(NodesProvider<PNode> nodesProvider, int sb, int bb,
			int stackSb, int stackBb, int granularity, boolean isSng,
			Holdem7CardsEvaluator eval) {
		checkNotNull(nodesProvider, "Nodes provider is null");
		checkArgument(sb >= 0, "Small blind must be >= 0");
		checkArgument(bb >= 0, "Big blind must be >= 0");
		checkArgument(stackSb > 0, "The small blind player's stack must be > 0");
		checkArgument(stackBb > 0, "The big blind player's stack must be > 0");
		checkArgument(sb <= bb, "Small blind must be <= big blind");
		if (isSng) {
			checkArgument(granularity > 0, "The granularity must be > 0");
			checkArgument(
					sb % granularity == 0 && bb % granularity == 0
							&& stackSb % granularity == 0
							&& stackBb % granularity == 0,
					"Small blind, big blind and stacks must be multiples of the granularity");
		}
		this.eval = checkNotNull(eval, "Holdem evaluator is null");
		this.deckOffset = eval.getCardsSpec().getOffset();
		this.deck = new Deck52Cards(eval.getCardsSpec().getOffset());
		this.uid = "NLHEHU-PF-" + sb + "-" + bb + "-" + stackSb + "-" + stackBb;
		fillCardsHands();
		// Detect special cases
		immediateAllIn = stackSb <= sb || stackBb <= sb;
		justSbCall = !immediateAllIn && (stackSb <= bb || stackBb <= bb);

		// Create all nodes
		int bbTmp;
		int id;

		// SNG
		if (isSng) {
			// SB wins BB
			if (immediateAllIn || justSbCall) {
				bbFold = null;
			} else {
				bbTmp = stackBb - bb;
				id = bbTmp / granularity - 1;
				bbFold = nodesProvider.getTerminalNode(id);
			}

			// BB wins SB
			if (immediateAllIn)
				sbFold = null;
			else {
				bbTmp = stackBb + sb;
				id = bbTmp / granularity - 1;
				sbFold = nodesProvider.getTerminalNode(id);
			}

			// SB wins all-in
			bbTmp = stackBb - Math.min(stackSb, stackBb);
			if (bbTmp == 0)
				sbWinsAllIn = nodesProvider.getTerminalNode(new double[] { 1,
						-1 });
			else
				sbWinsAllIn = nodesProvider.getTerminalNode(bbTmp / granularity
						- 1);

			// BB wins all-in
			bbTmp = stackBb + Math.min(stackSb, stackBb);
			if (bbTmp == stackBb + stackSb)
				bbWinsAllIn = nodesProvider.getTerminalNode(new double[] { -1,
						1 });
			else
				bbWinsAllIn = nodesProvider.getTerminalNode(bbTmp / granularity
						- 1);
			tie = nodesProvider.getTerminalNode(stackBb / granularity - 1);
		}
		// Not SNG
		else {
			// SB wins BB
			if (immediateAllIn || justSbCall)
				bbFold = null;
			else
				bbFold = nodesProvider
						.getTerminalNode(new double[] { bb, -bb });

			// BB wins SB
			if (immediateAllIn)
				sbFold = null;
			else {
				sbFold = nodesProvider
						.getTerminalNode(new double[] { -sb, sb });
			}

			// SB wins all-in
			int minStack = Math.min(stackSb, stackBb);
			sbWinsAllIn = nodesProvider.getTerminalNode(new double[] {
					minStack, -minStack });

			// BB wins all-in
			bbWinsAllIn = nodesProvider.getTerminalNode(new double[] {
					-minStack, minStack });
			tie = nodesProvider.getTerminalNode(new double[] { 0, 0 });
		}
		distribBoard = distribBbCards = distribSbCards = nodesProvider
				.getChanceNode();
		if (immediateAllIn || justSbCall)
			bbChoice = (PNode[]) new PlayerNode[0];
		else {
			bbChoice = (PNode[]) new PlayerNode[169];
			for (int i = 0; i < 169; i++) {
				bbChoice[i] = nodesProvider.getPlayerNode(1, 2);
			}
		}
		if (immediateAllIn)
			sbChoice = (PNode[]) new PlayerNode[0];
		else {
			sbChoice = (PNode[]) new PlayerNode[169];
			for (int i = 0; i < 169; i++) {
				sbChoice[i] = nodesProvider.getPlayerNode(0, 2);
			}
		}
	}

	private void fillCardsHands() {
		final IntCardsSpec spec = eval.getCardsSpec();
		int r1, r2;
		for (int i = deckOffset; i < deckOffset + 52; i++) {
			for (int j = deckOffset; j < deckOffset + 52; j++) {
				if (i == j)
					continue;
				r1 = spec.getStandardRank(i);
				r2 = spec.getStandardRank(j);
				if (r1 == r2) {
					cardsHands[i - deckOffset][j - deckOffset] = r1 * 13 + r2;
					continue;
				}
				if (spec.sameColor(i, j)) {
					cardsHands[i - deckOffset][j - deckOffset] = Math.max(r1,
							r2) * 13 + Math.min(r1, r2);
					continue;
				}
				cardsHands[i - deckOffset][j - deckOffset] = Math.min(r1, r2)
						* 13 + Math.max(r1, r2);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.CSCFRMGame#getPlayerNodesIterator()
	 */
	@Override
	public Iterator<PNode> getPlayerNodesIterator() {
		return ArraysIterator.get(sbChoice, bbChoice);
	}

	private void distributeCards() {
		deck.oneShotDeckDraw(intCards);
		System.arraycopy(intCards, 0, p1CardsInt, 0, 2);
		System.arraycopy(intCards, 2, p2CardsInt, 0, 2);
		System.arraycopy(intCards, 4, boardCardsInt, 0, 5);
		allInResult = eval.compare7CardsHands(p1CardsInt, p2CardsInt, boardCardsInt);
		p1Cards = cardsHands[p1CardsInt[0] - deckOffset][p1CardsInt[1]
				- deckOffset];
		p2Cards = cardsHands[p2CardsInt[0] - deckOffset][p2CardsInt[1]
				- deckOffset];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.CSCFRMBaseGame#onIterationStart()
	 */
	@Override
	public void onIterationStart() {
		distributeCards();
		ignoreChanceNodes = true;
		seq = Sequence.DISTRIBUTED_P2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.CSCFRMBaseGame#back()
	 */
	@Override
	public void back() {
		if (debug)
			log.debug("Back from {}", seq);
		switch (seq) {
		case BB_CALLED:
		case BB_FOLDED:
			seq = Sequence.SB_PUSHED;
			return;
		case DISTRIBUTED_P1:
			seq = Sequence.ROOT;
			return;
		case DISTRIBUTED_P2:
			seq = Sequence.DISTRIBUTED_P1;
			return;
		case ROOT:
			throw new IllegalArgumentException("Can't go back from ROOT");
		case SB_FOLDED:
			seq = Sequence.DISTRIBUTED_P2;
			return;
		case SB_PUSHED:
			seq = Sequence.DISTRIBUTED_P2;
			return;
		case ALL_IN_RESULT:
			if (immediateAllIn)
				if (ignoreChanceNodes)
					throw new IllegalArgumentException(
							"Cant go back from all-in result in immediate all-in");
				else
					seq = Sequence.DISTRIBUTED_P2;
			else if (justSbCall)
				seq = Sequence.SB_PUSHED;
			else
				seq = ignoreChanceNodes ? Sequence.SB_PUSHED
						: Sequence.BB_CALLED;
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.CSCFRMBaseGame#getCurrentNode()
	 */
	@Override
	public Node getCurrentNode() {
		if (debug)
			log.debug("Current node seq {}", seq);
		switch (seq) {
		case ALL_IN_RESULT:
			return allInResult > 0 ? sbWinsAllIn
					: allInResult < 0 ? bbWinsAllIn : tie;
		case BB_FOLDED:
			return bbFold;
		case DISTRIBUTED_P1:
			return distribBbCards;
		case DISTRIBUTED_P2:
			if (immediateAllIn)
				if (ignoreChanceNodes)
					return allInResult > 0 ? sbWinsAllIn
							: allInResult < 0 ? bbWinsAllIn : tie;
				else
					return distribBoard;
			return sbChoice[p1Cards];
		case ROOT:
			return distribSbCards;
		case SB_FOLDED:
			return sbFold;
		case SB_PUSHED:
			if (justSbCall)
				if (ignoreChanceNodes)
					return allInResult > 0 ? sbWinsAllIn
							: allInResult < 0 ? bbWinsAllIn : tie;
				else
					return distribBoard;
			return bbChoice[p2Cards];
		case BB_CALLED:
			return distribBoard;

		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.CSCFRMBaseGame#getNbPlayers()
	 */
	@Override
	public int getNbPlayers() {
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.CSCFRMBaseGame#getMaxDepth()
	 */
	@Override
	public int getMaxDepth() {
		return 7;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.CSCFRMBaseGame#getMaxNbPlActions()
	 */
	@Override
	public int getMaxNbPlActions() {
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.CSCFRMBaseGame#getUId()
	 */
	@Override
	public String getUId() {
		return uid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.generic.GameChancePicker#choseChanceAction
	 * ()
	 */
	@Override
	public int choseChanceAction() {
		if (debug)
			log.debug("Actively chose chance seq {}", seq);
		if (ignoreChanceNodes)
			throw new IllegalArgumentException(
					"Asked for chance but should ignore...");
		if (seq == Sequence.BB_CALLED || seq == Sequence.SB_PUSHED) {
			seq = Sequence.ALL_IN_RESULT;
			return allInResult;
		}
		if (seq == Sequence.ROOT) {
			seq = Sequence.DISTRIBUTED_P1;
			return p1Cards;
		}
		if (seq == Sequence.DISTRIBUTED_P1) {
			seq = Sequence.DISTRIBUTED_P2;
			return p2Cards;
		}
		if (seq == Sequence.DISTRIBUTED_P2 && immediateAllIn) {
			seq = Sequence.ALL_IN_RESULT;
			return allInResult;
		}
		throw new IllegalArgumentException("Bad sequence to chose a chance : "
				+ seq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.generic.NoChanceGameObserver#onGameStart()
	 */
	@Override
	public void onGameStart() {
		distributeCards(); // TODO not necessarily
		ignoreChanceNodes = false;
		seq = Sequence.ROOT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.generic.NoChanceGameObserver#chosePlayerAction
	 * (int)
	 */
	@Override
	public void chosePlayerAction(int actionIndex) {
		if (debug)
			log.debug("Chose player {} seq {}", actionIndex, seq);
		switch (seq) {
		case DISTRIBUTED_P2:
			seq = actionIndex == 0 ? Sequence.SB_FOLDED : Sequence.SB_PUSHED;
			return;
		case SB_PUSHED:
			if (justSbCall)
				throw new IllegalArgumentException("We just go all-in here");
			seq = actionIndex == 0 ? Sequence.BB_FOLDED
					: ignoreChanceNodes ? Sequence.ALL_IN_RESULT
							: Sequence.BB_CALLED;
			return;
		case ALL_IN_RESULT:
		case BB_CALLED:
		case BB_FOLDED:
		case DISTRIBUTED_P1:
		case SB_FOLDED:
		case ROOT:
			throw new IllegalArgumentException(
					"Cant chose player action for seq " + seq);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.generic.GameObserver#choseChanceAction(int)
	 */
	@Override
	public void choseChanceAction(int actionIndex) {
		if (debug)
			log.debug("Chose chance {} seq {}", actionIndex, seq);
		if (ignoreChanceNodes)
			throw new IllegalArgumentException(
					"Asked for chance but should ignore...");
		if (seq == Sequence.BB_CALLED) {
			seq = Sequence.ALL_IN_RESULT;
			allInResult = actionIndex;
			return;
		}
		if (seq == Sequence.ROOT) {
			seq = Sequence.DISTRIBUTED_P1;
			p1Cards = actionIndex;
			return;
		}
		if (seq == Sequence.DISTRIBUTED_P1) {
			seq = Sequence.DISTRIBUTED_P2;
			p2Cards = actionIndex;
			return;
		}
		if (seq == Sequence.SB_PUSHED && justSbCall) {
			seq = Sequence.ALL_IN_RESULT;
			allInResult = actionIndex;
			return;
		}
		if (seq == Sequence.DISTRIBUTED_P2 && immediateAllIn) {
			seq = Sequence.ALL_IN_RESULT;
			allInResult = actionIndex;
			return;
		}
		throw new IllegalArgumentException("Bad sequence to chose a chance : "
				+ seq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.generic.GamePlayerDecider#chosePlayerAction
	 * ()
	 */
	@Override
	public int chosePlayerAction() {
		if (debug)
			log.debug("Actively chose player action seq {}", seq);
		final double[] strat = ((PlayerNode) getCurrentNode()).getAvgStrategy();
		if (strat[0] >= 0.9) {
			chosePlayerAction(0);
			return 0;
		}
		if (strat[1] >= 0.9) {
			chosePlayerAction(1);
			return 1;
		}
		final double r = rand.nextDouble();
		double v = 0;
		for (int i = 0; i < strat.length; i++)
			if ((v += strat[i]) >= r) {
				chosePlayerAction(i);
				return i;
			}
		chosePlayerAction(0);
		return 0;
	}

	/**
	 * Builds standard push / call charts considering a push or a call will
	 * occure when the action frequency is > minAction
	 * 
	 * @param minAction
	 *            the threshold
	 * @return the string to print
	 */
	public String getBinaryStrategiesString(double minAction) {
		if (immediateAllIn)
			return "Game is an immediate all-in";

		StringBuilder sb = new StringBuilder();
		sb.append("#### ").append(getUId()).append('\n');
		StringBuilder sbSb = new StringBuilder();
		StringBuilder bbSb = new StringBuilder();
		for (int i = 12; i >= 0; i--) {
			for (int j = 12; j >= 0; j--) {
				int card = 13 * i + j;
				boolean push = sbChoice[card].getAvgStrategy()[1] > 0.5;
				sbSb.append(push ? holeCardsStr[card] + " " : "    ");
				if (!justSbCall) {
					boolean call = bbChoice[card].getAvgStrategy()[1] > 0.5;
					bbSb.append(call ? holeCardsStr[card] + " " : "    ");
				}
			}
			sbSb.append('\n');
			bbSb.append('\n');
		}
		sb.append("SB PUSH ").append("###########################\n")
				.append(sbSb).append('\n');
		if (!justSbCall)
			sb.append("BB CALL ").append("###########################\n")
					.append(bbSb).append('\n');
		return sb.toString();
	}

	/**
	 * Sets debug boolean
	 * 
	 * @param debug
	 *            value
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
