package calibration.ecj.multimodal;

import ec.*;
import ec.util.*;

import ec.vector.*;

/*
 *  MMDoubleVectorIndividual.java
 * Created: 2018
 * Class to adapt a Double vector individual with custom distance to other inds and blx-alpha as crossover
 * @author ebermejo
 *
 */

public class MMDoubleVectorIndividual extends DoubleVectorIndividual
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
        if (!(otherInd instanceof DoubleVectorIndividual)) 
            return super.distanceTo(otherInd);  // will return infinity!
        DoubleVectorIndividual other = (DoubleVectorIndividual) otherInd;
        double[] otherGenome = other.genome;
        double sumSquaredDistance =0.0;
        for(int i=0; i < other.genomeLength(); i++)
            {
            double dist = otherGenome[i]- this.genome[i];
            //Normalize distance
            dist/=(((FloatVectorSpecies)this.species).maxGene(i)-((FloatVectorSpecies)this.species).minGene(i));
            sumSquaredDistance += dist*dist;
            }
       // sumSquaredDistance /= this.genomeLength(); //Normalize Distance per dimension
        return (StrictMath.sqrt(sumSquaredDistance)/this.genomeLength());
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

  	 
	
	
    }
