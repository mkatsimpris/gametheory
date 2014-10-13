/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.moves;

import lombok.Getter;
import lombok.ToString;

/**
 * @author Pierre Mardon
 * 
 */
@ToString
public class Move<PlayerId> {

	@Getter
	private PlayerId playerId;
	@Getter
	private MoveType type;
	@Getter
	private int value;
	@Getter
	private int oldBet;

	private Move(PlayerId playerId, MoveType type, int value, int oldBet) {
		this.playerId = playerId;
		this.type = type;
		this.value = value;
		this.oldBet = oldBet;
	}

	public int addedChips() {
		return value - oldBet;
	}

	public <Id> Move<Id> getCopy(Id id) {
		return new Move<>(id, type, value, oldBet);
	}

	public static <PlayerId> Move<PlayerId> getFold(PlayerId playerId) {
		return new Move<>(playerId, MoveType.FOLD, -1, -1);
	}

	public static <PlayerId> Move<PlayerId> getBet(PlayerId playerId, int value) {
		return new Move<>(playerId, MoveType.BET, value, 0);
	}

	public static <PlayerId> Move<PlayerId> getCall(PlayerId playerId,
			int value, int oldBet) {
		return new Move<>(playerId, MoveType.CALL, value, oldBet);
	}

	public static <PlayerId> Move<PlayerId> getRaise(PlayerId playerId,
			int value, int oldBet) {
		return new Move<>(playerId, MoveType.RAISE, value, oldBet);
	}

	public static <PlayerId> Move<PlayerId> getAnte(PlayerId playerId, int value) {
		return new Move<>(playerId, MoveType.ANTE, value, 0);
	}

	public static <PlayerId> Move<PlayerId> getSb(PlayerId playerId, int value) {
		return new Move<>(playerId, MoveType.SB, value, 0);
	}

	public static <PlayerId> Move<PlayerId> getBb(PlayerId playerId, int value) {
		return new Move<>(playerId, MoveType.BB, value, 0);
	}
}
