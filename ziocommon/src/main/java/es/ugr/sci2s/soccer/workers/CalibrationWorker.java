package es.ugr.sci2s.soccer.workers;

import java.net.URL;
import java.util.List;

import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;
import es.ugr.sci2s.soccer.beans.HoldOutResult;
import es.ugr.sci2s.soccer.beans.SimulationConfig;
import es.ugr.sci2s.soccer.beans.SimulationResult;

import calibration.CalibrationConsole;
import calibration.CalibrationController;
import calibration.CalibrationParameter;
import calibration.CalibrationParametersManager;
import calibration.CalibrationResult;
import calibration.CalibrationTask;
import calibration.fitness.FitnessFunction;
import calibration.fitness.history.HistoryManager;
import model.ModelDefinition;
import model.ModelManager;
import model.ModelRunner;
import util.StringBean;
import util.exception.calibration.CalibrationException;
import util.exception.simulation.SimulationException;
import util.exception.view.TerminationException;
import util.random.RandomizerUtils;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics.TimePeriod;

public class CalibrationWorker implements Runnable{
	
	public static final String DEFAULT_ALGORITHM_CONFIG_FILE = "configSSGA_HC.ecj";
	public static final String DEFAULT_LOG_FOLDER = "/tmp/";
	
	protected int id;
	
	protected ModelDefinition md;
	protected int mcIterations;
	
	protected SimulationConfig baseConfig;
	
	protected HistoryManager manager;
	protected TimePeriod statPeriod;
	
	protected CalibrationParametersManager paramManager;
	protected double [] initialParamValues;
	
	protected CalibrationResponse calibrationResponse;
	
	protected CalibrationController controller;
	
	protected CalibrationConfig calibrationSetup;
	
	public CalibrationWorker(CalibrationConfig calibrationSetup, 
			CalibrationResponse response) {

		this.calibrationResponse=response;		
		id=calibrationSetup.getId();
		
		//Model definition
		baseConfig = calibrationSetup.getSimConfig();
		md = baseConfig.getModelDefinition();
		mcIterations = baseConfig.getnMC();
		statPeriod = baseConfig.getStatPeriod();
		
		this.calibrationSetup = calibrationSetup;
		
		manager=calibrationSetup.getHistoryManager();
	}
	
	protected void setupParameterManager(boolean realCoding) {
		StringBean[] paramsBeans = calibrationSetup.getCalibrationModelParameters();
		try {
			paramManager = new CalibrationParametersManager(paramsBeans, md, realCoding);
		} catch (CalibrationException e) {
			e.printStackTrace();
			calibrationResponse.fail();
		}
		paramManager.setModelManager(new ModelManager(md, 
				paramManager.getInvolvedDrivers()));
		
		List<CalibrationParameter> params = paramManager.getParameters();
		
		//Gather initial values for selected parameters
		final int nParams = params.size();
		initialParamValues = new double[nParams];
		
		for (int i = 0; i < nParams; i++) {
			try {
				initialParamValues[i] = paramManager.getParameterValue(i);
			} catch (CalibrationException e) {
				System.out.println("Error loading "+ paramsBeans[i].toString());
				e.printStackTrace();
				calibrationResponse.fail();
				break;
			}
		}
	}

	protected void lauchCalibration(
			String ecjAlg, 
			String masterHost,
			String masterPort
		) {
		
		//Setup the calibration parameter manager
		setupParameterManager(false);
		
		//Find algorithm configuration file
		URL algorithmURL = this.getClass().getClassLoader().getResource(ecjAlg);
		String algorithmConfigFile=algorithmURL.getPath();
		System.out.println(algorithmConfigFile);
		
		try {
			CalibrationTask task = new CalibrationTask(
					RandomizerUtils.PRIME_SEEDS[0], 
					String.valueOf(id), 
					algorithmConfigFile, 
					DEFAULT_LOG_FOLDER, 
					paramManager, 
					mcIterations, 
					md,
					manager,
					CalibrationTask.SKIP_VALIDATION
					);
			
			controller = new CalibrationController(task, 
					initialParamValues, calibrationSetup.getTargetEvaluations());
			
			/* Computes the initial adjustment simulating the initial model
			 * and gathering score and simulation time.
			 */
			initializeCalibration();
			
			/*
			 * Adjust number of agents during optimization if the parameter was provided.
			 */
			if(calibrationSetup.getCalibrationAgents()!= CalibrationConfig.USE_BASE_VALUE) {
				md.setNumberOfAgents(calibrationSetup.getCalibrationAgents());
			}
			
			/* If no errors where detected during initialization, the 
			 * calibration process starts.
			 */
			if(!calibrationResponse.hasFailed()) {
				CalibrationResult result =controller.execute(masterHost,
						masterPort, baseConfig.getNumberOfAgents());
				
				controller.updateModelDefinition(md, 
						result.getUnconvertedParameters());
				
				calibrationResponse.setCalibratedModel(md, baseConfig);
				
				//Training score details
				calibrationResponse.setScoreDetails(
						manager.computeTrainingScore(
								result.getSimulationResultMC()));

				//HoldOut score details
				if(calibrationSetup.getHoldOut()!=FitnessFunction.NO_HOLD_OUT) {
					calibrationResponse.setHoldOutDetails(
							manager.computeHoldOutScore(
									result.getSimulationResultMC()));
				}
				
				calibrationResponse.done();
			}
			
		} catch (CalibrationException e) {
			calibrationResponse.fail();
			calibrationResponse.setErrorMessage(e.getMessage());
			e.printStackTrace();
		} catch (SimulationException e) {
			calibrationResponse.fail();
			calibrationResponse.setErrorMessage(e.getMessage());
			e.printStackTrace();
		} catch (TerminationException e) {
			calibrationResponse.fail();
			System.out.println("Calibration with id '"
					+this.id+"' has been canceled.");
		}
	}
	
	protected void initializeCalibration() {
		
		System.out.println("Initializing calibration...");

		try {
			long simulationTime = evaluateInitialModel();
			
			controller.setSimulationTime(simulationTime);
			
			System.out.println("Uncalibrated fitness: "
					+calibrationResponse.printScoreValues());
			System.out.println("Simulation time: " +simulationTime);
		
		} catch (SimulationException e) {
			registerError("Errors initializing calibration:\n"+e.getMessage());
		}
	}
	
	private long evaluateInitialModel() throws SimulationException{
		//Simulates the model for getting a estimation of its computation time.
		long before = System.currentTimeMillis();
		
		MonteCarloStatistics mcStats= ModelRunner.simulateModel(
					md, mcIterations, false,
					manager.getStatsBean());
		
		long after = System.currentTimeMillis();
		
		long simulationTime = after-before;
		
		//Store simulation values
		SimulationResult result = new SimulationResult();
		
		result.loadValuesFromStatistics(mcStats, md.getAgentsRatio(), 
				manager.getStatsBean(), statPeriod);
		
		calibrationResponse.setResult(result);
		calibrationResponse.setScoreDetails(
				manager.computeTrainingScore(
						mcStats));
		if(calibrationSetup.getHoldOut()!=FitnessFunction.NO_HOLD_OUT) {
			calibrationResponse.setHoldOutDetails(
					manager.computeHoldOutScore(
							mcStats));
			calibrationResponse.setHoldOutResult(
					HoldOutResult.splitHoldOut(result, 
							calibrationSetup.getHoldOut()));
		}
		
		return simulationTime;
	}
	
	private void registerError(String message) {
		calibrationResponse.fail();
		calibrationResponse.setErrorMessage(message);
		
		System.out.println(message);
	}
	
	protected void evaluate() {
		try {
			calibrationResponse.setMillisecondsLeft(
						SimulationWorker.estimateDuration(
								md.getNumberOfAgents(), 
								md.getNumberOfWeeks())
					);
			
			long simulationTime = evaluateInitialModel();
			
			calibrationResponse.setMillisecondsLeft(simulationTime);
			calibrationResponse.done();
		
		} catch (SimulationException e) {
			registerError("Errors evaluating the model:\n"+e.getMessage());
			System.out.println("**** Printing Trace ****");
			e.printStackTrace();
		}
	}
	
	public long estimateTimeLeft() {
		return controller.estimateTimeLeft();
	}
	
	@Override
	public void run() {
		lauchCalibration(calibrationSetup.getAlgorithm(), 
				CalibrationConsole.NO_MASTER_HOST, 
					CalibrationConsole.NO_MASTER_PORT);
	}
	
	public void terminate() {
		controller.terminateCalibration();
	}
}
