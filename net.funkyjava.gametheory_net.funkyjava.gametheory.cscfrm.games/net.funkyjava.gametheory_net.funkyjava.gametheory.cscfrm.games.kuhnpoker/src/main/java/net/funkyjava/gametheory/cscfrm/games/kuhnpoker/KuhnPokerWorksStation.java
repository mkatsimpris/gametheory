package net.funkyjava.gametheory.cscfrm.games.kuhnpoker;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.Path;

import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMConfig;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMMultithreadUtilityManager;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.exe.CSCFRMCyclicStepsExecutor;
import net.funkyjava.gametheory.cscfrm.exe.CSCFRMMonothreadExecutor;
import net.funkyjava.gametheory.cscfrm.exe.CSCFRMMultiThreadExecutor;
import net.funkyjava.gametheory.cscfrm.impl.exe.DefaultWorkStation;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;

/**
 * This workstation provides executors for Kuhn poker.
 * 
 * @author Pierre Mardon
 * 
 */
public class KuhnPokerWorksStation {

	/** The private workstation. */
	private DefaultWorkStation ws;

	/** The Constant uid. */
	private static final String uid = "KuhnPokerWorksStation";

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
	public KuhnPokerWorksStation(Path workingDirectory) throws IOException {
		checkNotNull(workingDirectory, "The working directory is null");
		ws = new DefaultWorkStation(workingDirectory, 2, uid);
	}

	/**
	 * The constructor to work without loading capabilities
	 * 
	 * @throws IOException
	 *             the IO exception
	 */
	public KuhnPokerWorksStation() throws IOException {
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
	 * @param nbThreads
	 *            the nb threads
	 * @return the multithread executor
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMMultiThreadExecutor<DefaultPlayerNode, KuhnPoker<DefaultPlayerNode>> buildMultithreadExecutor(
			int nbThreads) throws IOException {
		return ws.buildMultithreadExecutor(
				new KuhnPokerBuilder<DefaultPlayerNode>(), new CSCFRMConfig(
						true, true, new CSCFRMMultithreadUtilityManager(2),
						null), loaderConfig, nbThreads);
	}

	/**
	 * Builds a multithread executor.
	 * 
	 * @return the multithread executor
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMMultiThreadExecutor<DefaultPlayerNode, KuhnPoker<DefaultPlayerNode>> buildMultithreadExecutor()
			throws IOException {
		return ws.buildMultithreadExecutor(
				new KuhnPokerBuilder<DefaultPlayerNode>(), new CSCFRMConfig(
						true, true, new CSCFRMMultithreadUtilityManager(2),
						null), loaderConfig);
	}

	/**
	 * Builds a monothread executor.
	 * 
	 * @return the monothread executor
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMMonothreadExecutor<DefaultPlayerNode, KuhnPoker<DefaultPlayerNode>> buildMonothreadExecutor()
			throws IOException {
		return ws.buildMonothreadExecutor(
				new KuhnPokerBuilder<DefaultPlayerNode>(), new CSCFRMConfig(
						false, true, null, null), loaderConfig);
	}

	/**
	 * Builds a sng executor.
	 * 
	 * @param nbBlindsStack
	 *            the nb blinds stack
	 * @param nbThreads
	 *            the nb threads
	 * @return the sng executor
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMCyclicStepsExecutor<DefaultPlayerNode, KuhnPoker<DefaultPlayerNode>> buildSngExecutor(
			int nbBlindsStack, int nbThreads) throws IOException {
		if (nbBlindsStack < 2)
			throw new IllegalArgumentException(
					"Players must have at least 2 blinds each to start a SNG");
		return ws.buildCyclicStepsExecutor(
				new SNGKuhnPokerBuilder<DefaultPlayerNode>(nbBlindsStack),
				loaderConfig);
	}

	/**
	 * Builds a sng executor.
	 * 
	 * @param nbBlindsStack
	 *            the nb blinds stack
	 * @return the sng executor
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMCyclicStepsExecutor<DefaultPlayerNode, KuhnPoker<DefaultPlayerNode>> buildSngExecutor(
			int nbBlindsStack) throws IOException {
		return buildSngExecutor(nbBlindsStack, Runtime.getRuntime()
				.availableProcessors());
	}

}