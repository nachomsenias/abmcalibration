package calibration.ecj.multimodal;

import ec.*;
import ec.util.*;

import ec.vector.*;

/*
 *  MMIntegerVectorIndividual.java
 * Created: 2018
 * Class to adapt a Integer vector individual with a custom distance to other inds
 * @author ebermejo
 *
 */

public class MMIntegerVectorIndividual extends IntegerVectorIndividual
    {
	private static final long serialVersionUID = 1L;
 

	/*BLX-alpha parameter*/
    public static final String P_ALPHA = "alpha";
    public double alpha;

  //=========================================================================
  	//		OVERRIDE OPERATIONS
  	//=========================================================================
	@Override
	public void setup(EvolutionState state, Parameter base) {
		// TODO Auto-generated method stub
		super.setup(state, base);
	 
	    alpha=state.parameters.getDouble(base.push(P_ALPHA), null,0.001);
		
		if (alpha<0)
			state.output.fatal("Parameter not found or its value is less than 0.",base.push(P_ALPHA),null);

	}
	
  	@Override
  	//TODO: Override calculating an euclidean/Hamming distance
    public double distanceTo(Individual otherInd)
        { 
        if (!(otherInd instanceof IntegerVectorIndividual)) 
            return super.distanceTo(otherInd);  // will return infinity!
        IntegerVectorIndividual other = (IntegerVectorIndividual) otherInd;
        int[] otherGenome = other.genome;
        double sumSquaredDistance =0.0;
        for(int i=0; i < other.genomeLength(); i++)
            {
            double dist = otherGenome[i]- this.genome[i];
            //Normalize distance
            dist/=(((IntegerVectorSpecies)this.species).maxGene(i)-((IntegerVectorSpecies)this.species).minGene(i));
            sumSquaredDistance += dist*dist;            
            }

        return (StrictMath.sqrt(sumSquaredDistance)/this.genomeLength());
        }
  	   
	public void BLXCrossover(EvolutionState state, int thread, VectorIndividual i) {		
		int[] ind = ((MMIntegerVectorIndividual)i).genome;
        double alpha = 3.0;
        
		IntegerVectorSpecies s = (IntegerVectorSpecies)species;
 
		for(int gene = 0; gene < ind.length; gene++) {
			int min = Math.min(this.genome[gene], ind[gene]);
			int max = Math.max(this.genome[gene], ind[gene]);
			
			// Compute the bounds of the interval
			int minInterval = (int) Math.ceil(min-alpha*(max-min));
			int maxInterval = (int) Math.floor(max+alpha*(max-min));
			
			// Generate random values in the interval
			((MMIntegerVectorIndividual)this).genome[gene] = randomValueFromClosedInterval(minInterval,maxInterval,state.random[thread]);
			((MMIntegerVectorIndividual)i).genome[gene] = randomValueFromClosedInterval(minInterval,maxInterval,state.random[thread]);
			
			// Check that the constraints hold
			if(this.genome[gene] < s.minGene(gene))
				this.genome[gene] = (int) s.minGene(gene);
			else if(this.genome[gene] > s.maxGene(gene))
				this.genome[gene] = (int) s.maxGene(gene);
			if(((MMIntegerVectorIndividual)i).genome[gene] < s.minGene(gene))
				((MMIntegerVectorIndividual)i).genome[gene] = (int) s.minGene(gene);
			else if(((MMIntegerVectorIndividual)i).genome[gene] > s.maxGene(gene))
				((MMIntegerVectorIndividual)i).genome[gene] = (int) s.maxGene(gene);
		}
		
		this.evaluated = false;
		i.evaluated = false;
			 
	}	
	
    }
