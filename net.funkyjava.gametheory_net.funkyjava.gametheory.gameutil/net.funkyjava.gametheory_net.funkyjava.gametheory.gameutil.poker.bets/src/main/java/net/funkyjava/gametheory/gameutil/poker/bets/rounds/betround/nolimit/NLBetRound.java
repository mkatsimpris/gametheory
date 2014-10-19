/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.nolimit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.ToString;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundState;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetChoice;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetRange;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetRoundStartData;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.CallValue;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.RaiseRange;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayersData;

/**
 * @author Pierre Mardon
 * 
 */
@ToString
public class NLBetRound implements Cloneable {

	private final boolean[] inHand;
	private final boolean[] played;
	private final int[] stacks;
	private final int[] bets;
	private final int[] playersBetSubRound;
	private final int bigBlind;
	private final int nbPlayers;
	private int player;
	private int highestBet, lastRaise;
	private final int firstBetSubRound;
	private int betSubRound;
	private RoundState state;
	private final List<Move<Integer>> seq = new LinkedList<>();

	public NLBetRound(BetRoundStartData startRoundData) {
		checkStartData(startRoundData);
		seq.clear();
		nbPlayers = startRoundData.getStacks().length;
		this.stacks = startRoundData.getStacks().clone();
		this.bets = startRoundData.getBets().clone();
		this.inHand = startRoundData.getInHand().clone();
		played = new boolean[nbPlayers];
		playersBetSubRound = new int[nbPlayers];
		lastRaise = this.bigBlind = startRoundData.getBigBlind();
		highestBet = 0;
		for (int i = 0; i < nbPlayers; i++)
			if (bets[i] > highestBet)
				highestBet = bets[i];
		firstBetSubRound = betSubRound = highestBet > 0 ? 1 : 0;
		if (highestBet > 0)
			highestBet = Math.max(highestBet, bigBlind);
		player = startRoundData.getFirstPlayerIndex() - 1;
		if (player < 0)
			player += nbPlayers;
		goToNextState();
	}

	private NLBetRound(NLBetRound source) {
		this.seq.addAll(source.seq);
		this.bets = source.bets.clone();
		this.betSubRound = source.betSubRound;
		this.firstBetSubRound = source.firstBetSubRound;
		this.bigBlind = source.bigBlind;
		this.highestBet = source.highestBet;
		this.inHand = source.inHand.clone();
		this.lastRaise = source.lastRaise;
		this.nbPlayers = source.nbPlayers;
		this.played = source.played.clone();
		this.player = source.player;
		this.playersBetSubRound = source.playersBetSubRound.clone();
		this.stacks = source.stacks.clone();
		this.state = source.state;
	}

	private static void checkStartData(BetRoundStartData data) {
		int firstPlayerIndex = data.getFirstPlayerIndex();
		int bigBlind = data.getBigBlind();
		checkNotNull(data.getStacks(), "Provided stacks are null");
		checkNotNull(data.getBets(), "Provided blinds are null");
		checkNotNull(data.getInHand(),
				"Provided 'inHand' booleans array is null");
		int nbPlayers = data.getStacks().length;
		checkArgument(
				nbPlayers == data.getBets().length
						&& nbPlayers == data.getInHand().length,
				"Stacks, blinds and 'inHand' array must have the same length (= number of players)");
		checkArgument(firstPlayerIndex >= 0 && firstPlayerIndex < nbPlayers,
				"First player index must be between 0 and %s", nbPlayers);
		checkArgument(bigBlind > 0, "Big blind must be > 0");
		checkArgument(data.getInHand()[firstPlayerIndex],
				"First player must be in hand");
		checkArgument(data.getStacks()[firstPlayerIndex] > 0,
				"First player must not be all-in");
		int nbInHandPlayers = 0;
		int highestBet = 0;
		for (int i = 0; i < nbPlayers; i++) {
			checkArgument(data.getStacks()[i] >= 0,
					"Player %s has a negative stack", i);
			checkArgument(data.getBets()[i] >= 0,
					"Blinds for player %s are negative", i);
			checkArgument(bigBlind >= data.getBets()[i],
					"Player %s has a blind > bigblind");
			highestBet = Math.max(highestBet, data.getBets()[i]);
			checkArgument(data.getInHand()[i] || data.getBets()[i] == 0,
					"Player %s cannot have blinds as he is not in hand", i);
			if (data.getInHand()[i] && data.getStacks()[i] > 0)
				nbInHandPlayers++;
		}
		checkArgument(nbInHandPlayers > 1
				|| data.getBets()[firstPlayerIndex] < highestBet,
				"It seems like a direct showdown...");
	}

	public PlayersData getData() {
		return new PlayersData(inHand.clone(), stacks.clone(), bets.clone());
	}

	public int getNbMoves() {
		return seq.size();
	}

	public List<Move<Integer>> getMoves() {
		return Collections.unmodifiableList(seq);
	}

	public RoundState getState() {
		return state;
	}

	public int getCurrentPlayer() {
		checkState(state == RoundState.WAITING_MOVE,
				"Wrong state %s to ask for active player", state);
		checkState(player >= 0 && player < nbPlayers,
				"Internal error : Invalid player index %s, nbPlayers ", player,
				nbPlayers);
		return player;
	}

	public RaiseRange getRaiseRange() {
		checkState(state == RoundState.WAITING_MOVE,
				"Wrong state %s to ask for possible moves", state);
		int fullStack = bets[player] + stacks[player];
		if (fullStack <= highestBet
				|| playersBetSubRound[player] == betSubRound)
			return RaiseRange.getNoRange();
		if (fullStack <= highestBet + lastRaise)
			return RaiseRange.getSingleton(bets[player], fullStack);
		return new RaiseRange(bets[player], highestBet + lastRaise, fullStack);
	}

	public CallValue getCallValue() {
		checkState(state == RoundState.WAITING_MOVE,
				"Wrong state %s to ask for possible moves", state);
		int call = Math.min(stacks[player] + bets[player], highestBet);
		return new CallValue(call, call - bets[player]);
	}

	public BetRange getBetRange() {
		if (betSubRound > 0)
			return BetRange.getNoRange();
		return new BetRange(Math.min(stacks[player], bigBlind), stacks[player]);
	}

	public void doMove(Move<Integer> m) {
		checkState(state == RoundState.WAITING_MOVE,
				"Round state is %s, cannot do any move.", state);
		checkArgument(m.getPlayerId() == player,
				"Wrong player %s for this move, expected %s", m.getPlayerId(),
				player);
		int val = m.getValue();
		switch (m.getType()) {
		case BET:
			checkState(betSubRound == 0,
					"Can't bet, maybe you mean call or raise");
			checkState(bets[player] == 0, "This player has already betted");
			checkArgument(val >= bigBlind || val == stacks[player],
					"Incorrect value for player %s bet of %s, stack %s",
					player, val, stacks[player]);
			checkArgument(m.getOldBet() == bets[player]);
			doBet(val);
			break;
		case CALL:
			checkArgument(
					highestBet == val
							|| (stacks[player] + bets[player] == val && val < highestBet),
					"Wrong call value %s", val);
			checkArgument(m.getOldBet() == bets[player]);
			doCall(val);
			break;
		case RAISE:
			RaiseRange raiseTo = getRaiseRange();
			checkState(raiseTo.exists(), "Player %s can't raise !", player);
			checkArgument(raiseTo.getMin() <= val && raiseTo.getMax() >= val,
					"Raise %s is invalid, expected between %s and %s",
					raiseTo.getMin(), raiseTo.getMax());
			checkArgument(m.getOldBet() == bets[player]);
			doRaise(val);
			break;
		case FOLD:
			inHand[player] = false;
			break;
		default:
			throw new IllegalArgumentException("Unauthorized move " + m);
		}
		seq.add(m);
		played[player] = true;
		goToNextState();
	}

	private boolean isFullRaise(int val) {
		return val >= highestBet + lastRaise;
	}

	private void doBet(int val) {
		stacks[player] -= val;
		bets[player] = val;
		betSubRound = 1;
		playersBetSubRound[player] = 1;
		lastRaise = highestBet = Math.max(val, bigBlind);
	}

	private void doRaise(int val) {
		stacks[player] -= val - bets[player];
		bets[player] = val;
		if (isFullRaise(val)) {
			playersBetSubRound[player] = ++betSubRound;
			lastRaise = val - highestBet;
			highestBet = val;
			return;
		}
		playersBetSubRound[player] = betSubRound;
		lastRaise += val - highestBet;
		highestBet = val;
	}

	private void doCall(int val) {
		playersBetSubRound[player] = betSubRound;
		stacks[player] -= val - bets[player];
		bets[player] = val;
	}

	private void goToNextState() {
		int p, i;
		int nextPlayer = -1;
		int nbInHand = 0;
		int nbNotAllIn = 0;
		int nbCanPlay = 0;
		for (i = 0; i < nbPlayers; i++) {
			p = (player + i + 1) % nbPlayers;
			if (!inHand[p])
				continue;
			nbInHand++;
			if (stacks[p] == 0)
				continue;
			nbNotAllIn++;
			if (playersBetSubRound[p] < betSubRound

			||

			(playersBetSubRound[p] == betSubRound && bets[p] < highestBet)

			||

			(!played[p]))

			{
				nbCanPlay++;
				if (nextPlayer < 0)
					nextPlayer = p;
			}
		}
		if (nbInHand == 1) {
			state = RoundState.END_NO_SHOWDOWN;
			return;
		}
		if (nbCanPlay > 0) {
			state = RoundState.WAITING_MOVE;
			player = nextPlayer;
			return;
		}
		if (nbNotAllIn <= 1) {
			state = RoundState.SHOWDOWN;
			return;
		}
		state = RoundState.NEXT_ROUND;
	}

	@Override
	public NLBetRound clone() {
		return new NLBetRound(this);
	}

	public BetChoice getBetChoice() {
		checkState(state == RoundState.WAITING_MOVE,
				"Wrong state %s to ask for active player bet choice", state);
		return new BetChoice(getBetRange(), getCallValue(), getRaiseRange(),
				getCurrentPlayer());
	}
}
