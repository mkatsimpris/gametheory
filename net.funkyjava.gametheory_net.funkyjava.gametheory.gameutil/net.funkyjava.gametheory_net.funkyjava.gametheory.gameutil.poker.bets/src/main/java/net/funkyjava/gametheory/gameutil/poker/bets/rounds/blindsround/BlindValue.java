/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a blind value a player is expected to pay
 * 
 * @author Pierre Mardon
 * 
 */
@Data
@AllArgsConstructor
public class BlindValue {

	/**
	 * Type of the blind
	 * 
	 * @author Pierre Mardon
	 * 
	 */
	public static enum Type {
		/**
		 * Small blind
		 */
		SB,
		/**
		 * Big blind
		 */
		BB
	}

	private Type type;
	private int value;

}
