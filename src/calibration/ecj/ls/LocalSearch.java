package calibration.ecj.ls;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ec.vector.IntegerVectorIndividual;


/**
 * This interface defines how Local Search strategies must be implemented
 * so the other components in ECJ recognize them.
 * Local Search strategies take an individual as input and improve it
 * iteratively until a stopping criterion is met.
 * 
 * For any kind of Local Search, we look for these parameters:
 *  -	max-iterations: Maximum number of iterations for the local search. It
 *  		is an optional parameter.
 * 	-	threshold: Minimum improvement to consider a neighbor as an improving
 * 			neighbor
 *   
 * @author jjpalacios
 *
 */
public abstract class LocalSearch {
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/**
	 * Parameter indicating the maximum number of iterations for the local
	 * search
	 */
	public static final String P_ITERATIONS = "max-iterations";
	public int maxIterations;
	
	/**
	 * Parameter indicating the maximum number of evaluations to be made by
	 * the local search
	 */
	public static final String P_EVALUATIONS = "max-evaluations";
	public int maxSteps;

	public int totalEvaluations;
	
	/**
	 * Minimum improvement to consider a neighbor as an improving one
	 */
	public static final String P_THRESHOLD = "threshold";
	public double threshold;
	
	public static final String P_STEP = "step";
	public double step;	
	
	protected EvolutionState state;
	
	
	
	//=========================================================================
	//		CONSTRUCTORS / INITIALIZERS
	//=========================================================================
	/**
	 * Default constructor. Nothing to do
	 */
	public LocalSearch() { }
	
	
	/**
	 * Initializing function for ECJ objects. Its structure is fixed
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param base ECJ object that contains the root for parameter names
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		 // Reads the parameter for the maximum number of iterations.
        maxIterations = state.parameters.getIntWithDefault(
        		base.push(P_ITERATIONS), null, Integer.MAX_VALUE);
        
        // Reads the parameter for the maximum number of evaluations.
        maxSteps = state.parameters.getIntWithDefault(
        		base.push(P_EVALUATIONS), null, Integer.MAX_VALUE);
        
        // Reads the parameter for the threshold.
        threshold = state.parameters.getDoubleWithDefault(
        		base.push(P_THRESHOLD), null, 0.01);
        
        // Reads the step for real coded optimization
        step = state.parameters.getDoubleWithDefault(
        		base.push(P_STEP), null, 0.001);
        
        this.state=state;
	}
	
	
	/**
	 * Apply a Local Search algorithm to a given individual.
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param individual Individual to improve through the Local Search
	 * @param subpopulation Subpopulation to which the individual belongs. This is
	 * 	necessary to evaluate it.
	 */
	public abstract void apply(final EvolutionState state,
			Individual individual);
	
	/**
	 * Moves the current solution towards a neighbor
	 * 
	 * @param source Current solution
	 * @param target Solution to which we want to move
	 */	
	protected void moveToSolution(Individual source, Individual target) {
		if(source instanceof IntegerVectorIndividual) {
			((IntegerVectorIndividual)source).genome =
					((IntegerVectorIndividual)target).genome;
		}
		else if(source instanceof DoubleVectorIndividual) {
			((DoubleVectorIndividual)source).genome =
					((DoubleVectorIndividual)target).genome;
		}
		source.fitness = target.fitness;
	}
}
