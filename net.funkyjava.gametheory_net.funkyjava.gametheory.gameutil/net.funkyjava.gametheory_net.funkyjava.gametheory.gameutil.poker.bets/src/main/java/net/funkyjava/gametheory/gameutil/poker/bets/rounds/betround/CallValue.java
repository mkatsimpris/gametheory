/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Pierre Mardon
 * 
 */
@Data
@AllArgsConstructor
public class CallValue {

	private int value;
	private int toAdd;

	public boolean isCheck() {
		return toAdd == 0;
	}

	public boolean isValid() {
		return value >= 0 && toAdd >= value;
	}

	public static CallValue getNoCall() {
		return new CallValue(-1, -1);
	}
}
