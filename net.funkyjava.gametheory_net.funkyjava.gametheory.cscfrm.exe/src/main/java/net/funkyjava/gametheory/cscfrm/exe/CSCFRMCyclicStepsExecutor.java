package net.funkyjava.gametheory.cscfrm.exe;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMConfig;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMEngine;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMMultithreadUtilityManager;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMCtxExecutionLoaderProvider;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoader;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGame;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.cyclic.CSCFRMCyclicStepsGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;

/**
 * <p>
 * Executor for {@link CSCFRMCyclicStepsGameBuilder}.
 * {@link CSCFRMCyclicStepsGameBuilder#getBuilders()} returns a list of 'step
 * games' whose terminal states can refer to any step game by mapping their
 * {@link Node#id} to the builder's list index.
 * </p>
 * <p>
 * As the steps utility are sliding, iterations are performed one by one
 * randomly choosing the step to train.
 * </p>
 * <p>
 * All steps will be loaded / saved in a subcontext of the provided loader
 * according to {@link CSCFRMCyclicStepsGameBuilder#getUId()}.
 * </p>
 * <p>
 * As threads resources need to be released, don't forget to call
 * {@link #close()} once you're done with this executor.
 * </p>
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player node type
 * @param <StepGame>
 *            the step game type
 */
@Slf4j
public class CSCFRMCyclicStepsExecutor<PNode extends PlayerNode, StepGame extends CSCFRMGame<PNode>>
		implements AutoCloseable {

	/** Indicates when an exception was thrown in workers */
	private boolean threwException = false;

	/** The number of steps. */
	private final int nbStep;

	/** The games. */
	private final List<StepGame> games = new ArrayList<>();

	/** The engines. */
	private final CSCFRMEngine[][] engines;

	/** The number of threads. */
	private final int nbThreads;

	/** The number of players. */
	private final int nbPlayers;

	/** The steps utility managers. */
	private final CSCFRMMultithreadUtilityManager[] utils;

	/** The step games loaders. */
	private final List<CSCFRMExecutionLoader<PNode>> stepGamesLoaders = new ArrayList<>();

	/** The loader provider. */
	private final CSCFRMCtxExecutionLoaderProvider<PNode> loader;

	/** The sync object. */
	private final Object syncObject = new Object();

	/** The executor service. */
	private final ExecutorService service;

	/** The randoms for each thread to choose which step to train. */
	private final Random[] rands;

	/** The ended tasks count. */
	private int endedTasks;

	/**
	 * The Constructor.
	 * 
	 * @param nbThreads
	 *            the nb threads
	 * @param builder
	 *            the builder
	 * @param nodesProvider
	 *            the nodes provider
	 * @param loaderProvider
	 *            the loader provider
	 * @param loaderConfig
	 *            the loader configuration
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMCyclicStepsExecutor(int nbThreads,
			CSCFRMCyclicStepsGameBuilder<PNode, StepGame> builder,
			NodesProvider<PNode> nodesProvider,
			CSCFRMCtxExecutionLoaderProvider<PNode> loaderProvider,
			CSCFRMExecutionLoaderConfig loaderConfig) throws IOException {
		checkArgument(nbThreads > 0, "The number of threads must be > 0");
		checkNotNull(builder, "The builder cannot be null");
		checkNotNull(nodesProvider, "The nodes provider cannot be null");
		checkNotNull(loaderProvider, "The loader provider cannot be null");
		checkNotNull(loaderConfig, "The loader's configuration cannot be null");
		log.info("Creating {} for steps game {}", getClass().getName(),
				builder.getUId());
		List<CSCFRMGameBuilder<PNode, StepGame>> stepBuilders = builder
				.getBuilders();
		nbStep = stepBuilders.size();
		engines = new CSCFRMEngine[nbThreads][nbStep];
		utils = new CSCFRMMultithreadUtilityManager[nbStep];
		this.nbThreads = nbThreads;
		this.nbPlayers = builder.getNbPlayers();
		service = Executors.newFixedThreadPool(nbThreads);
		rands = new Random[nbThreads];
		for (int i = 0; i < nbThreads; i++)
			rands[i] = new Random();
		log.debug("Setting positions permutations");
		final int[][][] posPerms = new int[nbStep][nbStep][nbPlayers];
		for (int fromStep = 0; fromStep < nbStep; fromStep++)
			for (int toStep = 0; toStep < nbStep; toStep++)
				for (int fromPos = 0; fromPos < nbPlayers; fromPos++)
					posPerms[fromStep][toStep][fromPos] = builder
							.getNextPlayerPosition(fromStep, toStep, fromPos);
		loader = loaderProvider.getSubCtxProvider(builder.getUId());
		for (int step = 0; step < nbStep; step++) {
			log.debug("Initializing step {}", step);
			utils[step] = new CSCFRMMultithreadUtilityManager(nbPlayers);
			CSCFRMConfig conf = new CSCFRMConfig(true, true, utils[step],
					new MonothreadCyclicUtilReader(nbPlayers, utils,
							posPerms[step]));
			games.add(stepBuilders.get(step).getGame(nodesProvider));
			stepGamesLoaders.add(loader.getLoader(games.get(step).getUId(),
					loaderConfig));
			if (stepGamesLoaders.get(step).canLoad()) {
				log.debug("Loading step {}", step);
				stepGamesLoaders.get(step).loadPlayerNodes(
						games.get(step).getPlayerNodesIterator());
				utils[step].setState(stepGamesLoaders.get(step).loadState());
			}
			log.debug("Creating step {}'s engines", step);
			engines[0][step] = new CSCFRMEngine(games.get(step));
			engines[0][step].setConfig(conf);
			for (int thread = 1; thread < nbThreads; thread++) {
				CSCFRMConfig conf2 = new CSCFRMConfig(true,
						loaderConfig.isLoadVisitsAndRealWeight(), utils[step],
						new MonothreadCyclicUtilReader(nbPlayers, utils,
								posPerms[step]));
				engines[thread][step] = new CSCFRMEngine(stepBuilders.get(step)
						.getSharingGame(nodesProvider, games.get(step)));
				engines[thread][step].setConfig(conf2);
			}
		}
		log.info(
				"Created {} for steps game {} with {} players, {} threads and {} steps",
				getClass().getName(), builder.getUId(), nbPlayers, nbThreads,
				nbStep);
	}

	/**
	 * The Task that threads will run.
	 */
	private class Task implements Runnable {

		/** The number of iterations to perform. */
		private final int nbIter;

		/** The steps engines. */
		private final CSCFRMEngine[] engines;

		/** The number of engines. */
		private final int nbEngines;

		/** The thread's random. */
		private final Random rand;

		/** The i :). */
		private int i;

		private int chosenStep;

		/**
		 * The Constructor.
		 * 
		 * @param nbIter
		 *            the number of iterations to perform
		 * @param engines
		 *            the thread's engines
		 * @param rand
		 *            the threads random
		 */
		public Task(int nbIter, CSCFRMEngine[] engines, Random rand) {
			this.nbIter = nbIter;
			this.engines = engines;
			nbEngines = engines.length;
			this.rand = rand;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				for (i = 0; i < nbIter; i++) {
					engines[chosenStep = rand.nextInt(nbEngines)].train();
				}
			} catch (Exception e) {
				log.error(
						"Task threw {} on step {}, calling emergency procedure to avoid dead locks on player nodes",
						e, chosenStep);
				threwException = true;
				try {
					close();
				} catch (Exception e1) {
					log.error("Thrown exception when closing service executor",
							e1);
				}
				unlockNodes(games.get(chosenStep).getPlayerNodesIterator());
			}
			synchronized (syncObject) {
				endedTasks++;
				syncObject.notifyAll();
			}
		}
	}

	/**
	 * Run training for a given iterations amount.
	 * 
	 * @param nbIter
	 *            the number of itererations to execute.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public synchronized void run(int nbIter) throws InterruptedException {
		checkArgument(nbIter > 0, "The number of iterations must be > 0");
		log.info("Running for {} iterations.", nbIter);
		synchronized (syncObject) {
			log.debug("Acquired internal lock");
			endedTasks = 0;
			int iter = nbIter / nbThreads;
			int remainIter = nbIter % nbThreads;
			log.debug("Executing task 0 ({} iter)", iter + remainIter);
			service.execute(new Task(iter + remainIter, engines[0], rands[0]));
			for (int i = 1; i < nbThreads; i++) {
				log.debug("Executing task {} ({} iterations)", i, iter);
				service.execute(new Task(iter, engines[i], rands[i]));
			}
			log.debug("Waiting for tasks to end");
			while (endedTasks != nbThreads) {
				syncObject.wait();
				log.debug("Ended tasks : {}", endedTasks);
			}
		}
		log.info("End running {} iterations.", nbIter);
	}

	/**
	 * Save the games player nodes and the engines state.
	 * 
	 * @throws IOException
	 *             the IO exception
	 */
	public synchronized void save() throws IOException {
		if (threwException) {
			log.error("Won't save because an exception was thrown");
			return;
		}
		log.info("Saving...");
		for (int step = 0; step < nbStep; step++)
			stepGamesLoaders.get(step).save(
					games.get(step).getPlayerNodesIterator(),
					utils[step].getState());
		log.info("Saved!");
	}

	/**
	 * Gets the games.
	 * 
	 * @return the games
	 */
	public List<StepGame> getGames() {
		return games;
	}

	/**
	 * Gets the engines.
	 * 
	 * @return the engines
	 */
	public CSCFRMEngine[] getEngines() {
		return engines[0];
	}

	/**
	 * Gets the loader.
	 * 
	 * @return the loader
	 */
	public CSCFRMCtxExecutionLoaderProvider<PNode> getLoader() {
		return loader;
	}

	/**
	 * Unlock player nodes to avoid dead-locks
	 * 
	 * @param nodes
	 *            the game nodes
	 */
	private void unlockNodes(Iterator<PNode> nodes) {
		checkNotNull(nodes, "Cannot unlock nodes over null iterator");
		while (nodes.hasNext())
			nodes.next().unlock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		log.info("Shutting down executor service.");
		service.shutdown();
	}
}
