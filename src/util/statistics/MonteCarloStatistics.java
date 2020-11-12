package util.statistics;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math.util.MathUtils;
import org.apache.commons.math3.stat.StatUtils;

import util.functions.Functions;
import util.statistics.Statistics.TimePeriod;

public class MonteCarloStatistics {
	// ########################################################################	
	// Static variables 	
	// ########################################################################		
	
	protected static final int MONTE_CARLO_ARRAY_NR_ROWS = 3; // avg, min, max
	public static final int MONTE_CARLO_AVG_INDEX = 0;
	protected static final int MONTE_CARLO_MIN_INDEX = 1;
	protected static final int MONTE_CARLO_MAX_INDEX = 2;

	// ########################################################################	
	// Local/Instance variables 	
	// ########################################################################		

	protected int nrMC;
	protected int nrSegments;
	protected int nrBrands;
	protected int nrAttributes;
	protected int nrSteps;

	private Statistics[] statistics;

	
	private double[][][][] womVolumeBySegByBrandByStepMC;
	private double[][][][] womVolumeBySegByAttByStepMC;
	private double[][][] womSentimentByBrandByStepMC;
	private double[][][][] womReachBySegByBrandByStepMC;
	private double[][][] womVolumeByBrandByStepMC;
	private double[][][] womVolumeByAttByStepMC;		
	private double[][][] womReachByBrandByStepMC;
	
	// ########################################################################
	// Constructors
	// ########################################################################		

	public MonteCarloStatistics(
		int numberOfMC,
		int numberOfSegments,
		int numberOfBrands, 
		int numberOfAttributes,
		int numberOfSteps,
		int stepsByWeek
	) {
		this.nrMC = numberOfMC;
		this.nrSegments = numberOfSegments;
		this.nrBrands = numberOfBrands;
		this.nrAttributes = numberOfAttributes;
		this.nrSteps = numberOfSteps;
		
		// Reserve memory only for references!
		statistics = new Statistics[nrMC];
	}

	// ########################################################################
	// Methods/Functions
	// ########################################################################	
	
	synchronized public void saveStatistics(Statistics stats, int currentMC) {
		statistics[currentMC] = stats; 
	}
	
	public double[][][] computeScaledSalesByBrandByStep(TimePeriod period) {
		double[][][] sales = new double[nrMC][][];
		for(int i = 0; i < nrMC; i++) {
			sales[i] = statistics[i].computeScaledSalesByBrandByStep(period);
		}
		return sales;
	}
	
	public int[][][] computeScaledSalesByBrandBySegment() {
		int[][][] sales = new int[nrMC][][];
		for(int i = 0; i < nrMC; i++) {
			sales[i] = statistics[i].computeScaledSalesByBrandBySegment();
		}
		return sales;
	}
	
	public double[][][][] computeScaledSalesByBrandBySegmentByStep(
			TimePeriod period) {
		double[][][][] sales = new double[nrMC][][][];
		for(int i = 0; i < nrMC; i++) {
			sales[i] = statistics[i].
					computeScaledSegmentSalesByBrandBySegmentByStep(period);
		}
		return sales;
	}
	
	
	public StatisticsBean computeTimeSeriesAwareness() {
		//Single iteration case
		if(nrMC==1) {
			return new StatisticsBean(statistics[0].awarenessByBrandByStep);
		}
		
		double[][][] awarenessByBrandByStepMC =
			new double [MONTE_CARLO_ARRAY_NR_ROWS][nrBrands][nrSteps];	
		
		double[] aux = new double[nrMC];
		for(int b = 0; b < nrBrands; b++) {
			for(int t = 0; t < nrSteps; t++) {
				// awareness
				for(int mc = 0; mc < nrMC; mc++) {
					aux[mc] = statistics[mc].awarenessByBrandByStep[b][t];			
				}
				awarenessByBrandByStepMC[MONTE_CARLO_AVG_INDEX][b][t] = StatUtils.mean(aux);
				awarenessByBrandByStepMC[MONTE_CARLO_MIN_INDEX][b][t] = NumberUtils.min(aux);
				awarenessByBrandByStepMC[MONTE_CARLO_MAX_INDEX][b][t] = NumberUtils.max(aux);
			}
		}
		return new StatisticsBean(awarenessByBrandByStepMC[0], awarenessByBrandByStepMC[1], awarenessByBrandByStepMC[2]);		
	}

	public double [][][] getAwarenessByBrandByStep(TimePeriod period) {
		double[][][] awarenessByBrandByStepMC = new double [nrMC][][];
		for(int mc = 0; mc < nrMC; mc++) {
			awarenessByBrandByStepMC[mc] = statistics[mc].computeAwarenessByBrandByStep(period);			
		}
		return awarenessByBrandByStepMC;
	}
	
	public double [][] getTotalAwarenessByBrand() {
		double[][] awarenessByBrandByStepMC = new double [nrMC][];
		for(int mc = 0; mc < nrMC; mc++) {
			double[][] awarenessByBrandByStep = statistics[mc].computeAwarenessByBrandByStep(TimePeriod.WEEKLY);
			int brands = awarenessByBrandByStep.length;
			awarenessByBrandByStepMC[mc]=new double[brands];
			for (int b=0; b<brands; b++) {
				awarenessByBrandByStepMC[mc][b] = StatUtils.mean(awarenessByBrandByStep[b]);
			}
		}
		return awarenessByBrandByStepMC;
	}
	
	public double [][][][] getAwarenessBySegmentByBrandByStep(TimePeriod period) {
		double[][][][] awarenessByBrandByStepMC = new double [nrMC][][][];
		for(int mc = 0; mc < nrMC; mc++) {
			awarenessByBrandByStepMC[mc] = statistics[mc]
					.computeAwarenesssBySegmentByBrandByStep(period);			
		}
		return awarenessByBrandByStepMC;
	}
	
	public double [][][][] getAwarenessByBrandBySegmentByStep(TimePeriod period) {
		double[][][][] awarenessByBrandByStepMC = new double [nrMC][nrBrands][nrSegments][nrSteps];
		
		double[][][][] awarenessBySegmentByStepMC = getAwarenessBySegmentByBrandByStep(period);
		
		int periodSteps = awarenessBySegmentByStepMC[0][0][0].length;
		
		for(int mc = 0; mc < nrMC; mc++) {
			for(int b = 0; b < nrBrands; b++) {
				for(int seg = 0; seg < nrSegments; seg++) {
					for(int step = 0; step < periodSteps; step++) {
						awarenessByBrandByStepMC[mc][b][seg][step] = 
								awarenessBySegmentByStepMC[mc][seg][b][step];
					}
				}
			}
		}
		return awarenessByBrandByStepMC;
	}
	
	public double[][][] computePerceptionsByBrandByStepMC(TimePeriod period) {
		double[][][] perceptionsByBrandByStepMC = new double [nrMC][][];
		
		for (int mc =0; mc<nrMC; mc++) {
			perceptionsByBrandByStepMC[mc] = statistics[mc]
					.computeAveragedPerceptionsByBrandByStep();
		}
		
		return perceptionsByBrandByStepMC;
	}
	
	public double[][] computeAveragedPerceptionsByBrandByStep(TimePeriod period) {
		double[][][] perceptionsByBrandByStepMC = computePerceptionsByBrandByStepMC(period);
		
		int periodSteps = perceptionsByBrandByStepMC[0][0].length;
		
		double[][] averagedPerceptions = new double [nrBrands][periodSteps];
		
		double[] aux = new double[nrMC];
		for(int b = 0; b < nrBrands; b++) {
			for(int t = 0; t < periodSteps; t++) {
				for(int mc = 0; mc < nrMC; mc++) {
					aux[mc] = perceptionsByBrandByStepMC[mc][b][t];			
				}
				averagedPerceptions[b][t] = StatUtils.mean(aux);
			}
		}
		
		return averagedPerceptions;
	}
	
	public double[][][][] computePerceptionsByBrandBySegmentByStep(TimePeriod period) {
		
		double[][][][][] perceptionsBySegmentByAttByBrandByStepMC 
			= getPerceptionsBySegByAttByBrandByStep(period);
		
		int steps = perceptionsBySegmentByAttByBrandByStepMC[0][0][0][0].length;
		
		double[][][][] segmentPerceptions = new double [nrMC][nrBrands][nrSegments][steps];
		
		for (int mc = 0; mc<nrMC; mc++) {
			for(int b = 0; b < nrBrands; b++) {
				for(int s = 0; s < nrSegments; s++) {
					for(int t = 0; t < steps; t++) {
						double attValue = 0;
						for(int att = 0; att < nrAttributes; att++) {
							attValue += perceptionsBySegmentByAttByBrandByStepMC[mc][s][att][b][t];
						}
						attValue/=nrAttributes;
						segmentPerceptions[mc][b][s][t] = attValue;
					}
				}
			}
		}
		
		return segmentPerceptions;
	}
	
	public StatisticsBean[] computeTimeSeriesPerception() {
		
		StatisticsBean[] beans= new StatisticsBean[nrAttributes];
		
		//Single iteration case
		if(nrMC==1) {
			for (int a=0; a<nrAttributes; a++) {
				beans[a]= new StatisticsBean(statistics[0].perceptionsByAttByBrandByStep[a]);
			}
		}
		
		double[][][][] perceptionsByAttByBrandByStepMC =
			new double [MONTE_CARLO_ARRAY_NR_ROWS][nrAttributes][nrBrands][nrSteps];	
		
		double[] aux = new double[nrMC];
		for(int b = 0; b < nrBrands; b++) {
			for(int t = 0; t < nrSteps; t++) {
				// perceptions
				for(int k = 0; k < nrAttributes; k++) {
					for(int mc = 0; mc < nrMC; mc++) {
						aux[mc] = statistics[mc].perceptionsByAttByBrandByStep[k][b][t];			
					}
					perceptionsByAttByBrandByStepMC[MONTE_CARLO_AVG_INDEX][k][b][t] = StatUtils.mean(aux);
					perceptionsByAttByBrandByStepMC[MONTE_CARLO_MIN_INDEX][k][b][t] = NumberUtils.min(aux);
					perceptionsByAttByBrandByStepMC[MONTE_CARLO_MAX_INDEX][k][b][t] = NumberUtils.max(aux);
				}			
			}
		}

		for (int i=0; i<nrAttributes; i++) {
			beans[i] = new StatisticsBean(
					perceptionsByAttByBrandByStepMC[MONTE_CARLO_AVG_INDEX][i], 
					perceptionsByAttByBrandByStepMC[MONTE_CARLO_MIN_INDEX][i], 
					perceptionsByAttByBrandByStepMC[MONTE_CARLO_MAX_INDEX][i]
						);
		}
		
		return beans;		
	}
	
	public double [][][][] getPerceptionsByAttByBrandByStep() {
		double[][][][] perceptionsByAttByBrandByStep = 
				new double[nrMC][][][];
		for(int mc = 0; mc < nrMC; mc++) {
			perceptionsByAttByBrandByStep[mc] = 
					statistics[mc].perceptionsByAttByBrandByStep;			
		}
		return perceptionsByAttByBrandByStep;
	}
	
	public double [][][][][] getPerceptionsBySegByAttByBrandByStep(TimePeriod period) {
		double[][][][][] perceptionsBySegByAttByBrandByStepMC = 
				new double[nrMC][][][][];
		for(int mc = 0; mc < nrMC; mc++) {
			perceptionsBySegByAttByBrandByStepMC[mc] = 
					statistics[mc].computePerceptionsBySegByAttByBrandByStep(period);			
		}
		return perceptionsBySegByAttByBrandByStepMC;
	}
	
	/**
	 * Computes the perceptions values by attribute.
	 * IMPORTANT:: Indexes go as ATT MC BRANDS SEGMENTS STEPS
	 * @param period the given period for the statistics.
	 * @return the perceptions values.
	 */
	public double [][][][][] getPerceptionsByAttByBrandBySegByStep(TimePeriod period) {
		double[][][][][] perceptionsBySegByAttByBrandByStepMC = 
				getPerceptionsBySegByAttByBrandByStep(period);
		
		int periodSteps = perceptionsBySegByAttByBrandByStepMC[0][0][0][0].length;
		
		double[][][][][] perceptionsByAttByBrandBySegByStepMC = 
				new double[nrAttributes][nrMC][nrBrands][nrSegments][periodSteps];
		
		for(int mc = 0; mc < nrMC; mc++) {
			for (int a = 0; a<nrAttributes; a++) {
				for (int b =0; b<nrBrands; b++) {
					for (int seg = 0; seg<nrSegments; seg++) {
						for (int t = 0; t<periodSteps; t++) {
							perceptionsByAttByBrandBySegByStepMC[a][mc][b][seg][t] =
									perceptionsBySegByAttByBrandByStepMC[mc][seg][a][b][t];
						}
					}
				}
			}
		}
		
		return perceptionsByAttByBrandBySegByStepMC;
	}
	
	public double [][][][][] getPerceptionsBySegByAttByBrandByStep() {
		double[][][][][] perceptionsBySegByAttByBrandByStepMC = 
				new double[nrMC][][][][];
		for(int mc = 0; mc < nrMC; mc++) {
			perceptionsBySegByAttByBrandByStepMC[mc] = 
					statistics[mc].perceptionsBySegByAttByBrandByStep;			
		}
		return perceptionsBySegByAttByBrandByStepMC;
	}
	
	public StatisticsBean[][] computeTimeSeriesPerceptionBySegments() {
		
		StatisticsBean[][] beans= new StatisticsBean[nrSegments][nrAttributes];
		
		//Single iteration case
		if(nrMC==1) {
			for (int s=0; s<nrSegments; s++) {
				for (int a=0; a<nrAttributes; a++) {
					beans[s][a]= new StatisticsBean(statistics[0].perceptionsBySegByAttByBrandByStep[s][a]);
				}
			}
		}
		
		double[][][][][] perceptionsBySegByAttByBrandByStepMC =
			new double [MONTE_CARLO_ARRAY_NR_ROWS][nrSegments][nrAttributes][nrBrands][nrSteps];	
		
		double[] aux = new double[nrMC];
		for(int b = 0; b < nrBrands; b++) {
			for(int t = 0; t < nrSteps; t++) {
				for (int s=0; s < nrSegments; s++) {
					// perceptions
					for(int k = 0; k < nrAttributes; k++) {
						for(int mc = 0; mc < nrMC; mc++) {
							aux[mc] = statistics[mc].perceptionsBySegByAttByBrandByStep[s][k][b][t];			
						}
						perceptionsBySegByAttByBrandByStepMC[MONTE_CARLO_AVG_INDEX][s][k][b][t] = StatUtils.mean(aux);
						perceptionsBySegByAttByBrandByStepMC[MONTE_CARLO_MIN_INDEX][s][k][b][t] = NumberUtils.min(aux);
						perceptionsBySegByAttByBrandByStepMC[MONTE_CARLO_MAX_INDEX][s][k][b][t] = NumberUtils.max(aux);
					}
				}
			}
		}

		for (int s=0; s < nrSegments; s++) {
			for (int i=0; i<nrAttributes; i++) {
				beans[s][i] = new StatisticsBean(
						perceptionsBySegByAttByBrandByStepMC[MONTE_CARLO_AVG_INDEX][s][i], 
						perceptionsBySegByAttByBrandByStepMC[MONTE_CARLO_MIN_INDEX][s][i], 
						perceptionsBySegByAttByBrandByStepMC[MONTE_CARLO_MAX_INDEX][s][i]
							);
			}
		}
		
		return beans;		
	}
	
	private void initWoMStructs() {
		// WoM reports - Volume, Sentiment, Reach
		womVolumeBySegByBrandByStepMC = 
			new double [MONTE_CARLO_ARRAY_NR_ROWS][nrSegments][nrBrands][nrSteps];
		womVolumeBySegByAttByStepMC =
			new double [MONTE_CARLO_ARRAY_NR_ROWS][nrSegments][nrAttributes][nrSteps];
		womSentimentByBrandByStepMC =
			new double [MONTE_CARLO_ARRAY_NR_ROWS][nrBrands][nrSteps];
		womReachBySegByBrandByStepMC =
			new double [MONTE_CARLO_ARRAY_NR_ROWS][nrSegments][nrBrands][nrSteps];
		womVolumeByBrandByStepMC =
			new double [MONTE_CARLO_ARRAY_NR_ROWS][nrBrands][nrSteps];
		womVolumeByAttByStepMC =
			new double [MONTE_CARLO_ARRAY_NR_ROWS][nrAttributes][nrSteps];	
		womReachByBrandByStepMC =
			new double [MONTE_CARLO_ARRAY_NR_ROWS][nrBrands][nrSteps];
		
		computeTimeSeriesWom(
				womVolumeBySegByBrandByStepMC, 
				womVolumeBySegByAttByStepMC, 
				womSentimentByBrandByStepMC, 
				womReachBySegByBrandByStepMC, 
				womVolumeByBrandByStepMC, 
				womVolumeByAttByStepMC, 
				womReachByBrandByStepMC
					);
	}
	
	public StatisticsBean[] getWoMReachBean() {
		
		if(womReachByBrandByStepMC==null || 
				womReachBySegByBrandByStepMC == null) {
			initWoMStructs();
		}
		
		StatisticsBean[] beans = new StatisticsBean[nrSegments+1];
		beans[0] = new StatisticsBean(
				womReachByBrandByStepMC[MONTE_CARLO_AVG_INDEX], 
				womReachByBrandByStepMC[MONTE_CARLO_MIN_INDEX], 
				womReachByBrandByStepMC[MONTE_CARLO_MAX_INDEX]
			);
		for (int i=0; i<nrSegments; i++) {
			beans[i+1] = new StatisticsBean(
					womReachBySegByBrandByStepMC[MONTE_CARLO_AVG_INDEX][i], 
					womReachBySegByBrandByStepMC[MONTE_CARLO_MIN_INDEX][i], 
					womReachBySegByBrandByStepMC[MONTE_CARLO_MAX_INDEX][i]
				);
		}
		
		return beans;
	}
	
	public double [][][] getWomReachByBrandByStep() {
		double[][][] womReach = 
				new double[nrMC][][];
		for(int mc = 0; mc < nrMC; mc++) {
			womReach[mc] = 
					statistics[mc].womReachByBrandByStep;	
		}
		return womReach;
	}
	
	public double [][][][] getWomReachBySegmentByBrandByStep() {
		double[][][][] womReach = 
				new double[nrMC][][][];
		for(int mc = 0; mc < nrMC; mc++) {
			womReach[mc] = 
					statistics[mc].womReachBySegByBrandByStep;
		}
		return womReach;
	}
	
	public StatisticsBean[] getWoMVolumeBySegAndBrandBeans() {
		
		if(womVolumeBySegByBrandByStepMC==null 
				|| womVolumeByBrandByStepMC==null) {
			initWoMStructs();
		}
		
		StatisticsBean[] beans = new StatisticsBean[nrSegments+1];
		beans[0] = new StatisticsBean(
				womVolumeByBrandByStepMC[MONTE_CARLO_AVG_INDEX], 
				womVolumeByBrandByStepMC[MONTE_CARLO_MIN_INDEX], 
				womVolumeByBrandByStepMC[MONTE_CARLO_MAX_INDEX]
			);
		for (int i=0; i<nrSegments; i++) {
			beans[i+1] = new StatisticsBean(
					womVolumeBySegByBrandByStepMC[MONTE_CARLO_AVG_INDEX][i], 
					womVolumeBySegByBrandByStepMC[MONTE_CARLO_MIN_INDEX][i], 
					womVolumeBySegByBrandByStepMC[MONTE_CARLO_MAX_INDEX][i]
				);
		}
		
		return beans;
	}
	
	public double [][][] getVolumeByBrandByStep() {
		double[][][] womVolume = 
				new double[nrMC][][];
		for(int mc = 0; mc < nrMC; mc++) {
			womVolume[mc] = 
					statistics[mc].womVolumeByBrandByStep;
		}
		return womVolume;
	}
	
	public double [][][] getVolumeByAttByStep() {
		double[][][] womVolume = 
				new double[nrMC][][];
		for(int mc = 0; mc < nrMC; mc++) {
			womVolume[mc] = 
					statistics[mc].womVolumeByAttByStep;
		}
		return womVolume;
	}
	
	public double [][][][] getVolumeBySegByAttByStep() {
		double [][][][] womVolume = 
				new double[nrMC][][][];
		for(int mc = 0; mc < nrMC; mc++) {
			womVolume[mc] = 
					statistics[mc].womVolumeBySegByAttByStep;
		}
		return womVolume;
	}
	
	public double [][][][] getVolumeBySegByBrandByStep() {
		double [][][][] womVolume = 
				new double[nrMC][][][];
		for(int mc = 0; mc < nrMC; mc++) {
			womVolume[mc] = 
					statistics[mc].womVolumeBySegByBrandByStep;
		}
		return womVolume;
	}
	
	/**
	 * These data should be displayed using boxplots, so mc data should be at 
	 * the lowest level.
	 * @return TouchPoint contribution for WoM by segment and brand.
	 */
	public StatisticsBean[][] getWoMContributionBeansBySegmentByTpByMC() {
		//Position 0 stores aggregated segments
		StatisticsBean[][] beans = new StatisticsBean[nrSegments+1][nrBrands];
		
		int nrTouchPoints = statistics[0].getNumberOfTouchpoints();
		
		double[][][] accumulatedContributions = new double [nrMC][nrBrands][nrTouchPoints];
		double[][][][] womNormalizedMCBySeg = new double [nrSegments+1][nrBrands][nrTouchPoints][nrMC];
		
		for (int mc=0; mc<nrMC; mc++) {
			for (int s=0; s<nrSegments; s++) {
				for (int b=0; b<nrBrands; b++) {
					double[] normalizedContribution;
					try {
						normalizedContribution = MathUtils.normalizeArray(
							statistics[mc].womContributionBySegByBrandByTp[s][b],
							Functions.TOTAL_AMOUNT_NORMALIZABLE_VALUE
							);
					} catch (ArithmeticException e) {
						normalizedContribution= new double [nrTouchPoints];
					}
					for (int tp=0; tp<nrTouchPoints; tp++) {
						accumulatedContributions[mc][b][tp]
								+= statistics[mc].womContributionBySegByBrandByTp[s][b][tp];
						womNormalizedMCBySeg[s+1][b][tp][mc]=normalizedContribution[tp];
					}
				}
			}
			for (int b=0; b<nrBrands; b++) {
				double[] normalizedContribution;
				try {
					normalizedContribution =MathUtils.normalizeArray(
							accumulatedContributions[mc][b],
							Functions.TOTAL_AMOUNT_NORMALIZABLE_VALUE
							);
				} catch (ArithmeticException e) {
					normalizedContribution = new double [nrTouchPoints];
				}
				
				for (int tp=0; tp<nrTouchPoints; tp++) {
					womNormalizedMCBySeg[0][b][tp][mc]=normalizedContribution[tp];
				}
			}
		}

		for (int s=0; s<nrSegments+1; s++) {
			for (int b=0; b<nrBrands; b++) {
				if(nrMC>1) {
					beans[s][b] = new StatisticsBean(
							womNormalizedMCBySeg[s][b], 
							womNormalizedMCBySeg[s][b], 
							womNormalizedMCBySeg[s][b]
						);
				} else {
					beans[s][b] = new StatisticsBean(
							womNormalizedMCBySeg[s][b]
						);
				}
			}
		}

		return beans;
	}
	
	public double [][][][] getWomContributionstBySegmentByTp() {
		double[][][][] womContributions = 
				new double[nrMC][][][];
		for(int mc = 0; mc < nrMC; mc++) {
			womContributions[mc] = 
					statistics[mc].getWomContributionBySegByBrandByTp();
		}
		return womContributions;
	}
	
	public double [][][][] getContributionByAttByBrandByTp() {
		double[][][][] tpContributions = 
				new double[nrMC][][][];
		for(int mc = 0; mc < nrMC; mc++) {
			tpContributions[mc] = 
					statistics[mc].getContributionByAttByBrandByTp();
		}
		return tpContributions;
	}
	
	public double [][][][][] getContributionBySegByAttByBrandByTp() {
		double[][][][][] tpContributions = 
				new double[nrMC][][][][];
		for(int mc = 0; mc < nrMC; mc++) {
			tpContributions[mc] = 
					statistics[mc].getContributionBySegByAttByBrandByTp();
		}
		return tpContributions;
	}
	
	public StatisticsBean[] getWoMVolumeBySegAndAttributeBeans() {
		
		if(womVolumeByAttByStepMC==null || womVolumeBySegByAttByStepMC==null) {
			initWoMStructs();
		}
		
		StatisticsBean[] beans = new StatisticsBean[nrSegments+1];
		beans[0] = new StatisticsBean(
				womVolumeByAttByStepMC[MONTE_CARLO_AVG_INDEX], 
				womVolumeByAttByStepMC[MONTE_CARLO_MIN_INDEX], 
				womVolumeByAttByStepMC[MONTE_CARLO_MAX_INDEX]
			);
		for (int i=0; i<nrSegments; i++) {
			beans[i+1] = new StatisticsBean(
					womVolumeBySegByAttByStepMC[MONTE_CARLO_AVG_INDEX][i], 
					womVolumeBySegByAttByStepMC[MONTE_CARLO_MIN_INDEX][i], 
					womVolumeBySegByAttByStepMC[MONTE_CARLO_MAX_INDEX][i]
				);
		}
		
		return beans;
	}
	
	public StatisticsBean getWoMSentimentBean() {
		if(womSentimentByBrandByStepMC==null) {
			initWoMStructs();
		}
		return new StatisticsBean(
				womSentimentByBrandByStepMC[MONTE_CARLO_AVG_INDEX], 
				womSentimentByBrandByStepMC[MONTE_CARLO_MIN_INDEX], 
				womSentimentByBrandByStepMC[MONTE_CARLO_MAX_INDEX]
			);
	}
	
	public double [][][] getWomSentimentByBrandByStep() {
		double[][][] womSentiment = 
				new double[nrMC][][];
		for(int mc = 0; mc < nrMC; mc++) {
			womSentiment[mc] = 
					statistics[mc].womSentimentByBrandByStep;
		}
		return womSentiment;
	}
	
	public void computeTimeSeriesWom(
			double[][][][] womVolumeBySegByBrandByStepMC,
			double[][][][] womVolumeBySegByAttByStepMC,
			double[][][] womSentimentByBrandByStepMC,
			double[][][][] womReachBySegByBrandByStepMC,
			double[][][] womVolumeByBrandByStepMC,
			double[][][] womVolumeByAttByStepMC,			
			double[][][] womReachByBrandByStepMC
		) {
			double[] aux = new double[nrMC];
			for(int t = 0; t < nrSteps; t++) {
				for(int b = 0; b < nrBrands; b++) {
					// WoM reports - Volume & Reach
					for(int s = 0; s < nrSegments; s++) {
						// Volume (by brand)
						for(int mc = 0; mc < nrMC; mc++) {
							aux[mc] = statistics[mc].womVolumeBySegByBrandByStep[s][b][t];		// XXX [KT] should not be int ???		
						}
						womVolumeBySegByBrandByStepMC[MONTE_CARLO_AVG_INDEX][s][b][t] = StatUtils.mean(aux);
						womVolumeBySegByBrandByStepMC[MONTE_CARLO_MIN_INDEX][s][b][t] = NumberUtils.min(aux);
						womVolumeBySegByBrandByStepMC[MONTE_CARLO_MAX_INDEX][s][b][t] = NumberUtils.max(aux);
						
						// Reach
						for(int mc = 0; mc < nrMC; mc++) {
							aux[mc] = statistics[mc].womReachBySegByBrandByStep[s][b][t];		// XXX [KT] should not be int ???		
						}					
						womReachBySegByBrandByStepMC[MONTE_CARLO_AVG_INDEX][s][b][t] = StatUtils.mean(aux);
						womReachBySegByBrandByStepMC[MONTE_CARLO_MIN_INDEX][s][b][t] = NumberUtils.min(aux);
						womReachBySegByBrandByStepMC[MONTE_CARLO_MAX_INDEX][s][b][t] = NumberUtils.max(aux);
						
						for(int mc = 0; mc < nrMC; mc++) {
							aux[mc] = statistics[mc].womVolumeBySegByBrandByStep[s][b][t];		// XXX [KT] should not be int ???		
						}
					}	// end nrBrands
					
					// WoM reports - Sentiment pos
					for(int mc = 0; mc < nrMC; mc++) {
						aux[mc] = statistics[mc].womSentimentByBrandByStep[b][t];		// XXX [KT] should not be int ???		
					}
					womSentimentByBrandByStepMC[MONTE_CARLO_AVG_INDEX][b][t] = StatUtils.mean(aux);
					womSentimentByBrandByStepMC[MONTE_CARLO_MIN_INDEX][b][t] = NumberUtils.min(aux);
					womSentimentByBrandByStepMC[MONTE_CARLO_MAX_INDEX][b][t] = NumberUtils.max(aux);
					
					// WoM reports - Volume total (by brand)
					for(int mc = 0; mc < nrMC; mc++) {
						aux[mc] = statistics[mc].womVolumeByBrandByStep[b][t];		// XXX [KT] should not be int ???		
					}
					womVolumeByBrandByStepMC[MONTE_CARLO_AVG_INDEX][b][t] = StatUtils.mean(aux);
					womVolumeByBrandByStepMC[MONTE_CARLO_MIN_INDEX][b][t] = NumberUtils.min(aux);
					womVolumeByBrandByStepMC[MONTE_CARLO_MAX_INDEX][b][t] = NumberUtils.max(aux);
					
					// WoM reports - Reach total (by brand)
					for(int mc = 0; mc < nrMC; mc++) {
						aux[mc] = statistics[mc].womReachByBrandByStep[b][t];		// XXX [KT] should not be int ???		
					}
					womReachByBrandByStepMC[MONTE_CARLO_AVG_INDEX][b][t] = StatUtils.mean(aux);
					womReachByBrandByStepMC[MONTE_CARLO_MIN_INDEX][b][t] = NumberUtils.min(aux);
					womReachByBrandByStepMC[MONTE_CARLO_MAX_INDEX][b][t] = NumberUtils.max(aux);
				}
				for(int k = 0; k < nrAttributes; k++) {
					for(int s = 0; s < nrSegments; s++) {
					// WoM reports - Volume (by att)
						for(int mc = 0; mc < nrMC; mc++) {
							aux[mc] = statistics[mc].womVolumeBySegByAttByStep[s][k][t];			
						}
						womVolumeBySegByAttByStepMC[MONTE_CARLO_AVG_INDEX][s][k][t] = StatUtils.mean(aux);
						womVolumeBySegByAttByStepMC[MONTE_CARLO_MIN_INDEX][s][k][t] = NumberUtils.min(aux);
						womVolumeBySegByAttByStepMC[MONTE_CARLO_MAX_INDEX][s][k][t] = NumberUtils.max(aux);
					}
					
					// WoM reports - Volume total (by att)
					for(int mc = 0; mc < nrMC; mc++) {
						aux[mc] = statistics[mc].womVolumeByAttByStep[k][t];		// XXX [KT] should not be int ???		
					}
					womVolumeByAttByStepMC[MONTE_CARLO_AVG_INDEX][k][t] = StatUtils.mean(aux);
					womVolumeByAttByStepMC[MONTE_CARLO_MIN_INDEX][k][t] = NumberUtils.min(aux);
					womVolumeByAttByStepMC[MONTE_CARLO_MAX_INDEX][k][t] = NumberUtils.max(aux);
				}
			}	
		}	
	
	public static int getMonteCarloArrayNrRows() {
		return MONTE_CARLO_ARRAY_NR_ROWS;
	}

	public int getNrSegments() {
		return nrSegments;
	}

	public int getNrBrands() {
		return nrBrands;
	}

	public int getNrAttributes() {
		return nrAttributes;
	}

	public int getNrSteps() {
		return nrSteps;
	}

	///////////////////////////////////////////////////////////////////////////
	// PRIVATE
	///////////////////////////////////////////////////////////////////////////
	
	public Statistics[] getStatistics() {
		return statistics;
	}

	public int getNumberOfMonteCarloRepetitions() {
		return statistics.length;
	}

	public void disableAdditionalStatistics() {
		for (Statistics s : statistics) {
			s.disableAdditionalStatistics();
		}
	}
	
	/**
	 * Computes the actual reach for every Monte-Carlo iteration.
	 * @return actual reach for every Monte-Carlo iteration.
	 */
	public double [][][][] computeReachByMcByTouchpointByBrandBySegment() {
		double [][][][] reach = new double [nrMC][][][];
		
		for (int mc = 0; mc<nrMC; mc++) {
			reach [mc] = statistics[mc].getReachByTouchpointByBrandBySegment();
		}
		
		return reach;
	}
}
