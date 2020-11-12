package calibration.ecj;
import ec.*;
import ec.steadystate.SteadyStateBSourceForm;
import ec.steadystate.SteadyStateEvolutionState;
import ec.util.*;


/**
 * This class is used to select the worst individual in the population.
 * This is needed for SSGA to work well
 * 
 * In the ecj structure, this object is specified in steady.deselector
 * 
 * This class does not take new parameters from the ecj configuration file
 *   
 * @author jjpalacios
 *
 */

public class EcjSteadySelection extends SelectionMethod implements SteadyStateBSourceForm
    {
	
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String P_STEADYSELECTION = "steady-selection";
	public static final String P_REPEATED = "replace-repeated";
	
	public boolean replaceRep = false;
	
	/**
	 * Replacement probability
	 */
	private double replacementProb;
    
    
	//=========================================================================
	//		CONSTRUCTORS / INITIALIZERS
	//=========================================================================
    /**
	 * Initializing function for ECJ objects. Its structure is fixed
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param base ECJ object that contains the root for parameter names
	 */
    public void setup(final EvolutionState state, final Parameter base) {
        // Call the parent constructor
        super.setup(state,base);
        
        // Looks for the allowance of repeated elements
        this.replaceRep = state.parameters.getBoolean(base.push(P_REPEATED), null, false);
        
        // Read the replacement probability for the deselector
        this.replacementProb = state.parameters.getDoubleWithDefault(
        		new Parameter("steady.replacement-probability"), null, 1.0);
    }
    
	/**
	 * Specify the name of the parameter option
	 */
	public Parameter defaultBase() { return new Parameter(P_STEADYSELECTION); }
    	
	
	//=========================================================================
	//		METHODS
	//=========================================================================
    /**
	 * This function looks for the worst individual in the population
	 * 
	 * @param subpopulation Number of sub-population to which apply the selection .
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param thread Id of the thread for parallel computation
	 */
	 public int produce(final int subpopulation, final EvolutionState state, final int thread)
     {
		 int pick=0;
		 Individual [] pop = state.population.subpops[subpopulation].individuals;
		 Fitness worstFitness = pop[0].fitness;

		 // If there is a repeated element, replace it
		 if(this.replaceRep) {
			 for(int i=0; i < pop.length; i++) {
				 for(int j=i+1; j < pop.length; j++) {
					 if(pop[i].fitness.equivalentTo(pop[j].fitness)) {
						 ((SteadyStateEvolutionState)state).replacementProbability = 1.0;
						 return j;
					 }
				 }
			 }
		 }
		 
		 // If not, look for the worst individual in the population
		 ((SteadyStateEvolutionState)state).replacementProbability = this.replacementProb;
		 for(int i=1; i < pop.length; i++) {
			 if(worstFitness.betterThan(pop[i].fitness)) {
				 worstFitness = pop[i].fitness;
				 pick = i;
			 }
		 }
		
		return pick;
    }
		 
		 
	 
	 public void individualReplaced(SteadyStateEvolutionState state,
             int subpopulation,
             int thread,
             int individual) { }
	 
	 public void sourcesAreProperForm(SteadyStateEvolutionState state) { }
             
             
             
 }  // close the class
		
	
	