/**
 * 
 */
package calibration.ecj.multimodal;


import ec.*;
import ec.util.*;
import ec.vector.*;

import java.util.ArrayList;
import java.util.Collections;

import calibration.ecj.EcjModelEvaluation;
 
/**
 * Adaptation of the Crowding GA mechanism
 * It includes two rules:
 * Deterministic vs Probabilistic
 * 
 * Dependent of a SUSselectionMethod which requires the fitness having positive values.
 * It was implemented based on MultimodalVectorIndividual so we can use BLX-alpha.
 * This class is based on DoubleVectorIndividual, but it can be used with an IntegerVector if needed
 * 
 * @author ebermejo
 * @email enric2186@gmail.com
 */

public class CrowdingGABreeder extends Breeder {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/*
	 * Generic CRO parameters
	*/
	/*Number of retries*/
	public final static String P_CROSSP="crossover-prob";
	public final static String P_MUTP="mutation-prob";
	public final static String P_RULE="rule";

	public ArrayList<Integer> indexes;
	/*
	 * Specific variables for real-coded operators
	*/ 
	
	/*
	 * Initialize parameters
	*/
	public String rule;
	public double pc,pm;
	//=========================================================================
	//		METHODS
	//=========================================================================
	@Override
	public void setup(EvolutionState state, Parameter base) {	
		//Read parameters, check and initialize with default values if wrong 
		
		rule=state.parameters.getString(base.push(P_RULE),null);
		if(!rule.equals("deterministic") && !rule.equals("probabilistic"))
			state.output.fatal("Parameter not found or its value is not Deterministic or Probabilistic: "+rule, base.push(P_CROSSP),null);
		pc=state.parameters.getDouble(base.push(P_CROSSP), null,0.8);
		if(pc<0)
			state.output.fatal("Parameter not found or its value is less than 0.", base.push(P_CROSSP),null);
		pm=state.parameters.getDouble(base.push(P_MUTP), null,0.2);
		if (pm<0)
			state.output.fatal("Parameter not found or its value is less than 0.", base.push(P_MUTP),null);
 	}
	@Override
	public Population breedPopulation(EvolutionState state) { 
		//Define variables
		if(indexes==null) {
			indexes= new ArrayList<Integer>();
			for (int i=0;i<state.population.subpops[0].individuals.length;i++) 
				indexes.add(i);
		}
		Population newpop=(Population) state.population.emptyClone();
		int size,otherind; 
		Individual parent,otherparent;
		double p1c1,p1c2,p2c1,p2c2;
		//Shuffle the array of indexes that will generate a random paring between individuals
 		Collections.shuffle(indexes);
 		//iterate each individual of the population
		for(int subpop=0;subpop<state.population.subpops.length;subpop++){
			Subpopulation subp=(Subpopulation)state.population.subpops[subpop];
			size=subp.individuals.length;
 			for(int ind=0;ind<size;ind++){
				parent=(Individual) subp.individuals[ind].clone();
				otherind=indexes.get(ind);
				otherparent=(Individual) subp.individuals[otherind].clone();
				if(parent instanceof MMDoubleVectorIndividual) {
					//Crossover to generate both children
					if(state.random[0].nextDouble()<pc)		((MMDoubleVectorIndividual)parent).BLXCrossover(state, 0, (VectorIndividual)otherparent);
					//Mutation
					if(state.random[0].nextDouble()<pm)		((MMDoubleVectorIndividual)parent).defaultMutate(state,0);
					if(state.random[0].nextDouble()<pm)		((MMDoubleVectorIndividual)otherparent).defaultMutate(state,0);
				}else if (parent instanceof MMIntegerVectorIndividual) {//Note that we would use the default distanceto method
					//Crossover to generate both children
					if(state.random[0].nextDouble()<pc)	((MMIntegerVectorIndividual)parent).BLXCrossover(state, 0, (VectorIndividual)otherparent);
					//Mutation
					if(state.random[0].nextDouble()<pm)		((MMIntegerVectorIndividual)parent).defaultMutate(state,0);
					if(state.random[0].nextDouble()<pm)		((MMIntegerVectorIndividual)otherparent).defaultMutate(state,0);
				}

				//Evaluate so we can compare
				((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, parent, 0,0);
				((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, (Individual)otherparent, 0,0);
				
				//Check distances between parents
				if(parent.species instanceof FloatVectorSpecies) {
					p1c1=((MMDoubleVectorIndividual)parent).distanceTo(subp.individuals[ind]);
					p2c1=((MMDoubleVectorIndividual)parent).distanceTo(subp.individuals[otherind]);
					p1c2=((MMDoubleVectorIndividual)otherparent).distanceTo(subp.individuals[ind]);
					p2c2=((MMDoubleVectorIndividual)otherparent).distanceTo(subp.individuals[otherind]);
				}else if(parent.species instanceof IntegerVectorSpecies){
					p1c1=((MMIntegerVectorIndividual)parent).distanceTo(subp.individuals[ind]);
					p2c1=((MMIntegerVectorIndividual)parent).distanceTo(subp.individuals[otherind]);
					p1c2=((MMIntegerVectorIndividual)otherparent).distanceTo(subp.individuals[ind]);
					p2c2=((MMIntegerVectorIndividual)otherparent).distanceTo(subp.individuals[otherind]);
				}else{
					p1c1=parent.distanceTo(subp.individuals[ind]);
					p2c1=parent.distanceTo(subp.individuals[otherind]);
					p1c2=otherparent.distanceTo(subp.individuals[ind]);
					p2c2=otherparent.distanceTo(subp.individuals[otherind]);
				} 
				
				//Pick a replacement match through distance and replace with fitness tournament ---- DETERMINISTIC CROWDING RULE
				if(p1c1+p2c2<p2c1+p1c2) {
					if(rule.equals("deterministic")) {
						if(parent.fitness.betterThan(subp.individuals[ind].fitness) || (parent.fitness.equals(subp.individuals[ind].fitness) && state.random[0].nextBoolean())) 
							subp.individuals[ind]=(Individual) parent.clone(); 
						if(otherparent.fitness.betterThan(subp.individuals[otherind].fitness) || (otherparent.fitness.equals(subp.individuals[otherind].fitness) && state.random[0].nextBoolean()))
							subp.individuals[otherind]=(Individual) otherparent.clone();
					}else if (rule.equals("probabilistic")) {
						double p1 = parent.fitness.fitness() /(subp.individuals[ind].fitness.fitness()+parent.fitness.fitness());
						if(state.random[0].nextBoolean(p1))
							subp.individuals[ind]=(Individual) parent.clone(); 
						double p2 = otherparent.fitness.fitness() /(subp.individuals[otherind].fitness.fitness()+otherparent.fitness.fitness());
						if(state.random[0].nextBoolean(p2))
							subp.individuals[otherind]=(Individual) otherparent.clone(); 
					}
				}else {
					if(rule.equals("deterministic")){
						if(parent.fitness.betterThan(subp.individuals[otherind].fitness) || (parent.fitness.equals(subp.individuals[otherind].fitness) && state.random[0].nextBoolean())) 
							subp.individuals[otherind]=(Individual) parent.clone();
						if(otherparent.fitness.betterThan(subp.individuals[ind].fitness) || (otherparent.fitness.equals(subp.individuals[ind].fitness) && state.random[0].nextBoolean()))
							subp.individuals[ind]=(Individual) otherparent.clone();
					}else if (rule.equals("probabilistic")) {
						double p1 = parent.fitness.fitness() /(subp.individuals[otherind].fitness.fitness()+parent.fitness.fitness());
						if(state.random[0].nextBoolean(p1))
							subp.individuals[otherind]=(Individual) parent.clone(); 
						double p2 = otherparent.fitness.fitness() /(subp.individuals[ind].fitness.fitness()+otherparent.fitness.fitness());
						if(state.random[0].nextBoolean(p2))
							subp.individuals[ind]=(Individual) otherparent.clone(); 
					}
				}

				
			} 
			//We directly replace parents as we clone the previous population into the new one
		 	newpop.subpops[subpop].individuals=subp.individuals.clone();
		
		}
		return newpop;
	}

}
