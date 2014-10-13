package net.funkyjava.gametheory.cscfrm.util.game.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.commonmodel.game.CyclicStepsGamePositionMapper;

import org.junit.Test;

/**
 * Test class for {@link CyclicStepsGamePositionMapperValidator}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class CyclicStepsGamePositionMapperValidatorTest {

	private static class IdentityMapper implements
			CyclicStepsGamePositionMapper {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.funkyjava.cscfrm.game.itf.generic.CyclicStepsGamePositionMapper
		 * #getNextPlayerPosition(int, int, int)
		 */
		@Override
		public int getNextPlayerPosition(int fromStep, int toStep, int fromPos) {
			return fromPos;
		}

	}

	private static class ExceptionMapper implements
			CyclicStepsGamePositionMapper {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.funkyjava.cscfrm.game.itf.generic.CyclicStepsGamePositionMapper
		 * #getNextPlayerPosition(int, int, int)
		 */
		@Override
		public int getNextPlayerPosition(int fromStep, int toStep, int fromPos) {
			throw new IllegalArgumentException();
		}

	}

	private static class NegativePosMapper implements
			CyclicStepsGamePositionMapper {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.funkyjava.cscfrm.game.itf.generic.CyclicStepsGamePositionMapper
		 * #getNextPlayerPosition(int, int, int)
		 */
		@Override
		public int getNextPlayerPosition(int fromStep, int toStep, int fromPos) {
			return -1;
		}

	}

	private static class HigherPosMapper implements
			CyclicStepsGamePositionMapper {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.funkyjava.cscfrm.game.itf.generic.CyclicStepsGamePositionMapper
		 * #getNextPlayerPosition(int, int, int)
		 */
		@Override
		public int getNextPlayerPosition(int fromStep, int toStep, int fromPos) {
			return Integer.MAX_VALUE;
		}

	}

	private static class StaticPosMapper implements
			CyclicStepsGamePositionMapper {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.funkyjava.cscfrm.game.itf.generic.CyclicStepsGamePositionMapper
		 * #getNextPlayerPosition(int, int, int)
		 */
		@Override
		public int getNextPlayerPosition(int fromStep, int toStep, int fromPos) {
			return 0;
		}

	}

	/**
	 * 
	 * 
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testValidateAllStepsMapping() throws Exception {
		log.info("Testing identity mapper is valid");
		assertTrue("Identity mapper must be valid",
				Valid.isValid(new IdentityMapper(), 10, 10));
		log.info("Testing exception mapper is invalid");
		assertFalse("Validator must fail when mapper throws an exception",
				Valid.isValid(new ExceptionMapper(), 10, 10));
		log.info("Testing negative position mapper is invalid");
		assertFalse(
				"Validator must fail when mapper returns a negative position",
				Valid.isValid(new NegativePosMapper(), 10, 10));
		log.info("Testing negative position mapper is invalid");
		assertFalse(
				"Validator must fail when mapper returns a negative position",
				Valid.isValid(new HigherPosMapper(), 10, 10));
		log.info("Testing static position mapper is invalid");
		assertFalse(
				"Validator must fail when mapper returns always the same position",
				Valid.isValid(new StaticPosMapper(), 10, 10));
	}

	/**
	 * Testing with wrong nb of players
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testValidationIllegalArgNbPlayers() {
		log.info("Testing validator fails with wrong number of players");
		Valid.isValid(new IdentityMapper(), 1, 10);
	}

	/**
	 * Testing with wrong nb of steps
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testValidationIllegalArgNbSteps() {
		log.info("Testing validator fails with wrong number of steps");
		Valid.isValid(new IdentityMapper(), 10, 0);
	}

}
