package net.funkyjava.gametheory.cscfrm.games.kuhnpoker;

import net.funkyjava.gametheory.commonmodel.game.CyclicStepsGamePositionMapper;


/**
 * The position mapper for SNG kuhn poker steps.
 * 
 * @author Pierre Mardon
 */
public class SNGKuhnPokerPositionMapper implements
		CyclicStepsGamePositionMapper {

	/**
	 * The Constructor.
	 */
	public SNGKuhnPokerPositionMapper() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.itf.cyclicsteps.CyclicStepsPositionMapper#
	 * getNextPlayerPosition(int, int, int)
	 */
	@Override
	public int getNextPlayerPosition(int fromStep, int toStep, int fromPos) {
		return fromPos == 0 ? 1 : 0;
	}

}
