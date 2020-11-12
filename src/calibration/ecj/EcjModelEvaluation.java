package calibration.ecj;

import java.util.HashMap;

import calibration.EcjInterface;
import calibration.ecj.multimodal.*;
import calibration.ecj.multimodal.mobide.MOBiDE_Fitness;
import calibration.ecj.multimodal.nichepso.NicheParticle;
import calibration.ecj.multimodal.nmmso.NMMSOParticle;
import calibration.ecj.multimodal.nsga2mm.*;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.Subpopulation;
import ec.multiobjective.MultiObjectiveFitness;
import ec.pso.Particle;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ec.vector.FloatVectorSpecies;
import util.exception.calibration.CalibrationException;
import util.exception.sales.SalesScheduleError;


/**
 * This class is needed for ECJ to work properly.
 * It implements the evaluation function for a given chromosome
 * 
 * In this case, the chromosome is an array of integer values, which
 * is represented by an IntegerVectorIndividual object in the ECJ library.
 * 
 * The evaluation process requires to take the parameters and test them
 * in the model.
 * 
 * This class must have access to the calibration setup instance that is
 * being used to properly evaluate the individuals
 *   
 * @author jjpalacios
 * 
 */


public class EcjModelEvaluation extends Problem implements SimpleProblemForm
    {
	/**
	 * HINT: Remember than when using ECJ, the recommended way to write text
	 * is to use the state.output.print (println) function. If the second parameter
	 * of this function is 0, it writes in the standard output, and if it is greater
	 * than 1 it writes in a custom log that needs to be defined before
	 * running the algorithm.
	 */
	
		
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Calibration Controller to evaluate the fitness.
	 */
	private EcjInterface ecjInterface;
	/**
	 * Check if the class has been initialized correctly
	 */
	private boolean classInitialized = false;
	
	/**
	 * Archive of evaluated solutions
	 */
	//private HashMap<IntegerVectorIndividual, Double> history;	
	private HashMap<Individual, Double> history;
	/**
	 * Number of real evaluations performed
	 */
	private static int numEvaluations = 0;
	/**
	 * Modality indicator for PNA-NSGA2
	 */
    private String              modality;

	
	//=========================================================================
	//		CONSTRUCTORS / INITIALIZERS
	//=========================================================================	
    /*
     * Parameter setup
     * @see ec.Problem#setup(ec.EvolutionState, ec.util.Parameter)
     */
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        modality = state.parameters.getStringWithDefault(base.push("modality"),
                null, "none");
        
    }
    
	/**
	 * Only the default constructor could be redefined.
	 */

	public Object clone() {
		EcjModelEvaluation copy = (EcjModelEvaluation)super.clone();
		copy.ecjInterface = this.ecjInterface;
		copy.classInitialized = this.classInitialized;
		
		return copy;
	}
	
	/**
	 * Initialize the evaluation function using a ecjInterface object.
	 * 
	 * @param ecjInterface Data required for the evaluation 
	 */
	public void init(EcjInterface ecjInterface) {
		this.ecjInterface = ecjInterface;
		this.classInitialized = true;
		this.history = new HashMap<Individual, Double>();
		numEvaluations = 0;
	}
	
	
	
	//=========================================================================
	//		METHODS
	//=========================================================================
	 @Override
	    public void prepareToEvaluate(final EvolutionState state,
	            final int threadnum) {
	 
	        super.prepareToEvaluate(state, threadnum);
	        if( state.evaluator instanceof NSGA2MM_Evaluator) { 
		        for (int i = 0; i < state.population.subpops.length; i++) {
		            // we will set all indvs evaluation state to not_evaluated
		            Subpopulation mypop = state.population.subpops[i];
		            int t;
		            for (t = 0; t < mypop.individuals.length; t++) {
		                mypop.individuals[t].evaluated = false;
		                // sets a circular reference ind<->fit
		                ((NSGA2MM_Fitness) mypop.individuals[t].fitness).individual = mypop.individuals[t];
		            }
		        }
	        }
	    }
	/**
	 * Evaluation function called by the ECJ library.
	 * 
	 * This is the main function of this class and its heading cannot be
	 * changed.
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param ind Individual to be evaluated (ECJ object).
	 * @param subpopulation Population to which the individual belongs.
	 * @param threadnum Thread number. Just in case of parallelization. 
	 */
    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
    {
        // If it is already evaluated, return
        if (ind.evaluated)
        	return;
        
        if(!classInitialized) {
        	state.output.fatal("The object CalibrationController has not"
            		+ " been provided to ECJ",null);
        }
        
        // Check if the individual has been already evaluated
        Double hashValue = this.history.get(ind);
        double fitness = 0.0;

        // Check than individual is in the bounds (just for PSO)
        if(ind.species.i_prototype instanceof Particle 
        		|| ind.species.i_prototype instanceof NicheParticle 
        		|| ind.species.i_prototype instanceof NMMSOParticle) {
        	FloatVectorSpecies species = (FloatVectorSpecies)ind.species;
        	DoubleVectorIndividual floatInd = (DoubleVectorIndividual)ind;
        	for(int i=0; i < floatInd.genomeLength(); i++) {
        		if(floatInd.genome[i] < species.minGene(i))
        			floatInd.genome[i] = species.minGene(i);
    			else if(floatInd.genome[i] > species.maxGene(i))
    				floatInd.genome[i] = species.maxGene(i);
        	}
        }
        		
        ScoreWrapper wrapper = null;
        // If it has been evaluated, assign the fitness directly
        if(hashValue != null) {
        	fitness = hashValue;
        
        // If not, fully evaluate the individual
        } else {
	        try {
	        	wrapper = ecjInterface.fitnessInterface(ind);
	        	numEvaluations++;
	        	fitness = wrapper.finalScore;
	        	
	        	if(numEvaluations % 100 == 0)
	        		state.output.println("Number of regular evaluations: "
	        				+ String.valueOf(numEvaluations), 0);
	        	
	        } catch (CalibrationException | SalesScheduleError e) {
				state.output.fatal(e.getMessage(), null);
			}
        }
 
		// Check that individual is correct
		if (ind.fitness instanceof SimpleFitness) {
	        if((ind.species.i_prototype instanceof MMDoubleVectorIndividual 
	        			|| ind.species.i_prototype instanceof MMIntegerVectorIndividual) 
	        		&& !(state.breeder instanceof calibration.ecj.multimodal.denrand.NRand2DEBreeder))
	        	//Special case of NicheGAs with SUSSelection
	        	((SimpleFitness)ind.fitness).setFitness(state,
		        		1000-fitness,	// Maximizing score
		        		(fitness == 0.0));	
	        else
				((SimpleFitness)ind.fitness).setFitness(state,
		        		-fitness,	// Maximizing score
		        		(fitness == 0.0));		
		 
		} else if (ind.fitness instanceof NSGA2MM_Fitness) { 
			double[] fitnesses = ((MultiObjectiveFitness)ind.fitness).getObjectives();
			
			fitnesses[0]= -fitness;

			/* Replace last (in this case the second objective) 
			 * with the euclidean distance to maximize exploration*/
			if (modality.compareTo("mm") != 0) 
				fitnesses[fitnesses.length - 1] = 0;
	        else 
	        	fitnesses[fitnesses.length - 1] = PNSGAII_getDistAll(state, ind,subpopulation);
	        
			((NSGA2MM_Fitness)ind.fitness).setObjectives(state,fitnesses); 
		}else if (ind.fitness instanceof MOBiDE_Fitness) { 
			double[] fitnesses = ((MultiObjectiveFitness)ind.fitness).getObjectives();
			fitnesses[0]= -fitness;

			/* Replace last (in this case the second objective) 
			 * with the euclidean distance to maximize exploration*/
			fitnesses[fitnesses.length - 1] = MOBiDE_getDistAll(state, ind,subpopulation);
	        
			((MOBiDE_Fitness)ind.fitness).setObjectives(state,fitnesses); 
		} else if (ind.fitness instanceof MultiObjectiveFitness 
				&& !(ind.fitness instanceof NSGA2MM_Fitness)
				&& !(ind.fitness instanceof MOBiDE_Fitness)) {
			double[] fitnesses = ((MultiObjectiveFitness)ind.fitness).getObjectives(); 
			fitnesses[0] = -wrapper.awarenessScore.getScore();
 			fitnesses[1] =  wrapper.womVolumeScore.getScore();
			((MultiObjectiveFitness)ind.fitness).setObjectives(state,fitnesses); 
			
		}else {
			state.output.fatal("There has been a fatal error in ECJ:"
            		+ "fitness type invalid",null);
		}

        ind.evaluated = true;
        
        history.put(ind, fitness);
    }
    
    public static int getCurrentEvaluations() {
    	return numEvaluations;
    }
    
    // A Parameterless-Niching-Assisted Bi-objective Approach to Multimodal Optimization
    // Page 2 formula 5
    private double PNSGAII_getDistAll(final EvolutionState state, final Individual ind,
            final int subpopulation) {
        Subpopulation mypop = state.population.subpops[subpopulation];
        int popsize = mypop.individuals.length;
        double retval = 0;
        for (int t1 = 0; t1 < popsize; t1++) 
            retval += distance_Euclide_Individuals(ind,mypop.individuals[t1]);
        
        return  1.0 / retval;
    }
    
    // S. Das paper MOBiDE eq.(9b)
    private double MOBiDE_getDistAll(final EvolutionState state, final Individual ind,
            final int subpopulation) {
        Subpopulation mypop = state.population.subpops[subpopulation];
        int popsize = mypop.individuals.length;
        double retval = 0;
        for (int t1 = 0; t1 < popsize; t1++) 
            retval += distance_Euclide_Individuals(ind,mypop.individuals[t1]);

        return retval/popsize;
    }


    private static double distance_Euclide_Individuals(Individual ind1,
                                                       Individual ind2) {
        int tg;
        DoubleVectorIndividual iv1 = (DoubleVectorIndividual) ind1;
        DoubleVectorIndividual iv2 = (DoubleVectorIndividual) ind2;
        double[] g1 = iv1.genome;
        double[] g2 = iv2.genome;
        double tmp;
        int numDecisionVars = g1.length;
        double sum = 0;

        for (tg = 0; tg < numDecisionVars; tg++) {
            tmp = g1[tg] - g2[tg];
            sum += tmp * tmp;
        }
        return Math.sqrt(sum);
    }
}
