package model.customer;

import java.util.Arrays;

import model.Model;
import model.decisionmaking.DecisionMaking;
import model.touchpoints.TouchPointOwnedRegistry;
import model.touchpoints.earned.AbstractTouchPoint;
import model.touchpoints.earned.ProductUsage;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.exception.sales.SalesScheduleError;
import util.exception.simulation.NoAwarenessException;
import util.functions.Functions;
import util.random.Randomizer;


/**
 * Defines a client agent for ABM model. It includes all the actions of the
 * agent during one step such as:
 * 1. Word of Mouth (WoM).
 * 		- awareness diffusion
 * 		- perception diffusion
 * 		- discussion heat
 * 2. Decays (awareness, perceptions, discussion heat).
 * 3. Exposure to touchpoints.
 * 4. Posting/reading online.
 * 5. Product usage.
 * 6. Buying a product of a brand.
 * 
 * @author ktrawinski
 *
 */
public class Agent {
	
	// ########################################################################
	// Static
	// ########################################################################

	/**
	 * Number of modules influencing the agent: this modules are shuffled in 
	 * order to avoid biasing results because of how influences are received.
	 */
	public static final byte NUM_MODULES = 3;
	/**
	 * Maximum value for every talking probability.
	 */
	public static final double MAXIMUM_TALKING = 1.0;
	
	/**
	 * Root logger: this is the main logger for this class. Every information 
	 * logged will be included here.
	 */
	private final static Logger logger = 
		LoggerFactory.getLogger(Agent.class);
	
	/**
	 * Logging flag for debug level.
	 */
	private final static boolean LOG_DEBUG = logger.isDebugEnabled();
	/**
	 * Logging flag for info level.
	 */
	private final static boolean LOG_INFO = logger.isInfoEnabled();
	
	// ########################################################################
	// Variables
	// ########################################################################	
	
	/**
	 * An array of flags marking touch point owned exposure by step.
	 * 
	 * exposure[step] = true if any brand by any touch point is influencing 
	 * the agent at this step. 
	 */
	protected boolean[] exposeToTouchpoints;

	/**
	 * Identifiers of the brands purchased by the agent. This array is used
	 * during sales scheduling with the goal of being an intermediate buffer.
	 */
	protected boolean[] brandPurchased;
	
	/**
	 * The brands usable by the agent. This structure may 
	 * also be called 'Inventory'.
	 */
	private boolean[] hasBrand;
	
	/**
	 * An unique client Id
	 */
	public final int clientId;
	/**
	 * Identifier of the segment where the agent belongs. An agent will 
	 * not switch segment during the simulation.
	 */
	public final int segmentId;
	
	/**
	 * Agent perceptions split by brand and attribute.
	 * 
	 * perceptions[i][j] = perception value of brand i for attribute j.
	 */
	protected double[][] perceptions;
	
	/**
	 * Agent awareness of brands.
	 * 
	 * awareness[i] = true if the agent has awareness of brand i.
	 */
	protected boolean[] awareness;
	
	/**
	 * Perception increments by touch points, brands and attributes.
	 * 
	 * increments[i][j][k] = influence of brand j at attribute k 
	 * 							using touch point i.
	 */
	protected double[][][] touchpointPerceptionIncrements;

	/**
	 * Returns touch point contribution to given attribute and brand
	 * perceptions.
	 * 
	 * @param att - attribute id
	 * @param brand - brand id
	 * @param tp - touch point id
	 * @return contribution for touch point to brand's attributes perceptions.
	 */
	public double getContributionNowByAttByBrandByTp(int att, int brand, int tp) {
		double increment = 0;
		
		if(touchpointPerceptionIncrements[tp]!=null 
				&& touchpointPerceptionIncrements[tp][brand]!=null
				) {
			increment = touchpointPerceptionIncrements[tp][brand][att];
		}	
		return increment;
	}
	
	/**
	 * Inherited talking probability. This probability is the segment's 
	 * talking probability, no it will not change.
	 */
	private final double baseTalkingProbability;
	
	/**
	 * Talking probabilities by brand. Those is the actual values used for 
	 * diffusion, and those are the values that are modified by touch points 
	 * by using discussion heat impact.
	 */
	protected double [] talkingProbabilities;
	
	/**
	 * Amount of talking probability increased by touch point and brand.
	 * 
	 * applied[i][j] = discussion heat achieved by brand j through 
	 * 					touch point i.
	 */
	private double [][] appliedDiscussionHeatImpact;

	/**
	 * WoM awareness impact. This value is inherited from agent's segment.
	 */
	protected final double awarenessImpact;
	
	/**
	 * Agent awareness decay. This value is inherited from agent's segment.
	 */
	protected final double segmentAwarenessDecay;

	/**
	 * WoM discussion heat impact. This value is inherited from 
	 * agent's segment.
	 */
	protected final double womDiscussionHeatImpact;
	
	/**
	 * WoM perception speed. This value is inherited from agent's segment.
	 */
	protected final double perceptionSpeed;

	/**
	 * Number of brands that the agent has awareness of.
	 */
	protected int awarenessCount;
	
	/**
	 * Decision cycle flag. If the flag is true, the agent is performing 
	 * its decision cycle and is not able to buy.
	 */
	protected boolean inDecisionCycle;
	
	/**
	 * Instead of using double probability to decide whether the product
	 * will be used, a byte-shaped plan is calculated a priori based using 
	 * an amount of steps that will define the interval where the brand
	 * will be experienced.
	 * 
	 * This plan stores how many times the customer experiences the product
	 * by any step in the simulation.
	 */
	private byte[] usePlanning;
	
	//---------------------------- Social Network ---------------------------//
	
	/**
	 * Agent's neighbors by id. Those identifiers comes from social network 
	 * topology and are currently not modified.
	 */
	protected final int[] neighbors;
	
	//------------------------------ WoM reports ----------------------------//
	
	/**
	 * WoM reports are useful in order to provide the user with more insight
	 * of what happend in WoM during the simulation 
	 */
	
	/**
	 * Gathers a nr of conversations that agent obtained during the current
	 * step. It is divided by brand.
	 */
	protected int[] womVolumeByBrand;
	
	/**
	 * Gathers a nr of conversations that agent obtained during the current
	 * step. It is divided by attributes.
	 */
	protected int[] womVolumeByAtt;
	
	/**
	 * Gathers a nr of positive conversations that agent obtained 
	 * during the current step. It is divided by brands.
	 */	
	protected int[] womSentimentPos;
	
	/**
	 * Gathers a nr of negative conversations that agent obtained 
	 * during the current step. It is divided by brands.
	 */		
	protected int[] womSentimentNeg;
	
	/**
	 * A boolean flag whether a conversation about current brand 
	 * arrived to agent during the current step. It is divided by brands.
	 * It serves to calculate the total reach in the simulation.
	 */		
	protected boolean[] womReachByBrand;
	
	/**
	 * Stores raw touch point discussion heat impact by brand and touch point. 
	 * This value is stacked at the statistics bean and used as a percentage at
	 * the report level.
	 */
	private double[][] womContributionByBrandByTp;
	
	/**
	 * Enables / Disables word of mouth reports.
	 */
	protected boolean WOM_REPORTS = false;
	
	// ########################################################################
	// Constructors
	// ######################################################################## 	
	
	/**
	 * Initializes an instance of agent.
	 * 
	 * @param segmentTalking base segment probability.
	 * @param segmentAwarenessDecay awareness decay probability.
	 * @param awarenessImpact WoM awareness impact.
	 * @param discussionHeatImpact WoM discussion heat impact.
	 * @param perceptionSpeed WoM perception speed.
	 * @param initialItems Product Usage Initial inventory.
	 * @param clientId unique agent identifier.
	 * @param segmentId id of the segment where the agent belongs.
	 * @param perceptionOfProducts initial agent perception of brands 
	 * and attributes.
	 * @param awarenessOfProducts initial agent awareness of brands.
	 * @param neighbors identifiers of the agents that are neighbors of this agent
	 * @param nrBrands number of brands present at the simulation.
	 */
	public Agent(
			double segmentTalking,
			double segmentAwarenessDecay,
			double awarenessImpact,
			double discussionHeatImpact,
			double perceptionSpeed,
			boolean[] initialItems,
			int clientId, 
			int segmentId, 
			double[][] perceptionOfProducts, 
			boolean[] awarenessOfProducts,
			int[] neighbors,
			int nrBrands,
			int nrTouchpoints
		){
		this.segmentAwarenessDecay = segmentAwarenessDecay;		
		this.awarenessImpact = awarenessImpact;
		this.womDiscussionHeatImpact = discussionHeatImpact;
		this.perceptionSpeed = perceptionSpeed;
		this.clientId = clientId;
		this.segmentId = segmentId;
		this.neighbors = neighbors;
		
		//Talking probabilities are initialized to base talking value.
		talkingProbabilities = new double[nrBrands];
		Arrays.fill(talkingProbabilities, segmentTalking);
		baseTalkingProbability = segmentTalking;
		
		perceptions = new double[perceptionOfProducts.length]
				[perceptionOfProducts[0].length];
		for(int i=0; i<perceptionOfProducts.length; i++) {
			for(int j=0; j<perceptionOfProducts[i].length; j++) {
				perceptions[i][j]=perceptionOfProducts[i][j];
			}
		}
		
		awareness = new boolean [nrBrands];
		awarenessCount = 0;
		for(int i=0; i<nrBrands; i++){
			if(awarenessOfProducts[i]) {
				awarenessCount++;
				awareness[i]=true;
			}			
		}
		//When simulation starts, the agent is not in decision cycle.
		inDecisionCycle=false;

		brandPurchased = new boolean [nrBrands];
		hasBrand = initialItems;
		
		//Use planning is not initialized until first use.
		usePlanning=null;
		
		touchpointPerceptionIncrements = new double[nrTouchpoints][][];

		appliedDiscussionHeatImpact = new double [nrTouchpoints][];
	}
	
	// ########################################################################	
	// Get/Set methods
	// ########################################################################

	public boolean[] getAwareness() {
		return awareness;
	}
	
	public void setAwareness(boolean[] awareness) {
		this.awareness=awareness;
	}
	
	public boolean getAwarenessOfBrand(int brandId) {
		return awareness[brandId];
	}
	
	public int getAwarenessCount() {
		return awarenessCount;
	}

	public double[][] getPerceptions() {
		return perceptions;
	}
	
	public double getAttributePerceptionByBrand(int brandid, int att) {
		return perceptions[brandid][att];
	}
	
	public void setPerceptions(double[][] perceptions) {
		this.perceptions=perceptions;
	}
	
	public int[] getNeighbors() {
		return neighbors;
	}
	
	public byte[] getUsePlanning() {
		return usePlanning;
	}
	
	public int[] getWomVolumeByBrand() {
		return womVolumeByBrand;
	}
	
	public int[] getWomVolumeByAtt() {
		return womVolumeByAtt;
	}
	
	public int[] getWomSentimentPos() {
		return womSentimentPos;
	}

	public int[] getWomSentimentNeg() {
		return womSentimentNeg;
	}

	public boolean[] getWomReachByBrand() {
		return womReachByBrand;
	}
	
	public double[][] getWomContributionByBrandByTp() {
		return womContributionByBrandByTp;
	}

	public void setTouchPointExposure(boolean[] exposure) {
		this.exposeToTouchpoints=exposure;
	}

	// ########################################################################	
	// Functionality
	// ########################################################################

	/**
	 * Initializes WoM report statistics.
	 */
	public void enableWoMReports() {
		int nrBrands = awareness.length;
		WOM_REPORTS = true;
		this.womVolumeByBrand = new int[nrBrands];
		this.womVolumeByAtt = new int[perceptions[0].length];
		this.womSentimentPos = new int[nrBrands];
		this.womSentimentNeg = new int[nrBrands];
		this.womReachByBrand = new boolean[nrBrands];
		this.womContributionByBrandByTp = new double [nrBrands][touchpointPerceptionIncrements.length];
	}
	
	/**
	 * Restarts WoM report statistics. Usually used after each step.
	 */
	public void cleanWomReportArrays() {
		for(int i = 0; i < womVolumeByBrand.length; i++) {
			womVolumeByBrand[i] = 0;
			womSentimentPos[i] = 0;
			womSentimentNeg[i] = 0;
			womReachByBrand[i] = false;
			Arrays.fill(womContributionByBrandByTp[i],0.0);
		}
		Arrays.fill(womVolumeByAtt,0);
	}
	
	/**
	 * Checks if agent perceptions were changed by this touch point and 
	 * brand before, creating the array structures if needed.
	 *  
	 * @param touchpoint - the touch point id.
	 * @param brand -  the brand id.
	 */
	public void checkTouchpointPerceptionIncrement(
			int touchpoint,
			int brand
		) {
		
		if(touchpointPerceptionIncrements[touchpoint]==null
			|| touchpointPerceptionIncrements[touchpoint][brand]==null
				) {
			//If the touch point didn't hit the agent before, arrays are created.
			if(touchpointPerceptionIncrements[touchpoint]==null) {
				int brands = perceptions.length;
				touchpointPerceptionIncrements[touchpoint] = new double [brands][];
			}
			if(touchpointPerceptionIncrements[touchpoint][brand]==null) {
				int attributes = perceptions[0].length;
				touchpointPerceptionIncrements[touchpoint][brand] = new double [attributes];
			}
		}
	}
	
	/**
	 * Checks if talking probability was changed by this touch point and
	 * brand before, creating structures if needed.
	 * 
	 * @param touchpoint - touch point id.
	 * @param brand - brand id.
	 */
	private void checkAppliedDiscussionHeat(
			int touchpoint,
			int brand
		) {
		
		//If the touch point didn't hit the agent before, arrays are created.
		if(appliedDiscussionHeatImpact[touchpoint]==null) {
			int brands = perceptions.length;
			appliedDiscussionHeatImpact[touchpoint] = new double [brands];
		}
	}
	
	/**
	 * Retrieves touch point influenced already supplied to given 
	 * attribute's brand.
	 *  
	 * @param touchpoint - touch point id.
	 * @param brand - brand id.
	 * @param attribute - attribute id.
	 * @return the amount of perception change supplied.
	 */
	public double getTouchpointPerceptionInfluenceByAttribute(
			int touchpoint,
			int brand,
			int attribute
		) {

		checkTouchpointPerceptionIncrement(touchpoint, brand);

		return touchpointPerceptionIncrements[touchpoint][brand][attribute];
	}
	
	/**
	 * Changes the perceptions of the mental state of the agent.
	 * @param brand - brand id
	 * @param attribute - attribute id
	 * @param value - perception change
	 * @param touchpoint - touch point id
	 */
	public void changePerceptions(
			int brand, 
			int attribute, 
			double value,
			int touchpoint
		){
		
		/*
		 * Change calculates the amount of perception modified during 
		 * the event (touch point, WoM, etc.). When the final value goes beyond 
		 * the intervals [0, 10], the change is different then the provided value.
		 */
		double change;
		
		if(perceptions[brand][attribute] + value > Functions.PERCEPTION_MAX) {
			change = Functions.PERCEPTION_MAX - perceptions[brand][attribute];
			perceptions[brand][attribute] = Functions.PERCEPTION_MAX;
		} else if(perceptions[brand][attribute] + value < 0.0) {
			change = - perceptions[brand][attribute];
			perceptions[brand][attribute] = 0.0;				
		} else {
			change = value;
			perceptions[brand][attribute] += value;
		}
		
		touchpointPerceptionIncrements[touchpoint][brand][attribute]+=change;
	}
	
	/**
	 * Applies perception decays for given simulation step.
	 * 
	 * @param step - current simulation step
	 */
	private void decayPerceptions(Model m) {
		TouchPointOwnedRegistry tpor = m.getTPORegistry();
		
		double [][] decays = tpor.getPerceptionDecays();
		
		int numTouchpoints = tpor.getNumberOfTouchpoints();
		int numBrands = perceptions.length;
		int numAtts = perceptions[0].length;
		
		for (int tp=0; tp<numTouchpoints; tp++) {
			for (int brand = 0; brand<numBrands; brand++) {
				/*
				 * Different levels of influence are done depending on
				 * media emphasis, thus, decays are computed by attributes.
				 */
				if(touchpointPerceptionIncrements[tp] == null
						|| touchpointPerceptionIncrements[tp][brand] == null) {
					continue;
				}
				for (int att =0; att <numAtts; att++) {
					double increment = touchpointPerceptionIncrements[tp][brand][att];
					double value = (increment) * decays[tp][segmentId];
					
					if(value!=0) {
						/*
						 * Different cases appear when upside down variances appear.
						 */
						if(perceptions[brand][att]-value > Model.MAXIMUM_PERCEPTION_VALUE) {
							value = Model.MAXIMUM_PERCEPTION_VALUE - perceptions[brand][att];
							perceptions[brand][att]= Model.MAXIMUM_PERCEPTION_VALUE;
						} else if(perceptions[brand][att]-value < Model.MINIMUM_PERCEPTION_VALUE) {
							value = perceptions[brand][att] - Model.MINIMUM_PERCEPTION_VALUE;
							perceptions[brand][att]= Model.MINIMUM_PERCEPTION_VALUE;
						} else {
							perceptions[brand][att]-= value;
						}

						touchpointPerceptionIncrements[tp][brand][att]-=value;
						
						if(LOG_INFO) {
							logger.info(
								"Step " + m.getStep() + " PERCEPTION DECAY"
								+ " agent " + clientId 
								+ " segment " + segmentId 
								+ " -->" 
								+ " brand " + brand 
								+ " attribute " + att 
								+ " value " + value
								+ " touchpoint " + tp
							);
						}
					}
				}
			}
		}
	}
	
	/**
	 * (Discussion heat) increases agent talking probability, which
	 * eventually decreases over time.
	 * 
	 * @param step - current simulation step.
	 * @param discussionHeatApplied - previous discussion heat supplied.
	 * @param brandId - brand id
	 * @param tpId - touch point id
	 */
	public void applyDiscussionHeat(
			int step,
			double discussionHeatApplied,
			int brandId,
			int tpId
		) {
		checkAppliedDiscussionHeat(tpId, brandId);
		
		double probabilityIncrement = 
				discussionHeatApplied * baseTalkingProbability;
		
		//Check probability overflow
		if(talkingProbabilities[brandId] + probabilityIncrement
				>= MAXIMUM_TALKING
				) {
			probabilityIncrement = 
					MAXIMUM_TALKING - talkingProbabilities[brandId];
		}
		talkingProbabilities[brandId] += probabilityIncrement;
		appliedDiscussionHeatImpact[tpId][brandId]+=probabilityIncrement;
		
		if(WOM_REPORTS) {
			womContributionByBrandByTp[brandId][tpId] += probabilityIncrement;
		}
		
		if(LOG_DEBUG) {
			logger.debug(
				clientId + " increaseDiscussionHeat()"
				+ discussionHeatApplied + " " + step
			);
		}
	}
	
	/**
	 * Decays (discussion heat) talking probabilities for every brand. 
	 */
	protected void decayDiscussionHeat(Model m) {
		
		//Discussion heat decay values are stored at TouchPointOwnedRegistry.
		double[][] discussionHeatDecays = 
				m.getTPORegistry().getDiscussionHeatDecays();
		
		for (int tp=0; tp<appliedDiscussionHeatImpact.length; tp++) {
			
			if(appliedDiscussionHeatImpact[tp]==null) continue;
			
			for (int brand =0; brand<appliedDiscussionHeatImpact[tp].length; brand ++) {
				double increment = appliedDiscussionHeatImpact[tp][brand];

				double value = (increment) * discussionHeatDecays[tp][segmentId];
				
				appliedDiscussionHeatImpact[tp][brand]-=value;
				
				talkingProbabilities[brand] -= value;
			}			
		}
	}
	
	/**
	 * Models the social network diffusion at the level of agent. 
	 * It checks if the agent talks to his/her neighbors
	 * and if so whether it distributes its awareness and perceptions.
	 * 
	 * @param model - the simulation model object.
	 */
	public void diffusion(Model model) throws NoAwarenessException {
		
		//Clients without neighbors can not start diffusion.
		final int numNeighbors = neighbors.length;
		final int step = model.getStep();
		final double[] stepTalkProbabilities = talkingProbabilities;

		/*
		 * If either the agent has no neighbors or it has null talking probability,
		 * the agent is not able to talk. 
		 */		
		if (numNeighbors == 0 || 
				NumberUtils.max(stepTalkProbabilities) == 0) return;
		
		double r;		
		final Randomizer randomizer = model.random;
		final Agent[] agents = model.getAgents();
		
		if(LOG_DEBUG) {
			String aux = "";
			for(int i = 0; i < neighbors.length; i++) {
				// TODO: [KT] neighbors[i] == agents[neighbors[i]].getClientId()
				aux += " " + neighbors[i] 
					+ " " + agents[neighbors[i]].clientId;
			}
			logger.debug(aux);
		}		
		
		for(int brand = 0; brand < model.getNrBrands(); brand++) {
			// Get random value
			r = randomizer.nextDouble(); // [0, 1)
			
			if(LOG_DEBUG) {
				logger.debug("diffusion() model.getRandomizer().nextDouble() " + r);
				logger.debug(
					clientId+ " " + step +
					 " "+ " => " + stepTalkProbabilities				
				);
			}
			
			// Check if I will talk to neighbors
			if(r < stepTalkProbabilities[brand]) {
				// If agent talks, he does it with all neighbors
				for(int i=0; i<numNeighbors; i++) {
					Agent neighbor = agents[neighbors[i]];
					
					// Discussion Heat
					if(womDiscussionHeatImpact>0.0) {
						neighbor.applyDiscussionHeat(
								step, 
								womDiscussionHeatImpact, 
								brand, 
								AbstractTouchPoint.WOM
							);
					}

					if(LOG_INFO) {
						// Log DM heuristic
						logger.info(
							"Step " + step + " WOM TALKING"
							+ " agent " + clientId + " segment " + segmentId
							+ " --> brand " + brand
							+ " DM heuristic " + model.getDecisionMaking().getLogDM()
						);
					}
					// Diffusion methods (e.g. awareness, perception)
					diffusionAwareness(neighbor, model, brand);
					diffusionPerception(neighbor, model, brand);
				}
			}			
		}
	}

	/**
	 * The diffusion of the awareness (true, false for each product) to the
	 * neighbor of the agent of a given brand.
	 * 
	 * @param neighbor the current neighbor of the agent.
	 * @param model a simulation model object.
	 * @param indexSelectedBrand diffusing brand id.
	 */
	private void diffusionAwareness(
			Agent neighbor, Model model, int indexSelectedBrand) {
		
		// Variables
		double r;
		boolean neighborAwarenessOfProducts = 
			neighbor.getAwarenessOfBrand(indexSelectedBrand);
		
		if(!neighborAwarenessOfProducts) {
			// Let's draw a random value
			r = model.random.nextDouble(); // [0, 1)
			if(LOG_DEBUG) logger.debug(
				"diffusionAwareness() model.getRandomizer().nextDouble() " + r
			);

			String logBuffer = null;
			if(LOG_INFO) logBuffer = 
				"Step " + model.getStep() + " WOM AWARENESS"
				+ " agent " + clientId + " segment " + segmentId 
				+ " -->"
				+ " brand "	+ indexSelectedBrand 
				+ " -->"
				+ " neighbour agent" + neighbor.clientId  
				+ " neighbour segment " + neighbor.segmentId;			
			
			if(r <= awarenessImpact) {
				// If my neighbor doesn't know about the product but I do so,
				// I will make him aware of the product.
				if(awareness[indexSelectedBrand] == true) { 
					neighbor.gainAwareness(model,indexSelectedBrand, model.getStep());
				} else if(LOG_INFO){
					logBuffer += " NEIGHBOUR HAS AWARENESS";				
				}
			// If there is no awareness impact, just log it.	
			} else if(LOG_INFO){
				logBuffer += " NO IMPACT";			
			}

			if(LOG_INFO) logger.info(logBuffer);
		}
	}

	/**
	 * The diffusion of the perception ([1,10] for each product)
	 * of a given brand.
	 * 
	 * @param neighbor the current neighbor of the agent.
	 * @param model a simulation model object. 
	 * @param selectedBrand diffusing brand id.
	 */
	private void diffusionPerception(
			Agent neighbor, Model model, int selectedBrand) {

		String logBuffer = "";	
		boolean hasChangedPerception = false;

		double r = model.random.nextDouble(); // [0, 1)
		final int talkAttribute = Functions.randomWeightedSelection(
			model.getSegments().getDrivers()[segmentId], r
		);
		final double[][] neighborPerceptions = neighbor.perceptions;

		final double sentimentPos = model.getWomSentimentPositive();
		final double sentimentNeg = model.getWomSentimentNegative();
		
		final double influence = 
				model.getSegments().getSegmentInfluenceValue(
						segmentId, neighbor.segmentId
				);
		
		// Perform the following checks:
		// 1. if awareness filter is disabled --> proceed
		// 2. if awareness filter is enabled AND agent has awareness of brand  --> proceed
		// 3. else do not enter the code
		if(
			!model.isAwarenessFilter() || 
			(model.isAwarenessFilter() && awareness[selectedBrand] == true)
		) {
			// Pass your perceptions based on the given equation:
			// Pa(t+1) = Pa(t) + (Pb(t) - Pa(t)) * Rab
					
			// Calculate perception change
			double perceptionChange = (
				perceptions[selectedBrand][talkAttribute]
				- neighborPerceptions[selectedBrand][talkAttribute]
			) * influence;
			
			// Change perceptions
			if (perceptionChange != 0.0) {
				double perception = perceptionChange * perceptionSpeed;
				neighbor.checkTouchpointPerceptionIncrement(
						AbstractTouchPoint.WOM, 
						selectedBrand
					);
				neighbor.changePerceptions(
						selectedBrand, 
						talkAttribute, 
						perception, 
						AbstractTouchPoint.WOM
					);

				hasChangedPerception = true;
				if(LOG_INFO) logBuffer += String.format(
					"[%d=%.3f] ", talkAttribute, perception
				);
			}
			
			if(WOM_REPORTS) {
				// WoM reports - Volume
				neighbor.womVolumeByBrand[selectedBrand]++;
				neighbor.womVolumeByAtt[talkAttribute]++;
				// WoM reports - Sentiment
				if(perceptions[selectedBrand][talkAttribute] 
						>= sentimentPos) {
					neighbor.womSentimentPos[selectedBrand]++;
				} else if (perceptions[selectedBrand][talkAttribute] 
						<= sentimentNeg) {
					neighbor.womSentimentNeg[selectedBrand]++;
				}
				// WoM reports - Reach
				neighbor.womReachByBrand[selectedBrand] = true;
			}
			
			if(LOG_INFO) {			
				logger.info(String.format(
					"Step %d WOM PERCEPTION"
					+ " agent %d(%d) --> " 
					+ " brand %d --> "
					+ " neighbour %d(%d) -->"
					+ " [attribute=perception] %s", 
					model.getStep(), 
					clientId, segmentId, 
					selectedBrand, 
					neighbor.clientId,	
					neighbor.segmentId,
					(hasChangedPerception)? 
						logBuffer : "NO PERCEPTIONS CHANGE"
				));			
			}
		}
	}
	
	/**
	 * Models the awareness decay. It decides if the customer forgets about 
	 * any brand at random (checks all brands).
	 * @param model - a simulation model object.
	 */
	private void decayAwareness(Model model) {
		if(awarenessCount>0) {
			for(int i=0; i<awareness.length; i++) {
				// Check if agent has awareness of the brand
				if(awareness[i]) {
					double r = model.random.nextDouble(); // [0, 1)
					if(LOG_DEBUG) logger.debug(
						"diffusionAwarenessDecay() Randomizer.nextDouble() " + r
					);
					// Check awareness decay
					if(r <= segmentAwarenessDecay) {
						awareness[i] = false;
						awarenessCount--;
						// Remove from scheduler
						if(!inDecisionCycle && awarenessCount==0) {
							model.getSalesScheduler().enabled[segmentId].remove(clientId);
						}
						// Log
						if(LOG_INFO) logger.info(
								"Step " + model.getStep() + " AWARENESS DECAY"
								+ " agent " + clientId + " segment " + segmentId 
								+ " --> brand " + i
						);
					}
				}
			}
		}
	}
	
	/**
	 * Changes awareness of the agent to true, then
	 * the agent may be enabled for
	 * purchasing if it fits certain requirements.
	 * 
	 * @param brandId - the brand gaining awareness
	 * @param step - the step when awareness is being gained.
	 */
	public void gainAwareness(Model m, int brandId, int step) {
		if(!awareness[brandId]) {				
			awareness[brandId] = true;
			if(awarenessCount==0 && !inDecisionCycle) {
				m.getSalesScheduler().enabled[segmentId].add(clientId);
			}
			awarenessCount++;
			if(LOG_INFO) logger.info(
				"Step " + step + " GAINS AWARENESS"
				+ " agent " + clientId + " segment " + segmentId 
				+ " --> brand " + brandId
			);
		}
	}
	
	/**
	 * If the agent bought some brand, it begins its decision cycle
	 * and is excluded until finishing it.
	 */
	public void beginDecisionCycle() {
		inDecisionCycle=true;
		// Purchased brands are now used as an inventory.
		int numBrands = hasBrand.length;
		hasBrand = brandPurchased;
		// Reset purchased brands buffer
		brandPurchased = new boolean [numBrands];
	}
	
	/**
	 * If the agent completed its decision cycle, it may be able to
	 * purchase again.
	 */
	public void endDecisionCycle() {
		inDecisionCycle=false;
	}
	
	/**
	 * Performs a new simulation step and the corresponding actions
	 * (e.g. TPs, WoM, decays, ... etc.).
	 * 
	 * @param model - a simulation model object.
	 */
	public void step(Model model) {
		
		final int step = model.getStep();
		Randomizer random = model.random;
		
		/*
		 * Decays take effect at the beginning of the step.
		 */
		decayPerceptions(model);
		decayAwareness(model);
		decayDiscussionHeat(model);
		
		for (byte b: model.nextModulesOrder()) {
			switch (b) {
				case 0:
					//TouchPointsOwned
					if(exposeToTouchpoints[step]) {
						model.getTPORegistry().executeTPOs(this, step, model);
					}
					break;
				case 1:
					//ProductUsage
					//If customer has any brand
					if(BooleanUtils.or(hasBrand)) {
						/*
						 * If the agent is checking its usage planning for
						 * the first time, its planning algorithm is called.
						 */
						if (usePlanning==null) {
							usePlanning=ProductUsage.generateUsagePlanning(
									model.getUsage().usageFrequency[segmentId], 
									random,
									step,
									model.getMaximumNumberOfSteps(),
									model.getStepsByWeek()
								);
						}
						/*
						 * If more than 0 uses are planned, the agent uses
						 * the product that many times.
						 */
						if (usePlanning[step]>0) {
							model.getUsage().useProduct(
									this, 
									model, 
									step, 
									usePlanning[step]
											);
						}
					}
					break;
				case 2:
					//OnlineReading
					model.getPostReadOnline().readAboutOneBrand(this, model, step);
					break;
				default:
					throw new IllegalStateException(
						"There is no module number " + b
					);
			}
		}
				
		/*
		 * WoM diffusion of the social network:
		 * 1. Changes the given agent's neighbors awareness
		 * 2. Changes the given agent's neighbors perceptions
		 */
		try{
			diffusion(model);
		} catch (NoAwarenessException noAwarenessException) {
			if(LOG_INFO) {
				logger.info(
					"Step " + step + " WOM TALKING"
					+ " agent " + clientId + " segment " + segmentId 
					+ " --> NoAwarenessException", 
					noAwarenessException
				);
				// Log NoAwareness exception thrown.
				logger.debug(
					"Agent " + clientId +" in step " + step
					+" throwed NoAwarenessException while WOM TALKING",
					noAwarenessException
				);
			}
		}
		
		// Post online
		try {
			if(awarenessCount>0) {
				model.getPostReadOnline().postAboutOneBrand(
					awareness, perceptions, model.getSegments().getDrivers()[segmentId],
					segmentId, clientId, random, step, model.isAwarenessFilter()
				);
			}				
		} catch (NoAwarenessException noAwarenessException) {
			if(LOG_INFO) {
				logger.info(
					"Step " + step + " POSTING ONLINE"
					+ " agent " + clientId + " segment " + segmentId 
					+ " --> NoAwarenessException", 
					noAwarenessException
				);
				// Log NoAwareness exception thrown.
				logger.debug(
					"Agent " + clientId +" in step " + step
					+" throwed NoAwarenessException while POSTING ONLINE {}",
					noAwarenessException
				);
			}
		}
	}
	
	/**
	 * If the agent has been selected for buying, chooses which brand
	 * to buy using the decision making module.
	 * 
	 * @throws SalesScheduleError - if the agents has awareness while trying to 
	 * buy, a schedule error is thrown. 
	 */
	public int buyOneBrand(DecisionMaking dm, int step, boolean [] filteredAwareness) throws SalesScheduleError {
		try {
			int indexBought =
				dm.buyOneBrand(filteredAwareness, perceptions, segmentId);
			
			brandPurchased[indexBought] = true;
			
			if(LOG_INFO) logger.info(
				"Step " + step + " PRODUCT PURCHASE"
				+ " agent " + clientId + " segment " + segmentId 
				+ " -->"
				+ " brand " + brandPurchased 
				+ " DM heuristic " + dm.getLogDM()
			);
			
			return indexBought;
		} catch (NoAwarenessException noAwarenessException) {			
			//TODO Exceptions while buying are thrown if no brand is
			//eligible (for example if the agent is not aware of any
			//brand).
			if(LOG_INFO) {
				String toubleMessage=
					"Agent " + clientId +" in step " + step
					+" throwed NoAwarenessException while PRODUCT PURCHASE";
				logger.info(
					"Step " + step + " PRODUCT PURCHASEE"
					+ " agent " + clientId + " segment " + segmentId 
					+ " --> NoAwarenessException", 
					noAwarenessException
				);
				// Log NoAwareness exception thrown.
				logger.debug(toubleMessage, 
						noAwarenessException);
			}
			/*
			 * Failing to schedule sales forces the simulation to stop.
			 */
			throw new SalesScheduleError("Error at sales scheduling: "+noAwarenessException.getMessage());
		}
	}
	
	/**
	 * Which brand is possessed by the agent? This parameter represents
	 * the brand owned by the agent with its brand id. The value "-1"
	 * means that the agent do not have any brand.
	 * 
	 * @return The id of the brand possessed by the agent, with "-1"
	 * as "no brand" value.
	 */
	public boolean[] hasBrand() {
		return hasBrand;
	}
}
