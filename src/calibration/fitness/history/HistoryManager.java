package calibration.fitness.history;

import calibration.fitness.FitnessFunction;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import util.functions.MatrixFunctions;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics;
import util.statistics.Statistics.TimePeriod;


/**
 * The history manager handles how fitness and scores are computed. This is
 * performed differently if sales or multiple KPI are employed.
 * @author imoya
 *
 */
public abstract class HistoryManager {
	
	/**
	 * Period of time for every sales history step.
	 */
	protected Statistics.TimePeriod salesHistoryPeriod;

	/**
	 * Target sales history for every brand and step.
	 */
	protected double[][] salesAggregatedHistory;

	/**
	 * Total sales weight regarding history sales.
	 */
	protected double totalSalesWeight;

	/**
	 * Fitness function employed for calculating the error.
	 */
	protected FitnessFunction function;
	
	/**
	 * Percentage of steps used by the hold out evaluation.
	 */
	protected double holdOut;
	
	/**
	 * This recording bean is generated in the constructor.
	 */
	protected StatisticsRecordingBean statsBean;

	/**
	 * Creates an instance of a history manager.
	 * 
	 * @param salesHistoryPeriod the time period for the sales KPI.
	 * @param salesAgregatedHistory the sales history by brand and step.
	 * @param totalSalesWeight the percentage weight of the total sales 
	 * for the linear combination performed for calculating fitness and 
	 * scores.
	 * @param function the error function used during fitness and score
	 * computations.
	 * @param holdOut percentage of steps used by the hold out evaluation.
	 */
	public HistoryManager(
			TimePeriod salesHistoryPeriod,
			int[][] salesAgregatedHistory,
			double totalSalesWeight,
			FitnessFunction function,
			double holdOut
		) {
		super();
		this.salesHistoryPeriod = salesHistoryPeriod;
		if(salesAgregatedHistory!=null) {
			this.salesAggregatedHistory = 
				MatrixFunctions.intToDouble(salesAgregatedHistory);
		}
		this.totalSalesWeight = totalSalesWeight;
		this.function = function;
		this.holdOut=holdOut;
	}
	
	/**
	 * Computes sales score and returns its details
	 * @param mcStats the output of the model
	 * @param agentsRatio the ratio of the models population
	 * @return the computed model sales score details.
	 */
	public abstract ScoreWrapper computeTrainingScore(MonteCarloStatistics mcStats);
	
	/**
	 * Computes sales score and returns its details
	 * @param mcStats the output of the model
	 * @param agentsRatio the ratio of the models population
	 * @return the computed model sales score details.
	 */
	public abstract ScoreWrapper computeHoldOutScore(MonteCarloStatistics mcStats);
	
	/**
	 * Creates a recording bean based on the KPI used for calibration.
	 * @return a new recording bean based on the KPI used for calibration. 
	 */
	public StatisticsRecordingBean getStatsBean() {
		return statsBean;
	}
	
	/*
	 * Getters
	 */

	public Statistics.TimePeriod getSalesHistoryPeriod() {
		return salesHistoryPeriod;
	}

	public double [][] getSalesAggregatedHistory() {
		return salesAggregatedHistory;
	}

	public double getTotalSalesWeight() {
		return totalSalesWeight;
	}

	public FitnessFunction getErrorFunction() {
		return function;
	}
	
	public void setErrorFunction(FitnessFunction newFunction) {
		this.function = newFunction;
	}
}
