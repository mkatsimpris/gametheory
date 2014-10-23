package net.funkyjava.gametheory.gameutil.cards.indexing.bucketing;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.NonNull;

/**
 * A look-up table containing double values
 * 
 * @author Pierre Mardon
 * 
 */
@Data
public class DoubleLUT {
	@NonNull
	private final double[] table;
	private final int[] occurences;

	private final static Set<OpenOption> options = new HashSet<OpenOption>();
	static {
		options.add(StandardOpenOption.WRITE);
		options.add(StandardOpenOption.READ);
		options.add(StandardOpenOption.CREATE);
	}

	/**
	 * Write a look-up table to a non-existing file
	 * 
	 * @param path
	 *            the path of the file to write to
	 * @param writeOccurences
	 *            specifies if occurences counts must be written
	 * @throws IOException
	 *             can be thrown if file exist, parent path doesn't exist or
	 *             rights are missing
	 */
	public void writeToFile(Path path, boolean writeOccurences)
			throws IOException {
		if (Files.exists(path))
			throw new IOException("File at path " + path + " already exists");
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		final FileChannel chan = FileChannel.open(path, options);
		final int indexSize = table.length;
		buffer.putInt(indexSize);
		buffer.putInt(writeOccurences ? 1 : 0);
		writeBuffer(chan, buffer, 8);
		for (double val : table) {
			buffer.putDouble(val);
			writeBuffer(chan, buffer, 8);
		}
		for (int occ : occurences) {
			buffer.putInt(occ);
			writeBuffer(chan, buffer, 4);
		}
		chan.close();
	}

	/**
	 * Read a {@link DoubleLUT} from a file
	 * 
	 * @param path
	 *            the target file
	 * @param getOccurences
	 *            specifies if occurences must be read
	 * @return the double LUT
	 * @throws IOException
	 *             whenever there is a problem
	 */
	public static DoubleLUT readFromFile(Path path, boolean getOccurences)
			throws IOException {
		if (!Files.exists(path))
			throw new IOException("File at path " + path.toString()
					+ " doesn't exist");
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		final FileChannel chan = FileChannel.open(path, options);
		read(chan, buffer, 8);
		final int indexSize = buffer.getInt();
		final boolean writeOccurences = buffer.getInt() > 0;
		checkArgument(!getOccurences || writeOccurences,
				"Trying to get LUT occurences count but they were not written");
		final double[] table = new double[indexSize];
		final int[] occurences = getOccurences ? new int[indexSize] : null;
		for (int i = 0; i < indexSize; i++) {
			read(chan, buffer, 8);
			table[i] = buffer.getDouble();
		}
		if (getOccurences)
			for (int i = 0; i < indexSize; i++) {
				read(chan, buffer, 4);
				occurences[i] = buffer.getInt();
			}
		chan.close();
		return new DoubleLUT(table, occurences);
	}

	private static void writeBuffer(FileChannel chan, ByteBuffer buffer,
			int nbBytes) throws IOException {
		buffer.rewind().limit(nbBytes);
		if (chan.write(buffer) != nbBytes)
			throw new IOException("Couldn't write all " + nbBytes + " bytes");
		buffer.rewind();
	}

	private static void read(FileChannel chan, ByteBuffer buffer, int nbBytes)
			throws IOException {
		buffer.rewind().limit(nbBytes);
		if (chan.read(buffer) != nbBytes)
			throw new IOException("Couldn't read all " + nbBytes + " bytes");
		buffer.rewind();
	}
}
