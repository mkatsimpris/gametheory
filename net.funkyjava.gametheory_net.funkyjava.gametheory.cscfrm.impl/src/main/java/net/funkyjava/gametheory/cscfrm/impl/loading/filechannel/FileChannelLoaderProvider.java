package net.funkyjava.gametheory.cscfrm.impl.loading.filechannel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMCtxExecutionLoaderProvider;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import lombok.extern.slf4j.Slf4j;

/**
 * The FileChannelLoaderProvider is a simple implementation of
 * {@link CSCFRMCtxExecutionLoaderProvider} whose context is interpreted as a
 * path.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player node type
 */
@Slf4j
public class FileChannelLoaderProvider<PNode extends PlayerNode> implements
		CSCFRMCtxExecutionLoaderProvider<PNode> {

	/** The base path. */
	private final Path basePath;

	/**
	 * Constructor.
	 * 
	 * @param basePath
	 *            the base path
	 */
	public FileChannelLoaderProvider(Path basePath) {
		this.basePath = checkNotNull(basePath,
				"The path for the loader provider cannot be null");
		log.debug("Creating FileChannelLoaderProvider for path {}", basePath);
		checkArgument(Files.exists(basePath), "The path %s doesn't exist !",
				basePath);
		checkArgument(Files.isDirectory(basePath),
				"The path %s isn't a directory !", basePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.loading.itf.CSCFRMCtxExecutionLoaderProvider#getLoader
	 * (java.lang.String)
	 */
	@Override
	public FileChannelLoader<PNode> getLoader(String gameId,
			CSCFRMExecutionLoaderConfig config) throws IOException {
		checkNotNull(gameId, "The game id cannot be null");
		checkArgument(!gameId.isEmpty(), "The game id cannot be empty");
		checkNotNull(config, "The loader's configuration cannot be null");
		log.debug("Getting FileChannelLoader for gameId {} in path {}", gameId,
				basePath);
		try {
			Path newPath = Paths.get(basePath.toUri().resolve(
					URLEncoder.encode(gameId + "-" + config, "UTF-8")));
			return new FileChannelLoader<PNode>(newPath, config);
		} catch (Exception e) {
			log.error("Failed to create loader for game {} in base path {}",
					gameId, basePath, e);
			throw new IllegalArgumentException(
					"Something went wrong getting loader for game " + gameId
							+ " in base path " + basePath, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.loading.itf.CSCFRMCtxExecutionLoaderProvider#
	 * getSubCtxProvider(java.lang.String)
	 */
	@Override
	public FileChannelLoaderProvider<PNode> getSubCtxProvider(String subCtxId) {
		log.debug("Getting subcontext for context id {} in path {}", subCtxId,
				basePath);
		checkNotNull(subCtxId, "The subcontext id cannot be null");
		checkArgument(!subCtxId.isEmpty(), "The subcontext id cannot be empty");
		Path newPath;
		try {
			newPath = Paths.get(basePath.toUri().resolve(
					URLEncoder.encode(subCtxId, "UTF-8")));
			if (!Files.exists(newPath))
				Files.createDirectories(newPath);
			return new FileChannelLoaderProvider<PNode>(newPath);
		} catch (IOException e) {
			log.error(
					"Failed to resolve or create a path for subcontext {} in base path {}",
					subCtxId, basePath, e);
			throw new IllegalArgumentException(
					"Something went wrong the subcontext " + subCtxId
							+ " in base path " + basePath, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.loading.itf.CSCFRMCtxExecutionLoaderProvider#clear()
	 */
	@Override
	public void clear() throws IOException {
		log.debug("Removing recursively files in path {}", basePath);
		removeRecursive(basePath, false);
	}

	/**
	 * Removes all files recursively.
	 * 
	 * @param path
	 *            the path to delete
	 * @throws IOException
	 *             the IO exception
	 */
	private static void removeRecursive(final Path path,
			final boolean deleteRoot) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				// try to delete the file anyway, even if its attributes
				// could not be read, since delete-only access is
				// theoretically possible
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				if (exc == null) {
					if ((dir != path || deleteRoot))
						Files.delete(dir);
					return FileVisitResult.CONTINUE;
				} else {
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
		});
	}
}
