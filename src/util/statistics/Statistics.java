package util.statistics;

import org.apache.commons.math.util.MathUtils;

import model.customer.Agent;
import util.functions.ArrayFunctions;
import util.functions.Functions;
import util.functions.MatrixFunctions;

/**
 * Bean containing statistic values for sales, awareness, perceptions, etc.
 * 
 * @author imoya
 *
 */
public class Statistics {
	
	/**
	 * 100% percentage constant value.
	 */
	public static final double PERCENTAGE_VALUE=100.0;
	
	/**
	 * Considered periods of time for statistic recording.
	 * 
	 * @author imoya
	 *
	 */
	public enum TimePeriod {
		/**
		 * One day period (0 weeks).
		 */
		DAILY, 
		/**
		 * Seven days period (1 week).
		 */
		WEEKLY, 
		/**
		 * 30 days period (4 weeks).
		 */
		MONTHLY, 
		/**
		 * 91 days period (13 weeks).
		 */
		QUARTERLY, 
		/**
		 * 364 days period (52 weeks).
		 */
		YEARLY
	}

	/**
	 * Returns number of weeks for given time period.
	 * 
	 * @param period given time period for calculating number of weeks.
	 * @return number of weeks for given period.
	 */
	public static int calculateWeeksPerPeriod(TimePeriod period) {
		int periodLength;
		
		switch (period) {
		case DAILY:
			periodLength=0;
			break;
		case WEEKLY:
			periodLength=1;
			break;
		case MONTHLY:
			periodLength=4;
			break;
		case QUARTERLY:
			periodLength=13;
			break;
		case YEARLY:	
			periodLength=52;
			break;
		default:
			throw new IllegalArgumentException(
				"Unknown period " + period
			);
		}
		return periodLength;
	}
	
	/**
	 * Returns a time period value matching string description.
	 * 
	 * @param strPeriod time period description as a string.
	 * @return a time period value matching string description.
	 */
	public static TimePeriod timePeriodFromString(String strPeriod) {
		if (strPeriod.equals(TimePeriod.DAILY.toString()))     return TimePeriod.DAILY;
		if (strPeriod.equals(TimePeriod.WEEKLY.toString()))    return TimePeriod.WEEKLY;
		if (strPeriod.equals(TimePeriod.MONTHLY.toString()))    return TimePeriod.MONTHLY;
		if (strPeriod.equals(TimePeriod.QUARTERLY.toString())) return TimePeriod.QUARTERLY;
		if (strPeriod.equals(TimePeriod.YEARLY.toString()))    return TimePeriod.YEARLY;
		throw new IllegalArgumentException(strPeriod);
	}
	
	public static final boolean RECORD_WOM_REPORTS = true; 
	
	/* Time series */	

	protected int[][][] salesByBrandBySegmentByStep;
	
	protected double[][] awarenessByBrandByStep;	
	protected double[][][] perceptionsByAttByBrandByStep;
	
	private int numSegments;
	private int numBrands;
	private int numAtts; 
	
	private int numSteps;
	
	private int stepsForWeek;
	
	/* Recording modes */
	
	private boolean recordAwareness;
	private boolean recordPerceptions;
	private boolean recordContributions;

	private boolean recordWomReport;
	
	private double agentsRatio;
	
	///////////////////////////////////////////////////////////////////////////
	// XXX [KT] Optional: only if recordWomReport is active or by default??????	
	
	// By segment
	protected double[][][] womVolumeBySegByBrandByStep;	
	protected double[][][] womVolumeBySegByAttByStep;		
	protected double[][] womSentimentByBrandByStep;	
	protected double[][][] womReachBySegByBrandByStep;
	protected double[][][] womContributionBySegByBrandByTp;
	// Total
	protected double[][] womVolumeByBrandByStep;	
	protected double[][] womVolumeByAttByStep;	
	protected double[][] womReachByBrandByStep;
	
	///////////////////////////////////////////////////////////////////////////
	// Optional: only if recordSegmentDetails is active
	
	private int[] segmentSizes;
	protected double[][][] awarenessBySegByBrandByStep;	
	protected double[][][][] perceptionsBySegByAttByBrandByStep;
	
	private int numTouchPoints;
	private double[][][] contributionByAttByBrandByTp;
	private double[][][][] contributionBySegByAttByBrandByTp;
	
	private double [][][] reachByTouchpointByBrandBySegment;
	
	///////////////////////////////////////////////////////////////////////////

	public Statistics(
			int numberOfSegments,
			int numberOfBrands,
			int numberOfAttributes, 
			int numberOfSteps,
			int stepsForWeek,
			double agentsRatio) {
		
		this.numSegments=numberOfSegments;
		this.numBrands=numberOfBrands;
		this.numAtts=numberOfAttributes;
		this.numSteps=numberOfSteps;
		this.stepsForWeek=stepsForWeek;
		this.agentsRatio=agentsRatio;

		salesByBrandBySegmentByStep = 
				new int[numBrands][numSegments][numSteps];
		
		disableAdditionalStatistics();		
	}
	
	/**
	 * Disable additional data (and clean memory) 
	 */
	public void disableAdditionalStatistics() {
		recordAwareness = false;
		recordPerceptions = false;
		recordContributions = false;
		awarenessBySegByBrandByStep = null;
		perceptionsBySegByAttByBrandByStep = null;
		contributionByAttByBrandByTp = null;
		contributionBySegByAttByBrandByTp = null;
	}
	
	public void enableAdditionalStatistics(
			boolean recordAwareness, 
			boolean recordPerceptions, 
			boolean recordSegmentDetails,
			boolean recordWoMReports,
			int[] segmentSizes,
			boolean recordContributions,
			int numTouchPoints) {
		
		this.recordAwareness = recordAwareness;
		if (recordAwareness) {
			awarenessByBrandByStep = 
				new double[numBrands][numSteps];
			awarenessBySegByBrandByStep = 
					new double[numSegments][numBrands][numSteps];	
		}
		
		this.recordPerceptions = recordPerceptions;
		if (recordPerceptions) {
			perceptionsByAttByBrandByStep = 
				new double[numAtts][numBrands][numSteps];
			perceptionsBySegByAttByBrandByStep = 
					new double[numSegments][numAtts][numBrands][numSteps];
		}
		
		this.recordContributions = recordContributions;
		this.numTouchPoints = numTouchPoints;
		if (recordContributions) {
			contributionByAttByBrandByTp = 
				new double[numAtts][numBrands][numTouchPoints];
			contributionBySegByAttByBrandByTp =
					new double[numSegments][numAtts][numBrands][numTouchPoints];
		}
		
		this.segmentSizes = segmentSizes;
		
		if(recordWoMReports) {
			this.recordWomReport=true;
			// By segment
			womVolumeBySegByBrandByStep = new double[numSegments][numBrands][numSteps];
			womVolumeBySegByAttByStep = new double[numSegments][numAtts][numSteps];
			womSentimentByBrandByStep = new double[numBrands][numSteps];
			womReachBySegByBrandByStep = new double[numSegments][numBrands][numSteps];
			womContributionBySegByBrandByTp = new double[numSegments][numBrands][numTouchPoints];
			// Total
			womVolumeByBrandByStep = new double[numBrands][numSteps];
			womVolumeByAttByStep = new double[numAtts][numSteps];
			womReachByBrandByStep = new double[numBrands][numSteps];
		}
	}
	
	/**
	 * Brand sales simulated by the model (unscaled)
	 * @return Reference to brand sales (2D: Brands x Steps)
	 */
	public int[][][] referenceToSalesByBrandBySegmentByStep() {
		return salesByBrandBySegmentByStep;
	}
	
	/**
	 * Copies and scales the total sales by brand, segment and step.
	 * @return a new array containing the total sales adjusted with the 
	 * population-scale
	 */
	public int[][][] computeScaledSalesByBrandBySegmentByStep() {
		return MatrixFunctions.scaleCopyInt3dMatrix(salesByBrandBySegmentByStep,agentsRatio);
	}
	
	/**
	 * Adds the scaled sales by step and returns the result by brand and segment.
	 * @return the total sales by brand by segment after adding the sales by step.
	 */
	public int [][] computeScaledSalesByBrandBySegment() {
		int [][] sales = new int [numBrands][numSegments];
		int [][][] baseSales = computeScaledSalesByBrandBySegmentByStep();
		
		for (int b =0; b<numBrands; b++) {
			for (int s = 0; s<numSegments; s++) {
				sales[b][s] = ArrayFunctions.
						addArray(baseSales[b][s]);
			}
		}
		
		return sales;
	}
	
	/**
	 * Accumulates sales by brand for a specific period of time. 
	 * @param period Time period for summary
	 * @return Copy of scaled brand sales (two dims: brands x periods)
	 */
	public double[][] computeScaledSalesByBrandByStep(TimePeriod period) {
		
		double [][][] salesByBrandBySegment = 
				computeScaledSegmentSalesByBrandBySegmentByStep(period);
		
		int periodSteps = salesByBrandBySegment[0][0].length;
		
		double [][] salesByBrand = new double [numBrands][periodSteps];
		
		for (int b = 0; b<numBrands; b++) {
			for (int step = 0; step<periodSteps; step++) {
				for (int seg = 0; seg<numSegments; seg++) {
					salesByBrand[b][step] += salesByBrandBySegment[b][seg][step];
				}
			}
		}
		
		return salesByBrand;
	}
	
	/**
	 * Accumulates sales for the provided segment using given period and scales 
	 * them with the given ratio.
	 * @param period the period used for the sales computation.
	 * @param ratio the ratio used to scale sales.
	 * @return the accumulates sales using given period and scales them with 
	 * the given ratio.
	 */
	public double [][][] computeScaledSegmentSalesByBrandBySegmentByStep(
			TimePeriod period) {

		int [][][] baseSales = computeScaledSalesByBrandBySegmentByStep();
		
		//Daily periods are considered for backward compatibility.
		if(period==TimePeriod.WEEKLY || period==TimePeriod.DAILY) {
			return MatrixFunctions.intToDouble(baseSales);
		}
		
		// All other cases (aggregate per period)...
		int totalWeeks = numSteps / stepsForWeek;
		int weeksPerSalesPeriod = calculateWeeksPerPeriod(period);
		
		int salesLength = totalWeeks / weeksPerSalesPeriod;
		int offset = stepsForWeek * weeksPerSalesPeriod;
		
		// If the provided periodicity leaves some missing weeks, 
		// we include another step.
		if(totalWeeks%weeksPerSalesPeriod!=0) {
			salesLength++;
		}
		
		
		
		double [][][] salesByBrandBySegByStep = 
				new double [numBrands][numSegments][salesLength];
		for (int b = 0; b < numBrands; b++) {
			for (int s = 0; s < numSegments; s++) {
				for (int j = 0; j < salesLength; j++) {
					int begin = j * offset;
					int end = Math.min((j+1) * offset, numSteps);
					int total = ArrayFunctions.addArraySegment(
							baseSales[b][s], begin, end
					);
					salesByBrandBySegByStep[b][s][j] = total;
				}
			}
		}		
		
		return salesByBrandBySegByStep;
	}
	
	public void updateTimeSeries(Agent[] agents, int step) {		
		
		final int numAgents = agents.length;		
		
		if (recordContributions) {
			final double numSteps = this.numSteps;
			for(int k = 0; k < numAtts; k++) {
				for(int b = 0; b < numBrands; b++) {
					for(int t = 0; t < numTouchPoints; t++) {
						double totalSum = 0.0;
						double[] sumBySeg = null;
						sumBySeg = new double[numSegments];
						for(int a = 0; a < numAgents; a++) {
							final double contrib = 
								agents[a].getContributionNowByAttByBrandByTp(k,b,t);
							totalSum += contrib;
							
							sumBySeg[agents[a].segmentId] += contrib;
							
						}
						contributionByAttByBrandByTp[k][b][t]
							+= ((totalSum / (double) numAgents) / numSteps);
						for (int s = 0; s < numSegments; s++) {
							contributionBySegByAttByBrandByTp[s][k][b][t] 
								+= (
									(sumBySeg[s] / (double) segmentSizes[s]) 
									/ numSteps
								);
						}
					}
						
				}
			}
		}
		
		if (recordAwareness || recordPerceptions || recordWomReport) {
			
			int[] womSentimentByBrandSum = new int[numBrands];
			// Aggregate data from each agent...
			for(int a = 0; a < numAgents; a++) {
				
				if(recordAwareness) {
					final boolean[] agentAwarareness = agents[a].getAwareness();
					for(int b = 0; b < numBrands; b++) {
						if (agentAwarareness[b]) {
							awarenessByBrandByStep[b][step]++;
						}
					}
					for(int b = 0; b < numBrands; b++) {
						if (agentAwarareness[b]) {
							awarenessBySegByBrandByStep
								[agents[a].segmentId][b][step]++;
						}
					}
					
				}
				
				if(recordPerceptions) {
					final double[][] agentPerceptions = agents[a].getPerceptions();
					for(int b = 0; b < numBrands; b++) {
						for(int k = 0; k < numAtts; k++) {
							perceptionsByAttByBrandByStep
								[k][b][step] 
								+= agentPerceptions[b][k];				
						}					
					}
					for(int b = 0; b < numBrands; b++) {
						for(int k = 0; k < numAtts; k++) {
							perceptionsBySegByAttByBrandByStep
								[agents[a].segmentId][k][b][step]
								+= agentPerceptions[b][k];	
						}
					}
				}
				
				if(recordWomReport) {
					// XXX [KT] if (recordWomReport) ???
					int segmentId = agents[a].segmentId;
					int[] agentWomVolumeByBrand = agents[a].getWomVolumeByBrand();
					int[] agentWomVolumeByAtt = agents[a].getWomVolumeByAtt();
					int[] agentWomSentimentPos = agents[a].getWomSentimentPos();
					int[] agentWomSentimentNeg = agents[a].getWomSentimentNeg();
					boolean[] agentWomReachByBrand = agents[a].getWomReachByBrand();
					double[][] womContribution = agents[a].getWomContributionByBrandByTp();
					
					for(int b = 0; b < numBrands; b++) {
						// By segment
						womVolumeBySegByBrandByStep[segmentId][b][step] += (double) agentWomVolumeByBrand[b] * agentsRatio;
						womSentimentByBrandByStep[b][step] += (double) agentWomSentimentPos[b];
						womSentimentByBrandByStep[b][step] -= (double) agentWomSentimentNeg[b];
						womSentimentByBrandSum[b] += (double) agentWomSentimentPos[b];
						womSentimentByBrandSum[b] += (double) agentWomSentimentNeg[b];
						if(agentWomReachByBrand[b] == true) {
							womReachBySegByBrandByStep[segmentId][b][step]+= agentsRatio;
						}
						// Total
						womVolumeByBrandByStep[b][step] += (double) agentWomVolumeByBrand[b] * agentsRatio;
						if(agentWomReachByBrand[b] == true) {
							womReachByBrandByStep[b][step]+= agentsRatio;
						}
						//Contribution
						for (int tp=0; tp<womContribution[b].length; tp++) {
							womContributionBySegByBrandByTp[segmentId][b][tp]+= (womContribution[b][tp] / numAgents);
						}
					}
					for(int at = 0; at < numAtts; at++) {
						// By segment
						womVolumeBySegByAttByStep[segmentId][at][step] += (double) agentWomVolumeByAtt[at] * agentsRatio;
						// Total
						womVolumeByAttByStep[at][step] += (double) agentWomVolumeByAtt[at] * agentsRatio;
					}
					// Clean WoM report arrays!!!
					agents[a].cleanWomReportArrays();
				}
			}
			if(recordWomReport) {
				// Compute Reach in the current step
				for(int b = 0; b < numBrands; b++) {
					if(womSentimentByBrandSum[b] != 0) {
						womSentimentByBrandByStep[b][step] /= (double) womSentimentByBrandSum[b];
						womSentimentByBrandByStep[b][step] *= PERCENTAGE_VALUE;
						womSentimentByBrandSum[b] = 0;					
					}
				}
			}
			
			// ...and then compute totals
			
			if(recordAwareness) {
				for(int b = 0; b < numBrands; b++) {
					awarenessByBrandByStep[b][step] /= (double) numAgents;
				}
				for (int seg = 0; seg < numSegments; seg++) {
					for(int b = 0; b < numBrands; b++) {
						awarenessBySegByBrandByStep
							[seg][b][step] 
							/= (double) segmentSizes[seg];
					}
				}
			}
			
			if(recordPerceptions) {
				for (int k = 0; k < numAtts; k++) {
					for(int b = 0; b < numBrands; b++) {
						perceptionsByAttByBrandByStep
							[k][b][step]
							/= (double) numAgents;
					}
				}

				for (int seg = 0; seg < numSegments; seg++) {
					for (int k = 0; k < numAtts; k++) {
						for(int b = 0; b < numBrands; b++) {
							perceptionsBySegByAttByBrandByStep
								[seg][k][b][step] 
								/= (double) segmentSizes[seg];
						}
					}
				}
			}
			
		} // if (recordAwareness || recordPerceptions)
	}

	public double[][] getAwarenessByBrandByStep() {
		return awarenessByBrandByStep;
	}
	
	/**
	 * Computes the total awareness evolution by brand and step using the 
	 * provided period offset. 
	 * 
	 * @param period the desired periodicity for the awareness evolution.
	 * @return the computed awareness evolution by brand and step using the 
	 * provided period offset. 
	 */
	public double[][] computeAwarenessByBrandByStep(TimePeriod period) {
		
		if(period==TimePeriod.WEEKLY || period==TimePeriod.DAILY) {
			return awarenessByBrandByStep;
		}
		// All other cases (aggregate per period)...
		int totalWeeks = numSteps / stepsForWeek;
		int weeksPerPeriod = calculateWeeksPerPeriod(period);

		int awarenessLength = totalWeeks / weeksPerPeriod;
		int offset = stepsForWeek * weeksPerPeriod;
		
		// If the provided periodicity leaves some missing weeks, 
		// we include another step.
		if(totalWeeks%weeksPerPeriod!=0) {
			awarenessLength++;
		}
		
		double[][] awareness = new double[numBrands][awarenessLength];
		for (int i = 0; i < numBrands; i++) {
			for (int j = 0; j < awarenessLength; j++) {
				int end = Math.min((j+1) * offset, numSteps-1);
				awareness[i][j] = awarenessByBrandByStep[i][end];
			}
		}
		return awareness;
	}
	
	/**
	 * Computes the brand awareness evolution by segment and step using the 
	 * provided period offset.
	 * 
	 * @param period the desired periodicity for the awareness evolution.
	 * @return the computed awareness evolution by brand, segment and step 
	 * using the provided period offset. 
	 */
	public double[][][] computeAwarenesssBySegmentByBrandByStep(TimePeriod period) {
		if(period==TimePeriod.WEEKLY || period==TimePeriod.DAILY) {
			return awarenessBySegByBrandByStep;
		}
		
		// All other cases (aggregate per period)...
		int totalWeeks = numSteps / stepsForWeek;
		int weeksPerPeriod = calculateWeeksPerPeriod(period);

		int awarenessLength = totalWeeks / weeksPerPeriod;
		int offset = stepsForWeek * weeksPerPeriod;
		
		// If the provided periodicity leaves some missing weeks, 
		// we include another step.
		if(totalWeeks%weeksPerPeriod!=0) {
			awarenessLength++;
		}
		
		double[][][] awareness = new double[numSegments][numBrands][awarenessLength];
		for (int b = 0; b < numBrands; b++) {
			for (int s = 0; s < numSegments; s++) {			
				for (int step = 0; step < awarenessLength; step++) {
					int end = Math.min((step+1) * offset, numSteps-1);
					awareness[s][b][step] = awarenessBySegByBrandByStep[s][b][end];
				}
			}
		}
		return awareness;
	}

	public double[][][] getPerceptionsByAttByBrandByStep() {
		return perceptionsByAttByBrandByStep;
	}
	
	public double [][] computeAveragedPerceptionsByBrandByStep() {
		double [][] averagedPerceptions = new double [numBrands][numSteps];
		
		for (int step=0; step<numSteps; step++) {
			for (int b=0; b<numBrands; b++) {
				for (int a=0; a<numAtts; a++) {
					averagedPerceptions[b][step]+=perceptionsByAttByBrandByStep[a][b][step];
				}
				averagedPerceptions[b][step]/=numAtts;
			}
		}
		
		return averagedPerceptions;
	}
	
	/**
	 * Returns perceptions values by attribute, brand and steps according to given
	 * time period.
	 * @param period the period of time to use as offset of the data
	 * @return perceptions values by attribute, brand and steps according to given
	 * time period
	 */
	public double [][][] computePerceptionsByAttByBrandByStep(TimePeriod period) {
		if(period==TimePeriod.WEEKLY || period==TimePeriod.DAILY) {
			return perceptionsByAttByBrandByStep;
		}
		
		// All other cases (aggregate per period)...
		int totalWeeks = numSteps / stepsForWeek;
		int weeksPerPeriod = calculateWeeksPerPeriod(period);

		int perceptionsLength = totalWeeks / weeksPerPeriod;
		int offset = stepsForWeek * weeksPerPeriod;
		
		// If the provided periodicity leaves some missing weeks, 
		// we include another step.
		if(totalWeeks%weeksPerPeriod!=0) {
			perceptionsLength++;
		}
		
		double[][][] perceptions = new double[numAtts][numBrands][perceptionsLength];
		for (int a = 0; a < numAtts; a++) {
			for (int i = 0; i < numBrands; i++) {
				for (int j = 0; j < perceptionsLength; j++) {
					int end = Math.min((j+1) * offset, numSteps-1);
					perceptions[a][i][j] = perceptionsByAttByBrandByStep[a][i][end];
				}
			}
		}
		return perceptions;
	}
	
	/**
	 * Computes the average attribute perception evolution by brand and step. 
	 * The data is formatted using the given time period.
	 * @param period the periodicity for the computed values
	 * @return the computed average attribute perception evolution by brand 
	 * and step using given periodicity 
	 */
	public double [][] computeAveragedPerceptionsByBrandByStep(TimePeriod period) {
		
		double [][] averagedPerceptions = computeAveragedPerceptionsByBrandByStep();
		
		if(period==TimePeriod.WEEKLY || period==TimePeriod.DAILY) {
			return averagedPerceptions;
		}
		
		// All other cases (aggregate per period)...
		int totalWeeks = numSteps / stepsForWeek;
		int weeksPerPeriod = calculateWeeksPerPeriod(period);

		int perceptionsLength = totalWeeks / weeksPerPeriod;
		int offset = stepsForWeek * weeksPerPeriod;
		
		// If the provided periodicity leaves some missing weeks, 
		// we include another step.
		if(totalWeeks%weeksPerPeriod!=0) {
			perceptionsLength++;
		}
		
		double[][] perceptions = new double[numBrands][perceptionsLength];
		for (int b = 0; b < numBrands; b++) {
			for (int step = 0; step < perceptionsLength; step++) {
				int end = Math.min((step+1) * offset, numSteps-1);
				perceptions[b][step] = averagedPerceptions[b][end];
			}
		}

		return perceptions;
	}
	
	public double[][][][] computePerceptionsBySegByAttByBrandByStep(TimePeriod period) {
		if(period==TimePeriod.WEEKLY || period==TimePeriod.DAILY) {
			return perceptionsBySegByAttByBrandByStep;
		}
		
		// All other cases (aggregate per period)...
		int totalWeeks = numSteps / stepsForWeek;
		int weeksPerPeriod = calculateWeeksPerPeriod(period);

		int perceptionsLength = totalWeeks / weeksPerPeriod;
		int offset = stepsForWeek * weeksPerPeriod;
		
		// If the provided periodicity leaves some missing weeks, 
		// we include another step.
		if(totalWeeks%weeksPerPeriod!=0) {
			perceptionsLength++;
		}
		
		double[][][][] perceptions = new double[numSegments][numAtts][numBrands][perceptionsLength];
		
		for (int s = 0; s < numSegments; s++) {
			for (int a = 0; a < numAtts; a++) {
				for (int b = 0; b < numBrands; b++) {
					for (int step = 0; step < perceptionsLength; step++) {
						int end = Math.min((step+1) * offset, numSteps-1);
						perceptions[s][a][b][step] 
								= perceptionsBySegByAttByBrandByStep[s][a][b][end];
					}
				}
			}
		}

		return perceptions;
	}
	
	public double[][][][] getPerceptionsBySegByAttByBrandByStep() {
		return perceptionsBySegByAttByBrandByStep;
	}
	
	public double[][][] getContributionByAttByBrandByTp() {
		return contributionByAttByBrandByTp;
	}
	
	public double[][][][] getContributionBySegByAttByBrandByTp() {
		return contributionBySegByAttByBrandByTp;
	}
	
	public double[][] getWomNormalizedContributionByBrandByTp() {
		double[][] normalizedContribution = new double [numBrands][];
		
		for (int b=0; b<numBrands; b++) {
			int numTp = womContributionBySegByBrandByTp[0][b].length;
			normalizedContribution[b] = new double [numTp];
			for (int tp=0; tp<numTp; tp++) {
				for (int s=0; s<numSegments; s++) {
					normalizedContribution[b][tp]
							+=womContributionBySegByBrandByTp[s][b][tp];
				}
			}
			try {
				normalizedContribution[b] = MathUtils.normalizeArray(
					normalizedContribution[b],
					Functions.TOTAL_AMOUNT_NORMALIZABLE_VALUE
						);
			} catch (ArithmeticException e) {
				/*
				 * Exceptions could be thrown, but normalizing with no 
				 * contribution could be a regular situation.
				 */
				normalizedContribution[b] = new double [numTp];
			}
		}
		return normalizedContribution;
	}
	
	public double[][][] getWomNormalizedContributionBySegByBrandByTp() {
		double[][][] normalizedContribution = new double [numSegments][numBrands][];
		for (int s=0; s<numSegments; s++) {
			for (int b=0; b<numBrands; b++) {
				try {
					normalizedContribution[s][b] = MathUtils.normalizeArray(
						womContributionBySegByBrandByTp[s][b],
						Functions.TOTAL_AMOUNT_NORMALIZABLE_VALUE
							);
				} catch (ArithmeticException e) {
					normalizedContribution[s][b] = new double 
							[womContributionBySegByBrandByTp[s][b].length];
				}
			}
		}
		return normalizedContribution;
	}
	
	public double[][] getWomReachByBrandByStep() {
		return womReachByBrandByStep;
	}
	
	public double[][][] getWomReachBySegmentByBrandByStep() {
		return womReachBySegByBrandByStep;
	}
	
	public double[][] getWomSentimentByBrandByStep() {
		return womSentimentByBrandByStep;
	}
	
	public double[][] getWomVolumenByBrandByStep() {
		return womVolumeByBrandByStep;
	}
	
	public double[][][] getWomVolumenBySegByBrandByStep() {
		return womVolumeBySegByBrandByStep;
	}
	
	public double[][] getWomVolumenByAttByStep() {
		return womVolumeByAttByStep;
	}
	
	public double[][][] getWomVolumenBySegByAttByStep() {
		return womVolumeBySegByAttByStep;
	}
	
	public double[][][] getWomContributionBySegByBrandByTp() {
		return womContributionBySegByBrandByTp;
	}
	
	public int getNumberOfTouchpoints() {
		return numTouchPoints;
	}
	
	public int getNumberOfSegments() {
		return numSegments;
	}
	
	public int getNumberOfAttributes() {
		return numAtts;
	}

	public double[][][] getReachByTouchpointByBrandBySegment() {
		return reachByTouchpointByBrandBySegment;
	}

	public void setReachByTouchpointByBrandBySegment(double[][][] reachByTouchpointByBrandBySegment) {
		this.reachByTouchpointByBrandBySegment = reachByTouchpointByBrandBySegment;
	}
}
