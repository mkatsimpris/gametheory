/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The allowed call value for a player expected to do a move
 * 
 * @author Pierre Mardon
 * 
 */
@Data
@AllArgsConstructor
public class CallValue {

	private int value;
	private int toAdd;

	/**
	 * Check if the call is a check
	 * 
	 * @return true when the call is a check
	 */
	public boolean isCheck() {
		return toAdd == 0;
	}

	/**
	 * Check if the call value is valid
	 * 
	 * @return true when valid
	 */
	public boolean isValid() {
		return value >= 0 && toAdd <= value;
	}

	/**
	 * Get an invalid call value
	 * 
	 * @return an invalid call value
	 */
	public static CallValue getNoCall() {
		return new CallValue(-1, -1);
	}
}
