package calibration.ecj.multimodal.shade;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import ec.*;
import ec.util.*;
import ec.vector.*;

public class SHADEBreeder extends Breeder{
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	
    public static final double CR_UNSPECIFIED = -1;
    
    public int retries = 0;
    
    public static final String P_OUT_OF_BOUNDS_RETRIES = "out-of-bounds-retries";
    public static final String P_BEST_RATE="pbest-rate";
     
    double pbest_rate;
    int memory_size;
    /** the previous population is stored in order to have parents compete directly with their children */
    public Population previousPopulation = null;

    /** the best individuals in each population (required by some DE breeders).  It's not required by DEBreeder's algorithm */
    public int[] bestSoFarIndex = null;
    Individual[] Children;
    ArrayList<Double> Fitnesses; //or ArrayList
    
   
	
    public void setup(final EvolutionState state, final Parameter base) {

        pbest_rate = state.parameters.getDouble(base.push(P_BEST_RATE),null,0.0);
        if ( pbest_rate < 0.0 || pbest_rate > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0,1.0].", base.push(P_BEST_RATE), null );
            
        retries = state.parameters.getInt(base.push(P_OUT_OF_BOUNDS_RETRIES), null, 0);
        if (retries < 0)
            state.output.fatal(" Retries must be a value >= 0.0.", base.push(P_OUT_OF_BOUNDS_RETRIES), null);
        Parameter parameter=  new Parameter("pop.subpop.0.size");
        int pop_size=state.parameters.getInt(parameter, null,0);
        Fitnesses=new ArrayList<Double>(pop_size);
        for(int i=0;i<pop_size;i++)
        	Fitnesses.add(i,-1000.0);
        Parameter parameter2=  new Parameter("pop.subpop.0.species.genome-size");
        memory_size=state.parameters.getInt(parameter2, null,0);
        
    }
    /*
    	Return random value from Cauchy distribution with mean "mu" and variance "gamma"
    	http://www.sat.t.u-tokyo.ac.jp/~omi/random_variables_generation.html#Cauchy
    */
    double cauchy_g(final EvolutionState state, double mu, double gamma) {
    	return mu + gamma * Math.tan(Math.PI*(state.random[0].nextDouble(true, true)  - 0.5));
    }

    /*
    	Return random value from normal distribution with mean "mu" and variance "gamma"
    	http://www.sat.t.u-tokyo.ac.jp/~omi/random_variables_generation.html#Gauss
    */
    double gauss(final EvolutionState state, double mu, double sigma){
      return mu + sigma * Math.sqrt(-2.0 * Math.log(state.random[0].nextDouble(true, true) )) * Math.sin(2.0 * Math.PI * state.random[0].nextDouble(true, true) );
    }
    
    // this function is called just before chldren are to be bred
    public void prepareDEBreeder(EvolutionState state)
        {
        // update the bestSoFar for each population
        if( bestSoFarIndex == null || state.population.subpops.length != bestSoFarIndex.length )
            bestSoFarIndex = new int[state.population.subpops.length];
 
        for( int subpop = 0 ; subpop < state.population.subpops.length ; subpop++ )
            {

            Individual[] inds = state.population.subpops[subpop].individuals;
            bestSoFarIndex[subpop] = 0;
            Fitnesses.clear();
            Fitnesses=new ArrayList<Double>(inds.length);
            this.Fitnesses.add(0,inds[0].fitness.fitness());
            for( int j = 1 ; j < inds.length ; j++ ) {
            	this.Fitnesses.add(j,inds[j].fitness.fitness());
                if( inds[j].fitness.betterThan(inds[bestSoFarIndex[subpop]].fitness) ) 
                    bestSoFarIndex[subpop] = j;
                //	state.output.message(""+bestSoFarIndex[subpop]+inds[bestSoFarIndex[subpop]].fitness.fitness());
                    
            	}
            }
        }
	public Integer[] sortFitnesses(){
		Integer [] indexes =new Integer[this.Fitnesses.size()];

		for (int n=0; n< this.Fitnesses.size();n++){
			indexes[n]=n;
		}

		Arrays.sort(indexes, new Comparator<Integer>() {
		    @Override public int compare(final Integer o1, final Integer o2) {
		        return Double.compare(Fitnesses.get(o1), Fitnesses.get(o2));
		    }
		});
		return indexes.clone();
	}
	
    public Population breedPopulation(EvolutionState state)
        {

        //for new parameters sampling
        double mu_sf, mu_cr;
        int rand_mem_index;
 

        //for current-to-pbest/1
        int p_best_ind;
        int p_num = (int)Math.round(state.population.subpops[0].individuals.length *  this.pbest_rate);
	    if (p_num <= 1)  p_num = 2;
        Integer[] sorted_array = new Integer[state.population.subpops[0].individuals.length];
        memory_size = (int) state.population.subpops[0].individuals[0].size(); //((DoubleVectorIndividual)this.individuals[0]).genomeLength();

        // prepare the breeder (some global statistics might need to be computed here)
        prepareDEBreeder(state);
        sorted_array=sortFitnesses();

        // create the new population
        Population newpop = (Population) state.population.emptyClone();
        for(int i = 0; i < newpop.subpops.length; i++)
        	newpop.subpops[i].individuals = new Individual[state.population.subpops[i].individuals.length];
        
        // breed the children
        for( int subpop = 0 ; subpop < state.population.subpops.length ; subpop++ )
            {
            if (state.population.subpops[subpop].individuals.length < 4)  // Magic number, sorry.  createIndividual() requires at least 4 individuals in the pop
                state.output.fatal("Subpopulation " + subpop + " has fewer than four individuals, and so cannot be used with DEBreeder.");

            SHADESubPopulation subp=(SHADESubPopulation) state.population.subpops[subpop];
            Individual[] inds = subp.individuals;
            for( int target = 0 ; target < inds.length ; target++ )
                {
            	newpop.subpops[subpop].individuals[target]=(DoubleVectorIndividual)subp.individuals[target].clone();
            	rand_mem_index=state.random[0].nextInt(memory_size); 
            	 mu_sf = subp.memory_sf[rand_mem_index];
                 mu_cr = subp.memory_cr[rand_mem_index];
  
                 //generate CR_i and repair its value
                 if (mu_cr == -1) {
                	 subp.pop_cr[target] = 0;
                 }
                 else {
                	 subp.pop_cr[target] = gauss(state,mu_cr, 0.1);
                     if (subp.pop_cr[target] > 1) subp.pop_cr[target] = 1;
                     else if (subp.pop_cr[target] < 0) subp.pop_cr[target] = 0; 
                 }
                
                 //generate F_i and repair its value
                 do {    
                	 subp.pop_sf[target] = cauchy_g(state,mu_sf, 0.1);
                 } while (subp.pop_sf[target] <= 0);
  
                 if (subp.pop_sf[target] > 1) subp.pop_sf[target] = 1;
  
                 //p-best individual is randomly selected from the top pop_size *  p_i members
                 p_best_ind = sorted_array[state.random[0].nextInt(p_num) ];
  
                 operateCurrentToPBest1BinWithArchive(state,subpop,newpop,0, target, p_best_ind, subp.pop_sf[target], subp.pop_cr[target]);
                 newpop.subpops[subpop].individuals[target].evaluated=false;
            	
                //newpop.subpops[subpop].individuals[i] = createIndividual( state, subpop, i, 0);  // unthreaded for now
                }
            }

        // store the current population for competition with the new children
        previousPopulation = state.population;
        return newpop;
        }

    /** Tests the Individual to see if its values are in range. */
    public boolean valid(DoubleVectorIndividual ind)
        {
        //FloatVectorSpecies species = (FloatVectorSpecies)(ind.species);
        return (ind.isInRange());
        }

    void operateCurrentToPBest1BinWithArchive( EvolutionState state,int subpop,Population newpop,int thread, int target, int p_best_individual, double scaling_factor, double cross_rate) {
        int r1, r2;
        Individual[] inds = state.population.subpops[subpop].individuals;
        SHADESubPopulation subp=(SHADESubPopulation) state.population.subpops[subpop];

        int pop_size=inds.length;
        int problem_size=(int) ((DoubleVectorIndividual)inds[0]).genome.length;
         
        do {  r1 = state.random[0].nextInt(pop_size);      			  } while (r1 == target);
        do {  r2 = (state.random[0].nextInt(pop_size + ((SHADESubPopulation)state.population.subpops[subpop]).num_arc_inds));  } while ((r2 == target) || (r2 == r1));
 
        int random_variable =state.random[0].nextInt(problem_size); 
       
        if (r2 >= pop_size) {
            r2 -= pop_size;
            for (int i = 0; i < problem_size; i++) {
            	if ((state.random[0].nextDouble(true, true) < cross_rate) || (i == random_variable)) ((DoubleVectorIndividual)newpop.subpops[subpop].individuals[target]).genome[i] = ((DoubleVectorIndividual)inds[target]).genome[i] + scaling_factor * (((DoubleVectorIndividual)inds[p_best_individual]).genome[i] - ((DoubleVectorIndividual)inds[target]).genome[i]) + scaling_factor * (((DoubleVectorIndividual)inds[r1]).genome[i] - ((DoubleVectorIndividual)subp.Archive[r2]).genome[i]);
            	else ((DoubleVectorIndividual)newpop.subpops[subpop].individuals[target]).genome[i] = ((DoubleVectorIndividual)inds[target]).genome[i];
            }
        }else {
            for (int i = 0; i < problem_size; i++) {
            	if ((state.random[0].nextDouble(true, true) < cross_rate) || (i == random_variable)) ((DoubleVectorIndividual)newpop.subpops[subpop].individuals[target]).genome[i] = ((DoubleVectorIndividual)inds[target]).genome[i] + scaling_factor * (((DoubleVectorIndividual)inds[p_best_individual]).genome[i] - ((DoubleVectorIndividual)inds[target]).genome[i]) + scaling_factor * (((DoubleVectorIndividual)inds[r1]).genome[i] - ((DoubleVectorIndividual)inds[r2]).genome[i]);
            	else ((DoubleVectorIndividual)newpop.subpops[subpop].individuals[target]).genome[i] = ((DoubleVectorIndividual)inds[target]).genome[i];
            }
        }
 
    //If the mutant vector violates bounds, the bound handling method is applied
    // For each dimension j, if the mutant vector element v_j is outside the boundaries [x_min , x_max], we applied this bound handling method
    // If you'd like to know that precisely, please see:
    // J. Zhang and A. C. Sanderson, "JADE: Adaptive differential evolution with optional external archive,"
    // IEEE Tran. Evol. Comput., vol. 13, no. 5, pp. 945–958, 2009.
     
        for (int i = 0; i < problem_size; i++) {
        	double min_region=((FloatVectorSpecies)state.population.subpops[subpop].species).minGene(i);
        	double max_region=((FloatVectorSpecies)state.population.subpops[subpop].species).maxGene(i);
            if (((DoubleVectorIndividual)newpop.subpops[subpop].individuals[target]).genome[i] < min_region) ((DoubleVectorIndividual)newpop.subpops[subpop].individuals[target]).genome[i] = (min_region + ((DoubleVectorIndividual)inds[target]).genome[i]) / 2.0;   
            else if (((DoubleVectorIndividual)newpop.subpops[subpop].individuals[target]).genome[i] > max_region) ((DoubleVectorIndividual)newpop.subpops[subpop].individuals[target]).genome[i] = (max_region + ((DoubleVectorIndividual)inds[target]).genome[i]) / 2.0;        
        }
     //   ((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state,((Individual)newpop.subpops[subpop].individuals[target]), 0,0);
 
    ////////    modifySolutionWithParentMedium(child,  pop[target]);
    }
    
 
    
//    public DoubleVectorIndividual createIndividual(
//        EvolutionState state,
//        int subpop,
//        int index,
//        int thread)
//        {
//        Individual[] inds = state.population.subpops[subpop].individuals;
//
//        DoubleVectorIndividual v = (DoubleVectorIndividual)(state.population.subpops[subpop].species.newIndividual(state, thread));
//        int retry = -1;
//        do
//            {
//            retry++;
//            
//            // select three indexes different from each other and from that of the current parent
//            int r0, r1, r2;
//            do
//                {
//                r0 = state.random[thread].nextInt(inds.length);
//                }
//            while( r0 == index );
//            do
//                {
//                r1 = state.random[thread].nextInt(inds.length);
//                }
//            while( r1 == r0 || r1 == index );
//            do
//                {
//                r2 = state.random[thread].nextInt(inds.length);
//                }
//            while( r2 == r1 || r2 == r0 || r2 == index );
//
//            DoubleVectorIndividual g0 = (DoubleVectorIndividual)(inds[r0]);
//            DoubleVectorIndividual g1 = (DoubleVectorIndividual)(inds[r1]);
//            DoubleVectorIndividual g2 = (DoubleVectorIndividual)(inds[r2]);
//
//            for(int i = 0; i < v.genome.length; i++)
//                v.genome[i] = g0.genome[i] + F * (g1.genome[i] - g2.genome[i]);
//            }
//        while(!valid(v) && retry < retries);
//        if (retry >= retries && !valid(v))  // we reached our maximum
//            {
//            // completely reset and be done with it
//            v.reset(state, thread);
//            }
//
//        return crossover(state, (DoubleVectorIndividual)(inds[index]), v, thread);
//        }

//
//    /** Crosses over child with target, storing the result in child and returning it.  The default
//        procedure copies each value from the target, with independent probability CROSSOVER, into
//        the child.  The crossover guarantees that at least one child value, chosen at random, will
//        not be overwritten.  Override this method to perform some other kind of crossover. */
//                
//    public DoubleVectorIndividual crossover(EvolutionState state, DoubleVectorIndividual target, DoubleVectorIndividual child, int thread)
//        {
//        if (Cr == CR_UNSPECIFIED)
//            state.output.warnOnce("Differential Evolution Parameter cr unspecified.  Assuming cr = 0.5");
//                        
//        // first, hold one value in abeyance
//        int index = state.random[thread].nextInt(child.genome.length);
//        double val = child.genome[index];
//                
//        // do the crossover
//        for(int i = 0; i < child.genome.length; i++)
//            {
//            if (state.random[thread].nextDouble() < Cr)
//                child.genome[i] = target.genome[i];
//            }
//                
//        // reset the one value so it's not just a duplicate copy
//        child.genome[index] = val;
//        
//        return child;
//        }
                        
    


}
