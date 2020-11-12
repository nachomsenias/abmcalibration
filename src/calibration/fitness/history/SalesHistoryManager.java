package calibration.fitness.history;

import calibration.fitness.FitnessFunction;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import util.functions.Functions;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics.TimePeriod;

/**
 * The sales history manager computes the fitness and score values 
 * using only the sales KPI.
 * @author imoya
 *
 */
public class SalesHistoryManager extends HistoryManager{

	public SalesHistoryManager(TimePeriod salesHistoryPeriod, 
			int[][] salesAgregatedHistory, double totalSalesWeight,
			FitnessFunction function, double holdOut) {
		super(salesHistoryPeriod, salesAgregatedHistory, 
				totalSalesWeight, function, holdOut);
		statsBean=StatisticsRecordingBean.onlySalesBean();
	}

	@Override
	public ScoreWrapper computeTrainingScore(MonteCarloStatistics mcStats) {
		double[][][] salesByBrandStep = mcStats.computeScaledSalesByBrandByStep(
				salesHistoryPeriod);
		
		//Create the wrapper
		ScoreBean base = new ScoreBean();
		ScoreWrapper wrapper = base.new ScoreWrapper();
		
		wrapper.historySalesScore = ScoreBean.mergeBeans(
				function.computeScoreDetails( 
						salesAggregatedHistory, salesByBrandStep, 
						FitnessFunction.COMPUTE_TRAINING, holdOut));
		
		wrapper.totalSalesScore = ScoreBean.mergeBeans(
				function.computeTotalScoreDetails(
						salesAggregatedHistory, salesByBrandStep,
							FitnessFunction.COMPUTE_TRAINING, holdOut));
		
		wrapper.finalScore = Functions.linearCombination(
				wrapper.historySalesScore.getScore(), 
				wrapper.totalSalesScore.getScore(), 
				totalSalesWeight);
		
		return wrapper;
	}
	
	@Override
	public ScoreWrapper computeHoldOutScore(MonteCarloStatistics mcStats) {
		double [][][] salesByBrandStep = mcStats.computeScaledSalesByBrandByStep(
				salesHistoryPeriod);
		
		//Create the wrapper
		ScoreBean base = new ScoreBean();
		ScoreWrapper wrapper = base.new ScoreWrapper();
		
		wrapper.historySalesScore = ScoreBean.mergeBeans(
				function.computeScoreDetails( 
						salesAggregatedHistory, salesByBrandStep,
							FitnessFunction.COMPUTE_HOLDOUT, holdOut));
		
		wrapper.totalSalesScore = ScoreBean.mergeBeans(
				function.computeTotalScoreDetails(
						salesAggregatedHistory, salesByBrandStep, 
							FitnessFunction.COMPUTE_HOLDOUT, holdOut));
		
		wrapper.finalScore = Functions.linearCombination(
				wrapper.historySalesScore.getScore(), 
				wrapper.totalSalesScore.getScore(), 
				totalSalesWeight);
		
		return wrapper;
	}
}
