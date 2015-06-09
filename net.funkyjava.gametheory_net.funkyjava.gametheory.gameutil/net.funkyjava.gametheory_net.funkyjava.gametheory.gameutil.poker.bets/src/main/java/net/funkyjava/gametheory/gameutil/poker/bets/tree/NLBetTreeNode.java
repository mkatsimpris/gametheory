package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.Pot;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.SharedPot;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.NLHandRounds;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundState;

/**
 * 
 * A bet tree node represents a node in the bet sequences tree.
 * 
 * @author Pierre Mardon
 * 
 */
@AllArgsConstructor
public class NLBetTreeNode {

	/**
	 * The children of this node when it's a bet node, null elsewhere
	 */
	private final NLBetTreeNode[] children;

	/**
	 * Index of the round this node is part of
	 */
	@Getter
	private int betRoundIndex;

	/**
	 * The index of the node in its round. Expected to be by node type : bet
	 * nodes should be indexed from 0 to nb of bet nodes -1 for each round
	 * Initial value should be < 0
	 */
	@Getter
	@Setter
	private int roundNodeIndex;

	/**
	 * Bet choices for a bet node, pot values for the showdown node or
	 * no-showdown node
	 */
	@Getter
	public final int[] values;

	/**
	 * Players stacks
	 */
	@Getter
	public final int[] stacks;

	/**
	 * Player in each pot for showdown node
	 */
	@Getter
	public final int[][] showdownPlayers;

	/**
	 * Betting player for a bet node, winning player for the no-showdown node,
	 * no meaning for showdown node
	 */
	@Getter
	public final int player;

	/**
	 * Boolean to indicate this node is a bet node
	 */
	@Getter
	public final boolean isBetNode;

	/**
	 * Boolean to indicate this node is a showdown node
	 */
	@Getter
	public final boolean isShowDownNode;

	/**
	 * Boolean to indicate this node is a no-showdown node
	 */
	@Getter
	public final boolean isNoShowDownNode;

	/**
	 * The hand state for this node
	 */
	private final NLHandRounds hand;

	/**
	 * Gets the hand state for this node
	 * 
	 * @return the hand state for this node
	 */
	public NLHandRounds getHand() {
		return hand.clone();
	}

	/**
	 * Gets the child of this node for thie provided index. If this node is not
	 * a bet node, will result in a {@link NullPointerException}. There's no
	 * check on the index, it's assumed to be between 0 and the number of
	 * children minus one.
	 * 
	 * @param childIndex
	 * @return The child for the provided index
	 */
	public NLBetTreeNode getChild(int childIndex) {
		return children[childIndex];
	}

	/**
	 * Gets the number of children of this bet node. If it's not a bet node,
	 * will result in a {@link NullPointerException}.
	 * 
	 * @return the number of children
	 */
	public int getChildrenCount() {
		return children.length;
	}

	/**
	 * Set the child for provided index. If this node is not a bet node, will
	 * result in a {@link NullPointerException}. There's no check on the index,
	 * it's assumed to be between 0 and the number of children minus one.
	 * 
	 * @param childIndex
	 * @param child
	 */
	public void setChild(int childIndex, NLBetTreeNode child) {
		children[childIndex] = child;
	}

	/**
	 * Return the number of bets choices the player has. If this node is not a
	 * bet node, will result in a {@link NullPointerException};
	 * 
	 * @return the number of moves the player can do
	 */
	public int getPossibleBetsCount() {
		return children.length;
	}

	/**
	 * Gets this bet node's children as a list for convenience. Not intended to
	 * be used for fast tree walking.
	 * 
	 * @return the children nodes
	 */
	public List<NLBetTreeNode> getChildren() {
		checkArgument(isBetNode, "This is not a bet node");
		return Arrays.asList(children);
	}

	/**
	 * Gets this bet node's bet value / child map.
	 * 
	 * @return the bet value / child map
	 */
	public HashMap<Integer, NLBetTreeNode> getBetsChildren() {
		final HashMap<Integer, NLBetTreeNode> res = new HashMap<Integer, NLBetTreeNode>();
		for (int i = 0; i < children.length; i++)
			res.put(values[i], children[i]);
		return res;
	}

	/**
	 * Creates a bet node.
	 * 
	 * @param hand
	 *            state of the hand for this node
	 * @param betChoices
	 *            all possible fold/call/bet/raise represented by integers
	 * @return the bet tree node
	 */
	public static NLBetTreeNode getBetNode(NLHandRounds hand, int[] betChoices) {
		checkArgument(hand.getRoundState() == RoundState.WAITING_MOVE,
				"Wrong round state for a bet node");
		return new NLBetTreeNode(new NLBetTreeNode[betChoices.length],
				hand.getBetRoundIndex(), -1, betChoices, hand.getPlayersData()
						.getStacks(), null, hand.getBettingPlayer(), true,
				false, false, hand);
	}

	/**
	 * Creates a showdown node.
	 * 
	 * @param hand
	 *            state of the hand for this node
	 * @return the bet tree node
	 */
	public static NLBetTreeNode getShowdownNode(NLHandRounds hand) {
		checkArgument(hand.getRoundState() == RoundState.SHOWDOWN,
				"Wrong round state for a showdown node");
		final List<Pot<Integer>> pots = hand.getCurrentPots();
		final int nbOfPots = pots.size();
		final int[] potsValues = new int[nbOfPots];
		final int[][] potsPlayers = new int[nbOfPots][];
		for (int i = 0; i < nbOfPots; i++) {
			final Pot<Integer> pot = pots.get(i);
			potsValues[i] = pot.getValue();
			final List<Integer> players = pot.getPlayers();
			final int nbPlayers = players.size();
			potsPlayers[i] = new int[nbPlayers];
			for (int j = 0; j < nbPlayers; j++)
				potsPlayers[i][j] = players.get(j);
		}
		return new NLBetTreeNode(null, hand.getBetRoundIndex(), -1, potsValues,
				hand.getPlayersData().getStacks(), potsPlayers, -1, false,
				true, false, hand);
	}

	/**
	 * Creates a no-showdown node.
	 * 
	 * @param hand
	 *            state of the hand for this node
	 * @return the bet tree node
	 */
	public static NLBetTreeNode getNoShowdownNode(NLHandRounds hand) {
		checkArgument(hand.getRoundState() == RoundState.END_NO_SHOWDOWN,
				"Wrong round state for a no-showdown node");
		final List<SharedPot<Integer>> pots = hand.getSharedPots().get();
		checkArgument(!pots.isEmpty(),
				"There is no pot to create this showdown node");
		final int nbOfPots = pots.size();
		final int[] potsValues = new int[nbOfPots];
		int winningPlayer = -1;
		for (int i = 0; i < nbOfPots; i++) {
			final Pot<Integer> pot = pots.get(i).getPot();
			potsValues[i] = pot.getValue();
			winningPlayer = pot.getPlayers().get(0);
		}

		return new NLBetTreeNode(null, hand.getBetRoundIndex(), -1, potsValues,
				hand.getPlayersData().getStacks(), null, winningPlayer, false,
				false, true, hand);
	}

	public static void walkTree(NLBetTreeNode rootNode, SimpleNLBetTreeWalker walker) {
		walkTree(rootNode, walker, 0, 0);
	}

	private static void walkTree(NLBetTreeNode rootNode,
			SimpleNLBetTreeWalker walker, int depth, int lastBet) {
		if (walker.handleCurrentNode(rootNode, depth, lastBet)
				&& rootNode.isBetNode())
			for (int i = 0; i < rootNode.getChildrenCount(); i++)
				walkTree(rootNode.getChild(i), walker, depth + 1,
						rootNode.getValues()[i]);
	}
}
