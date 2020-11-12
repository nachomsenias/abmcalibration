package calibration.ecj.multimodal.nichepso;

import java.util.ArrayList; 

import ec.* ;
import ec.util.* ;
import ec.vector.* ;


/*
 * PSOBreeder.java
 * Created: Thu May  2 17:09:40 EDT 2013
 */

/**
 * PSOBreeder is a simple single-threaded Breeder which performs 
 * Particle Swarm Optimization using the Particle class as individuals. 
 * PSOBreeder relies on a number of parameters which define weights for
 * various vectors computed during Particle Swarm Optimization, plus
 * a few flags:
 *
 * <ul>
 * <li> Neighborhoods for particles have a size S determined by the parameter neighborhood-size.  It's best if S were even.
 * <li> Neighborhoods for particles are constructed in one of three ways:
 * <ul>
 * <li> random: pick S informants randomly without replacement within the subpopulation, not including the particle itself, once at the beginning of the run.
 * <li> random-each-time: pick S informants randomly without replacement within the subpopulation, not including the particle itself, every single generation.
 * <li> toroidal: pick the floor(S/2) informants to the left of the particle's location within the subpopulation and the ceiling(S/2) informants to the right of the particle's location in the subpopulation, once at the beginning of the run.
 * </ul>
 * <li> To this you can add the particle itself to the neighborhood, with include-self. 
 * <li> The basic velocity update equation is VELOCITY <-- (VELOCITY * velocity-coefficent) + (VECTOR-TO-GLOBAL-BEST * global-coefficient) + (VECTOR-TO-NEIGHBORHOOD-BEST * informant-coefficient) + (VECTOR-TO-PERSONAL-BEST * personal-coefficient)
 * <li> The basic particle update equation is PARTICLE <-- PARTICLE + VELOCITY
 * </ul>
 *
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>velocity-coefficient</tt><br>
 *  <font size=-1>float &ge; 0</font></td>
 *  <td valign=top>(The weight for the velocity)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>personal-coefficient</tt><br>
 *  <font size=-1>float &ge; 0</font></td>
 *  <td valign=top>(The weight for the personal-best vector)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>informant-coefficient</tt><br>
 *  <font size=-1>float &ge; 0</font></td>
 *  <td valign=top>(The weight for the neighborhood/informant-best vector)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>global-coefficient</tt><br>
 *  <font size=-1>float &ge; 0</font></td>
 *  <td valign=top>(The weight for the global-best vector)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>neighborhood-size</tt><br>
 *  <font size=-1>int &gt; 0</font></td>
 *  <td valign=top>(The size of the neighborhood of informants, not including the particle)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>neighborhood-style</tt><br>
 *  <font size=-1>String, one of: random toroidal random-each-time</font></td>
 *  <td valign=top>(The method of generating the neighborhood of informants, not including the particle)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>include-self</tt><br>
 *  <font size=-1>true or false (default)</font></td>
 *  <td valign=top>(Whether to include the particle itself as a member of the neighborhood after building the neighborhood)</td>
 * </tr>
 *
 * </table>
 *
 * @author Khaled Ahsan Talukder
 */


public class NichePSOBreeder extends Breeder
    { 

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String P_VELOCITY_COEFFICIENT = "velocity-coefficient" ;
    public static final String P_PERSONAL_COEFFICIENT = "personal-coefficient" ;
    public static final String P_INFORMANT_COEFFICIENT = "informant-coefficient" ;
    public static final String P_GLOBAL_COEFFICIENT = "global-coefficient" ;
    public static final String P_RHO ="rho-coefficient";

    public double velCoeff = 0.5 ;          //  coefficient for the velocity
    public double personalCoeff = 0.5 ;             //  coefficient for self
    public double informantCoeff = 0.5 ;            //  coefficient for informants/neighbours
	public double storedinformantCoeff=informantCoeff;

    public double globalCoeff = 0.5 ;               //  coefficient for global best, this is not done in the standard PSO
    public boolean includeSelf = false;         

    public double[][] globalBest = null ; // one for each subpopulation
    public Fitness[] globalBestFitness = null;
    public Fitness[] oldglobalBestFitness = null;
    public int globalBestindex=0;

    public double MaxAllowedRadius=0.1;
    public double delta=0.0001;
    public double mu=0.001;
    //Phi1 = 1.2 Phi2=0 Rho=0.1 MergeMU 0.001 CreateDelta 0.0001
    public double rhoCoeff=1;
    public int sc=15;
    public int fc=5;
    public int ns=0;
    public int nf=0;
    class SubSwarm{
    	public ArrayList<Integer> subSwarmInds;
    	public double subSwarmradius;
		public int subSwarmBest;
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
        storedinformantCoeff=informantCoeff;
        globalCoeff = state.parameters.getDouble(base.push(P_GLOBAL_COEFFICIENT),null,0.0);
        if ( globalCoeff < 0.0 )
            state.output.fatal( "Parameter not found, or its value is less than 0.", base.push(P_GLOBAL_COEFFICIENT), null );
        
        rhoCoeff = state.parameters.getDouble(base.push(P_RHO), null, 1);               
        if (rhoCoeff <= 0 )
            state.output.fatal("Rho undefined.", base.push(P_RHO), null);
        
        Parameter p = new Parameter(Initializer.P_POP);
        Parameter p_subpop;
        Parameter p_default_subpop = p.push("default-subpop").push("size");
        p_subpop = p.push(Population.P_SUBPOP).push("" + 0).push(Subpopulation.P_SUBPOPSIZE);
        int originalPopSize = state.parameters.getInt(p_subpop,p_default_subpop, 1);
        
        subSwarms= new ArrayList<SubSwarm>();
        indexes= new ArrayList<Integer>();
		for (int i=0;i<originalPopSize;i++) 
			indexes.add(i);
		
		}

	public int	findNeighbor(EvolutionState state,NicheParticle indy,int subpop) {
		int index= -1;
		double mindist = Double.POSITIVE_INFINITY;
		for (int i=0;i<indexes.size();i++) {
        	NicheParticle other=(NicheParticle)state.population.subpops[subpop].individuals[indexes.get(i)];
        	if(other.popindex!=indy.popindex) {
        		double dist= indy.distanceTo(other);
        		if(dist<=mindist) {
        			mindist=dist;
        			index=i;
        		}
        	}
		}
		return index;
		
	}
 
	public void createSubSwarms(EvolutionState state) {
    	boolean rerun=false;
    	int subpop=0;//Rembember only 1 subpop is allowed
    	for(int i=0;i<this.indexes.size();i++) {
            if(this.indexes.size()<2) 
            	break;
        	NicheParticle particle=(NicheParticle)state.population.subpops[subpop].individuals[indexes.get(i)];
 
        	if(particle.stdev<delta) {
        		//Add i and neighbor from indexes/mainSwarm to the new subswarm
        		SubSwarm newSubSwarm=new SubSwarm();
        		newSubSwarm.subSwarmInds=new ArrayList<Integer>(2);
        		newSubSwarm.subSwarmInds.add(indexes.get(i));
        		
        		
        		int neigh=findNeighbor(state,particle,subpop);
        		if(neigh==-1) state.output.error("Particle has no neighbor, check code for bugs");
        		newSubSwarm.subSwarmInds.add(indexes.get(neigh));
        		if(i>neigh) {
        			indexes.remove(i);
        			indexes.remove(neigh);
        		}else{
        			indexes.remove(neigh);
        			indexes.remove(i);
        		}
        		//Remove i and neighbor from indexes/mainSwarm
        		newSubSwarm.subSwarmradius=0;
        		this.subSwarms.add(newSubSwarm);
        		updateSubSwarmBest(state,subSwarms.size()-1);
        		updateSubSwarmRadius(state,subSwarms.size()-1);
        		//i=0; //Should we start again, does not seem right
        		rerun=true;
        		break;
        	
        	}
    	}
        if (rerun)        createSubSwarms(state);
    }
 
    public void updateSubSwarmBest(EvolutionState state, int ss) {
    	int index=0;
    	ArrayList<Integer> subswarm=this.subSwarms.get(ss).subSwarmInds;
    	for(int i=1;i<subswarm.size();i++) {
        	NicheParticle ssbest=(NicheParticle)state.population.subpops[0].individuals[subswarm.get(index)];
        	NicheParticle other=(NicheParticle)state.population.subpops[0].individuals[subswarm.get(i)];
    		if(other.fitness.betterThan(ssbest.fitness)) index=i;
    	}
    	this.subSwarms.get(ss).subSwarmBest=index;
    }
    
    public void updateSubSwarmRadius(EvolutionState state,int ss) {
    	double rad=getSubSwarmRadius(state,this.subSwarms.get(ss).subSwarmInds,this.subSwarms.get(ss).subSwarmBest);
    	this.subSwarms.get(ss).subSwarmradius=rad;
    }

    public double getSubSwarmRadius(EvolutionState state,ArrayList<Integer> subswarm,int bestss) {
    	if(subswarm.size()==0 || subswarm.size()==1) return 0;
    	double max= Double.NEGATIVE_INFINITY;
    	NicheParticle subBest=(NicheParticle) state.population.subpops[0].individuals[subswarm.get(bestss)];
    	//Then we find the distance between individuals as the max distance in the subswarm
    	for (Integer i:subswarm) {
    		NicheParticle sol=(NicheParticle) state.population.subpops[0].individuals[i];
    		double dist=sol.distanceTo(subBest);
    		if(dist>max) max=dist;
    	}
    	return max;
    }
    
    public boolean shouldMerge(EvolutionState state,int i,int j) {
    	int besti=this.subSwarms.get(i).subSwarmInds.get(this.subSwarms.get(i).subSwarmBest);
    	int bestj=this.subSwarms.get(j).subSwarmInds.get(this.subSwarms.get(j).subSwarmBest);
    	double ri=Math.min(this.MaxAllowedRadius,this.subSwarms.get(i).subSwarmradius);
    	double rj=Math.min(this.MaxAllowedRadius,this.subSwarms.get(j).subSwarmradius);
		NicheParticle i1=(NicheParticle) state.population.subpops[0].individuals[besti];
		NicheParticle j1=(NicheParticle) state.population.subpops[0].individuals[bestj];
		double dist = i1.distanceTo(j1);
    	if(dist<Math.abs(ri+rj) || dist<this.mu)
    		return true;
    	else 
    		return false;
    }
    //
    public void mergeSubSwarms(EvolutionState state) {
    	boolean rerun=false;
    	for(int i=0;i<this.subSwarms.size();i++)
    		for (int j=i+1;j<this.subSwarms.size();j++) 
    			if(shouldMerge(state,i,j)) {
    				for(Integer jtomerge:this.subSwarms.get(j).subSwarmInds) 
    					this.subSwarms.get(i).subSwarmInds.add(jtomerge);
    				
     				this.subSwarms.get(j).subSwarmInds.clear();
    				this.subSwarms.remove(j);
    				updateSubSwarmBest(state,i);
    				updateSubSwarmRadius(state,i);
    				rerun=true;
    			}
    	if(rerun) mergeSubSwarms(state);
    }
    
    //Integrate particles in the already formed sub-swarms
    public void absorbSubSwarms(EvolutionState state) {
    	boolean rerun=false;
    	for(int i=0;i<this.indexes.size();i++) {
    		NicheParticle current=(NicheParticle) state.population.subpops[0].individuals[indexes.get(i)];
    		for (int j=0;j<this.subSwarms.size();j++) {
    			SubSwarm ss=this.subSwarms.get(j);
    			int ssbestindex=ss.subSwarmInds.get(ss.subSwarmBest);
    			NicheParticle ssbest=(NicheParticle) state.population.subpops[0].individuals[ssbestindex];
     			if(current.distanceTo(ssbest)<=ss.subSwarmradius) {
    				ss.subSwarmInds.add(indexes.get(i));
    				indexes.remove(i);
    				updateSubSwarmBest(state, j);
    				updateSubSwarmRadius(state, j);
    				rerun=true;
    				break;
    			}
    		}
    	}
    	if(rerun) absorbSubSwarms(state);
    }
    
    
    public Population breedPopulation(EvolutionState state)
        {
        // initialize the global best
        if (globalBest == null){
            globalBest = new double[state.population.subpops.length][];
            globalBestFitness = new Fitness[state.population.subpops.length];
            oldglobalBestFitness = new Fitness[state.population.subpops.length];

            }

        // update global best
        for(int subpop = 0 ; subpop < state.population.subpops.length ; subpop++){
        	//We want to check all the individuals to find the best one even if they belong to a subswarm
        	for(int ind = 0 ; ind < state.population.subpops[subpop].individuals.length ; ind++){
                if (globalBestFitness[subpop] == null ||
                    state.population.subpops[subpop].individuals[ind].fitness.betterThan(globalBestFitness[subpop]))
                    {
                    globalBest[subpop] = ((DoubleVectorIndividual)state.population.subpops[subpop].individuals[ind]).genome;
                    globalBestFitness[subpop] = state.population.subpops[subpop].individuals[ind].fitness;
                    oldglobalBestFitness[subpop]=(Fitness) globalBestFitness[subpop].clone();
                    globalBestindex=ind;
                    }
                 ((NicheParticle)state.population.subpops[subpop].individuals[ind]).update(state, subpop, ind, 0); 
            }
        	if(state.generation>1)
	        	if(globalBestFitness[subpop]!=null)
	        		if(globalBestFitness[subpop].betterThan(oldglobalBestFitness[subpop])) {
	        			++ns;
	        			nf=0;
	        		}else {
	        			ns=0;
	        			++nf;
	        		}
        	if(ns>sc)
        		this.rhoCoeff=this.rhoCoeff*2.0;
        	if(nf>fc)
        		this.rhoCoeff=this.rhoCoeff*0.5;
            // clone global best
            globalBest[subpop] = (double[])(globalBest[subpop].clone());
            globalBestFitness[subpop] = (Fitness)(globalBestFitness[subpop].clone());
            }


        // now move the particles of the main swarm
        for(int subpop = 0 ; subpop < state.population.subpops.length ; subpop++)
            {
        	/*** Main SWARM ***/
        	informantCoeff=0;//Ensure cognition-only mode for inds in main swarm
        	globalCoeff=0;
        	for (Integer i:indexes) 
         		((NicheParticle)state.population.subpops[subpop].individuals[i]).tweak(state,   globalBest[subpop],
                    velCoeff, personalCoeff, informantCoeff, globalCoeff, 0);
        	/*** Sub-SWARMs ***/
           	 informantCoeff=storedinformantCoeff;
             for(int ss=0;ss<this.subSwarms.size();ss++) {
             	ArrayList<Integer> subSwarmidxs=subSwarms.get(ss).subSwarmInds;
              	for(int ind=0;ind<subSwarmidxs.size();ind++)
             		if(ind==subSwarms.get(ss).subSwarmBest)         // tweak with GCPSO for the best particle in the subswarm
             			((NicheParticle)state.population.subpops[subpop].individuals[subSwarmidxs.get(ind)]).tweakGC(state,velCoeff,rhoCoeff,0);
             		else { 											// tweak with best as neighbor guide for the remainin particles
             			int bestssind=subSwarmidxs.get(subSwarms.get(ss).subSwarmBest); 
                     	((NicheParticle)state.population.subpops[subpop].individuals[subSwarmidxs.get(ind)]).setBestNeighbor(state,subpop,bestssind);
              			((NicheParticle)state.population.subpops[subpop].individuals[subSwarmidxs.get(ind)]).tweak(state,   globalBest[subpop],
                             velCoeff, personalCoeff, informantCoeff, globalCoeff, 0);
             		}
             }
            }
        this.mergeSubSwarms(state);
        this.absorbSubSwarms(state);
        this.createSubSwarms(state);
        // we return the same population
        return state.population ;
        }
    }

