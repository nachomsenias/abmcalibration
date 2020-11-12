package model.simple;

import model.Model;
import model.customer.Agent;
import model.touchpoints.TouchPointOwned;

public class SimpleTouchPointOwned extends TouchPointOwned {

	public SimpleTouchPointOwned(int id, int numBrands, 
			double[] perceptionSpeed, double[] awarenessImpact, 
			double[] discussionImpact, double[] perceptionPotential, 
			double[] weeklyReachMaximun, double[] annualReachMaximun, 
			double[] annualReachSpeed, InvestmentType investment) {
		super(id, numBrands, perceptionSpeed, awarenessImpact, 
				discussionImpact, perceptionPotential, weeklyReachMaximun,
				annualReachMaximun, annualReachSpeed, investment);
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
	@Override
	protected void modifyAgentStats(
			Agent agent, 
			int hits,			
			int brand,
			int step,
			Model model
		){
		// Check if the customer has awareness of that product
		// If not, process...
		if(!agent.getAwareness()[brand]) {
			checkBrandAwareness(agent, brand, 
					agent.segmentId, hits, model, step);
		}
	}
}
