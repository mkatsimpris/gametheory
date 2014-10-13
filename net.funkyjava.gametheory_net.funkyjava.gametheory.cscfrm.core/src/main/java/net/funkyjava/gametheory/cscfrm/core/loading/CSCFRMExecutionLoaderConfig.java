/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.core.loading;

/**
 * @author Pierre Mardon
 * 
 */
public class CSCFRMExecutionLoaderConfig {

	/**
	 * Indicates if the loader should save and load visits and realization
	 * weight
	 */
	private boolean loadVisitsAndRealWeight;

	/**
	 * @param loadVisitsAndRealWeight
	 *            indicates if the loader should save and load visits and
	 *            realization weight
	 * 
	 */
	public CSCFRMExecutionLoaderConfig(boolean loadVisitsAndRealWeight) {
		this.loadVisitsAndRealWeight = loadVisitsAndRealWeight;
	}

	/**
	 * 
	 * @return true when the loader should save and load visits and realization
	 *         weight
	 */
	public boolean isLoadVisitsAndRealWeight() {
		return loadVisitsAndRealWeight;
	}

	@Override
	public String toString() {
		return "visits" + (loadVisitsAndRealWeight ? "1" : "0");
	}
}
