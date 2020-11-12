package model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.customer.Agent;
import model.decisionmaking.DecisionMaking;
import model.sales.SalesScheduler;
import model.socialnetwork.SocialNetwork;
import model.socialnetwork.SocialNetwork.NetworkType;
import model.touchpoints.TouchPointOwnedRegistry;
import model.touchpoints.earned.AbstractTouchPoint;
import model.touchpoints.earned.PostReadOnline;
import model.touchpoints.earned.ProductUsage;
import util.exception.sales.SalesScheduleError;
import util.functions.ArrayFunctions;
import util.random.Randomizer;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;
import util.statistics.Statistics;

/**
 * Model instances are simulation scenarios. Models states are: ready 
 * (to run), running and done (finished running).
 * 
 * @author imoya
 *
 */
public class Model {
	
	//#########################################################################
	// Static attributes & constants
	//#########################################################################	

	/**
	 * Logger for Model.class.
	 */
	private final static Logger logger = LoggerFactory.getLogger(Model.class);
	
	/**
	 * Checks if log is enabled for debug level. 
	 */
	private final static boolean LOG_DEBUG = logger.isDebugEnabled();

	//-------------------------------------------------------------------------
	
	/**
	 * Default model scenario name.
	 */
	public static final String DEFAULT_NAME = "Unnamed model";
	/**
	 * Default model scenario description.
	 */
	public static final String DEFAULT_DESCRIPTION = "No description available";
	
	/**
	 * Proportion of agents shuffled at every simulation step.
	 */
	public static final double AGENT_ORDER_PERCENTAGE = 0.5;
	
	/**
	 * Default size of agent population.
	 */
	public static final int DEFAULT_NUMBER_OF_AGENTS=100;
	/**
	 * Default size of real population.
	 */
	public static final int DEFAULT_POPULATION_SIZE=10000;	
	/**
	 * Default number of segments.
	 */
	public static final int DEFAULT_NUMBER_OF_SEGMENTS=1;
	/**
	 * Default number of brands.
	 */
	public static final int DEFAULT_NUMBER_OF_BRANDS=1;
	/**
	 * Default number of attributes.
	 */
	public static final int DEFAULT_NUMBER_OF_ATTRIBUTES=1;
	/**
	 * Default number of touch points.
	 */
	public static final int DEFAULT_NUMBER_OF_TOUCHPOINTS=0;
	
	/**
	 * Default initial awareness percentage value.
	 */
	public static final double DEFAULT_INITIAL_AWARENESS=0.05;
	/**
	 * Default awareness decay probability.
	 */
	public static final double DEFAULT_AWARENESS_DECAY=0.05;
	
	/**
	 * Default brand penetration percentage value.
	 */
	public static final double DEFAULT_BRAND_PENETRATION=0.1;
	
	/**
	 * Default brand attribute perception value.
	 */
	public static final double DEFAULT_BRAND_ATTRIBUTE_VALUE=0.5;
	
	/**
	 * Default awareness impact probability.
	 */
	public static final double DEFAULT_AWARENESS_IMPACT=0.1;
	/**
	 * Default discussion heat impact value.
	 */
	public static final double DEFAULT_DISCUSION_HEAT_IMPACT=0.1;
	/**
	 * Default discussion heat decay value.
	 */
	public static final double DEFAULT_DISCUSION_HEAT_DECAY=0.01;
	/**
	 * Default perception speed percentage value.
	 */
	public static final double DEFAULT_PERCEPTION_SPEED=0.1;
	/**
	 * Default perception decay percentage value.
	 */
	public static final double DEFAULT_PERCEPTION_DECAY=0.01;
	/**
	 * Default Word of Mouth talking probability.
	 */
	public static final double DEFAULT_TALKING_PROBABILITY=0.1;
	/**
	 * Default Word of Mouth segment influence percentage value.
	 */
	public static final double DEFAULT_SEGMENT_INFLUENCE=0.5;
	/**
	 * Default Word of Mouth segment connectivity percentage value.
	 */
	public static final double DEFAULT_SEGMENT_CONNECTIVITY=0.5;
	
	/**
	 * Default Posting probability.
	 */
	public static final double DEFAULT_ONLINE_POSTING_PROBABILITY=0.1;
	/**
	 * Default Reading probability.
	 */
	public static final double DEFAULT_ONLINE_READING_PROBABILITY=0.1;	
	
	/**
	 * Default network type.
	 */
	public static final NetworkType DEFAULT_NETWORK_TYPE = 
		NetworkType.SCALE_FREE_NETWORK;
	
	/**
	 * Default Model seed.
	 */
	public static final long DEFAULT_SEED=RandomizerUtils.PRIME_SEEDS[0];
	
	/**
	 * Initial simulation step.
	 */
	protected static final int INITIAL_STEP_VALUE=0;	
	
	/**
	 * Default decision cycle value in number of steps.
	 */
	public static final int DEFAULT_DECISION_CYCLE=7;
	/**
	 * Default checkpoint length in number of steps.
	 */
	public static final int DEFAULT_CHECKPOINT=DEFAULT_DECISION_CYCLE;
	
	/**
	 * Week duration in single day steps.
	 */
	public static final int DAYS_OF_WEEK = 7;
	
	/**
	 * Daily simulation steps value.
	 */
	public static final int SINGLE_STEP_PER_DAY = 1;

	/**
	 * Number of step by week for daily simulations.
	 */
	public static final int STEPS_FOR_WEEK_DAILY = 7;
	
	/**
	 * Number of step by week for weekly simulations.
	 */
	public static final int STEPS_FOR_WEEK_WEEKLY = 1;
	
	/**
	 * Minimum value for agents perceptions.
	 */
	public static final double MINIMUM_PERCEPTION_VALUE=0.0;
	/**
	 * Default value for agents perceptions.
	 */
	public static final double DEFAULT_PERCEPTION_VALUE=5.0;
	/**
	 * Maximum value for agents perceptions.
	 */
	public static final double MAXIMUM_PERCEPTION_VALUE=10.0;
	
	/**
	 * Default simulation duration in weeks.
	 */
	public static final int DEFAULT_NUMBER_OF_WEEKS=52;
	
	/**
	 * Default simulation duration in days.
	 */
	public static final int DEFAULT_NUMBER_OF_STEPS_DAILY=
		DEFAULT_NUMBER_OF_WEEKS*DAYS_OF_WEEK;
	
	/**
	 * Default awareness filtering flag value during perception diffusion.
	 */
	public static final boolean NO_AWARENESS_FILTER = false;
	
	/**
	 * Default upper bound for Word of Mouth sentiment analysis.
	 */
	public static final double DEFAULT_WOM_SENTIMENT_POSITIVE = 7.0;
	/**
	 * Default lower bound for Word of Mouth sentiment analysis.
	 */
	public static final double DEFAULT_WOM_SENTIMENT_NEGATIVE = 3.0;
	
	// ########################################################################
	// Instance attributes
	// ########################################################################	
	
	/**
	 * Number of agents.
	 */
	private final int nrAgents;
	/**
	 * Number of brands.
	 */
	private final int nrBrands;
	/**
	 * Number of attributes.
	 */
	private final int nrAttributes;
	/**
	 * Number of touch points.
	 */
	private final int nrTouchpoints;
	
	/**
	 * Current simulation step.
	 */
	protected int step = INITIAL_STEP_VALUE;
	/**
	 * Number of simulation steps.
	 */
	protected final int numberOfSteps;
	
	/**
	 * Number of simulated steps by each model week.
	 */
	private final int stepsByWeek;
	
	/**
	 * Segments bean class instance.
	 */
	private ClientSegments segments = null;
	/**
	 * Agent population as an array.
	 */
	private Agent[] agents = null;
	
	/**
	 * Brand description beans.
	 */
	private Brand[] brands = null;
	
	/**
	 * Product usage bean.
	 */
	private ProductUsage usage = null;
	
	/**
	 * Simulation randomizer.
	 */
	public final Randomizer random;
	
	/**
	 * Touchpoint owned registry
	 */
	private TouchPointOwnedRegistry registry;

	/**
	 * Decision making module
	 */	
	protected DecisionMaking decisionMaking;
	
	/**
	 * Posting and reading online module
	 */		
	private PostReadOnline postReadOnline;
	
	/**
	 * Gathers simulation statistics for every simulated step.
	 */
	protected Statistics statistics;	
	
	/**
	 * Order of agents module execution.
	 */
	protected byte[] modulesOrder;
	
	/**
	 * Agent step execution order at a given step.
	 */
	protected int[] agentsOrder;
	
	/**
	 * Simulation sales dispatcher. Assigns when every agent at the population 
	 * completed its step.
	 */
	protected SalesScheduler scheduler;
	
	/**
	 * Flag for awareness filtering during perception diffusion.
	 */
	private boolean awarenessFilter;
	
	// Social network 
	
	/**
	 * Social network generator instance.
	 */
	private SocialNetwork socialNetwork;
	
	// WoM sentiment ranges
	
	/**
	 * Current upper bound for Word of Mouth sentiment analysis.
	 */
	private double womSentimentPositive;
	/**
	 * Current lower bound for Word of Mouth sentiment analysis.
	 */
	private double womSentimentNegative;
	
	protected boolean recordSales;
	
	/**
	 * Creates a Model instance. This instance will be ready to run when 
	 * every component setup is finished.
	 * 
	 * @param stepsByWeek number of simulations steps by each defined week.
	 * @param nrAgents number of agents populating current simulation.
	 * @param nrSegments number of segments modeled at current simulation.
	 * @param nrBrands number of brands considered at current simulation.
	 * @param nrAttributes number of attributes modeled at current simulation.
	 * @param nrWeeks number of weeks for current simulation.
	 * @param nrTouchpoints number of touch points modeled at current 
	 * simulation.
	 * @param seed randomizer seed used for instancing simulation randomizer.
	 * @param awarenessFilter enables/disables using awareness as a filter 
	 * during perception diffusion.
	 * @param womSentimentPositive upper bound used for Word of Mouth sentiment 
	 * analysis. 
	 * @param womSentimentNegative lower bound used for Word of Mouth sentiment 
	 * analysis. 
	 * @param agentsRatio ratio defining the relationship 
	 * [real population / agent population].
	 */
	public Model(
			int stepsByWeek,
			int nrAgents,
			int nrSegments,
			int nrBrands,
			int nrAttributes,
			int nrWeeks,
			int nrTouchpoints,
			long seed,
			boolean awarenessFilter,
			double womSentimentPositive,
			double womSentimentNegative,
			double agentsRatio
		) {
		
		this.stepsByWeek = stepsByWeek;

		this.numberOfSteps=nrWeeks*stepsByWeek;
		
		this.nrAgents = nrAgents;
		this.nrBrands = nrBrands;
		this.nrAttributes=nrAttributes;
		this.nrTouchpoints=nrTouchpoints;
		
		this.awarenessFilter = awarenessFilter;
		
		random = RandomizerFactory.createRandomizer(
			RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, seed
		);
		
		modulesOrder = ArrayFunctions.shuffleFast(Agent.NUM_MODULES, random);
		
		
		segments = new ClientSegments();
		
	    statistics = new Statistics(
	    	nrSegments, 
	    	nrBrands, 
	    	nrAttributes, 
	    	numberOfSteps,
	    	stepsByWeek,
	    	agentsRatio
	    );
	    
	    this.womSentimentPositive = womSentimentPositive;
	    this.womSentimentNegative = womSentimentNegative;

	    agentsOrder = new int[nrAgents];
	    for (int i=0; i<nrAgents; i++) {
	    	agentsOrder[i]=i;
	    }
	    
	    if (LOG_DEBUG) logger.debug("Model() end");
	}

	// ########################################################################
	// Get/Set methods
	// ########################################################################	

	/**
	 * Returns the size of agent population.
	 * @return the size of agent population.
	 */
	public int getNrAgents() {
		return nrAgents;
	}

	/**
	 * Returns current simulation step value.
	 * @return current simulation step value.
	 */
	public int getStep() {
		return step;
	}
	
	/**
	 * Returns number of steps for each modeled week.
	 * @return number of steps for each modeled week.
	 */
	public int getNumberOfSteps() {
		return numberOfSteps;
	}
	
	/**
	 * This method returns the maximum number of steps possible, 
	 * that equals to the number of weeks iterated daily.
	 * @return maximum number of steps.
	 */
	public int getMaximumNumberOfSteps() {
		if(stepsByWeek!=Model.DAYS_OF_WEEK) {
			double proportion = 1.0 / (double) stepsByWeek;
			int maximumPossible = (int)(proportion 
					* numberOfSteps * Model.DAYS_OF_WEEK);
			return maximumPossible;
		} else return numberOfSteps;
	}

	/**
	 * Returns the number of brands modeled.
	 * @return the number of brands modeled.
	 */
	public int getNrBrands() {
		return nrBrands;
	}

	/**
	 * Returns the number of attributes modeled.
	 * @return the number of attributes modeled.
	 */
	public int getNrAttributes() {
		return nrAttributes;
	}
	
	/**
	 * Returns the segments description bean.
	 * @return the segments description bean.
	 */
	public ClientSegments getSegments() {
		return segments;
	}

	/**
	 * Sets the segments description bean to given instance.
	 * @param segments the new segments description bean.
	 */
	public void setSegments(ClientSegments segments) {
		this.segments = segments;
	}
	
	/**
	 * Returns the brand beans array.
	 * @return the brand beans array.
	 */
	public Brand[] getBrands() {
		return brands;
	}
	
	/**
	 * Sets the brand beans array to a new instance.
	 * @param brands new brand beans array.
	 */
	public void setBrands(Brand[] brands) {
		this.brands=brands;
	}
	
	/**
	 * Returns agent population as an array.
	 * @return agent population as an array.
	 */
	public Agent[] getAgents() {
		return agents;
	}

	/**
	 * Sets the agent population to given value.
	 * @param agents the new agent population value.
	 */
	public void setAgents(Agent[] agents) {
		this.agents = agents;
	}

	/**
	 * Returns the social network generator instance.
	 * @return the social network generator instance.
	 */
	public SocialNetwork getSocialNetwork() {
		return socialNetwork;
	}

	/**
	 * Sets the social network generator instance to given value.
	 * @param socialNetwork the new social network generator instance.
	 */
	public void setSocialNetwork(SocialNetwork socialNetwork) {
		this.socialNetwork = socialNetwork;
	}
	
	public void setTouchPointRegistry(TouchPointOwnedRegistry tpor) {
		registry = tpor;
	}
	
	public TouchPointOwnedRegistry getTPORegistry() {
		return registry;
	}
	
	/**
	 * Returns the sales scheduler for current simulation.
	 * @return the sales scheduler for current simulation.
	 */
	public SalesScheduler getSalesScheduler() {
		return scheduler;
	}
	
	/**
	 * Sets current sales scheduler to given instance.
	 * @param scheduler the new sales scheduler for current simulation.
	 */
	public void setSalesScheduler(SalesScheduler scheduler) {
		this.scheduler=scheduler;
	}
	
	/**
	 * Returns the value of awareness filter flag.
	 * @return the value of awareness filter flag.
	 */
	public boolean isAwarenessFilter() {
		return awarenessFilter;
	}
	
	/**
	 * Returns the product usage bean instance.
	 * @return the product usage bean instance.
	 */
	public ProductUsage getUsage() {
		return usage;
	}
	
	/**
	 * Sets the product usage bean to given instance.
	 * @param productUsage the new product usage bean instance.
	 */
	public void setUsage(ProductUsage productUsage) {
		usage=productUsage;
	}
	
	/**
	 * Returns the number of simulation steps by modeled week.
	 * @return the number of simulation steps by modeled week.
	 */
	public int getStepsByWeek() {
		return stepsByWeek;
	}
	
	/**
	 * Returns statistics recording instance.
	 * @return statistics recording instance.
	 */
	public Statistics getStatistics() {
		return statistics;
	}
	
	/**
	 * Returns the value of current upper bound for Word of Mouth sentiment 
	 * analysis.
	 * @return the value of current upper bound for Word of Mouth sentiment 
	 * analysis.
	 */
	public double getWomSentimentPositive() {
		return womSentimentPositive;
	}

	/**
	 * Returns the value of current lower bound for Word of Mouth sentiment 
	 * analysis.
	 * @return the value of current lower bound for Word of Mouth sentiment 
	 * analysis.
	 */
	public double getWomSentimentNegative() {
		return womSentimentNegative;
	}
	
	/**
	 * Sets the decision making instance
	 * @param dm - the current decision making instance
	 */
	public void setDecisionMaking(DecisionMaking dm) {
		this.decisionMaking = dm;
	}
	
	/**
	 * Returns the current decision making instance
	 * @return - the current decision making instance
	 */
	public DecisionMaking getDecisionMaking() {
		return decisionMaking;
	}

	/**
	 * Returns the current postReadOnline instance
	 * @return - the current postReadOnline instance
	 */
	public PostReadOnline getPostReadOnline() {
		return postReadOnline;
	}

	/**
	 * Sets the postReadOnline instance
	 * @param postReadOnline - the current postReadOnline instance
	 */
	public void setPostReadOnline(PostReadOnline postReadOnline) {
		this.postReadOnline = postReadOnline;
	}	
	
	// ########################################################################
	// Functionality
	// ########################################################################	

	/**
	 * Enables addition statistics recording based on given values. 
	 * (By default, only sales are recorded).
	 * 
	 * @param recordAwareness enables awareness statistics recording.
	 * @param recordRerceptions enables perceptions statistics recording.
	 * @param recordSegmentDetails enables segment specific details recording.
	 * @param recordContributions enables touch point contributions recording.
	 */
	public void enableAdditionalStatistics(
			boolean recordSales, 
			boolean recordAwareness, 
			boolean recordRerceptions, 
			boolean recordSegmentDetails,
			boolean recordWomReports,
			boolean recordContributions) {
		
		this.recordSales = recordSales;
		statistics.enableAdditionalStatistics(
			recordAwareness, 
			recordRerceptions, 
			recordSegmentDetails,
			recordWomReports,
			segments.getSegmentSizesInt(),
			recordContributions,
			nrTouchpoints + AbstractTouchPoint.NUM_EARNED_TPS
		);
	}
	
	/**
	 * Enables Word of Mouth reporting for every agent at the population.
	 */
	public void enableWoMReports() {
		for (Agent c: agents) {
			c.enableWoMReports();
		}
	}
	
	/**
	 * Runs the model, printing execution times.
	 * 
	 * @throws SalesScheduleError if a problem was found during sales scheduling.
	 */
	public void run() throws SalesScheduleError {

		///////////////////////////////////////////////////////////////////////
		// DEBUG: Time measure start
		final long startTime = System.nanoTime();
		///////////////////////////////////////////////////////////////////////
		
		runSilent();
		
		///////////////////////////////////////////////////////////////////////
		// DEBUG: Time measure stop
		final long endTime = System.nanoTime();
		System.out.println(String.format(
			" Model running time: %.3f sec.", 
			(endTime - startTime) / 1000000000.0
		));	
	}

	/**
	 * Runs the model without displaying execution times.
	 * 
	 * @throws SalesScheduleError if a problem was found during sales scheduling.
	 */
	public void runSilent() throws SalesScheduleError {
		// Reset the step
		step = Model.INITIAL_STEP_VALUE;

		// Iterate the agents "numberOfSteps" times

		while(step < numberOfSteps) {
			
			agentsOrder=ArrayFunctions.partialShuffle(
					agentsOrder, random, AGENT_ORDER_PERCENTAGE);
			// Run every agent for every step using randomized order.
			for (int index : agentsOrder) {
				agents[index].step(this);
			}
			
			// Assign sales and store step data in statistics
			updateStatistics();

			step++;
		}
	}
	
	/**
	 * Assign sales and records data in statistics for current step.
	 * 
	 * @throws SalesScheduleError if a problem was found during sales scheduling.
	 */
	protected void updateStatistics() throws SalesScheduleError {
		if(recordSales) {
			scheduler.assignSales(step, random, statistics, decisionMaking);
		}
		statistics.updateTimeSeries(agents, step);
	}
	
	/**
	 * Shuffles module order. Because this shuffle is done for every agent at 
	 * every step, swapping two modules is considered enough, so there is no 
	 * way of predict what execution order is going to follow a concrete agent 
	 * at a specific time.
	 * @return the next modules order.
	 */
	public byte[] nextModulesOrder() {
		final int swapIndex = random.nextInt(Agent.NUM_MODULES);
		final byte temp = modulesOrder[0];
		modulesOrder[0] = modulesOrder[swapIndex];
		modulesOrder[swapIndex] = temp;
		return modulesOrder;
	}
}
