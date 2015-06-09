package net.funkyjava.gametheory.cscfrm.exe;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMConfig;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMEngine;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMCtxExecutionLoaderProvider;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoader;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGame;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;

/**
 * Convenience class to execute CSCFRM algorithm on a given game, mono-threaded
 * way. Intends to be mono-threaded driven.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player nodes type
 * @param <GameClass>
 *            the game class type
 */
@Slf4j
public class CSCFRMMonothreadExecutor<PNode extends PlayerNode, GameClass extends CSCFRMGame<PNode>> {

	/** The game. */
	final GameClass game;

	/** The execution loader. */
	final CSCFRMExecutionLoader<PNode> loader;

	/** The engine. */
	final CSCFRMEngine engine;

	/**
	 * The Constructor.
	 * 
	 * @param game
	 *            the game
	 * @param loaderProvider
	 *            the loader provider
	 * @param config
	 *            the engine's configuration
	 * @param loaderConfig
	 *            the loader's configuration
	 * @throws IOException
	 *             the IO exception
	 */
	public CSCFRMMonothreadExecutor(GameClass game,
			CSCFRMCtxExecutionLoaderProvider<PNode> loaderProvider,
			CSCFRMConfig config, CSCFRMExecutionLoaderConfig loaderConfig)
			throws IOException {
		log.info("Initializing for game {}", game.getUId());
		engine = new CSCFRMEngine(this.game = game);
		if (config != null)
			engine.setConfig(config);
		if (loaderProvider == null) {
			loader = null;
			return;
		}
		if (config != null && loaderConfig != null)
			checkArgument(
					config.isUpdateVisitsAndWeight() == loaderConfig
							.isLoadVisitsAndRealWeight(),
					"Incoherent configuration on player nodes visits / realization weight,"
							+ " loader configuration and engine's configuration don't match");
		loader = loaderProvider.getLoader(game.getUId(), loaderConfig);
		if (loader.canLoad()) {
			log.debug("Loading game");
			loader.loadPlayerNodes(game.getPlayerNodesIterator());
			engine.getUtilManager().setState(loader.loadState());
		}
	}

	/**
	 * Run training for a given iterations amount.
	 * 
	 * @param nbIter
	 *            the number of itererations to execute.
	 * @throws Exception
	 *             relaying engine's exception
	 */
	public synchronized void run(int nbIter) throws Exception {
		checkArgument(nbIter > 0, "The number of iterations must be > 0");
		log.info("Running for {} iterations", nbIter);
		try {
			for (int i = 0; i < nbIter; i++)
				engine.train();
		} catch (Exception e) {
			log.error("Engine threw {}, check your game's implementation", e);
			throw e;
		}
	}

	/**
	 * Save the game's player nodes and the engine state.
	 * 
	 * @throws IOException
	 *             the IO exception
	 */
	public synchronized void save() throws IOException {
		if (loader != null) {
			log.info("Saving {}...", game.getUId());
			loader.save(game.getPlayerNodesIterator(), engine.getUtilManager()
					.getState());
			log.info("Saved !");
		}
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
	 * Gets the game.
	 * 
	 * @return the game
	 */
	public GameClass getGame() {
		return game;
	}

	/**
	 * Gets the engine.
	 * 
	 * @return the engine
	 */
	public CSCFRMEngine getEngine() {
		return engine;
	}

}
