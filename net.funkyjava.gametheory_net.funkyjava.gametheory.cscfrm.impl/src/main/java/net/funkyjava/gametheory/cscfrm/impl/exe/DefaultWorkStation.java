package net.funkyjava.gametheory.cscfrm.impl.exe;

import java.io.IOException;
import java.nio.file.Path;

import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMCtxExecutionLoaderProvider;
import net.funkyjava.gametheory.cscfrm.exe.WorkStation;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.provider.DefaultNodesProvider;
import net.funkyjava.gametheory.cscfrm.impl.loading.filechannel.FileChannelLoaderProvider;
import net.funkyjava.gametheory.cscfrm.impl.loading.none.NoneLoaderProvider;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;

/**
 * The DefaultWorkStation is an implementation of {@link WorkStation} for
 * {@link DefaultPlayerNode}. Its {@link NodesProvider} will always be a
 * {@link DefaultNodesProvider}, and the
 * {@link CSCFRMCtxExecutionLoaderProvider} may be a
 * {@link FileChannelLoaderProvider} or a {@link NoneLoaderProvider}, depending
 * of the chosen constructor.
 * 
 * @author Pierre Mardon
 */
public final class DefaultWorkStation extends WorkStation<DefaultPlayerNode> {

	/**
	 * The main constructor.
	 * 
	 * @param loader
	 *            the contextual loader
	 * @param nbPlayers
	 *            the number of players
	 * @param uid
	 *            the uid
	 * @throws IOException
	 *             the IO exception
	 */
	protected DefaultWorkStation(
			CSCFRMCtxExecutionLoaderProvider<DefaultPlayerNode> loader,
			int nbPlayers, String uid) throws IOException {
		super(loader, new DefaultNodesProvider(), uid);
	}

	/**
	 * The constructor with no loading.
	 * 
	 * @param nbPlayers
	 *            the number of players
	 * @param uid
	 *            the uid
	 * @throws IOException
	 *             the IO exception
	 */
	public DefaultWorkStation(int nbPlayers, String uid) throws IOException {
		this(new NoneLoaderProvider<DefaultPlayerNode>(), nbPlayers, uid);
	}

	/**
	 * The constructor with {@link FileChannelLoaderProvider}.
	 * 
	 * @param workingDirectory
	 *            the working directory
	 * @param nbPlayers
	 *            the number of players
	 * @param uid
	 *            the uid
	 * @throws IOException
	 *             the IO exception
	 */
	public DefaultWorkStation(Path workingDirectory, int nbPlayers, String uid)
			throws IOException {
		this(
				new FileChannelLoaderProvider<DefaultPlayerNode>(
						workingDirectory), nbPlayers, uid);
	}

}
