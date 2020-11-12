package es.ugr.sci2s.soccer.beans;

import java.util.Arrays;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.StatUtils;

import util.functions.ArrayFunctions;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics;
import util.statistics.Statistics.TimePeriod;

public class SimulationResult {

	// Brand Segment
	public double[][] salesByBrandBySegmentAvg;
	double[][] salesByBrandBySegmentMin;
	double[][] salesByBrandBySegmentMax;
	
	public double[][][] salesByBrandBySegmentMC;
	
	// Brand Segment Step
	public double[][][] salesByBrandBySegmentByStepAvg;
	public double[][][] salesByBrandBySegmentByStepMin;
	public double[][][] salesByBrandBySegmentByStepMax;
	
	// Brand Segment Step MC
	public double[][][][] salesByBrandBySegmentByStepMC;

	// Attribute Brand Segment Step
	public double[][][][] perceptionsByDriverByBrandBySegmentByStepAvg;
	public double[][][][] perceptionsByDriverByBrandBySegmentByStepMin;
	public double[][][][] perceptionsByDriverByBrandBySegmentByStepMax;
	
	// Attribute Segment Brand
	double[][][][] perceptionsByDriverByBrandBySegmentLastStepMC;
	
	// Brand Segment Step
	public double[][][] awarenessByBrandBySegByStepAvg;
	public double[][][] awarenessByBrandBySegByStepMin;
	public double[][][] awarenessByBrandBySegByStepMax;
	
	// Brand Segment MC
	double[][][] awarenessByBrandBySegmentLastStepMC;

	// Brand Segment Step
	public double [][][] womVolumeByBrandBySegByStepAvg;
	public double [][][] womVolumeByBrandBySegByStepMin;
	public double [][][] womVolumeByBrandBySegByStepMax;
	
	// Brand Segment MC 
	public double [][][] womVolumeByBrandBySegMC;
	
	// Driver Segment Step
	public double [][][] womVolumeByDriverBySegByStepAvg;
	public double [][][] womVolumeByDriverBySegByStepMin;
	public double [][][] womVolumeByDriverBySegByStepMax;
	
	// Driver Segment MC
	public double [][][] womVolumeByDriverBySegMC;
	
	// Brand Segment Step
	public double [][][] womReachByBrandBySegByStepAvg;
	public double [][][] womReachByBrandBySegByStepMin;
	public double [][][] womReachByBrandBySegByStepMax;
	
	// Brand Step
	public double [][] womSentimentByBrandByStepAvg;
	public double [][] womSentimentByBrandByStepMin;
	public double [][] womSentimentByBrandByStepMax;
	
	// Touchpoint Brand Segment 
	double [][][] womContributionByTpByBrandBySegAvg;
	double [][][] womContributionByTpByBrandBySegMin;
	double [][][] womContributionByTpByBrandBySegMax;
	
	// Touchpoint Brand Segment MC 
	public double [][][][] womContributionByTpByBrandBySegMC;

	// Touchpoint Driver Brand Segment
	public double [][][][] contributionByTouchpointByDriverByBrandBySegmentAvg;
	double [][][][] contributionByTouchpointByDriverByBrandBySegmentMin;
	double [][][][] contributionByTouchpointByDriverByBrandBySegmentMax;
	
	// Touchpoint Driver Brand Segment MC
	double [][][][][] contributionByTouchpointByDriverByBrandBySegmentMC;

	// Touchpoint Brand Segment MC
	double [][][][] reachByTouchpointByBrandBySegmentMC;
	
	private String errorMessage;
	
	public void loadValuesFromStatistics(
			MonteCarloStatistics statistics, 
			double ratio,
			StatisticsRecordingBean recordingBean,
			TimePeriod period
		) {
		
		int step = statistics.getNrSteps();
		int stepsByPeriod = Statistics.calculateWeeksPerPeriod(period);
		
		int totalSteps = step / stepsByPeriod;
		
		// If the provided periodicity leaves some missing weeks, 
		// we include another step.
		if(step%stepsByPeriod!=0) {
			totalSteps++;
		}
		
		//Sales
		if(recordingBean.exportSales) {
			loadSales(statistics, ratio, step, stepsByPeriod, totalSteps);
		}
		
		//Perceptions
		if(recordingBean.exportPerceptions) {
			loadPerceptions(statistics, step, stepsByPeriod, totalSteps);
		}
		
		//Awareness
		if(recordingBean.exportAwareness) {
			loadAwareness(statistics, step, stepsByPeriod, totalSteps);
		}
		
		if(recordingBean.anyWoM) {
			if(recordingBean.exportWomReach) {
				loadWomReach(statistics, step, stepsByPeriod, totalSteps);
			}
			
			if(recordingBean.exportWomVolumen) {
				loadWoMVolume(statistics, step, stepsByPeriod, totalSteps);
			}
			
			if(recordingBean.exportWomSentiment) {
				loadWoMSentiment(statistics, step, stepsByPeriod, totalSteps);
			}
			
			if(recordingBean.exportWomContributions) {
				loadWoMContributions(statistics);
			}
		}
		
		if(recordingBean.exportTouchPointContributions) {
			loadTPContributions(statistics);
		}
		
		if(recordingBean.exportReach) {
			loadTPReach(statistics);
		}
		
	}
	
	private void loadSales(
			MonteCarloStatistics statistics, 
			double ratio,
			int step,
			int stepsByPeriod,
			int totalSteps) {
		// MC Brand Segment Step
		double[][][][] salesByBrandBySegmentByStep = 
				statistics.computeScaledSalesByBrandBySegmentByStep(TimePeriod.WEEKLY);
		// MC Brand Segment
		int[][][] salesByBrandBySegment = statistics.computeScaledSalesByBrandBySegment();
		
		int numMC = statistics.getNumberOfMonteCarloRepetitions();
		int brand = statistics.getNrBrands();
		int segment = statistics.getNrSegments();
		double[] aux = new double[numMC];
		
		salesByBrandBySegmentAvg = new double [brand][segment];
		salesByBrandBySegmentMin = new double [brand][segment];
		salesByBrandBySegmentMax = new double [brand][segment];
		
		salesByBrandBySegmentMC = new double [brand][segment][numMC];
		
		salesByBrandBySegmentByStepAvg = new double [brand][segment][totalSteps];
		salesByBrandBySegmentByStepMin = new double [brand][segment][totalSteps];
		salesByBrandBySegmentByStepMax = new double [brand][segment][totalSteps];
		salesByBrandBySegmentByStepMC = new double [brand][segment][totalSteps][];
		
		for(int b = 0; b < brand; b++) {
			for(int s = 0; s < segment; s++) {
				
				for(int mc = 0; mc < numMC; mc++) {
					salesByBrandBySegmentMC[b][s][mc] = salesByBrandBySegment[mc][b][s];
				}
				
				salesByBrandBySegmentAvg[b][s] = StatUtils.mean(salesByBrandBySegmentMC[b][s]);
				salesByBrandBySegmentMin[b][s] = NumberUtils.min(salesByBrandBySegmentMC[b][s]);
				salesByBrandBySegmentMax[b][s] = NumberUtils.max(salesByBrandBySegmentMC[b][s]);
				
				for(int t = 0; t < totalSteps; t++) {
					
					int start = t*stepsByPeriod;
					int end = Math.min(
							start+stepsByPeriod, 
							step);
					
					for(int mc = 0; mc < numMC; mc++) {
						
						aux[mc] = ArrayFunctions.addArraySegment(
								salesByBrandBySegmentByStep[mc][b][s],
								start,end);
					}
					
					salesByBrandBySegmentByStepAvg[b][s][t] = StatUtils.mean(aux);
					salesByBrandBySegmentByStepMin[b][s][t] = NumberUtils.min(aux);
					salesByBrandBySegmentByStepMax[b][s][t] = NumberUtils.max(aux);
					
					salesByBrandBySegmentByStepMC[b][s][t] = Arrays.copyOf(aux, numMC);
				}
			}
		}
		
	}
	
	private void loadAwareness(
			MonteCarloStatistics statistics, 
			int step,
			int stepsByPeriod,
			int totalSteps) {
		
		// MC Segment Brand Step
		double[][][][] awarenessBySegByBrandByStep = 
				statistics.getAwarenessBySegmentByBrandByStep(TimePeriod.WEEKLY);
		
		int numMC = statistics.getNumberOfMonteCarloRepetitions();
		int brand = statistics.getNrBrands();
		int segment = statistics.getNrSegments();
		double[] aux = new double[numMC];
		
		awarenessByBrandBySegByStepAvg = new double [brand][segment][totalSteps];
		awarenessByBrandBySegByStepMin = new double [brand][segment][totalSteps];
		awarenessByBrandBySegByStepMax = new double [brand][segment][totalSteps];
		
		awarenessByBrandBySegmentLastStepMC = new double [brand][segment][numMC];
		
		for(int b = 0; b < brand; b++) {
			for(int s = 0; s < segment; s++) {
				for(int t = 0; t < totalSteps; t++) {
					int end = Math.min((t+1) * stepsByPeriod - 1,step-1);
					
					for(int mc = 0; mc < numMC; mc++) {
						aux[mc] = awarenessBySegByBrandByStep[mc][s][b][end];
						
						if (end==step-1) {
							awarenessByBrandBySegmentLastStepMC[b][s][mc] 
									= awarenessBySegByBrandByStep[mc][s][b][end];
						}
					}
					
					awarenessByBrandBySegByStepAvg[b][s][t] = StatUtils.mean(aux);
					awarenessByBrandBySegByStepMin[b][s][t] = NumberUtils.min(aux);
					awarenessByBrandBySegByStepMax[b][s][t] = NumberUtils.max(aux);	
				}
			}
		}
	}
	
	private void loadPerceptions(
			MonteCarloStatistics statistics, 
			int step,
			int stepsByPeriod,
			int totalSteps) {
		// MC Segment Attribute Brand Step
		double[][][][][] perceptionsBySegByAttByBrandByStep = statistics.getPerceptionsBySegByAttByBrandByStep();
		
		int numMC = statistics.getNumberOfMonteCarloRepetitions();
		int att = statistics.getNrAttributes();
		int brand = statistics.getNrBrands();
		int segment = statistics.getNrSegments();
		double[] aux = new double[numMC];
		
		perceptionsByDriverByBrandBySegmentByStepAvg = new double [att][brand][segment][totalSteps];
		perceptionsByDriverByBrandBySegmentByStepMin = new double [att][brand][segment][totalSteps];
		perceptionsByDriverByBrandBySegmentByStepMax = new double [att][brand][segment][totalSteps];
		
		perceptionsByDriverByBrandBySegmentLastStepMC = new double [att][brand][segment][numMC];
		
		for(int a = 0; a < att; a++) {
			for(int b = 0; b < brand; b++) {
				for(int s = 0; s < segment; s++) {
					for(int t = 0; t < totalSteps; t++) {
						
						int end = Math.min((t+1) * stepsByPeriod - 1,step-1);
						
						for(int mc = 0; mc < numMC; mc++) {
							double value = perceptionsBySegByAttByBrandByStep[mc][s][a][b][end];
							aux[mc] = value;
							
							if(end==step-1) {
								perceptionsByDriverByBrandBySegmentLastStepMC[a][b][s][mc]=value;
							}
						}
						
						double mean = StatUtils.mean(aux); 
						perceptionsByDriverByBrandBySegmentByStepAvg[a][b][s][t] = mean;
						
						perceptionsByDriverByBrandBySegmentByStepMin[a][b][s][t] = NumberUtils.min(aux);
						perceptionsByDriverByBrandBySegmentByStepMax[a][b][s][t] = NumberUtils.max(aux);
					}
				}
			}
		}
	}
	
	private void loadTPContributions(
			MonteCarloStatistics statistics
			) {
		
		// MC Segment Attribute Brand Touchpoint
		double [][][][][] contributionBySegByAttByBrandByTp = statistics.getContributionBySegByAttByBrandByTp();
		
		int numTp = contributionBySegByAttByBrandByTp[0][0][0][0].length;
		int numMC = statistics.getNumberOfMonteCarloRepetitions();
		int att = statistics.getNrAttributes();
		int brand = statistics.getNrBrands();
		int segment = statistics.getNrSegments();
		
		contributionByTouchpointByDriverByBrandBySegmentAvg = new double [numTp][att][brand][segment];
		contributionByTouchpointByDriverByBrandBySegmentMin = new double [numTp][att][brand][segment];
		contributionByTouchpointByDriverByBrandBySegmentMax = new double [numTp][att][brand][segment];
		
		contributionByTouchpointByDriverByBrandBySegmentMC = new double [numTp][att][brand][segment][numMC];
		
		for(int tp = 0; tp < numTp; tp++) {
			for(int a = 0; a < att; a++) {
				for(int b = 0; b < brand; b++) {
					for(int s = 0; s < segment; s++) {
						for(int mc = 0; mc < numMC; mc++) {
							contributionByTouchpointByDriverByBrandBySegmentMC[tp][a][b][s][mc] 
									= contributionBySegByAttByBrandByTp[mc][s][a][b][tp];
						}
						
						contributionByTouchpointByDriverByBrandBySegmentAvg[tp][a][b][s] 
								= StatUtils.mean(contributionByTouchpointByDriverByBrandBySegmentMC[tp][a][b][s]);
						contributionByTouchpointByDriverByBrandBySegmentMin[tp][a][b][s] 
								= NumberUtils.min(contributionByTouchpointByDriverByBrandBySegmentMC[tp][a][b][s]);
						contributionByTouchpointByDriverByBrandBySegmentMax[tp][a][b][s] 
								= NumberUtils.max(contributionByTouchpointByDriverByBrandBySegmentMC[tp][a][b][s]);
					}
				}
			}
		}
	}
	
	private void loadTPReach(
			MonteCarloStatistics statistics
			) {
		// MC Tp Brand Segment
		double [][][][] reach  = statistics.computeReachByMcByTouchpointByBrandBySegment();
		
		int numMC = reach.length;
		int numTp = reach[0].length;
		int brands = statistics.getNrBrands();
		int segments = statistics.getNrSegments();
		
		double [][][][] formattedReach = new double [numTp][brands][segments][numMC];
		
		for (int tp = 0; tp<numTp; tp++) {
			for (int b = 0; b<brands; b++) {
				for (int seg = 0; seg<segments; seg++) {
					for (int mc = 0; mc<numMC; mc++) {
						formattedReach[tp][b][seg][mc] = reach[mc][tp][b][seg];
					}
				}
			}
		}
		
		this.reachByTouchpointByBrandBySegmentMC=formattedReach;
	}
	
	private void loadWomReach(
			MonteCarloStatistics statistics, 
			int step,
			int stepsByPeriod,
			int totalSteps) {
		
		// MC Segment Brand Step
		double [][][][] womReachBySegByBrandByStep = statistics.getWomReachBySegmentByBrandByStep();
		
		int numMC = statistics.getNumberOfMonteCarloRepetitions();
		int brand = statistics.getNrBrands();
		int segment = statistics.getNrSegments();
		double[] aux = new double[numMC];
		
		womReachByBrandBySegByStepAvg = new double [brand][segment][totalSteps];
		womReachByBrandBySegByStepMin = new double [brand][segment][totalSteps];
		womReachByBrandBySegByStepMax = new double [brand][segment][totalSteps];
		
		for(int b = 0; b < brand; b++) {
			for(int s = 0; s < segment; s++) {
				for(int t = 0; t < totalSteps; t++) {
					
					int end = Math.min((t+1) * stepsByPeriod - 1,step-1);
					
					for(int mc = 0; mc < numMC; mc++) {
						aux[mc] = womReachBySegByBrandByStep[mc][s][b][end];
					}
					
					womReachByBrandBySegByStepAvg[b][s][t] = StatUtils.mean(aux);
					womReachByBrandBySegByStepMin[b][s][t] = NumberUtils.min(aux);
					womReachByBrandBySegByStepMax[b][s][t] = NumberUtils.max(aux);
				}
			}
		}
	}
	
	private void loadWoMVolume(
			MonteCarloStatistics statistics, 
			int step,
			int stepsByPeriod,
			int totalSteps) {
		
		// MC Segment Brand Step
		double [][][][] womVolumeBySegByBrandByStep = statistics.getVolumeBySegByBrandByStep();
		// MC Segment Attribute Step
		double [][][][] womVolumeBySegByAttByStep = statistics.getVolumeBySegByAttByStep();

		int numMC = statistics.getNumberOfMonteCarloRepetitions();
		int att = statistics.getNrAttributes();
		int brand = statistics.getNrBrands();
		int segment = statistics.getNrSegments();
		double[] aux = new double[numMC];
		
		womVolumeByBrandBySegByStepAvg = new double [brand][segment][totalSteps];
		womVolumeByBrandBySegByStepMin = new double [brand][segment][totalSteps];
		womVolumeByBrandBySegByStepMax = new double [brand][segment][totalSteps];
		
		womVolumeByBrandBySegMC = new double [brand][segment][numMC];
		
		womVolumeByDriverBySegByStepAvg = new double [att][segment][totalSteps];
		womVolumeByDriverBySegByStepMin = new double [att][segment][totalSteps];
		womVolumeByDriverBySegByStepMax = new double [att][segment][totalSteps];
		
		womVolumeByDriverBySegMC = new double [att][segment][numMC];
		
		for(int s = 0; s < segment; s++) {
			for(int t = 0; t < totalSteps; t++) {
				
				int end = Math.min((t+1) * stepsByPeriod - 1,step-1);
				
				for(int a = 0; a < att; a++) {
					for(int mc = 0; mc < numMC; mc++) {
						aux[mc] = womVolumeBySegByAttByStep[mc][s][a][end];
						womVolumeByDriverBySegMC[a][s][mc] 
								+= womVolumeBySegByAttByStep[mc][s][a][end];
					}
					womVolumeByDriverBySegByStepAvg[a][s][t] = StatUtils.mean(aux);
					womVolumeByDriverBySegByStepMin[a][s][t] = NumberUtils.min(aux);
					womVolumeByDriverBySegByStepMax[a][s][t] = NumberUtils.max(aux);
					
				}
				
				for(int b = 0; b < brand; b++) {
					for(int mc = 0; mc < numMC; mc++) {
						aux[mc] = womVolumeBySegByBrandByStep[mc][s][b][end];
						womVolumeByBrandBySegMC[b][s][mc] 
								+= womVolumeBySegByBrandByStep[mc][s][b][end];
					}
					womVolumeByBrandBySegByStepAvg[b][s][t] = StatUtils.mean(aux);
					womVolumeByBrandBySegByStepMin[b][s][t] = NumberUtils.min(aux);
					womVolumeByBrandBySegByStepMax[b][s][t] = NumberUtils.max(aux);
				}
			}
		}
	}
	
	private void loadWoMSentiment(
			MonteCarloStatistics statistics, 
			int step,
			int stepsByPeriod,
			int totalSteps) {
		
		// MC Brand Step
		double [][][] womSentimentByBrandByStep = statistics.getWomSentimentByBrandByStep();
		
		int numMC = statistics.getNumberOfMonteCarloRepetitions();
		int brand = statistics.getNrBrands();
		double[] aux = new double[numMC];
		
		womSentimentByBrandByStepAvg = new double [brand][totalSteps];
		womSentimentByBrandByStepMin = new double [brand][totalSteps];
		womSentimentByBrandByStepMax = new double [brand][totalSteps];
		
		for(int b = 0; b < brand; b++) {
			for(int t = 0; t < totalSteps; t++) {
				for(int mc = 0; mc < numMC; mc++) {
					
					int end = Math.min((t+1) * stepsByPeriod - 1,step-1);
					
					aux[mc] = womSentimentByBrandByStep[mc][b][end];
				}
				womSentimentByBrandByStepAvg[b][t] = StatUtils.mean(aux);
				womSentimentByBrandByStepMin[b][t] = NumberUtils.min(aux);
				womSentimentByBrandByStepMax[b][t] = NumberUtils.max(aux);
			}
		}
	}
	
	private void loadWoMContributions(
			MonteCarloStatistics statistics
			) {
		
		// MC Segment Brand Touchpoint
		double [][][][] womContributionBySegByBrandByTp = statistics.getWomContributionstBySegmentByTp();
		
		int numTp = womContributionBySegByBrandByTp[0][0][0].length;
		int numMC = statistics.getNumberOfMonteCarloRepetitions();
		int brand = statistics.getNrBrands();
		int segment = statistics.getNrSegments();
		
		womContributionByTpByBrandBySegAvg = new double [numTp][brand][segment];
		womContributionByTpByBrandBySegMin = new double [numTp][brand][segment];
		womContributionByTpByBrandBySegMax = new double [numTp][brand][segment];
		
		womContributionByTpByBrandBySegMC = new double [numTp][brand][segment][numMC];
		
		for(int tp = 0; tp < numTp; tp++) {
			for(int b = 0; b < brand; b++) {
				for(int s = 0; s < segment; s++) {
					for(int mc = 0; mc < numMC; mc++) {
						womContributionByTpByBrandBySegMC[tp][b][s][mc] 
								= womContributionBySegByBrandByTp[mc][s][b][tp];
					}
					
					womContributionByTpByBrandBySegAvg[tp][b][s] = StatUtils.mean(womContributionByTpByBrandBySegMC[tp][b][s]);
					womContributionByTpByBrandBySegMin[tp][b][s] = NumberUtils.min(womContributionByTpByBrandBySegMC[tp][b][s]);
					womContributionByTpByBrandBySegMax[tp][b][s] = NumberUtils.max(womContributionByTpByBrandBySegMC[tp][b][s]);
				}
			}
		}
	}
	
	public static SimulationResult[] getResults(
			MonteCarloStatistics[] statistics, 
			TimePeriod period, 
			double ratio,
			StatisticsRecordingBean recordingBean
		) {
		int numResults = statistics.length;
		SimulationResult[] results = new SimulationResult[numResults];
		
		for (int i=0; i<numResults; i++) {
			results[i] = new SimulationResult();
			results[i].loadValuesFromStatistics(
					statistics[i], ratio, recordingBean, period);
		}
		
		return results;
	}
	
	public int getOverTimeSteps() {
		return salesByBrandBySegmentByStepAvg[0][0].length;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(awarenessByBrandBySegByStepAvg);
		result = prime * result + Arrays.deepHashCode(awarenessByBrandBySegByStepMax);
		result = prime * result + Arrays.deepHashCode(awarenessByBrandBySegByStepMin);
		result = prime * result + Arrays.deepHashCode(awarenessByBrandBySegmentLastStepMC);
		result = prime * result + Arrays.deepHashCode(contributionByTouchpointByDriverByBrandBySegmentAvg);
		result = prime * result + Arrays.deepHashCode(contributionByTouchpointByDriverByBrandBySegmentMC);
		result = prime * result + Arrays.deepHashCode(contributionByTouchpointByDriverByBrandBySegmentMax);
		result = prime * result + Arrays.deepHashCode(contributionByTouchpointByDriverByBrandBySegmentMin);
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + Arrays.deepHashCode(perceptionsByDriverByBrandBySegmentByStepAvg);
		result = prime * result + Arrays.deepHashCode(perceptionsByDriverByBrandBySegmentByStepMax);
		result = prime * result + Arrays.deepHashCode(perceptionsByDriverByBrandBySegmentByStepMin);
		result = prime * result + Arrays.deepHashCode(perceptionsByDriverByBrandBySegmentLastStepMC);
		result = prime * result + Arrays.deepHashCode(salesByBrandBySegmentAvg);
		result = prime * result + Arrays.deepHashCode(salesByBrandBySegmentByStepAvg);
		result = prime * result + Arrays.deepHashCode(salesByBrandBySegmentByStepMC);
		result = prime * result + Arrays.deepHashCode(salesByBrandBySegmentByStepMax);
		result = prime * result + Arrays.deepHashCode(salesByBrandBySegmentByStepMin);
		result = prime * result + Arrays.deepHashCode(salesByBrandBySegmentMC);
		result = prime * result + Arrays.deepHashCode(salesByBrandBySegmentMax);
		result = prime * result + Arrays.deepHashCode(salesByBrandBySegmentMin);
		result = prime * result + Arrays.deepHashCode(womContributionByTpByBrandBySegAvg);
		result = prime * result + Arrays.deepHashCode(womContributionByTpByBrandBySegMC);
		result = prime * result + Arrays.deepHashCode(womContributionByTpByBrandBySegMax);
		result = prime * result + Arrays.deepHashCode(womContributionByTpByBrandBySegMin);
		result = prime * result + Arrays.deepHashCode(womReachByBrandBySegByStepAvg);
		result = prime * result + Arrays.deepHashCode(womReachByBrandBySegByStepMax);
		result = prime * result + Arrays.deepHashCode(womReachByBrandBySegByStepMin);
		result = prime * result + Arrays.deepHashCode(womSentimentByBrandByStepAvg);
		result = prime * result + Arrays.deepHashCode(womSentimentByBrandByStepMax);
		result = prime * result + Arrays.deepHashCode(womSentimentByBrandByStepMin);
		result = prime * result + Arrays.deepHashCode(womVolumeByBrandBySegByStepAvg);
		result = prime * result + Arrays.deepHashCode(womVolumeByBrandBySegByStepMax);
		result = prime * result + Arrays.deepHashCode(womVolumeByBrandBySegByStepMin);
		result = prime * result + Arrays.deepHashCode(womVolumeByBrandBySegMC);
		result = prime * result + Arrays.deepHashCode(womVolumeByDriverBySegByStepAvg);
		result = prime * result + Arrays.deepHashCode(womVolumeByDriverBySegByStepMax);
		result = prime * result + Arrays.deepHashCode(womVolumeByDriverBySegByStepMin);
		result = prime * result + Arrays.deepHashCode(womVolumeByDriverBySegMC);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimulationResult other = (SimulationResult) obj;
		if (!Arrays.deepEquals(awarenessByBrandBySegByStepAvg, other.awarenessByBrandBySegByStepAvg))
			return false;
		if (!Arrays.deepEquals(awarenessByBrandBySegByStepMax, other.awarenessByBrandBySegByStepMax))
			return false;
		if (!Arrays.deepEquals(awarenessByBrandBySegByStepMin, other.awarenessByBrandBySegByStepMin))
			return false;
		if (!Arrays.deepEquals(awarenessByBrandBySegmentLastStepMC, other.awarenessByBrandBySegmentLastStepMC))
			return false;
		if (!Arrays.deepEquals(contributionByTouchpointByDriverByBrandBySegmentAvg,
				other.contributionByTouchpointByDriverByBrandBySegmentAvg))
			return false;
		if (!Arrays.deepEquals(contributionByTouchpointByDriverByBrandBySegmentMC,
				other.contributionByTouchpointByDriverByBrandBySegmentMC))
			return false;
		if (!Arrays.deepEquals(contributionByTouchpointByDriverByBrandBySegmentMax,
				other.contributionByTouchpointByDriverByBrandBySegmentMax))
			return false;
		if (!Arrays.deepEquals(contributionByTouchpointByDriverByBrandBySegmentMin,
				other.contributionByTouchpointByDriverByBrandBySegmentMin))
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (!Arrays.deepEquals(perceptionsByDriverByBrandBySegmentByStepAvg,
				other.perceptionsByDriverByBrandBySegmentByStepAvg))
			return false;
		if (!Arrays.deepEquals(perceptionsByDriverByBrandBySegmentByStepMax,
				other.perceptionsByDriverByBrandBySegmentByStepMax))
			return false;
		if (!Arrays.deepEquals(perceptionsByDriverByBrandBySegmentByStepMin,
				other.perceptionsByDriverByBrandBySegmentByStepMin))
			return false;
		if (!Arrays.deepEquals(perceptionsByDriverByBrandBySegmentLastStepMC,
				other.perceptionsByDriverByBrandBySegmentLastStepMC))
			return false;
		if (!Arrays.deepEquals(salesByBrandBySegmentAvg, other.salesByBrandBySegmentAvg))
			return false;
		if (!Arrays.deepEquals(salesByBrandBySegmentByStepAvg, other.salesByBrandBySegmentByStepAvg))
			return false;
		if (!Arrays.deepEquals(salesByBrandBySegmentByStepMC, other.salesByBrandBySegmentByStepMC))
			return false;
		if (!Arrays.deepEquals(salesByBrandBySegmentByStepMax, other.salesByBrandBySegmentByStepMax))
			return false;
		if (!Arrays.deepEquals(salesByBrandBySegmentByStepMin, other.salesByBrandBySegmentByStepMin))
			return false;
		if (!Arrays.deepEquals(salesByBrandBySegmentMC, other.salesByBrandBySegmentMC))
			return false;
		if (!Arrays.deepEquals(salesByBrandBySegmentMax, other.salesByBrandBySegmentMax))
			return false;
		if (!Arrays.deepEquals(salesByBrandBySegmentMin, other.salesByBrandBySegmentMin))
			return false;
		if (!Arrays.deepEquals(womContributionByTpByBrandBySegAvg, other.womContributionByTpByBrandBySegAvg))
			return false;
		if (!Arrays.deepEquals(womContributionByTpByBrandBySegMC, other.womContributionByTpByBrandBySegMC))
			return false;
		if (!Arrays.deepEquals(womContributionByTpByBrandBySegMax, other.womContributionByTpByBrandBySegMax))
			return false;
		if (!Arrays.deepEquals(womContributionByTpByBrandBySegMin, other.womContributionByTpByBrandBySegMin))
			return false;
		if (!Arrays.deepEquals(womReachByBrandBySegByStepAvg, other.womReachByBrandBySegByStepAvg))
			return false;
		if (!Arrays.deepEquals(womReachByBrandBySegByStepMax, other.womReachByBrandBySegByStepMax))
			return false;
		if (!Arrays.deepEquals(womReachByBrandBySegByStepMin, other.womReachByBrandBySegByStepMin))
			return false;
		if (!Arrays.deepEquals(womSentimentByBrandByStepAvg, other.womSentimentByBrandByStepAvg))
			return false;
		if (!Arrays.deepEquals(womSentimentByBrandByStepMax, other.womSentimentByBrandByStepMax))
			return false;
		if (!Arrays.deepEquals(womSentimentByBrandByStepMin, other.womSentimentByBrandByStepMin))
			return false;
		if (!Arrays.deepEquals(womVolumeByBrandBySegByStepAvg, other.womVolumeByBrandBySegByStepAvg))
			return false;
		if (!Arrays.deepEquals(womVolumeByBrandBySegByStepMax, other.womVolumeByBrandBySegByStepMax))
			return false;
		if (!Arrays.deepEquals(womVolumeByBrandBySegByStepMin, other.womVolumeByBrandBySegByStepMin))
			return false;
		if (!Arrays.deepEquals(womVolumeByBrandBySegMC, other.womVolumeByBrandBySegMC))
			return false;
		if (!Arrays.deepEquals(womVolumeByDriverBySegByStepAvg, other.womVolumeByDriverBySegByStepAvg))
			return false;
		if (!Arrays.deepEquals(womVolumeByDriverBySegByStepMax, other.womVolumeByDriverBySegByStepMax))
			return false;
		if (!Arrays.deepEquals(womVolumeByDriverBySegByStepMin, other.womVolumeByDriverBySegByStepMin))
			return false;
		if (!Arrays.deepEquals(womVolumeByDriverBySegMC, other.womVolumeByDriverBySegMC))
			return false;
		return true;
	}
}
