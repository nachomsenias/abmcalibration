package calibration.ecj.multimodal.nmmso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator; 

import calibration.ecj.EcjModelEvaluation;
import ec.*;
import ec.util.* ;
import ec.vector.* ;


/*
 * PSOBreeder.java
 * Created: Thu May  2 17:09:40 EDT 2018
 * @author ebermejo NichePSO rework of PSOBreeder
 */
 


public class NMMSOBreeder extends Breeder
    { 
	private static final long serialVersionUID = 1L;

    public static final String P_VELOCITY_COEFFICIENT = "velocity-coefficient" ;
    public static final String P_PERSONAL_COEFFICIENT = "personal-coefficient" ;
    public static final String P_INFORMANT_COEFFICIENT = "informant-coefficient" ;
    public static final String P_TOLERANCE = "tol" ;
    public static final String P_N = "n" ;
    public static final String P_MAX_INC= "max_inc" ;
    
    public double velCoeff = 0.5 ;          //  coefficient for the velocity
    public double personalCoeff = 0.5 ;             //  coefficient for self
    public double informantCoeff = 0.5 ;            //  coefficient for informants/neighbours
 
    public boolean includeSelf = false;         

    public double[][] globalBest = null ; // one for each subpopulation
    public Fitness[] globalBestFitness = null;
    public int globalBestindex=0;

    public double tol;
    public int n,max_inc,active_modes_changed,ssSize;
 
    //Dynamic Sub-Swarm class to properly manage the individuals instead of using the fixed array in Population
    class SubSwarm{
    	public ArrayList<Integer> subSwarmInds;
		public int subSwarmBest;
		public Fitness fitBest;
    	public boolean flagged;
		public double nndist;
    };
    
	public ArrayList<Integer> indexes;
	public ArrayList<SubSwarm> subSwarms;
	
  	public void setup(final EvolutionState state, final Parameter base)
        {
		velCoeff = state.parameters.getDouble(base.push(P_VELOCITY_COEFFICIENT),null,0.0);
        if ( velCoeff < 0.0 )
            state.output.fatal( "Parameter not found, or its value is less than 0.", base.push(P_VELOCITY_COEFFICIENT), null );

        personalCoeff = state.parameters.getDouble(base.push(P_PERSONAL_COEFFICIENT),null,0.0);
        if ( personalCoeff < 0.0)
            state.output.fatal( "Parameter not found, or its value is less than 0.", base.push(P_PERSONAL_COEFFICIENT), null );

        informantCoeff = state.parameters.getDouble(base.push(P_INFORMANT_COEFFICIENT),null,0.0);
        if ( informantCoeff < 0.0)
            state.output.fatal( "Parameter not found, or its value is less than 0.", base.push(P_INFORMANT_COEFFICIENT), null );
 
        tol = state.parameters.getDouble(base.push(P_TOLERANCE),null,0.0);
        if ( tol < 0.0)
            state.output.fatal( "Parameter not found, or its value is less than 0.", base.push(P_TOLERANCE), null );
  
        n = state.parameters.getInt(base.push(P_N),null,0);
        if ( n <= 0)
            state.output.fatal( "Parameter not found, or its value is less than 0.", base.push(P_N), null );
  
        max_inc = state.parameters.getInt(base.push(P_MAX_INC),null,0);
        if ( max_inc <= 0)
            state.output.fatal( "Parameter not found, or its value is less than 0.", base.push(P_MAX_INC), null );
  
        
        Parameter p = new Parameter(Initializer.P_POP);
        Parameter p_subpop;
        Parameter p_default_subpop = p.push("default-subpop").push("size");
        p_subpop = p.push(Population.P_SUBPOP).push("" + 0).push(Subpopulation.P_SUBPOPSIZE);
        int originalPopSize = state.parameters.getInt(p_subpop,p_default_subpop, 1);
        
        subSwarms= new ArrayList<SubSwarm>();
        indexes= new ArrayList<Integer>();
		for (int i=0;i<originalPopSize;i++) 
			indexes.add(i);
		active_modes_changed=0;
		}

	/*********** Sorting and Updating tools *************/
	//Updating the log of best individual so we can track the evolution
	public void updateGlobalBest(EvolutionState state,int subpop,int ind) {
		if (globalBestFitness[subpop] == null ||
			state.population.subpops[subpop].individuals[ind].fitness.betterThan(globalBestFitness[subpop])){
				globalBest[subpop] = ((DoubleVectorIndividual)state.population.subpops[subpop].individuals[ind]).genome.clone();
                globalBestFitness[subpop] = (Fitness) state.population.subpops[subpop].individuals[ind].fitness.clone();
                globalBestindex=ind;
                state.output.message("NMMSO Improved its global best at individual "+ind+" Fitness: "+globalBestFitness[subpop].fitness());
            }
	}
    //Assign the maximum Sub-Swarm size as n*Problem Dimension and store this value
	public void updateSSSize(EvolutionState state) {
    	this.ssSize=this.n*((FloatVectorSpecies)state.population.subpops[0].species).genomeSize;
    }

    public void updateSubSwarmBest(EvolutionState state, int ss) {
    	int index=0;
    	ArrayList<Integer> subswarm=this.subSwarms.get(ss).subSwarmInds;
    	for(int i=1;i<subswarm.size();i++) {
        	NMMSOParticle ssbest=(NMMSOParticle)state.population.subpops[0].individuals[subswarm.get(index)];
        	NMMSOParticle other=(NMMSOParticle)state.population.subpops[0].individuals[subswarm.get(i)];
    		if(other.fitness.betterThan(ssbest.fitness)) index=i;
    	}
    	this.subSwarms.get(ss).subSwarmBest=index;
    	this.subSwarms.get(ss).fitBest=(Fitness) state.population.subpops[0].individuals[subswarm.get(index)].fitness.clone();
    }

	
	public Integer[] sortFitnesses(){
		Integer [] indexes =new Integer[this.subSwarms.size()];
		final ArrayList<Double> fitSorting=new ArrayList<Double>(subSwarms.size());
		for (int n=0; n< this.subSwarms.size();n++){
			indexes[n]=n;
    		fitSorting.add(this.subSwarms.get(n).fitBest.fitness());
		}
		Arrays.sort(indexes, new Comparator<Integer>() {
		    @Override public int compare(final Integer o1, final Integer o2) {
		        return Double.compare(fitSorting.get(o1), fitSorting.get(o2));
		    }
		});
		return indexes;
	}
	/*******************************************************************/


	public Population breedPopulation(EvolutionState state)
    {
 	if(state.generation==0) {
 		updateSSSize(state);
        if (globalBest == null){        // initialize the global best
            globalBest = new double[state.population.subpops.length][];
            globalBestFitness = new Fitness[state.population.subpops.length];
             }
        for(int subpop = 0 ; subpop < state.population.subpops.length ; subpop++)
        	initializeSubSwarm(state,subpop);//We only have 1 subpop
	}
	//see if modes should be merged together     while sum(nmmso_state.active_modes_changed)>0
	while(active_modes_changed>0)
		this.attemptMerge(state); 
	for(int i=0;i<this.subSwarms.size();i++) 	//Reset swarm flags
		this.subSwarms.get(i).flagged=false;
	this.active_modes_changed=0;

    // Now increment the swarms
    // if we have more than max_evol, then only increment a subset
	Integer[] sortedSS=sortFitnesses();
	this.incrementSwarm(state,new ArrayList<Integer>(Arrays.asList(sortedSS))); 
	
	//Attempt to split off a member from one of the swarms to seed a new swarm
	this.hive(state);
	//create speculative new swarm, either at random in design space, or
 	if(state.random[0].nextBoolean() || this.subSwarms.size()<2)
		this.random_new(state);
	else {  // via crossover   
		sortedSS=sortFitnesses();
		this.evolve_new(state,sortedSS);
	}
    // we return the same population we have tweaked
    return state.population ;
    }  
	
	
	private void initializeSubSwarm(EvolutionState state,int subpop) {
		//The  algorithm  starts  by  generating  and  evaluating  a  single solution at random within X,
		if(this.indexes.isEmpty()) state.output.error("Indexes is empty while initializing a new swarm");
		int subind=state.random[0].nextInt(indexes.size()); //We get a random location of initialized individuals in our population
		int popind=indexes.get(subind);
		this.indexes.remove(subind);
		//Evaluate the individual and update the system
		Individual indi=state.population.subpops[0].individuals[popind];
		((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, indi, 0,0);
        ((NMMSOParticle)indi).update(state, popind); 
        updateGlobalBest(state,0,popind);
		//make the initial swarm
		SubSwarm ssnew=new SubSwarm();
		ssnew.subSwarmInds=new ArrayList<Integer>(this.ssSize);
		ssnew.subSwarmInds.add(popind);
		ssnew.subSwarmBest=0;
		ssnew.flagged=true;
		ssnew.nndist=1.0;
		ssnew.fitBest=(Fitness) indi.fitness.clone();
		this.subSwarms.add(ssnew);
		this.active_modes_changed++;
	}
	 
	public void attemptMerge(EvolutionState state) {
		boolean shouldmerge;
    	if(active_modes_changed>=1) {
    		this.active_modes_changed=0;
    		if(this.subSwarms.size()>1) 
 		    	for(int i=0;i<this.subSwarms.size();i++)
 		    		if(subSwarms.get(i).flagged) {
 		    			subSwarms.get(i).flagged=false;
 		    			int j=getclosestSS(state,i);
 		    			if(j!=-1) {
	 		    			if(subSwarms.get(i).nndist<this.tol) 
	 		    				shouldmerge=true;
	 		    			else 
	 		    				shouldmerge=checkMidPoint(state,i,j);
	 		    			if(shouldmerge) {
	 		    				this.subSwarms.get(j).flagged=false;
	 		    				mergeSubSwarm(state,i,j);
	 		    			}
	 		    		}
 		    		}
    	}
    }
	
	/**** Merging auxiliary methods ****/
	public int getclosestSS(EvolutionState state,int i) {
		int minj=-1;
		double mindist=Double.POSITIVE_INFINITY;
	    int besti=this.subSwarms.get(i).subSwarmInds.get(this.subSwarms.get(i).subSwarmBest);
		NMMSOParticle i1=(NMMSOParticle) state.population.subpops[0].individuals[besti];
  		for (int j=0;j<this.subSwarms.size();j++) 
  			if(j!=i) {
  				int ssbestj=this.subSwarms.get(j).subSwarmBest;
		    	int bestj=this.subSwarms.get(j).subSwarmInds.get(ssbestj); 
				NMMSOParticle j1=(NMMSOParticle) state.population.subpops[0].individuals[bestj];
				double dist = i1.distanceTo(j1);
		    	if(dist<mindist) {
		    		mindist=dist;
		    		minj=j;
		    		this.subSwarms.get(i).nndist=mindist; //TODO:CHECK HOW TO INITIALIZE as distance to limits
		   		}
  			}
  		return minj;
	    }
	
	public boolean checkMidPoint(EvolutionState state, int i, int j) {
		  boolean shouldmerge=false;
		  int bestpopi=this.subSwarms.get(i).subSwarmInds.get(this.subSwarms.get(i).subSwarmBest);
		  NMMSOParticle ibest=(NMMSOParticle) state.population.subpops[0].individuals[bestpopi];
		  int bestpopj=this.subSwarms.get(j).subSwarmInds.get(this.subSwarms.get(j).subSwarmBest); 
		  NMMSOParticle jbest=(NMMSOParticle) state.population.subpops[0].individuals[bestpopj];
		  NMMSOParticle midloc=(NMMSOParticle) jbest.clone();
		  for (int x=0;x<midloc.genomeLength();x++) //TODO: Check midloc out of bounds
			  midloc.genome[x]=0.5*(ibest.genome[x]-jbest.genome[x])+jbest.genome[x];
		  midloc.evaluated=false;
		  ((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, midloc, 0,0);
		  if(midloc.fitness.betterThan(jbest.fitness)) {
			  state.population.subpops[0].individuals[bestpopj]=(Individual) midloc.clone();
			  ((NMMSOParticle)state.population.subpops[0].individuals[bestpopj]).update(state, bestpopj);
			  updateGlobalBest(state,0,bestpopj);
			  this.subSwarms.get(j).flagged=true;
			  this.subSwarms.get(j).fitBest=(Fitness) midloc.fitness.clone();
			  this.active_modes_changed+=1;
			  shouldmerge=true;
		  }else if(midloc.fitness.betterThan(ibest.fitness))
		  	shouldmerge=true;
		  
		  return shouldmerge;
	}
	
	public void mergeSubSwarm(EvolutionState state, int i, int j) {
 		int other,merged,ssworst;
		if(this.subSwarms.get(i).fitBest.betterThan(this.subSwarms.get(j).fitBest)) {
				other=j;			merged=i;	//Merge j into i
		}else {	other=i;			merged=j;}	//Merge i into j	
	
		this.subSwarms.get(merged).flagged=true;
		for(int indi=0;indi<this.subSwarms.get(other).subSwarmInds.size();indi++) 
			this.subSwarms.get(merged).subSwarmInds.add(this.subSwarms.get(other).subSwarmInds.get(indi));
		//Remove the excess of individuals in the swarm if size is over the maximum
		ssworst=0;
		while(this.subSwarms.get(merged).subSwarmInds.size()>this.ssSize) {
			int popworst=this.subSwarms.get(merged).subSwarmInds.get(ssworst);
			Fitness worst=(Fitness)state.population.subpops[0].individuals[popworst].fitness;
			for(int x=1;x<this.subSwarms.get(merged).subSwarmInds.size();x++) {
				int popind=this.subSwarms.get(merged).subSwarmInds.get(x);
				if(worst.betterThan((Fitness)state.population.subpops[0].individuals[popind].fitness)) {
					worst=(Fitness)state.population.subpops[0].individuals[popind].fitness;
					popworst=popind;
					ssworst=x;
				}
			}
			this.subSwarms.get(merged).subSwarmInds.remove(ssworst); //remove particle from the swarm
			this.indexes.add(popworst); //Add it to the list of free individuals in the population
			((NMMSOParticle)state.population.subpops[0].individuals[popworst]).fullreset(state, 0);
			ssworst=0;
		}
		this.updateSubSwarmBest(state, merged);
		this.subSwarms.get(other).fitBest=null;
		this.subSwarms.get(other).subSwarmInds.clear();
		this.subSwarms.remove(other);
		this.active_modes_changed++;
		
	}
	/*******************************************************************/
	
	public void incrementSwarm(EvolutionState state, ArrayList<Integer> sortedSS) {
 		int limit=(int) Math.min(this.max_inc,this.subSwarms.size());
		if(limit>this.max_inc) {
			if(state.random[0].nextBoolean())
				Collections.shuffle(sortedSS);
			while(sortedSS.size()>limit) 
				sortedSS.remove(sortedSS.size()-1);
		}
		for(int x=0;x<sortedSS.size();x++) {
			int ssindex=sortedSS.get(x);
			NMMSOParticle newparticle;
			int bestssind=this.subSwarms.get(ssindex).subSwarmInds.get(this.subSwarms.get(ssindex).subSwarmBest);
			int ssindiv=0;
			int popind=0;
			if(this.indexes.isEmpty())
				state.output.error("Indexes is empty, we reached maximum capacity while incrementing a subswarm");
			// if swarm not yet at capacity, simply add a new particle
			if(this.subSwarms.get(ssindex).subSwarmInds.size()<this.ssSize) {
				popind=this.indexes.get(this.indexes.size()-1);
				this.indexes.remove(this.indexes.size()-1);
				newparticle=(NMMSOParticle) state.population.subpops[0].individuals[popind];
 				newparticle.genome=((NMMSOParticle)state.population.subpops[0].individuals[bestssind]).genome.clone();
				double[] uniform_sphere_sample=this.sphere_sample(state, 1, newparticle.genomeLength());
				for(int g=0;g<newparticle.genomeLength();g++)
					newparticle.genome[g]+=uniform_sphere_sample[g]*this.subSwarms.get(ssindex).nndist/2;
				uniform_sphere_sample=this.sphere_sample(state, 1, newparticle.genomeLength());
				for(int g=0;g<newparticle.genomeLength();g++)
					newparticle.velocity[g]=uniform_sphere_sample[g]*this.subSwarms.get(ssindex).nndist/2;
				this.subSwarms.get(ssindex).subSwarmInds.add(popind);
				ssindiv=this.subSwarms.get(ssindex).subSwarmInds.size()-1;
			}else {// otherwise move an existing particle
				int randind=state.random[0].nextInt(this.subSwarms.get(ssindex).subSwarmInds.size());
				ssindiv=randind;
				popind=this.subSwarms.get(ssindex).subSwarmInds.get(randind);
				newparticle=(NMMSOParticle) state.population.subpops[0].individuals[popind];
				newparticle.setBestNeighbor(state, 0, bestssind);
				newparticle.tweak(state, this.velCoeff, this.personalCoeff, this.informantCoeff, 0);
			}
			newparticle.evaluated=false;
			((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, newparticle, 0,0);
			newparticle.update(state, popind);
			updateGlobalBest(state,0,popind);
			if(newparticle.fitness.betterThan(state.population.subpops[0].individuals[bestssind].fitness)) {
				this.subSwarms.get(ssindex).flagged=true;
				this.subSwarms.get(ssindex).fitBest=(Fitness) newparticle.fitness.clone();
				this.subSwarms.get(ssindex).subSwarmBest=ssindiv;
				this.active_modes_changed+=1;
			}

		}
	}
	/*********** Auxiliary method for increment swarms ****************************/
	/*function generates n points unformly within the unit sphere in d dimensions	*/
	//http://mathworld.wolfram.com/HyperspherePointPicking.html 
	//https://stackoverflow.com/questions/52808880/algorithm-for-generating-uniformly-distributed-random-points-on-the-n-sphere 
	public double[] sphere_sample(EvolutionState state, int n,int d) {	//	X = uniform_sphere_points(n,d) n=1, d=genomelength
		double[] points=new double[d]; 			
		double[] z=new double[d];
		double sum=0;
		for(int i=0;i<d;i++) {
			z[i]=state.random[0].nextDouble();	// z= randn(n,d);
			sum+=z[i]*z[i];
			}
		double r1=1.0/Math.sqrt(sum);	// r1 = sqrt(sum(z.^2,2));
		for(int i=0;i<d;i++) {	
			double x=z[i]*r1;	//X=z./repmat(r1,1,d);
			//	double r=Math.pow(state.random[0].nextDouble(),(1/d));		//r=rand(n,1).^(1/d);
			//	x=x*r;														//X = X.*repmat(r,1,d);
			if(state.random[0].nextBoolean())
				points[i]=x;
			else points[i]=-x;
		}
		return points;
	}
	/*******************************************************************/

	public void hive(EvolutionState state) {
		ArrayList<Integer> fullss=new ArrayList<Integer>();
		for(int i=0;i<this.subSwarms.size();i++)
			if(this.subSwarms.get(i).subSwarmInds.size()==this.ssSize)
				fullss.add(i);
		if(!fullss.isEmpty()) {
			int ssindex=fullss.get(state.random[0].nextInt(fullss.size()));
			int popbest=this.subSwarms.get(ssindex).subSwarmInds.get(this.subSwarms.get(ssindex).subSwarmBest);
			int ssindiv;
			do {
				ssindiv=state.random[0].nextInt(this.subSwarms.get(ssindex).subSwarmInds.size());
			}while(ssindiv==popbest);
			int popind=this.subSwarms.get(ssindex).subSwarmInds.get(ssindiv);
			NMMSOParticle randparticle=(NMMSOParticle) state.population.subpops[0].individuals[popind];
			NMMSOParticle bestparticle=(NMMSOParticle) state.population.subpops[0].individuals[popbest];
			if(randparticle.distanceTo(bestparticle)>this.tol) {
				 NMMSOParticle midloc=(NMMSOParticle) randparticle.clone();
				  for (int x=0;x<midloc.genomeLength();x++) //TODO: Check midloc out of bounds
					  midloc.genome[x]=0.5*(bestparticle.genome[x]-randparticle.genome[x])+randparticle.genome[x];
				  midloc.evaluated=false;
				  ((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, midloc, 0,0);
				  if(midloc.fitness.betterThan(randparticle.fitness)) {
					  //Update individual in the population
					  double[] uniform_sphere_sample=this.sphere_sample(state, 1, midloc.genomeLength());
					  double dist=midloc.distanceTo(bestparticle);
					  for(int g=0;g<midloc.genomeLength();g++)
						  midloc.velocity[g]=uniform_sphere_sample[g]*(dist/2);
					  state.population.subpops[0].individuals[popind]=(Individual) midloc.clone();
					  ((NMMSOParticle)state.population.subpops[0].individuals[popind]).update(state, popind);
					  updateGlobalBest(state,0,popind);	
					  //Remove individual from current Subswarm
					  this.subSwarms.get(ssindex).flagged=false;
					  this.subSwarms.get(ssindex).subSwarmInds.remove(ssindiv);
					  this.updateSubSwarmBest(state, ssindex);

					  //Create new SubSwarm and add it to the list
					  SubSwarm newSS=new SubSwarm();
					  newSS.subSwarmInds=new ArrayList<Integer>(this.ssSize);
					  newSS.subSwarmInds.add(popind);
					  newSS.subSwarmBest=0;
					  newSS.flagged=true;
					  newSS.fitBest=(Fitness) midloc.fitness.clone();
					  newSS.nndist=1.0;
					  this.subSwarms.add(newSS);
					  this.active_modes_changed+=1;
				  }else {
					  if(midloc.fitness.betterThan(bestparticle.fitness)) {
						  //Replace bestparticle of the swarm
						  state.population.subpops[0].individuals[popbest]=(Individual) midloc.clone();
						  ((NMMSOParticle)state.population.subpops[0].individuals[popbest]).update(state, popbest);
						  updateGlobalBest(state,0,popbest);
						 this.subSwarms.get(ssindex).fitBest=(Fitness)midloc.fitness.clone();
						 this.subSwarms.get(ssindex).flagged=false; //CHECK: Should this swarm flag the improvement
					  }
				  }
			}
		}
	} 

	private void random_new(EvolutionState state) {
		if(!indexes.isEmpty()) {
			//Randomly select a free location in the population
			int freeind=state.random[0].nextInt(indexes.size());
			int popind=indexes.get(freeind);
			this.indexes.remove(freeind);//Occupy pop
			Individual indi=state.population.subpops[0].individuals[popind];
			((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, indi, 0,0);
	        ((NMMSOParticle)indi).update(state, popind);
	        updateGlobalBest(state,0,popind);
	        //Assign new random individual to a new subswarm
			SubSwarm ssnew=new SubSwarm();
			ssnew.subSwarmBest=0;
			ssnew.subSwarmInds=new ArrayList<Integer>(this.ssSize);
			ssnew.subSwarmInds.add(popind); 
			ssnew.flagged=true;
			ssnew.fitBest=(Fitness) indi.fitness.clone();
			ssnew.nndist=1.0;
			this.active_modes_changed++;	
			this.subSwarms.add(ssnew);		
			}else 
				state.output.error("Indexes is empty while randoming a new swarm");
	}

    private void evolve_new(EvolutionState state,Integer[] sortedSS) {
    	int sscsize=this.subSwarms.size();
    	int ssa,ssb;
		if(state.random[0].nextBoolean()){
			ssa=sortedSS[0]; ssb=sortedSS[1]; //Evolve Fittest pair
		}else 
			do { //Evolve Random pair
			ssa=sortedSS[state.random[0].nextInt(sscsize)]; ssb=sortedSS[state.random[0].nextInt(sscsize)];
			}while(ssa==ssb);
		int popinda=this.subSwarms.get(ssa).subSwarmInds.get(this.subSwarms.get(ssa).subSwarmBest);
		int popindb=this.subSwarms.get(ssb).subSwarmInds.get(this.subSwarms.get(ssb).subSwarmBest);
		NMMSOParticle PA=(NMMSOParticle) state.population.subpops[0].individuals[popinda].clone();
		NMMSOParticle PB=(NMMSOParticle) state.population.subpops[0].individuals[popindb].clone();
		//PA.simulatedBinaryCrossover(state.random[0], (DoubleVectorIndividual)PB, 2.0); //OR TODO: UniformCrossover maybe
		PA.uniformCrossover(state, PB, 0);
		PA.evaluated=false;
		((EcjModelEvaluation)state.evaluator.p_problem).evaluate(state, PA, 0,0);
		//Assign a free location in the population to the newly generated individual, updating everything
		if(this.indexes.isEmpty()) state.output.error("Indexes is empty while evolving new swarm");
		int freeind=state.random[0].nextInt(indexes.size());
		int popind=indexes.get(freeind);
		state.population.subpops[0].individuals[popind]=(Individual) PA.clone();
        ((NMMSOParticle)PA).update(state, popind);
        updateGlobalBest(state,0,popind);
		this.indexes.remove(freeind); //Occupy Pop
        //And create a new subswarm containing the evolved individual
		SubSwarm ssnew=new SubSwarm();
		ssnew.subSwarmInds=new ArrayList<Integer>(this.ssSize);
		ssnew.subSwarmInds.add(popind);
		ssnew.subSwarmBest=0;
		ssnew.flagged=true;
		ssnew.fitBest=(Fitness) PA.fitness.clone();
		this.subSwarms.add(ssnew);			
		this.active_modes_changed++;	
    }
 

    }

