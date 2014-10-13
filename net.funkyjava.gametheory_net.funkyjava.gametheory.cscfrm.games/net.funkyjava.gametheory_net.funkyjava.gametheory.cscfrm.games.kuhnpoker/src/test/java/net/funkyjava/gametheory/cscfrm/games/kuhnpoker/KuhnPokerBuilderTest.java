package net.funkyjava.gametheory.cscfrm.games.kuhnpoker;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMTerminalUtilReader;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.provider.DefaultNodesProvider;
import net.funkyjava.gametheory.cscfrm.util.game.validation.Valid;

import org.junit.Test;

/**
 * Test class for {@link KuhnPokerBuilder}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class KuhnPokerBuilderTest {

	private static final int nbIter = 100000;

	/**
	 * Testing KuhnPokerBuilder for SNG
	 * 
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testKuhnPokerBuilderIntInt() throws Exception {
		final int totBlinds = 10;
		CSCFRMTerminalUtilReader utilMock = new CSCFRMTerminalUtilReader() {

			@Override
			public void read(int id, double[] dest) throws InterruptedException {
				if (id < 0 || id > totBlinds - 6)
					throw new IllegalArgumentException(
							"Steps ids in terminal nodes don't seem good, saw id "
									+ id);

			}
		};
		Random r = new Random();
		int blinds1 = 3 + r.nextInt(totBlinds - 6);
		int blinds2 = totBlinds - blinds1;
		log.info(
				"Testing KuhnPoker SNG step with stacks {} and {} for {} iterations",
				blinds1, blinds2, nbIter);
		assertTrue("SNG KuhnPoker builder isn't valid !", Valid.isValid(
				new KuhnPokerBuilder<DefaultPlayerNode>(blinds1, blinds2),
				utilMock, new DefaultNodesProvider(), nbIter));
	}

	/**
	 * Testing Kuhn poker builder for classic game
	 * 
	 * @throws Exception
	 *             unexepected exception
	 */
	@Test
	public void testKuhnPokerBuilder() throws Exception {
		log.info("Testing KuhnPoker classic for {} iterations", nbIter);
		assertTrue("Classic kuhn poker builder didn't pass the validation",
				Valid.isValid(new KuhnPokerBuilder<DefaultPlayerNode>(),
						new DefaultNodesProvider(), nbIter));
	}
}
