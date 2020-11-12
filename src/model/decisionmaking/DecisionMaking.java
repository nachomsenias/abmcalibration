package model.decisionmaking;

import util.exception.simulation.NoAwarenessException;
import util.functions.ArrayFunctions;
import util.functions.Functions;
import util.functions.MatrixFunctions;
import util.random.Randomizer;

/**
 * Defines the decision making module. It is the core class responsible
 * for all the decisions taken. It consists of four heuristics for decision
 * making: utility maximization, majority rule, elimination-by-aspects, and
 * satisficing. Its responsibilities are following:
 * 1.) It decides (using one of heuristics) which brand to choose when:
 * 	- buying (One heuristic is chosen; it uses awareness, drivers and perceptions)
 *  - talking (The same as buying, but since we can talk about positive and 
 * negative attributes, we take an "absolute value" of the negative perceptions
 * for the calculations. The values below 5 are transformed using "10-value".)
 *  - posting online (The same as talking)
 * 2.) It chooses the attributes of the brand when:
 *  - diffusing awareness (so far one at random)
 *  - diffusing perceptions (so far all of them)
 *  - posting online (so far all of them)
 */
public class DecisionMaking {
	/**
	 * Default number of heuristics.
	 */
	private static final int NR_OF_HEURISTICS = 4;
	/**
	 * Utility maximization index.
	 */
	private static final int UTILITY_MAXIMIZATION = 0;
	/**
	 * Majority rule index.
	 */
	private static final int MAJORITY_RULE = 1;
	/**
	 * Elimination by aspects index.
	 */
	private static final int ELIMINATION_BY_ASPECTS = 2;
	/**
	 * Satisficing index.
	 */
	private static final int SATISFICING = 3;
	
	/**
	 * Maximum possible value for a single driver.
	 */
	public static final double DRIVER_MAX = 1.0;
	/**
	 * Minimum possible value for a single driver.
	 */
	public static final double DRIVER_MIN = 0.0;
	/**
	 * Normalization sum value for all the drivers.
	 */
	public static final double DRIVERS_SUM = 1.0;
	
	/**
	 * Default emotional value.
	 */
	public static final double DEFAULT_EMOTIONAL_VALUE=0.5;
	/**
	 * Default involved value.
	 */
	public static final double DEFAULT_INVOLVED_VALUE=0.5;
	
	/**
	 * Buying decision type.
	 */
	private static final boolean BUY_BRAND = false;
	/**
	 * Talk and Post decision type.
	 */
	private static final boolean TALK_POST_ABOUT_BRAND = true;
	
	/**
	 * The randomizer instance used by the heuristics.
	 */
	Randomizer random;
	/**
	 * Selection probabilities for all the 4 heuristics.
	 */
	private double[] heuristicSelectionProb;
	
	/**
	 * Number of brands.
	 */
	private int nrBrands;
	/**
	 * Number of attributes.
	 */
	private int nrAttributes;
	
	// Heuristic class objects 
	private AbstractHeuristic utilityMaximization;
	private AbstractHeuristic majorityRule;
	private AbstractHeuristic eliminationByAspects;
	private AbstractHeuristic satisficing;
	
	// Log info
	private static String[] heuristicNames = {"UMAX", "MRULE", "EBA", "SAT"};
	private String logDM;
	
	// ########################################################################
	// Constructors
	// ######################################################################## 	
	
	/**
	 * Initializes an instance of the decision making.
	 */
	public DecisionMaking(Randomizer r, double[][] drivers, double involved, 
			double nonInvolved, double emotional, double nonEmotional, 
			int nrAttributes, int nrBrands) {
		this.random = r;
		// Initialize heuristics
		this.utilityMaximization = new UtilityMaximization();
		this.majorityRule = new MajorityRule();
		this.eliminationByAspects = new EliminationByAspects();
		this.satisficing = new Satisficing();
		initializeRandomizer(r);
		this.nrAttributes = 0;
		this.logDM = "";
		initializeDrivers(drivers);
		initializeHeuristicSelectionProb(involved, nonInvolved, emotional, nonEmotional);
		this.nrAttributes = nrAttributes;
		this.nrBrands = nrBrands;
		initializePercArray();
	}
	
	// ########################################################################	
	// Getters / Setters
	// ########################################################################		

	public void setHeuristicSelectionProb(double[] heuristicSelectionProb) {
		this.heuristicSelectionProb = heuristicSelectionProb;
	}
	
	/**
	 * Gets the logDM string containing information about the last chosen heuristic.
	 * @return - the string containing the name of last used heuristic.
	 */
	public String getLogDM() {
		return this.logDM;
	}
	
	// ########################################################################	
	// Methods/Functions 	
	// ########################################################################
	
	/**
	 * Initializes the heuristic selection probabilities using the emotional 
	 * and involved values.
	 * @param involved the involved value [0, 1].
	 * @param nonInvolved 1 - involved.
	 * @param emotional the involved value [0, 1].
	 * @param nonEmotional 1 - emotional.
	 */
	private void initializeHeuristicSelectionProb(
		double involved, double nonInvolved, double emotional, double nonEmotional
	) {		
		heuristicSelectionProb = new double[NR_OF_HEURISTICS];
		heuristicSelectionProb[UTILITY_MAXIMIZATION] = involved * nonEmotional;
		heuristicSelectionProb[MAJORITY_RULE] = involved * emotional;
		heuristicSelectionProb[ELIMINATION_BY_ASPECTS] = nonInvolved * nonEmotional;
		heuristicSelectionProb[SATISFICING] = nonInvolved * emotional;
	}
	
	/**
	 * Initialize the drivers values using the weights defined by model 
	 * definition.
	 * @param drivers the weights values as a double matrix.
	 */
	private void initializeDrivers(double[][] drivers) {
		//Checks the matrix dimensions.
		MatrixFunctions.checkMatrixBoundaries(drivers, DRIVER_MIN, DRIVER_MAX);
		MatrixFunctions.checkMatrixSum(drivers, DRIVERS_SUM);
				
		utilityMaximization.setDrivers(drivers);
		majorityRule.setDrivers(drivers);
		eliminationByAspects.setDrivers(drivers);
		satisficing.setDrivers(drivers);
	}
	
	/**
	 * Initialize the perc array using the number of brands and attributes.
	 */
	private void initializePercArray() {
		double[][] perc = new double[this.nrBrands][nrAttributes];
		
		utilityMaximization.setPerc(perc);
		majorityRule.setPerc(perc);
		eliminationByAspects.setPerc(perc);
		satisficing.setPerc(perc);
	}
	
	/**
	 * Sets the given random number generator instance for every heuristic.
	 * @param r the random number instance.
	 */
	private void initializeRandomizer(Randomizer r) {
		utilityMaximization.setRandom(r);
		majorityRule.setRandom(r);
		eliminationByAspects.setRandom(r);
		satisficing.setRandom(r);
	}

	//-------------------------- Execution methods --------------------------//

	/**
	 * Selects one brand using on of the heuristics. The heuristic is chosen
	 * based on the level of involvement and emotional variables. There must be
	 * at least two brands that the client agent is aware of. When there is 
	 * only one brand, that brand is returned. Finally, when there is no brand.
	 * The exception is thrown.
	 * @param decisionType - The flag indicating whether we talk/post (true) or
	 * we buy (false).
	 * @param awareness - the awareness of the current client agent.
	 * @param perceptions - the perceptions of the current client agent.
	 * @param segment - the index of the segment that the client agent is a member.
	 * @return - the index of the selected brand.
	 * @throws NoAwarenessException 
	 */
	private int selectOneBrandHeuristics(
			boolean decisionType, 
			boolean[] awareness, 
			double[][] perceptions, 
			int segment) throws NoAwarenessException {
		
		int brand;
		int typeOfHeuristic;
		int counter = 0;
		int ind = -1;
		//TODO [KT] separate this method...
		typeOfHeuristic = selectHeuristic();
		// Log type of the heuristic
		this.logDM = DecisionMaking.heuristicNames[typeOfHeuristic];
		// Check awareness
		for(int i=0; i<awareness.length; i++) {
			if(awareness[i]) {
				counter++;
				ind = i;
			}
		}
		//Check if perceptions are in the range [0,10]
		MatrixFunctions.checkMatrixBoundaries(perceptions, 
				AbstractHeuristic.PERCEPTION_MIN, 
				AbstractHeuristic.PERCEPTION_MAX);
		
		// If there is only one brand, return it.
		if (counter == 1) return ind;
		// no awareness case TODO [KT] throws exception so far... 
		// [IM] There is not enough information at this level to identify
		// properties like agent id for agents with no awareness, maybe this
		// should be done at the agent level.
		else if(counter == 0) {
			throw new NoAwarenessException("No awareness! DM heuristic " + this.logDM);
		} else {
			if(decisionType) {
				utilityMaximization.setAbsoluteValPerceptions(TALK_POST_ABOUT_BRAND);
				majorityRule.setAbsoluteValPerceptions(TALK_POST_ABOUT_BRAND);
				eliminationByAspects.setAbsoluteValPerceptions(TALK_POST_ABOUT_BRAND);
				satisficing.setAbsoluteValPerceptions(TALK_POST_ABOUT_BRAND);
			} else {
				utilityMaximization.setAbsoluteValPerceptions(BUY_BRAND);
				majorityRule.setAbsoluteValPerceptions(BUY_BRAND);
				eliminationByAspects.setAbsoluteValPerceptions(BUY_BRAND);
				satisficing.setAbsoluteValPerceptions(BUY_BRAND);		
			}

			switch (typeOfHeuristic) {
				case UTILITY_MAXIMIZATION:
					brand = utilityMaximization.calculate(awareness, perceptions, segment);
					break;
				case MAJORITY_RULE:
					brand = majorityRule.calculate(awareness, perceptions, segment);
					break;
				case ELIMINATION_BY_ASPECTS:
					brand = eliminationByAspects.calculate(awareness, perceptions, segment);
					break;
				case SATISFICING:
					brand = satisficing.calculate(awareness, perceptions, segment);
					break;
				default:
					throw new IllegalArgumentException("Unknown heuristic rule");
			}
			return brand;			
		}			
	}
	
	/**
	 * Decides which product to buy using one of four heuristics implemented.
	 * @return - the product that the consumer agent will buy.
	 * @throws NoAwarenessException 
	 */
	public int buyOneBrand(
			boolean[] awareness, double[][] perceptions, int segment
			) throws NoAwarenessException {
		return selectOneBrandHeuristics(BUY_BRAND, awareness, perceptions, segment);
	}
	
	/**
	 * Decides which product to buy using one of four heuristics implemented.
	 * @return - the product that the consumer agent will buy.
	 * @throws NoAwarenessException 
	 */
	public int buyRandom(
			boolean[] awareness) {
		return ArrayFunctions.selectRandomIndex(awareness, random);
	}
	
	/**
	 * Selects one of the decision making heuristics, which eventually will 
	 * select a brand, based on the weights assigned to each heuristic.
	 * @return - the id of the heuristic to be chosen.
	 */
	public int selectHeuristic() {
		return Functions.randomWeightedSelection(
			heuristicSelectionProb, 
			random.nextDouble() // [0, 1)
		);
	}       	
}
