package calibration.ecj.statistics;
import ec.*;
import ec.steadystate.*;
import ec.util.*;

import java.io.*;
import ec.vector.*;


/**
 * This class is used to evaluate a surrogate function for the model
 * evaluation.
 * 
 * It waits until a whole generation has been evaluated to store all
 * the individuals generated and their similarity measure in a square
 * matrix and export it to a file. 
 *  
 * @author jjpalacios
 *
 */
public class EcjStatisticsSurrogate extends Statistics
implements SteadyStateStatisticsForm
    {
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Output files
	 */
    public static final String P_LOG_SIM = "out-similarity";
    public int logSimilarity = -1;
    public static final String P_LOG_FIT = "out-fitness";
    public int logFitness = -1;

    
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
        
        // Reads the path of the log file and open it for writting
        File logFile = state.parameters.getFile(base.push(P_LOG_SIM), null);
		try {
			if(logFile != null) {
				logSimilarity = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
		
		logFile = state.parameters.getFile(base.push(P_LOG_FIT), null);
		try {
			if(logFile != null) {
				logFitness = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
    }
	
    /**
	 * Set the log for storing the data
	 * 
	 * @param state ECJ object containing every variable of the process.
	 * @param logSimilarity file path for the similarity log.
	 * @param logFitness file path for the fitness log.
	 */
	public void setLogs(final EvolutionState state, final String logSimilarity,
			final String logFitness) {
		
		if(this.logSimilarity > 0)
			state.output.removeLog(this.logSimilarity);
		if(this.logFitness > 0)
			state.output.removeLog(this.logFitness);
		
		File logFile = new File(logSimilarity);
		try {
			if(logFile != null) {
				this.logSimilarity = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
		
		logFile = new File(logFitness);
		try {
			if(logFile != null) {
				this.logFitness = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
	}
    	
	
	
    	
	//=========================================================================
	//		METHODS
	//=========================================================================
    /**
	 * This function prints the fitness of each individual and its similarity
	 * degree with respect to the others.
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 */
	public void postEvaluationStatistics(final EvolutionState state)
    {
		// 	be certain to call the hook on super!
		super.postEvaluationStatistics(state);
		
		for(int i=0; i < state.population.subpops[0].individuals.length; i++) {
			state.output.print(String.valueOf(i+1), logSimilarity);
			state.output.print(String.valueOf(i+1), logFitness);
			
			state.output.print(";"
			+ String.valueOf(
					state.population.subpops[0].individuals[i].fitness.fitness()),
					logSimilarity);
			
			state.output.print(";"
					+ String.valueOf(
							state.population.subpops[0].individuals[i].fitness.fitness()),
							logFitness);
			
			
			for(int j=0; j < state.population.subpops[0].individuals.length; j++) {
				state.output.print(";" + String.valueOf(
						similarity(state,
								state.population.subpops[0].individuals[i],
								state.population.subpops[0].individuals[j])),
								logSimilarity );
				
				state.output.print(";" + String.valueOf(
								Math.abs(
									state.population.subpops[0].individuals[i].fitness.fitness() -
									state.population.subpops[0].individuals[j].fitness.fitness())),
								logFitness );
			}
			state.output.println("", logSimilarity);
			state.output.println("", logFitness);
		}
    }
	
	/**
	 * Computes the similarity of two individuals as the square difference:
	 * sqrt((Ia - Ib)/(max-min))/sqrt(n)
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param ind1 the first individual to be compared.
	 * @param ind2 the second individual to be compared.
	 * @return the similarity of two individuals as the square difference.
	 */
	private double similarity(final EvolutionState state, final Individual ind1, 
			final Individual ind2) {
		
		int [] array1, array2; 
		
		if(!(ind1 instanceof IntegerVectorIndividual) ||
				!(ind2 instanceof IntegerVectorIndividual))
			state.output.fatal("Error: the indiviaul type is not correct for"
					+ " the similarity function");
		
		array1 = ((IntegerVectorIndividual)ind1).genome;
		array2 = ((IntegerVectorIndividual)ind2).genome;
		
		double squareSum = 0.0;
		for(int i=0; i < array1.length; i++) {
			double min = ((IntegerVectorSpecies)state.population.subpops[0].species).minGene(i);
			double max = ((IntegerVectorSpecies)state.population.subpops[0].species).maxGene(i);
			double normDiff = ((double)(array1[i] - array2[i])) / (max - min);
			squareSum += normDiff * normDiff;
		}
					
		return Math.sqrt(squareSum) / Math.sqrt(array1.length);
	}
	
		
    }
	
	