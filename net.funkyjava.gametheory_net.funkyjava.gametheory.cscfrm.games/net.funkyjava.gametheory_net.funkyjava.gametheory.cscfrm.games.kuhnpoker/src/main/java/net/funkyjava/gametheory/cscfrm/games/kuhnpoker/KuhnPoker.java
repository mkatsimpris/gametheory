package net.funkyjava.gametheory.cscfrm.games.kuhnpoker;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMFullGame;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.ChanceNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.TerminalNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;
import net.funkyjava.gametheory.cscfrm.util.game.helpers.ArraysIterator;

/**
 * The KuhnPoker game. Can be both instanciated for classic game and as Kuhn
 * poker SNG step.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player nodes type
 */
@Slf4j
public class KuhnPoker<PNode extends PlayerNode> implements
		CSCFRMFullGame<PNode> {

	/**
	 * The players nodes. The first index is the player's chance and the second
	 * is the matching {@link Sequence#pNodeIndex}.
	 */
	private final PNode[][] pNodes;

	/** The random. */
	private Random rand = new Random();

	/** The terminal nodes. */
	private final TerminalNode p1w1, p1w2, p1w3, p1l1, p1l2, p1l3;

	/**
	 * The chances nodes. Will be ignored for CSCFRM, ie when
	 * {@link #onIterationStart()} is called
	 */
	private final ChanceNode distribP1, distribP2;

	/**
	 * The game sequence that determines the state of the game minus the chance
	 * drawings
	 */
	private Sequence seq = Sequence.ROOT;

	/** The players cards. */
	private int p1Card, p2Card;

	/** The game uid */
	private final String uid;

	/** The cards strings. */
	private static final String[] cardsStrings = new String[] { "J", "Q", "K" };

	private boolean debug = false;

	/**
	 * The game sequences. When their {@link Sequence#pNodeIndex} is >= 0, it is
	 * the index of the player node in the second dimension of {@link #pNodes}
	 */
	private static enum Sequence {
		// Sequences not matching a player node
		ROOT(-1), DISTRIBUTED_P1(-1), CHECK_RAISE_FOLD(-1), CHECK_RAISE_CALL(-1), CHECK_CHECK(
				-1), RAISE_FOLD(-1), RAISE_CALL(-1), RAISE_RAISE_CALL(-1), RAISE_RAISE_FOLD(
				-1),
		// Sequences matching a player node
		DISTRIBUTED_P2(0), CHECK(1), CHECK_RAISE(2), RAISE(3), RAISE_RAISE(4);

		/**
		 * The Constructor.
		 * 
		 * @param pNodeIndex
		 *            the player node index
		 */
		private Sequence(int pNodeIndex) {
			this.pNodeIndex = pNodeIndex;
		}

		/** The player node index. */
		public int pNodeIndex;
	}

	/** The array of sequences that match a player node */
	private static final Sequence[] pNodesSeq = new Sequence[] {
			Sequence.DISTRIBUTED_P2, Sequence.CHECK, Sequence.CHECK_RAISE,
			Sequence.RAISE, Sequence.RAISE_RAISE };

	/**
	 * The Constructor for classic payoffs (not SNG), sharing resources with a
	 * source game.
	 * 
	 * @param nodesProvider
	 *            the nodes provider
	 * @param source
	 *            the source game
	 */
	public KuhnPoker(NodesProvider<PNode> nodesProvider, KuhnPoker<PNode> source) {
		this(nodesProvider, source, true, 0, 0);
	}

	/**
	 * The Constructor for classic payoffs (not SNG).
	 * 
	 * @param nodesProvider
	 *            the nodes provider
	 */
	public KuhnPoker(NodesProvider<PNode> nodesProvider) {
		this(nodesProvider, null, true, 0, 0);
	}

	/**
	 * The Constructor for SNG.
	 * 
	 * @param nodesProvider
	 *            the nodes provider
	 * @param nbBlindsP1
	 *            the number of blinds of the first player
	 * @param nbBlindsP2
	 *            the number of blinds of the second player
	 */
	public KuhnPoker(NodesProvider<PNode> nodesProvider, int nbBlindsP1,
			int nbBlindsP2) {
		this(nodesProvider, null, nbBlindsP1, nbBlindsP2);
	}

	/**
	 * The Constructor SNG, sharing resources with a source game.
	 * 
	 * @param nodesProvider
	 *            the nodes provider
	 * @param source
	 *            the source game
	 * @param nbBlindsP1
	 *            the number of blinds of the first player
	 * @param nbBlindsP2
	 *            the number of blinds of the second player
	 */
	public KuhnPoker(NodesProvider<PNode> nodesProvider,
			KuhnPoker<PNode> source, int nbBlindsP1, int nbBlindsP2) {
		this(nodesProvider, source, false, nbBlindsP1, nbBlindsP2);
	}

	/**
	 * The private Constructor.
	 * 
	 * @param nodesProvider
	 *            the nodes provider
	 * @param source
	 *            the source
	 * @param classicPayoffs
	 *            true when it's not SNG
	 * @param nbBlindsP1
	 *            the number of blinds of the first player
	 * @param nbBlindsP2
	 *            the number of blinds of the second player
	 */
	@SuppressWarnings("unchecked")
	private KuhnPoker(NodesProvider<PNode> nodesProvider,
			KuhnPoker<PNode> source, boolean classicPayoffs, int nbBlindsP1,
			int nbBlindsP2) {
		checkNotNull(nodesProvider, "Nodes provider should not be null !");
		checkArgument(classicPayoffs || (nbBlindsP1 > 2 && nbBlindsP2 > 2),
				"Cannot instanciate SNG kuhn poker step with less than 3 blinds per player");
		if (source != null) {
			p1w1 = source.p1w1;
			p1w2 = source.p1w2;
			p1w3 = source.p1w3;
			p1l1 = source.p1l1;
			p1l2 = source.p1l2;
			p1l3 = source.p1l3;
			distribP1 = source.distribP1;
			distribP2 = source.distribP2;
			pNodes = source.pNodes;
			uid = source.uid;
			return;
		}
		if (classicPayoffs) {
			p1w1 = nodesProvider.getTerminalNode(new double[] { 1, -1 });
			p1w2 = nodesProvider.getTerminalNode(new double[] { 2, -2 });
			p1w3 = nodesProvider.getTerminalNode(new double[] { 3, -3 });
			p1l1 = nodesProvider.getTerminalNode(new double[] { -1, 1 });
			p1l2 = nodesProvider.getTerminalNode(new double[] { -2, 2 });
			p1l3 = nodesProvider.getTerminalNode(new double[] { -3, 3 });
			uid = "KuhnPoker";
		} else {
			// in kuhn SNG, non final games are indexed by nbBlindsP1 - 3
			// because a player with nbBlinds < 3 can not play.
			if (nbBlindsP2 - 1 < 3)
				p1w1 = nodesProvider.getTerminalNode(new double[] { 1, -1 });
			else {
				p1w1 = nodesProvider.getTerminalNode(nbBlindsP2 - 1 - 3);
			}
			if (nbBlindsP2 - 2 < 3)
				p1w2 = nodesProvider.getTerminalNode(new double[] { 1, -1 });
			else {
				p1w2 = nodesProvider.getTerminalNode(nbBlindsP2 - 2 - 3);
			}
			if (nbBlindsP2 - 3 < 3)
				p1w3 = nodesProvider.getTerminalNode(new double[] { 1, -1 });
			else {
				p1w3 = nodesProvider.getTerminalNode(nbBlindsP2 - 3 - 3);
			}
			if (nbBlindsP1 - 1 < 3)
				p1l1 = nodesProvider.getTerminalNode(new double[] { -1, 1 });
			else
				p1l1 = nodesProvider.getTerminalNode(nbBlindsP2 + 1 - 3);

			if (nbBlindsP1 - 2 < 3)
				p1l2 = nodesProvider.getTerminalNode(new double[] { -1, 1 });
			else
				p1l2 = nodesProvider.getTerminalNode(nbBlindsP2 + 2 - 3);
			if (nbBlindsP1 - 3 < 3)
				p1l3 = nodesProvider.getTerminalNode(new double[] { -1, 1 });
			else
				p1l3 = nodesProvider.getTerminalNode(nbBlindsP2 + 3 - 3);
			uid = "KuhnPoker-" + nbBlindsP1 + "-" + nbBlindsP2;
		}
		distribP1 = nodesProvider.getChanceNode();
		distribP2 = nodesProvider.getChanceNode();
		pNodes = (PNode[][]) new PlayerNode[3][5];
		for (int card = 0; card < 3; card++) {
			// First action : P1 can check/raise
			pNodes[card][Sequence.DISTRIBUTED_P2.pNodeIndex] = nodesProvider
					.getPlayerNode(0, 2);
			// Sequence CHECK : P2 can check/raise
			pNodes[card][Sequence.CHECK.pNodeIndex] = nodesProvider
					.getPlayerNode(1, 2);
			// Sequence CHECK, RAISE : P1 can fold/call
			pNodes[card][Sequence.CHECK_RAISE.pNodeIndex] = nodesProvider
					.getPlayerNode(0, 2);
			// Sequence RAISE : P2 can fold/call/raise
			pNodes[card][Sequence.RAISE.pNodeIndex] = nodesProvider
					.getPlayerNode(1, 3);
			// Sequence RAISE, RAISE : P1 can call/fold
			pNodes[card][Sequence.RAISE_RAISE.pNodeIndex] = nodesProvider
					.getPlayerNode(0, 2);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.CSCFRMBaseGame#onIterationStart()
	 */
	@Override
	public void onIterationStart() {
		// We can skip chance nodes for a CSCFRM iteration.
		seq = Sequence.DISTRIBUTED_P2;
		p1Card = rand.nextInt(3);
		p2Card = rand.nextInt(2);
		if (p1Card == 0)
			p2Card++;
		else if (p1Card == 1 && p2Card == 1)
			p2Card = 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.generic.NoChanceGameObserver#onGameStart()
	 */
	@Override
	public void onGameStart() {
		seq = Sequence.ROOT;
		p1Card = rand.nextInt(3);
		p2Card = rand.nextInt(2);
		if (p1Card == 0)
			p2Card++;
		else if (p1Card == 1 && p2Card == 1)
			p2Card = 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.generic.NoChanceGameObserver#
	 * onPlayerActionChosen (int)
	 */
	@Override
	public void onPlayerActionChosen(int actionIndex) {
		if (debug)
			log.debug("{} : player action {}", seq, actionIndex);
		switch (seq) {
		case CHECK:
			if (actionIndex == 0) { // Check
				seq = Sequence.CHECK_CHECK;
				return;
			}
			if (actionIndex == 1) { // Raise
				seq = Sequence.CHECK_RAISE;
				return;
			}
			break;
		case CHECK_RAISE:
			if (actionIndex == 0) { // Fold
				seq = Sequence.CHECK_RAISE_FOLD;
				return;
			}
			if (actionIndex == 1) { // Call
				seq = Sequence.CHECK_RAISE_CALL;
				return;
			}
			break;
		case DISTRIBUTED_P2:
			if (actionIndex == 0) { // Check
				seq = Sequence.CHECK;
				return;
			}
			if (actionIndex == 1) { // Raise
				seq = Sequence.RAISE;
				return;
			}
			break;
		case RAISE:
			if (actionIndex == 0) { // Fold
				seq = Sequence.RAISE_FOLD;
				return;
			}
			if (actionIndex == 1) { // Call
				seq = Sequence.RAISE_CALL;
				return;
			}
			if (actionIndex == 2) {
				seq = Sequence.RAISE_RAISE;
				return;
			}
			break;
		case RAISE_RAISE:
			if (actionIndex == 0) {
				seq = Sequence.RAISE_RAISE_FOLD;
				return;
			}
			if (actionIndex == 1) {
				seq = Sequence.RAISE_RAISE_CALL;
				return;
			}
		default:
			break;
		}
		throw new IllegalArgumentException("Incoherent player action, index "
				+ actionIndex + " for sequence " + seq);
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
			log.debug("{} : provide chance", seq);
		if (seq == Sequence.ROOT) {
			seq = Sequence.DISTRIBUTED_P1;
			return p1Card;
		}
		if (seq == Sequence.DISTRIBUTED_P1) {
			seq = Sequence.DISTRIBUTED_P2;
			return p2Card;
		}
		throw new IllegalArgumentException(
				"Incoherent sequence for chance action " + seq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.itf.generic.GameObserver#onChanceActionChosen
	 * (int)
	 */
	@Override
	public void onChanceActionChosen(int actionIndex) {
		if (debug)
			log.debug("{} : chose chance {}", seq, actionIndex);
		if (seq == Sequence.ROOT) {
			seq = Sequence.DISTRIBUTED_P1;
			p1Card = actionIndex;
			return;
		}
		if (seq == Sequence.DISTRIBUTED_P1) {
			seq = Sequence.DISTRIBUTED_P2;
			p2Card = actionIndex;
			return;
		}
		throw new IllegalArgumentException(
				"Incoherent sequence for chance action " + seq);

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
		case RAISE:
		case CHECK:
			seq = Sequence.DISTRIBUTED_P2;
			return;
		case CHECK_RAISE:
		case CHECK_CHECK:
			seq = Sequence.CHECK;
			return;
		case CHECK_RAISE_CALL:
		case CHECK_RAISE_FOLD:
			seq = Sequence.CHECK_RAISE;
			return;
		case DISTRIBUTED_P1:
			seq = Sequence.ROOT;
			return;
		case DISTRIBUTED_P2:
			seq = Sequence.DISTRIBUTED_P1;
			return;
		case RAISE_CALL:
		case RAISE_FOLD:
		case RAISE_RAISE:
			seq = Sequence.RAISE;
			return;
		case RAISE_RAISE_CALL:
		case RAISE_RAISE_FOLD:
			seq = Sequence.RAISE_RAISE;
			return;
		case ROOT:
			throw new IllegalArgumentException("Trying to go back from " + seq);
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
			log.debug("Current seq = " + seq);
		switch (seq) {
		case RAISE:
		case CHECK:
			return pNodes[p2Card][seq.pNodeIndex];
		case DISTRIBUTED_P2:
		case CHECK_RAISE:
		case RAISE_RAISE:
			return pNodes[p1Card][seq.pNodeIndex];
		case CHECK_CHECK:
			if (p1Card > p2Card)
				return p1w1;
			return p1l1;
		case CHECK_RAISE_FOLD:
			return p1l1;
		case DISTRIBUTED_P1:
			return distribP2;
		case CHECK_RAISE_CALL:
		case RAISE_CALL:
			if (p1Card > p2Card)
				return p1w2;
			return p1l2;
		case RAISE_FOLD:
			return p1w1;
		case ROOT:
			return distribP1;
		case RAISE_RAISE_CALL:
			if (p1Card > p2Card)
				return p1w3;
			return p1l3;
		case RAISE_RAISE_FOLD:
			return p1l2;
		}
		throw new IllegalStateException("No node for seq " + seq);
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
		// Two chances, maximum three player nodes and one terminal node
		return 2 + 3 + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.CSCFRMBaseGame#getMaxNbPlActions()
	 */
	@Override
	public int getMaxNbPlActions() {
		// Every node has two (check/raise, call/fold) or three
		// (fold/call/raise) possible actions
		return 3;
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
	 * @see net.funkyjava.cscfrm.game.itf.CSCFRMGame#getPlayerNodesEnumeration()
	 */
	@Override
	public Iterator<PNode> getPlayerNodesIterator() {
		return ArraysIterator.get(pNodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("###\nGame " + getUId()
				+ " :\nNodes average strategies and visits:\n");
		for (int card = 0; card < 3; card++)
			for (Sequence s : pNodesSeq)
				sb.append(s
						+ "("
						+ cardsStrings[card]
						+ ") "
						+ Arrays.toString(pNodes[card][s.pNodeIndex]
								.getAvgStrategy())
						+ pNodes[card][s.pNodeIndex].visits + "\n");
		sb.append("###\n");
		return sb.toString();
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
		final double[] strat = ((PlayerNode) getCurrentNode()).getAvgStrategy();
		final double r = rand.nextDouble();
		double v = 0;
		for (int i = 0; i < strat.length; i++)
			if ((v += strat[i]) >= r) {
				onPlayerActionChosen(i);
				return i;
			}
		onPlayerActionChosen(0);
		return 0;
	}

	/**
	 * Set debug mode (enable logs)
	 * 
	 * @param debug
	 *            debug boolean
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
