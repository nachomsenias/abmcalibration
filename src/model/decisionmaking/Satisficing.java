package model.decisionmaking;

import util.functions.ArrayFunctions;
import util.functions.Functions;

/**
 * Implements the satisficing heuristic, which is considered as
 * non-involved and emotional. It chooses brands at random and checks if the
 * current one fulfills the given criteria. The first one doing so is returned.
 */	
public class Satisficing extends AbstractHeuristic {

	// ########################################################################
	// Constructors
	// ######################################################################## 	
	
	/**
	 * Initializes an instance of the Satisficing class.
	 */
	public Satisficing() {
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
		// Used for Posting and Talking
		// As we tend to talk about the products having attributes with
		// boundary values (very good and very bad either!), the "absolute values"
		// (10-val) are taken for the negative attribute values (<5.0).
		// It results with values in a scale [5,10], which is transformed back
		// to the perceptions scale [1,10].
		if(absoluteValPerceptions) {
			currentPerceptions = super.absoluteValuePerceptions(perceptions);
		// Used for buying
		// The standard behavior is to select a product based on its 
		// attribute values. The higher value, the better chance to be bought.	
		} else {
			currentPerceptions = perceptions;
		}		
		
		int nrBrands = currentPerceptions.length;
		int nrAttributes = currentPerceptions[0].length;
		double[] currentCutoffs = new double[nrAttributes];
		boolean flag1 = false;	
		int selectedBrand = -1;
		
		for(int i=0; i<nrAttributes; i++) {
			// Used for Posting and Talking
			// Since the absolute values are taken to transform the perception
			// values, we do the same with the cutoffs. We generate them
			// in a scale [0,5] and then transform them to the original 
			// perception scale [0,10].
			if(absoluteValPerceptions) {
				double val = random.nextDouble() // [0, 1)
						* (PERCEPTION_TALK_POST_SCALE_MAX);
				currentCutoffs[i] = Functions.normalizeMinMax(
						val, 
						PERCEPTION_MIDDLE,
						PERCEPTION_MAX,
						PERCEPTION_MIN,
						PERCEPTION_MAX
				);	
			// Used for buying
			// The cutoffs are random values generated from range [0,10]	
			} else {
				currentCutoffs[i] = random.nextDouble() // [0, 1)
						* Functions.PERCEPTION_MULTIPLIER_SCALE;				
			}
		}
		byte[] brandIndexes = ArrayFunctions.shuffleFast((byte) nrBrands, random);
		
		while(!flag1) {
			for(int i=0; i<nrBrands; i++) {
				if(awareness[brandIndexes[i]]){
					// Check the cutoff criteria
					// TODO [KT] do not pass perceptions and drivers as argument?
					if(checkCutoffCriteria(currentPerceptions[brandIndexes[i]], currentCutoffs)) {
						selectedBrand = brandIndexes[i];
						flag1 = true;
						break;
					}					
				}	
			}
			if(!flag1) {
				for(int i=0; i<nrAttributes; i++) {
					currentCutoffs[i] -= CUTOFF_DECREASE;
				}				
			}
		}
		return selectedBrand;
	}

	/**
	 * Checks if the given perceptions meet the cutoff criteria.
	 * @param perceptions - the perceptions of the current client agent.
	 * @param cutoffs - the cutoffs for the agent's perceptions.
	 * @return - true if the criteria are fulfilled, false otherwise.
	 */
	private boolean checkCutoffCriteria(double[] perceptions, double[] cutoffs) {
		boolean flag = true;
		for(int i=0; i<perceptions.length; i++) {
			if(perceptions[i] < cutoffs[i]) {
				flag = false;
				break;
			}
		}
		return flag;
	}
}
