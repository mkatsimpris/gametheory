package net.funkyjava.gametheory.cscfrm.core.engine;

import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;


/**
 * Custom configuration that can be passed to the {@link CSCFRMEngine}.
 * 
 * @author Pierre Mardon
 */
public class CSCFRMConfig {

	/** The 'lock player nodes' boolean. */
	private boolean lockPlayerNodes;
	/**
	 * Indicates whether the engine must update player nodes visits and
	 * realization weight or not.
	 */
	private boolean updateVisitsAndWeight;

	/** The utility manager. */
	private CSCFRMUtilityManager utilityManager;

	/** The terminal utility reader. */
	private CSCFRMTerminalUtilReader terminalUtilReader;

	/**
	 * The default Constructor.
	 */
	public CSCFRMConfig() {
	}

	/**
	 * The Constructor.
	 * 
	 * @param lockPlayerNodes
	 *            the engine's 'lock player nodes' boolean. Should be true when
	 *            multithreading and more generally when the {@link Node#lock()}
	 *            and {@link Node#unlock()} methods should be called by the
	 *            engine.
	 * @param updateVisitsAndWeight
	 *            Indicates whether the engine must update player nodes visits
	 *            and realization weight or not.
	 * @param utilityManager
	 *            the engine's utility manager
	 * @param terminalUtilReader
	 *            the engine's terminal utility reader
	 */
	public CSCFRMConfig(boolean lockPlayerNodes, boolean updateVisitsAndWeight,
			CSCFRMUtilityManager utilityManager,
			CSCFRMTerminalUtilReader terminalUtilReader) {
		this.lockPlayerNodes = lockPlayerNodes;
		this.updateVisitsAndWeight = updateVisitsAndWeight;
		this.utilityManager = utilityManager;
		this.terminalUtilReader = terminalUtilReader;
	}

	/**
	 * Checks if the engine must lock player nodes.
	 * 
	 * @return true, if the engine must lock player nodes.
	 */
	public boolean isLockPlayerNodes() {
		return lockPlayerNodes;
	}

	/**
	 * Gets the utility manager.
	 * 
	 * @return the utility manager
	 */
	public CSCFRMUtilityManager getUtilityManager() {
		return utilityManager;
	}

	/**
	 * Gets the term util reader.
	 * 
	 * @return the term util reader
	 */
	public CSCFRMTerminalUtilReader getTermUtilReader() {
		return terminalUtilReader;
	}

	/**
	 * Indicates whether the engine must update player nodes visits and
	 * realization weight or not.
	 * 
	 * @return true if the engine must update visits and realization weight
	 */
	public boolean isUpdateVisitsAndWeight() {
		return updateVisitsAndWeight;
	}

}
