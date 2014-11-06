/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.moves;

import lombok.Getter;
import lombok.ToString;

/**
 * The move class
 * 
 * @author Pierre Mardon
 * 
 * @param <PlayerId>
 *            the players ids class
 */
@ToString
public class Move<PlayerId> {

	/**
	 * The move's player id
	 */
	@Getter
	private PlayerId playerId;

	/**
	 * The move's type
	 */
	@Getter
	private MoveType type;

	/**
	 * The move's value
	 */
	@Getter
	private int value;

	/**
	 * The previous bet of the player in this round. Meaningful only for call
	 * and raise
	 */
	@Getter
	private int oldBet;

	private Move(PlayerId playerId, MoveType type, int value, int oldBet) {
		this.playerId = playerId;
		this.type = type;
		this.value = value;
		this.oldBet = oldBet;
	}

	/**
	 * Get the chips added by the player to its current bet by this move
	 * 
	 * @return the added chips
	 */
	public int addedChips() {
		return value - oldBet;
	}

	/**
	 * Get a copy of this move for another id type
	 * 
	 * @param <Id>
	 *            class of the target player id
	 * @param id
	 *            value of the target player id
	 * @return the same move with the new id
	 */
	public <Id> Move<Id> getCopy(Id id) {
		return new Move<>(id, type, value, oldBet);
	}

	/**
	 * Get fold move
	 * 
	 * @param <PlayerId>
	 *            the players ids class
	 * @param playerId
	 *            the move's player id
	 * @return the fold move for this player
	 */
	public static <PlayerId> Move<PlayerId> getFold(PlayerId playerId) {
		return new Move<>(playerId, MoveType.FOLD, -1, -1);
	}

	/**
	 * Get a bet move
	 * 
	 * @param <PlayerId>
	 *            the players ids class
	 * @param playerId
	 *            the move's player id
	 * @param value
	 *            the bet value
	 * @return the bet move
	 */
	public static <PlayerId> Move<PlayerId> getBet(PlayerId playerId, int value) {
		return new Move<>(playerId, MoveType.BET, value, 0);
	}

	/**
	 * Get a call move
	 * 
	 * @param <PlayerId>
	 *            the players ids class
	 * @param playerId
	 *            the move's player id
	 * @param value
	 *            the call value
	 * @param oldBet
	 *            the bet value of the player before this call
	 * @return the call move
	 */
	public static <PlayerId> Move<PlayerId> getCall(PlayerId playerId,
			int value, int oldBet) {
		return new Move<>(playerId, MoveType.CALL, value, oldBet);
	}

	/**
	 * Get a raise move
	 * 
	 * @param <PlayerId>
	 *            the players ids class
	 * @param playerId
	 *            the move's player id
	 * @param value
	 *            the raise value
	 * @param oldBet
	 *            the bet value of the player before this raise
	 * @return the raise move
	 */
	public static <PlayerId> Move<PlayerId> getRaise(PlayerId playerId,
			int value, int oldBet) {
		return new Move<>(playerId, MoveType.RAISE, value, oldBet);
	}

	/**
	 * Get an ante move
	 * 
	 * @param <PlayerId>
	 *            the players ids class
	 * @param playerId
	 *            the move's player id
	 * @param value
	 *            the ante value
	 * @return the ante move
	 */
	public static <PlayerId> Move<PlayerId> getAnte(PlayerId playerId, int value) {
		return new Move<>(playerId, MoveType.ANTE, value, 0);
	}

	/**
	 * Get an ante pay refuse move
	 * 
	 * @param <PlayerId>
	 *            the players ids class
	 * @param playerId
	 *            the move's player id
	 * @return the no ante move
	 */
	public static <PlayerId> Move<PlayerId> getNoAnte(PlayerId playerId) {
		return new Move<>(playerId, MoveType.NO_ANTE, 0, 0);
	}

	/**
	 * Get a small blind move
	 * 
	 * @param <PlayerId>
	 *            the players ids class
	 * @param playerId
	 *            the move's player id
	 * @param value
	 *            the small blind value
	 * @return the small blind move
	 */
	public static <PlayerId> Move<PlayerId> getSb(PlayerId playerId, int value) {
		return new Move<>(playerId, MoveType.SB, value, 0);
	}

	/**
	 * Get a big blind move
	 * 
	 * @param <PlayerId>
	 *            the players ids class
	 * @param playerId
	 *            the move's player id
	 * @param value
	 *            the big blind value
	 * @return the big blind move
	 */
	public static <PlayerId> Move<PlayerId> getBb(PlayerId playerId, int value) {
		return new Move<>(playerId, MoveType.BB, value, 0);
	}

	/**
	 * Get an blind pay refuse move
	 * 
	 * @param <PlayerId>
	 *            the players ids class
	 * @param playerId
	 *            the move's player id
	 * @return the no-blind move
	 */
	public static <PlayerId> Move<PlayerId> getNoBlind(PlayerId playerId) {
		return new Move<>(playerId, MoveType.NO_BLIND, 0, 0);
	}
}
