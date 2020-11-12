package calibration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import calibration.CalibrationController;
import calibration.fitness.FitnessFunction;
import calibration.fitness.history.HistoryManager;
import calibration.fitness.history.SalesHistoryManager;
import model.ModelDefinition;
import model.ModelManager;
import util.exception.calibration.CalibrationException;
import util.exception.sales.SalesScheduleError;
import util.exception.simulation.SimulationException;
import util.exception.view.TerminationException;
import util.io.CSVFileUtils;
import util.io.ConfigFileReader;
import util.random.RandomizerUtils;
import util.statistics.Statistics;
import util.statistics.Statistics.TimePeriod;

/**
 * Console-based view for ZioABM project experiments.
 * 
 * @author jbarranquero
 */
public class CalibrationConsole {
	
///////////////////////////////////////////////////////////////////////////////////
	
	public static final String NO_MASTER_HOST= null;
	public static final String NO_MASTER_PORT= null;
	
	/**
	 * CSV separator.
	 */
	public final static String CSV_SEP = ";";
	/**
	 * CSV extension.
	 */
	public final static String CSV_EXT = ".csv";
	
	/**
	 * Folder separator.
	 */
	private final static String FSEP = File.separator;
	/**
	 * Line separator.
	 */
	private final static String LSEP = "\n";
	/**
	 * CSV result header. 
	 */
	private final static String CSV_HEADER = 
		"ALGORITHM" + CSV_SEP + 
		"SCENARIO" 	+ CSV_SEP + 
		"TASK" 		+ CSV_SEP + 
		"FITNESS" 	+ CSV_SEP + 
		"TIME" 		+ CSV_SEP +
		"PARAMS";

///////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * ZIO file extension.
	 */
	private static String MODEL_FILE_EXT = ".zio";
	/**
	 * Calibration file extension.
	 */
	private static String PARAMS_FILE_EXT = ".clb";
	
	/**
	 * Configuration algorithm header filename.
	 */
	private static String ALGORITHM_FILE = "config";
	/**
	 * Algorithm file extension.
	 */
	private static String ALGORITHM_FILE_EXT = ".ecj";
	
	/**
	 * Experiment header filename.
	 */
	private static String EXPERIMENT_FILE = "CalibrationConfig";
	/**
	 * Experiment file extension.
	 */
	private static String EXPERIMENT_FILE_EXT = ".properties";
	
	/**
	 * Result header filename.
	 */
	private static String RESULTS_FILE = "CalibrationExperiment";
	/**
	 * Result file extension.
	 */
	private static String RESULTS_FILE_EXT = ".csv";
	
	/**
	 * Calibration files folder.
	 */
	private static String CONFIG_FOLDER = "." + FSEP + "calibration" + FSEP;
	/**
	 * Data files folder.
	 */
	private static String DATA_FOLDER = "." + FSEP + "calibration" + FSEP;
	/**
	 * Log files folder.
	 */
	private static String LOG_FOLDER = "." + FSEP + "log" + FSEP;
	
///////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Log header filename.
	 */
	private static String EXPLOG_NAME = RESULTS_FILE;
	
	/**
	 * Exports calibration log.
	 * 
	 * @param message - calibration log.
	 * @param printAlsoInConsole - if true, also displays log on screen.
	 * @throws CalibrationException - an exception is thrown when opening 
	 * log file fails.
	 */
	private static void EXPLOG(
			String message, boolean printAlsoInConsole
			) throws CalibrationException {
		
		String timestamp =
			new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss] ")
			.format(Calendar.getInstance().getTime());
		
		if (printAlsoInConsole) {
			System.out.println(timestamp + message);
			System.out.flush();
			try { Thread.sleep(100); } catch (InterruptedException e) {}
			System.err.println(timestamp + message);
			System.err.flush();
			try { Thread.sleep(100); } catch (InterruptedException e) {}
		}
		BufferedWriter logfile;
		try {
			logfile = new BufferedWriter(new FileWriter(EXPLOG_NAME + ".log", true));
			logfile.write(timestamp);
			logfile.write(message);
			logfile.newLine();
			logfile.close();
		} catch (IOException e) {
			throw new CalibrationException(
				"Unnable to open experiment log at " + EXPLOG_NAME, e
			);
		} 
	}
	
///////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Properties file reader.
	 */
	private static ConfigFileReader CONFIG;
	
	/**
	 * Returns the property value that matches given property name.
	 * That value is retrieved as a String.
	 * 
	 * @param propertyName property key name.
	 * @return property value as a String.
	 */
	private static String getParameterString(String propertyName) {
		String value = CONFIG.getParameterString(propertyName);
		if (value == null) throw new NullPointerException(propertyName);
		return value;
	}
	
	/**
	 * Returns the property value that matches given property name.
	 * That value is retrieved as an Integer.
	 * 
	 * @param propertyName property key name.
	 * @return property value as an Integer.
	 */
	private static int getParameterInteger(String propertyName) {
		return CONFIG.getParameterInteger(propertyName);
	}
	
	/**
	 * Returns the property value that matches given property name.
	 * That value is retrieved as a Double.
	 * 
	 * @param propertyName - property key name.
	 * @return property value as a Double.
	 */
	private static double getParameterDouble(String propertyName) {
		return CONFIG.getParameterDouble(propertyName);
	}

//###########################################################################//
//								MAIN                                         //
//###########################################################################//
	
	/**
	 * Runs calibration in console mode. Calibration signature and experiment
	 * name are passed by arguments: first should be experimentSignature 
	 * (or # for no signature), second should be experiment name.
	 * 
	 * @param args - calibration arguments.
	 */
	public static void main(String[] args) {
		
		try {
		
		///////////////////////////////////////////////////////////////////////
		// Experiment environment
		///////////////////////////////////////////////////////////////////////
		
		// Set experiment signature (using timestamp by default) 
		String experimentSignature = Long.toString(System.currentTimeMillis());
		// But can be set with first argument (for overwritting logs and results)
		if (args.length > 0 && !args[0].equals("#")) {
			experimentSignature = args[0];
		}
		
		// Define root results folder
		String resultsFolder = LOG_FOLDER + "calibration";
		// In case you want to use a non-default experiment config file
		if (args.length > 1) resultsFolder += "_" + args[1];
		resultsFolder += FSEP;
		
		new File(resultsFolder).mkdirs();
		
		// Define test log file (!= DLL log, logs experiment progress only)
		EXPLOG_NAME = resultsFolder + EXPLOG_NAME + "." + experimentSignature;

		// Experiment config file
		String experimentFile = DATA_FOLDER + EXPERIMENT_FILE;
		// In case you want to use a non-default experiment config file
		if (args.length > 1) experimentFile += "_" + args[1];
		experimentFile += EXPERIMENT_FILE_EXT;

		// CSV name
		String csvname = 
			resultsFolder + RESULTS_FILE + "." + 
			experimentSignature + RESULTS_FILE_EXT;

		///////////////////////////////////////////////////////////////////////
		// Load experiment configuration
		///////////////////////////////////////////////////////////////////////

		String calibrationMode = null;
		
		int repetitions = 1;
		int mcIterations = 1;
		
		TimePeriod historyPeriod = TimePeriod.WEEKLY;
		
		FitnessFunction fitnessFunction = new FitnessFunction();
		
		double totalSalesWeight = CalibrationTask.ONLY_HISTORY_FITNESS;
		
		String[] algorithms = null;
		String[] scenarios = null;
		String[] tasks = null;

		try {
			
			String configLogBuffer = "Experiment configuration..." + LSEP + LSEP;
			
			CONFIG = new ConfigFileReader();
			CONFIG.readConfigFile(new File(experimentFile));
			
			///////////////////////////////////////////////////////////////////		
			configLogBuffer += "Calibration mode: ";
			calibrationMode = getParameterString("calibrationMode");
			configLogBuffer += calibrationMode + LSEP;
			configLogBuffer += LSEP;
			///////////////////////////////////////////////////////////////////
			configLogBuffer += "Repetitions: ";
			try {
				repetitions = getParameterInteger("repetitions");
			} catch(NumberFormatException e) {
				repetitions = 1;
				EXPLOG("WARNING: undefined property 'repetitions'", true);
			}
			if (repetitions > RandomizerUtils.PRIME_SEEDS.length) {
				throw new CalibrationException("Too many repetitions");
			}
			configLogBuffer += repetitions + LSEP;
			///////////////////////////////////////////////////////////////////
			configLogBuffer += "Monte Carlo: ";
			try {
				mcIterations = getParameterInteger("montecarlo");
			} catch(NumberFormatException e) {
				mcIterations = 1;
				EXPLOG("WARNING: undefined property 'mcIterations'", true);
			}
			if (mcIterations > RandomizerUtils.PRIME_SEEDS.length) {
				throw new CalibrationException("Too many MC iterations");
			}
			configLogBuffer += mcIterations + LSEP;
			configLogBuffer += LSEP;
			///////////////////////////////////////////////////////////////////
			configLogBuffer += "History period: ";
			try {
				historyPeriod = Statistics.timePeriodFromString(
					getParameterString("historyPeriod")
				);
			} catch(NullPointerException e) {
				EXPLOG("WARNING: undefined property 'historyPeriod'", true);
			} catch(IllegalArgumentException e) {
				EXPLOG("ERROR: illegal value for 'historyPeriod'", true);
				throw new IllegalStateException(
					"Illegal value for 'historyPeriod'" + 
					getParameterString("historyPeriod")
				);
			}
			configLogBuffer += historyPeriod + LSEP;
			///////////////////////////////////////////////////////////////////
			configLogBuffer += "Total sales weight: ";
			try {
				totalSalesWeight = getParameterDouble("totalSalesWeight");
			} catch(NumberFormatException e) {
				totalSalesWeight = CalibrationTask.ONLY_HISTORY_FITNESS;
				EXPLOG("WARNING: undefined property 'totalSalesWeight'", true);
			}
			configLogBuffer += totalSalesWeight + LSEP;
			///////////////////////////////////////////////////////////////////
			configLogBuffer += "Algorithms:" + LSEP;
			algorithms = getParameterString("algorithms").split(",");
			for (int i = 0; i < algorithms.length; i++) {
				configLogBuffer += " " + algorithms[i] + LSEP;
			}
			configLogBuffer += LSEP;
			///////////////////////////////////////////////////////////////////			
			configLogBuffer += "Scenarios:" + LSEP;
			scenarios = getParameterString("scenarios").split(",");
			for (int i = 0; i < scenarios.length; i++) {
				configLogBuffer += " " + scenarios[i] + LSEP;
			}
			configLogBuffer += LSEP;
			///////////////////////////////////////////////////////////////////		
			configLogBuffer += "Tasks:" + LSEP;
			tasks = getParameterString("tasks").split(",");
			for (int i = 0; i < tasks.length; i++) {
				configLogBuffer += " " + tasks[i] + LSEP;
			}
			configLogBuffer += LSEP;
			///////////////////////////////////////////////////////////////////
			
			EXPLOG(configLogBuffer, true);
			
		} catch(Exception e) {
			EXPLOG("Problem loading experiment config file: " + e.getMessage(), true);
			e.printStackTrace();
			System.in.read();
		}
		
		///////////////////////////////////////////////////////////////////////
		// Launch experiments
		///////////////////////////////////////////////////////////////////////

		if (!calibrationMode.equals("optimize")) {
			throw new CalibrationException(
				"Unknown calibration mode: " + calibrationMode
			);
		}

		for (int alg = 0; alg < algorithms.length; alg++) {
			for (int tsk = 0; tsk < tasks.length; tsk++) {
				for (int sce = 0; sce < scenarios.length; sce++) {
					runTask(
						experimentSignature, resultsFolder, csvname,
						algorithms[alg], scenarios[sce], tasks[tsk],
						repetitions, mcIterations,
						historyPeriod, fitnessFunction,
						totalSalesWeight
					);
				}
			}
		}	

		///////////////////////////////////////////////////////////////////////
		// THE END
		///////////////////////////////////////////////////////////////////////

		EXPLOG("Done", true);
		
		///////////////////////////////////////////////////////////////////////
		// GOLBAL CONSOLE CATCH
		///////////////////////////////////////////////////////////////////////
		
		} catch (Throwable e) {
			e.printStackTrace();
		}  			
	}
	
	/**
	 * Creates a controller from the given configuration file path.
	 * @param config the configuration file path.
	 * @return the controller instance using given setup.
	 */
	public static CalibrationController getCalibrationController(String config) {
		try {
			
			// Set experiment signature (using timestamp by default) 
			String experimentSignature = Long.toString(System.currentTimeMillis());
			
			// Define root results folder
			String resultsFolder = LOG_FOLDER + "calibration";
			resultsFolder += FSEP;
			new File(resultsFolder).mkdirs();
			
			// Define test log file (!= DLL log, logs experiment progress only)
			EXPLOG_NAME = resultsFolder + EXPLOG_NAME + "." + experimentSignature;
			
			int repetitions = 1;
			int mcIterations = 1;
			
			TimePeriod historyPeriod = TimePeriod.WEEKLY;
			
			FitnessFunction fitnessFunction = new FitnessFunction();
			
			double totalSalesWeight = CalibrationTask.ONLY_HISTORY_FITNESS;
			
			String algorithms = null;
			String scenarios = null;
			String tasks = null;

			try {
				
				CONFIG = new ConfigFileReader();
				System.out.println("Running with file: "+config);
				CONFIG.readConfigFile(new File(config));
				
				///////////////////////////////////////////////////////////////////
				// Repetitions
				try {
					repetitions = getParameterInteger("repetitions");
				} catch(NumberFormatException e) {
					repetitions = 1;
					EXPLOG("WARNING: undefined property 'repetitions'", true);
				}
				if (repetitions > RandomizerUtils.PRIME_SEEDS.length) {
					throw new CalibrationException("Too many repetitions");
				}
				///////////////////////////////////////////////////////////////////
				// Monte-Carlo
				try {
					mcIterations = getParameterInteger("montecarlo");
				} catch(NumberFormatException e) {
					mcIterations = 1;
					EXPLOG("WARNING: undefined property 'mcIterations'", true);
				}
				if (mcIterations > RandomizerUtils.PRIME_SEEDS.length) {
					throw new CalibrationException("Too many MC iterations");
				}
				
				///////////////////////////////////////////////////////////////////
				// History period
				try {
					historyPeriod = Statistics.timePeriodFromString(
						getParameterString("historyPeriod")
					);
				} catch(NullPointerException e) {
					EXPLOG("WARNING: undefined property 'historyPeriod'", true);
				} catch(IllegalArgumentException e) {
					EXPLOG("ERROR: illegal value for 'historyPeriod'", true);
					throw new IllegalStateException(
						"Illegal value for 'historyPeriod'" + 
						getParameterString("historyPeriod")
					);
				}

				try {
					totalSalesWeight = getParameterDouble("totalSalesWeight");
				} catch(NumberFormatException e) {
					totalSalesWeight = CalibrationTask.ONLY_HISTORY_FITNESS;
					EXPLOG("WARNING: undefined property 'totalSalesWeight'", true);
				}

				// Optimization algorithm
				algorithms = getParameterString("algorithms").split(",")[0];
				
				// Zio Scenario
				scenarios = getParameterString("scenarios").split(",")[0];
				
				tasks = getParameterString("tasks").split(",")[0];
				
			} catch(Exception e) {
				EXPLOG("Problem loading experiment config file: " + e.getMessage(), true);
				e.printStackTrace();
				System.in.read();
			}
			
			// Algorithm config file
			String algorithmConfigFile = 
				CONFIG_FOLDER + ALGORITHM_FILE + 
				algorithms + ALGORITHM_FILE_EXT;
			
			// Model, params & history files
			String modelFile = 
				DATA_FOLDER + scenarios + MODEL_FILE_EXT;
			String paramsFile = 
				DATA_FOLDER + scenarios + "_" + tasks + PARAMS_FILE_EXT;
			String historyFile = 
				DATA_FOLDER + scenarios + "_sales" + CSV_EXT;
			
			// Calibration parameters manager
			CalibrationParametersManager paramManager = 
				new CalibrationParametersManager(paramsFile);
			
			List<CalibrationParameter> params = paramManager.getParameters();
			
			// Store the parameters for including them in the optimization process
			final int nParams = params.size();
			
			// Historical data
			int[][] salesHistory = null;
			try {
				salesHistory = CSVFileUtils.readRawHistoryFromCSV(historyFile);
			} catch (IOException e) {
				throw new CalibrationException(
					"Unable to load sales history file", e
				);
			}

			ModelDefinition modelDefinition = new ModelDefinition();
			modelDefinition.loadValuesFromFile(new File(modelFile));
			
			paramManager.setModelManager(new ModelManager(modelDefinition, 
				paramManager.getInvolvedDrivers()));
		
			double [] initialParams = new double[nParams];
			for (int i = 0; i < nParams; i++) {
				initialParams[i] = paramManager.getParameterValue(i);
			}
			
			HistoryManager manager = new SalesHistoryManager(historyPeriod, 
					salesHistory, totalSalesWeight, fitnessFunction,
					FitnessFunction.NO_HOLD_OUT);
			
			//Custom number of evaluations
			int targetEvaluations = 10000;
			
			return new CalibrationController(
					new CalibrationTask(
							RandomizerUtils.PRIME_SEEDS[0],
							experimentSignature,
							algorithmConfigFile,
							resultsFolder,
							paramManager,
							mcIterations,
							modelDefinition,
							manager,
							false
							), 
					initialParams,
					targetEvaluations
				);

			} catch (Throwable e) {
				e.printStackTrace();
			}
		return null;
	}

//###########################################################################//
//								RUN                                          //
//###########################################################################//
	/**
	 * Runs calibration experiment with given parameters.
	 * 
	 * @param experimentSignature - experiment signature.
	 * @param resultsFolder - result folder destination.
	 * @param csvname - result file name.
	 * @param algorithm - Algorithm name.
	 * @param scenario - experiment name.
	 * @param task - parameter configuration name.
	 * @param repetitions - number of experiment repetitions.
	 * @param mcIterations - number of Monte-Carlo iterations.
	 * @param historyPeriod - history check period (target seasonality 
	 * should fit this period).
	 * @param fitnessFunction - model evaluation function for guiding search. 
	 * @param totalSalesWeight - weight for final value against seasonable value.
	 * @throws CalibrationException - if problems arise during calibration.
	 * @throws IOException - if problems arise during opening/saving files.
	 * @throws TerminationException if the execution is canceled.
	 * @throws SalesScheduleError - if model execution fails at any point.
	 */
	private static void runTask(
			String experimentSignature,	String resultsFolder, String csvname,
			String algorithm, String scenario, String task, 
			int repetitions, int mcIterations,
			Statistics.TimePeriod historyPeriod, 
			FitnessFunction fitnessFunction,
			double totalSalesWeight
			) throws CalibrationException, IOException, SimulationException, TerminationException {
		
		// Task title
		String taskTitle = algorithm;	
		taskTitle += " over " + scenario;
		taskTitle += " with task " + task;
	
		// Algorithm folder
		String algorithmFolder = resultsFolder + 
			algorithm + "_" + scenario + "_" + task + FSEP;
		new File(algorithmFolder).mkdirs();
		
		// Algorithm config file
		String algorithmConfigFile = 
			CONFIG_FOLDER + ALGORITHM_FILE + 
			algorithm + ALGORITHM_FILE_EXT;
		
		// Model, params & history files
		String modelFile = 
			DATA_FOLDER + scenario + MODEL_FILE_EXT;
		String paramsFile = 
			DATA_FOLDER + scenario + "_" + task + PARAMS_FILE_EXT;
		String historyFile = 
			DATA_FOLDER + scenario + "_sales" + CSV_EXT;
		
		// Calibration parameters manager
		CalibrationParametersManager paramManager = 
			new CalibrationParametersManager(paramsFile);
						
		///////////////////////////////////////////////////////////////////////
		// Write CSV Header...
		List<CalibrationParameter> params = paramManager.getParameters();
		
		StringBuilder paramsHeader = 
			new StringBuilder(params.get(0).parameterName);
		
		for (int i = 1; i < params.size(); i++) {
			paramsHeader.append(CSV_SEP);
			paramsHeader.append(params.get(i).signature);
		}
		
		BufferedWriter csvfile = 
			new BufferedWriter(new FileWriter(csvname, false)); 

		csvfile.write(CSV_HEADER.replace("PARAMS", paramsHeader));
		csvfile.newLine();
		csvfile.close();
		///////////////////////////////////////////////////////////////////////
		
		ModelDefinition modelDefinition = new ModelDefinition();
		modelDefinition.loadValuesFromFile(new File(modelFile));
		
		// Historical data
		int[][] salesHistory = null;
		try {
			salesHistory = CSVFileUtils.readRawHistoryFromCSV(historyFile);
		} catch (IOException e) {
			throw new CalibrationException(
				"Unable to load sales history file", e
			);
		}
		
		// Store the parameters for including them in the optimization process
		final int nParams = params.size();

		paramManager.setModelManager(new ModelManager(modelDefinition, 
			paramManager.getInvolvedDrivers()));
	
		double [] initialParams = new double[nParams];
		for (int i = 0; i < nParams; i++)
			initialParams[i] = paramManager.getParameterValue(i);
		
		// Compute uncalibrated fitness
		EXPLOG("Experiment: " + taskTitle, true);
		EXPLOG("Signature: " + experimentSignature, true);
		EXPLOG("Computing uncalibrated fitness...", true);
		
		HistoryManager manager = new SalesHistoryManager(historyPeriod, 
				salesHistory, totalSalesWeight, fitnessFunction, 
				FitnessFunction.NO_HOLD_OUT);
		
		//Custom number of evaluations
		int targetEvaluations = 10000;
		
		CalibrationResult unclbResult = new CalibrationController(
			new CalibrationTask(
				mcIterations, 
				modelDefinition, 
				manager,
				!CalibrationTask.VALIDATE_MODEL,
				!CalibrationTask.INIT_SNAPSHOT
			),
			initialParams,
			targetEvaluations
		).execute(null,null,modelDefinition.getNumberOfAgents());
		String unclbFitness = String.format("%.6f", unclbResult.getFitness());	
		
		
		EXPLOG("Uncalibrated fitness is " + unclbFitness, true);
		
		for (int i = 0; i < repetitions; i++) {
		
			// Create calibration task
			CalibrationTask taskdef = new CalibrationTask(
				RandomizerUtils.PRIME_SEEDS[i], 
				experimentSignature, 
				algorithmConfigFile, 
				algorithmFolder, 
				paramManager, 
				mcIterations,
				modelDefinition, 
				manager,
				!CalibrationTask.VALIDATE_MODEL
			);	
			
			EXPLOG("Experiment: " + taskTitle, true);
			EXPLOG(
				"Repetition " + (i+1) + " of " + repetitions +
				 " (seed=" + taskdef.getCalibrationSeed() +  ")...", true
			); 
			
///////////////////////////////////////////////////////////////////////////////////////////
			CalibrationController clbController = 
					new CalibrationController(taskdef,initialParams,targetEvaluations); 
			long startTime = System.currentTimeMillis();
			CalibrationResult result = clbController.execute(
					CalibrationConsole.NO_MASTER_HOST, CalibrationConsole.NO_MASTER_PORT,
					modelDefinition.getNumberOfAgents());
			long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
/////////////////////////////////////////////////////////////////////////////////////////////
			
			String bestFitness = String.format("%.6f", result.getFitness());
			
			EXPLOG("Best fitness is " + bestFitness + " (of " + unclbFitness + ")", true);
			EXPLOG("Elapsed time: " + elapsedSeconds + " seconds.", true);
			
			// CSV of calibration results
			writeSolutionCSV(
				csvname, algorithm, scenario, task, 
				result, elapsedSeconds, bestFitness
			);
		}		
	}

	/**
	 * Writes calibration result.
	 * 
	 * @param csvname - CSV result log name.
	 * @param algorithm - algorithm name used during calibration.
	 * @param scenario - experiment name.
	 * @param task - parameter configuration name.
	 * @param result - bean containing calibration result.
	 * @param elapsedSeconds - calibration total time length.
	 * @param bestFitness - fitness of best solution found.
	 * @throws IOException - if problems arise when writing result.
	 */
	public static void writeSolutionCSV(
			String csvname, 
			String algorithm, String scenario, String task, 
			CalibrationResult result,
			long elapsedSeconds, String bestFitness
			) throws IOException {
		
		BufferedWriter csvfile;
		csvfile = new BufferedWriter(new FileWriter(csvname, true)); 
		csvfile.write(algorithm);
		csvfile.write(CSV_SEP);
		csvfile.write(scenario);
		csvfile.write(CSV_SEP);
		csvfile.write(task);
		csvfile.write(CSV_SEP);
		csvfile.write(bestFitness);
		
		csvfile.write(CSV_SEP);
		csvfile.write(Long.toString(elapsedSeconds));
		
		double[] parameters = result.getCalibratedParameters();
		for (int p = 0; p < parameters.length; p++) {
			csvfile.write(CSV_SEP);
			csvfile.write(Double.toString(parameters[p]));
			
		} 
		csvfile.newLine();
		csvfile.close();
	}
}
