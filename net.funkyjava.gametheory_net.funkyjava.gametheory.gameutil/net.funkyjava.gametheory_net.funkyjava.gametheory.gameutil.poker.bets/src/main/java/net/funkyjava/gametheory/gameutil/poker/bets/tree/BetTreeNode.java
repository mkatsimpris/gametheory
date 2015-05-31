package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.Pot;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.SharedPot;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.NLHandRounds;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundState;

@AllArgsConstructor
public class BetTreeNode {

	/**
	 * The children of this node when it's a bet node
	 */
	private BetTreeNode[] children = null;

	/**
	 * Parent node
	 */
	@Getter
	private final BetTreeNode parent;

	/**
	 * Index of the round this node is part of
	 */
	@Getter
	private final int betRoundIndex;

	/**
	 * The index of the node in its round
	 */
	@Getter
	private final int roundNodeIndex;

	/**
	 * Bet choices for a bet node, pot values for the showdown node or
	 * no-showdown node
	 */
	@Getter
	private final int[] values;
	/**
	 * Player in each pot for showdown node
	 */
	@Getter
	private final int[][] showdownPlayers;

	/**
	 * Betting player for a bet node, winning player for the no-showdown node,
	 * no meaning for showdown node
	 */
	@Getter
	private final int player;

	/**
	 * Boolean for the type of node
	 */
	@Getter
	private final boolean isBetNode, isShowDownNode, isNoShowDownNode;

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
	public BetTreeNode getChild(int childIndex) {
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
	public void setChild(int childIndex, BetTreeNode child) {
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
	public List<BetTreeNode> getChildren() {
		checkArgument(isBetNode, "This is not a bet node");
		return Arrays.asList(children);
	}

	/**
	 * Gets this bet node's bet value / child map.
	 * 
	 * @return the bet value / child map
	 */
	public HashMap<Integer, BetTreeNode> getBetsChildren() {
		final HashMap<Integer, BetTreeNode> res = new HashMap<Integer, BetTreeNode>();
		for (int i = 0; i < children.length; i++)
			res.put(values[i], children[i]);
		return res;
	}

	/**
	 * Creates a bet node.
	 * 
	 * @param hand
	 *            state of the hand for this node
	 * @param parent
	 *            parent node or null if root
	 * @param roundNodeIndex
	 *            the index of the node in this bet round
	 * @param betChoices
	 *            all possible fold/call/bet/raise represented by integers
	 * @return the bet tree node
	 */
	public static BetTreeNode getBetNode(NLHandRounds hand, BetTreeNode parent,
			int[] betChoices, int roundNodeIndex) {
		checkArgument(hand.getRoundState() == RoundState.WAITING_MOVE);
		return new BetTreeNode(new BetTreeNode[betChoices.length], parent,
				hand.getBetRoundIndex(), roundNodeIndex, betChoices, null,
				hand.getBettingPlayer(), true, false, false, hand);
	}

	/**
	 * Creates a showdown node.
	 * 
	 * @param hand
	 *            state of the hand for this node
	 * @param parent
	 *            parent node or null if root
	 * @param roundNodeIndex
	 *            the index of the node in this bet round
	 * @return the bet tree node
	 */
	public static BetTreeNode getShowdownNode(NLHandRounds hand,
			BetTreeNode parent, int roundNodeIndex) {
		checkArgument(hand.getRoundState() == RoundState.SHOWDOWN);
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
		return new BetTreeNode(null, parent, hand.getBetRoundIndex(),
				roundNodeIndex, potsValues, potsPlayers, -1, false, true,
				false, hand);
	}

	/**
	 * Creates a no-showdown node.
	 * 
	 * @param hand
	 *            state of the hand for this node
	 * @param parent
	 *            parent node or null if root
	 * @param roundNodeIndex
	 *            the index of the node in this bet round
	 * @return the bet tree node
	 */
	public static BetTreeNode getNoShowdownNode(NLHandRounds hand,
			BetTreeNode parent, int roundNodeIndex) {
		checkArgument(hand.getRoundState() == RoundState.END_NO_SHOWDOWN);
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

		return new BetTreeNode(null, parent, hand.getBetRoundIndex(),
				roundNodeIndex, potsValues, null, winningPlayer, false, false,
				true, hand);
	}
}
