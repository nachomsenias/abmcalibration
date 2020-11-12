/**
 * 
 */
package calibration.ecj.HM;


import ec.*;
import ec.util.*;

import calibration.ecj.EcjModelEvaluation;
	
/**
 * @author ebermejo
 * @email enric2186@gmail.com
 */
public class HeatMapBreeder extends Breeder {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	
	public final static String P_HM_A= "heat_a";
	public final static String P_HM_B= "heat_b";
	public int heat_a,heat_b;
	//=========================================================================
	//		METHODS
	//=========================================================================
	@Override
	public void setup(EvolutionState state, Parameter base) {	
		//Read parameters, check and initialize with default values if wrong
	heat_a=state.parameters.getInt(base.push(P_HM_A), null,0);
		if (heat_a<0)
			state.output.fatal("Parameter not found or its value is less than 0.", base.push(P_HM_A),null);
	heat_b=state.parameters.getInt(base.push(P_HM_B), null,0);
		if (heat_b<0)
			state.output.fatal("Parameter not found or its value is less than 0.", base.push(P_HM_B),null);
		
	}


	
	/* Generation of Coral Reefs by reproduction and depredation
	 * @param state ECJ object that contains all the information of the algorithm.
	 */
	@Override
	public Population breedPopulation(EvolutionState state) {
        state.output.message("We are breeding folks!");

		//Define variables
		Population newpop=(Population) state.population.emptyClone();
		
		Individual solution;
		
		//iterate each individual of the population
		for(int subpop=0;subpop<state.population.subpops.length;subpop++){
			HMSubPopulation subp=(HMSubPopulation)state.population.subpops[subpop];
			int ind=0;
			int nind=subp.individuals.length;
			subp.fill_iterators(state,heat_a,heat_b,(int)Math.sqrt(nind));
			for(int j=0;j<(int)Math.sqrt(nind);j++){
				for(int k=0;k<(int)Math.sqrt(nind);k++){
					/***Retrieve the individual in the population, find its location in the reef, and calculate the substrate it belongs***/
					solution=(Individual) subp.individuals[ind].clone();
					
					((DoubleHM)solution).onestep(state, 0, heat_a, heat_b, subp.HM_a.get(j), subp.HM_b.get(k));
					solution.evaluated=false;
					((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, solution, 0,0);
					subp.individuals[ind]=(Individual)solution.clone();
					ind+=1;
				} 
			}
			newpop.subpops[subpop].individuals=subp.individuals.clone();
		}
		
		return newpop;
	}

}
