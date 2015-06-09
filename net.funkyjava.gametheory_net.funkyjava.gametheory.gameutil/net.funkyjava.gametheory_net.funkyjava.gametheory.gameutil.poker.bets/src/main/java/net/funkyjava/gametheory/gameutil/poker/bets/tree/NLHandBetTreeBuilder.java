package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import java.awt.IllegalComponentStateException;
import java.util.Map;

import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.NLHandRounds;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundState;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundType;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.anteround.AnteValue;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetChoice;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround.BlindValue;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround.BlindValue.Type;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayersData;

/**
 * Class to simply builds a reduced tree for a given hand state and a bet range
 * slicer
 * 
 * @author Pierre Mardon
 * 
 */
public class NLHandBetTreeBuilder {

	private final NLBetTreeNode root;
	private final NLBetRangeSlicer slicer;
	private final NLBetTreeBuildingReducer reducer;

	private NLHandBetTreeBuilder(NLBetRangeSlicer slicer, NLHandRounds hand,
			NLBetTreeBuildingReducer reducer) {
		this.slicer = slicer;
		this.reducer = reducer;
		doAnteAndBlinds(hand);
		this.root = createNodeRec(hand);
	}

	private NLBetTreeNode createNodeRec(NLHandRounds hand) {
		switch (hand.getRoundState()) {
		case CANCELED:
			throw new IllegalComponentStateException("Wrong hand state "
					+ hand.getRoundState());
		case END_NO_SHOWDOWN:
			return reducer
					.findEquivalent(NLBetTreeNode.getNoShowdownNode(hand));
		case NEXT_ROUND:
			NLHandRounds newHand = hand.clone();
			newHand.nextBetRound();
			return createBetNode(newHand);
		case SHOWDOWN:
			return reducer.findEquivalent(NLBetTreeNode.getShowdownNode(hand));
		case WAITING_MOVE:
			return createBetNode(hand);
		}
		throw new IllegalComponentStateException("Impossible hand state "
				+ hand.getRoundState());
	}

	private NLBetTreeNode createBetNode(NLHandRounds hand) {
		final BetChoice choice = hand.getBetChoice();
		final PlayersData playersData = hand.getPlayersData();
		final int player = choice.getPlayer();
		final int[] bets = slicer.slice(hand.clone());
		final NLBetTreeNode node = NLBetTreeNode.getBetNode(hand, bets);

		// Fill the node with children
		final int callValue = choice.getCallValue().getValue();
		for (int i = 0; i < bets.length; i++) {
			final NLHandRounds newHand = hand.clone();
			if (bets[i] == NLBetRangeSlicer.fold) {
				if (!newHand.doMove(Move.getFold(player)))
					throw new IllegalStateException("Can't do this move");
				node.setChild(i, createNodeRec(newHand));
				continue;
			}
			if (bets[i] == callValue) {
				newHand.doMove(Move.getCall(player, callValue,
						playersData.getBets()[player]));
				node.setChild(i, createNodeRec(newHand));
				continue;
			}
			if (!newHand.doMove(Move.getRaise(choice.getPlayer(), bets[i],
					playersData.getBets()[player])))
				throw new IllegalStateException("Can't do this move");
			node.setChild(i, createNodeRec(newHand));
		}
		return reducer.findEquivalent(node);
	}

	/**
	 * Positively perform all ante and blinds payments when asked
	 * 
	 * @param hand
	 *            the hand to perform payments
	 */
	private static void doAnteAndBlinds(NLHandRounds hand) {
		if (hand.getRoundType() == RoundType.ANTE) {
			if (hand.getRoundState() == RoundState.WAITING_MOVE) {
				Map<Integer, AnteValue> antes = hand.getMissingAnte();
				for (Integer p : antes.keySet())
					if (!hand.doMove(Move.getAnte(p, antes.get(p).getValue())))
						throw new IllegalStateException("Failed to pay ante");
			}
			if (hand.getRoundState() != RoundState.NEXT_ROUND)
				throw new IllegalComponentStateException(
						"There's no other bet round after antes");
			hand.nextRoundAfterAnte();
		}
		if (hand.getRoundType() == RoundType.BLINDS) {
			if (hand.getRoundState() == RoundState.WAITING_MOVE) {
				Map<Integer, BlindValue> blinds = hand.getMissingBlinds();
				for (Integer p : blinds.keySet())
					if (blinds.get(p).getType() == Type.BB) {
						if (!hand.doMove(Move
								.getBb(p, blinds.get(p).getValue())))
							throw new IllegalStateException(
									"Can't do this move");
					} else if (!hand.doMove(Move.getSb(p, blinds.get(p)
							.getValue())))
						throw new IllegalStateException("Can't do this move");
			}
			if (hand.getRoundState() != RoundState.NEXT_ROUND)
				throw new IllegalComponentStateException(
						"There's no other bet round after blinds");
			hand.betRoundAfterBlinds();
		}
	}

	/**
	 * Builds a indexed and reduced NL betting tree
	 * {@link NLBetTreeNode#roundNodeIndex} set to 0
	 * 
	 * @param hand
	 *            the hand in its initial state
	 * @param slicer
	 *            the bet slicer to decide what bets are allowed
	 * @param reducer
	 *            the tree reducer
	 * @return the indexed betting tree
	 */
	public static NLIndexedBetTree getTree(NLHandRounds hand,
			NLBetRangeSlicer slicer, NLBetTreeBuildingReducer reducer) {
		return new NLIndexedBetTree(new NLHandBetTreeBuilder(slicer, hand,
				reducer).root);
	}

	/**
	 * Builds a indexed and not reduced NL betting tree
	 * {@link NLBetTreeNode#roundNodeIndex} set to 0
	 * 
	 * @param hand
	 *            the hand in its initial state
	 * @param slicer
	 *            the bet slicer to decide what bets are allowed
	 * @return the indexed betting tree
	 */
	public static NLIndexedBetTree getTree(NLHandRounds hand,
			NLBetRangeSlicer slicer) {
		return new NLIndexedBetTree(new NLHandBetTreeBuilder(slicer, hand,
				new NLBetTreeBuildingReducer() {

					@Override
					public NLBetTreeNode findEquivalent(NLBetTreeNode sourceNode) {
						return sourceNode;
					}
				}).root);
	}
}
