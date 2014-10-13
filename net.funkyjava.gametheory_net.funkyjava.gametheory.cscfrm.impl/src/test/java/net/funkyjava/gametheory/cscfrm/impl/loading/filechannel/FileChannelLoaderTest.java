package net.funkyjava.gametheory.cscfrm.impl.loading.filechannel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMState;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.cscfrm.util.game.helpers.ArraysIterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test class for {@link FileChannelLoader}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class FileChannelLoaderTest {

	private static final int NB_PLN = 5;

	private static int nbLoaders;
	private static Random rand = new Random();
	private static CSCFRMExecutionLoaderConfig[] allConfs;
	private static Path[] allPaths;
	private static FileChannelLoader<DefaultPlayerNode>[] loaders;
	private static DefaultPlayerNode[] baseNodes = new DefaultPlayerNode[NB_PLN];
	private static DefaultPlayerNode[] loadedNodes = new DefaultPlayerNode[NB_PLN];
	private static CSCFRMState baseState = new CSCFRMState(123456789,
			new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });

	/**
	 * The temporary folder
	 */
	@ClassRule
	public static TemporaryFolder folder = new TemporaryFolder();

	/**
	 * Filling all needed objects
	 * 
	 * @throws IOException
	 *             unexpected exception
	 */
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void fillEverything() throws IOException {
		log.info("Filling everything");
		allConfs = new CSCFRMExecutionLoaderConfig[] {
				new CSCFRMExecutionLoaderConfig(false),
				new CSCFRMExecutionLoaderConfig(true) };
		allPaths = new Path[] { folder.getRoot().toPath().resolve("1"),
				folder.getRoot().toPath().resolve("2") };
		nbLoaders = allConfs.length;
		loaders = new FileChannelLoader[nbLoaders];
		for (int i = 0; i < nbLoaders; i++) {
			loaders[i] = new FileChannelLoader<DefaultPlayerNode>(allPaths[i],
					allConfs[i]);
		}
		for (int i = 0; i < NB_PLN; i++) {
			loadedNodes[i] = new DefaultPlayerNode(i, i + 2);
			baseNodes[i] = new DefaultPlayerNode(i, i + 2);
			baseNodes[i].visits = rand.nextLong();
			baseNodes[i].realWeightSum = rand.nextDouble();
			for (int j = 0; j < i + 2; j++) {
				baseNodes[i].regretSum[j] = rand.nextDouble();
				baseNodes[i].stratSum[j] = rand.nextDouble();
			}
		}
	}

	private void resetLoadedNodes() {
		log.info("Reset loaded nodes");
		for (int i = 0; i < NB_PLN; i++) {
			for (int j = 0; j < loadedNodes[i].regretSum.length; j++) {
				loadedNodes[i].regretSum[j] = -1;
				loadedNodes[i].stratSum[j] = -1;
			}
			loadedNodes[i].visits = -1;
			loadedNodes[i].realWeightSum = -1;
		}
	}

	private static void checkEquals(DefaultPlayerNode n1, DefaultPlayerNode n2,
			boolean checkVisitsAndReal) {
		if (checkVisitsAndReal) {
			assertEquals("Visits are not equal", n1.visits, n2.visits);
			assertEquals("Real weight are not equal", n1.realWeightSum,
					n2.realWeightSum, 0);
		}
		for (int j = 0; j < n1.regretSum.length; j++) {
			assertArrayEquals("Strat sum are not equals", n1.stratSum,
					n2.stratSum, 0);
			assertArrayEquals("Regret sum are not equals", n1.regretSum,
					n2.regretSum, 0);
		}
	}

	/**
	 * 
	 * If the provided path points to a folder, the loader must throw an
	 * {@link IOException}
	 * 
	 * @throws IOException
	 *             the expected exception
	 * 
	 */
	@SuppressWarnings("resource")
	@Test(expected = IllegalArgumentException.class)
	public void testFileChannelLoaderFailsOnFolder() throws IOException {
		log.info("Testing that loader fails on folder");
		new FileChannelLoader<DefaultPlayerNode>(folder.newFolder().toPath(),
				new CSCFRMExecutionLoaderConfig(true));
	}

	/**
	 * If an empty file is provided, the loader must throw an
	 * {@link IOException}
	 * 
	 * @throws IOException
	 *             the expected exception
	 */
	@SuppressWarnings("resource")
	@Test(expected = IOException.class)
	public void testFileChannelLoaderFailsOnEmptyFile() throws IOException {
		log.info("Fail on empty test");
		new FileChannelLoader<DefaultPlayerNode>(folder.newFile().toPath(),
				new CSCFRMExecutionLoaderConfig(true));
	}

	/**
	 * 
	 * Attempt apply the normal process of saving/loading, then verify it fails
	 * with wrong configuration
	 * 
	 * @throws IOException
	 *             unexpected IOException
	 */
	@Test
	public void testFileChannelLoader() throws IOException {
		try {
			testRightLoading();
			testWrongConfigLoading();
		} catch (IOException e) {
			log.error("Global test failed with IOException", e);
		}
	}

	private void testRightLoading() throws IOException {
		log.info("Testing right loading");
		for (int i = 0; i < nbLoaders; i++) {
			assertFalse("Shouldn' be able to load !", loaders[i].canLoad());
			loaders[i].save(ArraysIterator.get(baseNodes), baseState);
			assertTrue("Should be able to load !", loaders[i].canLoad());
			loaders[i].loadPlayerNodes(ArraysIterator.get(loadedNodes));
			for (int j = 0; j < NB_PLN; j++)
				checkEquals(loadedNodes[j], baseNodes[j],
						allConfs[i].isLoadVisitsAndRealWeight());
			CSCFRMState state = loaders[i].loadState();
			assertEquals("Loaded nbIter doesn't match for loader " + i,
					baseState.getNbIter(), state.getNbIter());
			assertArrayEquals("Utility sum doesn't match for loader " + i,
					baseState.getGameUtilSum(), state.getGameUtilSum(), 0);
			resetLoadedNodes();
		}
	}

	private void testWrongConfigLoading() throws IOException {
		log.info("Testing wrong configuration loading");
		for (int i = 0; i < nbLoaders; i++) {
			for (int k = 0; k < nbLoaders; k++) {
				if (i == k)
					continue;
				try {
					wrongConfigLoad(allPaths[i], allConfs[k]);
				} catch (IOException e) {
					continue;
				}
				fail("An exception should have occured when loading with wrong configuration");
			}
		}
	}

	@SuppressWarnings("resource")
	private void wrongConfigLoad(Path path, CSCFRMExecutionLoaderConfig conf)
			throws IOException {
		log.info("Testing wrong config...");
		new FileChannelLoader<DefaultPlayerNode>(path, conf);
	}

	/**
	 * Properly close loaders
	 * 
	 * @throws Exception
	 *             unexpected exception
	 * 
	 */
	@AfterClass
	public static void end() throws Exception {
		log.info("End test");
		for (int i = 0; i < nbLoaders; i++)
			loaders[i].close();
	}
}
