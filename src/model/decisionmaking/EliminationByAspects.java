package model.decisionmaking;

import util.functions.Functions;
import util.random.Randomizer;

/**
 * Implements the elimination by aspects heuristic, which is considered as
 * non-involved and non-emotional (TODO [KT] sure??). It starts with the
 * most important attribute and rejects all the brands that do not fulfill
 * the given criteria. Then, the algorithm iterates over the attributes
 * in order of their importance and repeats the selection. When one brand
 * is left the algorithm finishes its execution. When all the brands are
 * eliminated, the criteria is loosen and the iteration is repeated.  
 */
public class EliminationByAspects extends AbstractHeuristic {

	// ########################################################################
	// Constructors
	// ######################################################################## 	
	
	/**
	 * Initializes an instance of the EliminationByAspects class.
	 */
	public EliminationByAspects() {
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
	public int calculate(boolean[] awareness, double[][] perceptions, int segment) 
		throws IllegalArgumentException{
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
		boolean[] rejectedBrands = new boolean[nrBrands];
		double[] currentCutoffs = new double[nrAttributes];
		int[] attributeIndexes = new int[nrAttributes];
		boolean flag = false;
		int initCount = 0;
		int counter = 0;

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
			// The cutoffs are random values generated from the standard
			// perception range [0,10]	
			} else {
				currentCutoffs[i] = random.nextDouble() // [0, 1)
					* Functions.PERCEPTION_MULTIPLIER_SCALE;			
			}
		}		
		for(int i=0; i<nrAttributes; i++) {
			attributeIndexes[i] = i;
		}
		attributeIndexes = 
			Functions.getIndicesRandomWeightedOrder(drivers[segment], random);
		
		// The brands without awareness are rejected by default
		for(int i=0; i<nrBrands; i++) {
			if(!awareness[i]) {
				rejectedBrands[i] = true;	
				initCount++;
			}
		}
		counter = initCount;
		
		while(!flag) {
			flag = true;
			// Check for each attribute if the brand fulfills the criteria
			for(int i=0; i<nrAttributes; i++) {
				for(int j=0; j<nrBrands; j++) {
					if(!rejectedBrands[j] 
						&&	currentPerceptions[j][attributeIndexes[i]] 
						< currentCutoffs[attributeIndexes[i]]
					) {
							rejectedBrands[j] = true;
							counter++;
					}
				}
				// If there are no brands, decrease the criteria
				// and set flag to repeat iteration
				if(counter == nrBrands) {
					for(int k=0; k<nrAttributes; k++) {
						currentCutoffs[k] -= CUTOFF_DECREASE;
					}
					for(int k=0; k<nrBrands; k++) {
						if(awareness[k]){
							rejectedBrands[k] = false;							
						}
					}			
					counter = initCount;
					flag = false;
					break;
				// If there is only one brand left, exit 	
				} else if(counter == (nrBrands - 1)) {
					break;
				}
			}
		}
		return selectOneFromBooleanArray(rejectedBrands, (nrBrands - counter), random);
	}
	
	/**
	 * Selects randomly one index (brand's index) from the boolean array,
	 * which is not set to false.  
	 * @param array - the array of boolean values.
	 * @param nrOfFalse - the number of values set to false.
	 * @param r - the random number generator.
	 * @return - the index of the selected brand.
	 */
	private int selectOneFromBooleanArray(
			boolean[] array, int nrOfFalse, Randomizer r
	) {
		int nrBrands = array.length;
		// Only one brand left
		if(nrOfFalse == 1) {
			for(int i=0; i<nrBrands; i++) {
				if(!array[i]) {
					return i;
				}
			}
			throw new IllegalArgumentException(
				"There must be a false value in the array"
			);
		// several brands left, select one at random from the ones left
		} else {
			int[] brandIndexes = new int[nrOfFalse];
			int counter = 0;
			for(int i=0; i<nrBrands; i++) {
				if(!array[i]) {
					brandIndexes[counter] = i;
					counter++;
				}
			}
			return brandIndexes[r.nextInt(nrOfFalse)];
		}
	}	
}
