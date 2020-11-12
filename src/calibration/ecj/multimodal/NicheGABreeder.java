 

package calibration.ecj.multimodal;
import ec.Individual;
import ec.BreedingPipeline;

import java.util.Arrays;

import ec.EvolutionState;
import ec.Population;
import ec.util.Parameter;
import ec.util.*;
import ec.simple.*;
 
public class NicheGABreeder extends SimpleBreeder
    { 
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	
    public static final String P_RADIUS= "radius";
    public static final String P_K = "kniches";
    public static final String P_METHOD = "nichemethod";
    /** An array[subpop] of the number of elites to keep for that subpopulation */
      
    public double radius;
    public int niches;
    String method;
    
    @Override
    public void setup(final EvolutionState state, final Parameter base) 
        {
    	super.setup(state, base);
	    niches=state.parameters.getInt(base.push(P_K), null,1);
	    if (niches < 0)
			state.output.fatal(	"Parameter not found, or its value is negative",base.push(P_K), null);
        radius = state.parameters.getDouble(base.push(P_RADIUS), null);
        if (radius < 0.0 )
			state.output.fatal(	"Parameter not found, or its value is negative",base.push(P_RADIUS), null);
        
		method=state.parameters.getString(base.push(P_METHOD),null);
		if(method==null)
			method="clearing";
        state.output.exitIfErrors();
        } 



	private void sortPop(EvolutionState state, int subpop) {
		java.util.Arrays.sort(state.population.subpops[subpop].individuals,
		          new java.util.Comparator<Object>(){
		              public int compare(Object o1, Object o2) {
		                  Individual a = (Individual) o1;
		                  Individual b = (Individual) o2;
		                  if (a.fitness.betterThan(b.fitness))         return -1;
		                  if (b.fitness.betterThan(a.fitness))         return 1;
		                  return 0;
		              }
		          });
	}
    
    double computeSharedFitness(final EvolutionState s,final int subpopulation,final int thread, int x) {
    	int length=s.population.subpops[subpopulation].individuals.length;
    	double auxfit=((Individual)(s.population.subpops[subpopulation].individuals[x])).fitness.fitness();
    	double normalizer=1;
    	for (int i=0; i<length;i++) {
    		if(i!=x) {
	    		double distance=((MMDoubleVectorIndividual)s.population.subpops[subpopulation].individuals[x]).distanceTo(s.population.subpops[subpopulation].individuals[i]);
	    		if(distance<this.radius) 
	    			normalizer += 1 -(distance/this.radius);
	    		}
    	}
    	return auxfit/normalizer;
    }
    void shareFitness(final EvolutionState s,final int subpopulation,final int thread) {
    	int length=s.population.subpops[subpopulation].individuals.length;
    	double normalizer;
    	double [] sharingFunction=new double[length];
    	Arrays.fill(sharingFunction,1);
    	//we iterate in order (pop was sorted previously
    	for (int i=0; i<length;i++) {
	    	double ifit=((Individual)(s.population.subpops[subpopulation].individuals[i])).fitness.fitness();
	    	for (int j=i+1; j<length;j++) {
	    		double distance=((MMDoubleVectorIndividual)s.population.subpops[subpopulation].individuals[i]).distanceTo(s.population.subpops[subpopulation].individuals[j]);
	    		if(distance<this.radius) {
	    			normalizer= 1- (distance/this.radius);
	    			sharingFunction[i]+=normalizer;
	    			sharingFunction[j]+=normalizer;
	    		}
			}//Remember we are maximizing!!!!
			((SimpleFitness)((Individual)s.population.subpops[subpopulation].individuals[i]).fitness).setFitness(s,ifit/sharingFunction[i],false);	    	
    	}
     }
    
    
    int clearFitness(final EvolutionState s,final int subpopulation,final int thread) {
    	int nwins, nniches=0;
    	//we iterate in order (pop was sorted previously
    	int length=s.population.subpops[subpopulation].individuals.length;
    	for (int i=0; i<length;i++) {
	    	double ifit=((Individual)(s.population.subpops[subpopulation].individuals[i])).fitness.fitness();
	    	if(ifit>0) {
	    		nwins=1;
	        	nniches++;
	 	    	for (int j=i+1; j<length;j++) {
	 	    		double jfit=((Individual)(s.population.subpops[subpopulation].individuals[j])).fitness.fitness();
			    	if(jfit>0){
		 	    		double distance=((MMDoubleVectorIndividual)s.population.subpops[subpopulation].individuals[i]).distanceTo(s.population.subpops[subpopulation].individuals[j]);
 			    		if(distance<this.radius) {	
 		 	    			if(nwins<this.niches)
				    			nwins+=1;
				    		else
				    			((SimpleFitness)((Individual)s.population.subpops[subpopulation].individuals[j]).fitness).setFitness(s, 0, false);
			    		}
			    	}
			    }
	    	}
    	}
    	return nniches;
    }
    

    /** A private helper function for breedPopulation which breeds a chunk
        of individuals in a subpopulation for a given thread.
        Although this method is declared
        public (for the benefit of a private helper class in this file),
        you should not call it. */

    protected void breedPopChunk(Population newpop, EvolutionState state, int[] numinds, int[] from, int threadnum) 
        {
        for(int subpop=0;subpop<newpop.subpops.length;subpop++)
            {
            // if it's subpop's turn and we're doing sequential breeding...
            if (!shouldBreedSubpop(state, subpop, threadnum))  
                {
                // instead of breeding, we should just copy forward this subpopulation.  We'll copy the part we're assigned
                for(int ind=from[subpop] ; ind < numinds[subpop] - from[subpop]; ind++)
                    // newpop.subpops[subpop].individuals[ind] = (Individual)(state.population.subpops[subpop].individuals[ind].clone());
                    // this could get dangerous
                    newpop.subpops[subpop].individuals[ind] = state.population.subpops[subpop].individuals[ind];
                }
            else
                {
                // do regular breeding of this subpopulation
                BreedingPipeline bp = null;
                if (clonePipelineAndPopulation)
                    bp = (BreedingPipeline)newpop.subpops[subpop].species.pipe_prototype.clone();
                else
                    bp = (BreedingPipeline)newpop.subpops[subpop].species.pipe_prototype;
                /*****************************************/
                /** ONLY CHANGES IN THE BREEDER PIPELINE**/ 
                /*****************************************/

               	//We sort
            	sortPop(state, subpop);
            	//We trim
                switch(this.method) {
                case "clearing":
                    clearFitness(state,  subpop, threadnum);
                    break;
                case "sharing":
                	shareFitness(state, subpop,threadnum);
                	break;
                default:
                	state.output.fatal(	"Niching method not specified");
                	break;
                }
                /*****************************************/
                // check to make sure that the breeding pipeline produces
                // the right kind of individuals.  Don't want a mistake there! :-)
                int x;
                if (!bp.produces(state,newpop,subpop,threadnum))
                    state.output.fatal("The Breeding Pipeline of subpopulation " + subpop + " does not produce individuals of the expected species " + newpop.subpops[subpop].species.getClass().getName() + " or fitness " + newpop.subpops[subpop].species.f_prototype );
                bp.prepareToProduce(state,subpop,threadnum);
                                        
                // start breedin'!
                                        
                x=from[subpop];
                int upperbound = from[subpop]+numinds[subpop];
                while(x<upperbound)
                    x += bp.produce(1,upperbound-x,x,subpop,
                        newpop.subpops[subpop].individuals,
                        state,threadnum);
                if (x>upperbound) // uh oh!  Someone blew it!
                    state.output.fatal("Whoa!  A breeding pipeline overwrote the space of another pipeline in subpopulation " + subpop + ".  You need to check your breeding pipeline code (in produce() ).");

                bp.finishProducing(state,subpop,threadnum);
                }
            }
        }



    static class EliteComparator implements SortComparatorL
        {
        Individual[] inds;
        public EliteComparator(Individual[] inds) {super(); this.inds = inds;}
        public boolean lt(long a, long b)
            { return inds[(int)b].fitness.betterThan(inds[(int)a].fitness); }
        public boolean gt(long a, long b)
            { return inds[(int)a].fitness.betterThan(inds[(int)b].fitness); }
        }

    protected void unmarkElitesEvaluated(EvolutionState state, Population newpop)
        {
        for(int sub=0;sub<newpop.subpops.length;sub++)
            {
            if (!shouldBreedSubpop(state, sub, 0))
                continue;
            for(int e=0; e < numElites(state, sub); e++)
                {
                int len = newpop.subpops[sub].individuals.length;
                if (reevaluateElites[sub])
                    newpop.subpops[sub].individuals[len - e - 1].evaluated = false;
                }
            }
        }

    /** A private helper function for breedPopulation which loads elites into
        a subpopulation. */

    protected void loadElites(EvolutionState state, Population newpop)
        {
        // are our elites small enough?
        for(int x=0;x<state.population.subpops.length;x++)
            {
            if (numElites(state, x)>state.population.subpops[x].individuals.length)
                state.output.error("The number of elites for subpopulation " + x + " exceeds the actual size of the subpopulation", 
                    new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
            if (numElites(state, x)==state.population.subpops[x].individuals.length)
                state.output.warning("The number of elites for subpopulation " + x + " is the actual size of the subpopulation", 
                    new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
            }
        state.output.exitIfErrors();

        // we assume that we're only grabbing a small number (say <10%), so
        // it's not being done multithreaded
        for(int sub=0;sub<state.population.subpops.length;sub++) 
            {
            if (!shouldBreedSubpop(state, sub, 0))  // don't load the elites for this one, we're not doing breeding of it
                {
                continue;
                }
                        
            // if the number of elites is 1, then we handle this by just finding the best one.
            if (numElites(state, sub)==1)
                {
                int best = 0;
                Individual[] oldinds = state.population.subpops[sub].individuals;
                for(int x=1;x<oldinds.length;x++)
                    if (oldinds[x].fitness.betterThan(oldinds[best].fitness))
                        best = x;
                Individual[] inds = newpop.subpops[sub].individuals;
                inds[inds.length-1] = (Individual)(oldinds[best].clone());
                }
            else if (numElites(state, sub)>0)  // we'll need to sort
                {
                int[] orderedPop = new int[state.population.subpops[sub].individuals.length];
                for(int x=0;x<state.population.subpops[sub].individuals.length;x++) orderedPop[x] = x;

                // sort the best so far where "<" means "not as fit as"
                QuickSort.qsort(orderedPop, new EliteComparator(state.population.subpops[sub].individuals));
                // load the top N individuals

                Individual[] inds = newpop.subpops[sub].individuals;
                Individual[] oldinds = state.population.subpops[sub].individuals;
                for(int x=inds.length-numElites(state, sub);x<inds.length;x++)
                    inds[x] = (Individual)(oldinds[orderedPop[x]].clone());
                }
            }
                
        // optionally force reevaluation
        unmarkElitesEvaluated(state, newpop);
        }
    }
 
