package model.customer;

import org.apache.commons.lang3.math.NumberUtils;

import model.decisionmaking.DecisionMaking;
import model.simple.SimpleModel;
import model.touchpoints.earned.AbstractTouchPoint;
import util.exception.sales.SalesScheduleError;
import util.exception.simulation.NoAwarenessException;
import util.functions.ArrayFunctions;
import util.random.Randomizer;

public class SimpleAgent extends Agent{
	
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
	public SimpleAgent(
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
		super(segmentTalking,
			segmentAwarenessDecay,
			awarenessImpact,
			discussionHeatImpact,
			perceptionSpeed,
			initialItems,
			clientId, 
			segmentId, 
			perceptionOfProducts, 
			awarenessOfProducts,
			neighbors,
			nrBrands,
			nrTouchpoints);
	}
	
	// ########################################################################	
	// Get/Set methods
	// ########################################################################

	public void setTouchPointExposure(boolean[] exposure) {
		this.exposeToTouchpoints=exposure;
	}

	// ########################################################################	
	// Functionality
	// ########################################################################
	
	/**
	 * Models the social network diffusion at the level of agent. 
	 * It checks if the agent talks to his/her neighbors
	 * and if so whether it distributes its awareness and perceptions.
	 * 
	 * @param model - the simulation model object.
	 */
	public void diffusion(SimpleModel model) throws NoAwarenessException {
		
		//Clients without neighbors can not start diffusion.
		final int numNeighbors = neighbors.length;
		final double[] stepTalkProbabilities = talkingProbabilities;

		/*
		 * If either the agent has no neighbors or it has null talking probability,
		 * the agent is not able to talk. 
		 */		
		if (numNeighbors == 0 || 
				NumberUtils.max(stepTalkProbabilities) == 0) return;
		
		double r;		
		final Randomizer randomizer = model.random;
		final SimpleAgent[] agents = model.getAgents();
		
		for(int brand = 0; brand < model.getNrBrands(); brand++) {
			
			// If the agent does not have awareness of the brand, 
			// it should not spread awareness.
			if(!awareness[brand]) {
				continue;
			}
			
			// Get random value
			r = randomizer.nextDouble(); // [0, 1)
			
			// Check if I will talk to neighbors
			if(r < stepTalkProbabilities[brand]) {
				// If agent talks, he does it with all neighbors
				for(int i=0; i<numNeighbors; i++) {
					SimpleAgent neighbor = agents[neighbors[i]];

					// Discussion Heat
					if(womDiscussionHeatImpact>0.0) {
						neighbor.applyDiscussionHeat(
								model.getStep(), 
								womDiscussionHeatImpact, 
								brand, 
								AbstractTouchPoint.WOM
							);
					}
					
					// Diffusion methods (e.g. awareness, perception)
					diffusionAwareness(neighbor, model, brand);
					
					if(WOM_REPORTS) {
						// WoM reports - Volume
						neighbor.womVolumeByBrand[brand]++;
						// WoM reports - Sentiment
						
						// WoM reports - Reach
						neighbor.womReachByBrand[brand] = true;
					}
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
			SimpleAgent neighbor, SimpleModel model, int indexSelectedBrand) {
		
		// Variables
		double r;
		boolean neighborAwarenessOfProducts = 
			neighbor.getAwarenessOfBrand(indexSelectedBrand);
		
		if(!neighborAwarenessOfProducts) {
			// Let's draw a random value
			r = model.random.nextDouble(); // [0, 1)
			
			if(r <= awarenessImpact) {
				// If my neighbor doesn't know about the product but I do so,
				// I will make him aware of the product.
				if(awareness[indexSelectedBrand] == true) { 
					neighbor.gainAwareness(model,indexSelectedBrand, model.getStep());
				}
			}


		}
	}
	
	/**
	 * Models the awareness decay. It decides if the customer forgets about 
	 * any brand at random (checks all brands).
	 * @param model - a simulation model object.
	 */
	private void decayAwareness(SimpleModel model) {
		if(awarenessCount>0) {
			for(int i=0; i<awareness.length; i++) {
				// Check if agent has awareness of the brand
				if(awareness[i]) {
					double r = model.random.nextDouble(); // [0, 1)

					// Check awareness decay
					if(r <= segmentAwarenessDecay) {
						awareness[i] = false;
						awarenessCount--;
						// Remove from scheduler
						if(!inDecisionCycle && awarenessCount==0) {
							model.getSalesScheduler().enabled[segmentId].remove(clientId);
						}
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
	public void gainAwareness(SimpleModel m, int brandId, int step) {
		if(!awareness[brandId]) {				
			awareness[brandId] = true;
			if(awarenessCount==0 && !inDecisionCycle) {
				m.getSalesScheduler().enabled[segmentId].add(clientId);
			}
			awarenessCount++;
		}
	}
	
	/**
	 * Performs a new simulation step and the corresponding actions
	 * (e.g. TPs, WoM, decays, ... etc.).
	 * 
	 * @param model - a simulation model object.
	 */
	public void step(SimpleModel model) {
		
		final int step = model.getStep();
		
		/*
		 * Decays take effect at the beginning of the step.
		 */
		decayAwareness(model);
		decayDiscussionHeat(model);
		
		if(exposeToTouchpoints[step]) {
			model.getTPORegistry().executeTPOs(this, step, model);
		}
				
		/*
		 * WoM diffusion of the social network:
		 * 1. Changes the given agent's neighbors awareness
		 * 2. Changes the given agent's neighbors perceptions
		 */
		try{
			diffusion(model);
		} catch (NoAwarenessException noAwarenessException) {
			System.out.println(noAwarenessException.getMessage());
			noAwarenessException.printStackTrace();
		}
	}
	
	/**
	 * If the agent has been selected for buying, chooses which brand
	 * to buy using the decision making module.
	 * 
	 * @throws SalesScheduleError - if the agents has awareness while trying to 
	 * buy, a schedule error is thrown. 
	 */
	public int buyRandom(Randomizer random) throws SalesScheduleError {
		int indexBought =
			ArrayFunctions.selectRandomIndex(awareness, random);
		
		brandPurchased[indexBought] = true;
		
		return indexBought;
	}
	
	/**
	 * If the agent has been selected for buying, chooses which brand
	 * to buy using the decision making module.
	 * 
	 * @throws SalesScheduleError - if the agents has awareness while trying to 
	 * buy, a schedule error is thrown. 
	 */
	@Override
	public int buyOneBrand(DecisionMaking dm, int step, 
			boolean [] filteredAwareness) throws SalesScheduleError {
		int indexBought = dm.buyRandom(awareness);
		
		brandPurchased[indexBought] = true;
					
		return indexBought;
	}
}
