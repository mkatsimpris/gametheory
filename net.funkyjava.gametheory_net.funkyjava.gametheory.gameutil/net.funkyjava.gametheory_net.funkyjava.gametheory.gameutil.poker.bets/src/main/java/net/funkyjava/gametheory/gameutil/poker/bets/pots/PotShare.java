/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.pots;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Share from a pot earned by a player
 * 
 * @author Pierre Mardon
 * 
 * @param <Id>
 *            the players ids class
 */
@AllArgsConstructor
public class PotShare<Id> {
	@Getter
	private final int value;
	@Getter
	private final Id player;

}
