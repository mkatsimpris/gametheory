package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.IllegalComponentStateException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.NLHandRounds;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundState;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundType;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.anteround.AnteValue;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetChoice;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround.BlindValue;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround.BlindValue.Type;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayersData;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.model.BetNode;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.model.PotsNodes;

/**
 * Class to build a hand reduced bet tree via it's method
 * {@link #getBetTree(NLHandRounds, BetRangeSlicer)}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class HandBetTreeBuilder {
	private final RoundBetTreeBuilder[] rounds;
	private final BetRangeSlicer slicer;
	private final NLHandRounds baseHand;
	private int nbNodesCreated = 0;
	private final TerminalNodesBuilder term = new TerminalNodesBuilder();

	private List<Integer> bets = new LinkedList<>();

	private HandBetTreeBuilder(NLHandRounds hand, BetRangeSlicer slicer) {
		this.slicer = slicer;
		rounds = new RoundBetTreeBuilder[hand.getNbBetRounds()];
		for (int i = 0; i < rounds.length; i++)
			rounds[i] = new RoundBetTreeBuilder();
		this.baseHand = hand;
	};

	private BettingTree getBettingTree() {
		createNodeRec(baseHand, 1, 0);
		log.info("End generating bet tree, {} nodes created ", nbNodesCreated);
		RoundBetTree[] res = new RoundBetTree[rounds.length];
		for (int i = 0; i < rounds.length; i++)
			res[i] = rounds[i].build();
		return new BettingTree(res, term.build());
	}

	private int createNodeRec(NLHandRounds hand, int raiseIndex, int moveIndex) {
		nbNodesCreated++;
		switch (hand.getRoundState()) {
		case CANCELED:
			throw new IllegalComponentStateException("Wrong hand state "
					+ hand.getRoundState());
		case END_NO_SHOWDOWN:
			return -RoundBetTree.offset + createNoShowDownNode(hand);
		case NEXT_ROUND:
			bets.add(-1);
			NLHandRounds newHand = hand.clone();
			newHand.nextBetRound();
			int res = createBetNode(newHand, 0, 0) + RoundBetTree.offset;
			bets.remove(bets.size() - 1);
			return res;
		case SHOWDOWN:
			return -RoundBetTree.offset * 2 + createShowDownNode(hand);
		case WAITING_MOVE:
			return createBetNode(hand, raiseIndex, moveIndex);
		}
		throw new IllegalComponentStateException("Impossible hand state "
				+ hand.getRoundState());
	}

	private int createBetNode(NLHandRounds hand, int raiseIndex, int moveIndex) {
		final BetChoice choice = hand.getBetChoice();
		final PlayersData playersData = hand.getPlayersData();
		final int player = choice.getPlayer();
		final int[] bets = slicer.slice(hand.getCurrentPots(), playersData,
				choice, raiseIndex, hand.getBetRoundIndex());
		final BetNode node = new BetNode(player, bets);
		final int res = moveIndex == 0 ? rounds[hand.getBetRoundIndex()]
				.findOrCreateStartBetNode(node) : rounds[hand
				.getBetRoundIndex()].createBetNode(node);
		// Fill the node
		final int callValue = choice.getCallValue().getValue();
		for (int i = 0; i < bets.length; i++) {
			final NLHandRounds newHand = hand.clone();
			if (bets[i] == BetRangeSlicer.fold) {
				if (nbNodesCreated == 846803)
					log.info("Fold");
				if (!newHand.doMove(Move.getFold(player)))
					throw new IllegalStateException("Can't do this move");
				this.bets.add(BetRangeSlicer.fold);
				node.getNextNodes()[i] = createNodeRec(newHand, raiseIndex,
						moveIndex + 1);
				this.bets.remove(this.bets.size() - 1);
				if (nbNodesCreated == 846803)
					log.info("End Fold");
				continue;
			}
			if (bets[i] == callValue) {
				if (nbNodesCreated == 846803)
					log.info("Call");
				this.bets.add(callValue);
				newHand.doMove(Move.getCall(player, callValue,
						playersData.getBets()[player]));
				node.getNextNodes()[i] = createNodeRec(newHand, raiseIndex,
						moveIndex + 1);
				this.bets.remove(this.bets.size() - 1);
				continue;
			}
			if (raiseIndex == 0) {
				if (nbNodesCreated == 846803)
					log.info("Bet");
				this.bets.add(bets[i]);
				if (!newHand.doMove(Move.getBet(choice.getPlayer(), bets[i])))
					throw new IllegalStateException("Can't do this move");

				node.getNextNodes()[i] = createNodeRec(newHand, raiseIndex + 1,
						moveIndex + 1);
				this.bets.remove(this.bets.size() - 1);
				continue;
			}
			if (nbNodesCreated == 846803)
				log.info("Raise");
			this.bets.add(bets[i]);
			if (!newHand.doMove(Move.getRaise(choice.getPlayer(), bets[i],
					playersData.getBets()[player])))
				throw new IllegalStateException("Can't do this move");
			node.getNextNodes()[i] = createNodeRec(newHand, raiseIndex + 1,
					moveIndex + 1);
			this.bets.remove(this.bets.size() - 1);
		}
		if (nbNodesCreated == 846803)
			log.info("End Create bet node");
		return res;
	}

	private int createShowDownNode(NLHandRounds hand) {
		// System.err.println("createShowDownNode");
		return term.findOrCreateShowDownNode(new PotsNodes(hand
				.getCurrentPots()));
	}

	private int createNoShowDownNode(NLHandRounds hand) {
		// System.err.println("createNoShowDownNode");
		return term.findOrCreateNoShowDownNode(new PotsNodes(hand
				.getCurrentPots()));
	}

	/**
	 * Build a betting tree for a hand given a {@link BetRangeSlicer}
	 * 
	 * @param hand
	 *            the hand for which the betting tree must be built
	 * @param slicer
	 *            the bet range slicer to reduce the bet possibilities
	 * @return the resulting {@link BettingTree}
	 */
	public static BettingTree getBetTree(NLHandRounds hand,
			BetRangeSlicer slicer) {
		checkNotNull(hand, "The hand cannot be null");
		checkArgument(hand.getRoundState() == RoundState.WAITING_MOVE,
				"The hand doesn't seem to expect a move");
		doAnteAndBlinds(hand);
		return new HandBetTreeBuilder(hand, slicer).getBettingTree();
	}

	/**
	 * Positively perform all ante and blinds payments when asked
	 * 
	 * @param hand
	 *            the hand to perform payments
	 */
	public static void doAnteAndBlinds(NLHandRounds hand) {
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

}
