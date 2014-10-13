package net.funkyjava.gametheory.cscfrm.model.game.nodes;

import net.funkyjava.gametheory.commonmodel.game.nodes.MinPublicNode;

/**
 * The {@link MinPublicNode} implementation base on which the CSCFRM algorithm
 * implementation will run.
 * 
 * @author Pierre Mardon
 */
public abstract class Node implements MinPublicNode {

	/** The byte type. */
	public byte bType;

	/** For player nodes, the sum of all iterations strategies. */
	public double[] stratSum;

	/** For player nodes, the cumulative regret for each action. */
	public double[] regretSum;

	/** For terminal nodes, the payoffs. */
	public double[] payoffs;

	/** For player nodes, the player index. */
	public int player;

	/** For terminal nodes, the id referring to a terminal state. */
	public int id;

	/** For player nodes, the visits count. */
	public long visits = 0;

	/** For player nodes, the realization weight sum. */
	public double realWeightSum = 0;

	/**
	 * The Constructor for a chance node.
	 */
	protected Node() {
		bType = Type.CHANCE.getByteType();
		stratSum = regretSum = null;
		player = -1;
		payoffs = null;
		id = -1;
	}

	/**
	 * The Constructor for a player node.
	 * 
	 * @param player
	 *            the player index
	 * @param nbPlayerAction
	 *            the number of player possible actions
	 */
	protected Node(int player, int nbPlayerAction) {
		bType = Type.PLAYER.getByteType();
		stratSum = new double[nbPlayerAction];
		regretSum = new double[nbPlayerAction];
		this.player = player;
		payoffs = null;
		id = -1;
	}

	/**
	 * The Constructor for a simple terminal node.
	 * 
	 * @param payoffs
	 *            the payoffs
	 */
	protected Node(double[] payoffs) {
		bType = Type.TERMINAL.getByteType();
		this.payoffs = payoffs;
		stratSum = regretSum = null;
		player = -1;
		id = -1;
	}

	/**
	 * The Constructor for a custom terminal node.
	 * 
	 * @param payoffs
	 *            the payoffs
	 * @param id
	 *            the id representing this terminal state.
	 */
	protected Node(double[] payoffs, int id) {
		bType = Type.TERMINAL.getByteType();
		this.payoffs = payoffs;
		stratSum = regretSum = null;
		player = -1;
		this.id = id;
	}

	/**
	 * Lock this player node. Should be called only by the CSCFRM algorithm or
	 * in case on emergency to avoid dead locks.
	 * 
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public abstract void lock() throws InterruptedException;

	/**
	 * Unlock this player node. Should be called only by the CSCFRM algorithm or
	 * in case on emergency to avoid dead locks.
	 */
	public abstract void unlock();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.nodes.itf.MinPublicNode#getType()
	 */
	@Override
	public Type getType() {
		return Type.fromByte(bType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.nodes.itf.MinPublicNode#getId()
	 */
	@Override
	public int getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.nodes.itf.MinPublicNode#getPayoffs()
	 */
	@Override
	public double[] getPayoffs() {
		return payoffs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.game.nodes.itf.MinPublicNode#getPlayer()
	 */
	@Override
	public int getPlayer() {
		return player;
	}
}
