/**
 * 
 */
package calibration.ecj.CoralReefOptimizer;


import ec.*;
import ec.util.*;
import ec.vector.*;

import java.util.ArrayList;
import java.util.Collections;

import calibration.ecj.EcjModelEvaluation;

/**
 * @author ebermejo
 * @email enric2186@gmail.com
 * Reference: S. Salcedo-Sanz, J. Del Ser, S. Gil-López, I. Landa-Torres and J.
 * A. Portilla-Figueras, "The coral reefs optimization algorithm: an efficient
 * meta-heuristic for solving hard optimization problems," 15th Applied
 * Stochastic Models and Data Analysis International Conference, Mataró, Spain,
 * June, 2013.
 */

public class CROBreeder extends Breeder {
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
	public final static String P_CRO_K= "cro_k";
	/*Percentage of broadcast spawning corals*/
	public final static String P_CRO_FA= "cro_fa";
	/*Percentage of brooder corals*/
	public final static String P_CRO_FB= "cro_fb";
	/*Factor of depredating corals*/
	public final static String P_CRO_FD= "cro_fd";
	/*Probability of depredation*/
	public final static String P_CRO_PD= "cro_pd";
	

	/*
	 * Specific variables for real-coded operators
	*/ 
	
	/*
	 * Initialize parameters
	*/
	public int cro_k;
	public double Fa, Fb, Fd, Pd;

	//=========================================================================
	//		METHODS
	//=========================================================================
	@Override
	public void setup(EvolutionState state, Parameter base) {	
		//Read parameters, check and initialize with default values if wrong
		cro_k=state.parameters.getInt(base.push(P_CRO_K), null,0);
		if (cro_k<0)
			state.output.fatal("Parameter not found or its value is less than 0.", base.push(P_CRO_K),null);
		Fa=state.parameters.getDouble(base.push(P_CRO_FA),null,0.001);
		
		if (Fa<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_FA),null);
		Fb=state.parameters.getDouble(base.push(P_CRO_FB), null,0.001);
		
		if (Fb<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_FB),null);
		Fd=state.parameters.getDouble(base.push(P_CRO_FD), null,0.001);
		
		if (Fd<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_FD),null);
		Pd=state.parameters.getDouble(base.push(P_CRO_PD), null,0.001);
		
		if (Pd<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_PD),null);
		
	}

	/*
	 * Crossover individual id selector, based on the reef location that states if the individual is valid
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param subpop number of population
	 * @param size of the reef
	 * @param actual id of the spawning coral, to avoid reproduction with oneself
	 */
	public int selectrandom(EvolutionState state, int subpop,int size, int actual){
		int other=-1;		
		int rand=-1;
		int loc=-1;
		do{
			rand=state.random[0].nextInt(size);
			if(state.population.subpops[subpop].species instanceof IntegerVectorSpecies)
				loc=((IntegerCoral) state.population.subpops[0].individuals[rand]).getPosition();
			else if(state.population.subpops[subpop].species instanceof FloatVectorSpecies)
				loc=((DoubleCoral) state.population.subpops[0].individuals[rand]).getPosition();
			if(rand!=actual && loc!=-1)
				other=rand;
		}while(other==-1);
		return other;
	
	}
	
	/* Generation of Coral Reefs by reproduction and depredation
	 * @param state ECJ object that contains all the information of the algorithm.
	 */
	
	ArrayList<Individual> performBroadcastSpawning(EvolutionState state, int thread, CoralSubPopulation subp,int numESL) {		
		ArrayList<Individual> larvae=new ArrayList<Individual>(2*numESL);
		Individual spawnerA,spawnerB;
		ArrayList<Integer> spawners, listA,listB;
		
		larvae.ensureCapacity(numESL*2);
		spawners=new ArrayList<Integer>(numESL);
		listA=new ArrayList<Integer>(numESL/2);
		listB=new ArrayList<Integer>(numESL/2);
		int loc=-1;
		for(int i=0;i<subp.individuals.length;i++) {
			if(subp.species instanceof IntegerVectorSpecies) 
				loc=((IntegerCoral) subp.individuals[i]).getPosition();
			else if(subp.species instanceof FloatVectorSpecies)
				loc=((DoubleCoral) subp.individuals[i]).getPosition();
			if(loc!=-1)				spawners.add(i);
			
		}
		Collections.shuffle(spawners);

		for(int i=0;i<numESL;i++) {
			if(i<numESL/2)	listA.add(spawners.get(i));
			else 		listB.add(spawners.get(i));
		}
		Collections.shuffle(listA);
		Collections.shuffle(listB);

		for(int i=0;i<listA.size();i++){
			spawnerA=(Individual) subp.individuals[listA.get(i)].clone();
			spawnerB=(Individual) subp.individuals[listB.get(i)].clone();
			if(subp.species instanceof IntegerVectorSpecies) 
				((IntegerCoral) spawnerA).BLXCrossover(state, thread, (VectorIndividual) spawnerB);
			else if(subp.species instanceof FloatVectorSpecies)
				((DoubleCoral) spawnerA).BLXCrossover(state, thread, (VectorIndividual) spawnerB);

			larvae.add((Individual) spawnerA.clone());
			larvae.add((Individual) spawnerB.clone());
		}
		return larvae;
	
	}
	ArrayList<Individual> performBrooding(EvolutionState state, int thread, CoralSubPopulation subp, int numISL) {		
		ArrayList<Individual> larvae=new ArrayList<Individual>(2*numISL);
		Individual coral;
		ArrayList<Integer> brooders;
		
		larvae.ensureCapacity(numISL);
		brooders=new ArrayList<Integer>(numISL); 
		int loc=-1;
		for(int i=0;i<subp.individuals.length;i++) {
			if(subp.species instanceof IntegerVectorSpecies) 
				loc=((IntegerCoral) subp.individuals[i]).getPosition();
			else if(subp.species instanceof FloatVectorSpecies)
				loc=((DoubleCoral) subp.individuals[i]).getPosition();
			if(loc!=-1)				brooders.add(i);
			
		}
		Collections.shuffle(brooders);
		
		for(int i=0;i<numISL;i++) {
		
			coral=(Individual) subp.individuals[brooders.get(i)].clone();
			if(subp.species instanceof IntegerVectorSpecies) 
				((IntegerCoral) coral).defaultMutate(state, thread);
			else if(subp.species instanceof FloatVectorSpecies)
				((DoubleCoral) coral).Mutate(state, thread,0); //Gaussian Mutation

			larvae.add((Individual) coral.clone());
		}
		return larvae;
	
	}
	
	ArrayList<Individual> performBudding(CoralSubPopulation subp, int numASL) {		
		ArrayList<Individual> larvae=new ArrayList<Individual>(numASL);
		int pindex;
		Integer [] fitindexes;
		larvae.ensureCapacity(numASL);
		fitindexes=subp.sortReefFit();
		int max=fitindexes.length-numASL;
		for(int i=fitindexes.length-1;i>max;i--) {
			pindex=subp.cro_ReefO[fitindexes[i]];
			if(pindex==-1)
				max-=1;
			else
				larvae.add((Individual) subp.individuals[pindex].clone());
		}
		return larvae;
	}
	
	
	void larvaeSetting(EvolutionState state, CoralSubPopulation subp, ArrayList<Individual> larvae) {
		int reefSize=subp.individuals.length;
		int tries,grid,locindex;
		double fit;
		Individual coral;
		
		for(int i=0;i<larvae.size();i++){
		tries=0;
		if(larvae.get(i)!=null){
			coral=(Individual)larvae.get(i).clone();
			while(tries<cro_k){
				grid=state.random[0].nextInt(reefSize);
				if(subp.cro_ReefO[grid]==-1){
					if(subp.species instanceof IntegerVectorSpecies)
						((IntegerCoral)coral).setPosition(grid);
					else if(subp.species instanceof FloatVectorSpecies)
						((DoubleCoral)coral).setPosition(grid);
						
					locindex=subp.cro_EmptyIndvs.get(subp.cro_EmptyIndvs.size()-1);
					subp.cro_ReefO[grid]=locindex;
					subp.cro_ReefFit.set(grid,coral.fitness.fitness());
					subp.cro_EmptyIndvs.remove(subp.cro_EmptyIndvs.size()-1);
					subp.individuals[locindex]=(Individual)coral.clone();
					tries=cro_k;
				}else{
					fit=coral.fitness.fitness(); 
					if(subp.cro_ReefFit.get(grid)<fit){
						if(subp.species instanceof IntegerVectorSpecies)
							((IntegerCoral)coral).setPosition(grid);
						else if(subp.species instanceof FloatVectorSpecies)
							((DoubleCoral)coral).setPosition(grid);
						locindex=subp.cro_ReefO[grid];
						subp.individuals[locindex]=(Individual)coral.clone();
						subp.cro_ReefFit.set(grid, fit);
						tries=cro_k;
					}else tries++;
				}
			}
		}
	}
		
	}
	
	void extremeDepredation(EvolutionState state, CoralSubPopulation subp) {
		int numDep,grid;
		int reefSize=subp.individuals.length;
		Integer[] depindexes; 
		/*** Depredation ***/
		depindexes=null;
		numDep=(int)Math.ceil(this.Fd*reefSize);
		depindexes=subp.sortReefFit();
		int l=-1;

		for(int d=0;d<numDep;d++) {

			if(this.Pd>state.random[0].nextDouble(true, true)){ 
				grid=depindexes[d];
				l=subp.cro_ReefO[grid];
				subp.individuals[l]=subp.species.newIndividual(state, 0); 
				if(subp.species instanceof IntegerVectorSpecies)
					((IntegerCoral)subp.individuals[l]).setPosition(-1);
				else if(subp.species instanceof FloatVectorSpecies)
					((DoubleCoral)subp.individuals[l]).setPosition(-1);
				//Remove information associated with individual
				subp.cro_EmptyIndvs.add(l);
				subp.cro_ReefFit.set(grid,0.0);
				subp.cro_ReefO[grid]=-1;
			}

		}
	}
	
	@Override
	public Population breedPopulation(EvolutionState state) { 
		//Define variables
		Population newpop=(Population) state.population.emptyClone();
		int reefSize;
		int numC,numESL,numISL,numASL; 
		Individual coral;
		ArrayList<Individual> sLarvae, asLarvae, isLarvae, esLarvae;

		//iterate each individual of the population
		for(int subpop=0;subpop<state.population.subpops.length;subpop++){
			CoralSubPopulation subp=(CoralSubPopulation)state.population.subpops[subpop];

			subp.buildReefFit();
			subp.sortEmptyIndexes();
			reefSize=subp.individuals.length;
			numC=reefSize-subp.cro_EmptyIndvs.size();
			numESL=(int) Math.ceil(numC*Fb);
			if(numESL%2 != 0) numESL--;
			numISL=(int) Math.ceil(numC*(1-Fb));
			sLarvae=new ArrayList<Individual>(2*numESL+numISL);


//			esLarvae=new Individual[numESL];
//			isLarvae=new Individual[numISL];
//			sLarvae=new Individual[numESL+numISL];
			
			esLarvae=performBroadcastSpawning(state,0,subp,numESL);
			isLarvae=performBrooding(state,0,subp,numISL);
			
			//Evaluate both sets of larvae
			for (int i=0; i<numESL;i++ ){ //External reproduction evaluation
				coral=(Individual) esLarvae.get(i).clone();		//Get solution
				coral.evaluated=false;
				((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, coral, 0,0);
				sLarvae.add((Individual) coral.clone());
			}				
			for (int i=0; i<numISL;i++ ){ //External reproduction evaluation
				coral=(Individual) isLarvae.get(i).clone();		//Get solution
				coral.evaluated=false;
				((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, coral, 0,0);
				sLarvae.add((Individual) coral.clone());
			}			

			larvaeSetting(state,subp,sLarvae);
			
			
			numC=reefSize-subp.cro_EmptyIndvs.size();
			numASL=(int) Math.ceil(numC*Fa);
			
			asLarvae=performBudding(subp,numASL);
			for (int i=0; i<numASL;i++ ){ //External reproduction evaluation
				coral=(Individual) asLarvae.get(i).clone();		//Get solution
				coral.evaluated=false;
				((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, coral, 0,0);
				asLarvae.add((Individual) coral.clone());
			}	
			larvaeSetting(state,subp,asLarvae);

			extremeDepredation(state,subp);
			
			newpop.subpops[subpop].individuals=subp.individuals.clone();
		
		}
		return newpop;
	}

}
