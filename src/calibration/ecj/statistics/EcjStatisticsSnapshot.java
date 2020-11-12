package calibration.ecj.statistics;
import calibration.EcjInterface;
import ec.EvolutionState;
import ec.Statistics;
import ec.simple.SimpleStatistics;
import ec.steadystate.SteadyStateStatisticsForm;
import ec.util.Parameter;

// TODO Remove this class

/**
 * This class is used to generate snapshots for the current run of the algorithm
 * ECJ can make statistics after evaluating an individual just in the
 * case in which we are using a SSGA. In general, we will use as a condition for
 * taking a snapshot, a given number of generations.
 * 
 * In the ecj structure, this object depends on stat.chlid.x
 * 
 * This class takes two new parameters from the ecj configuration file
 * 		activate: Indicates weather or not the user wants to take snapshots
 * 		generations: Number of generations between each snapshot
 *  
 * @author jjpalacios
 *
 */
public class EcjStatisticsSnapshot extends Statistics implements SteadyStateStatisticsForm
    {
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	
//	/**
//	 * Calibration Controller to evaluate the fitness.
//	 */
//	private EcjInterface ecjInterface;
	/**
	 * Check if the class has been initialized correctly
	 */
	private boolean classInitialized = false;
	
    
    /**
	 * Number of evaluations between snapshots
	 */
    public static final String P_SNAP_INTERVAL = "generations";
    public int generations;

    
	//=========================================================================
	//		CONSTRUCTORS / INITIALIZERS
	//=========================================================================
    /**
	 * Initialize the evaluation function using a ecjInterface object.
	 * 
	 * @param ecjInterface Data required for the evaluation 
	 */
	public void init(EcjInterface ecjInterface) {
//		this.ecjInterface = ecjInterface;
		this.classInitialized = true;
	}
	
	/**
	 * Initializing function for ECJ objects. Its structure is fixed
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param base ECJ object that contains the root for parameter names
	 */
    public void setup(final EvolutionState state, final Parameter base) {
        // Call the parent constructor
        super.setup(state,base);
        
		// Read the number of generations between snapshots (1 by default)
		generations = state.parameters.getIntWithDefault(base.push(P_SNAP_INTERVAL), null, 1);
		if(generations <= 0) {
			state.output.fatal("The interval between snapshots must be a value "
					+ "grater than 0", null);
		}
    }
    	
    	
	//=========================================================================
	//		METHODS
	//=========================================================================
    /**
	 * This function takes a snapshot of the best individual every interval evaluations.
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 */
	public void postEvaluationStatistics(final EvolutionState state)
    {
		// 	be certain to call the hook on super!
		super.postEvaluationStatistics(state);
		
		// Do not generate a snapshot before initializing the population
		if(state.generation == 0 || state.generation % generations != 0)
			return;
		
		if(!classInitialized) {
        	state.output.fatal("The object CalibrationController has not"
            		+ " been provided to ECJ",null);
        }
		
		// Take the best solution found so far by the algorithm
		if (!(state.statistics instanceof SimpleStatistics))
			state.output.fatal("The obtained statistics are not in the right format");
			
//		Individual[] inds = ((SimpleStatistics)(state.statistics)).getBestSoFar();
		    	        
//		if(inds.length > 0) {		    		
//		    try {
//		    	// Generate the snapshot
////		    	ecjInterface.snapshotInterface(
////		    			state.generation*state.population.subpops[0].individuals.length,
////						inds[0]);
//			} catch (CalibrationException | IOException | SimulationException e) {
//				state.output.fatal(e.getMessage());
//			}
//		 }
		 
//		else
//			state.output.fatal("No solution was generated");
    }
}
	
	