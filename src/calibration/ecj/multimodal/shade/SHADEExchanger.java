/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package calibration.ecj.multimodal.shade;
import ec.EvolutionState;
import ec.Population;
import ec.Exchanger;
import ec.Fitness;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ec.steadystate.*;
import ec.Individual;

/* 
 * SimpleExchanger.java
 * 
 * Created: Tue Aug 10 21:59:17 1999
 * By: Sean Luke
 */

/**
 * A SimpleExchanger is a default Exchanger which, well, doesn't do anything.
 * Most applications don't need Exchanger facilities; this simple version
 * will suffice.
 * 
 * <p>The SimpleExchanger implements the SteadyStateExchangerForm, mostly
 * because it does nothing with individuals.  For this reason, it is final;
 * implement your own Exchanger if you need to do something more advanced.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public final class SHADEExchanger extends Exchanger implements SteadyStateExchangerForm
    {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Population children;
    public static final String P_SHADE_VARIANT= "variant";
    String method;
    int initial_pop_size;
    int current_evaluation;
    public void setup(final EvolutionState state, final Parameter base) {
    	
    	method=state.parameters.getString(base.push(P_SHADE_VARIANT), null);
    	if (method==null || (method.equals("SHADE") && method.equals("LSHADE")))
    		 state.output.fatal(" Exchanger.variant variable should note SHADE or LSHADE");
    	Parameter parameter=  new Parameter("pop.subpop.0.size");
        initial_pop_size=state.parameters.getInt(parameter, null,0);
    }

    /** Doesn't do anything. */
    public void initializeContacts(final EvolutionState state)
        {
        // don't care
        return;
        }

    public void setcurrentEvaluation(int n) {
    	this.current_evaluation=n;
    }
    /** Doesn't do anything. */
    public void reinitializeContacts(final EvolutionState state)
        {
        // don't care
        return;
        }

    /** Simply returns state.population. */
    public Population preBreedingExchangePopulation(final EvolutionState state)
        {
        // don't care
        return state.population;
        }
    
    public void setChildrenPop(Population newpopulation) {
    	this.children=newpopulation;
    }

    /** Simply returns state.population. */
    public Population postBreedingExchangePopulation(final EvolutionState state)
        {
        // we care here
    	Population pop = state.population;
    	Individual[] children_inds;
    	Individual[] pop_inds;
    	int min_pop_size=4;
    	int next_pop_size,num_redu_inds;
   	 	//memory index counter
        int memory_pos = 0;
        int rand_arc_ind;
        double temp_sum_sf1, temp_sum_sf2, temp_sum_cr1, temp_sum_cr2, temp_sum, temp_weight;
        int num_success_params = 0;
        for( int subpop = 0 ; subpop < state.population.subpops.length ; subpop++ ) {
            SHADESubPopulation subp=(SHADESubPopulation) state.population.subpops[subpop];
        	double [] dif_fitness = new double[subp.individuals.length];
        	children_inds=this.children.subpops[subpop].individuals;
        	pop_inds=pop.subpops[subpop].individuals;
        	int memory_size=((DoubleVectorIndividual)pop_inds[0]).genomeLength();

 			//generation alternation
	        for (int i = 0; i < pop.subpops[subpop].individuals.length; i++) { 
	            if (children_inds[i].fitness.equivalentTo( pop_inds[i].fitness) ){ //Reemplazo
	            	pop_inds[i]=(Individual) children_inds[i].clone();//Clone genome
	            	pop_inds[i].fitness=(Fitness) children_inds[i].fitness.clone(); //clone fitness
	            }else if (children_inds[i].fitness.betterThan(pop_inds[i].fitness)) {//Archivo pop y reemplazo
	                //parent vectors x_i which were worse than the trial vectors u_i are preserved
	                if (subp.arc_size > 1) { 
	                    if (subp.num_arc_inds < subp.arc_size) {
	                    	subp.Archive[subp.num_arc_inds]=(Individual)pop_inds[i].clone();
	                    	subp.Archive[subp.num_arc_inds].fitness=(Fitness)pop_inds[i].fitness.clone();
	                    	subp.num_arc_inds++;  
	                    }
	                    //Whenever the size of the archive exceeds, randomly selected elements are deleted to make space for the newly 						inserted elements
	                    else {
	                        rand_arc_ind = state.random[0].nextInt(subp.arc_size); 
	                    	subp.Archive[rand_arc_ind]=(Individual)pop_inds[i].clone();
	                    	subp.Archive[rand_arc_ind].fitness=(Fitness)pop_inds[i].fitness.clone();
 	                    }
	                }
	
	                dif_fitness[num_success_params] = Math.abs(pop_inds[i].fitness.fitness() - children_inds[i].fitness.fitness());
	                pop_inds[i].fitness=(Fitness)children_inds[i].fitness.clone();
	            	pop_inds[i]=(Individual) children_inds[i].clone();//Clone genome
	                //successful parameters are preserved in S_F and S_CR
	                subp.success_sf[num_success_params] = subp.pop_sf[i];
	                subp.success_cr[num_success_params] = subp.pop_cr[i];
	                num_success_params++;
	            }
	            pop_inds[i].evaluated=true;
	        }
	   
	        if (num_success_params > 0) {
	            temp_sum_sf1 = 0;
	            temp_sum_sf2 = 0;
	            temp_sum_cr1 = 0;
	            temp_sum_cr2 = 0;
	            temp_sum = 0;
	            temp_weight = 0;
	
	            for (int i = 0; i < num_success_params; i++) temp_sum += dif_fitness[i];
	           
	            //weighted lehmer mean
	            for (int i = 0; i < num_success_params; i++) {
	                temp_weight = dif_fitness[i] / temp_sum;
	
	                temp_sum_sf1 += temp_weight * subp.success_sf[i] * subp.success_sf[i];
	                temp_sum_sf2 += temp_weight * subp.success_sf[i];
	
	                temp_sum_cr1 += temp_weight * subp.success_cr[i] * subp.success_cr[i];
	                temp_sum_cr2 += temp_weight * subp.success_cr[i];
	            }
	
	            subp.memory_sf[memory_pos] = temp_sum_sf1 / temp_sum_sf2;
	
	            if (temp_sum_cr2 == 0 || subp.memory_cr[memory_pos] == -1) subp.memory_cr[memory_pos] = -1;
	            else subp.memory_cr[memory_pos] = temp_sum_cr1 / temp_sum_cr2;
	
	            //increment the counter
	            memory_pos++;
	            if (memory_pos >= memory_size) memory_pos = 0;
	        }
	        subp.individuals=pop_inds;
	        
	        if(method.equals("LSHADE")) {
	        	int pop_size=subp.individuals.length;
	        	next_pop_size = (int)Math.round((((min_pop_size - initial_pop_size) / (double)state.numEvaluations) * current_evaluation) + initial_pop_size);
	 	         if (pop_size > next_pop_size) {
	 		        num_redu_inds = pop_size - next_pop_size;
	 		        if ((pop_size - num_redu_inds) <  min_pop_size) num_redu_inds = pop_size - min_pop_size;
	 		        ((SHADESubPopulation)subp).reducePopulationWithSort(num_redu_inds);
	 	         }
	        }
 
        }
        return pop;
        }

    /** Doesn't do anything. */
    public void closeContacts(final EvolutionState state, final int result)
        {
        // don't care
        return;
        }

    /** Always returns null */
    public String runComplete(final EvolutionState state)
        {
        return null;
        }

    }
