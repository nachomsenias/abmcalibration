package model.touchpoints.earned;

import model.Model;
import model.customer.Agent;


/**
 * This abstract class is intended to be extended by any touch point
 * defined.
 * 
 * @author imoya
 *
 */
public abstract class AbstractTouchPoint {
	
	//Earned touch points have reserved identifiers.
	/**
	 * Product usage touch point id.
	 */
	public final static int USE = 0;
	/**
	 * Word of Mouth touch point id.
	 */
	public final static int WOM = 1;
	/**
	 * Posting and Reading Online touch point id.
	 */
	public final static int POST = 2;
	/**
	 * Number of earned touch points: USE+WOM+PRO
	 */
	public final static int NUM_EARNED_TPS = 3;
	
	/**
	 * Perception speed stores perception change after touch point
	 * exposure for every segment.
	 */
	public final double[] perceptionSpeed;

	/**
	 * The weekly probability for the agent to get aware of the brand
	 * after being exposed to the touch point for every segment.
	 */
	public final double[] awarenessImpact;
	
	/**
	 * The percentage increment of the probability to talk after being 
	 * exposed to the touch point for every segment.
	 */
	public final double[] discussionImpact;

	/**
	 * Creates an instance of AbstractTouchPoint.
	 * 
	 * @param perceptionSpeed - perception speed by segment.
	 * @param awarenessImpact - awareness impact by segment.
	 * @param discussionImpact - discussion heat by segment.
	 */
	public AbstractTouchPoint(
			double[] perceptionSpeed,
			double[] awarenessImpact,
			double[] discussionImpact
		) {
		
		this.perceptionSpeed = perceptionSpeed;
		this.awarenessImpact = awarenessImpact;
		this.discussionImpact = discussionImpact;
	}
	
	/**
	 * Touch points modify agents perceptions, awareness and talking probabilities.
	 * 		
	 * @param customer - agent checking if is going to get affected 
	 * by the touch point.
	 * @param m current model instance.
	 * @param step current simulation step.
	 */
	public abstract void modifyAgent(Agent customer, Model m, int step, int brandId);

	/**
	 * Touch point influence may make the agent aware of the brand.
	 * 
	 * @param customer The agent exposed to the touch point.
	 * @param random current simulation randomizer.
	 * @param brandid The brand behind the touch point influence.
	 * @param awarenessProbability awareness probability for checking 
	 * touch point.
	 * @param step current simulation step.
	 * @return True if the agent became aware if the brand. If not,
	 * return False.
	 */
	protected boolean checkAwarenessImpact(
			Agent customer, 
			Model m, 
			int brandid, 
			double awarenessProbability,
			int step
		) {
		/**
		 * If the awareness impact is over 100%, 
		 * there is no need to roll.
		 */
		if(awarenessProbability>=1.0) {
			customer.gainAwareness(m,brandid, step);
			return true;
		} else {
			double roll = m.random.nextDouble();
			if(roll<awarenessProbability) {
				customer.gainAwareness(m,brandid, step);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the agent will gain awareness based on the number of hits 
	 * received and the awareness impact for every hit.
	 * 
	 * If the agent already has awareness of the brand, no probability is
	 * checked.
	 * 
	 * @param agent - the agent checking awareness gain
	 * @param brand - the whose awareness is being checked
	 * @param segment - agent's segment id
	 * @param hits - number of exposures 
	 * @param random - current simulation randomizer
	 * @param step - current simulation step
	 * @return if the agent became aware of the brand
	 */
	protected boolean checkBrandAwareness(
			Agent agent,
			int brand,
			int segment,
			int hits,
			Model m,
			int step) 
	{
		boolean awareOf=agent.getAwarenessOfBrand(brand);
		if(!awareOf) {
			//For each time the agent is exposed to the touch point, 
			//its probability of becoming aware of the brand increases.
			double awarenessProbability = hits * awarenessImpact[segment];
			awareOf=checkAwarenessImpact(agent, m, brand, awarenessProbability, step);
		}
		return awareOf;
	}
}
