package net.funkyjava.gametheory.cscfrm.impl.loading.filechannel;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMState;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.cscfrm.util.game.helpers.ArraysIterator;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 
 * Test class for {@link FileChannelLoaderProvider}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class FileChannelLoaderProviderTest {

	/**
	 * Temporary folder
	 */
	@ClassRule
	public static TemporaryFolder folder = new TemporaryFolder();

	/**
	 * 
	 * Test that constructor fails when provided path doesn't exist
	 * 
	 * @throws Exception
	 *             an exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testFileChannelLoaderProviderInexistingPath() throws Exception {
		new FileChannelLoaderProvider<DefaultPlayerNode>(folder.getRoot()
				.toPath().resolve("some_inexisting_folder"));
	}

	/**
	 * 
	 * Test that constructor fails when provided path points to a file
	 * 
	 * @throws Exception
	 *             an exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testFileChannelLoaderProviderPathIsFile() throws Exception {
		new FileChannelLoaderProvider<DefaultPlayerNode>(folder.newFile()
				.toPath());
	}

	/**
	 * Test constructor works for a fresh folder
	 * 
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testFileChannelLoaderProvider() throws Exception {
		new FileChannelLoaderProvider<DefaultPlayerNode>(folder.newFolder()
				.toPath());
	}

	private Iterator<DefaultPlayerNode> getNodes() {
		final int NB_PLN = 5;
		final Random rand = new Random();
		DefaultPlayerNode[] baseNodes = new DefaultPlayerNode[NB_PLN];
		for (int i = 0; i < NB_PLN; i++) {
			baseNodes[i] = new DefaultPlayerNode(i, i + 2);
			baseNodes[i].visits = rand.nextLong();
			baseNodes[i].realWeightSum = rand.nextDouble();
			for (int j = 0; j < i + 2; j++) {
				baseNodes[i].regretSum[j] = rand.nextDouble();
				baseNodes[i].stratSum[j] = rand.nextDouble();
			}
		}
		return ArraysIterator.get(baseNodes);
	}

	private CSCFRMState getState() {
		return new CSCFRMState(123456789, new double[] { 1, 2, 3, 4, 5, 6, 7,
				8, 9 });
	}

	/**
	 * Test getting a loader works for an simple example with a strange game id,
	 * and we can save nodes and state
	 * 
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testGetLoader() throws Exception {

		FileChannelLoader<DefaultPlayerNode> loader = new FileChannelLoaderProvider<DefaultPlayerNode>(
				folder.newFolder().toPath()).getLoader(
				"some_ \\!ï++\\nuµ$øgame *_id",
				new CSCFRMExecutionLoaderConfig(true));
		loader.save(getNodes(), getState());
		loader.close();
	}

	/**
	 * Test we can get a subcontext
	 * 
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testGetSubCtxProvider() throws Exception {
		new FileChannelLoaderProvider<DefaultPlayerNode>(folder.newFolder()
				.toPath()).getSubCtxProvider("some_ \\!ï++\\nuµ$øsubCTX *_id");
	}

	/**
	 * Test clearing has the right behavior
	 * 
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testClear() throws Exception {
		try {
			Path path = folder.newFolder().toPath();
			FileChannelLoaderProvider<DefaultPlayerNode> prov = new FileChannelLoaderProvider<DefaultPlayerNode>(
					path);
			FileChannelLoader<DefaultPlayerNode> loader = prov.getLoader(
					"some_game", new CSCFRMExecutionLoaderConfig(true));
			loader.save(getNodes(), getState());
			loader.close();
			loader = prov.getSubCtxProvider("some_subctx").getLoader(
					"some_game", new CSCFRMExecutionLoaderConfig(true));
			loader.save(getNodes(), getState());
			loader.close();
			assertTrue("Files not created properly",
					path.toFile().listFiles().length == 2);
			assertTrue(
					"File in subcontext not created properly",
					path.resolve("some_subctx").toFile().listFiles().length == 1);
			prov.clear();
			assertTrue("Folder was not cleared properly", path.toFile()
					.listFiles().length == 0);
		} catch (Exception e) {
			log.error("There was an error clearing the loader provider.", e);
			throw e;
		}
	}
}
