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
 * Test class for {@link KuhnPoker}
 * 
 * @author Pierre Mardon
 * 
 *         TODO validate error cases (wrong nb of blinds...)
 * 
 */
@Slf4j
public class KuhnPokerTest {

	private static final int nbIter = 100000;

	/**
	 * Validate the game with classic payoffs
	 */
	@Test
	public void testKuhnPokerNodesProvider() {
		KuhnPoker<DefaultPlayerNode> game = new KuhnPoker<DefaultPlayerNode>(
				new DefaultNodesProvider());
		assertTrue("The validation of Kuhn Poker with classic payoffs failed",
				Valid.isValid(game, 1000));
	}

	/**
	 * Validate a sharing game with classic payoffs
	 */
	@Test
	public void testKuhnPokerNodesProviderKuhnPoker() {
		DefaultNodesProvider provider = new DefaultNodesProvider();
		KuhnPoker<DefaultPlayerNode> baseGame = new KuhnPoker<DefaultPlayerNode>(
				provider);
		KuhnPoker<DefaultPlayerNode> game = new KuhnPoker<DefaultPlayerNode>(
				provider, baseGame);
		assertTrue(
				"The validation of shared Kuhn Poker with classic payoffs failed",
				Valid.isValid(game, 1000));
	}

	/**
	 * Test a random SNG kuhn poker step
	 */
	@Test
	public void testKuhnPokerNodesProviderIntInt() {
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
		assertTrue("SNG KuhnPoker isn't valid !", Valid.isValid(
				new KuhnPoker<DefaultPlayerNode>(new DefaultNodesProvider(),
						blinds1, blinds2), utilMock, nbIter));
	}

	/**
	 * 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testKuhnPokerIllegalArguments1() {
		DefaultNodesProvider provider = new DefaultNodesProvider();
		new KuhnPoker<DefaultPlayerNode>(provider, 2, 75);
	}

	/**
	 * 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testKuhnPokerIllegalArguments2() {
		DefaultNodesProvider provider = new DefaultNodesProvider();
		new KuhnPoker<DefaultPlayerNode>(provider, 75, 2);
	}

	/**
	 * 
	 */
	@Test(expected = NullPointerException.class)
	public void testKuhnPokerIllegalArguments3() {
		new KuhnPoker<DefaultPlayerNode>(null);
	}
}
