/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.games.poker.nlhe.pushfold;

import net.funkyjava.gametheory.commonmodel.game.CyclicStepsGamePositionMapper;


/**
 * Simple position mapper for SNG HU poker
 * 
 * @author Pierre Mardon
 * 
 */
public class HUPositionMapper implements CyclicStepsGamePositionMapper {

	/**
	 * The constructor
	 */
	public HUPositionMapper() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.generic.CyclicStepsGamePositionMapper#
	 * getNextPlayerPosition(int, int, int)
	 */
	@Override
	public int getNextPlayerPosition(int fromStep, int toStep, int fromPos) {
		return fromPos == 0 ? 1 : 0;
	}

}
