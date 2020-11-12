package calibration;

import java.io.IOException;
import java.util.List;

import calibration.fitness.history.ScoreBean;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import model.ModelDefinition;
import model.ModelManager;
import model.ModelRunner;
import util.StringBean;
import util.exception.calibration.CalibrationException;
import util.exception.sales.SalesScheduleError;
import util.exception.simulation.SimulationException;
import util.exception.view.TerminationException;
import util.statistics.MonteCarloStatistics;

/**
 * This class is the main controller for the calibration process. It is 
 * designed to be used in both experiment and production environments.
 * 
 * Its main responsibilities are:
 * 
 *  a. Loading model with default parameters for each scenario (not required 
 *     for production, only for experimentation).
 *  
 *  b. Launching C++ calibrator, along with a list of parameters to calibrate 
 *     and their feasible ranges. It should also pass a reference to self.
 *  
 *  c. Managing any call-backs from C++ calibrator, like fitness computation 
 *     requests. It also receives a list of values for the parameters.
 *      
 *  d. Returning simulation result, summarized in a simple double value that
 *     represents the fitness of the model.
 *  
 *  There is no need to interchange information about the calibration targets
 *  because fitness computation is performed on this side (Java).
 *  
 *  The interchange of information is done through JNI.
 *  http://www3.ntu.edu.sg/home/ehchua/programming/java/JavaNativeInterface.html
 *  http://www.ibm.com/developerworks/library/j-jni/ (best practices)
 *   
 * @author jbarranquero
 * 
 */
public class CalibrationController {
	
	/**
	 * CSV separator.
	 */
	private final static String CSV_SEP = ";";
	
//	/**
//	 * Experiment snapshot filename header.
//	 */
//	private final static String SNAPSHOT_HEADER = "EVALS";
//	/**
//	 * Experiment snapshot file extension.
//	 */
//	private final static String SNAPSHOT_EXT = ".STATS.MC.csv";
//	/**
//	 * Monte-Carlo iterations included at experiment snapshot.
//	 */
//	private final static int[] SNAPSHOT_MC = {5, 15, 30};
	
	/**
	 * Null parameters used for running calibration in evaluation only mode.
	 */
	private final static double[] UNCALIBRATED = null;

	/** 
	 * Number of times that fitness has been evaluated.
	 */
	private int simulationsCount;
	
	/**
	 * Calibration definition task: bean defining calibration parameters.
	 */
	private CalibrationTask taskdef;
	
	
//	/**
//	 * Monte-Carlo statistics filename.
//	 */
//	private String snapshotFilename = null;
	
	/**
	 * Set of initial parameters to include in the calibration
	 */
	private double[] initialParams;
	
	/**
	 * Connection with the ECJ semantics.
	 */
	private EcjInterface calibrationInterface;
	
	/**
	 * Model simulation time in milliseconds.
	 */
	private long millisecondsPerSimulation;
	
	/**
	 * Custom number of total evaluations.
	 */
	private int numEvaluations;
	
	private StringBean[] additionalConfig;
	
	/**
	 * Creates a calibration controller using given task definition.
	 * 
	 * @param taskdef calibration bean task.
	 * @param numEvaluations maximum number of evaluations.
	 * @throws CalibrationException 
	 */
	public CalibrationController(
			CalibrationTask taskdef, double [] params, int numEvaluations
		) throws CalibrationException {
		
		this.taskdef = taskdef;
		this.initialParams = params;
		this.numEvaluations = numEvaluations;
	}
	
	/**
	 * Updates model definition with current parameter values and 
	 * computes the average Monte-Carlo error. 
	 * 
	 * @param parameters calibrator tuned calibration parameters.
	 * @return computed fitness for new tuned calibration parameters.
	 * @throws CalibrationException if problems arise when updating 
	 * model definition or writing log.
	 * @throws SalesScheduleError if problems arise while running the model.
	 */
	public ScoreWrapper fitnessCallback(
			double[] parameters) throws CalibrationException, SalesScheduleError {
		ScoreBean bean = new ScoreBean();
		ScoreWrapper score = bean.new ScoreWrapper();
		
		final int mcIterations =  taskdef.getMonteCarloIterations();

		ModelDefinition md = taskdef.getModelDefinition();

		// Update model definition
		if (parameters != UNCALIBRATED) {
			updateModelDefinition(md, parameters);
		}
		
		try {
			
			MonteCarloStatistics results=ModelRunner.simulateModel(
						md, mcIterations, false,
							taskdef.getHistoryManager().getStatsBean());
			simulationsCount++;
			
			score = taskdef.getHistoryManager().computeTrainingScore(results);
		} catch (Exception e) {
			System.out.print("Simulation error for next individual: ");
			for(int i=0; i < parameters.length; i++)
				System.out.print(parameters[i] + " ");
			System.out.println("\n * Error message: "+e.getMessage());
			return score;
		}
		
//		for(int i=0; i < parameters.length; i++)
//			System.out.print(parameters[i] + " ");
//		System.out.println(";" + (score.finalScore));

		return score;
	}	
	
	/**
	 * Generates current snapshot results. Those results include parameters 
	 * for every individual at the population and fitness for best  
	 * individual at different number of MC iterations.
	 * 
	 * @param evaluations current number of model evaluations.
	 * @param parameters parameters for best individual.
	 * @throws CalibrationException - if problems arise when trying to update 
	 * the model or writing files.
	 * @throws IOException if problems arise when writing files.
	 * @throws SalesScheduleError if problems arise while running the model.
	 */
	public void snapshotCallback(
			long evaluations, double[] parameters
		) throws CalibrationException, IOException, SimulationException {
		
//		if (snapshotFilename == null) initSnapshotFilename();
		
		if(evaluations < 0)
			evaluations = simulationsCount;
		
		///////////////////////////////////////////////////////////////////////
		// Evaluate model for different MC iterations
		
//		final double[] fitnessErrorMC = new double[SNAPSHOT_MC.length];
		
//		ModelDefinition md = taskdef.getModelDefinition();
		
//		for (int s = 0; s < SNAPSHOT_MC.length; s++) {
//			
//			final int mcIterations = SNAPSHOT_MC[s];
//			
//			System.out.println(
//				String.format("Launching Monte Carlo (%dx)...", mcIterations)
//			);
//
//			MonteCarloStatistics results=ModelRunner.simulateModel(
//						md, mcIterations, false,
//							taskdef.getHistoryManager().getStatsBean());
//
//			ScoreWrapper scores = taskdef.getHistoryManager().computeTrainingScore(
//					results);
//			
//			fitnessErrorMC[s] = scores.finalScore;
//		}
		///////////////////////////////////////////////////////////////////////

		// Export results
//		exportSnapshotResults(
//			evaluations, parameters, 
//			fitnessErrorMC//, historyErrorMC, totalSalesErrorMC
//		);
		exportModelDefinition(evaluations, parameters);
	}

//	/**
//	 * Initializes the snapshot file writing CSV headers.
//	 * Creates an unique filename using signature and seed.
//	 * 
//	 * @throws IOException if problems arise when writing the file.
//	 */
//	private void initSnapshotFilename() throws IOException {
//		
//		snapshotFilename = 
//			taskdef.getCalibrationLogFolder()
//			+ "." + taskdef.getCalibrationSignature() 
//			+ "." + taskdef.getCalibrationSeed();
//		
//		BufferedWriter csvfile;
//		csvfile = new BufferedWriter(
//			new FileWriter(snapshotFilename + SNAPSHOT_EXT, true)
//		); 
//		csvfile.write(SNAPSHOT_HEADER);
//		for (int i = 0; i < SNAPSHOT_MC.length; i++) {
//			csvfile.write(CSV_SEP);
//			csvfile.write("FITNESS_");
//			csvfile.write(Integer.toString(SNAPSHOT_MC[i]));
//			csvfile.write(CSV_SEP);
//			csvfile.write("HISTORY_");
//			csvfile.write(Integer.toString(SNAPSHOT_MC[i]));
//			csvfile.write(CSV_SEP);
//			csvfile.write("TSALES_");
//			csvfile.write(Integer.toString(SNAPSHOT_MC[i]));
//		}
//		csvfile.write(CSV_SEP);
//		csvfile.write(formatParamNamesCSV());
//		csvfile.newLine();
//		csvfile.close();
//	}
	
	/**
	 * Exports model definition file for current best individual.
	 * 
	 * @param evaluations current number of model evaluations.
	 * @param parameters tuned best parameters.
	 * @throws CalibrationException if problems arise when updating the 
	 * model.
	 */
	private void exportModelDefinition(
			long evaluations, double[] parameters
			) throws CalibrationException {
		
//		String modelFilename = snapshotFilename + ".E" + evaluations + ".zio";
		
		ModelDefinition modelDefinition = taskdef.getModelDefinition();
		modelDefinition.setCalibrationSeed(taskdef.getCalibrationSeed());
		
		updateModelDefinition(modelDefinition, parameters);
		
//		try {
//			modelDefinition.writeModelDefinition(
//				new File(modelFilename)
//			);
//		} catch (IOException e) {
//			throw new CalibrationException(
//				"Unable to export model definition", e
//			);
//		} catch (Exception e) {
//			throw new CalibrationException(
//				"Unable to export model definition", e
//			);
//		}
//		
//		String logMessage = "Snapshot model exported at " + modelFilename;
//		System.out.println(logMessage);
	}
	
//	/**
//	 * Writes snapshot results for best solution found, including number of 
//	 * evaluations and fitness for different MC iterations.
//	 * 
//	 * @param evaluations number of model evaluations.
//	 * @param parameters parameters for best solution found.
//	 * @param fitnessErrorMC fitness split by MC iterations.
//	 * @param historyErrorMC fitness for seasonality by MC iterations.
//	 * @param totalSalesErrorMC fitness for total sales by MC iterations.
//	 * @throws CalibrationException if problems arise while converting 
//	 * calibration parameters to those displayed for the model.
//	 * @throws IOException if problems arise while writing target file.
//	 */
//	private void exportSnapshotResults(
//			final long evaluations,
//			final double[] parameters, 
//			final double[] fitnessErrorMC
//			) throws CalibrationException, IOException {
//		
//		BufferedWriter csvfile = null;
//		try {
//			csvfile = new BufferedWriter(
//				new FileWriter(snapshotFilename + SNAPSHOT_EXT, true)
//			); 
//			
//			csvfile.write(Long.toString(evaluations));
//			
//			for (int i = 0; i < fitnessErrorMC.length; i++) {
//				csvfile.write(CSV_SEP);
//				csvfile.write(String.format("%.6f", fitnessErrorMC[i]));
//				csvfile.write(CSV_SEP);
//			}
//			
//			double[] clbParameters = convertCalibrationParameters(parameters);
//			for (int i = 0; i < clbParameters.length; i++) {
//				csvfile.write(CSV_SEP);
//				csvfile.write(Double.toString(clbParameters[i]));
//			} 
//			
//			csvfile.newLine();
//			
//		} finally {
//			
//			if (csvfile != null) csvfile.close();
//		}
//	}

	/**
	 * Launches calibrator in optimization mode. It uses the ECJInterface
	 * to run the ECJ library
	 * 
	 * @param masterHost the ip of the master host used when calibrating with multiple machines.
	 * @param masterPort the port used by the master host when calibrating with multiple machines.
	 * @param baseNumberOfAgents base number of agents for evaluate final model.
	 * @return calibration bean with best solution found.
	 * @throws CalibrationException if problems arise while trying to connect
	 * with JNI, or evaluating the model.
	 * @throws SalesScheduleError if problems are found while evaluating final 
	 * model.
	 */
	public CalibrationResult execute(String masterHost, String masterPort, int baseNumberOfAgents) 
			throws CalibrationException, SimulationException, TerminationException {
		
		this.simulationsCount = 0; // reset count
		
		///////////////////////////////////////////////////////////////////////
		// Initialize the parameter bounds
		
		List<CalibrationParameter> params = 
				taskdef.getCalibrationParametersManager().getParameters();
		final int n = params.size();
		final double[] mins = new double[n];
		final double[] maxs = new double[n];
		for (int i = 0; i < n; i++) {
			mins[i] = params.get(i).minValue;
			maxs[i] = params.get(i).maxValue;
		}
		
		
		///////////////////////////////////////////////////////////////////////
		// Call ECJ to run the calibration
		
		calibrationInterface = new EcjInterface(
				this,masterHost,masterPort);
		
		calibrationInterface.setAdditionalAlgorithmParameters(additionalConfig);
		
		double[] parameters =calibrationInterface.runCalibration(
				taskdef.getCalibrationSignature(),
				taskdef.getCalibrationConfigFile(), 
				taskdef.getCalibrationLogFolder(), 
				formatParamNamesCSV(), mins, maxs,
				initialParams, 
				taskdef.getCalibrationSeed(),
				numEvaluations
			);
		
		///////////////////////////////////////////////////////////////////////
		
		System.out.println("Launching best model...");
		
		return evaluateFinalModel(parameters,baseNumberOfAgents);
	}

	/**
	 * Exports parameter names in CSV format.
	 * 
	 * @return returns parameter names in CSV format.
	 */
	private String formatParamNamesCSV() {
		
		List<CalibrationParameter> params = 
			taskdef.getCalibrationParametersManager().getParameters();
		
		String names = params.get(0).signature;
		for (int i = 1; i < params.size(); i++) {
			names += CSV_SEP + params.get(i).signature;
		}
		return names;
	}

	/**
	 * Evaluates best solution found, creating fitness data beans and storing 
	 * statistics results.
	 * 
	 * @param parameters calibration parameters for best solution.
	 * @param baseNumberOfAgents base number of agents for evaluate final model.
	 * @return returns calibration bean for best solution.
	 * @throws CalibrationException if problems arise when creating calibration 
	 * bean.
	 * @throws SalesScheduleError if problems are found when evaluation the model.
	 */
	private CalibrationResult evaluateFinalModel(
			double[] parameters, int baseNumberOfAgents) 
					throws CalibrationException, SimulationException {
		
		final int mcIterations = taskdef.getMonteCarloIterations();
		
		ModelDefinition md = taskdef.getModelDefinition();
		
		md.setNumberOfAgents(baseNumberOfAgents);
		
		MonteCarloStatistics statisticsMC = ModelRunner.simulateModel(
				md, mcIterations, false,
				taskdef.getHistoryManager().getStatsBean());

		ScoreWrapper scores = taskdef.getHistoryManager().computeTrainingScore(
				statisticsMC);
		
		return new CalibrationResult(
			scores,
			statisticsMC, 
			convertCalibrationParameters(parameters),
			parameters
		);
	}

	/**
	 * Converts calibration parameters into its Model semantics applying 
	 * the proper conversion factor to them.
	 * 
	 * @param parameters the solution parameters.
	 * @return calibration parameters translated to model semantics.
	 * @throws CalibrationException if problems arise while parameter model
	 * parameters are translated.
	 */
	private double[] convertCalibrationParameters(
			double[] parameters) throws CalibrationException {
		
		double[] clbParameters = null;
		if (parameters != UNCALIBRATED) {
			clbParameters = new double[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				clbParameters[i] = 
					taskdef.getCalibrationParametersManager()
					.convertParameterValue(i, parameters[i]);
			}
		}
		return clbParameters;
	}
	
	/**
	 * Updates given model definition instance with supplied calibration 
	 * parameters.
	 * 
	 * @param modelDefinition the model definition to be updated.
	 * @param parameters the parameters to be updated at model definition.
	 * @throws CalibrationException if problems are found while modifying
	 * model definition values.
	 */
	public void updateModelDefinition(
			ModelDefinition modelDefinition,
			double[] parameters
			) throws CalibrationException {
		
		final CalibrationParametersManager paramManager = 
			taskdef.getCalibrationParametersManager();
		
		// Update parameters
		paramManager.setModelManager(new ModelManager(modelDefinition, 
				paramManager.getInvolvedDrivers()));
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] != -1) {
				parameters[i] = paramManager.setParameterValue(i, parameters[i]);
			} else {
				System.out.println();
				System.out.println(
					"[JAVA/CalibrationController] Ignoring parameter " + i
				);
			}
		}
		paramManager.endStep();
	}
	
	/**
	 * Returns current calibration task instance.
	 * @return current calibration task instance.
	 */
	public CalibrationTask getTask() {
		return taskdef;
	}
	
	/**
	 * Stores the simulation time in milliseconds for estimating the remaining 
	 * execution time.
	 * @param millis the simulation time in milliseconds.
	 */
	public void setSimulationTime(long millis) {
		this.millisecondsPerSimulation=millis;
	}
	
	/**
	 * Estimates time left using the current number of simulations and the 
	 * defined maximum of individual evaluations. Used by the calibrations 
	 * executing in only one machine.
	 * @return the estimated time left in milliseconds
	 */
	public long estimateTimeLeft() {
		if(calibrationInterface!=null) {
			long evaluations = calibrationInterface.getNumberOfEvaluations();
			if(evaluations!=Long.MAX_VALUE) {
				return (evaluations-simulationsCount)*millisecondsPerSimulation;
			}
		}
		return Long.MAX_VALUE;
	}
	
	public void setAdditionalAlgorithmParameters(StringBean[] pairs) {
		this.additionalConfig = pairs;
	}
	
	/**
	 * Estimates time left using the number of generations already completed. 
	 * Used by the calibrations executed in multiple nodes.
	 * @return the estimated time left in milliseconds
	 */
	public long estimateTimeLeftUsingNumberOfGenerations() {
		if(calibrationInterface!=null) {
			long evaluations = calibrationInterface.estimateEvaluationsLeft();
			if(evaluations!=Long.MAX_VALUE) {
				return evaluations * millisecondsPerSimulation;
			}
		}
		return Long.MAX_VALUE;
	}
	
	/**
	 * Cancels the current calibration execution.
	 */
	public void terminateCalibration() {
		while(calibrationInterface==null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		calibrationInterface.terminate();
	}
}
