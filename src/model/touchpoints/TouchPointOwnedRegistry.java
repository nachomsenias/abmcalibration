package model.touchpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.ClientSegments;
import model.Model;
import model.customer.Agent;
import model.touchpoints.earned.AbstractTouchPoint;
import test.ReachTestBean;
import util.functions.ArrayFunctions;
import util.functions.Functions;
import util.functions.MatrixFunctions;
import util.random.Randomizer;

/**
 * TouchPointOwnedRegistry defines a centralized access to touch points
 * and marketing plans.
 * 
 * TouchPointOwnedRegistry class originally implemented the Singleton pattern,
 * however for the parallelizable version it was modified to be a regular class.
 * 
 * @author imoya
 *
 */
public class TouchPointOwnedRegistry {
	
	/**
	 * Activates / disables debug mode.
	 */
	private boolean debug = false;
	
	/**
	 * Touch points store at the registry.
	 */
	private TouchPointOwned[] touchpoints = null;
	
	/**
	 * Touch point order of application is shuffled for every time 
	 * they are applied.
	 */
	private byte[] permutation = null;

	/**
	 * Test beans used while in debug mode.
	 */
	private ReachTestBean[][] testBeans = null;
	
	/**
	 * List of TouchPointScheduler for each brand using this touch point.
	 * 
	 * This list is only filled when debuggin mode is enabled.
	 */
	private List<TouchPointScheduler> schedulers = null;
	
	
	/**
	 * Randomizer used during current simulation.
	 */
	private Randomizer random;
	
	/**
	 * Number of brands in the simulation.
	 */
	private int numBrands;
	
	/**
	 * Number of owned touch point that are stored at the registry.
	 */
	private int numTouchPoints;
	
	/**
	 * Perception decay defines the weekly amount of perception lost
	 * after touch point influence (the amount lost will not exceed
	 * the perception gained by touch point exposure) for every
	 * segment.
	 */
	public double[][] perceptiondecayByTouchPointAndSegment;
	
	/**
	 * The amount of talking probability lost after being exposed to
	 * the touch point for every segment.
	 */
	public double[][] discussionHeatdecayByTouchPointAndSegment;
	
	/**
	 * Creates a new Registry instance.
	 * 
	 * @param numTouchPoints - number of owned touch points
	 * @param numBrands - number of brands at the simulation
	 * @param random - current simulation Randomizer
	 */
	public TouchPointOwnedRegistry(
			int numTouchPoints, int numBrands, Randomizer random) {
		
		if (numTouchPoints == 0) throw new IllegalArgumentException(
			"Number of touchpoints should be greater than 0"	
		);
		
		touchpoints = new TouchPointOwned[numTouchPoints];
		testBeans = null;

		this.numTouchPoints = numTouchPoints;
		this.numBrands=numBrands;
		this.random = random;
		
		if (numTouchPoints == 1) {
			permutation= new byte[1];
		} else {
			permutation=ArrayFunctions.shuffleFast(
					(byte) numTouchPoints, random);
		}
	}

	/**
	 * Stores ReachTestBean object for given touch point and brand.
	 * 
	 * This methods is only called in debug mode.
	 * 
	 * @param touchpoint - touch point id for this bean
	 * @param brandid - brand id for this bean
	 * @param bean - ReachTestBean instance
	 */
	public void addBean(int touchpoint, int brandid, ReachTestBean bean) {
		if(testBeans==null) {
			testBeans = new ReachTestBean[numTouchPoints][numBrands];
		}
		testBeans[touchpoint][brandid] = bean;
	}
	
	/*
	 * Public/Static
	 */

	
	/**
	 * Adds a new touch point to current instance.
	 * 
	 * @param id - id for the new touch point
	 * @param tp - the object representing the touch point
	 */
	public final void addTouchPoint(int id, TouchPointOwned tp) {
		touchpoints[id] = tp;
	}
	
	/**
	 * Returns the touch point by its id.
	 * 
	 * @param id The identifier of the touch point
	 * @return The touch point represented by this id
	 */
	public final TouchPointOwned getTouchPointById(int id) {
		return touchpoints[id];
	}
	
	/**
	 * Set debug mode to debug parameter.
	 * 
	 * @param debug - Enable/Disable debug mode
	 */
	public void setDebugMode(boolean debug) {
		this.debug = debug;
	}
	
	/**
	 * Retrieves the value of the debug flag.
	 * 
	 * @return the value of debug flag.
	 */
	public boolean isDebugModeEnabled() {
		return debug;
	}
	
	/**
	 * This method is intended to be called before the simulation begins.
	 * Uses global parameters like the population of agents, the segment 
	 * sizes and the randomizer used by the model.
	 * Returns the effective reach for every touchpoint, brand and segment.
	 * 
	 * @param segmentSizes The sizes of the segments.
	 * @param customers
	 * @return the effective reach for every touchpoint, brand and segment.
	 */
	public final double [][][] scheduleTouchPoints(
			double[] segmentSizes, 
			Agent[] customers, 
			ClientSegments segments,
			int numberOfWeeks,
			int stepsForWeek,
			double agentPopulationFactor
		) {
		
		boolean[][] agentExposure = initializeAgentExposure(
				customers, numberOfWeeks, stepsForWeek
			);
		double [][][] reach = iterateTPs(customers, segments, agentExposure, 
				numberOfWeeks, stepsForWeek, agentPopulationFactor);
		
		//Dispatches combined touchpoint exposure for each agent.
		for (int i = 0; i < customers.length; i++) {
			customers[i].setTouchPointExposure(agentExposure[i]);
		}
		
		return reach;
	}

	/**
	 * Iterates over every touchpoint, scheduling them.
	 * Returns the effective reach for every touchpoint, brand and segment.
	 * 
	 * @param customers - ClientAgent array
	 * @param segments - ClientSegments instance
	 * @param agentExposure - bolean matrix where [i][j] means that agent i
	 * is exposed to any touchpoint at step j
	 * @param stepsForWeek - number of simulation steps for week
	 * @return the effective reach for every touchpoint, brand and segment.
	 */
	private double [][][] iterateTPs(
			Agent[] customers,
			ClientSegments segments, 
			boolean[][] agentExposure,
			int numberOfweeks,
			int stepsForWeek,
			double agentPopulationFactor
		) {
		
		if(debug) {
			schedulers = new ArrayList<TouchPointScheduler>();
		}
		
		double [][][] actualReachByTp = new double [numTouchPoints][][];
		
		for (TouchPointOwned tpo : touchpoints) {
			MarketingPlan[] mps = tpo.getMarketingPlans();
			byte[][][] schedules = new byte[mps.length][][];
			
			TouchPointScheduler scheduler = new TouchPointScheduler(
					segments,
					customers.length, 
					tpo.weeklyReachMaximun,
					tpo.annualReachMaximun, 
					tpo.annualReachSpeed, 
					numberOfweeks,
					stepsForWeek,
					tpo.investmentType,
					agentPopulationFactor,
					debug
				);
			
			if(debug) {
				schedulers.add(scheduler);
			}
			
			actualReachByTp[tpo.id] = iterateMPs(customers, segments, 
					agentExposure, tpo, mps, schedules, scheduler);
			tpo.setSchedules(schedules);
		}
		
		return actualReachByTp;
	}

	/**
	 * Schedules every marketing plan associated to a given touchpoint. 
	 * Returns the effective reach for every brand and segment.
	 * 
	 * If debug mode is enabled, a ReachTestBean is stored.
	 * 
	 * @param customers - ClientAgent array
	 * @param agentExposure - bolean matrix where [i][j] means that agent i
	 * is exposed to any touchpoint at step j
	 * @param tpo - current TouchPointOwned
	 * @param mps - MarketingPlans for this touchpoint
	 * @param schedules - 3d matrix with the schedule for every brand
	 * @param scheduler - scheduler instance configured for given touchpoint
	 * @return the effective reach for every brand and segment.
	 */
	private double[][] iterateMPs(
			Agent[] customers,
			ClientSegments segments, 
			boolean[][] agentExposure, 
			TouchPointOwned tpo,
			MarketingPlan[] mps, 
			byte[][][] schedules,
			TouchPointScheduler scheduler
		) {
		
		double [][] actualReachByBrandBySegment = new double[numBrands][];
		
		for (MarketingPlan m: mps) {
			scheduler.setPlan(m.weeklyPlan);			
			scheduler.schedule(random);
			int brandid=m.brandId;
			byte[][] schedule = scheduler.getSchedule();
			for (int i=0; i<schedule.length; i++) {
				for (int j=0; j<schedule[i].length; j++) {
					agentExposure[i][j] |= schedule[i][j]>0;
				}
			}
			schedules[brandid]=schedule;
			actualReachByBrandBySegment[brandid] = 
					Arrays.copyOf(scheduler.getActualRM(), 
							segments.getNumSegments());			
			
			if(debug) {
				ReachTestBean test = new ReachTestBean(
					customers, 
					schedule,
					scheduler.getDebugSchedule(),
					ArrayFunctions.scaleCopyOfDoubleArray(scheduler.getActualRM(),
							Functions.IDENTITY_SCALE),
					scheduler.getAgentsReached(),
							segments.getNumSegments(),
							MatrixFunctions.scaleCopyOfDoubleMatrix(scheduler.getCARM(),
							Functions.IDENTITY_SCALE),
							MatrixFunctions.scaleCopyOfDoubleMatrix(scheduler.getActualARMByStep(), 
							Functions.IDENTITY_SCALE)
				);
				addBean(tpo.id, brandid, test);
			}					
		}
		
		return actualReachByBrandBySegment;
	}

	/**
	 * Initiates and returns agent exposure as a boolean matrix 
	 * with [numAgents][numSteps].
	 * 
	 * @param customers - a ClientAgent array instance
	 * @param stepsByWeek - number of steps for week
	 * @return initial agent exposure matrix
	 */
	private boolean[][] initializeAgentExposure(
			Agent[] customers, int numberOfWeeks, int stepsByWeek
		) {
		boolean[][] agentExposure = new boolean [customers.length]
				[numberOfWeeks*stepsByWeek];
		return agentExposure;
	}
	
	/**
	 * Expose the given agent c to every touchpoint registered at given step.
	 * 
	 * @param c - the agent exposed to media
	 * @param step - current simulation step
	 */
	public final void executeTPOs(Agent c, int step, Model m) {
		ArrayFunctions.shuffleArrayFast(permutation, random);
		for (int i = 0; i < touchpoints.length; i++) {
			// Get schedule for the touchpoint
			byte[][][] schedule = touchpoints[i].getSchedules();
			// for each brand, it it has impacts, modify agent.
			for (int b=0; b<numBrands; b++) {
				if(schedule[b][c.clientId][step]>0) {
					touchpoints[permutation[i]].modifyAgent(
						c, m, step,b
					);
				}
			}
		}
	}
	
	/**
	 * Returns the test bean related to the touch point id with the 
	 * brand id.
	 * 
	 * @param tpid - the touch point id
	 * @param brandid - the brand id
	 * @return the test bean associated to the given touch point and brand
	 */
	public final ReachTestBean getTest(int tpid, int brandid) {
		return testBeans[tpid][brandid];
	}
	
	/**
	 * Gets number of registered touch points.
	 * 
	 * @return number of touch points.
	 */
	public final int getNumberOfTouchpoints() {
		return numTouchPoints + AbstractTouchPoint.NUM_EARNED_TPS;
	}
	
	/**
	 * Store touch point perception decays for every touch point, including 
	 * the earned ones.
	 * 
	 * @param womPerceptionDecay - WoM decays
	 * @param usagePerceptionDecay - Product Usage decays
	 * @param postPerceptionDecay - Post Online decays
	 * @param touchpointOwnedPerceptionDecay - TouchPointOwned decays
	 */
	public final void storeDecays(
			double[] womPerceptionDecay, 
			double[] usagePerceptionDecay,
			double[] postPerceptionDecay, 
			double[][] touchpointOwnedPerceptionDecay
		) {
		perceptiondecayByTouchPointAndSegment = 
				new double [numTouchPoints
				            +AbstractTouchPoint.NUM_EARNED_TPS][];
		perceptiondecayByTouchPointAndSegment
						[AbstractTouchPoint.WOM] = womPerceptionDecay;
		perceptiondecayByTouchPointAndSegment
						[AbstractTouchPoint.USE] = usagePerceptionDecay;
		perceptiondecayByTouchPointAndSegment
						[AbstractTouchPoint.POST] = postPerceptionDecay;
		for (int tp=0; tp<numTouchPoints; tp++) {
			perceptiondecayByTouchPointAndSegment
						[tp+AbstractTouchPoint.NUM_EARNED_TPS] 
								= touchpointOwnedPerceptionDecay[tp];
		}
	}
	
	/**
	 * Returns all perception decay arrays, including the earned ones.
	 * 
	 * @return the perception decay values for every touch point.
	 */
	public final double [][] getPerceptionDecays() {
		return perceptiondecayByTouchPointAndSegment;
	}
	
	/**
	 * Store touch point discussion heat decays for every touch point, 
	 * including the earned ones.
	 * 
	 * @param womDiscussionHeatDecay - WoM discussion heat decays
	 * @param usageDiscussionHeatDecay - Product Usage discussion heat decays
	 * @param postDiscussionHeatDecay - Post Online discussion heat decays
	 * @param touchpointOwnedDiscussionHeatDecay - TouchPointOwned 
	 * discussion heat decays
	 */
	public final void storeDiscussionHeatDecay(
			double[] womDiscussionHeatDecay, 
			double[] usageDiscussionHeatDecay,
			double[] postDiscussionHeatDecay, 
			double[][] touchpointOwnedDiscussionHeatDecay
		) {
		discussionHeatdecayByTouchPointAndSegment = 
				new double [numTouchPoints 
				            	+ AbstractTouchPoint.NUM_EARNED_TPS][];
		discussionHeatdecayByTouchPointAndSegment
						[AbstractTouchPoint.WOM] = womDiscussionHeatDecay;
		discussionHeatdecayByTouchPointAndSegment
						[AbstractTouchPoint.USE] = usageDiscussionHeatDecay;
		discussionHeatdecayByTouchPointAndSegment
						[AbstractTouchPoint.POST] = postDiscussionHeatDecay;
		for (int tp=0; tp<numTouchPoints; tp++) {
			discussionHeatdecayByTouchPointAndSegment
					[tp+AbstractTouchPoint.NUM_EARNED_TPS] 
							= touchpointOwnedDiscussionHeatDecay[tp];
		}
	}
	
	/**
	 * Returns all discussion heat decay arrays, including the earned ones.
	 * 
	 * @return the discussion heat decay values for every touch point.
	 */
	public final double [][] getDiscussionHeatDecays() {
		return discussionHeatdecayByTouchPointAndSegment;
	}
	
	/**
	 * Retrieves the list of TouchPointScheduler for this touch point.
	 * 
	 * This instance is only created when using debug mode, if the flag
	 * is not enabled, it will cause NullPointerException. 
	 * 
	 * @return the list of schedulers
	 */
	public final List<TouchPointScheduler> getSchedulers() {
		return schedulers;
	}
}
