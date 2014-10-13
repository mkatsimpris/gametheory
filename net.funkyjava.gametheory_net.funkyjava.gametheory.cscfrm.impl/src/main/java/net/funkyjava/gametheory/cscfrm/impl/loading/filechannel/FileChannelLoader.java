package net.funkyjava.gametheory.cscfrm.impl.loading.filechannel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMState;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoader;
import net.funkyjava.gametheory.cscfrm.core.loading.CSCFRMExecutionLoaderConfig;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import lombok.extern.slf4j.Slf4j;

/**
 * The FileChannelLoader class is a simple implementation of
 * {@link CSCFRMExecutionLoader} that writes data to a specific file.
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player node type
 */
@Slf4j
public class FileChannelLoader<PNode extends PlayerNode> implements
		CSCFRMExecutionLoader<PNode>, AutoCloseable {

	/** The file channel. */
	private FileChannel chan;

	/** The path. */
	private final Path path;

	/**
	 * Indicates whether the engine must update player nodes visits and
	 * realization weight or not.
	 */
	private boolean updateVisitsAndWeight;

	/** The Constant options. */
	private final static Set<OpenOption> options = new HashSet<OpenOption>();

	/** The state bytes offset. Depends on {@link #nbPlayers} */
	private long stateOffset;

	/**
	 * The header offset
	 */
	private static long headerOffset = 5;

	/** The file exists ?. */
	private boolean fileExists;

	/** The number of players. */
	private int nbPlayers;

	/** The buffer. */
	private ByteBuffer buffer = ByteBuffer.allocateDirect(16);;

	static {
		options.add(StandardOpenOption.WRITE);
		options.add(StandardOpenOption.READ);
		options.add(StandardOpenOption.CREATE);
	}

	/**
	 * Constructor.
	 * 
	 * @param path
	 *            the path of the file.
	 * @param config
	 *            the loader's configuration
	 * @throws IOException
	 *             the IO exception
	 */
	public FileChannelLoader(Path path, CSCFRMExecutionLoaderConfig config)
			throws IOException {
		checkNotNull(path, "The path cannot be null.");
		checkNotNull(config, "The loader's configuration cannot be null");
		log.info("New file channel loader for path {}, with config {}", path,
				config);
		this.path = path;
		fileExists = Files.exists(path);
		this.updateVisitsAndWeight = config.isLoadVisitsAndRealWeight();
		if (fileExists) {
			checkArgument(!Files.isDirectory(path),
					"The path %s is a directory !", path);
			log.debug("File {} exists, opening FileChannel", path);
			try {
				chan = FileChannel.open(path, options);
				setOffsets();
			} catch (IOException e) {
				if (chan != null)
					chan.close();
				throw e;
			}
		} else
			log.debug(
					"File {} doesn't exist, waiting for saving before opening FileChannel",
					path);
	}

	/**
	 * Sets the offsets : before offset are written
	 * <ul>
	 * <li>A byte for the configuration</li>
	 * <li>An int for the number of players</li>
	 * <li>Then the players double utilities</li>
	 * <li>The number of iterations</li>
	 * </ul>
	 * 
	 * @throws IOException
	 *             the IO exception
	 */
	private void setOffsets() throws IOException {
		// Read nb of players
		log.debug("Setting offset for file {}", path);
		chan.position(0);
		boolean updateVisitsAndWeight = readByte() != 0;
		if (updateVisitsAndWeight != this.updateVisitsAndWeight) {
			log.error(
					"Error reading the first bytes of the file {}. The read configuration differs from the one provided."
							+ " It may be have been created another way and isn't valid for this use",
					path);
			throw new IOException("Wrong configuration read");
		}
		stateOffset = 1 + 4 + ((nbPlayers = readInt()) + 1) * 8;
		log.debug("Read header, updateVisitsAndWeight = {}, nbPlayers = {}",
				updateVisitsAndWeight, nbPlayers);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.loading.itf.CSCFRMExecutionLoader#loadPlayerNodes
	 * (java.util.Enumeration)
	 */
	@Override
	public synchronized void loadPlayerNodes(Iterator<PNode> nodes)
			throws IOException {
		checkNotNull(nodes, "Nodes iterator is null");
		log.debug("Loading player nodes from file {}", path);
		if (!fileExists) {
			log.error("Trying to load nodes from the not existing file {}",
					path);
			throw new IllegalStateException("File is empty for now");
		}
		chan.position(stateOffset);
		PlayerNode node;
		int nbNodes = 0;
		while (nodes.hasNext()) {
			node = nodes.next();
			read(node.regretSum);
			read(node.stratSum);
			if (updateVisitsAndWeight) {
				node.realWeightSum = readDouble();
				node.visits = readLong();
			}
			nbNodes++;
		}
		log.debug("Loaded {} player nodes", nbNodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.loading.itf.CSCFRMExecutionLoader#loadState()
	 */
	@Override
	public synchronized CSCFRMState loadState() throws IOException {
		log.debug("Loading state from file {}", path);
		if (!fileExists) {
			log.error(
					"Trying to load CSCFRMState from the not existing file {}",
					path);
			throw new IllegalStateException("File is empty for now");
		}
		chan.position(headerOffset);
		final long nbIter = readLong();
		final double[] util = new double[nbPlayers];
		read(util);
		log.debug("Read nb iter {}, util {}", nbIter, util);
		return new CSCFRMState(nbIter, util);
	}

	/**
	 * Write the header (config and nb players) and the state of the execution.
	 * 
	 * @param state
	 *            the state
	 * @throws IOException
	 *             the IO exception
	 */
	private void writeHeaderAndState(CSCFRMState state) throws IOException {
		log.debug("Writing CSCFRMState in {}, nb iter {}, util {}", path,
				state.getNbIter(), state.getGameUtilSum());
		final double[] util = state.getGameUtilSum();
		chan.position(0);
		write(updateVisitsAndWeight ? (byte) 1 : (byte) 0);
		write(nbPlayers = util.length);
		write(state.getNbIter());
		write(util);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.loading.itf.CSCFRMExecutionLoader#save(java.util
	 * .Enumeration, net.funkyjava.cscfrm.engine.CSCFRMState)
	 */
	@Override
	public synchronized void save(Iterator<PNode> nodes, CSCFRMState state)
			throws IOException {
		checkNotNull(nodes, "Nodes iterator is null");
		checkNotNull(state, "CSCFRM state is null");
		log.debug("Writing nodes and CSCFRMState in {}", path);
		if (!fileExists) {
			log.debug("File {} doesn't exist, opening FileChannel", path);
			chan = FileChannel.open(path, options);
		}
		checkState(chan.isOpen(), "Channel is closed");
		writeHeaderAndState(state);
		setOffsets();
		chan.position(stateOffset);
		PlayerNode node;
		int nbNodes = 0;
		while (nodes.hasNext()) {
			node = nodes.next();
			write(node.regretSum);
			write(node.stratSum);
			if (updateVisitsAndWeight) {
				write(node.realWeightSum);
				write(node.visits);
			}
			nbNodes++;
		}
		log.debug("Wrote {} nodes in {}", nbNodes, path);
		fileExists = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public synchronized void close() throws Exception {
		log.debug("Closing channel for path {}", path);
		chan.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.loading.itf.CSCFRMExecutionLoader#canLoad()
	 */
	@Override
	public synchronized boolean canLoad() {
		return fileExists;
	}

	private void read(int nbBytes) throws IOException {
		buffer.rewind().limit(nbBytes);
		if (chan.read(buffer) != nbBytes)
			throw new IOException("Couldn't read all " + nbBytes + " bytes");
		buffer.rewind();
	}

	private void writeBuffer(int nbBytes) throws IOException {
		buffer.rewind().limit(nbBytes);
		if (chan.write(buffer) != nbBytes)
			throw new IOException("Couldn't write all " + nbBytes + " bytes");
		buffer.rewind();
	}

	private byte readByte() throws IOException {
		read(1);
		return buffer.get();
	}

	private int readInt() throws IOException {
		read(4);
		return buffer.getInt();
	}

	private long readLong() throws IOException {
		read(8);
		return buffer.getLong();
	}

	private double readDouble() throws IOException {
		read(8);
		return buffer.getDouble();
	}

	private void read(double[] dest) throws IOException {
		for (int i = 0; i < dest.length; i++)
			dest[i] = readDouble();
	}

	private void write(byte value) throws IOException {
		buffer.rewind();
		buffer.limit(1);
		buffer.put(value);
		writeBuffer(1);
	}

	private void write(int value) throws IOException {
		buffer.rewind();
		buffer.limit(4);
		buffer.putInt(value);
		writeBuffer(4);
	}

	private void write(long value) throws IOException {
		buffer.rewind();
		buffer.limit(8);
		buffer.putLong(value);
		writeBuffer(8);
	}

	private void write(double value) throws IOException {
		buffer.rewind();
		buffer.limit(8);
		buffer.putDouble(value);
		writeBuffer(8);
	}

	private void write(double[] values) throws IOException {
		for (int i = 0; i < values.length; i++)
			write(values[i]);
	}
}
