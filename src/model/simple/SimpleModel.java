package model.simple;

import model.Model;
import model.customer.SimpleAgent;
import util.exception.sales.SalesScheduleError;
import util.functions.ArrayFunctions;

public class SimpleModel extends Model{
	
	/**
	 * Agent population as an array.
	 */
	private SimpleAgent[] agents = null;
	
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
	public SimpleModel(
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
		super(stepsByWeek, nrAgents, nrSegments, nrBrands, 
				nrAttributes, nrWeeks, nrTouchpoints, seed, 
				awarenessFilter, womSentimentPositive, 
				womSentimentNegative, agentsRatio);
	}

	/**
	 * Returns agent population as an array.
	 * @return agent population as an array.
	 */
	public SimpleAgent[] getAgents() {
		return agents;
	}

	/**
	 * Sets the agent population to given value.
	 * @param agents the new agent population value.
	 */
	public void setAgents(SimpleAgent[] agents) {
		this.agents = agents;
	}
	
	/**
	 * Runs the model without displaying execution times.
	 * 
	 * @throws SalesScheduleError if a problem was found during sales scheduling.
	 */
	@Override
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
	@Override
	protected void updateStatistics() throws SalesScheduleError {
		if(recordSales) {
			scheduler.assignSales(step, random, statistics, decisionMaking);
		}
		statistics.updateTimeSeries(agents, step);
	}
	
	/**
	 * Enables Word of Mouth reporting for every agent at the population.
	 */
	@Override
	public void enableWoMReports() {
		for (SimpleAgent c: agents) {
			c.enableWoMReports();
		}
	}
}
