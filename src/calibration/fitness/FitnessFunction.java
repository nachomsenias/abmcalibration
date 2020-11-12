package calibration.fitness;

import org.apache.commons.math3.stat.StatUtils;

import calibration.fitness.history.ScoreBean;
import util.functions.ArrayFunctions;

/**
 * Fitness functions calculate the error of model instances when comparing its 
 * sales values to a target seasonality.
 *  
 * @author imoya
 *
 */
public class FitnessFunction {

	/**
	 * The inner class IntervalBean manages the TRA-TST interval boundaries.
	 * Using this objects the same boundaries are employed during the whole 
	 * process.
	 * 
	 * @author imoya
	 *
	 */
	protected class IntervalBean {
		protected int begin;
		protected int end;
		protected int computeSteps;

		public IntervalBean(int begin, int end, int computeSteps) {
			super();
			this.begin = begin;
			this.end = end;
			this.computeSteps = computeSteps;
		}
	}
	
	/**
	 * Creates an interval bean using the number of steps, the holdout 
	 * percentage and a flag marking the training or the test set.
	 * @param steps the number of steps for the whole simulation.
	 * @param computeTraining the flag marking the training or the test set.
	 * @param holdOut the holdout percentage
	 * @return an interval bean for the given parameters.
	 */
	protected IntervalBean computeInterval(int steps, boolean computeTraining,
			double holdOut) {
		int begin,end, computeSteps;
		
		if(holdOut == NO_HOLD_OUT) {
			return new IntervalBean(0, steps, steps);
		}
		
		int holdOutSteps = (int) Math.ceil(steps * holdOut);
		
		if(computeTraining) {
			begin = 0;
			end = steps - holdOutSteps;
			computeSteps = end; 
		} else {
			end = steps;
			begin = steps - holdOutSteps;
			computeSteps = holdOutSteps;
		}
		
		return new IntervalBean(begin, end, computeSteps);
	}
	
	/**
	 * Computes score details using training set.
	 */
	public static final boolean COMPUTE_TRAINING = true;
	
	/**
	 * Computes score details using hold out set.
	 */
	public static final boolean COMPUTE_HOLDOUT = false;
	
	/**
	 * Default hold out value is 0%.
	 */
	public final static double NO_HOLD_OUT = 0.0;
	
	/**
	 * Computes the adjustment score employing given values and returns the details.
	 * 
	 * @param brandHistory target sales for each brand and step. 
	 * @param brandSimulated simulated sales for brand and step.
	 * @param computeTraining if true, compute the score over the training set, 
	 * else computes score values over the hold out set.
	 * @param holdOut the percentage value of hold out set.
	 * @return the computed score between 0.0 and 1.0 including the 
	 * details.
	 */
	public ScoreBean[] computeScoreDetails(
			final double[][] brandHistory, 
			final double[][][] brandSimulated,
			boolean computeTraining,
			double holdOut) {
		
		int steps = brandHistory[0].length;
		
		IntervalBean interval = computeInterval(steps,computeTraining,holdOut);
		
		int iterations = brandSimulated.length;
		ScoreBean[] beans = new ScoreBean[iterations];
		for (int mc =0; mc<iterations; mc++) {
			beans[mc] = computeScoreDetails(brandHistory, 
					brandSimulated[mc], interval);
		}
		return beans;
	}
	
	/**
	 * Computes the adjustment score employing given values and returns the details.
	 * 
	 * @param brandHistory target sales for each brand and step. 
	 * @param brandSimulated simulated sales for brand and step.
	 * @param interval given interval for computing score values.
	 * @return the computed score between 0.0 and 1.0 including the 
	 * details.
	 */
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
				
				brandScoreByStep[b][currentStep] = pointError(observed,simulated);
			}
		}
		
		//The brand score equals the averaged step score.
		double[] averagedBrandScore = new double [brands];
		for (int b = 0; b<brands; b++) {
			averagedBrandScore[b] = StatUtils.mean(brandScoreByStep[b]);
		}
		
		return new ScoreBean(averagedBrandScore);
	}
	
	public ScoreBean[] computeScoreDetails(
			final double[] brandHistory, 
			final double[][] brandSimulated,
			boolean computeTraining,
			double holdOut) {
		
		int iterations = brandSimulated.length;
		ScoreBean[] beans = new ScoreBean[iterations];
		for (int mc =0; mc<iterations; mc++) {
			beans[mc] = computeScoreDetails(brandHistory, 
					brandSimulated[mc]);
		}
		return beans;
	}
	
	protected ScoreBean computeScoreDetails(
			final double[] brandHistory, 
			final double[] brandSimulated) {

		int brands = brandHistory.length;
		
		assert(brandHistory.length == brandSimulated.length);
		
		double[] brandScore = new double [brands];
		
		for (int b = 0; b<brands; b++) {
			double observed = brandHistory[b];
			double simulated =  brandSimulated[b];
			
			brandScore[b] = pointError(observed,simulated);
		}
		
		return new ScoreBean(brandScore);
	}
	
	/**
	 * Computes the adjustment score employing segment values and returns the 
	 * details.
	 * 
	 * @param segmentBrandHistory target KPI for each brand, segment, 
	 * and step. 
	 * @param segmentBrandSimulated simulated KPI for brand, segment,  
	 * and step.
	 * @param computeTraining if true, compute the score over the training set, 
	 * else computes score values over the hold out set.
	 * @param holdOut the percentage value of hold out set.
	 * @return the computed segment score between 0.0 and 1.0 including 
	 * the details.
	 */
	public ScoreBean[] computeSegmentScoreDetails(
			final double[][][] segmentBrandHistory, 
			final double[][][][] segmentBrandSimulated,
			boolean computeTraining,
			double holdOut) {
		
		int steps = segmentBrandHistory[0][0].length;
		
		IntervalBean interval = computeInterval(steps,computeTraining,holdOut);
		
		int iterations = segmentBrandSimulated.length;
		ScoreBean[] beans = new ScoreBean[iterations];
		for (int mc =0; mc<iterations; mc++) {
			beans[mc] = computeSegmentScoreDetails(segmentBrandHistory, 
					segmentBrandSimulated[mc], interval);
		}
		return beans;
	}
	
	/**
	 * Computes the adjustment score employing segment values and returns the 
	 * details.
	 * 
	 * @param segmentBrandHistory target KPI for each brand, segment, 
	 * and step. 
	 * @param segmentBrandSimulated simulated KPI for brand, segment, 
	 * and step.
	 * @param interval given interval for computing score values.
	 * @return the computed segment score between 0.0 and 1.0 including 
	 * the details.
	 */
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
					
					segmentBrandScoreByStep[b][seg][currentStep] = 
							pointError(observed,simulated);
				}
			}
		}
		
		//The brand score equals the averaged step score.
		double[][] averagedBrandScore = new double [brands][segment];
		for (int b = 0; b<brands; b++) {
			for (int seg = 0; seg<segment; seg++) {
				averagedBrandScore[b][seg] = StatUtils.mean(
						segmentBrandScoreByStep[b][seg]);
			}
		}
		
		return new ScoreBean(averagedBrandScore);
	}
	
//	/**
//	 * Computes the sales score with respect to the target sales overtime and 
//	 * returns the details.
//	 * @param brandSalesHistory target sales by brand and step.
//	 * @param brandSalesSimulated simulated sales for every iteration 
//	 * by brand and step.
//	 * @param computeTraining if true, compute the score over the training set, 
//	 * else computes score values over the hold out set.
//	 * @param holdOut the percentage value of hold out set.
//	 * @return the computed sales score comparing with the overtime sales with 
//	 * details.
//	 */
//	public ScoreBean[] computeHistorySalesScoreDetails(
//			final int[][] brandSalesHistory, 
//			final int[][][] brandSalesSimulated,
//			boolean computeTraining,
//			double holdOut) {
//		
//		int steps = brandSalesHistory[0].length;
//		
//		IntervalBean interval = computeInterval(steps,computeTraining,holdOut);
//		
//		int iterations = brandSalesSimulated.length;
//		ScoreBean[] beans = new ScoreBean[iterations];
//		for (int mc =0; mc<iterations; mc++) {
//			beans[mc] = computeHistorySalesScoreDetails(brandSalesHistory, 
//					brandSalesSimulated[mc], interval);
//		}
//		return beans;
//	}
//	
//	/**
//	 * Computes the sales score with respect to the target sales overtime and 
//	 * returns the details.
//	 * @param brandSalesHistory target sales by brand and step.
//	 * @param brandSalesSimulated simulated sales for a single iteration 
//	 * by brand and step.
//	 * @param interval given interval for computing score values.
//	 * @return the computed sales score comparing with the overtime sales with 
//	 * details.
//	 */
//	private ScoreBean computeHistorySalesScoreDetails(
//			final int[][] brandSalesHistory, 
//			final int[][] brandSalesSimulated,
//			IntervalBean interval) {
//		
//		int brands = brandSalesHistory.length;
//		
//		assert(brandSalesHistory.length == brandSalesSimulated.length);
//		assert(brandSalesHistory[0].length == brandSalesSimulated[0].length);
//		
//		double[][] brandScoreByStep = new double [brands][interval.computeSteps];
//		double[] seasonalityByStep = new double [interval.computeSteps];
//		
//		for (int s = interval.begin; s<interval.end; s++) {
//			for (int b = 0; b<brands; b++) {
//				double observed = brandSalesHistory[b][s];
//				double simulated =  brandSalesSimulated[b][s];
//				
//				int currentStep = s - interval.begin;
//				
//				seasonalityByStep[currentStep]+=observed;
//				
//				brandScoreByStep[b][currentStep] = pointError(observed,simulated);
//			}
//		}
//		
//		//Normalize using the "total" sales for each step (the seasonality value).
//		for (int s = 0; s<interval.computeSteps; s++) {
//			for (int b = 0; b<brands; b++) {
//				brandScoreByStep[b][s] = relativeScore(
//						brandScoreByStep[b][s], seasonalityByStep[s]);
//			}
//		}
//		
//		//The brand score equals the averaged step score.
//		double[] averagedBrandScore = new double [brands];
//		for (int b = 0; b<brands; b++) {
//			averagedBrandScore[b] = StatUtils.mean(brandScoreByStep[b]);
//		}
//		
//		return new ScoreBean(averagedBrandScore);
//	}
	
//	/**
//	 * Computes the segment sales score with respect to the target sales 
//	 * overtime and returns the details.
//	 * @param segmentBrandSalesHistory target sales by brand, segment, and step.
//	 * @param segmentBrandSalesSimulated simulated sales by brand, segment, and 
//	 * step.
//	 * @param computeTraining if true, compute the score over the training set, 
//	 * else computes score values over the hold out set.
//	 * @param holdOut the percentage value of hold out set.
//	 * @return the computed segment sales score comparing with the overtime 
//	 * sales with details.
//	 */
//	public ScoreBean[] computeHistorySegmentSalesScoreDetails(
//			final int[][][] segmentBrandSalesHistory, 
//			final int[][][][] segmentBrandSalesSimulated,
//			boolean computeTraining,
//			double holdOut) {
//
//		int steps = segmentBrandSalesHistory[0][0].length;
//		
//		IntervalBean interval = computeInterval(steps,computeTraining,holdOut);
//		
//		int iterations = segmentBrandSalesSimulated.length;
//		ScoreBean[] beans = new ScoreBean[iterations];
//		for (int mc =0; mc<iterations; mc++) {
//			beans[mc] = computeHistorySegmentSalesScoreDetails(
//					segmentBrandSalesHistory, segmentBrandSalesSimulated[mc],
//						interval);
//		}
//		return beans;
//	}
	
//	/**
//	 * Computes the segment sales score with respect to the target sales 
//	 * overtime and returns the details.
//	 * @param segmentBrandSalesHistory target sales by brand, segment, and step.
//	 * @param segmentBrandSalesSimulated simulated sales for a single iteration 
//	 * supplied by brand, segment, and step.
//	 * @param interval given interval for computing score values.
//	 * @return the computed segment sales score comparing with the overtime 
//	 * sales with details.
//	 */
//	private ScoreBean computeHistorySegmentSalesScoreDetails(
//			final int[][][] segmentBrandSalesHistory, 
//			final int[][][] segmentBrandSalesSimulated,
//			IntervalBean interval) {
//		
//		int brands = segmentBrandSalesHistory.length;
//		int segment = segmentBrandSalesHistory[0].length;
//		
//		
//		assert(segmentBrandSalesHistory.length == segmentBrandSalesSimulated.length);
//		assert(segmentBrandSalesHistory[0].length == segmentBrandSalesSimulated[0].length);
//		
//		
//		double[][][] segmentBrandScoreByStep = new double [brands][segment][interval.computeSteps];
//		double[][] seasonalityByBrandAndStep = new double [brands][interval.computeSteps];
//		
//		for (int s = interval.begin; s<interval.end; s++) {
//			for (int b = 0; b<brands; b++) {
//				for (int seg = 0; seg<segment; seg++) {
//					double observed = segmentBrandSalesHistory[b][seg][s];
//					double simulated = segmentBrandSalesSimulated[b][seg][s];
//					
//					int currentStep = s - interval.begin;
//					
//					seasonalityByBrandAndStep[b][currentStep]+=observed;
//					
//					segmentBrandScoreByStep[b][seg][currentStep] = 
//							pointError(observed,simulated);
//				}
//			}
//		}
//		
//		//Normalize using the "total" sales for each step (the seasonality value).
//		
//		for (int b = 0; b<brands; b++) {
//			for (int seg = 0; seg<segment; seg++) {
//				for (int s = 0; s<interval.computeSteps; s++) {
//					segmentBrandScoreByStep[b][seg][s] = relativeScore(
//							segmentBrandScoreByStep[b][seg][s],
//								seasonalityByBrandAndStep[b][s]);
//				}
//			}
//		}
//		
//		//The brand score equals the averaged step score.
//		double[][] averagedBrandScore = new double [brands][segment];
//		for (int b = 0; b<brands; b++) {
//			for (int seg = 0; seg<segment; seg++) {
//				averagedBrandScore[b][seg] = StatUtils.mean(
//						segmentBrandScoreByStep[b][seg]);
//			}
//		}
//		
//		return new ScoreBean(averagedBrandScore);
//	}
	
	/**
	 * Computes the segment sales score with respect to the target total sales 
	 * and return the details.
	 * @param segmentBrandHistorySales target total sales by brand, segment, and step.
	 * @param segmentBrandSalesSimulated simulated total sales by mc, brand, segment and 
	 * step.
	 * @param computeTraining if true, compute the score over the training set, 
	 * else computes score values over the hold out set.
	 * @param holdOut the percentage value of hold out set.
	 * @return the computed segment sales score comparing with total sales 
	 * including the details.
	 */
	public ScoreBean[] computeTotalSegmentScoreDetails(
			final double[][][] segmentBrandHistorySales, 
			final double[][][][] segmentBrandSalesSimulated,
			boolean computeTraining,
			double holdOut) {
		
		int steps = segmentBrandHistorySales[0][0].length;
		
		IntervalBean interval = computeInterval(steps,computeTraining,holdOut);
		int iterations = segmentBrandSalesSimulated.length;
		ScoreBean[] beans = new ScoreBean[iterations];
		for (int mc =0; mc<iterations; mc++) {
			beans[mc] = computeTotalSegmentScoreDetails(
					segmentBrandHistorySales, segmentBrandSalesSimulated[mc],
						interval);
		}
		return beans;
	}
	
	/**
	 * Computes the segment sales score with respect to the target total sales 
	 * and return the details.
	 * @param segmentBrandHistorySales target total sales by brand, segment and step.
	 * @param segmentBrandSalesSimulated simulated total sales for a given 
	 * iteration, supplied by brand, segment and step.
	 * @param interval given interval for computing score values.
	 * @return the computed segment sales score comparing with total sales 
	 * including the details.
	 */
	protected ScoreBean computeTotalSegmentScoreDetails(
			final double[][][] segmentBrandHistorySales, 
			final double[][][] segmentBrandSalesSimulated,
			IntervalBean interval) {
		
		assert(segmentBrandHistorySales.length == segmentBrandSalesSimulated.length);
		
		int brands = segmentBrandHistorySales.length;
		int segment = segmentBrandHistorySales[0].length;
		
		
		double [][] segmentBrandTotalScore = new double [brands][segment];
		
		//Compute the total sales error by brand and segment.
		for (int b = 0; b<brands; b++) {
			for (int seg = 0; seg<segment; seg++) {
				int segmentBrandSimulatedTotalSales = 
						ArrayFunctions.addArraySegment(
								segmentBrandSalesSimulated[b][seg], 
									interval.begin, interval.end);
				int segmentBrandTotalSales = 
						ArrayFunctions.addArraySegment(
								segmentBrandHistorySales[b][seg], 
									interval.begin, interval.end);
				segmentBrandTotalScore[b][seg]= pointError(segmentBrandTotalSales,
						segmentBrandSimulatedTotalSales);
			}
		}
		
//		// Normalize the error using overall total sales.
//		for (int b = 0; b<brands; b++) {
//			for (int seg = 0; seg<segment; seg++) {
//				segmentBrandTotalScore[b][seg] = relativeScore(
//						segmentBrandTotalScore[b][seg],overAllTotalBrandSales[b]);
//			}
//		}
		
		return new ScoreBean(segmentBrandTotalScore);
	}
	
	/**
	 * Computes the sales score with respect to the target total sales and 
	 * return the details.
	 * @param brandHistorySales target total sales by brand and step.
	 * @param brandSalesSimulated simulated total sales by MC, brand and step.
	 * @param computeTraining if true, compute the score over the training set, 
	 * else computes score values over the hold out set.
	 * @param holdOut the percentage value of hold out set.
	 * @return the computed sales score comparing with total sales including 
	 * the details.
	 */
	public ScoreBean[] computeTotalScoreDetails(
			final double [][] brandHistorySales, 
			final double [][][] brandSalesSimulated,
			boolean computeTraining,
			double holdOut) {
		
		int steps = brandHistorySales[0].length;
		
		IntervalBean interval = computeInterval(steps,computeTraining,holdOut);
		int iterations = brandSalesSimulated.length;
		ScoreBean[] beans = new ScoreBean[iterations];
		for (int mc =0; mc<iterations; mc++) {
			beans[mc] = computeTotalScoreDetails(
					brandHistorySales, brandSalesSimulated[mc], interval);
		}
		return beans;
	}
	
	/**
	 * Computes the sales score with respect to the target total sales and 
	 * return the details.
	 * @param brandHistorySales target total sales by brand and step.
	 * @param brandSalesSimulated simulated total sales for a single iteration 
	 * by brand and step.
	 * @param interval given interval for computing score values.
	 * @return the computed sales score comparing with total sales including 
	 * the details.
	 */
	protected ScoreBean computeTotalScoreDetails(
			final double [][] brandHistorySales, 
			final double [][] brandSalesSimulated,
			IntervalBean interval) {
		
		assert(brandHistorySales.length == brandSalesSimulated.length);
		
		int brands = brandHistorySales.length;
		
		double [] brandTotalScore = new double [brands];
		
		//Compute the total sales error by brand.
		for (int b = 0; b<brands; b++) {
			int brandSimulatedTotalSales = 
					ArrayFunctions.addArraySegment(brandSalesSimulated[b],
							interval.begin,interval.end);
			int brandHistoryTotalSales = 
					ArrayFunctions.addArraySegment(brandHistorySales[b],
							interval.begin,interval.end);
			brandTotalScore[b]= pointError(brandHistoryTotalSales,brandSimulatedTotalSales);
			
//			brandTotalScore[b] = relativeScore(brandTotalScore[b],brandHistoryTotalSales);
		}
		
		
		return new ScoreBean(brandTotalScore);
	}
	
	/**
	 * Computes error for two given double point values. 
	 * @param observed target double value.
	 * @param simulated simulated double value.
	 * @return computed error for two given double values.
	 */
	protected double pointError(final double observed, final double simulated) {
		double num= Math.abs(observed - simulated);
		return (num/observed)*100;
	}
	
	
	
//	/**
//	 * Computes score for a single point using single point error fitness.
//	 * This score is relative to another value.
//	 * 
//	 * @param singlePointError computed error value for this single point.
//	 * @param observed target value.
//	 * @return computed score for single point.
//	 */
//	protected double relativeScore(
//			final double singlePointError, 
//			final double observed) {
//		
//		double score = singlePointError / observed;
//		if (score < 0) score = 0;
//		if (score > 1) score = 1;
//		return ((1 - score) * 100);
//	}
	

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
