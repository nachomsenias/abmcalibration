package calibration.fitness.history;

import calibration.fitness.AlternateFitnessFunction;
import calibration.fitness.FitnessFunction;
import util.statistics.Statistics.TimePeriod;

/**
 * Handles the history manager creation depending on the history KPI provided.
 * @author imoya
 *
 */
public class HistoryManagerFactory {

	/**
	 * Generates an appropriate history manager depending on the history KPI 
	 * supplied.
	 * @param salesHistoryPeriod time period for the sales history.
	 * @param awarenessHistoryPeriod time period for the awareness history.
	 * @param perceptionsHistoryPeriod time period for the perceptions history.
	 * @param salesHistory sales history by brand and step.
	 * @param salesHistoryBySegment sales history by segment, brand, and step.
	 * @param awarenessHistory awareness history by brand and step.
	 * @param awarenessHistoryBySegment history for every segment, brand and 
	 * step.
	 * @param perceptionsHistory perceptions history by brand and step.
	 * @param perceptionsHistoryBySegment perceptions history by brand, segment 
	 * and step.
	 * @param totalSalesWeight weight for total sales in the fitness computation 
	 * for the sales KPI.
	 * @param salesWeight weight for the sales KPI in the fitness computation.
	 * @param awarenessWeight weight for the awareness KPI in the fitness 
	 * computation.
	 * @param perceptionsWeight weight for the perceptions KPI in the fitness 
	 * computation.
	 * @param brandWeights weight for every brand during the fitness and score 
	 * computation.
	 * @param fitnessFunction the desired fitness function for guiding the 
	 * calculations.
	 * @param holdOut percentage of steps used by the hold out evaluation.
	 * @return a history manager instance that matches the given KPIs.
	 */
	public final static HistoryManager getNewManager(
			TimePeriod salesHistoryPeriod, 
			TimePeriod awarenessHistoryPeriod,
			TimePeriod perceptionsHistoryPeriod,
			TimePeriod womVolumeHistoryPeriod, 
			int[][] salesHistory,
			int[][][] salesHistoryBySegment, 
			double[][] awarenessHistory, 
			double[][][] awarenessHistoryBySegment, 
			double[][] perceptionsHistory,
			double[][][] perceptionsHistoryBySegment,
			double[][][][] perceptionsHistoryByAttribute,
			double[][] womVolumeHistory,
			double totalSalesWeight, 
			double salesWeight, 
			double awarenessWeight, 
			double perceptionsWeight,
			double womVolumeWeight,
			String fitnessFunction,
			double holdOut
			) {

		boolean usingAwareness = awarenessHistoryBySegment != null
				|| awarenessHistory != null;
		
		boolean usingPerceptions = perceptionsHistory != null
				|| perceptionsHistoryBySegment != null;
		
		FitnessFunction function = new FitnessFunction();
		
		if(usingAwareness || usingPerceptions || salesHistoryBySegment!=null) {
			return new MultipleKPIHistoryManager(
					salesHistoryPeriod, 
					awarenessHistoryPeriod, 
					perceptionsHistoryPeriod, 
					womVolumeHistoryPeriod,
					salesHistory,
					salesHistoryBySegment,
					awarenessHistory, 
					awarenessHistoryBySegment, 
					perceptionsHistory, 
					perceptionsHistoryBySegment, 
					perceptionsHistoryByAttribute,
					womVolumeHistory,
					totalSalesWeight, 
					salesWeight, 
					awarenessWeight, 
					perceptionsWeight,
					womVolumeWeight,
					function,
					holdOut);
		} else {
			return new SalesHistoryManager(
					salesHistoryPeriod, 
					salesHistory, 
					totalSalesWeight, 
					function,
					holdOut);
		}
	}
	
	public final static HistoryManager getAlternateManager(
			TimePeriod salesHistoryPeriod, 
			TimePeriod awarenessHistoryPeriod,
			TimePeriod perceptionsHistoryPeriod,
			TimePeriod womVolumeHistoryPeriod, 
			int[][] salesHistory,
			int[][][] salesHistoryBySegment, 
			double[][] awarenessHistory, 
			double[][][] awarenessHistoryBySegment, 
			double[][] perceptionsHistory,
			double[][][] perceptionsHistoryBySegment,
			double[][][][] perceptionsHistoryByAttribute,
			double[][] womVolumeHistory,
			double totalSalesWeight, 
			double salesWeight, 
			double awarenessWeight, 
			double perceptionsWeight,
			double womVolumeWeight,
			String fitnessFunction,
			double holdOut,
			double threshold
			) {

		boolean usingAwareness = awarenessHistoryBySegment != null
				|| awarenessHistory != null;
		
		boolean usingPerceptions = perceptionsHistory != null
				|| perceptionsHistoryBySegment != null;
		
		FitnessFunction function = new AlternateFitnessFunction(threshold);
		
		if(usingAwareness || usingPerceptions || salesHistoryBySegment!=null) {
			return new MultipleKPIHistoryManager(
					salesHistoryPeriod, 
					awarenessHistoryPeriod, 
					perceptionsHistoryPeriod, 
					womVolumeHistoryPeriod,
					salesHistory,
					salesHistoryBySegment,
					awarenessHistory, 
					awarenessHistoryBySegment, 
					perceptionsHistory, 
					perceptionsHistoryBySegment, 
					perceptionsHistoryByAttribute,
					womVolumeHistory,
					totalSalesWeight, 
					salesWeight, 
					awarenessWeight, 
					perceptionsWeight,
					womVolumeWeight,
					function,
					holdOut);
		} else {
			return new SalesHistoryManager(
					salesHistoryPeriod, 
					salesHistory, 
					totalSalesWeight, 
					function,
					holdOut);
		}
	}
}
