package calibration.ecj.statistics;

import ec.Statistics;
import ec.steadystate.SteadyStateStatisticsForm;
import ec.steadystate.SteadyStateEvolutionState;
import ec.Individual;
import ec.vector.ByteVectorIndividual;
import ec.vector.DoubleVectorIndividual;
import ec.vector.IntegerVectorIndividual;
import ec.vector.LongVectorIndividual;
import ec.vector.ShortVectorIndividual;


/**
 * This class is used to include a specific individual in the initial
 * population. The main idea is to use the current model as input for 
 * the calibrator so we cannot get worse results
 * 
 * In the ecj structure, this object depends on stat.chlid.x
 * 
 * Objects of this class must be initialized through the init method.
 *   
 * @author jjpalacios
 *
 */

public class EcjSteadyInitialSolution
extends Statistics implements SteadyStateStatisticsForm
    {
	
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Initial set of parameters
	 */
	double[] initialParameters;
	
	/**
	 * Initialize the object with a set of initial parameters
	 * 
	 * @param initialParams Array of genes for the initial iteration 
	 */
	public void init(double[] initialParams) {
		this.initialParameters = initialParams;
	}
	
	        	
    	
	//=========================================================================
	//		METHODS
	//=========================================================================
    /**
	 * This function takes one individual at random from the initial population
	 * and replace it by the set of parameters provided
	 * 
	 * @param subpop Subopopulation to which apply the operator
	 * @param state ECJ object that contains all the information of the algorithm.
	 */
	public void enteringSteadyStateStatistics(int subpop, SteadyStateEvolutionState state)
    {
		// 	be certain to call the hook on super!
		super.enteringSteadyStateStatistics(subpop, state);
		
		// Take one individual at random
		Individual ind = state.population.subpops[subpop].individuals[0];
		
		// And update its genome
		if(ind instanceof IntegerVectorIndividual) {
			int[] genotype = ((IntegerVectorIndividual)ind).genome;
			for(int i=0; i < initialParameters.length; i++) {
				genotype[i] = (int)initialParameters[i];
			}
		}
		else if(ind instanceof DoubleVectorIndividual) {
			double[] genotype = ((DoubleVectorIndividual)ind).genome;
			for(int i=0; i < initialParameters.length; i++) {
				genotype[i] = (double)initialParameters[i];
			}
		}
		
		else if(ind instanceof ByteVectorIndividual) {
			byte[] genotype = ((ByteVectorIndividual)ind).genome;
			for(int i=0; i < initialParameters.length; i++) {
				genotype[i] = (byte)initialParameters[i];
			}
		}
		
		else if(ind instanceof ShortVectorIndividual) {
			short[] genotype = ((ShortVectorIndividual)ind).genome;
			for(int i=0; i < initialParameters.length; i++) {
				genotype[i] = (short)initialParameters[i];
			}
		}
		
		else if(ind instanceof LongVectorIndividual) {
			long[] genotype = ((LongVectorIndividual)ind).genome;
			for(int i=0; i < initialParameters.length; i++) {
				genotype[i] = (long)initialParameters[i];
			}
		}
		ind.evaluated = false;
		state.evaluator.evaluatePopulation(state);
    }
	
}
	
	