package model.sales;

import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.math.util.MathUtils;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import model.customer.Agent;
import model.decisionmaking.DecisionMaking;
import util.exception.sales.NoCandidatesException;
import util.exception.sales.SalesScheduleError;
import util.functions.Functions;
import util.random.Randomizer;
import util.statistics.Statistics;

/**
 * Schedules the purchases contained in the simulation from the market
 * sales, represented through time using seasonal values. Along with
 * the sales, a ratio agents / population is used in order to scale the
 * sales that shall appear into the simulation.
 *  
 * @author imoya
 *
 */
public class SalesScheduler {
	
	/**
	 * Stores the id of the agents able to purchase for each segment.
	 */
	public TIntArrayList[] enabled;
	
	/**
	 * Real population / Agent population ratio.
	 */
	private double ratio;
	
	/**
	 * Sales carried out from previous steps.
	 */
	private double carryOverSales;
	
	/**
	 * Market share percent for every segment. 
	 */
	private double [] maxMarketPercentBySegment;
	
	/**
	 * Provided seasonality. Those are the sales to be scheduled.
	 */
	private double[] seasonality;
	
	/**
	 * Product availability for every brand at any step. It can be zero.
	 */
	private double[][] availabilityByStep;
	
	/**
	 * Matrix containing the id of the agents coming back at the 
	 * candidates pool.
	 * 
	 * disabledUntil[i][j] = k
	 * 
	 * That means that at the i-th step, the agent with id k is
	 * being activated again.
	 */
	private int[][] disabledUntil;	

	/**
	 * If no agent was selected for buying, the algorithm returns
	 * this INVALID_CLIENT id.
	 */
	private final int INVALID_CLIENT = -1;
	
	/**
	 * Enable/disable debug/test mode.
	 */
	private boolean test;
	
	/**
	 * Records id for the agent that performed at least one purchase 
	 * at any step. Identifiers are only collected when in debug mode.
	 */
	private BitSet[] salesHistoryRecord;
	
	/**
	 * Number of steps in which the agent is not able to purchase.
	 */
	private int decisionCycle;
	
	/**
	 * Number of steps separating milestones. Sales dispatching will 
	 * be checked every this number of steps.
	 */
	private int checkpointInSteps;
	
	/**
	 * Current agent population as an array object.
	 */
	private Agent[] consumers;	

	/**
	 * Creates a SalesScheduler instance and initializes the candidate pool.
	 * 
	 * @param seasonality - provided seasonality.
	 * @param availability - provided availability of brands by step.
	 * @param checkpoint - number of steps separating every milestone.
	 * @param marketPercentBySegment - market share for every segment.
	 * @param decisionCycle - decision cycle duration in steps.
	 * @param numberOfSteps - total number of simulation steps.
	 * @param ratio - Real population / Agent population ratio.
	 * @param consumers - Current agent population as an array object.
	 */
	public SalesScheduler(
			double [] seasonality,
			double [][] availability,
			int checkpoint,
			double[] marketPercentBySegment,
			int decisionCycle,
			int numberOfSteps,
			double ratio, 
			Agent[] consumers
		) {
		this.seasonality = seasonality;
		this.ratio=ratio;
		
		availabilityByStep = availability;
		
		maxMarketPercentBySegment = marketPercentBySegment;
		
		this.decisionCycle = decisionCycle;

		enabled = new TIntArrayList[marketPercentBySegment.length];
		
		this.consumers = consumers;
		
		disabledUntil = new int[numberOfSteps][];
		
		checkpointInSteps=checkpoint;
		
		test=false;
		
		prepareAgents();
	}

	/**
	 * Prepare the initial pool of agents, that consist on a map filled
	 * with lists of agents. This way, agents are separated by segments.
	 */
	private void prepareAgents() {		
		for (int i=0; i<maxMarketPercentBySegment.length; i++) {
			enabled[i]= new TIntArrayList();
		}
		
		for (Agent agent: consumers) {
			if(agent.getAwarenessCount()>0) {
				enabled[agent.segmentId].add(agent.clientId);
			}			
		}
	}

	/**
	 * At the end of the step, the agents that have been chosen for
	 * purchasing are disabled while they are going through their
	 * decision cycle. 
	 * 
	 * Also, agents finishing their decision cycle become available
	 * again and are re-introduced in the pool of enabled agents. 
	 * 
	 * @param step - simulation step that is ending.
	 * @param disabled - The disabled agents that are about to go
	 * through their decision cycle. 
	 */
	private void endStep(int step, TIntHashSet disabled) {		
		//Exclude agents while they are in cool down.
		int comeback = step + decisionCycle;
		
		int[] disabledArray = disabled.toArray();
		
		if(comeback < disabledUntil.length) {	
			disabledUntil[comeback]=disabledArray;
		}
		//Purchasing agents begin their decision cycle.
		for (int disabledId: disabledArray) {
			Agent  agentBuying = consumers[disabledId];
			
			agentBuying.beginDecisionCycle();
			//They are also removed from the candidate list.
			enabled[agentBuying.segmentId].remove(disabledId);				
		}
		
		//Bring back agents that finished their cool down
		if(step<disabledUntil.length 
				&& disabledUntil[step]!=null
			) {
			for (int i = 0; i < disabledUntil[step].length; i++) {
				Agent c = consumers[disabledUntil[step][i]];
				if(c.getAwarenessCount()>0) {
					enabled[c.segmentId].add(c.clientId);
					c.endDecisionCycle();
				}				
			}
		}
	}
	
	/**
	 * Randomly chooses a buyer using segment shares.
	 * 
	 * @param random - The randomizer used by the current simulation.
	 * @throws SalesScheduleError 
	 */
	private int assignSale(
			Randomizer random
		) {
		double roll=random.nextDouble();
		
		//Create the segment roulette using segments with candidates.
		int numSegments = maxMarketPercentBySegment.length;
		TIntArrayList segmentsWithCandidates = new TIntArrayList(numSegments);
		
		boolean anyEmpty = false;
		int emptySegments = 0;
		
		TDoubleArrayList marketShareForSegmentsWithCandidates 
				= new TDoubleArrayList(numSegments);
		
		for (int s = 0; s<numSegments; s++) {
			if(!enabled[s].isEmpty()) {
				segmentsWithCandidates.add(s);
				marketShareForSegmentsWithCandidates.add(
						maxMarketPercentBySegment[s]);
			} else {
				anyEmpty = true;
				emptySegments++;
			}
		}
		
		if(emptySegments == numSegments) {
			return INVALID_CLIENT;
		}
		
		int segment=INVALID_CLIENT;
		
		if(anyEmpty) {
			double[] normalizedShared = MathUtils.normalizeArray(
					marketShareForSegmentsWithCandidates.toArray(),
						Functions.IDENTITY_SCALE);
			int index= Functions.simpleRouletteSelection(normalizedShared, roll);
			segment = segmentsWithCandidates.toArray()[index];
		} else {
			segment = Functions.simpleRouletteSelection(maxMarketPercentBySegment, roll);
		}
		
		if(segment==INVALID_CLIENT) {
			return INVALID_CLIENT;
		}
		
		//Check enabled agents from that segment
		TIntArrayList candidates = enabled[segment];
		
		//Chose a candidate randomly
		int randomIndex = random.nextInt(candidates.size());
		int clientid = candidates.getQuick(randomIndex);

		return clientid;
	}
	
	/**
	 * Generates availabilities for every brand at given step.
	 * 
	 * This availability will only be used by one purchasing agent.
	 * 
	 * @param random - randomizer instance used at current simulation.
	 * @param step - current simulation step.
	 * @return brand availability at given step for selected agent.
	 */
	private boolean[] checkAvailability(Randomizer random, int step) {
		int numBrands = availabilityByStep.length;
		boolean [] availableBrands = new boolean [numBrands];
		
		for (int i=0; i<numBrands; i++) {
			if(random.nextDouble() < availabilityByStep[i][step]) {
				availableBrands[i] = true;
			}
		}
		
		return availableBrands;
	}
	
	/**
	 * Calculates maximum number of sales assignment attempts as the number 
	 * of agents enabled.
	 * @return the maximum number of sales assignment attempts.
	 */
	private int calculateNumberOfAttempts() {
		int attempts = 0;
		
		for (TIntArrayList list : enabled) {
			attempts+=list.size();
		}
		
		return attempts;
	}
	
	/**
	 * Analyzes the possible causes behind the scheduling error.
	 * @throws SalesScheduleError the resulting scheduling exception.
	 */
	private void analyzeError(String baseError, int step) throws SalesScheduleError {
		//Check candidate list state
		boolean empty = true;
		for (TIntArrayList list : enabled) {
			empty &= list.isEmpty();
		}
		
		if (empty) {
			String message = String.format("Empty candidate list. "
					+ "Buying decision cycle value: %d. "
					+ "Also, awareness decay could be too high.\n",decisionCycle);
			throw new NoCandidatesException(baseError.concat(message));
		}
		
		/*If the candidate list is not empty, the error may be involved with
		 * either awareness or availability values.
		 */
		int brands = availabilityByStep.length;
		double[] availabilityValues = new double [brands];
		
		//Availability is gathered by brand.
		for (int b=0; b<brands; b++) {
			availabilityValues[b]=availabilityByStep[b][step];
		}
		
		double[] awarenessValues = new double [brands];
		int totalCandidates = 0;
		
		//Awareness is gathered as averaged values by candidate for each brand.
		for (TIntArrayList list : enabled) {
			totalCandidates += list.size();
			int[] candidates=list.toArray();
			for (int c : candidates) {
				Agent agent = consumers[c];
				for (int b=0; b<brands; b++) {
					if(agent.getAwarenessOfBrand(b)) {
						awarenessValues[b]++;
					}
				}
			}
		}
		
		if (totalCandidates>0) {
			//Average awareness values by number of candidates
			for (int b=0; b<brands; b++) {
				awarenessValues[b]/=(double)totalCandidates;
			}
		}
		
		String message = String.format("Awareness may be too low, or maybe the "
				+ "availability is too low.\n Average awareness values by brand: %s.\n"
				+ "Availability values for this step: %s.\n Agents decision cycle "
				+ "may be too high: %d. \n Additional info: \n Total buying agents: %d.\n"
				+ "Total number of agents: %d.\n", 
					Arrays.toString(awarenessValues), 
					Arrays.toString(availabilityValues), 
					decisionCycle,
					totalCandidates, consumers.length);
		
		throw new SalesScheduleError(baseError.concat(message));
	}
	
	/**
	 * Assign sales for the given step.
	 * 
	 * @param step current simulation step
	 * @param random current simulation randomizer
	 * @param statistics simulation statistics object
	 * @throws SalesScheduleError if the sales of a step can't be assigned,
	 * an exception is thrown.
	 */
	public void assignSales(
			int step,
			Randomizer random,
			Statistics statistics,
			DecisionMaking dm
			) throws SalesScheduleError
		{
		//Add current step sales to accumulated sales
		carryOverSales+=seasonality[step];
		
		//If some sale fails to be assigned, it will be tried again on
		//next steps.
		boolean skip = false;
		TIntHashSet disabled = new TIntHashSet();
		
		//Local reference (to avoid using public data in Statistics)
		int[][][] salesByBrandBySegmentByStep = 
			statistics.referenceToSalesByBrandBySegmentByStep();
		
		//Sale attempts
		int attempts = calculateNumberOfAttempts();
		
		//Assign sales until sales amount is below ratio
		while(
				!skip &&
				(carryOverSales>=ratio
					//Sales could
					|| carryOverSales>=(ratio*0.5))
			) {
			int buyer=assignSale(random);
			
			if(buyer!=INVALID_CLIENT) {
				Agent customerBuying = consumers[buyer];
				
				boolean [] availability = checkAvailability(random, step);				
				boolean [] awareness = customerBuying.getAwareness();
				
				boolean [] filteredAwareness = Functions.and(availability,awareness);
				
				boolean anyBrandAvailable = BooleanUtils.or(filteredAwareness);
				
				if(anyBrandAvailable) {
					
					int brand = customerBuying.buyOneBrand(dm, step, filteredAwareness);
					
					if (test && !filteredAwareness[brand]) {
						String message="Availability failed for brand "+brand+" at step "+step
								+"\n Availability:"+Arrays.toString(availability)
								+"\n Awareness: "+Arrays.toString(awareness);
						throw new SalesScheduleError(message);
					}
					
					/*
					 * If purchase success, sale is noted at statistics object.
					 */	
					int segment = customerBuying.segmentId;
					
					///////////////////////////////////////////////////////////////
					salesByBrandBySegmentByStep[brand][segment][step] ++;
					///////////////////////////////////////////////////////////////
					
					/*
					 * Consumer is marked as buyer and the amount of sales
					 * left is reduced.
					 */
					disabled.add(buyer);
					carryOverSales-=ratio;
					
					if(test) {
						/*
						 * If test mode is enabled, consumer id is noted.
						 */
						if(salesHistoryRecord[step]==null) {
							salesHistoryRecord[step] = new BitSet();
						}
						salesHistoryRecord[step].set(customerBuying.clientId);
					}
				} else {
					attempts--;
					if(attempts==0) {
						skip = true;
					}
				}
			} else {
				skip=true;
			}			
		}
		
		//Check checkpoint Period
		if( step>0 
				&& step % checkpointInSteps == 0
					&& carryOverSales > (ratio*0.5)
			) {
			String baseMessage = String.format("Sales scheduled for checkpoint at "
					+ "step %d failed to be accurate (carry over > (ratio*0.5)).\n", 
							step);
			analyzeError(baseMessage, step);
		}
		
		//Prepare next step
		endStep(step, disabled);
	}
	
	/**
	 * Returns the double array containing the provided market share.
	 * 
	 * @return current market share
	 */
	public double[] getMaxMarketShareBySegment() {
		return this.maxMarketPercentBySegment;
	}

	/**
	 * Returns decision cycle value in steps.
	 * 
	 * @return decision cycle value in steps.
	 */
	public int getDecisionCycle() {
		return decisionCycle;
	}
	
	/**
	 * Returns the sales that could not be assigned. Those purchases 
	 * represent the remainder after transform the total amount of 
	 * sales with the population – agent ratio.
	 * 
	 * @return gross sales that could not be assigned.
	 */
	public double getCarryOverSales() {
		return carryOverSales;
	}
	
	/**
	 * Enable debug/test mode.
	 */
	public void enableTest() {
		test=true;
		salesHistoryRecord= new BitSet[disabledUntil.length];
	}
	
	/**
	 * Get buying history sales for debug mode.
	 * @return the sales record for every step
	 */
	public BitSet[] getSalesHistoryRecord() {
		return salesHistoryRecord;
	}
}
