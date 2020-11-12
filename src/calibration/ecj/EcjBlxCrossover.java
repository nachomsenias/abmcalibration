package calibration.ecj;

import ec.*;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ec.vector.FloatVectorSpecies;
import ec.vector.IntegerVectorIndividual;
import ec.vector.IntegerVectorSpecies;
import ec.vector.breed.VectorCrossoverPipeline;


/**
 * This class is needed for ECJ to work properly.
 * It implements the BLX crossover operator both for real and integer
 * array values.
 * 
 * Given 2 parents, A and B, the BLX-alpha crossover generates two
 * offspring such that each gene i in the offspring is taken as a
 * random value in the interval [min(i) - alpha x IR, max(i) + alpha x IR]
 * where min(i) = min{A(i), B(i)}, max(i) = max{A(i), B(i)}
 * and IR = max(i) - min(i).
 * 
 * In the case in which the genes have an interval of feasible values [MINi, MAXi],
 * we must take into account that the operator can generate values
 * out of the bounds. There are mainly two ways to fix this:
 * 
 * 1. Change the intervla in which we generate the random values to:
 * 		[max{min(i) - alpha x IR, MINi}, min{max(i) + alpha x IR, MAXi}]
 *   
 * 2. Generate the random value and if its larger MAXi (or smaller than MINi),
 * change its value to MAXi (MINi respectively)
 * 
 * Here we take the second approach because the first one makes more
 * difficult to have values at the extremes of the interval of feasible values
 * 
 * @author jjpalacios
 * 
 */


public class EcjBlxCrossover extends VectorCrossoverPipeline
    {
	/**
	 * HINT: Remember than when using ECJ, the recommended way to write text
	 * is to use the state.output.print (println) function. If the second parameter
	 * of this function is 0, it writes in the standard output, and if it is greater
	 * than 1 it writes in a custom log that needs to be defined before
	 * running the algorithm.
	 */
	
		
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Alpha value
	 */
    public static final String P_ALPHA = "alpha";
    public double alpha = -1.0;

	/**
	 * Retries to have different parents
	 */
    public static final String P_RETRIES = "retries";
    public int retries = 0;
    
	
	//=========================================================================
	//		CONSTRUCTORS / INITIALIZERS
	//=========================================================================	
	/**
	 * Only the default constructor could be redefined.
	 */

    /**
	 * Initializing function for ECJ objects. Its structure is fixed
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param base ECJ object that contains the root for parameter names
	 */
    public void setup(final EvolutionState state, final Parameter base) {
        // Call the parent constructor
        super.setup(state,base);
        
        // Looks for the alpha value
        this.alpha = state.parameters.getDoubleWithDefault(base.push(P_ALPHA), null, -1.0);
        
        if(this.alpha < 0)
        	state.output.fatal("Alpha value for crossover not found");
        
        // Looks for the number of retries
        this.retries = state.parameters.getIntWithDefault(base.push(P_RETRIES), null, 0);
    }
	
	
	
	//=========================================================================
	//		METHODS
	//=========================================================================
	/**
	 * BLX-alpha Crossover operator
	 * 
	 * 
	 * @param min Minimum number of solutions to be created.
	 * @param max Maximum number of solutions to be created.
	 * @param start Position of the first parent/offspring
	 * @param subpopulation Population to which the individual belongs.
	 * @param inds Array with the parents/offspring.
	 * @param state Evolution state with the information of the algorithm
	 * @param thread Thread number. Just in case of parallelization.
	 * @return Number of offspring created
	 */
	public int produce(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state, int thread) {
		// Call the source selection method twice
		sources[0].produce(1,1,start,subpopulation,inds,state,thread);
		if(max > 1) {
			sources[1].produce(1,1, start+1,subpopulation,inds,state,thread);
			int tries = 0;
			while(inds[start].fitness.equals(inds[start+1].fitness)
					&& tries < retries) {
				inds[start+1] = null;
				sources[1].produce(1,1, start+1,subpopulation,inds,state,thread);
				tries++;
			}
		}
				
		// If crossover must not be applied...
		if (!state.random[thread].nextBoolean(likelihood)) {
			// DON'T produce children from source -- we already did
			if(max > 1)
				return reproduce(2, start, subpopulation, inds, state, thread, false);
			else 
				return reproduce(1, start, subpopulation, inds, state, thread, false);
		}
		
		// Clone the individuals to modify them. This is needed because 
		// the source breeder is a selection method
		inds[start] = (Individual)inds[start].clone();
		inds[start+1] = (Individual)inds[start+1].clone();
		
		if(inds[start] instanceof IntegerVectorIndividual) {
			if(max > 1)
				return IntegerCrossover(start, 2, subpopulation, inds, state, thread);
			else
				return IntegerCrossover(start, 1, subpopulation, inds, state, thread);
		}
		
		else if(inds[start] instanceof DoubleVectorIndividual) {
			if(max > 1)
				return DoubleCrossover(start, 2, subpopulation, inds, state, thread);
			else
				return DoubleCrossover(start, 1, subpopulation, inds, state, thread);
		}

		return 0;
	}
	
	
	
	/**
	 * Crossover operator for the case of integer values
	 * 
	 * @param start Position of the first parent/offspring
	 * @param subpopulation Population to which the individual belongs.
	 * @param inds Array with the parents/offspring.
	 * @param state Evolution state with the information of the algorithm
	 * @param thread Thread number. Just in case of parallelization.
	 * @return Number of offspring created 
	 */
	public int IntegerCrossover(int start, int n, int subpopulation, Individual[] inds, EvolutionState state, int thread) {		
		int[] ind1 = ((IntegerVectorIndividual)inds[start]).genome;
		int[] ind2 = ((IntegerVectorIndividual)inds[start+1]).genome;
				
		IntegerVectorSpecies species = (IntegerVectorSpecies)state.population.subpops[subpopulation].species;
		
		for(int gene = 0; gene < ind1.length; gene++) {
			double min = Math.min(ind1[gene], ind2[gene]);
			double max = Math.max(ind1[gene], ind2[gene]);
			
			// Compute the bounds of the interval
			int intMin = (int)Math.ceil(min-alpha*(max-min));
			int intMax = (int)Math.floor(max+alpha*(max-min));

			// Generate random values in the interval			
			ind1[gene] = intMin + state.random[thread].nextInt(intMax-intMin+1);
			if(n > 1)
				ind2[gene] = intMin + state.random[thread].nextInt(intMax-intMin+1);

			// Check that the constraints hold
			if(ind1[gene] < species.minGene(gene))
				ind1[gene] = (int)species.minGene(gene);
			else if(ind1[gene] > species.maxGene(gene))
				ind1[gene] = (int)species.maxGene(gene);
			if(ind2[gene] < species.minGene(gene))
				ind2[gene] = (int)species.minGene(gene);
			else if(ind2[gene] > species.maxGene(gene))
				ind2[gene] = (int)species.maxGene(gene);
		}
		
		inds[start].evaluated = false;
		if(n > 1)
			inds[start+1].evaluated = false;
		
		return n;
	}
	

	/**
	 * Crossover operator for the case of double values
	 * 
	 * @param start Position of the first parent/offspring
	 * @param subpopulation Population to which the individual belongs.
	 * @param inds Array with the parents/offspring.
	 * @param state Evolution state with the information of the algorithm
	 * @param thread Thread number. Just in case of parallelization.
	 * @return Number of offspring created 
	 */
	public int DoubleCrossover(int start, int n,  int subpopulation, Individual[] inds, EvolutionState state, int thread) {		
		double[] ind1 = ((DoubleVectorIndividual)inds[start]).genome;
		double[] ind2 = ((DoubleVectorIndividual)inds[start+1]).genome;
				
		FloatVectorSpecies species = (FloatVectorSpecies)state.population.subpops[subpopulation].species;
		/*
		state.output.println("Parents:", 0);
		state.output.println(((DoubleVectorIndividual)inds[start]).genotypeToStringForHumans(), 0);
		state.output.println(((DoubleVectorIndividual)inds[start+1]).genotypeToStringForHumans(), 0);*/
		
		for(int gene = 0; gene < ind1.length; gene++) {
			double min = Math.min(ind1[gene], ind2[gene]);
			double max = Math.max(ind1[gene], ind2[gene]);
			
			// Compute the bounds of the interval
			double minInterval = min-alpha*(max-min);
			double maxInterval = max+alpha*(max-min);
			
			// Generate random values in the interval
			double rnd = state.random[thread].nextDouble(true, true);
			((DoubleVectorIndividual)inds[start]).genome[gene] = minInterval + rnd * (maxInterval - minInterval);
			if(n > 1) {
				rnd = state.random[thread].nextDouble(true, true);
				((DoubleVectorIndividual)inds[start+1]).genome[gene] = minInterval + rnd * (maxInterval - minInterval);
			}

			// Check that the constraints hold
			if(ind1[gene] < species.minGene(gene))
				ind1[gene] = species.minGene(gene);
			else if(ind1[gene] > species.maxGene(gene))
				ind1[gene] = species.maxGene(gene);
			if(ind2[gene] < species.minGene(gene))
				ind2[gene] = species.minGene(gene);
			else if(ind2[gene] > species.maxGene(gene))
				ind2[gene] = species.maxGene(gene);
		}
		
		inds[start].evaluated = false;
		if(n > 1)
			inds[start+1].evaluated = false;
		
		/*
		state.output.println("Offspring:", 0);
		state.output.println(((DoubleVectorIndividual)inds[start]).genotypeToStringForHumans(), 0);
		state.output.println(((DoubleVectorIndividual)inds[start+1]).genotypeToStringForHumans(), 0);*/
		
		return n;
	}	
	
    }
