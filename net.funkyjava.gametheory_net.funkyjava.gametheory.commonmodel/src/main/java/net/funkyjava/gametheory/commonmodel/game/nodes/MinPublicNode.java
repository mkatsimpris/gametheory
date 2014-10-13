package net.funkyjava.gametheory.commonmodel.game.nodes;

/**
 * <p>
 * The Interface MinPublicNode provides the minimum set of node methods required
 * to know the game's state : a decision has to be made (a chance draw or a
 * player choice), or a terminal state has been reached.
 * </p>
 * <p>
 * Each node has a {@link Type} provided by {@link #getType()}.
 * <ul>
 * <li>
 * {@link Type#CHANCE} nodes don't have to provide other consistent data. They
 * just notify a chance draw has to take place now in the game.</li>
 * <li>
 * {@link Type#PLAYER} nodes represents a player choice. must provide a player
 * index via {@link #getPlayer()} . It must be >= 0 and < to the number of
 * players in the game.</li>
 * <li>
 * {@link Type#TERMINAL} nodes represent terminal states where payoffs can
 * usually be read. In an execution context where terminal states can refer to
 * custom payoffs computing, an id can be provided via {@link #getId()} method.</li>
 * </ul>
 * </p>
 * 
 * @author Pierre Mardon
 * 
 */
public interface MinPublicNode {

	/**
	 * The Type enum for {@link MinPublicNode}.
	 */
	public static enum Type {

		/** The chance. */
		CHANCE((byte) 0),
		/** The player. */
		PLAYER((byte) 1),
		/** The terminal. */
		TERMINAL((byte) 2);

		/** The byte representation of the type. */
		public final byte byteType;

		/**
		 * The Constructor.
		 * 
		 * @param byteType
		 *            byte representation of the type
		 */
		private Type(byte byteType) {
			this.byteType = byteType;
		}

		/**
		 * Get the byte representation of the type.
		 * 
		 * @return byte representation of the type
		 */
		public byte getByteType() {
			return byteType;
		}

		/**
		 * Get a type from an byte value.
		 * 
		 * @param byteType
		 *            the int type
		 * @return the type
		 */
		public static Type fromByte(byte byteType) {
			switch (byteType) {
			case 0:
				return CHANCE;
			case 1:
				return PLAYER;
			case 2:
				return TERMINAL;
			}
			return null;
		}

	}

	/**
	 * Gets the node's type.
	 * 
	 * @return the type
	 */
	Type getType();

	/**
	 * Gets the player node's player.
	 * 
	 * @return the player
	 */
	int getPlayer();

	/**
	 * Gets the terminal node's payoffs.
	 * 
	 * @return the payoffs
	 */
	double[] getPayoffs();

	/**
	 * Gets the terminal node's id. When >= 0, the id is meant to be a link to a
	 * custom final state outside the current game.
	 * 
	 * @return the id
	 */
	int getId();
}
