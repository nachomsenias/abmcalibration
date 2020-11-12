package calibration.ecj.statistics;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.vector.ByteVectorIndividual;
import ec.vector.DoubleVectorIndividual;
import ec.vector.IntegerVectorIndividual;
import ec.vector.LongVectorIndividual;
import ec.vector.ShortVectorIndividual;

public class EcjInitialSolution extends Statistics {

	// =========================================================================
	// FIELDS
	// =========================================================================
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
	 * @param initialParams
	 *            Array of genes for the initial iteration
	 */
	public void init(double[] initialParams, final EvolutionState state) {
		this.initialParameters = initialParams;
		
		Individual ind = state.population.subpops[0].individuals[0];

		// And update its genome
		if (ind instanceof IntegerVectorIndividual) {
			int[] genotype = ((IntegerVectorIndividual) ind).genome;
			for (int i = 0; i < initialParameters.length; i++) {
				genotype[i] = (int) initialParameters[i];
			}
		} else if (ind instanceof DoubleVectorIndividual) {
			double[] genotype = ((DoubleVectorIndividual) ind).genome;
			for (int i = 0; i < initialParameters.length; i++) {
				genotype[i] = (double) initialParameters[i];
			}
		}

		else if (ind instanceof ByteVectorIndividual) {
			byte[] genotype = ((ByteVectorIndividual) ind).genome;
			for (int i = 0; i < initialParameters.length; i++) {
				genotype[i] = (byte) initialParameters[i];
			}
		}

		else if (ind instanceof ShortVectorIndividual) {
			short[] genotype = ((ShortVectorIndividual) ind).genome;
			for (int i = 0; i < initialParameters.length; i++) {
				genotype[i] = (short) initialParameters[i];
			}
		}

		else if (ind instanceof LongVectorIndividual) {
			long[] genotype = ((LongVectorIndividual) ind).genome;
			for (int i = 0; i < initialParameters.length; i++) {
				genotype[i] = (long) initialParameters[i];
			}
		}
		ind.evaluated = false;
	}

//	// =========================================================================
//	// METHODS
//	// =========================================================================
//
//	public void postInitializationStatistics(final EvolutionState state) {
//		super.postInitializationStatistics(state);
//
//		// Take one individual at random
//		Individual ind = state.population.subpops[0].individuals[0];
//
//		// And update its genome
//		if (ind instanceof IntegerVectorIndividual) {
//			int[] genotype = ((IntegerVectorIndividual) ind).genome;
//			for (int i = 0; i < initialParameters.length; i++) {
//				genotype[i] = (int) initialParameters[i];
//			}
//		} else if (ind instanceof DoubleVectorIndividual) {
//			double[] genotype = ((DoubleVectorIndividual) ind).genome;
//			for (int i = 0; i < initialParameters.length; i++) {
//				genotype[i] = (double) initialParameters[i];
//			}
//		}
//
//		else if (ind instanceof ByteVectorIndividual) {
//			byte[] genotype = ((ByteVectorIndividual) ind).genome;
//			for (int i = 0; i < initialParameters.length; i++) {
//				genotype[i] = (byte) initialParameters[i];
//			}
//		}
//
//		else if (ind instanceof ShortVectorIndividual) {
//			short[] genotype = ((ShortVectorIndividual) ind).genome;
//			for (int i = 0; i < initialParameters.length; i++) {
//				genotype[i] = (short) initialParameters[i];
//			}
//		}
//
//		else if (ind instanceof LongVectorIndividual) {
//			long[] genotype = ((LongVectorIndividual) ind).genome;
//			for (int i = 0; i < initialParameters.length; i++) {
//				genotype[i] = (long) initialParameters[i];
//			}
//		}
//		ind.evaluated = false;
////		state.evaluator.evaluatePopulation(state);
//	}
}