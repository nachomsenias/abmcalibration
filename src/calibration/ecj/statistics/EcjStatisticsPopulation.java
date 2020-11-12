package calibration.ecj.statistics;
import ec.*;
import ec.simple.SimpleStatistics;
import ec.steadystate.*;
import ec.util.*;
import ec.vector.DoubleVectorIndividual;
import ec.vector.FloatVectorSpecies;
import ec.vector.IntegerVectorIndividual;
import ec.vector.IntegerVectorSpecies;

import java.io.*;



/**
 * This class is used to store information about the current status of the
 * population. It prints the current generation, the fitness of the
 * best individual in the population, the average fitness, the standard
 * deviation, the average euclidean distance of all individuals with respect
 * to the best one and the average distance between all individuals
 * in the population.
 * 
 * In the ecj structure, this object depends on stat.chlid.x
 * 
 * This class takes four new parameters from the ecj configuration file
 * 		activate: Indicates weather or not the user wants to take snapshots
 * 		generations: Number of generations between each snapshot
 * 		distance: Indicates if we want to export the distance. This
 * 			increases the runtime.
 * 		out: Name of the file to which export the data
 *  
 * @author jjpalacios
 *
 */
public class EcjStatisticsPopulation extends Statistics implements SteadyStateStatisticsForm
    {
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;	
    
    /**
	 * Number of evaluations between snapshots
	 */
    public static final String P_SNAP_INTERVAL = "generations";
    public int generations;
    
    
    /**
	 * Number of evaluations between snapshots
	 */
    public static final String P_DISTANCE = "distance";
    public boolean showDistance;
    
    /**
	 * Output file
	 */
    public static final String P_LOG = "out";
    public int logId = -1;    

    
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
        
        File logFile;
       
		// Read the number of generations between snapshots (1 by default)
		generations = state.parameters.getIntWithDefault(base.push(P_SNAP_INTERVAL), null, 1);
		if(generations <= 0) {
			state.output.fatal("The interval between snapshots must be a value "
					+ "grater than 0", null);
		}
		
		logFile = state.parameters.getFile(base.push(P_LOG), null);
		logFile = nextFile(logFile);
		
		try {
			if(logFile != null) {
				logId = state.output.addLog(logFile, true);
				state.output.println("Iteration;Best value;Avg Fitness", logId);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
		
		// Reads the parameter indicating if the distance has to be computed.
		// The parameter takes false as default value
		showDistance = state.parameters.getBoolean(
				base.push(P_DISTANCE), null, false);
    }
    	
    /**
	 * Set the log for storing the data
	 * 
	 * @param state ECJ object containing the state of the process.
	 * @param logName File path for the log. 
	 */
	public void setLog(final EvolutionState state, final String logName) {
		try {
			if(logId > 0)
				state.output.removeLog(logId);
			File logFile;
			logFile = nextFile(new File(logName));
			if(logFile != null) {
				logId = state.output.addLog(logFile, true);
				state.output.println("Iteration;Best value;Avg Fitness", logId);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
	}
    	
	
	
	//=========================================================================
	//		METHODS
	//=========================================================================
    /**
	 * This function stores in the file the best and average fitness
	 * in the population.
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
		
		// Loops over the population to compute the statistical values
		if (!(state.statistics instanceof SimpleStatistics))
			state.output.fatal("The obtained statistics is not in the right format");
		
		double current, sum=0.0, sqrSum=0.0, best=-Double.MAX_VALUE;
		double distance, sumDistance=0.0, avgDistance;
		Individual bestIndividual = state.population.subpops[0].individuals[0];
		int count = 0, totalSize=0;
		
		for(int pop=0; pop < state.population.subpops.length; pop++) {
			int popSize = state.population.subpops[pop].individuals.length;
			totalSize += popSize;
			for(int i=0; i < popSize; i++) {
					current = state.population.subpops[pop].individuals[i].fitness.fitness();
					if(current > best) {
						best = current;
						bestIndividual = state.population.subpops[pop].individuals[i];
					}
					sum += current;
					sqrSum += current * current;
					count++;
					
					for(int j=i+1; j < popSize; j++) {
						distance = euclideanDistance(state,
								state.population.subpops[pop].individuals[i],
								state.population.subpops[pop].individuals[j]);
						sumDistance += distance;
				}
			}
		}
		avgDistance = 2 * sumDistance / (totalSize*(totalSize-1));
		
		// If the best individual is available in statistics, update the variable
		Individual newBest = (((SimpleStatistics)(state.statistics)).getBestSoFar())[0];
		if(newBest != null) {
			bestIndividual = newBest;
			best = bestIndividual.fitness.fitness();
			for(int pop=1; pop < state.population.subpops.length; pop++) {
				newBest = (((SimpleStatistics)(state.statistics)).getBestSoFar())[pop];
				if(newBest != null && newBest.fitness.betterThan(bestIndividual.fitness)) {
					bestIndividual = newBest;
					best = bestIndividual.fitness.fitness();
				}
			}
		}
			
		// Compute the average distance with respect to the best individual
		sumDistance = 0.0;
		for(int pop=0; pop < state.population.subpops.length; pop++) {
			int popSize = state.population.subpops[pop].individuals.length;
			for(int i=0; i < popSize; i++) {
				distance = euclideanDistance(state,
						state.population.subpops[0].individuals[i],
						bestIndividual);
				sumDistance += distance;
			}
		}
		
		// Print all values
		state.output.println(
				String.valueOf(state.generation) + ";" +
				String.valueOf(best) + ";" +
				String.valueOf(sum/count) + ";" +
				String.valueOf(Math.sqrt((sqrSum-sum*sum/count)/(count-1))) + ";" +
				String.valueOf(sumDistance / (totalSize-1)) + ";" +
				String.valueOf(avgDistance), logId);
    }
	
	
    /**
	 * This function returns the first file with the given
	 * name that does not exist.
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 */	
	private File nextFile(File logFile) {
		if(logFile == null)
			return null;
		
		File newFile;
		String fileExt, fileName;
		
		fileName = logFile.getAbsolutePath();
		fileExt = fileName.substring(fileName.length()-4);
		fileName = fileName.substring(0, fileName.length()-4);
		
		newFile = new File(fileName + "_1" + fileExt);
		
		int sufix=2;
		while(newFile.exists()) {
			newFile = new File(fileName + "_" + sufix + fileExt);
			sufix++;
		}
		return newFile;
	}
	
	
	/**
	 * This function computes the euclidean distance between two individuals.
	 * The distance between two genes is normalized according to the max and
	 * min values that each the genes can take.
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param ind1 First individual
	 * @param ind2 Second individual
	 * @return The euclidean distance between both
	 */
	 protected double euclideanDistance(final EvolutionState state,
			 final Individual ind1, final Individual ind2) {
		 
	 	// If the individual is made of integer values
		if(ind1 instanceof IntegerVectorIndividual) {
			IntegerVectorIndividual intInd1 = (IntegerVectorIndividual)ind1;
			IntegerVectorIndividual intInd2 = (IntegerVectorIndividual)ind2;
			IntegerVectorSpecies species = (IntegerVectorSpecies)ind1.species;
			
			double distance, totalDistance = 0.0;
			for(int i=0;i < intInd1.genomeLength(); i++) {
				distance = Math.abs(intInd1.genome[i] - intInd2.genome[i]);
				// Normalize
				distance = distance / (species.maxGene(i) - species.minGene(i)); 
				totalDistance += distance * distance;
			}
			return Math.sqrt(totalDistance);
		}
		
		// If the individual is made of integer values
		if(ind1 instanceof DoubleVectorIndividual) {
			DoubleVectorIndividual doubInd1 = (DoubleVectorIndividual)ind1;
			DoubleVectorIndividual doubInd2 = (DoubleVectorIndividual)ind2;
			FloatVectorSpecies species = (FloatVectorSpecies)ind1.species;
			
			double distance, totalDistance = 0.0;
			for(int i=0;i < doubInd1.genomeLength(); i++) {
				distance = Math.abs(doubInd1.genome[i] - doubInd2.genome[i]);
				// Normalize
				distance = distance / (species.maxGene(i) - species.minGene(i)); 
				totalDistance += distance * distance;
			}
			return Math.sqrt(totalDistance);
		} 
        return 0.0;
     }
	
		
    }
	
	