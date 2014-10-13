package net.funkyjava.gametheory.cscfrm.exe;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMConfig;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMEngine;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMUtilityManager;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMCtxExecutionLoaderProvider;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoader;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGame;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * Multi-threaded executor. Thread safe while no change is performed on game,
 * loader or engines during run or saving.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player node type
 * @param <GameClass>
 *            the game type
 */
@Slf4j
public class CSCFRMMultiThreadExecutor<PNode extends PlayerNode, GameClass extends CSCFRMGame<PNode>>
		implements AutoCloseable {

	/** Indicates when an exception was thrown in workers */
	private boolean threwException = false;

	/** The base game. */
	private final GameClass baseGame;

	/** The loader. */
	private final CSCFRMExecutionLoader<PNode> loader;

	/** The engines. */
	private final CSCFRMEngine[] engines;

	/** The executor service. */
	private final ExecutorService service;

	/** The sync object. */
	private final Object syncObject = new Object();

	/** The number of threads. */
	private final int nbThreads;

	/** The ended tasks count. */
	private int endedTasks;

	/**
	 * The Constructor. The configuration must provide at least a multi-thread
	 * able utility manager.
	 * 
	 * @param nbThreads
	 *            the number of threads to use for execution.
	 * @param gameBuilder
	 *            the game builder
	 * @param nodesProvider
	 *            the nodes provider
	 * @param loaderProvider
	 *            the contextual loader provider
	 * @param config
	 *            the engine's configuration
	 * @param loaderConfig
	 *            the loader's configuration
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMMultiThreadExecutor(int nbThreads,
			CSCFRMGameBuilder<PNode, GameClass> gameBuilder,
			NodesProvider<PNode> nodesProvider,
			CSCFRMCtxExecutionLoaderProvider<PNode> loaderProvider,
			CSCFRMConfig config, CSCFRMExecutionLoaderConfig loaderConfig)
			throws IOException {
		log.info("Creating {} ", getClass().getName());
		checkArgument(nbThreads > 0, "nbThreads must be > 0");
		checkNotNull(config.getUtilityManager(),
				"Config must provide an utility manager so that all threads share the same");
		checkArgument(
				config.getUtilityManager().getClass() != CSCFRMUtilityManager.class,
				"Config must provide an utility manager that has multithreading capabilities, its class cannot be %s",
				CSCFRMUtilityManager.class.getCanonicalName());
		checkNotNull(loaderConfig, "The loader's configuration is null");
		checkArgument(
				config.isUpdateVisitsAndWeight() == loaderConfig
						.isLoadVisitsAndRealWeight(),
				"Incoherent configuration on player nodes visits / realization weight,"
						+ " loader's configuration and engine's configuration don't match");
		checkNotNull(nodesProvider, "The nodes provider is null");
		this.nbThreads = nbThreads;
		service = Executors.newFixedThreadPool(nbThreads);
		engines = new CSCFRMEngine[nbThreads];
		engines[0] = new CSCFRMEngine(
				baseGame = gameBuilder.getGame(nodesProvider));
		engines[0].setConfig(config);
		loader = loaderProvider.getLoader(baseGame.getUId(), loaderConfig);
		if (loader.canLoad()) {
			loader.loadPlayerNodes(baseGame.getPlayerNodesIterator());
			engines[0].getUtilManager().setState(loader.loadState());
		}
		for (int i = 1; i < nbThreads; i++) {
			engines[i] = new CSCFRMEngine(gameBuilder.getSharingGame(
					nodesProvider, baseGame));
			engines[i].setConfig(config);
		}
		log.info("Created {} with {} threads", getClass().getName(), nbThreads);
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
		log.info("Running for {} iterations.", nbIter);
		synchronized (syncObject) {
			log.debug("Acquired internal lock");
			endedTasks = 0;
			int iter = nbIter / nbThreads;
			int remainIter = nbIter % nbThreads;
			log.debug("Executing task 0 ({} iterations)", iter + remainIter);
			service.execute(new Task(iter + remainIter, engines[0]));
			for (int i = 1; i < nbThreads; i++) {
				log.debug("Executing task {} ({} iterations)", i, iter);
				service.execute(new Task(iter, engines[i]));
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
	 * Save the game's player nodes and the engine state.
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
		loader.save(baseGame.getPlayerNodesIterator(), engines[0]
				.getUtilManager().getState());
		log.info("Saved!");
	}

	/**
	 * Gets the game.
	 * 
	 * @return the game
	 */
	public GameClass getGame() {
		return baseGame;
	}

	/**
	 * Gets the engine.
	 * 
	 * @return the engine
	 */
	public CSCFRMEngine getEngine() {
		return engines[0];
	}

	/**
	 * Gets the loader.
	 * 
	 * @return the loader
	 */
	public CSCFRMExecutionLoader<PNode> getLoader() {
		return loader;
	}

	/**
	 * Task class that will be executed by each executor thread.
	 */
	private class Task implements Runnable {

		/** The nb iter. */
		private final int nbIter;

		/** The engine. */
		private final CSCFRMEngine engine;

		/**
		 * The Constructor.
		 * 
		 * @param nbIter
		 *            the nb iter
		 * @param engine
		 *            the engine
		 */
		public Task(int nbIter, CSCFRMEngine engine) {
			this.nbIter = nbIter;
			this.engine = engine;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				engine.train(nbIter);
			} catch (Exception e) {
				log.error(
						"Task threw {}, calling emergency procedure to avoid dead locks on player nodes",
						e);
				threwException = true;
				try {
					close();
				} catch (Exception e1) {
					log.error("Thrown exception when closing service executor",
							e1);
				}
				unlockNodes(baseGame.getPlayerNodesIterator());
			}
			synchronized (syncObject) {
				endedTasks++;
				syncObject.notifyAll();
			}
		}

	}

	/**
	 * Unlock player nodes to avoid dead-locks
	 * 
	 * @param nodes
	 *            the game nodes
	 */
	private void unlockNodes(Iterator<PNode> nodes) {
		checkNotNull(nodes, "Cannot unlock nodes over a null iterator");
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