package calibration.ecj.CoralReefOptimizer;

import ec.*;
import ec.util.*;
import ec.vector.*;
/**
 * @author ebermejo
 * @email enric2186@gmail.com
 */
public class DoubleCoral extends DoubleVectorIndividual {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Default serial version
	 */
 	/*
	 * Adding the track of the physical position of this individual in the reef
	 */
	public int position;
	/*mutation parameter*/
	private static final String P_DINDEX = "distribution-index";

	/*BLX-alpha parameter*/
    public static final String P_ALPHA = "alpha";
    
	public double dist_index,alpha;
	//=========================================================================
	//		OVERRIDE OPERATIONS
	//=========================================================================
	@Override
	/**
	 * Copy the coral position along with the individual
	 */
	public Object clone() {
		// TODO Auto-generated method stub
		DoubleCoral obj=(DoubleCoral) super.clone();
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
		this.dist_index = state.parameters.getDouble(base.push(P_DINDEX), null, -1.0);
	        
	    if(this.dist_index < 0)
	     	state.output.fatal("Distribution index value for mutation not found");
	    alpha=state.parameters.getDouble(base.push(P_ALPHA), null,0.001);
		
		if (alpha<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_ALPHA),null);

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

        FloatVectorSpecies s = (FloatVectorSpecies) species;
        double tmp;
        int point;
        int len = Math.min(genome.length, ((DoubleVectorIndividual) i).genome.length);
        if (len != genome.length || len != ((DoubleVectorIndividual) i).genome.length)
            state.output.warnOnce("Genome lengths are not the same.  "
            		+ "Vector crossover will only be done in overlapping region.");
        point = state.random[thread].nextInt((len / s.chunksize));
        for(int x=0;x<point*s.chunksize;x++)
            { 
            tmp = ((DoubleVectorIndividual) i).genome[x];
            ((DoubleVectorIndividual) i).genome[x] = genome[x]; 
            genome[x] = tmp; 
            }
        i.evaluated=false;
		this.evaluated=false;
	}
	public void twoPointCrossover(EvolutionState state, int thread, VectorIndividual i){

        FloatVectorSpecies s = (FloatVectorSpecies) species;
        double tmp;
        int point;
        int len = Math.min(genome.length,((DoubleVectorIndividual) i).genome.length);
        if (len != genome.length || len != ((DoubleVectorIndividual) i).genome.length)
            state.output.warnOnce("Genome lengths are not the same.  "
            		+ "Vector crossover will only be done in overlapping region.");

        point = state.random[thread].nextInt((len / s.chunksize));
        int point0 = state.random[thread].nextInt((len / s.chunksize));
        if (point0 > point) { int p = point0; point0 = point; point = p; }
        for(int x=point0*s.chunksize;x<point*s.chunksize;x++)
            {
            tmp = ((DoubleVectorIndividual) i).genome[x];
            ((DoubleVectorIndividual) i).genome[x] = genome[x];
            genome[x] = tmp;
            }
        
		i.evaluated=false;
		this.evaluated=false;
	}
	
	public void HarmonySearch(EvolutionState state, int subpop,int thread, int actual,VectorIndividual i){
        FloatVectorSpecies s = (FloatVectorSpecies) species; 
        int chosen;
        double rand;
        int len = Math.min(genome.length, ((DoubleVectorIndividual) i).genome.length);
		for(int x=0;x<len;x++){
			if(((CROSLBreeder) state.breeder).HMCR>state.random[thread].nextDouble()){
				chosen=actual;
				while(chosen==actual)
					chosen=state.random[thread].nextInt(state.population.subpops[subpop].individuals.length);
				this.genome[x]=((DoubleVectorIndividual)state.population.subpops[subpop].individuals[chosen]).genome[x];
			}else 
				this.genome[x]=s.minGene(x) + state.random[thread].nextDouble(true, true) * (s.maxGene(x) - s.minGene(x));
		}
		for(int x=0;x<len;x++){
			if(((CROSLBreeder) state.breeder).PAR>state.random[thread].nextDouble()){
				rand=s.minGene(x) + state.random[thread].nextDouble(true, true) * (s.maxGene(x) - s.minGene(x));
				if(state.random[thread].nextBoolean())
					this.genome[x]=this.genome[x]+((CROSLBreeder) state.breeder).delta*rand;
				else
					this.genome[x]=this.genome[x]-((CROSLBreeder) state.breeder).delta*rand;
				if(this.genome[x] < s.minGene(x))				this.genome[x] = s.minGene(x);
				if(this.genome[x] > s.maxGene(x))				this.genome[x] = s.maxGene(x);		
			}
		}
		this.evaluated=false;
		
	}
	
	public void DifferentialEvolution(EvolutionState state,int thread,DoubleVectorIndividual b, DoubleVectorIndividual c){
		double func=((CROSLBreeder) state.breeder).func;
		double cr=((CROSLBreeder) state.breeder).cr;

		double value;
		FloatVectorSpecies s = (FloatVectorSpecies) species; 
	    int len = Math.min(genome.length, b.genome.length);
	    for(int x=0;x<len;x++){
	    	if(state.random[thread].nextDouble()<cr) {
		    	value=this.genome[x]+func*(b.genome[x]-c.genome[x]);
	
				if(value < s.minGene(x))			value = s.minGene(x);
				if(value > s.maxGene(x))			value = s.maxGene(x);	
				this.genome[x]=value;
	    	}
	    }
	    this.evaluated=false;
	}
	
	public void LineRecombCrossover(EvolutionState state, int thread, VectorIndividual i){

        FloatVectorSpecies s = (FloatVectorSpecies) species; 
        
        int len = Math.min(genome.length, ((DoubleVectorIndividual) i).genome.length);
        if (len != genome.length || len != ((DoubleVectorIndividual) i).genome.length)
            state.output.warnOnce("Genome lengths are not the same.  Vector crossover will only be done in overlapping region.");
        double alpha = state.random[thread].nextDouble(true, true) * (1 + 2*s.lineDistance) - s.lineDistance;
        double beta = state.random[thread].nextDouble(true, true) * (1 + 2*s.lineDistance) - s.lineDistance;
        double t,u,min,max;
        for (int x = 0; x < len; x++)
            {
            min = s.minGene(x);
            max = s.maxGene(x);
            t = alpha * genome[x] + (1 - alpha) * ((DoubleVectorIndividual) i).genome[x];
            u = beta * ((DoubleVectorIndividual) i).genome[x] + (1 - beta) * genome[x];
            if (!(t < min || t > max || u < min || u > max))
                {
                genome[x] = t;
                ((DoubleVectorIndividual) i).genome[x] = u; 
                }
            }
        i.evaluated=false;
        this.evaluated=false;
		
	}
	
	public void SBXCrossover(EvolutionState state, int thread, DoubleVectorIndividual ind){
	
	    final double EPS = 1.0e-14;
	    FloatVectorSpecies s = (FloatVectorSpecies) species;
	
	    int eta_c=s.crossoverDistributionIndex;
	    double[] parent1 = genome;
	    double[] parent2 = ind.genome; 
	            
	            
	    double y1, y2, yl, yu;
	    double c1, c2;
	    double alpha, beta, betaq;
	    double rand;
	            
	    for(int i = 0; i < parent1.length; i++)
        {
        if (state.random[thread].nextBoolean())  // 0.5f
            {
            if (Math.abs(parent1[i] - parent2[i]) > EPS)
                {
                if (parent1[i] < parent2[i])
                    {
                    y1 = parent1[i];
                    y2 = parent2[i];
                    }
                else
                    {
                    y1 = parent2[i];
                    y2 = parent1[i];
                    }
                yl = s.minGene(i); //min_realvar[i];
                yu = s.maxGene(i); //max_realvar[i];    
                rand = state.random[thread].nextDouble();
                beta = 1.0 + (2.0*(y1-yl)/(y2-y1));
                alpha = 2.0 - Math.pow(beta,-(eta_c+1.0));
                if (rand <= (1.0/alpha))
                    {
                    betaq = Math.pow((rand*alpha),(1.0/(eta_c+1.0)));
                    }
                else
                    {
                    betaq = Math.pow((1.0/(2.0 - rand*alpha)),(1.0/(eta_c+1.0)));
                    }
                c1 = 0.5*((y1+y2)-betaq*(y2-y1));
                beta = 1.0 + (2.0*(yu-y2)/(y2-y1));
                alpha = 2.0 - Math.pow(beta,-(eta_c+1.0));
                if (rand <= (1.0/alpha))
                    {
                    betaq = Math.pow((rand*alpha),(1.0/(eta_c+1.0)));
                    }
                else
                    {
                    betaq = Math.pow((1.0/(2.0 - rand*alpha)),(1.0/(eta_c+1.0)));
                    }
                c2 = 0.5*((y1+y2)+betaq*(y2-y1));
                if (c1<yl)
                    c1=yl;
                if (c2<yl)
                    c2=yl;
                if (c1>yu)
                    c1=yu;
                if (c2>yu)
                    c2=yu;
                if (state.random[thread].nextBoolean())
                    {
                    parent1[i] = c2;
                    parent2[i] = c1;
                    }
                else
                    {
                    parent1[i] = c1;
                    parent2[i] = c2;
                    }
                }
            else
                {
                // do nothing
                }
            }
        else
            {
            // do nothing
            }
        }
	    this.genome=parent1;
	    ind.genome=parent2;
	    this.evaluated=false;
	    ind.evaluated=false;
    }

	public void BLXCrossover(EvolutionState state, int thread, VectorIndividual i) {		
		double[] ind = ((DoubleVectorIndividual)i).genome;
		FloatVectorSpecies s = (FloatVectorSpecies)species;
		
		
		for(int gene = 0; gene < ind.length; gene++) {
			double min = Math.min(this.genome[gene], ind[gene]);
			double max = Math.max(this.genome[gene], ind[gene]);
			
			// Compute the bounds of the interval
			double minInterval = min-alpha*(max-min);
			double maxInterval = max+alpha*(max-min);
			
			// Generate random values in the interval
			double rnd = state.random[thread].nextDouble(true, true);
			((DoubleVectorIndividual)this).genome[gene] = minInterval + rnd * (maxInterval - minInterval);
			rnd = state.random[thread].nextDouble(true, true);
			((DoubleVectorIndividual)i).genome[gene] = minInterval + rnd * (maxInterval - minInterval);
			

			// Check that the constraints hold
			if(this.genome[gene] < s.minGene(gene))
				this.genome[gene] = s.minGene(gene);
			else if(this.genome[gene] > s.maxGene(gene))
				this.genome[gene] = s.maxGene(gene);
			if(((DoubleVectorIndividual)i).genome[gene] < s.minGene(gene))
				((DoubleVectorIndividual)i).genome[gene] = s.minGene(gene);
			else if(((DoubleVectorIndividual)i).genome[gene] > s.maxGene(gene))
				((DoubleVectorIndividual)i).genome[gene] = s.maxGene(gene);
		}
		this.evaluated = false;
		i.evaluated = false;
  
	}	
	//=========================================================================
	//		MUTATION METHODS
	//=========================================================================

    public void Mutate(EvolutionState state, int thread,int type)
        {
        FloatVectorSpecies s = (FloatVectorSpecies) species;

        MersenneTwisterFast rng = state.random[thread];
        for(int x = 0; x < genome.length; x++)
            if (rng.nextBoolean(s.mutationProbability(x)))
                {
                double old = genome[x];
                for(int retries = 0; retries < s.duplicateRetries(x) + 1 + 1; retries++)
                    { 
                    switch(type)
                        {
                        case 0:
                            this.gaussianMutationCoral(state, rng, s, x);
                            break;
                        case 1:
                            polynomialMutationCoral(state, rng, s, x);
                            break;
                        case 2:
                            floatResetMutationCoral(rng, s, x);
                            break;
                        case 3:
                            integerResetMutationCoral(rng, s, x);
                            break;
                        case 4:
                            integerRandomWalkMutationCoral(rng, s, x);
                            break;
                        default:
                            state.output.fatal("In DoubleVectorIndividual.defaultMutate,"
                            		+ " default case occurred when it shouldn't have");
                            break;
                        }
                    if (genome[x] != old) break;
                    // else genome[x] = old;  // try again
                    }
                }
        }
    
	    void integerRandomWalkMutationCoral(MersenneTwisterFast random, FloatVectorSpecies species, int index)
	    {
	    double min = species.minGene(index);
	    double max = species.maxGene(index);
	    if (!species.mutationIsBounded(index))
	        {
	        // okay, technically these are still bounds,
	    	//but we can't go beyond this without weird things happening
	        max = MAXIMUM_INTEGER_IN_DOUBLE;
	        min = -(max);
	        }
	    do
	        {
	        int n = (int)(random.nextBoolean() ? 1 : -1);
	        double g = Math.floor(genome[index]);
	        if ((n == 1 && g < max) ||
	            (n == -1 && g > min))
	            genome[index] = g + n;
	        else if ((n == -1 && g < max) ||
	            (n == 1 && g > min))
	            genome[index] = g - n;     
	        }
	    while (random.nextBoolean(species.randomWalkProbability(index)));
	    if(genome[index]<min) genome[index]=min;
	    if(genome[index]>max) genome[index]=max;
	    }

	private void integerResetMutationCoral(MersenneTwisterFast random, FloatVectorSpecies species, int index)
	    {
	    long minGene = (long)Math.floor(species.minGene(index));
	    long maxGene = (long)Math.floor(species.maxGene(index));
	    genome[index] = randomValueFromClosedIntervalCoral(minGene, maxGene, random); 
	    }
	
	void floatResetMutationCoral(MersenneTwisterFast random, FloatVectorSpecies species, int index)
	    {
	    double minGene = species.minGene(index);
	    double maxGene = species.maxGene(index);
	    genome[index] = minGene + random.nextDouble(true, true) * (maxGene - minGene);
	    }
	
	void gaussianMutationCoral(EvolutionState state, MersenneTwisterFast random, FloatVectorSpecies species, int index)
	    {
	    double val;
	    double min = species.minGene(index);
	    double max = species.maxGene(index);
	    double stdev = species.gaussMutationStdev(index);
	    int outOfBoundsLeftOverTries = species.outOfBoundsRetries;
	    boolean givingUpAllowed = species.outOfBoundsRetries != 0;
	    do
	        {

	        val = random.nextGaussian() * stdev + genome[index];

	        outOfBoundsLeftOverTries--;
	        if (species.mutationIsBounded(index) && (val > max || val < min))
	            {
	            if (givingUpAllowed && (outOfBoundsLeftOverTries == 0))
	                {
	                val = min + random.nextDouble() * (max - min);

	                species.outOfRangeRetryLimitReached(state);// it better get inlined
	                break;
	                }
	            } 
	        else break;
	        } 
	    while (true);
	    genome[index] = val;

	    if(genome[index]<min) genome[index]=min;
	    if(genome[index]>max) genome[index]=max;
	    }
	
	void polynomialMutationCoral(EvolutionState state, MersenneTwisterFast random, FloatVectorSpecies species, int index)
	    {
	    double eta_m = dist_index;
	    boolean alternativePolynomialVersion = species.polynomialIsAlternative(index);
	    
	    double rnd, delta1, delta2, mut_pow, deltaq;
	    double y, yl, yu, val, xy;
	    double y1;
	
	    y1 = y = genome[index];  
	    yl = species.minGene(index); 
	    yu = species.maxGene(index); 
	    delta1 = (y-yl)/(yu-yl);
	    delta2 = (yu-y)/(yu-yl);
	
	    int totalTries = species.outOfBoundsRetries;
	    int tries = 0;
	    for(tries = 0; tries < totalTries || totalTries == 0; tries++)  
	    	// keep trying until totalTries is reached if it's not zero.  If it's zero, go on forever.
	        {
	        rnd = random.nextDouble();
	        mut_pow = 1.0/(eta_m+1.0);
	        if (rnd <= 0.5)
	            {
	            xy = 1.0-delta1;
	            val = 2.0*rnd + (alternativePolynomialVersion ? (1.0-2.0*rnd)*(Math.pow(xy,(eta_m+1.0))) : 0.0);
	            deltaq =  Math.pow(val,mut_pow) - 1.0;
	            }
	        else
	            {
	            xy = 1.0-delta2;
	            val = 2.0*(1.0-rnd) + (alternativePolynomialVersion ? 2.0*(rnd-0.5)*(Math.pow(xy,(eta_m+1.0))) : 0.0);
	            deltaq = 1.0 - (Math.pow(val,mut_pow));
	            }
	        y1 = y + deltaq*(yu-yl);
	        if (!species.mutationIsBounded(index) || (y1 >= yl && y1 <= yu)) break;  // yay, found one
	        }
	                                                            
	    // at this point, if tries is totalTries, we failed
	    if (totalTries != 0 && tries == totalTries)
	        {
	        // just randomize
	        y1 = (double)(species.minGene(index) + random.nextDouble(true, true) * (species.maxGene(index) - species.minGene(index)));  //(double)(min_realvar[index] + random.nextDouble() * (max_realvar[index] - min_realvar[index]));
	        species.outOfRangeRetryLimitReached(state);// it better get inlined
	        }
	    genome[index] = y1; 
	    if(genome[index]<yl) genome[index]=yl;
	    if(genome[index]>yu) genome[index]=yu;
	    }
	
	long randomValueFromClosedIntervalCoral(long min, long max, MersenneTwisterFast random)
    {
    if (max - min < 0) // we had an overflow
        {
        long l = 0;
        do l = random.nextInt();
        while(l < min || l > max);
        return l;
        }
    else return min + random.nextLong(max - min + 1);
    }

}
