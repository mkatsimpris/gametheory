package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import static org.junit.Assert.assertTrue;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;
import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig.FlopIndexer;

import org.junit.Test;

/**
 * Test class for {@link EHS_EHS2FlopEvaluator}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class EHS_EHS2FlopEvaluatorTest {

	/**
	 * Test values based on <a href=
	 * "http://www.poker-ai.org/archive/pokerai.org/pf3/viewtopic57d8.html?f=3&t=2764&view=next"
	 * >Indiana results on poker-ai</a> , with big tolerance (1.5E-3) because
	 * its implementation has a bias (euclidian division for ties/2) <br>
	 * <a href=
	 * "http://www.poker-ai.org/archive/www.pokerai.org/pf3/viewtopic1e13-2.html?f=3&t=444&st=0&sk=t&sd=a&start=20"
	 * >Indiana base implementation</a>
	 */
	@Test
	public void testGetValues() {
		log.info("Testing EHS2 values");
		final FlopIndexer indexer = new FlopIndexer();
		indexer.initialize();
		final IntCardsSpec specs = DefaultIntCardsSpecs.getDefault();
		Cards52Strings c = new Cards52Strings(specs);
		log.info("Initializing 2+2 evaluator");
		final TwoPlusTwoEvaluator eval = new TwoPlusTwoEvaluator();
		final EHS_EHS2FlopEvaluator ehs = new EHS_EHS2FlopEvaluator(specs, eval);
		testValue(0.853, 0.733, "AsAd", "4cJh9s", c, ehs);
		testValue(0.829, 0.697, "AhJd", "4cJh9s", c, ehs);
		testValue(0.927, 0.864, "AhJd", "AcJh9s", c, ehs);
		testValue(0.952, 0.911, "JdJc", "4cJh9s", c, ehs);
		testValue(0.94, 0.888, "AhJd", "JcJh9s", c, ehs);
		testValue(0.951, 0.909, "KhQs", "9sTcJh", c, ehs);
		testValue(0.982, 0.967, "AhJh", "9hTh4h", c, ehs);
		testValue(0.994, 0.989, "AhJd", "JcJhAs", c, ehs);
		testValue(1.0, 1.0, "JhJd", "JcJs4s", c, ehs);
		testValue(1.0, 1.0, "KsQs", "9sTsJs", c, ehs);
		testValue(0.709, 0.623, "JsTs", "9s8s3d", c, ehs);
		testValue(0.553, 0.439, "JsTs", "9c8h3d", c, ehs);
		testValue(0.716, 0.584, "AsQs", "Js8s3d", c, ehs);
	}

	private double[] values = new double[2];

	private void testValue(double ehsValue, double ehs2Value, String handStr,
			String flopStr, Cards52Strings c, EHS_EHS2FlopEvaluator ehs) {
		int[][] flop = { c.getCards(handStr), c.getCards(flopStr) };
		ehs.getValues(flop, values, 0);
		assertTrue("For hand " + handStr + " flop " + flopStr
				+ " expected EHS = " + ehsValue + " but got " + values[0],
				Math.abs(values[0] - ehsValue) < 1.5E-3);
		assertTrue("For hand " + handStr + " flop " + flopStr
				+ " expected EHS2 = " + ehs2Value + " but got " + values[1],
				Math.abs(values[1] - ehs2Value) < 1.5E-3);
	}

	/**
	 * Monothread performance test
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPerformance() throws Exception {
		log.info("Testing EHS2 evaluator performance");
		final FlopIndexer indexer = new FlopIndexer();
		indexer.initialize();
		final IntCardsSpec specs = DefaultIntCardsSpecs.getDefault();
		Cards52Strings c = new Cards52Strings(specs);
		log.info("Initializing 2+2 evaluator");
		final TwoPlusTwoEvaluator eval = new TwoPlusTwoEvaluator();
		final EHS_EHS2FlopEvaluator ehs = new EHS_EHS2FlopEvaluator(specs, eval);
		int[][] flop = { c.getCards("AsQs"), c.getCards("Js8s3d") };
		for (int j = 0; j < 10; j++) {
			long start = System.currentTimeMillis();
			final int nb = 100;
			for (int i = 0; i < 100; i++)
				ehs.getValues(flop, values, 0);
			long time = System.currentTimeMillis() - start;
			log.info("Performed {} EHS_EHS2 computations in {} ms, {} iter/s",
					nb, time, nb * 1000 / ((double) time));
		}
	}

	// /**
	// * TODO remove this
	// *
	// * @throws Exception
	// */
	// @Test
	// public void testCreateLut() throws Exception {
	// final FlopIndexer indexer = new FlopIndexer();
	// indexer.initialize();
	// final IntCardsSpec specs = DefaultIntCardsSpecs.getDefault();
	// log.info("Initializing 2+2 evaluator");
	// final TwoPlusTwoEvaluatorProvider evalProvider = new
	// TwoPlusTwoEvaluatorProvider();
	// final EHSFlopEvaluatorProvider ehs = new EHSFlopEvaluatorProvider(
	// specs, evalProvider);
	// Double52CardsLUTBuilder.buildAndWriteLUT(indexer, ehs,
	// new int[] { 2, 3 }, 7, false, "HE_POKER_FLOP",
	// Paths.get("/home/pitt/EHS2_FLOP_LUT.dat"), true);
	// }
}
