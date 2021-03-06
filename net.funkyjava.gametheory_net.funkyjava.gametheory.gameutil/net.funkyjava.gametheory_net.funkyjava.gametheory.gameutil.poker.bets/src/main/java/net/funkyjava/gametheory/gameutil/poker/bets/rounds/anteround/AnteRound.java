/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.anteround;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.NonNull;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.MoveType;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundState;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.BlindsAnteParameters;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayersData;

/**
 * State machine for an ante round
 * 
 * @author Pierre Mardon
 * 
 */
public class AnteRound implements Cloneable {
	private final int nbPlayers;
	private final int[] bets;
	private final int[] stacks;
	private final boolean[] inHand;
	private final boolean[] payed;
	private final int bbIndex;
	private final int ante;
	private final List<Move<Integer>> seq = new LinkedList<>();
	private RoundState state = RoundState.WAITING_MOVE;

	/**
	 * Constructor
	 * 
	 * @param data
	 *            the round's parameters
	 */
	public AnteRound(@NonNull BlindsAnteParameters data) {
		inHand = data.getInHand().clone();
		stacks = data.getStacks().clone();
		nbPlayers = inHand.length;
		checkArgument(nbPlayers == stacks.length);
		checkArgument(data.getBbIndex() >= 0 && data.getBbIndex() < nbPlayers);
		bbIndex = data.getBbIndex();
		bets = new int[nbPlayers];
		int inHandPl = 0;
		for (int i = 0; i < nbPlayers; i++)
			if (inHand[i]) {
				checkArgument(stacks[i] > 0,
						"In hand player %s invalid stack %s ", i, stacks[i]);
				inHandPl++;
			}
		checkArgument(inHandPl > 1, "Not enough players in hand");
		payed = new boolean[nbPlayers];
		this.ante = data.getSpecs().getAnteValue();
		checkArgument(ante > 0, "Ante must be > 0, found %s", ante);
	}

	private AnteRound(AnteRound src) {
		inHand = src.inHand.clone();
		stacks = src.stacks.clone();
		nbPlayers = src.nbPlayers;
		bbIndex = src.bbIndex;
		bets = src.bets.clone();
		payed = src.payed.clone();
		ante = src.ante;
		state = src.state;
		seq.addAll(src.seq);
	}

	/**
	 * Get current {@link RoundState}
	 * 
	 * @return the current {@link RoundState}
	 */
	public RoundState getState() {
		return state;
	}

	/**
	 * Check is the ante round is finished
	 * 
	 * @return true when all in-hand players have payed
	 */
	public boolean finished() {
		for (int i = 0; i < nbPlayers; i++)
			if (inHand[i] && !payed[i])
				return false;
		return true;
	}

	/**
	 * Get the ante value a target player should pay
	 * 
	 * @param playerIndex
	 *            the player's index
	 * @return the ante value
	 */
	public AnteValue getAnteValueForPlayer(int playerIndex) {
		checkArgument(playerIndex >= 0 && playerIndex < nbPlayers,
				"Invalid player index %s", playerIndex);
		checkArgument(inHand[playerIndex], "Player %s is not in hand",
				playerIndex);
		return payed[playerIndex] ? new AnteValue(bets[playerIndex])
				: new AnteValue(Math.min(ante, stacks[playerIndex]));
	}

	/**
	 * Pay the ante for one player, or refuse to pay
	 * 
	 * @param move
	 *            the ante move
	 */
	public void doMove(@NonNull Move<Integer> move) {
		checkState(state == RoundState.WAITING_MOVE,
				"Current ante round state is %s", state);
		checkArgument(move.getType() == MoveType.ANTE
				|| move.getType() == MoveType.NO_ANTE, "Wrong move type %s",
				move.getType());
		int p = move.getPlayerId();
		checkArgument(p >= 0 && p < nbPlayers, "Invalid player index %s", p);
		checkArgument(inHand[p], "Player %s is not in hand", p);
		checkArgument(!payed[p], "Player %s already payed", p);
		if (move.getType() == MoveType.ANTE) {
			checkArgument(move.getValue() == Math.min(ante, stacks[p]),
					"Wrong move value");
			payed[p] = true;
			stacks[p] -= (bets[p] = move.getValue());
		} else {
			inHand[p] = false;
		}
		seq.add(move);
		updateState();
	}

	/**
	 * Only for cash game : make players that didn't pay expire
	 */
	public void expiration() {
		for (int p = 0; p < nbPlayers; p++)
			if (inHand[p] && !payed[p])
				inHand[p] = false;
		updateState();
	}

	private void updateState() {
		if (!finished())
			return;
		int nbPlNotAllIn = 0;
		int nbPl = 0;
		for (int p = 0; p < nbPlayers; p++)
			if (inHand[p]) {
				nbPl++;
				if (stacks[p] > 0)
					nbPlNotAllIn++;
			}
		if (nbPl < 2 || !payed[bbIndex])
			this.state = RoundState.CANCELED;
		else if (nbPlNotAllIn > 1)
			this.state = RoundState.NEXT_ROUND;
		else
			this.state = RoundState.SHOWDOWN;
	}

	/**
	 * Get the current list of moves of this ante round
	 * 
	 * @return the list of ante moves performed
	 */
	public List<Move<Integer>> getMoves() {
		return Collections.unmodifiableList(seq);
	}

	/**
	 * Get the current {@link PlayersData}
	 * 
	 * @return the players data
	 */
	public PlayersData getData() {
		boolean[] currInHand = inHand.clone();
		for (int i = 0; i < nbPlayers; i++)
			currInHand[i] = payed[i] && currInHand[i];
		return new PlayersData(currInHand, stacks.clone(), bets.clone());
	}

	/**
	 * Get the list of players that are in hand and didn't pay their antes
	 * 
	 * @return the list of players that are in hand and didn't pay their antes
	 */
	public List<Integer> getMissingAntePlayers() {
		List<Integer> res = new LinkedList<>();
		for (int i = 0; i < nbPlayers; i++)
			if (inHand[i] && !payed[i])
				res.add(i);
		return res;
	}

	/**
	 * Get the list of players that are in hand and did pay their antes
	 * 
	 * @return the list of players that are in hand and did pay their antes
	 */
	public List<Integer> getPayedAntePlayers() {
		List<Integer> res = new LinkedList<>();
		for (int i = 0; i < nbPlayers; i++)
			if (inHand[i] && payed[i])
				res.add(i);
		return res;
	}

	@Override
	public AnteRound clone() {
		return new AnteRound(this);
	}
}