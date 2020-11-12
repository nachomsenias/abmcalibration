package calibration.fitness.history;

import calibration.fitness.FitnessFunction;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics.TimePeriod;

public class AlternateHistoryManager extends HistoryManager {

	public AlternateHistoryManager(TimePeriod salesHistoryPeriod, int[][] salesAgregatedHistory,
			double totalSalesWeight, FitnessFunction function, double holdOut) {
		super(salesHistoryPeriod, salesAgregatedHistory, totalSalesWeight, function, holdOut);
	}

	@Override
	public ScoreWrapper computeTrainingScore(MonteCarloStatistics mcStats) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScoreWrapper computeHoldOutScore(MonteCarloStatistics mcStats) {
		// TODO Auto-generated method stub
		return null;
	}

}
