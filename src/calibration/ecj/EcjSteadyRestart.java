package calibration.ecj;
import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Statistics;
import ec.steadystate.SteadyStateEvolutionState;
import ec.steadystate.SteadyStateStatisticsForm;
import ec.util.Parameter;


/**
 * This class is used to make restarts in the population. As we cannot integrate
 * new components into the ecj general schema, we use the Statistics hooks
 * to perform this operation.
 * 
 * In the ecj structure, this object depends on stat.chlid.x
 * 
 * This class takes new parameters from the ecj configuration file
 * 		activate: Indicates weather or not the user wants to take snapshots
 * 		max-restarts: Number of generations between each snapshot
 * 		min-generations: Number of generations between each snapshot
 * 		max-generations: Number of generations between each snapshot
 * 		threshold: Number of generations between each snapshot 
 *   
 * @author jjpalacios
 *
 */

public class EcjSteadyRestart
extends Statistics implements SteadyStateStatisticsForm
    {
	
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Parameter indicating the maximum number of restarts
	 */
	public static final String P_MAX_RESTARTS = "max-restarts";
	public int maxRestarts;
	
	/**
	 * Parameter indicating the minimum number of iterations without restarts
	 */
	public static final String P_MIN_GENS = "min-generations";
	public int minGens;
	
	/**
	 * Parameter indicating the maximum number of iterations without improvement
	 */
	public static final String P_MAX_GENS = "max-generations";
	public int maxGens;
	
	/**
	 * Parameter indicating the threshold for the improvement
	 */
	public static final String P_THRESHOLD = "threshold";
	public double threshold;
	

	/**
	 * Number of restarts performed
	 */
	public int restarts;
	
	/**
	 * Number of iterations without improvement  
	 */
	public int noImprovingGens;
	
	/**
	 * Best fitness found so far  
	 */
	public double bestFitnessFound;
	
	/**
	 * Best individual found so far  
	 */
	public Individual bestIndividual;
	
    
	//=========================================================================
	//		CONSTRUCTORS / INITIALIZERS
	//=========================================================================
	/**
	 * Initializing function for ECJ objects. Its structure is fixed
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param base ECJ object that contains the root for parameter names
	 */
    public void setup(final EvolutionState state, final Parameter base) {
        // Call the parent constructor
        super.setup(state,base);
        
		// Read the maximum number of restarts (infinite if not defined)
		maxRestarts = state.parameters.getIntWithDefault(
				base.push(P_MAX_RESTARTS), null, Integer.MAX_VALUE);
		
		// Read the minimum number of generations without applying restarts
		// (0 by default)
		minGens = state.parameters.getIntWithDefault(
				base.push(P_MIN_GENS), null, 0);

		// Read the maximum number of generations without improvement
		maxGens = state.parameters.getInt(base.push(P_MAX_GENS), null, -1);
		
		// Read the threshold for checking the variations
		threshold = state.parameters.getDoubleWithDefault(
				base.push(P_THRESHOLD), null, 0.0);
		
		// Check that all parameters are right  		
		if(maxRestarts < 0) {
			state.output.fatal("The maximum number of restarts must be a"
					+ " positive number", null);
		}
		  		
		if(minGens < 0) {
			state.output.fatal("The minimum number of generations for the"
					+ " minimum generations must be a positive number", null);
		}
	
		if(maxGens < 0) {
			state.output.fatal("The maximum number of generations without"
					+ " improvement must be a positive value", null);
		}

		if(threshold < 0) {
			state.output.fatal("The threshold must be a"
					+ " positive number", null);
		}
    	
    	restarts = 0;
    	noImprovingGens = 0;
    	bestFitnessFound = -Double.MAX_VALUE;
    	bestIndividual = null; 
    }
    

    	
    	
	//=========================================================================
	//		METHODS
	//=========================================================================
    /**
	 * This function restarts the population if it detects that the algorithm
	 * has not improve the best solution in the last generations
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 */
	public void postEvaluationStatistics(final EvolutionState state)
    {
		// 	be certain to call the hook on super!
		super.postEvaluationStatistics(state);
		
		// If the initial population has not been initialized ...
		// or the total number of generations is reached ...
		if(state.generation == 0 || state.generation == state.numGenerations)
			return;
		
		// If we are not able to count generations yet or the maximum
		// number of restarts has been reached
		if(state.generation < this.minGens || this.restarts >= this.maxRestarts)
			return;
		
		// Look for the best individual in each population
		double currentFitness;
		boolean updated = false;
		
		for(int pop=0; pop < state.population.subpops.length; pop++) {
			int i=0;
			while(!updated && i < state.population.subpops[pop].individuals.length) {
				currentFitness =
						state.population.subpops[pop].individuals[i].fitness.fitness(); 
				if(isGreater(currentFitness, this.bestFitnessFound)) {
					this.bestFitnessFound = currentFitness;
					this.bestIndividual = (Individual)state.population.subpops[pop].individuals[i].clone();
					noImprovingGens = 0;
					updated = true;
				}
				i++;
			}
		}
		
		// If the best solution in the population is not improving
		if(!updated) {
			this.noImprovingGens++;
			
			// If a restart is required
			if(this.noImprovingGens >= this.maxGens) {
				restart(state);
				this.noImprovingGens = 0;
			} else {
				state.output.message("Not improving generation! Restarting population in " 
						+ (maxGens-noImprovingGens) + " generations.");
			}
		}
    }
	
    /**
	 * This function performs a restart in the population
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 */
	private void restart(final EvolutionState state) {
		if(state instanceof SteadyStateEvolutionState) {
			long currentEvals = ((SteadyStateEvolutionState)state).evaluations;
			if(currentEvals<state.numEvaluations) {
				doRestart(state);
			}else {
				state.output.message("Can't restart! Not enought evaluations left ("
						+(state.numEvaluations-currentEvals)+").");
			}
		} else {
			if(state.generation+1<state.numGenerations) {
				// We currently do not know how to include restarts in the breeder 
				// loop for non-steady algorithms.
				state.output.message("Warning :: Restart will consume a full "
						+ "generation instead of (population_size -1) evaluations.");
				doRestart(state);
			} else {
				// Algorithm wont stop if we restart in this case.
				state.output.message("Can't restart! Reached total number of generations.");
			}
		}
	}
	
	private void doRestart(final EvolutionState state) {
		state.output.message("Restarting population...");
		this.restarts++;
		state.output.message("Total restarts: "+restarts);
		Population newPop = state.initializer.initialPopulation(state, 0);
		newPop.subpops[0].individuals[0] = (Individual)this.bestIndividual.clone();
		state.population = newPop;
		state.evaluator.evaluatePopulation(state);
		if(state instanceof SteadyStateEvolutionState) {
			((SteadyStateEvolutionState)state).evaluations+=
					state.population.subpops[0].individuals.length-1;
		}else {
			state.generation+=1;
		}
	}

    /**
	 * This function determines if a given value "a" is better than a given
	 * value "b", for a relative threshold
	 * Because we are minimizing, fitness values are negative. 
	 * 
	 * @param a The value that is supposed to be lesser
	 * @param a The reference value
	 * @return a < b for a given threshold
	 */
	private boolean isGreater(final double a, final double b) {
		if((a * this.threshold) == (b * this.threshold))
			return false;
		return a > b;
	}
		
}
	
	