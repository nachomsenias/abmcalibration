package model.touchpoints;

import java.util.Arrays;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import model.ClientSegments;
import model.touchpoints.TouchPointOwned.InvestmentType;
import util.functions.MatrixFunctions;
import util.random.Randomizer;

/**
 * TouchPointScheduler implements the scheduling algorithm design for
 * paid touch points.
 * 
 * Its main functionality is to create the planning and provide some
 * debug getters that may be used for ensuring that the scheduling is
 * generated properly.
 * 
 * @author imoya
 *
 */
public class TouchPointScheduler {

	/**
	 * Debug flag is only enabled when the registry runs in debug mode.
	 */
	private boolean debug;

	/**
	 * Number of agents at the simulation.
	 */
	private int numAgents;
	
	/**
	 * Number of weeks for the simulation.
	 */
	private int weeks;
	
	/**
	 * Number of segments at the simulation.
	 */
	private final int numSegments;

	/**
	 * Percentage of total agent population for every segment.
	 */
	private double[] segmentSizes;

	/**
	 * Weekly reach maximum by this touch point for each segment.
	 */
	private double[] wrm;
	
	/**
	 * Annual reach maximum by this touch point for each segment.
	 */
	private double[] arm;
	
	/**
	 * Annual reach speed by this touch point for each segment.
	 */
	private double[] arms;
	
	/**
	 * Investment to be scheduled.
	 */
	private double[][] marketingPlan;
	
	/**
	 * Investment metric used by the touch point being scheduled.
	 */
	private InvestmentType investmentType;
	
	/**
	 * The population-agent factor. It should be below 1.0 (agent/population).
	 */
	private double populationFactor;
	
	/**
	 * ClientSegments instance describing every segment.
	 */
	private ClientSegments segments;

	/**
	 * Because maximum reach will evolve from the weekly value to 
	 * the annual one based on its speed, this variable stores the 
	 * current annual reach maximum.
	 */
	private double[] actualRM;
	
	/**
	 * Stores the evolution of actualRM by step. This value is only 
	 * recorded in debug mode.
	 */
	private double[][] actualRMByStep;
	
	/**
	 * Evolution of annual reach maximum for every segment given the 
	 * GRP investment.
	 */
	private double[][] carm;
	
	/**
	 * Stores agent-segment relationships when in debug mode:
	 * if agentASegment[i] = j; agent i belongs to segment j.
	 */
	private byte[] agentAtSegment;
	
	/**
	 * Id of agents reached by the touch point, split by segment. 
	 */
	private TIntArrayList [] agentsReached;
	
	/**
	 * Number of hits per agent. 
	 */
	private int [] hitsPerAgent;
	/**
	 * Percentage of increment of reach values when hitting a new agent,
	 * stored by segments.
	 */
	private double [] reachIncrementBySegment;
	
	/**
	 * Simulation step for each week into the simulation.
	 */
	private int stepsForWeek;

	/*
	 * The schedule will be returned as a matrix of pairs, those being:
	 * [AgentID][Step] = Hits (Hits > 0)
	 * 
	 * [AgentID][Step] = null if the agent will receive no hit that day.
	 */
	private byte[][] schedule;
	
	/**
	 * Original schedule by week (before translating by steps for week).
	 * 
	 * Only stored when in debug mode.
	 */
	private byte[][] debugSchedule;

	//#####################
	//CONSTRUCTOR
	//#####################
	/**
	 * Constructors the object representing the Scheduler.
	 * 
	 * @param segments - ClientSegments bean class used in the simulation.
	 * @param numAgents - Total agent population.
	 * @param wrm - An array representing the Weekly Reach Maximum for each segment as a double.
	 * @param arm - An array representing the Annual Reach Maximum for each segment as a double.
	 * @param arms - An array representing the Annual Reach Maximum Speed, 
	 * that measures how fast the reach grows as GRP are pushed until achieving the ARM value.
	 * @param weeks - Simulation length in weeks.
	 * @param stepsForWeek - Number of steps by each simulation week.
	 */
	public TouchPointScheduler(
			ClientSegments segments,
			int numAgents,
			double[] wrm,
			double[] arm, 
			double[] arms,
			int weeks,
			int stepsForWeek,
			InvestmentType investmentType,
			double agentPopulationFactor,
			boolean debug
			) {
		
		super();
		this.numAgents = numAgents;
		this.weeks = weeks;
		segmentSizes = segments.getSegmentSizes();
		this.wrm = wrm;
		this.arm = arm;
		this.arms=arms;
		
		this.investmentType=investmentType;
		populationFactor = agentPopulationFactor;
		
		marketingPlan= null;

		this.stepsForWeek=stepsForWeek;
		
		numSegments = segments.getNumSegments();
		
		if(debug) {
			this.debug = debug;
			agentAtSegment = new byte [numAgents];
			actualRMByStep = new double [numSegments][weeks];
		}	

		agentsReached = new TIntArrayList[numSegments];
		
		this.segments=segments;
		
		
		reachIncrementBySegment = new double [numSegments];
		for (int i=0; i<numSegments; i++) {
			int segmentTotal = (int)(segmentSizes[i] * numAgents);
			reachIncrementBySegment[i] = 1/(double) segmentTotal;
			
			agentsReached[i] = new TIntArrayList();
		}
		
		/*
		 * Actual Reach Maximum is used for controlling the evolution
		 * of touch point's reach. 
		 * 
		 * It will contain the actual value of the reach for every
		 * segment. 
		 */
		actualRM = new double[numSegments];
		
		/*
		 * The Current Reach Maximum is used as a reference of how reach
		 * evolves through the weeks as GRP are deployed.
		 */
		carm = new double[numSegments][weeks];
		
		/*
		 * The number of hits per agent is used to know if an agent
		 * has been hit before or not.
		 */
		hitsPerAgent = new int[numAgents];
	}
	
	/**
	 * Resets inner marketing values in order to make the scheduler 
	 * reusable for every brand advertising through the touch point.
	 */
	private void resetMarketingDependantValues() {
		//Reset depending values
		Arrays.fill(actualRM,0.0);
		
		if(debug) {
			Arrays.fill(agentAtSegment, (byte)0);
			for (double [] rmByStep : actualRMByStep) {
				Arrays.fill(rmByStep, 0.0);
			}
		}
		
		/*
		 * The value of the Current Reach Maximum is calculated with WRM,ARM,
		 * ARMS and the marketing plan.
		 * 
		 *  WRM is used as bottom value and ARM is used as top value.
		 */
		for (int i = 0; i < numSegments; i++) {			
			carm[i][0] = wrm[i];
			double grp = marketingPlan[i][0];
			
			for (int j = 1; j < weeks; j++) {				
				grp += marketingPlan[i][j];
				double reach = arms[i] * grp;
				
				if (reach > arm[i]) {
					carm[i][j]=arm[i];
				} else if (reach > wrm[i]) {
					carm[i][j] = reach;
				} else {
					carm[i][j] = wrm[i];
				}
			}
			
			agentsReached[i].clear();
		}
		schedule = new byte[numAgents][weeks];
		hitsPerAgent = new int[numAgents];
	}
	
	
	//#####################
	//GETTERS & SETTERS
	//#####################
	
	/**
	 * Retrieve the agents hit by the touch point.
	 * @return The set containing the agents reached.
	 */
	public TIntArrayList[] getAgentsReached() {
		return agentsReached;
	}
	
	/**
	 * Returns the array storing the agent-segment relationship.
	 * @return the array storing the agent-segment relationship.
	 */
	public byte[] getAgentsAtSegment() {
		return agentAtSegment;
	}
	
	/**
	 * Returns the matrix that contains the Annual Reach Maximum 
	 * evolution with Annual Reach Maximum speed rates.
	 * 
	 * @return A matrix of double containing the ARM values for each
	 * segment at every week of the plan. 
	 */
	public double[][] getCARM() {
		return carm;
	}
	
	/**
	 * Returns a matrix with effective reach for every segment.
	 * 
	 * @return a matrix with the effective reach for 
	 * each segment at very step.
	 */
	public double[][] getActualARMByStep() {
		return actualRMByStep;
	}
	
	/**
	 * Returns the effective reach achieved for every segment.
	 * @return An array with the effective reach for each segment as a double.
	 */
	public double[] getActualRM() {
		return actualRM;
	}
	
	/**
	 * Returns the planning of GRP for every week and each segment.
	 * @return A matrix with the GRP to be scheduled.
	 */
	public double[][] getPlan() {
		return marketingPlan;
	}
	
	/**
	 * Translates investment supplied in impacts or impressions to GRPs.
	 * @param marketingPlan the investment by segment and step.
	 * @return the supplied investment translated to GRPs.
	 */
	private double[][] adaptPlan(double[][] marketingPlan) {
		double scale;
		switch (investmentType) {
		case GRP:
			return marketingPlan;
		/*
		 * If the metric used is either impacts or impressions, 
		 * the investment needs to be scaled using the agent-population ratio.
		 */
		case IMPACTS:
			scale = populationFactor/(numAgents*0.01);
			return MatrixFunctions.scaleCopyOfDoubleMatrix(marketingPlan, scale);
		case IMPRESSIONS:
			scale = (populationFactor*1000)/(numAgents*0.01);
			return MatrixFunctions.scaleCopyOfDoubleMatrix(marketingPlan, scale);
		default:
			return marketingPlan;
		}
	}
	
	/**
	 * Sets new GRP values and resets internal variables. 
	 * @param marketingPlan - GRP investment.
	 */
	public void setPlan(double[][] marketingPlan) {
		this.marketingPlan=adaptPlan(marketingPlan);
		resetMarketingDependantValues();
	}
	
	public void setInvestmentMetric(InvestmentType metric) {
		this.investmentType=metric;
	}
	
	/**
	 * Return the size specified for the segments.
	 * @return The segment sizes
	 */
	public double[] getSegmentSizes() {
		return segmentSizes;
	}
	
	/**
	 * 
	 * @return A matrix of integers, where [AgentId][D]=X means that
	 * the agent with id=AgentId will be impacted X times at the D day. 
	 */
	public byte[][] getSchedule() {
		return schedule;
	}
	
	/**
	 * Returns the original schedule before applying the steps for week
	 * transformation.
	 * 
	 * @return the original schedule before applying the steps for week
	 * transformation.
	 */
	public byte[][] getDebugSchedule() {
		return debugSchedule;
	}
	
	//#####################
	//FUNCTIONALITY
	//#####################
	
	/**
	 * Schedules the GRP investment supplied for each segment.
	 * 
	 * @param random - randomizer used during the simulation. 
	 */
	public void schedule(Randomizer random){
		int numSegments=segmentSizes.length;		
		/*
		 * Because 1 GRP by definition should affect 1% of total population once,
		 * total hits available for this GRP depends of how many agents are gathered at
		 * this one percent.
		 */
		final double onePercen = numAgents * 0.01;
		
		for (int i=0; i<numSegments;i++)
			for(int j=0; j<weeks;j++) {
				double investment = marketingPlan[i][j];
				/*
				 * If no investment is scheduled for a given week, there is nothing
				 * to schedule.
				 */
				if(investment>0.0) {
					double swrm = wrm[i];
					/*
					 * Maximum number of candidates is calculated using the
					 * week reach maximum of the segment, and the number of agents
					 * for a given segment (calculated from the segment size and
					 * the total number of agents). 				
					 */
					int maxcandidates = (int)((double)numAgents*segmentSizes[i]*swrm);

					int totalhits = (int)(onePercen * investment);
					
					/*
					 * When the number of candidates is bigger than the number of hits, the number of
					 * possible candidates needs to be reduced, because once a candidate is selected,
					 * it will be marked as hit and taken into account for the global reach of his 
					 * segment.
					 */
					if(totalhits<maxcandidates) {
						maxcandidates=totalhits;
					}
					/*
					 * Randomly select the candidates for the touch point hits, the candidate set
					 * will be between 1 and maxcandidates.
					 */
					TIntHashSet candidates = selectCandidates(
						maxcandidates, i, j, segments, random
					);
									
					/*
					 * Touch point hits are distributed randomly between the selected agents. The
					 * distribution is return as a Map<AgentID,NumberOfHits>. 
					 * 
					 * Candidates are passed as an array.
					 */
					scheduleHitsBetweenCandidates(candidates.toArray(),
							totalhits, random,j);
				}
			}
		//If debug mode is enabled, stores original scheduling.
		if(debug) {
			debugSchedule=this.schedule;
		}
		//If the simulation runs with more than one step for week, 
		//the scheduling needs to be adapted.
		if(stepsForWeek>1) {
			translateSchedule(random);
		}
	}
	
	/**
	 * Translate the schedule to fit the steps for week relationship.
	 * 
	 * @param random - randomizer used during the simulation. 
	 */
	private void translateSchedule(Randomizer random) {
		byte[][] schedule = new byte[numAgents][weeks*stepsForWeek];
		for (int agent=0; agent<numAgents; agent++) {
			for (int week =0; week<weeks; week++) {
				byte hits = this.schedule[agent][week];
				for (int hit =0; hit<hits; hit++) {
					int randomStep = random.nextInt(stepsForWeek);
					int step = week*stepsForWeek;
					schedule[agent][step+randomStep]++;
				}
			}
		}
		this.schedule=schedule;
	}

	/**
	 * Distributes the given hits between the selected candidates, scheduling
	 * it at a random day of the given week.
	 * 
	 * Every candidate will receive at least one hit.
	 * 
	 * @param candidates - The array containing the id of each candidate.
	 * @param totalhits - The hits that will be distributed between the candidates.
	 * @param randomizer - The randomizer to be used in the distribution.
	 * @param step - The week considered at this step of the simulation. 
	 */
	private void scheduleHitsBetweenCandidates(
			int[] candidates, 
			int totalhits,
			Randomizer randomizer, 
			int step) 
	{
		//Ensure every candidate is hit one time at least
		// WARNING: If this part changes, the use of "agentsReached"
		// in the getRandomCandidateFromSegment method must change as well
		for (int i=0; i<candidates.length; i++) {
			schedule[candidates[i]][step]++;
		}
		
		int hitsleft=totalhits-candidates.length;
		/*
		 * Once every agent received one hit, the rest of
		 * the hits will be distributed randomly among all
		 * the candidates.
		 * 
		 */
		for (int i=0; i<hitsleft; i++) {
			int random = randomizer.nextInt(candidates.length);
			int selection = candidates[random];
			hitsPerAgent[selection]++;

			schedule[selection][step]++;
		}
	}
	
	/**
	 * Randomly selects the candidates. A HashSet is used for ensure no candidate
	 * is repeated.
	 * 
	 * @param numCandidates - The maximum number of candidates, that will be used 
	 * as an upper bound.
	 * @param segment - The id of the segment where the candidates will belong to. 
	 * @param week - The number of the week when the selection is done. Its needed
	 * for ensuring the reach boundaries are respected.
	 * @param segments - The segments that contains the agents.
	 * @param randomizer - The randomizer to be used in the selection.
	 * @return the candidates selected gathered into a set, ensuring one agent
	 * will only appear once as a candidate.
	 */
	private TIntHashSet selectCandidates(
			int numCandidates, 
			int segment, 
			int week,
			ClientSegments segments, 
			Randomizer randomizer)
	{
		TIntHashSet candidates = new TIntHashSet(numCandidates);
		/*
		 * The selection is done repeatedly calling for a random candidate. 
		 */
		for (int i=0; i<numCandidates; i++) {
			int candidate = getRandomCandidateFromSegment(
				segment, week, segments, randomizer
			);
			candidates.add(candidate);
		}		
		
		return candidates;
	}
	
	/**
	 *  Randomly selects a valid candidate for receiving the touch point hits,
	 *  preserving the reach boundaries.
	 *  
	 * @param segment - The segment id where the selected agents will belong to.
	 * @param week - The week that its being planned. This parameter is needed to
	 * ensure reach boundaries.
	 * @param randomizer - The randomizer to be used in the selection.
	 * @return The identifier of the agent selected. 
	 */
	private int getRandomCandidateFromSegment(
			int segment, 
			int week, 
			ClientSegments segments, 
			Randomizer randomizer) 
	{
		int agentId;
		/*
		 * First, if reach boundaries are checked.
		 *
		 * If another agent hit would break restrictions, 
		 * a previously hit one is chosen. 
		 */
		if(actualRM[segment]+reachIncrementBySegment[segment]>carm[segment][week]) {
			int reached = agentsReached[segment].size();
			int random = randomizer.nextInt(reached);
			
			agentId = agentsReached[segment].getQuick(random);
		} else {
			
			//Otherwise, choose a random agent from segment
			int[] agentsAtSegment = segments.getAgentsBySegment()[segment];			
			int random = randomizer.nextInt(agentsAtSegment.length);
			
			agentId = agentsAtSegment[random];
			
			//If the agent wasn't hit before, add it.
			if(hitsPerAgent[agentId] == 0) {
				agentsReached[segment].add(agentId);
				hitsPerAgent[agentId]++;
				actualRM[segment]+=reachIncrementBySegment[segment];
			}
			
			if(debug) {
				this.agentAtSegment[agentId]=(byte)segment;
				actualRMByStep[segment][week] = actualRM[segment];
			}
		}
		
		return agentId;
	}
}
