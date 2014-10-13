package net.funkyjava.gametheory.cscfrm.games.poker.nlhe.pushfold;

import static org.junit.Assert.assertTrue;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMConfig;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMTerminalUtilReader;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.exe.CSCFRMMonothreadExecutor;
import net.funkyjava.gametheory.cscfrm.impl.exe.DefaultWorkStation;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.provider.DefaultNodesProvider;
import net.funkyjava.gametheory.cscfrm.util.game.validation.Valid;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluator;

import org.junit.Test;

/**
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class NLHEHUPushFoldTest {

	private static final int nbIter = 1000_000;
	private static final CSCFRMTerminalUtilReader utilMock = new CSCFRMTerminalUtilReader() {

		@Override
		public void read(int id, double[] dest) throws InterruptedException {

		}
	};

	/**
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testNLHEHUPushFold() throws Exception {
		NLHEHUPushFold<DefaultPlayerNode> game = new NLHEHUPushFold<>(
				new DefaultNodesProvider(), 5, 10, 200, 200,
				new TwoPlusTwoEvaluator());
		assertTrue("The game " + game.getUId() + " is invalid",
				Valid.isValid(game, nbIter));
		DefaultWorkStation ws = new DefaultWorkStation(2, "Test WS");
		CSCFRMMonothreadExecutor<DefaultPlayerNode, NLHEHUPushFold<DefaultPlayerNode>> exe = ws
				.buildMonothreadExecutor(game, new CSCFRMConfig(),
						new CSCFRMExecutionLoaderConfig(false));
		exe.run(10000000);
		log.info(game.getBinaryStrategiesString(0.5));
		game = new NLHEHUPushFold<>(new DefaultNodesProvider(), 5, 10, 200,
				200, 5, new TwoPlusTwoEvaluator());
		assertTrue("The game " + game.getUId() + " is invalid",
				Valid.isValid(game, utilMock, nbIter));

		game = new NLHEHUPushFold<>(new DefaultNodesProvider(), 5, 10, 5, 200,
				5, new TwoPlusTwoEvaluator());
		assertTrue("The game " + game.getUId() + " is invalid",
				Valid.isValid(game, utilMock, nbIter));

		game = new NLHEHUPushFold<>(new DefaultNodesProvider(), 5, 10, 5, 10,
				5, new TwoPlusTwoEvaluator());
		assertTrue("The game " + game.getUId() + " is invalid",
				Valid.isValid(game, utilMock, nbIter));

	}
}
