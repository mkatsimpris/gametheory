/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.MoveType;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundState;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround.BlindValue.Type;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.BlindsAnteParameters;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayersData;

/**
 * Represents a blinds "round". The choice was made to call it a round as it
 * represents a determined step between ante or the hand start and the first bet
 * round.
 * 
 * @author Pierre Mardon
 * 
 */
public class BlindsRound implements Cloneable {

	private final int nbPlayers;
	private final int[] bets;
	private final int[] stacks;
	private final boolean[] inHand;
	private final boolean[] shouldPostEnteringBb;
	private final boolean[] payed;
	private final int sbValue, bbValue, bbIndex, sbIndex;
	private final List<Move<Integer>> seq = new LinkedList<>();
	private RoundState state = RoundState.WAITING_MOVE;

	private BlindsRound(BlindsRound src) {
		this.nbPlayers = src.nbPlayers;
		this.bets = src.bets.clone();
		this.stacks = src.stacks.clone();
		this.inHand = src.inHand.clone();
		this.shouldPostEnteringBb = src.shouldPostEnteringBb.clone();
		this.payed = src.payed.clone();
		this.sbValue = src.sbValue;
		this.bbValue = src.bbValue;
		this.bbIndex = src.bbIndex;
		this.sbIndex = src.sbIndex;
		this.seq.addAll(src.seq);
		this.state = src.state;
	}

	/**
	 * Constructor.
	 * 
	 * @param initState
	 *            the parameters for initializing this blinds round
	 * 
	 */
	public BlindsRound(BlindsAnteParameters initState) {
		checkNotNull(initState);
		inHand = checkNotNull(initState.getInHand()).clone();
		stacks = checkNotNull(initState.getStacks()).clone();
		this.shouldPostEnteringBb = checkNotNull(
				initState.getShouldPostEnteringBb()).clone();
		bbIndex = initState.getBbIndex();
		bbValue = initState.getBbValue();
		sbIndex = initState.getSbIndex();
		sbValue = initState.getSbValue();
		nbPlayers = inHand.length;
		checkArgument(nbPlayers == stacks.length
				&& nbPlayers == shouldPostEnteringBb.length);
		bets = new int[nbPlayers];
		int player = 0;
		while (!inHand[player])
			player++;
		checkArgument(player < nbPlayers, "No player in hand !");
		int inHandPl = 0;
		for (int i = 0; i < nbPlayers; i++)
			if (inHand[i]) {
				checkArgument(stacks[i] > 0,
						"In hand player %s invalid stack %s ", i, stacks[i]);
				inHandPl++;
			}
		checkArgument(inHandPl > 1, "Not enough players in hand");
		checkArgument(bbIndex >= 0 && bbIndex < nbPlayers, "Wrong bb index %s",
				bbIndex);
		checkArgument(inHand[bbIndex], "Bb %s is not in hand", bbIndex);
		if (sbIndex >= 0) {
			checkArgument(sbIndex < nbPlayers, "Wrong sb index %s", sbIndex);
			checkArgument(inHand[sbIndex], "Sb %s is not in hand", sbIndex);
			checkArgument(sbIndex != bbIndex, "Sb index %s is the same as bb",
					sbIndex);
		}
		payed = new boolean[nbPlayers];
	}

	/**
	 * Make blinds payments expire, setting out of hand all not paying players
	 */
	public void expiration() {
		for (int p = 0; p < nbPlayers; p++)
			if (inHand[p] && !payed[p] && p == sbIndex || p == bbIndex
					|| shouldPostEnteringBb[p])
				inHand[p] = false;
		updateState();
	}

	/**
	 * Get the current {@link RoundState}
	 * 
	 * @return the current {@link RoundState}
	 */
	public RoundState getState() {
		return state;
	}

	private void updateState() {
		if (inHand[bbIndex] && !allBlindsPayed())
			return;
		int nbPlNotAllIn = 0;
		int nbPl = 0;
		int lastNotAllIn = 0;
		int maxBet = 0;
		for (int p = 0; p < nbPlayers; p++) {
			if (inHand[p]) {
				maxBet = Math.max(bets[p], maxBet);
				nbPl++;
				if (stacks[p] > 0) {
					nbPlNotAllIn++;
					lastNotAllIn = p;
				}
			}
		}
		if (nbPl < 2 || !inHand[bbIndex])
			this.state = RoundState.CANCELED;
		else if (nbPlNotAllIn > 1)
			this.state = RoundState.NEXT_ROUND;
		else if (nbPlNotAllIn == 1 && bets[lastNotAllIn] < maxBet)
			this.state = RoundState.NEXT_ROUND;
		else
			this.state = RoundState.SHOWDOWN;
	}

	/**
	 * Check if optional sb and bb players payed their blinds
	 * 
	 * @return true when mandatory blinds were payed
	 */
	public boolean mandatoryBlindsPayed() {
		if (sbIndex >= 0 && !payed[sbIndex])
			return false;
		return payed[bbIndex];
	}

	/**
	 * Check if all potential blinds payer did pay
	 * 
	 * @return true when all in hand players payed their blinds when they have
	 *         to
	 */
	public boolean allBlindsPayed() {
		if (sbIndex >= 0 && !payed[sbIndex])
			return false;
		if (!payed[bbIndex])
			return false;
		for (int i = 0; i < nbPlayers; i++)
			if (i != sbIndex && i != bbIndex && inHand[i] && !payed[i]
					&& shouldPostEnteringBb[i])
				return false;
		return true;
	}

	/**
	 * Get the blind value for a given player that is expected to pay it
	 * 
	 * @param playerIndex
	 *            the target player
	 * @return the blind value
	 */
	public BlindValue getBlindValueForPlayer(int playerIndex) {
		checkArgument(playerIndex >= 0 && playerIndex < nbPlayers,
				"Invalid player index %s", playerIndex);
		checkArgument(inHand[playerIndex], "Player %s is not in hand",
				playerIndex);
		int bb = payed[playerIndex] ? bets[playerIndex] : Math.min(bbValue,
				stacks[playerIndex]);
		int sb = payed[playerIndex] ? bets[playerIndex] : Math.min(sbValue,
				stacks[playerIndex]);
		if (shouldPostEnteringBb[playerIndex] || playerIndex == bbIndex)
			return new BlindValue(Type.BB, bb);
		if (playerIndex == sbIndex)
			return new BlindValue(Type.SB, sb);
		throw new IllegalArgumentException("This player cannot pay blinds");
	}

	/**
	 * Perform a blind move : pay SB, BB or refuse to pay blinds
	 * 
	 * @param move
	 *            the move to perform
	 */
	public void doMove(Move<Integer> move) {
		checkState(state == RoundState.WAITING_MOVE,
				"Current blinds round state is %s", state);
		checkNotNull(move, "Move is null");
		checkArgument(move.getType() == MoveType.BB
				|| move.getType() == MoveType.SB
				|| move.getType() == MoveType.NO_BLIND, "Wrong move type %s",
				move.getType());
		int p = move.getPlayerId();
		checkArgument(p >= 0 && p < nbPlayers, "Invalid player index %s", p);
		checkArgument(inHand[p], "Player %s is not in hand", p);
		checkArgument(!payed[p], "Player %s already payed", p);
		switch (move.getType()) {
		case BB:
			checkArgument(bbIndex == p || shouldPostEnteringBb[p],
					"Player %s cannot pay big blind", p);
			checkArgument(move.getValue() == Math.min(bbValue, stacks[p]),
					"Wrong big blind value %s", move.getValue());
			payed[p] = true;
			stacks[p] -= (bets[p] = move.getValue());
			seq.add(move);
			updateState();
			return;
		case SB:
			checkArgument(sbIndex == p && !shouldPostEnteringBb[p],
					"Player %s cannot pay small blind", p);
			checkArgument(move.getValue() == Math.min(sbValue, stacks[p]),
					"Wrong small blind value %s", move.getValue());
			payed[p] = true;
			stacks[p] -= (bets[p] = move.getValue());
			seq.add(move);
			updateState();
			return;
		case NO_BLIND:
			checkArgument(bbIndex == p || sbIndex == p
					|| shouldPostEnteringBb[p],
					"Player %s has no blinds to pay", p);
			if (p == sbIndex && !shouldPostEnteringBb[p])
				checkArgument(move.getValue() == Math.min(sbValue, stacks[p]),
						"Wrong value for move no blind of sb");
			else
				checkArgument(move.getValue() == Math.min(bbValue, stacks[p]),
						"Wrong value for move no blind");
			inHand[p] = false;
			seq.add(move);
			updateState();
			return;
		default:
			throw new IllegalArgumentException("Should never happen !");
		}
	}

	/**
	 * Get the list of moves performed during this blinds round
	 * 
	 * @return the list of moves
	 */
	public List<Move<Integer>> getMoves() {
		return Collections.unmodifiableList(seq);
	}

	/**
	 * Get the players data (inHand, stacks, bets)
	 * 
	 * @return the players data
	 */
	public PlayersData getData() {
		boolean[] currInHand = inHand.clone();
		for (int i = 0; i < nbPlayers; i++) {
			if (!currInHand[i])
				continue;
			if (i == sbIndex || i == bbIndex || shouldPostEnteringBb[i])
				currInHand[i] = payed[i];
		}
		return new PlayersData(currInHand, stacks.clone(), bets.clone());
	}

	/**
	 * Get the list of players that should pay the bb to enter the hand
	 * 
	 * @return the list of players
	 */
	public List<Integer> getMissingEnteringBbPlayers() {
		List<Integer> res = new LinkedList<>();
		for (int i = 0; i < nbPlayers; i++)
			if (inHand[i] && !payed[i] && shouldPostEnteringBb[i])
				res.add(i);
		return res;
	}

	/**
	 * Check if the designated big blind player payed his blind
	 * 
	 * @return true when the bb player payed his blind
	 */
	public boolean hasBbPayed() {
		return payed[bbIndex];
	}

	/**
	 * Check if the designated small blind player payed his blind
	 * 
	 * @return true when the sb player exists and payed his blind
	 */
	public boolean hasSbPayed() {
		return sbIndex >= 0 && payed[sbIndex];
	}

	@Override
	public BlindsRound clone() {
		return new BlindsRound(this);
	}
}
