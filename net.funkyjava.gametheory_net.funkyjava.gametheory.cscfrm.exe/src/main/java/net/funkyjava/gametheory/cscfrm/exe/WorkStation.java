package net.funkyjava.gametheory.cscfrm.exe;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMConfig;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMCtxExecutionLoaderProvider;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGame;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.cyclic.CSCFRMCyclicStepsGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;

/**
 * A workstation is a nice place to execute your CFRM. It centralizes resources
 * to build executors with proper contextual loading. Every instance is only
 * player-node type dependent.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player node type
 */
public class WorkStation<PNode extends PlayerNode> {

	/** The contextual loader. */
	private final CSCFRMCtxExecutionLoaderProvider<PNode> loader;

	/** The nodes provider. */
	private final NodesProvider<PNode> provider;

	/** The uid. */
	private final String uid;

	/**
	 * The Constructor.
	 * 
	 * @param loader
	 *            the loader
	 * @param provider
	 *            the provider
	 * @param uid
	 *            the workstation's uid
	 * @throws IOException
	 *             the IO exception
	 */
	protected WorkStation(CSCFRMCtxExecutionLoaderProvider<PNode> loader,
			NodesProvider<PNode> provider, String uid) throws IOException {
		this.uid = checkNotNull(uid, "The uid cannot be null");
		checkArgument(!uid.isEmpty(), "The uid cannot be empty");
		this.loader = checkNotNull(
				checkNotNull(loader, "The loader cannot be null")
						.getSubCtxProvider(uid),
				"The subcontext loader provider is null for uid %s", uid);
		this.provider = checkNotNull(provider,
				"The nodes provider cannot be null");
	}

	/**
	 * Builds a monothread executor.
	 * 
	 * @param <GameClass>
	 *            the game type
	 * @param builder
	 *            the builder
	 * @param monothreadConfig
	 *            the CSCFRM configuration
	 * @param loaderConfig
	 *            the loader's configuration
	 * @return the monothread executor
	 * @throws IOException
	 *             the IO exception
	 */
	public <GameClass extends CSCFRMGame<PNode>> CSCFRMMonothreadExecutor<PNode, GameClass> buildMonothreadExecutor(
			CSCFRMGameBuilder<PNode, GameClass> builder,
			CSCFRMConfig monothreadConfig,
			CSCFRMExecutionLoaderConfig loaderConfig) throws IOException {
		checkNotNull(builder, "The builder cannot be null");
		checkNotNull(monothreadConfig,
				"The cscfrm configuration cannot be null");
		checkNotNull(loaderConfig, "The loader's configuration cannot be null");
		return new CSCFRMMonothreadExecutor<PNode, GameClass>(
				builder.getGame(provider), loader, monothreadConfig,
				loaderConfig);
	}

	/**
	 * Builds a monothread executor.
	 * 
	 * @param <GameClass>
	 *            the game type
	 * @param game
	 *            the game
	 * @param monothreadConfig
	 *            the CSCFRM configuration
	 * @param loaderConfig
	 *            the loader's configuration
	 * @return the monothread executor
	 * @throws IOException
	 *             the IO exception
	 */
	public <GameClass extends CSCFRMGame<PNode>> CSCFRMMonothreadExecutor<PNode, GameClass> buildMonothreadExecutor(
			GameClass game, CSCFRMConfig monothreadConfig,
			CSCFRMExecutionLoaderConfig loaderConfig) throws IOException {
		checkNotNull(game, "The game cannot be null");
		checkNotNull(monothreadConfig,
				"The cscfrm configuration cannot be null");
		checkNotNull(loaderConfig, "The loader's configuration cannot be null");
		return new CSCFRMMonothreadExecutor<PNode, GameClass>(game, loader,
				monothreadConfig, loaderConfig);
	}

	/**
	 * Builds a multithread executor.
	 * 
	 * @param <GameClass>
	 *            the generic type
	 * @param builder
	 *            the builder
	 * @param multithreadConfig
	 *            the CSCFRM configuration, expected to provide a multithread
	 *            able {@link CSCFRMUtilityManager}
	 * @param loaderConfig
	 *            the loader's configuration
	 * @param nbThreads
	 *            the number of threads
	 * @return the multithread executor
	 * @throws IOException
	 *             the IO exception
	 */
	public <GameClass extends CSCFRMGame<PNode>> CSCFRMMultiThreadExecutor<PNode, GameClass> buildMultithreadExecutor(
			CSCFRMGameBuilder<PNode, GameClass> builder,
			CSCFRMConfig multithreadConfig,
			CSCFRMExecutionLoaderConfig loaderConfig, int nbThreads)
			throws IOException {
		checkArgument(nbThreads > 0, "The number of threads must be > 0");
		checkNotNull(builder, "The builder cannot be null");
		checkNotNull(multithreadConfig,
				"The cscfrm configuration cannot be null");
		checkNotNull(loaderConfig, "The loader's configuration cannot be null");
		return new CSCFRMMultiThreadExecutor<PNode, GameClass>(nbThreads,
				builder, provider, loader, multithreadConfig, loaderConfig);
	}

	/**
	 * Builds a multithread executor with
	 * <code>Runtime.getRuntime().availableProcessors()</code> threads.
	 * 
	 * @param <GameClass>
	 *            the game type
	 * @param builder
	 *            the builder
	 * @param multithreadConfig
	 *            the CSCFRM configuration, expected to provide a multithread
	 *            able {@link CSCFRMUtilityManager}
	 * @param loaderConfig
	 *            the loader's configuration
	 * @return the multithread executor
	 * @throws IOException
	 *             the IO exception
	 */
	public <GameClass extends CSCFRMGame<PNode>> CSCFRMMultiThreadExecutor<PNode, GameClass> buildMultithreadExecutor(
			CSCFRMGameBuilder<PNode, GameClass> builder,
			CSCFRMConfig multithreadConfig,
			CSCFRMExecutionLoaderConfig loaderConfig) throws IOException {
		return buildMultithreadExecutor(builder, multithreadConfig,
				loaderConfig, Runtime.getRuntime().availableProcessors());
	}

	/**
	 * Builds a cyclic steps executor.
	 * 
	 * @param <GameClass>
	 *            the step game type
	 * @param builder
	 *            the builder
	 * @param loaderConfig
	 *            the loader's configuration
	 * @param nbThreads
	 *            the number of threads
	 * @return the cyclic steps executor
	 * @throws IOException
	 *             the IO exception
	 */
	public <GameClass extends CSCFRMGame<PNode>> CSCFRMCyclicStepsExecutor<PNode, GameClass> buildCyclicStepsExecutor(
			CSCFRMCyclicStepsGameBuilder<PNode, GameClass> builder,
			CSCFRMExecutionLoaderConfig loaderConfig, int nbThreads)
			throws IOException {
		checkArgument(nbThreads > 0, "The number of threads must be > 0");
		checkNotNull(builder, "The builder cannot be null");
		checkNotNull(loaderConfig, "The loader's configuration cannot be null");
		return new CSCFRMCyclicStepsExecutor<PNode, GameClass>(nbThreads,
				builder, provider, loader, loaderConfig);
	}

	/**
	 * Builds a cyclic steps executor with
	 * <code>Runtime.getRuntime().availableProcessors()</code> threads.
	 * 
	 * @param <GameClass>
	 *            the step game type
	 * @param builder
	 *            the builder
	 * @param loaderConfig
	 *            the loader's configuration
	 * @return the cyclic steps executor
	 * @throws IOException
	 *             the IO exception
	 */
	public <GameClass extends CSCFRMGame<PNode>> CSCFRMCyclicStepsExecutor<PNode, GameClass> buildCyclicStepsExecutor(
			CSCFRMCyclicStepsGameBuilder<PNode, GameClass> builder,
			CSCFRMExecutionLoaderConfig loaderConfig) throws IOException {
		return new CSCFRMCyclicStepsExecutor<PNode, GameClass>(Runtime
				.getRuntime().availableProcessors(), builder, provider, loader,
				loaderConfig);
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
	 * Gets the uid.
	 * 
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

}
