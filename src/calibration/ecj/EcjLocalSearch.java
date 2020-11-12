package calibration.ecj;

import calibration.ecj.ls.GradientDescent;
import calibration.ecj.ls.HillClimbing;
import calibration.ecj.ls.LocalSearch;
import calibration.ecj.ls.LBFGS;
import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Statistics;
import ec.steadystate.SteadyStateEvolutionState;
import ec.steadystate.SteadyStateStatisticsForm;
import ec.util.Parameter;


/**
 * This class applies a local search algorithm to a set of solutions in
 * the current population. Due to its computational complexity, this new
 * operator will have many parameters to set up it:
 * 
 *  -   type: Type of Local Search to use. for now, the only one that is
 *  		available is Hill-Climbing
 * 	-	frequency: Indicates when the hill climbing must be applied. This
 * 			parameter can take three values: initial, final and period.
 * 			If set to initial, hill climbing will be applied only to the
 * 			initial population. If it is final, only to the last population.
 * 			If it is period, it will be applied every "period-freq" iterations  
 * 	-	period-freq:	If the previous option is "period", we must specify the
 * 			number of iterations between different hill-climbing
 * 	-	target: best, worst, percentage value. Indicates to which solutions in
 * 			the population apply the hill climbing
 * 
 * Again, we have to use the trick of inheriting from the Statistics class to be
 * able to apply our operator just after the evaluation of a new population.
 *     
 * @author jjpalacios
 *
 */

public class EcjLocalSearch
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
	 * Parameter indicating the type of local search
	 */
	public static final String P_LOCAL_SEARCH = "type";
	public static final String P_LS_HILLCLIMBING = "hill-climbing";
	public static final String P_LS_GRADDESCENT = "gradient-descent";
	public static final String P_L_BFGS= "l-bfgs";
	public LocalSearch localSearch;
	
	/**
	 * Parameter indicating when to apply the local search
	 */
	public static final String P_FREQUENCY = "frequency";
	private static final String P_FREQ_INITIAL = "initial";
	private static final String P_FREQ_FINAL = "final";
	private static final String P_FREQ_PERIOD = "period";
	private static final String P_FREQ_STUCK = "stuck";
	private static final String P_FREQ_ALWAYS = "always";
	 
	public enum Frequency {INITIAL, FINAL, PERIOD, STUCK, ALWAYS}
	private Frequency frequency;
	
	/**
	 * Parameter indicating the number of iterations between searches
	 */
	public static final String P_PERIOD = "period-freq";
	public int periodFreq;
	
	/**
	 * Parameter indicating to which solutions apply the local search
	 */
	public static final String P_TARGET = "target";
	private static final String P_TARGET_BEST = "best";
	private static final String P_TARGET_WORST = "worst";
	
	public enum Target {BEST, WORST, SOME}
	private Target target;
	
	/**
	 * Percentage of individuals to apply LS to 
	 */
	private double blProb;
	
	/**
	 * Last known fitness
	 */
	private double lastFitness;
	
	/**
	 * Counter of iterations without improvement
	 */
	private int countStuck;
	
	/*
	 * Flag for disabling LS when close to the stopping criteria.
	 */
	private boolean disable;
	
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
        
        // Reads the type of local search
        String ls = state.parameters.getString(base.push(P_LOCAL_SEARCH), null);
        if(ls == null) {
        	state.output.fatal("The type of the local search has "
        			+ "not been specified", null);
        }
        if(ls.equalsIgnoreCase(P_LS_HILLCLIMBING))
        	localSearch = new HillClimbing();
        else if(ls.equalsIgnoreCase(P_LS_GRADDESCENT))
        	localSearch = new GradientDescent();
        else if(ls.equalsIgnoreCase(P_L_BFGS))
        	localSearch = new LBFGS();
        else {
        	state.output.fatal("Incorrent value for the local search "
    			+ "\'type\' parameter. Please, use \'hill-climbing\'"
    			, null);
        }
        localSearch.setup(state, base);
        
        // Reads the parameter for the "frequency" parameter.
        String freq = state.parameters.getString(base.push(P_FREQUENCY), null);
        if(freq == null) {
        	state.output.fatal("The frequency for the local search has "
        			+ "not been specified", null);
        }
        if(freq.equalsIgnoreCase(P_FREQ_INITIAL))
        	frequency = Frequency.INITIAL;
        else if(freq.equalsIgnoreCase(P_FREQ_FINAL))
        	frequency = Frequency.FINAL;
        else if(freq.equalsIgnoreCase(P_FREQ_PERIOD))
        	frequency = Frequency.PERIOD;
        else if(freq.equalsIgnoreCase(P_FREQ_STUCK))
        	frequency = Frequency.STUCK;
        else if(freq.equalsIgnoreCase(P_FREQ_ALWAYS))
        	frequency = Frequency.ALWAYS;
        else {
        	state.output.fatal("Incorrent value for the local search "
        			+ "\'frequency\' parameter. Please, use \'initial\', "
        			+ "\'final\', \'period\' or \'stuck\'", null);
        }
        
        // Reads the parameter for the period frequency.
        periodFreq = state.parameters.getIntWithDefault(base.push(P_PERIOD),
        		null, -1);
        if(frequency == Frequency.PERIOD && periodFreq < 0) {
        	state.output.fatal("You must specify a value for the period"
        			+ "between different searches (\'period-freq\'"
        			+ " parameter)", null);
        }
        
        // Reads the parameter for the target individuals.
        String target_str = state.parameters.getString(base.push(P_TARGET),
        		null);
        if(target_str == null) {
        	state.output.fatal("The target for the local search has "
        			+ "not been specified", null);
        }
        if(target_str.equalsIgnoreCase(P_TARGET_BEST))
        	target = Target.BEST;
        else if(target_str.equalsIgnoreCase(P_TARGET_WORST))
        	target = Target.WORST;
        else {
        	blProb = state.parameters.getDoubleWithDefault(base.push(P_TARGET),
            		null, -1.0);
        	if(blProb > 0) {
        		target = Target.SOME;
        	}
    		else {
            	state.output.fatal("Incorrent value for the local search "
            			+ "\'target\' parameter. Please, use \'best\', "
            			+ "\'worst\' or a real value", null);
        	}
        }
                
        lastFitness = -Double.MAX_VALUE;
        countStuck = 0;
        disable=false;
    }
    
    	
    
	//=========================================================================
	//		METHODS
	//=========================================================================
    /**
	 * This function applies the local search depending on the moment
	 * it is specified in the frequency parameter. This method estimates
	 * if the HC must be applied, and invoke it if it does.
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 */
	public void postBreedingStatistics(final EvolutionState state) {
		// 	be certain to call the hook on super!
		super.postBreedingStatistics(state);
		if(!disable) {
			switch (frequency) {
			case ALWAYS:
				apply(state);
				break;
			case INITIAL:
				if(state.generation == 0) 
					apply(state);
				break;
			case FINAL:
				if(state.generation == state.numGenerations-1) 
					apply(state);
				if(state instanceof SteadyStateEvolutionState &&
					state.numEvaluations == ((SteadyStateEvolutionState)state).evaluations) { 
						apply(state);
				}
				break;
			case STUCK:
				checkStuck(state);
				if(state.generation == state.numGenerations ||  countStuck >= periodFreq
					|| (state instanceof SteadyStateEvolutionState &&
					state.numEvaluations == ((SteadyStateEvolutionState)state).evaluations) 
						){
						apply(state);
				}
				break;
			case PERIOD:
				if(state.generation % periodFreq == 0) 
					apply(state);
				break;
			default:
				state.output.println(
					"Unknown local search frequency value: "+frequency.toString(), 0);
			}
		}
    }
	
	public void postEvaluationStatistics(final EvolutionState state) {
		super.postEvaluationStatistics(state);
		if(!disable) {
			switch (frequency) {
			case INITIAL:
				if(state.generation == 0) 
					apply(state);
				break;
			default:
				//Do nothing.
			}
		}
	}
	
	private void checkStuck(final EvolutionState state) {
		// Looks for the best fitness in the population
		Fitness bestFitness = state.population.subpops[0].individuals[0].fitness;
		for(int pop=0; pop < state.population.subpops.length; pop++) {
			for(int i=0; i < state.population.subpops[pop].individuals.length; i++) {
				Fitness currentFitness;
				Individual currentIdv = state.population.subpops[pop].individuals[i]; 
				currentFitness = currentIdv.fitness; 
				if(currentFitness.betterThan(bestFitness)) {
					bestFitness = currentFitness;
				}
			}
		}
		
		// Update the counter of stuck iterations 
		if(bestFitness.fitness() > lastFitness)
			countStuck = 0;
		else countStuck++;
		lastFitness = bestFitness.fitness();
	}
	
	/**
	 * This function determines to which individuals apply the HC based on the
	 * parameters 
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 */   
	protected void apply(final EvolutionState state) {
		
		switch (target) {
		case BEST:
			applyBest(state);
			break;
		case WORST:
			applyWorst(state);
			break;
		case SOME:
			applySome(state);
			break;
		default:
			state.output.println(
					"Unknown local search target value: "+target.toString(), 0);
		}
	}
	
	private void applyBest(final EvolutionState state) {
		Fitness bestFitness = state.population.subpops[0].individuals[0].fitness;
		Individual bestIndividual = state.population.subpops[0].individuals[0];
		for(int pop=0; pop < state.population.subpops.length; pop++) {
			for(int i=0; i < state.population.subpops[pop].individuals.length; i++) {
				Fitness currentFitness;
				Individual currentIdv = state.population.subpops[pop].individuals[i]; 
				currentFitness = currentIdv.fitness; 
				if(currentFitness.betterThan(bestFitness)) {
					bestFitness = currentFitness;
					bestIndividual = currentIdv;
				}
			}
		}
		localSearch.apply(state, bestIndividual);
	}
	
	private void applyWorst(final EvolutionState state) {
		Fitness worstFitness = state.population.subpops[0].individuals[0].fitness;
		Individual worstIndividual = state.population.subpops[0].individuals[0];
		
		for(int pop=0; pop < state.population.subpops.length; pop++) {
			for(int i=0; i < state.population.subpops[pop].individuals.length; i++) {
				Fitness currentFitness;
				Individual currentIdv = state.population.subpops[pop].individuals[i]; 
				currentFitness = currentIdv.fitness; 
				if(worstFitness.betterThan(currentFitness)) {
					worstFitness = currentFitness;
					worstIndividual = currentIdv;
				}
			}
		}
		localSearch.apply(state, worstIndividual);
	}
	
	private void applySome(final EvolutionState state) {
		if(!disable && frequency==Frequency.ALWAYS && target == Target.SOME) {
			int subpops = state.population.subpops.length;
			for (int sub = 0; sub<subpops; sub++) {
				applySome(state, state.population.subpops[sub].individuals);
			}
		}
	}
	
	private void applySome(final EvolutionState state, Individual[] individuals) {
		int maxRefinements = computeMaxRefinements(state);
		
		boolean stop = maxRefinements<=0;
		
		int refinedIndividuals = 0;
	
		// Apply HC with a blProb probability
		int i=0;
		while(i<individuals.length 
				&& !stop) {
			double prob =  state.random[0].nextDouble();
			if(prob<blProb) {
				localSearch.apply(state,
						individuals[i]);
				refinedIndividuals++;
			}
			if(maxRefinements==refinedIndividuals) {
				stop = true;
				
			}
			i++;
		}
	}
	
	private int computeMaxRefinements(final EvolutionState state) {
		long left = state.numEvaluations - 
				EcjModelEvaluation.getCurrentEvaluations();
		
		int max = (int)(left / localSearch.maxSteps);
		
		if(max<=0) {
			disable= true;
			if (state instanceof SteadyStateEvolutionState) {
				((SteadyStateEvolutionState)state).evaluations =
						EcjModelEvaluation.getCurrentEvaluations();
			} else {
				state.generation = state.numGenerations-2;
			}
			
			state.output.println("Only "+ left + " evaluations left. "+
					"Not enough evaluations left for applying local search refinement.", 0);
		}
		
		return max;
	}
	
	@Override
	public void individualsBredStatistics(SteadyStateEvolutionState state, Individual[] individuals) {
		if(!disable && frequency==Frequency.ALWAYS && target == Target.SOME) {
			applySome(state, individuals);
		}
    }
	
}
	
	