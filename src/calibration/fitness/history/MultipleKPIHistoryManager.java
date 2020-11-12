package calibration.fitness.history;

import org.apache.commons.math3.stat.StatUtils;

import calibration.fitness.FitnessFunction;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import util.functions.Functions;
import util.functions.MatrixFunctions;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics;
import util.statistics.Statistics.TimePeriod;

/**
 * The multiple KPI history manager computes the fitness and score 
 * values depending on the supplied KPI (sales, awareness, 
 * perceptions) in any combination of them.
 * @author imoya
 *
 */
public class MultipleKPIHistoryManager extends HistoryManager{

	/**
	 * KPI detail of provided histories.
	 */
	public enum KPIDetail {
		DISABLED, AGGREGATED, ONE_LEVEL, TWO_LEVEL 
	}

	/**
	 * Detail level for provided sales history.
	 */
	private KPIDetail salesDetail;
	
	/**
	 * Detail level for provided awareness history.
	 */
	private KPIDetail awarenessDetail;
	
	/**
	 * Detail level for provided perceptions history.
	 */
	private KPIDetail perceptionsDetail;
	
	/**
	 * Detail level for provided WOM Volume history.
	 */
	private KPIDetail womVolumeDetail;
	
	/**
	 * Period of time for every awareness history step.
	 */
	private Statistics.TimePeriod awarenessHistoryPeriod;
	
	/**
	 * Period of time for every perceptions history step.
	 */
	private Statistics.TimePeriod perceptionsHistoryPeriod;
	
	/**
	 * Period of time for every WOM Volume history step.
	 */
	// XXX WOM is stored directly by week.
	//private Statistics.TimePeriod womVolumeHistoryPeriod;
	
	/**
	 * Target sales history for every brand, segment, and step.
	 */
	private double[][][] salesHistory;
	
	/**
	 * Target total sales for every brand and segment.
	 */
	private int[][] totalSalesHistory;
	
	/**
	 * Target awareness history for every brand, segment and step.
	 */
	private double[][][] awarenessHistory;
	
	/**
	 * Target awareness history for every brand and step.
	 */
	private double[][] awarenessAggregatedHistory;
	
	/**
	 * Target total awareness for every brand.
	 */
	private double[] totalAwarenessHistory;
	
	/**
	 * Target perceptions history for every brand and step.
	 */
	private double[][] perceptionsAggregatedHistory;
	
	/**
	 * Target perceptions history by brand, segment and step.
	 */
	private double[][][] perceptionsHistoryBySegment;
	
	/**
	 * Target perceptions history by att, brand, segment and step.
	 */
	private double[][][][] perceptionsHistoryByAttribute;
	
	/**
	 * Target WOM Volume history for every brand and step.
	 */
	private double[][] womVolumeHistory;
	
	/**
	 * Sales weight when optimizing using multiple KPI.
	 */
	private double salesWeight;
	
	/**
	 * Awareness weight when optimizing using multiple KPI.
	 */
	private double awarenessWeight;
	
	/**
	 * Perceptions weight when optimizing using multiple KPI.
	 */
	private double perceptionsWeight;
	
	/**
	 * WOM Volume weight when optimizing using multiple KPI.
	 */
	private double womVolumeWeight;
	
	public MultipleKPIHistoryManager(
			TimePeriod salesHistoryPeriod, 
			TimePeriod awarenessHistoryPeriod,
			TimePeriod perceptionsHistoryPeriod,
			TimePeriod womVolumeHistoryPeriod, 
			int[][] salesAgregatedHistory, 
			int[][][] salesHistory,
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
			FitnessFunction function,
			double holdOut) {
		super(salesHistoryPeriod,
				salesAgregatedHistory,
				totalSalesWeight,
				function,
				holdOut);
		
		//KPI Time period
		this.awarenessHistoryPeriod = awarenessHistoryPeriod;
		this.perceptionsHistoryPeriod = perceptionsHistoryPeriod;
//		this.womVolumeHistoryPeriod = womVolumeHistoryPeriod;
		
		if(salesHistory!=null)
			this.salesHistory = MatrixFunctions.intToDouble(salesHistory);
		
		this.awarenessHistory = awarenessHistoryBySegment;
		this.awarenessAggregatedHistory = awarenessHistory;
		
		this.perceptionsAggregatedHistory = perceptionsHistory;
		this.perceptionsHistoryBySegment = perceptionsHistoryBySegment;
		this.perceptionsHistoryByAttribute = perceptionsHistoryByAttribute;
		
		this.womVolumeHistory = womVolumeHistory;
		
		// KPI Weights
		this.salesWeight = salesWeight;
		this.awarenessWeight = awarenessWeight;		
		this.perceptionsWeight = perceptionsWeight;
		this.womVolumeWeight = womVolumeWeight;
		
		//Checks supplied KPI
		checkKPIDetail();
	}
	
	/**
	 * Checks the level of detail based on the history structure provided.
	 */
	private void checkKPIDetail() {
		
		if(salesAggregatedHistory!=null) {
			salesDetail=KPIDetail.AGGREGATED;
		} else if(salesHistory!=null){
			salesDetail=KPIDetail.ONE_LEVEL;
		} else {
			salesDetail=KPIDetail.DISABLED;
		}
		
		if(awarenessAggregatedHistory!=null) {
			awarenessDetail = KPIDetail.AGGREGATED;
			//Setup total awareness values;
			int brands = awarenessAggregatedHistory.length;
			totalAwarenessHistory = new double [brands];
			for (int b=0; b<brands; b++) {
				totalAwarenessHistory[b] = 
						StatUtils.mean(awarenessAggregatedHistory[b]);
			}
		} else if (awarenessHistory!=null) {
			awarenessDetail = KPIDetail.ONE_LEVEL;
			
			//Setup total awareness by segment;
			int brands = awarenessHistory.length;
			int segments = awarenessHistory[0].length;
			
			//XXX BEWARE:: totalAwareness by segment is computed averaging segment, not using 
			//segment sizes!! This is caused due to not having segment sizes in this level.
			totalAwarenessHistory = new double [brands];
			for (int b=0; b<brands; b++) {
				for (int s=0; s<segments; s++) {
					totalAwarenessHistory[b]+= StatUtils.mean(awarenessHistory[b][s]) / segments;
				}
			}
			
		} else {
			awarenessDetail = KPIDetail.DISABLED;
		}
		
		if(perceptionsAggregatedHistory!=null) {
			perceptionsDetail=KPIDetail.AGGREGATED;
		} else if (perceptionsHistoryBySegment!=null) {
			perceptionsDetail=KPIDetail.ONE_LEVEL;
		} else if (perceptionsHistoryByAttribute!=null) {
			perceptionsDetail=KPIDetail.TWO_LEVEL;

		} else {
			perceptionsDetail=KPIDetail.DISABLED;
		}
		
		if(womVolumeHistory!=null) {
			womVolumeDetail=KPIDetail.AGGREGATED;
		} else {
			womVolumeDetail=KPIDetail.DISABLED;
		}
		
		statsBean = StatisticsRecordingBean.getBeanFromDetail(
				salesDetail, awarenessDetail, perceptionsDetail, womVolumeDetail);
	}

	public ScoreWrapper computeTrainingScore(MonteCarloStatistics mcStats) {
		
		//Create the wrapper
		ScoreBean base = new ScoreBean();
		ScoreWrapper wrapper = base.new ScoreWrapper();
		
		double salesScore = 0.0;
		
		if(salesDetail!=KPIDetail.DISABLED && salesWeight!=0.0) {
			ScoreWrapper salesWrapper = computeSalesScore(mcStats,  
					FitnessFunction.COMPUTE_TRAINING);
			
			wrapper.historySalesScore = salesWrapper.historySalesScore;
			wrapper.totalSalesScore = salesWrapper.totalSalesScore;
			
			salesScore = Functions.linearCombination(
					wrapper.historySalesScore.getScore(), 
					wrapper.totalSalesScore.getScore(), 
					totalSalesWeight);
		}

		double awarenessScore = 0.0;
		if(awarenessDetail!=KPIDetail.DISABLED && awarenessWeight!=0.0) {
			wrapper.awarenessScore = computeAwarenessScore(mcStats, 
					FitnessFunction.COMPUTE_TRAINING);
			awarenessScore = wrapper.awarenessScore.getScore();
			//Include total awareness
			wrapper.totalAwarenessScore = 
					computeTotalAwarenessScore(mcStats, FitnessFunction.COMPUTE_TRAINING);
		}
		
		double perceptionsScore = 0.0;
		if(perceptionsDetail!=KPIDetail.DISABLED && perceptionsWeight!=0.0) {
			wrapper.perceptionsScore = computePerceptionsScore(mcStats,
					FitnessFunction.COMPUTE_TRAINING);
			perceptionsScore = ScoreBean.getAverageScore(wrapper.perceptionsScore);
		}
		
		double womVolumeScore = 0.0;
		if(womVolumeDetail!=KPIDetail.DISABLED && womVolumeWeight!=0.0) {
			wrapper.womVolumeScore = computeWomVolumeScore(mcStats,
					FitnessFunction.COMPUTE_TRAINING);
			womVolumeScore=wrapper.womVolumeScore.getScore();
		}
		
		//Combine final value using weights
		wrapper.finalScore = salesScore * salesWeight 
						+ awarenessScore * awarenessWeight 
						+ perceptionsScore * perceptionsWeight
						+ womVolumeScore * womVolumeWeight;
		
		return wrapper;
	}
	
	public ScoreWrapper computeHoldOutScore(MonteCarloStatistics mcStats) {
		
		//Create the wrapper
		ScoreBean base = new ScoreBean();
		ScoreWrapper wrapper = base.new ScoreWrapper();
		
		double salesScore = 0.0;
		
		if(salesDetail!=KPIDetail.DISABLED) {
			ScoreWrapper salesWrapper = computeSalesScore(mcStats, 
					FitnessFunction.COMPUTE_HOLDOUT);
			
			wrapper.historySalesScore = salesWrapper.historySalesScore;
			wrapper.totalSalesScore = salesWrapper.totalSalesScore;
			
			salesScore = Functions.linearCombination(
					wrapper.historySalesScore.getScore(), 
					wrapper.totalSalesScore.getScore(), 
					totalSalesWeight);
		}

		double awarenessScore = 0.0;
		if(awarenessDetail!=KPIDetail.DISABLED) {
			wrapper.awarenessScore = computeAwarenessScore(mcStats,
					FitnessFunction.COMPUTE_HOLDOUT);
			awarenessScore = wrapper.awarenessScore.getScore();
			//Include total awareness
			wrapper.totalAwarenessScore = 
					computeTotalAwarenessScore(mcStats, FitnessFunction.COMPUTE_HOLDOUT);
		}
		
		double perceptionsScore = 0.0;
		if(perceptionsDetail!=KPIDetail.DISABLED) {
			wrapper.perceptionsScore = computePerceptionsScore(mcStats,
					FitnessFunction.COMPUTE_HOLDOUT);
			perceptionsScore = ScoreBean.getAverageScore(wrapper.perceptionsScore);
		}
		
		double womVolumeScore = 0.0;
		if(womVolumeDetail!=KPIDetail.DISABLED && womVolumeWeight!=0.0) {
			wrapper.womVolumeScore = computeWomVolumeScore(mcStats,
					FitnessFunction.COMPUTE_HOLDOUT);
		}
		
		//Combine final value using weights
		wrapper.finalScore = salesScore * salesWeight 
						+ awarenessScore * awarenessWeight 
						+ perceptionsScore * perceptionsWeight
						+ womVolumeScore * womVolumeWeight;
		
		return wrapper;
	}
	
	/**
	 * Computes the score details for the sales KPI.
	 * @param mcStats the statistics instance.
	 * @param computeTraining if true, computes the training set; if false computes 
	 * the test set.
	 * @return the score details for the sales KPI.
	 */
	private ScoreWrapper computeSalesScore(MonteCarloStatistics mcStats, 
			boolean computeTraining) {
		
		//Create the wrapper
		ScoreBean base = new ScoreBean();
		ScoreWrapper salesWrapper = base.new ScoreWrapper();
		
		switch (salesDetail) {
		case DISABLED:
			return null;
		case AGGREGATED:
			
			double[][][] salesByBrandStep = mcStats.computeScaledSalesByBrandByStep(
					salesHistoryPeriod);
			
			salesWrapper.historySalesScore = ScoreBean.mergeBeans(
					function.computeScoreDetails(
							salesAggregatedHistory, 
							salesByBrandStep, 
								computeTraining, holdOut));
			
			salesWrapper.totalSalesScore = ScoreBean.mergeBeans(
					function.computeTotalScoreDetails(
							salesAggregatedHistory, salesByBrandStep,
								computeTraining, holdOut));
			
			return salesWrapper;

		case ONE_LEVEL:
			
			double[][][][] salesByBrandSegmentStep = 
				mcStats.computeScaledSalesByBrandBySegmentByStep(salesHistoryPeriod);
			
			salesWrapper.historySalesScore = ScoreBean.mergeBeans(
					function.computeSegmentScoreDetails(
							salesHistory, salesByBrandSegmentStep, 
								computeTraining, holdOut));
			
			salesWrapper.totalSalesScore = ScoreBean.mergeBeans(
					function.computeTotalSegmentScoreDetails(
							salesHistory, salesByBrandSegmentStep, 
								computeTraining, holdOut));
			
			return salesWrapper;

		default:
			return null;
		}
	}
	
	/**
	 * Computes the score details for the awareness KPI. Notice that the awareness 
	 * values are scaled to a 0-100 scale.
	 * @param mcStats the model output.
	 * @param computeTraining if true, compute the score over the training set, 
	 * else computes score values over the hold out set.
	 * @return the computed score details for the awareness KPI.
	 */
	private ScoreBean computeAwarenessScore(
			MonteCarloStatistics mcStats,
			boolean computeTraining) {
		
		switch (awarenessDetail) {
		case DISABLED:
			return null;
		case AGGREGATED:
			
			double[][][] awarenessByBrandStep = 
					MatrixFunctions.scaleCopyOfDouble3dMatrix(
							mcStats.getAwarenessByBrandByStep(
									awarenessHistoryPeriod),
							100.0);
			
			return ScoreBean.mergeBeans(function.computeScoreDetails(
					awarenessAggregatedHistory, awarenessByBrandStep, 
						computeTraining, holdOut));

		case ONE_LEVEL:
			
			double[][][][] awarenessByBrandSegmentStep = 
					MatrixFunctions.scaleCopyOfDouble4dMatrix(
							mcStats.getAwarenessByBrandBySegmentByStep(
									awarenessHistoryPeriod),
							100.0);
			
			return ScoreBean.mergeBeans(function.computeSegmentScoreDetails(
					awarenessHistory, awarenessByBrandSegmentStep, 
						computeTraining, holdOut));

		default:
			return null;
		}
	}
	
	/**
	 * Computes the score details for the awareness KPI. Notice that the awareness 
	 * values are scaled to a 0-100 scale.
	 * @param mcStats the model output.
	 * @param computeTraining if true, compute the score over the training set, 
	 * else computes score values over the hold out set.
	 * @return the computed score details for the awareness KPI.
	 */
	private ScoreBean computeTotalAwarenessScore(
			MonteCarloStatistics mcStats,
			boolean computeTraining) {
		double[][] awarenessByBrandStep = 
				MatrixFunctions.scaleCopyOfDoubleMatrix(
						mcStats.getTotalAwarenessByBrand(),
						100.0);
		
		return ScoreBean.mergeBeans(function.computeScoreDetails(
				totalAwarenessHistory, awarenessByBrandStep, 
					computeTraining, holdOut));
	}
	
	/**
	 * XXX
	 * @param mcStats
	 * @param computeTraining
	 * @return
	 */
	private ScoreBean computeWomVolumeScore(
			MonteCarloStatistics mcStats,
			boolean computeTraining) {
		double[][][] womVolume = 
						mcStats.getVolumeByBrandByStep();
		
		return ScoreBean.mergeBeans(function.computeScoreDetails(
				womVolumeHistory, womVolume, 
					computeTraining, holdOut));
	}
	
	/**
	 * Computes the full score details for the perceptions KPI. Notice that the 
	 * perception values are scaled to a 0-100 scale.
	 * @param mcStats the model output.
	 * @param computeTraining if true, compute the score over the training set, 
	 * else computes score values over the hold out set.
	 * @return the computed full score details for the perceptions KPI.
	 */
	private ScoreBean[] computePerceptionsScore(
			MonteCarloStatistics mcStats,
			boolean computeTraining) {
		
		ScoreBean[] attributeLessBean = new ScoreBean[1];
		switch (perceptionsDetail) {
		case DISABLED:
			return null;
		case AGGREGATED:
			
			double[][][] perceptionsByBrandStep = 
					MatrixFunctions.scaleCopyOfDouble3dMatrix(
						mcStats.computePerceptionsByBrandByStepMC(
								perceptionsHistoryPeriod), 
						10.0);
			
			attributeLessBean[0] = ScoreBean.mergeBeans(function.computeScoreDetails(
						perceptionsAggregatedHistory, perceptionsByBrandStep,
							computeTraining, holdOut));
			return attributeLessBean;

		case ONE_LEVEL:
			double[][][][] perceptionsByBrandSegmentStep = 
					MatrixFunctions.scaleCopyOfDouble4dMatrix(
							mcStats.computePerceptionsByBrandBySegmentByStep(
									perceptionsHistoryPeriod),
							10.0);
			
			attributeLessBean[0] = ScoreBean.mergeBeans(function.computeSegmentScoreDetails(
					perceptionsHistoryBySegment, perceptionsByBrandSegmentStep,
						computeTraining, holdOut));
			return attributeLessBean;
		
		case TWO_LEVEL:
			double[][][][][] perceptionsByAttByBrandBySegByStepMC = 
					MatrixFunctions.scaleCopyOfDouble5dMatrix(
							mcStats.getPerceptionsByAttByBrandBySegByStep(
									perceptionsHistoryPeriod),
							10.0);
			int attributes = perceptionsByAttByBrandBySegByStepMC.length;
			
			ScoreBean[] beansByAttribute = new ScoreBean[attributes];
			for (int att = 0; att<attributes; att++) {
				beansByAttribute[att] = ScoreBean.mergeBeans(
						function.computeSegmentScoreDetails(
								perceptionsHistoryByAttribute[att], 
								perceptionsByAttByBrandBySegByStepMC[att],
									computeTraining, holdOut));
			}
			return beansByAttribute;
			
		default:
			return null;
		}
	}
	
	
	/*
	 * Getters
	 */
	
	public KPIDetail getSalesDetail() {
		return salesDetail;
	}

	public KPIDetail getAwarenessDetail() {
		return awarenessDetail;
	}

	public KPIDetail getPerceptionsDetail() {
		return perceptionsDetail;
	}
	
	public Statistics.TimePeriod getAwarenessHistoryPeriod() {
		return awarenessHistoryPeriod;
	}

	public Statistics.TimePeriod getPerceptionsHistoryPeriod() {
		return perceptionsHistoryPeriod;
	}
	
	public double [][][] getSalesHistory() {
		return salesHistory;
	}

	public int[][] getTotalSalesHistory() {
		return totalSalesHistory;
	}

	public double[][][] getAwarenessHistory() {
		return awarenessHistory;
	}

	public double[][] getAwarenessAggregatedHistory() {
		return awarenessAggregatedHistory;
	}
	
	public double[][] getPerceptionsAggregatedHistory() {
		return perceptionsAggregatedHistory;
	}
	
	public double getSalesWeight() {
		return salesWeight;
	}

	public double getAwarenessWeight() {
		return awarenessWeight;
	}

	public double getPerceptionsWeight() {
		return perceptionsWeight;
	}

	public double getWomVolumeWeight() {
		return womVolumeWeight;
	}
}
