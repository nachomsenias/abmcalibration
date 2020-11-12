package calibration;

import calibration.fitness.history.ScoreBean.ScoreWrapper;
import util.statistics.MonteCarloStatistics;

/**
 * Calibration bean containing result values and fitness data beans. Parameter 
 * values are already scaled after being retrieved "raw" from calibrator.
 * 
 * @author imoya
 *
 */
public class CalibrationResult {
	
	/**
	 * Beans containing scores. 
	 */
	private ScoreWrapper scores;
	
	/**
	 * Parameters values optimized for this result. Those values already 
	 * received conversion.
	 */
	private double calibratedParameters[];
	
	/**
	 * The unconverted version of the calibrated values for this result.
	 */
	private double [] unconvertedParameters;
	
	/**
	 * Monte-Carlo bean containing simulation statistics for this result.
	 */
	private MonteCarloStatistics simulationResultMC;
	
	/**
	 * Creates a simulation result bean with given fitness bean and statistics.
	 * 
	 * @param scores fitness bean for this result.
	 * @param simulationResultMC statistics for this result.
	 */
	public CalibrationResult(
			ScoreWrapper scores, 
			MonteCarloStatistics simulationResultMC) {
		
		this(scores, simulationResultMC, null, null);
	}

	/**
	 * Creates a simulation result bean with given fitness bean, statistics and 
	 * parameter values.
	 * 
	 * @param scores fitness bean for this result.
	 * @param simulationResultMC statistics for this result.
	 * @param calibratedParameters parameter values for this result.
	 */
	public CalibrationResult(
			ScoreWrapper scores, 
			MonteCarloStatistics simulationResultMC,
			double[] calibratedParameters,
			double[] unconvertedParameters) {

		this.scores = scores;
		this.calibratedParameters = calibratedParameters;
		this.simulationResultMC = simulationResultMC;
		this.unconvertedParameters = unconvertedParameters;
	}

	/**
	 * Returns global fitness error for this result.
	 * @return fitness bean for this result.
	 */
	public double getFitness() {
		return scores.finalScore;
	}
	
	/**
	 * Returns fitness scores for this result.
	 * @return fitness bean for this result.
	 */
	public ScoreWrapper getScoreDetails() {
		return scores;
	}

	/**
	 * Returns optimized parameter values for this result. 
	 * @return optimized parameter values for this result.
	 */
	public double[] getCalibratedParameters() {
		return calibratedParameters;
	}

	/**
	 * Returns statistics for this result.
	 * @return statistics for this result.
	 */
	public MonteCarloStatistics getSimulationResultMC() {
		return simulationResultMC;
	}
	
	/**
	 * Returns the unconverted version of the calibrated values for this result.
	 * @return unconverted version of the calibrated values for this result.
	 */
	public double [] getUnconvertedParameters() {
		return unconvertedParameters;
	}
}
