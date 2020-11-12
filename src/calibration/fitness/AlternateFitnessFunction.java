package calibration.fitness;

import org.apache.commons.math3.stat.StatUtils;

import calibration.fitness.history.ScoreBean;

public class AlternateFitnessFunction extends FitnessFunction {

	private final double threshold;
	public static final double DEFAULT_THRESHOLD = 10;
	
	public AlternateFitnessFunction(double threshold) {
		// Ensure positive value
		assert(threshold>0);
		this.threshold = threshold;
	}
	
	@Override
	protected ScoreBean computeScoreDetails(
			final double[][] brandHistory, 
			final double[][] brandSimulated,
			IntervalBean interval) {

		int brands = brandHistory.length;
		
		assert(brandHistory.length == brandSimulated.length);
		assert(brandHistory[0].length == brandSimulated[0].length);
		
		double[][] brandScoreByStep = new double [brands][interval.computeSteps];
		
		for (int s = interval.begin; s<interval.end; s++) {
			for (int b = 0; b<brands; b++) {
				double observed = brandHistory[b][s];
				double simulated =  brandSimulated[b][s];
				
				int currentStep = s - interval.begin;

				//New function retrieves 1 if the simulated value is within reach, 0 otherwise. 
				brandScoreByStep[b][currentStep] = pointError(observed,simulated);
			}
		}
		
		//Hence, we need to change how we aggregate the steps.
		double[] averagedBrandScore = new double [brands];
		for (int b = 0; b<brands; b++) {
			averagedBrandScore[b] = StatUtils.sum(brandScoreByStep[b]);
		}
		
		return new ScoreBean(averagedBrandScore);
	}
	
	@Override
	protected ScoreBean computeSegmentScoreDetails(
			final double[][][] segmentBrandHistory, 
			final double[][][] segmentBrandSimulated,
			IntervalBean interval) {
		
		int brands = segmentBrandHistory.length;
		int segment = segmentBrandHistory[0].length;
		
		assert(segmentBrandHistory.length == segmentBrandSimulated.length);
		assert(segmentBrandHistory[0].length == segmentBrandSimulated[0].length);
		
		double[][][] segmentBrandScoreByStep = 
				new double [brands][segment][interval.computeSteps];
		
		for (int s = interval.begin; s<interval.end; s++) {
			for (int b = 0; b<brands; b++) {
				for (int seg = 0; seg<segment; seg++) {
					double observed = segmentBrandHistory[b][seg][s];
					double simulated = segmentBrandSimulated[b][seg][s];
					
					int currentStep = s - interval.begin;
					
					//New function retrieves 1 if the simulated value is within reach, 0 otherwise. 
					segmentBrandScoreByStep[b][seg][currentStep] = 
							pointError(observed,simulated);
				}
			}
		}
		
		//Hence, we need to change how we aggregate the steps.
		double[][] averagedBrandScore = new double [brands][segment];
		for (int b = 0; b<brands; b++) {
			for (int seg = 0; seg<segment; seg++) {
				averagedBrandScore[b][seg] = StatUtils.sum(
						segmentBrandScoreByStep[b][seg]);
			}
		}
		
		return new ScoreBean(averagedBrandScore);
	}
	
	/**
	 * Computes error for two given double point values considering the threshold value. 
	 * If simulated point is within reach, we return 0, else 1 is returned.
	 * @param observed target double value.
	 * @param simulated simulated double value.
	 * @return computed error for two given double values.
	 */
	@Override
	protected double pointError(final double observed, final double simulated) {
		// If they are the same points, return 0.
		if(simulated == observed) return 0;
		
		// Simulated value is below observed point, but within reach.
		if((simulated < observed) && (simulated > observed-threshold)) {
			return 0;
		// Simulated value is above observed point, but within reach.
		} else if((simulated > observed) && (simulated < observed+threshold)) {
			return 0;
		// Simulated value is too far from the observed point.
		} else return 1;
	}
	
	/**
	 * Computes score for a single point based in the error fitness.
	 * 
	 * @param singlePointError computed error value for this single point.
	 * @return computed score for single point.
	 */
//	protected double pointScore(
//			final double singlePointError) {
//		if (singlePointError > 100) return 0;
//		else return 100-singlePointError;
//	}
}
