package model.decisionmaking;

import util.functions.Functions;

/**
 * Implements the utility maximization heuristic, which is considered as 
 * involved and non-emotional. It considers the value of each brand by
 * using a product of drivers and perceptions. The brand is chosen using
 * the weighted random selection using the calculated values of each brand. 
 */
public class UtilityMaximization extends AbstractHeuristic {
	//TODO [KT] Adjust the scale properly for the multinomial logit model!!!
	private static final double SCALE = 1;

	// ########################################################################
	// Constructors
	// ######################################################################## 	
	
	/**
	 * Initializes an instance of the UtilityMaximization class.
	 */
	public UtilityMaximization() {
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
		int nrAttributes = currentPerceptions[0].length;
		double[] prob = new double[nrBrands];
		double sum = 0;
		boolean[] restricted = new boolean[nrBrands];
		
		// Calculate probability for each brand
		for(int i=0; i<nrBrands; i++) {
			if(awareness[i]) {
				for(int j=0; j<nrAttributes; j++) {
					prob[i] += drivers[segment][j] * currentPerceptions[i][j] / SCALE;			
				}
				prob[i] = Math.exp(prob[i]);
				sum += prob[i];
			}
		}
		
		for(int i=0; i<nrBrands; i++) {
			if(awareness[i]) {
				prob[i] /= sum;				
			}
			restricted[i] = !awareness[i];
		}
		return Functions.randomWeightedSelectionRestricted(
			prob, restricted, random.nextDouble() // [0, 1)
		);
	}
	
}
