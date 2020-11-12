/**
 * 
 */
package calibration.ecj.CoralReefOptimizer;


import ec.*;
import ec.util.*;
import ec.vector.*;

import java.io.File;
import java.io.IOException;

import calibration.ecj.EcjModelEvaluation;

/**
 * @author ebermejo
 * @email enric2186@gmail.com
 */
public class CROSLBreeder extends Breeder {
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
	/*Harmonic crossover memory considering rate*/
	public final static String P_CRO_HMCR = "cro_hmcr";
	/*Harmonic crossover pitch adjusting rate*/
	public final static String P_CRO_PAR= "cro_par";
	/*Harmony crossover delta*/
	public final static String P_CRO_DELTA= "cro_delta";
	/*Differential crossover sigma*/
	public final static String P_CRO_CR= "cro_cr";
	/*Differential crossover function rate*/
	public final static String P_CRO_FUNC = "cro_func";
	/*
	 * Initialize parameters
	*/
	public int cro_k;
	public double Fa, Fb, Fd, Pd;
	public double HMCR, PAR;
	public double delta, cr, func;
	
	public int s0,s1,s2,s3;
	
	private int larvaeLog;

	//=========================================================================
	//		METHODS
	//=========================================================================
	@Override
	public void setup(EvolutionState state, Parameter base) {	
		//Read parameters, check and initialize with default values if wrong
		cro_k=state.parameters.getInt(base.push(P_CRO_K), null,0);
		if (cro_k<0)
			state.output.fatal("Parameter not found or its value is less than 0.", base.push(P_CRO_K),null);
		Fa=state.parameters.getDouble(base.push(P_CRO_FA),null,0);
		
		if (Fa<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_FA),null);
		Fb=state.parameters.getDouble(base.push(P_CRO_FB), null,0.01);
		
		if (Fb<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_FB),null);
		Fd=state.parameters.getDouble(base.push(P_CRO_FD), null,0.01);
		
		if (Fd<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_FD),null);
		Pd=state.parameters.getDouble(base.push(P_CRO_PD), null,0.01);
		
		if (Pd<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_PD),null);
		HMCR=state.parameters.getDouble(base.push(P_CRO_HMCR), null,0);
		
		if (HMCR<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_HMCR),null);
		PAR=state.parameters.getDouble(base.push(P_CRO_PAR), null,0);
		
		if (PAR<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_PAR),null);
		delta=state.parameters.getDouble(base.push(P_CRO_DELTA), null,0);
		
		if (delta<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_DELTA),null);
		cr=state.parameters.getDouble(base.push(P_CRO_CR), null,0.0001);
		
		if (cr<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_CR),null);
		func=state.parameters.getDouble(base.push(P_CRO_FUNC), null,0.001);
		
		if (func<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_CRO_FUNC),null);	
		
		s0=0;	s1=0;	s2=0;	s3=0;//Esto es nuevo
		
		// Initialize logs
		larvaeLog = -1;
		try {
			String path = state.parameters.getProperty("stat.file").split(".stat")[0];
			larvaeLog = state.output.addLog(new File(path+"_larvaeLog.csv"), false);
			//Print header
			state.output.println("Best;S0;S1;S2;S3;P0;P1;P2;P3", larvaeLog);
			
		} catch (IOException e) {
			System.out.println("Failed to create larvae log!");
			e.printStackTrace();
		}
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
	@Override
	public Population breedPopulation(EvolutionState state) { 
		//Define variables
		Population newpop=(Population) state.population.emptyClone();
		int reefSize,loc,sub,otherindex,tries,grid,locindex,numDep;
		double fit;
		Individual coral,otherparent;
		Individual[] Slarvae;
		Integer[] depindexes;
		float sectionSize;
		int numberOfSubstrates=4;
		Fitness minfit_gen=(Fitness) state.population.subpops[0].individuals[0].fitness.clone();//Esto es nuevo
		int minind_gen=0,minsus_gen=0;//Esto es nuevo
		//iterate each individual of the population
		for(int subpop=0;subpop<state.population.subpops.length;subpop++){
			CoralSubPopulation subp=(CoralSubPopulation)state.population.subpops[subpop];
			if(subp.species instanceof IntegerVectorSpecies)
				numberOfSubstrates=4;
			else
				numberOfSubstrates=3;
			subp.buildReefFit();
			subp.sortEmptyIndexes();
			reefSize=subp.individuals.length;

			sectionSize=(int)Math.floor((float)reefSize/numberOfSubstrates);
			Slarvae=new Individual[reefSize];
			for(int ind=0;ind<reefSize;ind++){
				/***Retrieve the individual in the population, find its location in the reef, and calculate the substrate it belongs***/
				loc=-1;
				coral=(Individual) subp.individuals[ind].clone();
			
				if(subp.species instanceof IntegerVectorSpecies) 
					loc=((IntegerCoral) coral).getPosition();
				else if(subp.species instanceof FloatVectorSpecies)
					loc=((DoubleCoral) coral).getPosition();
				if(loc!=-1){
					sub=(int)Math.floor((float)loc/sectionSize);
					if(sub>=numberOfSubstrates) sub=numberOfSubstrates-1;

					
					otherparent=null;
					if(subp.species instanceof IntegerVectorSpecies){
						/***If not broadcasting -> reset mutation of the larvae***/
						if(Fb<=state.random[0].nextDouble(true, true))		 ((IntegerCoral)coral).defaultMutate(state, 0);
						else switch(sub){
						case 0:		//Crossover 0 BLX-a
							otherindex=selectrandom(state,subpop,reefSize,ind);
							otherparent=(VectorIndividual)subp.individuals[otherindex].clone();
							((IntegerCoral)coral).BLXCrossover(state, 0, (VectorIndividual)otherparent);
							break;
//						case 1:		//Crossover 1 OnePoint
//							otherindex=selectrandom(state,subpop,reefSize,ind);
//							otherparent=(VectorIndividual)subp.individuals[otherindex].clone();
//							((IntegerCoral)coral).OnePointCrossover(state,0,(VectorIndividual)otherparent);
//							break;
//						case 2:		//Crossover 2 LineRecomb
//							otherindex=selectrandom(state,subpop,reefSize,ind);
//							otherparent=(IntegerVectorIndividual)subp.individuals[otherindex].clone();
//							((IntegerCoral)coral).LineRecombCrossover(state,0,(IntegerVectorIndividual)otherparent);							
//							break;
						case 1:		//Crossover 3 SBX
							otherindex=selectrandom(state,subpop,reefSize,ind);
							otherparent=(VectorIndividual)subp.individuals[otherindex].clone();
							((IntegerCoral)coral).SBXCrossover(state, 0, (IntegerVectorIndividual)otherparent);
							break;
						case 2:		//Genetic Mutation
							((IntegerCoral)coral).defaultMutate(state, 0);

							break;
						case 3://RandomWalkMutation Mutation
							
							((IntegerCoral)coral).RandomWalkMutation(state,0);
							break;
						default:
							state.output.warning("Substrate index out of bounds, check everything");
							break;
						}
						
					 }else if(subp.species instanceof FloatVectorSpecies){
						if(Fb<=state.random[0].nextDouble(true, true)){
							 ((DoubleCoral)coral).Mutate(state, 0, 3);
						}else switch(sub){
						case 0:		//Crossover 0 BLX-a
							otherindex=selectrandom(state,subpop,reefSize,ind);
							otherparent=(VectorIndividual)subp.individuals[otherindex].clone();
							((DoubleCoral)coral).BLXCrossover(state, 0, (VectorIndividual)otherparent);
							break;
						case 1:		//Crossover 1 SBX
							otherindex=selectrandom(state,subpop,reefSize,ind);
							otherparent=(VectorIndividual)subp.individuals[otherindex].clone();
							((DoubleCoral)coral).SBXCrossover(state, 0, (DoubleVectorIndividual)otherparent);	
							break;
				/*		case 3:		//Harmony Search
							otherindex=selectrandom(state,subpop,reefSize,ind);
							otherparent=(VectorIndividual)subp.individuals[otherindex].clone();
							((DoubleCoral)coral).HarmonySearch(state, subpop,0, ind,(VectorIndividual)otherparent);
							//Fix para no re-evaluar individuos que no cambian durante el crossover
							otherparent=null;
							break;
						case 2:		//Differential Evolution 
							otherindex=selectrandom(state,subpop,reefSize,ind);
							otherparent=(VectorIndividual)subp.individuals[otherindex].clone();
							otherindex=selectrandom(state,subpop,reefSize,ind);
							otherparent2=(VectorIndividual)subp.individuals[otherindex].clone();
							((DoubleCoral)coral).DifferentialEvolution(state, 0, (DoubleVectorIndividual)otherparent,(DoubleVectorIndividual)otherparent2);
							//Fix para no re-evaluar individuos que no cambian durante el crossover
							otherparent=null;
							break;*/
						case 2:		//Gaussian Mutation
							((DoubleCoral)coral).Mutate(state, 0, 0);
							break;
						case 3:		//Polinomial Mutation
							((DoubleCoral)coral).Mutate(state, 0, 1);
							break;
						default:
							state.output.warning("Substrate index out of bounds "+sub+", check everything");
							break;
						}
					}
					/***Now we evaluate the generated larvae***/
					//Maybe duplicate, force evaluation of new individual
					coral.evaluated=false;
					((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, coral, 0,0);
					
					if(otherparent!=null){
						((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, (Individual)otherparent, 0,0);
						if(coral.fitness.betterThan(otherparent.fitness))	Slarvae[ind]=(Individual)coral.clone();
						else												Slarvae[ind]=(Individual)otherparent.clone();
					}else													Slarvae[ind]=(Individual)coral.clone(); 
			
				
		/***************************************************************************************/		
		//Aquí montamos el cambio para ver de donde viene el mejor individuo de esta generacion		
				//Cogemos el primer individuo como el mejor, minimo fitness //inicializado en linea 149
				if(ind==0) {
					minfit_gen=(Fitness) Slarvae[ind].fitness.clone(); //No estoy seguro de si se puede clonar el fitness
					minind_gen=ind;
					minsus_gen=sub;
				}else {
					Fitness currfit=(Fitness) Slarvae[ind].fitness.clone();
					if(currfit.betterThan(minfit_gen)) {
						minfit_gen=currfit;
						minind_gen=ind;
						minsus_gen=sub;	
					}
				}
				
		/***************************************************************************************/		
				}
			}

		/***************************************************************************************/		
		//Aquí tendriamos el mejor fitness, individuo y sustrato de esta generacion.
			//Si funciona la segunda alternativa, esto podría desaparecer
			state.output.message("Generation "+state.generation+" Best substrate: "+minsus_gen);
			

			/* Esto puede funcionar para no tener que recalcular todo o sacar el fragmento 
			 * anterior a un log y analizarlo luego
			 * Llevamos un contador para cada sustrato que aumentamos cuando obtiene el mejor fitness
			 *  
			 */
			switch(minsus_gen) {
			case 0: 
				s0++;
				break;
			case 1: 
				s1++;
				break;
			case 2: 
				s2++;
				break;
			case 3: 
				s3++;
				break;
			}
			/*
			 * Calculamos el numero de casos donde cada sustrato es mejor entre el total 
			 * de generaciones transcurridas y lo imprimimos por pantalla.
			 */
			float currgen=(float)state.generation+1;

			state.output.message("Gen "+state.generation+" %Sustrato0: "+(float)s0/currgen
					+" %Sustrato1: "+(float)s1/currgen+" %Sustrato2: "+(float)s2/currgen
						+" %Sustrato3: "+(float)s3/currgen);
//			state.output.println((float)s0/currgen+";"+(float)s1/currgen
//					+";"+(float)s2/currgen+";"+(float)s3/currgen, percentageLog);
			
		/***************************************************************************************/		

			
			
			
			/*** Larvae Setting	***/	
			int larva_s0=0,larva_s1=0,larva_s2=0,larva_s3=0;
			for(int i=0;i<reefSize;i++){
				tries=0;
				boolean was_set=false;//Esto es nuevo
				int curloc=0,cursus_gen=0;//Esto es nuevo
				if(Slarvae[i]!=null){
					coral=(Individual)Slarvae[i].clone();
					
					//Recalculamos donde estaba la larva antes del operador
					if(subp.species instanceof IntegerVectorSpecies)//Esto es nuevo
						curloc=((IntegerCoral)coral).getPosition();
					else if(subp.species instanceof FloatVectorSpecies)
						curloc=((DoubleCoral)coral).getPosition();
				
					cursus_gen=(int)Math.floor((float)curloc/sectionSize);
					if(cursus_gen>=numberOfSubstrates) cursus_gen=numberOfSubstrates-1;

						
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
							was_set=true;
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
								was_set=true;
							}else tries++;
						}
					}
				/***************************************************************************************/		
				//Aquí vamos a llevar el contador de larvas que se asientan en el reef por cada sustrato
					
					if(was_set) {
						switch(cursus_gen) {
						case 0: 
							larva_s0++;
							break;
						case 1: 
							larva_s1++;
							break;
						case 2: 
							larva_s2++;
							break;
						case 3: 
							larva_s3++;
							break;
						}
					}

					
				}
			}
			
			state.output.message("Gen "+state.generation+" Larvas set by Sustrato0: "+larva_s0
					+" Larvas set by Sustrato1: "+larva_s1+" Larvas set by Sustrato2: "+larva_s2+
					" Larvas set by Sustrato3: "+larva_s3);
			state.output.println(minsus_gen+";"+larva_s0+";"+larva_s1
					+";"+larva_s2+";"+larva_s3+";"+(float)s0/currgen+";"+(float)s1/currgen
					+";"+(float)s2/currgen+";"+(float)s3/currgen, larvaeLog);

			/***************************************************************************************/		

			
			Slarvae=null; //delete array
			/*** Depredation ***/
			depindexes=null;
			//state.output.message(Pd+"> "+state.random[0].nextDouble(true, true));
			if(this.Pd>state.random[0].nextDouble(true, true)){ 
				numDep=(int)Math.ceil(this.Fd*reefSize);
				depindexes=subp.sortReefFit();
				int l=-1;
				state.output.message("Depredating "+numDep);
				for(int d=0;d<numDep;d++) {
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
			newpop.subpops[subpop].individuals=subp.individuals.clone();
		}
		
		return newpop;
	}

}
