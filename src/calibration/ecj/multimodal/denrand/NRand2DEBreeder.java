package calibration.ecj.multimodal.denrand;

import ec.*;
import ec.util.*;
import ec.vector.*;
import ec.de.*;

  

public class NRand2DEBreeder extends DEBreeder
    { 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state,base);
 
        if (state.parameters.exists(base.push(P_Cr), null))
            state.output.warning("Crossover parameter specified, but NRand2DEBreeder does not use crossover.", base.push(P_Cr));
        }
        
    public DoubleVectorIndividual createIndividual( final EvolutionState state,
        int subpop,
        int index,
        int thread )
        {
        Individual[] inds = state.population.subpops[subpop].individuals;

        DoubleVectorIndividual v = (DoubleVectorIndividual)(state.population.subpops[subpop].species.newIndividual(state, thread));
        int retry = -1;
        do
            {
            retry++;
            
            // select three indexes different from each other and from that of the current parent
            int r1, r2, r3, r4;
            do
                {
                r1 = state.random[thread].nextInt(inds.length);
                }
            while( r1 == index );
            do
                {
                r2 = state.random[thread].nextInt(inds.length);
                }
            while( r2 == r1 || r2 == index );
            do
                {
                r3 = state.random[thread].nextInt(inds.length);
                }
            while( r3 == r2 || r3 == r1 || r3 == index );
            do
            {
            r4 = state.random[thread].nextInt(inds.length);
            }
            while( r4 == r3 || r4 == r2 || r4 == r1 || r4 == index );
            int r0=getNN(inds,index);
            DoubleVectorIndividual g0 = (DoubleVectorIndividual)(inds[r0]);
            DoubleVectorIndividual g1 = (DoubleVectorIndividual)(inds[r1]);
            DoubleVectorIndividual g2 = (DoubleVectorIndividual)(inds[r2]);
            DoubleVectorIndividual g3 = (DoubleVectorIndividual)(inds[r3]);
            DoubleVectorIndividual g4 = (DoubleVectorIndividual)(inds[r4]);

            for(int i = 0; i < v.genome.length; i++) 
                    v.genome[i] = g0.genome[i] + F * (g1.genome[i] - g2.genome[i])+F * (g3.genome[i] - g4.genome[i]);  
             }
        while(!valid(v) && retry < retries);
        if (retry >= retries && !valid(v))  // we reached our maximum
            {
            // completely reset and be done with it
            v.reset(state, thread);
            }

        return v;       // no crossover is performed
     //   return crossover(state, (DoubleVectorIndividual)(inds[index]), v, thread);
        }



	private int getNN(Individual[] inds, int index) {
		double dist=Double.POSITIVE_INFINITY;
		int nn=index;
		// TODO Auto-generated method stub
		for (int i=0; i<inds.length;i++) 
			if(i!=index) {
				DoubleVectorIndividual me = (DoubleVectorIndividual) inds[index];
				DoubleVectorIndividual other = (DoubleVectorIndividual) inds[i];
				double current=me.distanceTo(other);
				if(current<dist) {
					dist=current;
					nn=i;
				}
			}
		return nn;
	}

    }
