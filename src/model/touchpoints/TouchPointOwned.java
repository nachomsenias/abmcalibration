package model.touchpoints;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Model;
import model.customer.Agent;
import model.touchpoints.earned.AbstractTouchPoint;
import util.functions.Functions;

/**
 * TouchPointOwned objects represents paid touchpoints.
 * 
 * This touchpoint are based on an a-priori approach, scheduling its
 * interaction with the customer before running the simulation.
 *  
 * @author imoya
 *
 */
public class TouchPointOwned
		extends AbstractTouchPoint {
	
	/**
	 * Defines the nature of the investment so the scheduler can appropriately 
	 * calculate its exposure.
	 */
	public enum InvestmentType {GRP, IMPACTS, IMPRESSIONS};
	
	/** 
	 * Logger instance for TPO class. 
	 */
	private final static Logger logger =
		LoggerFactory.getLogger(TouchPointOwned.class);
	
	/**
	 * Logging flag for info level.
	 */
	private static final boolean LOG_INFO = logger.isInfoEnabled();
	
	/**
	 * Touch point unique identifier.
	 */
	public final int id;
	/**
	 * The maximum perception value that can be achieved through touch
	 * point influence for every segment.
	 */
	public final double[] perceptionPotential;
	/**
	 * The maximum percentage from a given segment that can be weekly
	 * reached by the touch point.
	 */
	public final double[] weeklyReachMaximun;
	/**
	 * The maximum percentage from a given segment that can be yearly
	 * reached by the touch point.
	 */
	public final double[] annualReachMaximun;
	/**
	 * Defines how fast reach evolves as GRP are applied for every 
	 * segment.
	 */
	public final double[] annualReachSpeed;
	/**
	 * Defines the metric applied to the marketing plans of this touch point.
	 */
	public final InvestmentType investmentType;	
	/**
	 * Defined marketing plans associated to this touch point.
	 */
	protected MarketingPlan[] marketingPlans;	
	/**
	 * Touch point exposure schedule, with 
	 * [BrandId][AgentID][Step]=NumberOfHits.
	 */
	private byte[][][] schedules = null;

	/**
	 * Creates a TPO instance using given values.
	 * 
	 * @param id - unique touch point id.
	 * @param numBrands - number of brands at the simulation.
	 * @param perceptionSpeed - perception speed by segment.
	 * @param awarenessImpact - awareness impact by segment.
	 * @param discussionImpact - discussion heat impact by segment.
	 * @param perceptionPotential - perception potential by segment.
	 * @param weeklyReachMaximun - maximum weekly reach by segment.
	 * @param annualReachMaximun - maximum annual reach by segment.
	 * @param annualReachSpeed - annual reach speed by segment.
	 */
	public TouchPointOwned(
			int id,
			int numBrands,
			double[] perceptionSpeed,
			double[] awarenessImpact,
			double[] discussionImpact,
			double[] perceptionPotential,
			double[] weeklyReachMaximun,
			double[] annualReachMaximun,
			double[] annualReachSpeed,
			InvestmentType investment
		){
		super(
			perceptionSpeed,
			awarenessImpact,
			discussionImpact
		);
		this.id=id;
		this.perceptionPotential = perceptionPotential;
		this.weeklyReachMaximun = weeklyReachMaximun;
		this.annualReachMaximun = annualReachMaximun;
		this.annualReachSpeed = annualReachSpeed;
		
		this.investmentType=investment;

		marketingPlans = new MarketingPlan[numBrands];
	}

	/*
	 * Getters & Setters
	 */
	
	/**
	 * Returns MarketingPlan beans related to this touch point.
	 * 
	 * @return MarketingPlan beans related to this touch point.
	 */
	public MarketingPlan[] getMarketingPlans() {
		return marketingPlans;
	}
	
	/**
	 * Returns the metric followed by the marketing plans of this touch point.
	 * 
	 * @return the metric followed by the marketing plans of this touch point.
	 */
	public InvestmentType getInvestmentType() {
		return investmentType;
	}
	
	/**
	 * Sets a MarketingPlan bean to a concrete brand. 
	 * 
	 * @param mp - MarketingPlan bean.
	 * @param brandId - brand id.
	 */
	public void addMarketingPlan(MarketingPlan mp, int brandId) {
		this.marketingPlans[brandId]= mp;
	}

	/*
	 * Functionality
	 */

	@Override
	public void modifyAgent(
			Agent customer, 
			Model m, 
			int step,
			int brandId
		){
		//Because the touch point should have
		//been already scheduled, all the 
		//information needed for modifying the
		//agent is contained between the touch
		//point and the agent itself.
		
		//For every brand scheduled for the touch point
//		int numBrands=marketingPlans.length;
//		int clientId=customer.clientId;
//		for (int b=0; b<numBrands; b++) {
//			byte[][] schedule = ;
//			
			int hits = schedules[brandId][customer.clientId][step];
			
//			if(hits>0) {
				//Resolve hits
				modifyAgentStats(
						customer, hits,
							brandId, step, m);
//			}
//		}
	}
	
	/**
	 * Calculates the perception increment combining perception potential 
	 * and speed with the previous value.
	 * 
	 * This value is modified using quality and emphasis modifiers.
	 * 
	 * @param segment - the segment of the agent being affected
	 * @param hits - the number of exposures
	 * @param emphasis - the emphasis modifier for current campaign
	 * @param quality - the quality modifier for current campaign
	 * @param previousIncrement - previous influence achieved by the touchpoint 
	 * @return amount of perception increment
	 */
	protected double calculatePerceptionIncrement(
			int segment,
			int hits,
			double emphasis,
			double quality,
			double previousIncrement
		) {
		double speed = perceptionSpeed[segment] * emphasis * quality;
		
		if(speed==0.0) {
			return 0.0;
		} else if (speed>=1) {			
			return perceptionPotential[segment]-previousIncrement;
		}
		
		double effectiveSpeed = speed;
		
		for (int i=1; i<hits; i++) {
			effectiveSpeed += speed * (1.0-effectiveSpeed);
		}
		
		double increment = (perceptionPotential[segment]-previousIncrement)
				*effectiveSpeed;

		return increment;
	}
	
	/**
	 * Modifies agent perceptions and awareness.
	 * 
	 * @param agent - agent to be influenced by touch point.
	 * @param hits - number of exposures.
	 * @param brand - brand id.
	 * @param step - step when the touch point is influencing the agent.
	 * @param random - simulation randomizer instance.
	 */
	protected void modifyAgentStats(
			Agent agent, 
			int hits,			
			int brand,
			int step,
			Model model
		){
		
		final int segment = agent.segmentId;		
		
		MarketingPlan m = marketingPlans[brand];
		
		//Creative parameters
		byte[] creativityByStep = m.creativityByStep;
		
		double[] emphasis = m.emphasis[creativityByStep[step]];
		double[] quality = m.quality[creativityByStep[step]];
		
		//Log variables
		String logHeader = null;
		String logBody = null;
		if (LOG_INFO) {
			logHeader = " [" + brand + ":";
			logBody = "";
		}
		
		int touchpointId = id+AbstractTouchPoint.NUM_EARNED_TPS;
		
		// Modify perceptions for every attribute
		for (int att=0; att<emphasis.length; att++) {
			
			double increment = calculatePerceptionIncrement(
					segment,
					hits,
					emphasis[att],
					Functions.qualityFunction(quality[att]),
					agent.getTouchpointPerceptionInfluenceByAttribute(
						touchpointId, 
						brand,
						att
					)
				);
			
			if(increment!=0) {
				agent.changePerceptions(
					brand,
					att,
					increment,
					touchpointId
				);
				// Create a string for a log
				if(LOG_INFO) {
					logHeader += " " + att;
					logBody += " " + String.format("%.3f", increment);
				}
			}
		}
		
		// Discussion Heat
		double discussionHeat=this.discussionImpact[segment]*hits;
		if(discussionHeat!=0) {
			agent.applyDiscussionHeat(
					step, 
					discussionHeat, 
					brand, 
					touchpointId
				);
		}		
		
		// Awareness
		boolean awarenessChange = agent.getAwareness()[brand];
		// Check if the customer has awareness of that product
		// If not, process...
		if(!awarenessChange) {
			awarenessChange = awarenessChange != 
					checkBrandAwareness(agent, brand, segment, hits, model, step);
		}
		
		if(LOG_INFO) {
			String logAwareness;
			if(awarenessChange) {
				logAwareness = " AGENT HAS AWARENESS";
			} else {
				logAwareness = " NO IMPACT";
			}
			// Write awareness log to the file		
			String awarenessMessage = "Step " + step + " TP AWARENESS id " + this.id 
	        		+ " agent " + agent.clientId + " segment " 
	        		+ agent.segmentId + " --> brand " + brand + logAwareness;			
		    logger.info(awarenessMessage);
		    // Write perception log to the file
		    String logMessage = "Step " + step + " TP PERCEPTION id " + this.id
				+ " agent "	+ agent.clientId + " segment "
				+ agent.segmentId + " --> [brand: attributes; values]"	
				+ logHeader + ";" + logBody + "]";
			logger.info(logMessage);
		}
	}

	/**
	 * Sets the exposure schedule for this touch point.
	 * 
	 * @param schedule - the schedule for this touch point.
	 */
	public void setSchedules(byte[][][] schedule) {
			this.schedules=schedule;
	}
	
	public byte[][][] getSchedules() {
		return schedules;
	}
	
	/**
	 * Creates a default investment array filled with the GRP type.
	 * @param numTouchpoints the desired length for the array.
	 * @return an investment array filled with the GRP type.
	 */
	public static final InvestmentType[] getDefaultInvestmentType(int numTouchpoints) {
		InvestmentType[] investment = new InvestmentType[numTouchpoints];
		Arrays.fill(investment, InvestmentType.GRP);		
		return investment;
	}
}
