package calibration;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import calibration.ecj.EcjModelEvaluation;
import calibration.ecj.statistics.*;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.simple.SimpleStatistics;
import ec.util.Output;
import ec.util.ParameterDatabase;
import ec.vector.ByteVectorIndividual;
import ec.vector.DoubleVectorIndividual;
import ec.vector.IntegerVectorIndividual;
import ec.vector.LongVectorIndividual;
import ec.vector.ShortVectorIndividual;
import util.StringBean;
import util.exception.calibration.CalibrationException;
import util.exception.sales.SalesScheduleError;
import util.exception.simulation.SimulationException;
import util.exception.view.TerminationException;

/**
 * This class is responsible for preparing the ECJ components for the calibration process.
 * Once ready, it runs the algorithm.
 * 
 * @author jjpalacios
 *
 */
public class EcjInterface {
	/**
	 * Stats header filename.
	 */
//	private final String STATS_FILE = "CalibrationExperiment";
	
	/**
	 * Stats file extension.
	 */
	private final String STATS_FILE_EXT = ".stats";
	
	/**
	 * Name of package for ECJ implemented classes
	 */
	private static final String ECJ_PACKAGE_NAME = "calibration.ecj";
	 
	/**
	 * Calibration setup: bean defining calibration parameters.
	 */
	private CalibrationController clbController;
	
	/**
	 * The ip of the master host used when calibrating with multiple machines.
	 */
	private String masterHost;	
	
	/**
	 * The port used by the master host when calibrating with multiple machines.
	 */
	private String masterPort;
	
	/**
	 * Termination flag used for canceling the execution of the algorithm.
	 */
	private boolean stop = false;
	
	/**
	 * The target number of individual evaluations.
	 */
	private long numberOfEvaluations = Long.MAX_VALUE;
	
	/**
	 * ECJ evolution engine object.
	 */
	private EvolutionState evolutionState;
	
	private StringBean[] additionalConfig;
	
	/**
	 * Creates the instance of EcjInterface.
	 * 
	 * @param clbController controller to allow the evaluation of individuals.
	 * @param masterHost the ip of the master host used when calibrating with multiple machines.
	 * @param masterPort the port used by the master host when calibrating with multiple machines.
	 */
	public EcjInterface(CalibrationController clbController, String masterHost, 
			String masterPort) {
		this.clbController = clbController;
		this.masterHost=masterHost;
		this.masterPort=masterPort;
	}

	/**
	 * Runs the ECJ evolutionary algorithm to calibrate the parameters.
	 * 
	 * @param signature experiment signature.
	 * @param configFile path for algorithm configuration file.
	 * @param logFolder path for log folder.
	 * @param paramNames CSV header with parameter names.
	 * @param minValues minimum values for each optimizing parameter.
	 * @param maxValues maximum values for each optimizing parameter.
	 * @param seed for current scenario.
	 * @param numEvaluations maximum number of evaluations for this run. 
	 * @return resulting optimized values for each parameter.
	 * @throws CalibrationException if exceptions risen from the ECJ run.
	 * @throws TerminationException if the execution is cancelled.
	 */
	public double[] runCalibration(
			String signature, String configFile, String logFolder, 
			String paramNames, double[] minValues, double[] maxValues,
			double[] initialParams, long seed, int numEvaluations
		) throws CalibrationException, TerminationException {
		
		System.out.println("Setup calibration algorithm...");
		File parameterFile = new File(configFile);
		
		try {
			ParameterDatabase dbase = new ParameterDatabase(parameterFile);
			
			String paramName;
						
			// Set the output file for the ECJ statistics
			paramName = "stat.file";
			String logStatFile = logFolder + signature + STATS_FILE_EXT;
			System.out.println("Trying to create stat file at: ");
			System.out.println(logStatFile);
			System.out.println("Real file path: ");
			String absolutePath = new File(logStatFile).getAbsolutePath();
			System.out.println(absolutePath);
			// String logStatFile = STATS_FILE + "." + signature + STATS_FILE_EXT;
			dbase.set(new ec.util.Parameter(paramName), absolutePath);
			
			// Set the seed for the experiment
			paramName = "seed.0";
			dbase.set(new ec.util.Parameter(paramName), String.valueOf(seed));
			
			//Pareto font
			paramName = "stat.front";
//			String fontFile = "."+baseFolder+STATS_FILE + "." + signature + STATS_FILE_EXT;
//			String fontFile = "."+baseFolder + "front_"+signature + STATS_FILE_EXT;
			String fontFile = logFolder + "front_"+signature + STATS_FILE_EXT;
			dbase.set(new ec.util.Parameter(paramName), fontFile);
			
			// Set the number of parameters to optimize	
			int nParams = minValues.length;
			paramName = "pop.subpop.0.species.genome-size";
			dbase.set(new ec.util.Parameter(paramName), String.valueOf(nParams));
			
			// Set the min-max values for each parameter
			for(int i=0; i < nParams; i++) {
				paramName = "pop.subpop.0.species.min-gene." + String.valueOf(i);
				dbase.set(new ec.util.Parameter(paramName), String.valueOf(minValues[i]));
				
				paramName = "pop.subpop.0.species.max-gene." + String.valueOf(i);
				dbase.set(new ec.util.Parameter(paramName), String.valueOf(maxValues[i]));
			}
			
			//Distributed computing parameters
			if(masterHost!= null && masterPort!=null) {
				dbase.set(new ec.util.Parameter("eval.master.host"), masterHost);
				dbase.set(new ec.util.Parameter("eval.master.port"), masterPort);
			}
			
			//Overrides the number of evaluations of the ECJ file
			if(numEvaluations>0) {
				//Specify the maximum number of evaluations.
				dbase.set(new ec.util.Parameter("evaluations"), 
						String.valueOf(numEvaluations));
			}
			
			
			// Fix the path to custom classes
			fixClassValues(dbase);

			// Create the output for ECJ
			Output out = Evolve.buildOutput();
			// Redirect the output to a file instead of the console
			// out.getLog(0).filename = new File(logFolder + "logECJ.txt");
			
		
			// XXX Tune additional values
			tuneAlgorithmParameters(dbase);
			
			// Initialize the ECJ environment
			evolutionState = Evolve.initialize(dbase, 0, out);
			evolutionState.startFresh();

			// Provide the controller to the ECJ objects that may need it
			// Evaluation function
			if(evolutionState.evaluator.p_problem instanceof EcjModelEvaluation)
				((EcjModelEvaluation)evolutionState.evaluator.p_problem).init(this);

			// Statistics objects			
			for(int i=0; i < evolutionState.statistics.children.length; i++) {
				if(evolutionState.statistics.children[i] instanceof EcjStatisticsSnapshot)
					((EcjStatisticsSnapshot)evolutionState.statistics.children[i]).init(this);
				if(evolutionState.statistics.children[i] instanceof EcjStatisticsPopulation)
					((EcjStatisticsPopulation)evolutionState.statistics.children[i]).setLog(
							evolutionState,	logFolder + "Population_"+
							signature + ".csv");
				if(evolutionState.statistics.children[i] instanceof EcjStatisticsHeatMapPopulation)
					((EcjStatisticsHeatMapPopulation)evolutionState.statistics.children[i]).setLog(
							evolutionState,	logFolder + "HMPopulation_"+
							signature + ".csv");
				if(evolutionState.statistics.children[i] instanceof EcjStatisticsMMPopulation)
					((EcjStatisticsMMPopulation)evolutionState.statistics.children[i]).setLogs(
							evolutionState,	
							logFolder + "WholePopulation_"+ signature + ".csv",	
							logFolder + "MeanFitnessPopulation_"+ signature + ".csv",	
							logFolder + "DistancePopulation_"+ signature + ".csv",	
							logFolder + "NichePopulation_"+ signature + ".csv");
				if(evolutionState.statistics.children[i] instanceof EcjStatisticsSurrogate)
					((EcjStatisticsSurrogate)evolutionState.statistics.children[i]).setLogs(
							evolutionState,
							logFolder + "Surrogate_" + signature + "_Sim.csv",
							logFolder + "Surrogate_" + signature + "_Fit.csv"
							);
				if(evolutionState.statistics.children[i] instanceof EcjStatisticsChromosome)
					((EcjStatisticsChromosome)evolutionState.statistics.children[i]).setLog(
							evolutionState,	logFolder + "Chromosomes_"+
							signature + ".csv");
				if(evolutionState.statistics.children[i] instanceof EcjSteadyInitialSolution)
					((EcjSteadyInitialSolution)evolutionState.statistics.children[i]).init(initialParams);
				if(evolutionState.statistics.children[i] instanceof EcjInitialSolution)
					((EcjInitialSolution)evolutionState.statistics.children[i]).init(initialParams, evolutionState);
			}
			
			// Iterate the ECJ evolutionary algorithm
			System.out.println("Running calibration...");
			
			// Stores the target number of evaluations for estimating time left.
			numberOfEvaluations=evolutionState.numEvaluations;

			int result = EvolutionState.R_NOTDONE;
			while( result == EvolutionState.R_NOTDONE && !stop)
				result = evolutionState.evolve();
			
			if(stop) {
				throw new TerminationException("Calibration cancelled.");
			}
			
			evolutionState.finish(result);

			
			// Take the best solution found so far by the algorithm
			if (!(evolutionState.statistics instanceof SimpleStatistics))
				throw new CalibrationException("Fatal error in ECJ module. The obtained "
						+ "statistics is not in the right format");
			Individual[] inds = ((SimpleStatistics)(evolutionState.statistics)).getBestSoFar();
			
			// Export model for the best found solution
			snapshotInterface(-1, inds[0]);
			
			// Close the ECJ environment
			Evolve.cleanup(evolutionState);
			
			// Return the array of parameters
			if(inds.length > 0) {
				return getParameterArray(inds[0]);
			}
			else
				throw new CalibrationException("Fatal error in ECJ module. No solution"
						+ " was generated");
		
		} catch (IOException | SimulationException e) {
			throw new CalibrationException("Fatal error in ECJ module:" + e.getMessage());
		}
	}
	
		
	
	/**
	 * Acts as interface between the controller and the Ecj objects
	 * so they can call the fitness evaluation function.
	 * 
	 * @param ind Individual to be evaluated
	 * @throws SalesScheduleError 
	 * @throws CalibrationException 
	 * 
	 */
	public ScoreWrapper fitnessInterface(Individual ind)
			throws CalibrationException, SalesScheduleError {
		double[] paramArray = getParameterArray(ind);
		ScoreWrapper fitness = this.clbController.fitnessCallback(getParameterArray(ind));
		setParameterArray(ind, paramArray);
		
		return fitness;
	}	
		
	
	/**
	 * Acts as interface between the controller and the Ecj objects
	 * so they can call the snapshots function.
	 * 
	 * @param evaluations current number of evaluations.
	 * @param ind best individual found.
	 * @throws IOException 
	 * @throws SimulationException 
	 * @throws CalibrationException 
	 */
	public void snapshotInterface(final int evaluations, final Individual ind)
			throws CalibrationException, IOException, SimulationException {
		this.clbController.snapshotCallback(evaluations, getParameterArray(ind));
	}
	
	
	/**
	 * Fix the references to new implemented classes. For example, by
	 * adding the prefix calibration.ecj to the parameter values
	 * 
	 * @param dbase Loaded Parameter database
	 * 
	 */
	public static void fixClassValues(ParameterDatabase dbase) {
		String paramName, paramValue;
		
		// Iterates over the parameter database (it's a hashtable)
		Iterator<Entry<Object,Object>> iter = dbase.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Object, Object> pair = iter.next();
			paramName = pair.getKey().toString();
			paramValue = pair.getValue().toString();
			
			/* If the parameter value refers to any of the implemented
			 * classes of this project, fix the name by adding the prefix
			 */ 
			if(		paramValue.compareTo("EcjBlxCrossover") == 0
				||	paramValue.compareTo("EcjModelEvaluation") == 0
				|| 	paramValue.compareTo("EcjSteadyRestart") == 0
				|| 	paramValue.compareTo("EcjSteadySelection") == 0
				|| 	paramValue.compareTo("EcjLocalSearch") == 0
				) {
					dbase.set(new ec.util.Parameter(paramName),
							ECJ_PACKAGE_NAME + "." + paramValue);
					
			} else if (paramValue.compareTo("EcjStatisticsChromosome") == 0
					|| 	paramValue.compareTo("EcjStatisticsPopulation") == 0
					|| 	paramValue.compareTo("EcjStatisticsSnapshot") == 0
					|| 	paramValue.compareTo("EcjStatisticsSurrogate") == 0
					|| 	paramValue.compareTo("EcjSteadyInitialSolution") == 0
					|| 	paramValue.compareTo("EcjInitialSolution") == 0
				) {
				dbase.set(new ec.util.Parameter(paramName),
						ECJ_PACKAGE_NAME + ".statistics." + paramValue);
			}
		}
	}
	
	
	/**
	 * This method allows to convert any kind of Ecj individual into
	 * an int array to pass to the evaluation function
	 * 
	 * @param ind Individual to convert
	 * @return the array with the integer parameters
	 * 
	 */
	public static double[] getParameterArray(Individual ind) {
		if(ind instanceof IntegerVectorIndividual) {
			int[] genotype = ((IntegerVectorIndividual)ind).genome;
			double[] params = new double[genotype.length];
			for(int i=0; i < params.length; i++) {
				params[i] = genotype[i];
			}
			return params;
		}
		else if(ind instanceof DoubleVectorIndividual) {
			double[] genotype = ((DoubleVectorIndividual)ind).genome;
			return genotype;
		}
		
		else if(ind instanceof ByteVectorIndividual) {
			byte[] genotype = ((ByteVectorIndividual)ind).genome;
			double[] params = new double[genotype.length];
			for(int i=0; i < params.length; i++) {
				params[i] = genotype[i];
			}
			return params;
		}
		
		else if(ind instanceof ShortVectorIndividual) {
			short[] genotype = ((ShortVectorIndividual)ind).genome;
			double[] params = new double[genotype.length];
			for(int i=0; i < params.length; i++) {
				params[i] = genotype[i];
			}
			return params;
		}
		
		
		else if(ind instanceof LongVectorIndividual) {
			long[] genotype = ((LongVectorIndividual)ind).genome;
			double[] params = new double[genotype.length];
			for(int i=0; i < params.length; i++) {
				params[i] = genotype[i];
			}
			return params;
		}
		
		else return new double[0];
	}
	

	/**
	 * This method allows to convert an int array into any kind of
	 *  Ecj individual
	 * 
	 * @param ind Individual to update its genome
	 * @param newGenome array with the integer parameters
	 */
	public static void setParameterArray(Individual ind, double [] newGenome) {
		if(ind instanceof IntegerVectorIndividual) {
			int[] genotype = ((IntegerVectorIndividual)ind).genome;
			for(int i=0; i < newGenome.length; i++) {
				genotype[i] = (int)newGenome[i];
			}
		
		} else if(ind instanceof DoubleVectorIndividual) {
			
			((DoubleVectorIndividual)ind).genome = newGenome;
		}
		
		else if(ind instanceof ByteVectorIndividual) {
			byte[] genotype = ((ByteVectorIndividual)ind).genome;
			for(int i=0; i < newGenome.length; i++) {
				genotype[i] = (byte)newGenome[i];
			}
		}
		
		else if(ind instanceof ShortVectorIndividual) {
			short[] genotype = ((ShortVectorIndividual)ind).genome;
			for(int i=0; i < newGenome.length; i++) {
				genotype[i] = (short)newGenome[i];
			}
		}
		
		else if(ind instanceof LongVectorIndividual) {
			long[] genotype = ((LongVectorIndividual)ind).genome;
			for(int i=0; i < newGenome.length; i++) {
				genotype[i] = (long)newGenome[i];
			}
		}
	}
	
	/**
	 * Interrupts the evolution loop, finishing the calibration algorithm.
	 */
	public void terminate() {
		stop=true;
	}
	
	/**
	 * Includes further algorithmic configuration
	 */
	public void tuneAlgorithmParameters(ParameterDatabase dbase) {
		if(additionalConfig!=null) {
			for (StringBean pair : additionalConfig) {
				dbase.set(new ec.util.Parameter(pair.getKey()), pair.getValue());
			}
		}
	}
	
	public void setAdditionalAlgorithmParameters(StringBean[] pairs) {
		this.additionalConfig = pairs;
	}
	
	/**
	 * Returns the number of evaluations defined for the current experiment.
	 * @return the number of evaluations defined for the current experiment
	 */
	public long getNumberOfEvaluations() {
		return numberOfEvaluations;
	}
	
	/**
	 * Estimates the number of evaluations left using the number of individuals 
	 * and the current generation.
	 * @return the estimated number of evaluations left.
	 */
	public long estimateEvaluationsLeft() {
		if(evolutionState==null) {
			return Long.MAX_VALUE;
		} else return numberOfEvaluations-(evolutionState.generation *
				evolutionState.population.subpops[0].individuals.length);
	}
}
