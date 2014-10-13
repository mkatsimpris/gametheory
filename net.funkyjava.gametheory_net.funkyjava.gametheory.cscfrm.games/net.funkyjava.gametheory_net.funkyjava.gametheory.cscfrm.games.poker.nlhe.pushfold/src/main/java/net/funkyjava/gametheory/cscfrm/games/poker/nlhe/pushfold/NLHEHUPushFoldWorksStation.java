package net.funkyjava.gametheory.cscfrm.games.poker.nlhe.pushfold;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.Path;

import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMConfig;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMMultithreadUtilityManager;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.exe.CSCFRMCyclicStepsExecutor;
import net.funkyjava.gametheory.cscfrm.exe.CSCFRMMultiThreadExecutor;
import net.funkyjava.gametheory.cscfrm.impl.exe.DefaultWorkStation;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluatorProvider;

/**
 * This workstation provides executors for {@link NLHEHUPushFold}
 * 
 * @author Pierre Mardon
 * 
 */
public class NLHEHUPushFoldWorksStation {

	/** The private workstation. */
	private DefaultWorkStation ws;

	/** The Constant uid. */
	private static final String uid = "NLHEHUPushFoldStation";

	/** Loader's configuration */
	private static final CSCFRMExecutionLoaderConfig loaderConfig = new CSCFRMExecutionLoaderConfig(
			true);

	/**
	 * The constructor to work with filechannel loading
	 * 
	 * @param workingDirectory
	 *            the working directory
	 * @throws IOException
	 *             the IO exception
	 */
	public NLHEHUPushFoldWorksStation(Path workingDirectory) throws IOException {
		checkNotNull(workingDirectory, "The working directory is null");
		ws = new DefaultWorkStation(workingDirectory, 2, uid);
	}

	/**
	 * The constructor to work without loading capabilities
	 * 
	 * @throws IOException
	 *             the IO exception
	 */
	public NLHEHUPushFoldWorksStation() throws IOException {
		ws = new DefaultWorkStation(2, uid);
	}

	/**
	 * Gets the uid.
	 * 
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * Builds a multithread executor.
	 * 
	 * @param sb
	 *            the small blind
	 * @param bb
	 *            the big blind
	 * @param stackSb
	 *            the small blind player stack
	 * @param stackBb
	 *            the big blind player stack
	 * 
	 * @param nbThreads
	 *            the nb threads
	 * @return the multithread executor
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMMultiThreadExecutor<DefaultPlayerNode, NLHEHUPushFold<DefaultPlayerNode>> buildMultithreadExecutor(
			int sb, int bb, int stackSb, int stackBb, int nbThreads)
			throws IOException {
		return ws.buildMultithreadExecutor(
				new NLHEHUPushFoldBuilder<DefaultPlayerNode>(
						new TwoPlusTwoEvaluatorProvider(), sb, bb, stackSb,
						stackBb), new CSCFRMConfig(true, true,
						new CSCFRMMultithreadUtilityManager(2), null),
				loaderConfig, nbThreads);
	}

	/**
	 * Builds a multithread executor.
	 * 
	 * @param sb
	 *            the small blind
	 * @param bb
	 *            the big blind
	 * @param stackSb
	 *            the small blind player stack
	 * @param stackBb
	 *            the big blind player stack
	 * 
	 * @return the multithread executor
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMMultiThreadExecutor<DefaultPlayerNode, NLHEHUPushFold<DefaultPlayerNode>> buildMultithreadExecutor(
			int sb, int bb, int stackSb, int stackBb) throws IOException {
		return ws.buildMultithreadExecutor(
				new NLHEHUPushFoldBuilder<DefaultPlayerNode>(
						new TwoPlusTwoEvaluatorProvider(), sb, bb, stackSb,
						stackBb), new CSCFRMConfig(true, true,
						new CSCFRMMultithreadUtilityManager(2), null),
				loaderConfig);
	}

	/**
	 * Builds a sng executor.
	 * 
	 * @param sb
	 *            the small blind
	 * @param bb
	 *            the big blind
	 * @param startingStack
	 *            the starting stack for each player
	 * @param granularity
	 *            the terminal nodes ids are indexed by (next game's small blind
	 *            stack / granularity) - 1. Sb, bb, and stacks must be multiple
	 *            of the granularity
	 * 
	 * @param nbThreads
	 *            the nb threads
	 * @return the sng executor
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMCyclicStepsExecutor<DefaultPlayerNode, NLHEHUPushFold<DefaultPlayerNode>> buildSngExecutor(
			int sb, int bb, int startingStack, int granularity, int nbThreads)
			throws IOException {
		return ws.buildCyclicStepsExecutor(
				new SNGNLHEHUPushFoldBuilder<DefaultPlayerNode>(
						new TwoPlusTwoEvaluatorProvider(), sb, bb,
						startingStack, granularity), loaderConfig);
	}

	/**
	 * Builds a sng executor.
	 * 
	 * @param sb
	 *            the small blind
	 * @param bb
	 *            the big blind
	 * @param startingStack
	 *            the starting stack for each player
	 * @param granularity
	 *            the terminal nodes ids are indexed by (next game's small blind
	 *            stack / granularity) - 1. Sb, bb, and stacks must be multiple
	 *            of the granularity
	 * 
	 * @return the sng executor
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMCyclicStepsExecutor<DefaultPlayerNode, NLHEHUPushFold<DefaultPlayerNode>> buildSngExecutor(
			int sb, int bb, int startingStack, int granularity)
			throws IOException {
		return buildSngExecutor(sb, bb, startingStack, granularity, Runtime
				.getRuntime().availableProcessors());
	}

}