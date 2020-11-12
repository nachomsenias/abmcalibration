package model.decisionmaking;

import util.functions.ArrayFunctions;
import util.functions.Functions;
import util.random.Randomizer;

/**
 * Implements the majority rule heuristic, which is considered as
 * involved and emotional (TODO [KT] sure??). It selects a pair of brands
 * and compare their attributes. The brand with better attribute values
 * is selected in order to be compared with the brands that are left.  
 */
public class MajorityRule  extends AbstractHeuristic {

	// ########################################################################
	// Constructors
	// ######################################################################## 	
	
	/**
	 * Initializes an instance of the MajorityRule class.
	 */
	public MajorityRule() {
		super();
	}

	// ########################################################################	
	// Methods/Functions 	
	// ########################################################################	

	/**
	 * Calculates which brand is going to be chosen by the heuristic.
	 * @param awareness - the awareness of the current client agent.
	 * @param perceptions - the perceptions of the current client agent.
	 * @param segment - the index of the segment that the client agent is a member.
	 * @return - the index of the selected brand.
	 */
	@Override
	public int calculate(boolean[] awareness, double[][] perceptions, int segment) {
		double[][] currentPerceptions;
		if(absoluteValPerceptions) {
			currentPerceptions = super.absoluteValuePerceptions(perceptions);
		} else {
			currentPerceptions = perceptions;
		}
		
		int nrBrands = currentPerceptions.length;
		int ind1 = -1;
		int ind2 = -1;
		boolean flag = false;
		int counter = 0;
		
		byte[] brandIndexes = ArrayFunctions.shuffleFast((byte) nrBrands, random);
		
		// Take first shuffled index of a brand
		while(!flag) {
			if(awareness[brandIndexes[counter]]) {
				ind1 = brandIndexes[counter];
				flag = true;
			}
			counter++;
		}
		for(int i=counter; i<nrBrands; i++) {
			if(awareness[brandIndexes[i]]) {
				ind2 = brandIndexes[i];
				// TODO [KT] do not pass perceptions and drivers as argument
				ind1 = compareAttributes(
					random, ind1, ind2, drivers[segment], currentPerceptions
				);				
			}
		}
		return ind1;
	}
	
	/**
	 * Compares perceptions of two brands. For each winning attribute,
	 * the winning brand scores a weight taking a value from the 
	 * corresponding driver. After comparing all attributes, the probability
	 * of selecting a brand is constructed by the sum of the all weights.
	 * The final result is obtained using random weighted selection
	 * (In case of no probabilistic case, the brand with more wins is selected).
	 * @param r - the random number generator.
	 * @param ind1 - the index of the first brand.
	 * @param ind2 - the index of the second brand.
	 * @param drivers - the drivers of the given simulation.
	 * @param perceptions - the perceptions of the current client agent.
	 * @return - the index of the selected brand.
	 */
	private int compareAttributes(
		Randomizer r, int ind1, int ind2, double[] drivers, double[][] perceptions 
	) {
		final int NR_COMPARISONS = 2;
		final int INDEX_FIRST = 0;
		final int INDEX_SECOND = 1;
		double[] scores = new double[NR_COMPARISONS];
		double[] perceptions1 = perceptions[ind1];
		double[] perceptions2 = perceptions[ind2];
		boolean probabilistic = true;

		for(int i=0; i<perceptions1.length; i++) {
			if(perceptions1[i] > perceptions2[i]) {
				scores[INDEX_FIRST] += drivers[i];
			} else if (perceptions1[i] < perceptions2[i]) {
				scores[INDEX_SECOND] += drivers[i];
			} else if (perceptions1[i] == perceptions2[i]) {
				scores[INDEX_FIRST] += drivers[i]/2;
				scores[INDEX_SECOND] += drivers[i]/2;
			}
		}
		// (Stochastic) Choose randomly weighted by scores
		if(probabilistic){
			int res = Functions.randomWeightedSelection(
				scores, r.nextDouble() // [0, 1)
			);
			if(res == INDEX_FIRST) {
				return ind1;
			} else {
				return ind2;
			}
		// (Deterministic) Return the index with the highest score; 
		// otherwise choose randomly
		} else {
			if(scores[INDEX_FIRST] > scores[INDEX_SECOND]) {
				return ind1;
			} else if(scores[INDEX_FIRST] < scores[INDEX_SECOND]) {
				return ind2;
			} else {
				if(r.nextBoolean()) return ind1;
				else return ind2;
			}			
		}
	}
}
