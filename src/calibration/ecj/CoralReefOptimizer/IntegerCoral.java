/**
 * 
 */
package calibration.ecj.CoralReefOptimizer;

import ec.*;
import ec.util.*;
import ec.vector.*;

/**
 * @author ebermejo
 * @email enric2186@gmail.com
 */
public class IntegerCoral extends IntegerVectorIndividual {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * Adding the track of the physical position of this individual in the reef
	 */
	public int position;
	/*
	 * EPS Constant
	 */
	private static final double EPS = 1.0e-14;
	//=========================================================================
	//		OVERRIDE OPERATIONS
	//=========================================================================
	@Override
	/**
	 * Copy the coral position along with the individual
	 */
	public Object clone() {
		// TODO Auto-generated method stub
		IntegerCoral obj=(IntegerCoral) super.clone();
		obj.setPosition(this.position);
		return obj;
	}

	@Override
	public void reset(EvolutionState state, int thread) {
		// TODO Auto-generated method stub
		super.reset(state, thread);
		this.setPosition(-1);
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		// TODO Auto-generated method stub
		super.setup(state, base);
	}
	
	@Override
	public void reset(EvolutionState state, int thread, int newSize) {
		// TODO Auto-generated method stub
		super.reset(state, thread, newSize);
		this.setPosition(-1);
	}

	//=========================================================================
	//		CUSTOM PROPERTIES
	//=========================================================================
	/**
	 * Assign a reef location to the coral once it has settled
	 * @param number of position in the reef grid
	 */
	public void setPosition(int pos){
		this.position=pos;		
	}
	/**
	 * Retrieve the coral position in the reef grid
	 */
	public int getPosition(){
		return this.position;
	}
	//=========================================================================
	//		CROSSOVER METHODS
	//=========================================================================
	public void OnePointCrossover(EvolutionState state, int thread, VectorIndividual i){

		IntegerVectorSpecies s = (IntegerVectorSpecies) species;
        int tmp;
        int point;
        int len = Math.min(genome.length, ((IntegerVectorIndividual) i).genome.length);
        if (len != genome.length || len != ((IntegerVectorIndividual) i).genome.length)
            state.output.warnOnce("Genome lengths are not the same.  "
            		+ "Vector crossover will only be done in overlapping region.");
        point = state.random[thread].nextInt((len / s.chunksize));
        for(int x=0;x<point*s.chunksize;x++)
            { 
            tmp = ((IntegerVectorIndividual) i).genome[x];
            ((IntegerVectorIndividual) i).genome[x] = genome[x]; 
            genome[x] = tmp; 
            }
        i.evaluated=false;
		this.evaluated=false;
	}
	
	public void twoPointCrossover(EvolutionState state, int thread, VectorIndividual i){

		IntegerVectorSpecies s = (IntegerVectorSpecies) species;
        int tmp;
        int point;
        int len = Math.min(genome.length,((IntegerVectorIndividual) i).genome.length);
        if (len != genome.length || len != ((IntegerVectorIndividual) i).genome.length)
            state.output.warnOnce("Genome lengths are not the same."
            		+ "  Vector crossover will only be done in overlapping region.");

        point = state.random[thread].nextInt((len / s.chunksize));
        int point0 = state.random[thread].nextInt((len / s.chunksize));
        if (point0 > point) { int p = point0; point0 = point; point = p; }
        for(int x=point0*s.chunksize;x<point*s.chunksize;x++)
            {
            tmp = ((IntegerVectorIndividual) i).genome[x];
            ((IntegerVectorIndividual) i).genome[x] = genome[x];
            genome[x] = tmp;
            }
        i.evaluated=false;
        this.evaluated=false;
		
	}
	
	public void LineRecombCrossover(EvolutionState state, int thread, VectorIndividual i){

		IntegerVectorSpecies s = (IntegerVectorSpecies) species; 

        int len = Math.min(genome.length, ((IntegerVectorIndividual) i).genome.length);
        if (len != genome.length || len != ((IntegerVectorIndividual) i).genome.length)
            state.output.warnOnce("Genome lengths are not the same.  "
            		+ "Vector crossover will only be done in overlapping region.");
        double alpha = state.random[thread].nextDouble() * (1 + 2*s.lineDistance) - s.lineDistance;
        double beta = state.random[thread].nextDouble() * (1 + 2*s.lineDistance) - s.lineDistance;
        long t,u;
        long min, max;
        for (int x = 0; x < len; x++)
            {
            min = s.minGene(x);
            max = s.maxGene(x);
            t = (long) Math.floor(alpha * genome[x] + (1 - alpha) * ((IntegerVectorIndividual) i).genome[x] + 0.5);
            u = (long) Math.floor(beta * ((IntegerVectorIndividual) i).genome[x] + (1 - beta) * genome[x] + 0.5);
            if (!(t < min || t > max || u < min || u > max))
                {
                genome[x] = (int) t;
                ((IntegerVectorIndividual) i).genome[x] = (int) u; 
                }
            }


        i.evaluated=false;
        this.evaluated=false;
		
	}
	public void SBXCrossover(EvolutionState state, int thread, IntegerVectorIndividual i) {	
		int[] ind = ((IntegerCoral)i).genome;
		IntegerVectorSpecies s = (IntegerVectorSpecies)species;
		int eta_c=s.crossoverDistributionIndex;
		double rand;
		double y1, y2, yL, yu;
		double c1, c2;
		double alpha, beta, betaq;
		int valueX1, valueX2;
		int[] parent1 = this.genome;
		int[] parent2 = i.genome; 

		for(int gene = 0; gene < ind.length; gene++) {
			valueX1=this.genome[gene];
			valueX2=ind[gene];
			if (state.random[thread].nextDouble() <= 0.5) {
		          if (Math.abs(valueX1 - valueX2) > EPS) {
		        	  if (valueX1 < valueX2) {
		                  y1 = valueX1;
		                  y2 = valueX2;
		                } else {
		                  y1 = valueX2;
		                  y2 = valueX1;
		                }
		        	  
		        	  yL = s.minGene(gene);
		        	  yu = s.maxGene(gene);
		        	  rand = state.random[thread].nextDouble();
		        	  beta = 1.0 + (2.0 * (y1 - yL) / (y2 - y1));
		        	  alpha = 2.0 - Math.pow(beta, -(eta_c + 1.0));
		        	  if (rand <= (1.0 / alpha)) {
		                  betaq = Math.pow((rand * alpha), (1.0 / (eta_c + 1.0)));
		                } else {
		                  betaq = Math
		                    .pow(1.0 / (2.0 - rand * alpha), 1.0 / (eta_c + 1.0));
		                }
		        	  c1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
		              beta = 1.0 + (2.0 * (yu - y2) / (y2 - y1));
		              alpha = 2.0 - Math.pow(beta, -(eta_c + 1.0));

		              if (rand <= (1.0 / alpha)) {
		                betaq = Math.pow((rand * alpha), (1.0 / (eta_c + 1.0)));
		              } else {
		                betaq = Math
		                  .pow(1.0 / (2.0 - rand * alpha), 1.0 / (eta_c + 1.0));
		              }
		              c2 = 0.5 * (y1 + y2 + betaq * (y2 - y1));
		                if (c1<yL)
		                    c1=yL;
		                if (c2<yL)
		                    c2=yL;
		                if (c1>yu)
		                    c1=yu;
		                if (c2>yu)
		                    c2=yu;
		                if (state.random[thread].nextBoolean()){
	                    parent1[gene] = (int)c2; 
	                    parent2[gene] = (int)c1;
	                    }else{
	                    parent1[gene] = (int)c1;
	                    parent2[gene] = (int)c2;
	                    }
	                }else{
		                // do nothing
		                }
		            }
		        else
		            {
		            // do nothing
		            }
			          
			}
	    this.genome=parent1;
	    i.genome=parent2;
	    this.evaluated=false;
	    i.evaluated=false;
		}
	
	
	public void BLXCrossover(EvolutionState state, int thread, VectorIndividual i) {		
		int[] ind = ((IntegerCoral)i).genome;
        double alpha = 3.0;
        
		IntegerVectorSpecies s = (IntegerVectorSpecies)species;
 
		for(int gene = 0; gene < ind.length; gene++) {
			int min = Math.min(this.genome[gene], ind[gene]);
			int max = Math.max(this.genome[gene], ind[gene]);
			
			// Compute the bounds of the interval
			int minInterval = (int) (min-alpha*(max-min));
			int maxInterval = (int) (max+alpha*(max-min));
			
			// Generate random values in the interval
			((IntegerCoral)this).genome[gene] = randomValueFromClosedInterval(minInterval,maxInterval,state.random[thread]);
			((IntegerCoral)i).genome[gene] = randomValueFromClosedInterval(minInterval,maxInterval,state.random[thread]);
			
			// Check that the constraints hold
			if(this.genome[gene] < s.minGene(gene))
				this.genome[gene] = (int) s.minGene(gene);
			else if(this.genome[gene] > s.maxGene(gene))
				this.genome[gene] = (int) s.maxGene(gene);
			if(((IntegerCoral)i).genome[gene] < s.minGene(gene))
				((IntegerCoral)i).genome[gene] = (int) s.minGene(gene);
			else if(((IntegerCoral)i).genome[gene] > s.maxGene(gene))
				((IntegerCoral)i).genome[gene] = (int) s.maxGene(gene);
		}
		
		this.evaluated = false;
		i.evaluated = false;
			 
	}	
	//=========================================================================
	//		MUTATION METHODS
	//=========================================================================
	 void ResetMutation(EvolutionState state, int thread){
		IntegerVectorSpecies s = (IntegerVectorSpecies) species;
        for(int x = 0; x < genome.length; x++){
        	if (state.random[thread].nextBoolean(s.mutationProbability(x)))
            {
        		int old = genome[x];
                for(int retries = 0; retries < s.duplicateRetries(x) + 1; retries++){
                	 genome[x] = randomValueFromClosedInterval((int)s.minGene(x), (int)s.maxGene(x), state.random[thread]);
                     if (genome[x] != old) break;
                }
            }
        	if(genome[x]<(int)s.minGene(x)) genome[x]=(int)s.minGene(x);
        	if(genome[x]>(int)s.maxGene(x)) genome[x]=(int)s.maxGene(x);
        }
     }
	 void RandomWalkMutation(EvolutionState state, int thread){
			IntegerVectorSpecies s = (IntegerVectorSpecies) species;
	        for(int x = 0; x < genome.length; x++){
	        	if (state.random[0].nextBoolean(s.mutationProbability(x)))
	            {
	        		int old = genome[x];
	                for(int retries = 0; retries < s.duplicateRetries(x) + 1; retries++){
	                	int min = (int)s.minGene(x);
                        int max = (int)s.maxGene(x);
                        if (!s.mutationIsBounded(x))
                            {
                            // okay, technically these are still bounds, 
                        	// but we can't go beyond this without weird things happening
                            max = Integer.MAX_VALUE;
                            min = Integer.MIN_VALUE;
                            }
                        do
                            {
                            int n = (int)(state.random[thread].nextBoolean() ? 1 : -1);
                            int g = genome[x];
                            if ((n == 1 && g < max) ||
                                (n == -1 && g > min))
                                genome[x] = g + n;
                            else if ((n == -1 && g < max) ||
                                (n == 1 && g > min))
                                genome[x] = g - n;  
                            }
                        while (state.random[thread].nextBoolean(s.randomWalkProbability(x)));
                       
                        if (genome[x] != old) break;
	                }
	            }
	        	 if(genome[x]<(int)s.minGene(x)) genome[x]=(int) s.minGene(x);
                 if(genome[x]>(int)s.maxGene(x)) genome[x]=(int)s.maxGene(x);
	        }
	     }
	    
	/**
	 * 
	 */

}
