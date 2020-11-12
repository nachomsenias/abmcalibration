package test.calibration;

import util.exception.view.TerminationException;

/**
 * Toy-like wrapper for testing Java-C++ library communications using JNI. 
 * 
 * @author imoya
 *
 */
public class JNIWrapperTest {
	
	/**
	 * Controller using during tests.
	 */
	private CalibrationControllerTest controller;
	
	/**
	 * Creates an instance of JNIWrapperTest using given controller.
	 * 
	 * @param controller calibration controller handling JNI testing.
	 */
	public JNIWrapperTest(CalibrationControllerTest controller) {
		this.controller = controller;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Load libraries
	///////////////////////////////////////////////////////////////////////////
	
	static {
		// ABM-Calibration.dll (Windows) or libABM-Calibration.so (Unixes)
		System.loadLibrary("ABM-Calibration"); 
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Native methods
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Test JNI interaction using given configuration.
	 * 
	 * @param configFile algorithm configuration file path.
	 * @param logFolder log folder path.
	 * @param paramNames parameter names header for CSV exportation.
	 * @param minValues minimum parameter values used during calibration.
	 * @param maxValues maximum parameter values used during calibration.
	 * @return toy-like values for communication testing.
	 * @throws TerminationException if any problem rise during JNI-C++ interaction.
	 */
	public native int[] testRun(
		String configFile, String logFolder, 
		String paramNames, double[] minValues, double[] maxValues
	) throws TerminationException;
	
	///////////////////////////////////////////////////////////////////////////
	// Callback methods
	//////////////////////////////////////////////////////////////////////////

	/**
	 * Callback test using example controller.
	 * @param parameters parameter values for decoy fitness calculation.
	 * @return decoy fitness value.
	 * @throws TerminationException if any exception is thrown during fitness test.
	 */
	private double fitnessCallback(int[] parameters) throws TerminationException {
		try {
			return controller.testCallback(parameters);
		} catch (Exception e) { 
			throw new TerminationException(e); 
		}
	}
	
	/**
	 * Nothing to do! Null operation for matching snapshot exporting.
	 * 
	 * @param evaluations number of evaluations performed by optimization 
	 * algorithm.
	 * @param parameters parameter value for best individual.
	 * @throws TerminationException if any exception is thrown while generating the 
	 * snapshot. The new exception will mask the previous one.
	 */
	private void snapshotCallback(
			long evaluations, int[] parameters) throws TerminationException {
	}
}
