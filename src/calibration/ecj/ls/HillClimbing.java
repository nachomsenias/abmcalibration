package calibration.ecj.ls;
import ec.EvolutionState;
import ec.Individual;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ec.vector.FloatVectorSpecies;
import ec.vector.IntegerVectorIndividual;
import ec.vector.IntegerVectorSpecies;


/**
 * This class applies hill climbing to a given solution until it
 * cannot be improved anymore or a maximum number of steps is
 * reached.
 * The algorithm may use two different neighborhoods:
 *  single-step: A neighbor is made by incrementing or decrementing in
 * 		one unit, one parameter. Thus, each solution will have 2*n neighbors,
 * 		where n is the length of the genome.
 *  until-change: Similar to the previous one, but if the change in one
 *  	gene has no effect in the fitness, we keep increasing/decreasing
 *  	it until the fitness change or the bounds for the parameter
 *  	are reached. 
 * 
 * @author jjpalacios
 *
 */

public class HillClimbing extends LocalSearch {
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/**
	 * Type of neighborhood to use
	 */
	public static final String P_NEIGHS = "neighborhood";
	private static final String P_NEIGH_TYPE1 = "single-step";
	private static final String P_NEIGH_TYPE2 = "until-change";
	public int neighborhoodType;
	
	/**
	 * Array indicating the possible neighbors. Each position in the
	 * array has the number of a gene. If it is positive, it indicates that
	 * gene must be increased. If it is negative, it has to be decreased.
	 */	
	protected int [] neighborhood;
	
	/**
	 * Next neighbor to build
	 */
	protected int neighborIndex;
	
	//=========================================================================
	//		CONSTRUCTORS / INITIALIZERS
	//=========================================================================
	/**
	 * Default constructor. Nothing to do
	 */
	public HillClimbing() { super(); }
	
	
	/**
	 * Initializing function for ECJ objects. Its structure is fixed
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param base ECJ object that contains the root for parameter names
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		// Call the parent constructor
        super.setup(state,base);
        
        totalEvaluations=0;
        
        // Reads the type of neighbourhood to use
        String neighborhood_str = state.parameters.getString(
        		base.push(P_NEIGHS), null);
        if(neighborhood_str == null) {
        	state.output.fatal("The neighborhood for the hill climbing has "
        			+ "not been specified", null);
        }
        if(neighborhood_str.equalsIgnoreCase(P_NEIGH_TYPE1))
        	neighborhoodType = 1;
        else if(neighborhood_str.equalsIgnoreCase(P_NEIGH_TYPE2))
        	neighborhoodType = 2;
        else {
        	state.output.fatal("Incorrent value for the hill climbing "
        			+ "\'neighborhood\' parameter. Please, use \'single-step\'"
        			+ " or \'until-change\'", null);
    	}
	}
	
	
	
	//=========================================================================
	//		METHODS
	//=========================================================================
	/**
	 * Apply a Local Search algorithm to a given individual.
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param individual Individual to improve through the Local Search
	 * @param subpopulation Subpopulation to which the individual belongs. This is
	 * 	necessary to evaluate it.
	 */
	public void apply(final EvolutionState state,
			Individual individual) {
		state.output.println("Entering Hill Climbing....", 0);
		double currentFitness;
		if(individual.evaluated) {
			currentFitness = individual.fitness.fitness();
		} else {
			((SimpleProblemForm)state.evaluator.p_problem).evaluate(
					state, individual, 0, 0);
			currentFitness = individual.fitness.fitness();
		}
		

		
		prepareNeighborhood(individual);
		
		int iterations = 0;
		boolean stop = false;
		while(!stop && iterations < maxIterations) {
			restartNeighborhood();
			
			// Select a random neighbor
			boolean improves = false;
			Individual neighbor = nextNeighbor(state, individual);
			while(!improves && neighbor != null) {
				if(totalEvaluations >= maxSteps) {
					stop = true;
					break;
				} else if(neighbor.fitness.fitness() >
						individual.fitness.fitness() + threshold) {
					moveToSolution(individual, neighbor);
					improves = true;
				}
				else {
					neighbor = nextNeighbor(state, individual);
				}
			}
			if(!improves)
				stop = true;
			iterations++;
		}
		
		state.output.println("Finishing Hill Climbing....", 0);
		state.output.println("Improving from " + (-currentFitness) + " to "
				+ (-individual.fitness.fitness()), 0);
	}

	
	/**
	 * Prepare the array indicating all the neighbors. Each position in the
	 * array has the number of a gene. If it is positive, it indicates that
	 * gene must be increased. If it is negative, it has to be decreased.
	 * 
	 * @param individual Individual to compute the neighbors of 
	 */
	protected void prepareNeighborhood(Individual individual) {
		int genomeLength = (int)individual.size();
		int numNeighbors = 2 * genomeLength;
		neighborhood = new int[numNeighbors];
		for(int i=0;i < neighborhood.length; i+=2) {
			neighborhood[i] = i/2+1;
			neighborhood[i+1] = -neighborhood[i];
		}
	}
	
	
	/**
	 * Shuffles the array of neighbors so they can be explored
	 * randomly 
	 */
	public void restartNeighborhood() {
		// Shuffle the neighbors
		for(int i=0; i < neighborhood.length; i++) {
			int randInt = state.random[0].nextInt(neighborhood.length);
			int tmpSwap = neighborhood[i];
			neighborhood[i] = neighborhood[randInt];
			neighborhood[randInt] = tmpSwap;
		}
		neighborIndex = 0;
	}

	
	/**
	 * Compute the next neighbor of the current solution to be explored. 
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param individual Individual to improve through the Local Search
	 * @param subpopulation Subpopulation to which the individual belongs. This is
	 * 	necessary to evaluate it.
	 */
	protected Individual nextNeighbor(final EvolutionState state,
			Individual individual) {
		if(neighborhoodType == 1)
			return nextNeighborType1(state, individual);
		else if (neighborhoodType == 2) {
			return nextNeighborType2(state, individual);
		}
		return null;
	}
	
	/**
	 * Compute the next neighbor of the current solution to be explored using
	 * the type one neighborhood. A neighbor is made by incrementing or
	 *  decrementing in	one unit, one parameter. Thus, each solution will
	 *  have 2*n neighbors where n is the length of the genome.
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param individual Individual to improve through the Local Search
	 * @param subpopulation Subpopulation to which the individual belongs. This is
	 * 	necessary to evaluate it.
	 */
	protected Individual nextNeighborType1(final EvolutionState state,
			Individual individual) {
		if(neighborIndex >= neighborhood.length)
			return null;
		
		if(individual instanceof IntegerVectorIndividual) {
			IntegerVectorIndividual neighbor = (IntegerVectorIndividual)individual.clone();
			IntegerVectorSpecies species = (IntegerVectorSpecies)individual.species;
			
			// Increment a parameter
			if(neighborhood[neighborIndex] > 0) {	
				int index = neighborhood[neighborIndex]-1;
				if(neighbor.genome[index] < species.maxGene(index)) {
					neighbor.genome[index]++;
					neighbor.evaluated = false;
					totalEvaluations++;
				}
				// If there is no neighbor, move to the next one
				else {
					neighborIndex++;
					return nextNeighborType1(state, individual);
				}
			}
			
			// Decrement a parameter
			else{
				int index = -(neighborhood[neighborIndex]+1);
				if(neighbor.genome[index] > species.minGene(index)) {
					neighbor.genome[index]--;
					neighbor.evaluated = false;
					totalEvaluations++;
				}
				// If there is no neighbor, move to the next one
				else {
					neighborIndex++;
					return nextNeighborType1(state, individual);
				}
			}
			
			((SimpleProblemForm)state.evaluator.p_problem).evaluate(
					state, neighbor, 0, 0);
			
			neighborIndex++;
			return neighbor;
		}
		
		else if(individual instanceof DoubleVectorIndividual) {
			DoubleVectorIndividual neighbor = (DoubleVectorIndividual)individual.clone();
			FloatVectorSpecies species = (FloatVectorSpecies)individual.species;

			// Increment a parameter
			if(neighborhood[neighborIndex] > 0) {	
				int index = neighborhood[neighborIndex]-1;
				if(neighbor.genome[index] < species.maxGene(index)) {
					neighbor.genome[index] += step;
					neighbor.evaluated = false;
					totalEvaluations++;
				}		
				// If there is no neighbor, move to the next one	
				else {
					neighborIndex++;
					return nextNeighborType1(state, individual);
				}
			}
			
			// Decrement a parameter
			else{
				int index = -(neighborhood[neighborIndex]+1);
				if(neighbor.genome[index] > species.minGene(index)) {
					neighbor.genome[index] -= step;
					neighbor.evaluated = false;
					totalEvaluations++;
				}			
				// If there is no neighbor, move to the next one
				else {
					neighborIndex++;
					return nextNeighborType1(state, individual);
				}
			}
			
			((SimpleProblemForm)state.evaluator.p_problem).evaluate(
					state, neighbor, 0, 0);
			
			neighborIndex++;
			return neighbor;
		}
		
		return null;
	}
		

		
	/**
	 * Compute the next neighbor of the current solution to be explored using
	 * the type two neighborhood. Similar to the previous one, but if the
	 * change in one gene has no effect in the fitness, we keep
	 * increasing/decreasing it until the fitness change or the bounds
	 * for the parameter are reached. 
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param individual Individual to improve through the Local Search
	 * @param subpopulation Subpopulation to which the individual belongs. This is
	 * 	necessary to evaluate it.
	 */	
	protected Individual nextNeighborType2(final EvolutionState state,
			Individual individual) {
		if(neighborIndex >= neighborhood.length)
			return null;
		
		if(individual instanceof IntegerVectorIndividual) {
			IntegerVectorIndividual neighbor = (IntegerVectorIndividual)individual.clone();
			IntegerVectorSpecies species = (IntegerVectorSpecies)individual.species;
			
			boolean change = false;
			while(!change && totalEvaluations<maxSteps) {
				// Increment a parameter
				if(neighborhood[neighborIndex] > 0) {	
					int index = neighborhood[neighborIndex]-1;
					if(neighbor.genome[index] < species.maxGene(index)) {
						neighbor.genome[index]++;
						neighbor.evaluated = false;
						totalEvaluations++;
					}	
					else {
						neighborIndex++;
						return nextNeighborType2(state, individual);
					}
				}
				// Decrement a parameter
				else{
					int index = -(neighborhood[neighborIndex]+1);
					if(neighbor.genome[index] > species.minGene(index)) {
						neighbor.genome[index]--;
						neighbor.evaluated = false;
						totalEvaluations++;
					}	
					else {
						neighborIndex++;
						return nextNeighborType2(state, individual);
					}
				}

				((SimpleProblemForm)state.evaluator.p_problem).evaluate(
						state, neighbor, 0, 0);
				
				if((neighbor.fitness.fitness() >
						individual.fitness.fitness() + threshold)
					|| (neighbor.fitness.fitness() <
							individual.fitness.fitness() - threshold))
						change = true;
			}
			
			neighborIndex++;
			return neighbor;
		}
		
		else if(individual instanceof DoubleVectorIndividual) {
			DoubleVectorIndividual neighbor = (DoubleVectorIndividual)individual.clone();
			FloatVectorSpecies species = (FloatVectorSpecies)individual.species;
			
			boolean change = false;
			while(!change) {
				// Increment a parameter
				if(neighborhood[neighborIndex] > 0) {	
					int index = neighborhood[neighborIndex]-1;
					if(neighbor.genome[index] < species.maxGene(index)) {
						neighbor.genome[index] += step;
						neighbor.evaluated = false;
						totalEvaluations++;
					}				
					else {
						neighborIndex++;
						return nextNeighborType2(state, individual);
					}
				}
				// Decrement a parameter
				else{
					int index = -(neighborhood[neighborIndex]+1);
					if(neighbor.genome[index] > species.minGene(index)) {
						neighbor.genome[index] -= step;
						neighbor.evaluated = false;
						totalEvaluations++;
					}				
					else {
						neighborIndex++;
						return nextNeighborType2(state, individual);
					}
				}
				((SimpleProblemForm)state.evaluator.p_problem).evaluate(
						state, neighbor, 0, 0);
				
				if((neighbor.fitness.fitness() >
					individual.fitness.fitness() + threshold)
				|| (neighbor.fitness.fitness() <
						individual.fitness.fitness() - threshold))
					change = true;
			}
			neighborIndex++;
			return neighbor;
		}
		
		return null;
	}
}
	
	