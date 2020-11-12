package calibration.ecj.statistics;
import ec.*;
import ec.steadystate.*;
import ec.util.*;

import java.io.*;

import ec.vector.*;


/**
 * This class is used to print all the chromosomes of the population at
 * each iteration of the algorithm. Is made for debugging purposes
 * 
 * It stores in a csv file the bounds for each parameter and all the
 * chromosomes that are in each generation of the evolutionary algorithm
 *  
 * @author jjpalacios
 *
 */
public class EcjStatisticsChromosome extends Statistics
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
    public static final String P_LOG = "out";
    public int logId = -1;
    
    /**
     * Indicates if it is the first time the log is opened
     */
    private boolean firstPost = true;

    
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
        File logFile = state.parameters.getFile(base.push(P_LOG), null);
		try {
			if(logFile != null) {
				logId = state.output.addLog(logFile, true);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
		firstPost = true;
    }
	
    /**
	 * Set the log for storing the data
	 * 
	 * @param state ECJ object containing the state of the process.
	 * @param logName name for the log.
	 */
	public void setLog(final EvolutionState state, final String logName) {
		
		if(this.logId > 0)
			state.output.removeLog(this.logId);
				
		File logFile = new File(logName);
		try {
			if(logFile != null) {
				this.logId = state.output.addLog(logFile, true);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
	}
    	
	
	
    	
	//=========================================================================
	//		METHODS
	//=========================================================================
    /**
	 * This function stores in the log file all the chromosomes that are
	 * present in the current population
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 */
	public void postEvaluationStatistics(final EvolutionState state)
    {
		// 	be certain to call the hook on super!
		super.postEvaluationStatistics(state);
		
		// If it is the first time this method is called, print
		// the bound for each parameter
		if(firstPost) {
			state.output.print("Min:", logId);
			int numParams = ((IntegerVectorIndividual)state.population.subpops[0].individuals[0]).genomeLength();
			for(int i=0; i < numParams; i++) {
				state.output.print(String.valueOf(((IntegerVectorSpecies)state.population.subpops[0].species).minGene(i)), logId);
				state.output.print(";", logId);
			}
			state.output.println("", logId);
			state.output.print("Max:", logId);
			for(int i=0; i < numParams; i++) {
				state.output.print(String.valueOf(((IntegerVectorSpecies)state.population.subpops[0].species).maxGene(i)), logId);
				state.output.print(";", logId);
			}
			state.output.println("", logId);
		}
		
		// Print all the chromosomes in the population
		for(int pop=0; pop < state.population.subpops.length; pop++) {
			for(int i=0; i < state.population.subpops[0].individuals.length; i++) {
				IntegerVectorIndividual idv = ((IntegerVectorIndividual)state.population.subpops[pop].individuals[i]);
				state.output.print(i + ";", logId);
				
				for(int j=0; j < idv.genome.length; j++)
					state.output.print(idv.genome[j] + ";", logId);
			
				state.output.println(String.valueOf(idv.fitness.fitness()), logId);
			}
		}
    }
	
		
    }
	
	