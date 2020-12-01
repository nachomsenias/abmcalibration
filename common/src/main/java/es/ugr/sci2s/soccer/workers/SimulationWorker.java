package es.ugr.sci2s.soccer.workers;

import java.util.Map;

import com.google.gson.Gson;
import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;
import es.ugr.sci2s.soccer.beans.ComparisonConfig;
import es.ugr.sci2s.soccer.beans.HoldOutResult;
import es.ugr.sci2s.soccer.beans.ResultContainer;
import es.ugr.sci2s.soccer.beans.SensitivityAnalysisConfig;
import es.ugr.sci2s.soccer.beans.SimulationConfig;
import es.ugr.sci2s.soccer.beans.SimulationResult;

import calibration.fitness.FitnessFunction;
import calibration.fitness.history.HistoryManager;
import model.ModelDefinition;
import model.ModelRunner;
import util.exception.simulation.SimulationException;
import util.io.ComparisonResult;
import util.io.ScenarioComparison;
import util.io.SensitivityAnalysisBean;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics.TimePeriod;


public class SimulationWorker implements Runnable{

	public enum SimulationType{
		SIMPLE, EVALUATION, SENSITIVITY_ANALYSIS, TP_CONTRIBUTION, COMPARISON}; 
	
	protected String config;
	protected ResultContainer result;
	
	protected Map<Integer, ResultContainer> table;
	
	protected SimulationType executionType;
	
	protected Gson gson;
	
	protected SimulationConfig referenceConfig;
	
	protected int id=-1;
	
	protected ScenarioComparison comparator;
	
	//Estimation values ::
	private int nAgents = 10000;
	private int nSteps = 52;
	private int nExecutions = 10;
	
	private boolean canEstimate = false;
	private boolean stop = false;
	
	private static final double BASE_MILLIS = 0.04;
	
	private static final long BASE_SIMPLE = 20000;
	private static final long BASE_SA = BASE_SIMPLE * 8;
	private static final long BASE_TP = BASE_SIMPLE * 10;
	private static final long BASE_COMP = BASE_SIMPLE * 5;
	
	public SimulationWorker(String config, ResultContainer result, 
			Map<Integer, ResultContainer> table, int id) {
		this.config = config;
		this.result = result;
		this.table = table;
		this.id = id;
		
		comparator = new ScenarioComparison();
		
		gson = new Gson();
	}
	
	private void createTimer() {
		ResultTimer timer = new ResultTimer(id,table);
		Thread timerThread = new Thread(timer);
		timerThread.start();
	}
	
	@Override
	public void run() {
		try {
			createTimer();
			
			switch (executionType) {
			case SIMPLE:
				executeSimple();
				break;
			case EVALUATION:
				executeEvaluation();
				break;
			case SENSITIVITY_ANALYSIS:
				executeSA();
				break;
			case TP_CONTRIBUTION:
				executeTP();
				break;
			case COMPARISON:
				executeComparison();
				break;
			default:
				result.fail();
				result.setErrorMessage("Invalid simulation type.");
				break;
			}
		} catch (Exception e) {
			result.setErrorMessage(e.getMessage());
			result.fail();
		}
		result.setFinished(true);
	}
	
	public void executeSimple() {
		//Read configuration from JSON
		referenceConfig = 
				gson.fromJson(config, SimulationConfig.class);
		
		SimulationResult newResult = new SimulationResult(); 
		
		try {
			//Run simulation
			ModelDefinition md = referenceConfig.getModelDefinition();
			
			//Estimation values
			nAgents = md.getNumberOfAgents();
			nSteps = md.getNumberOfWeeks();
			canEstimate = true;
			
			StatisticsRecordingBean recordingBean = 
					referenceConfig.getStatisticRecordingConfiguration();
			MonteCarloStatistics statistics = ModelRunner.simulateModel(
					md, 
					referenceConfig.getnMC(), 
					false, 
					recordingBean
				);
			
			
			newResult.loadValuesFromStatistics(
					statistics,
						md.getAgentsRatio(),
								recordingBean,
								referenceConfig.getStatPeriod());
		} catch (SimulationException e) {
			newResult.setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
		
		result.setSimpleResult(newResult);
	}
	
	public void executeEvaluation() {
		//Read configuration from JSON
		CalibrationConfig calibrationConfig = gson.fromJson(config, CalibrationConfig.class);
		CalibrationResponse response = new CalibrationResponse();
		
		SimulationConfig baseConfig = calibrationConfig.getSimConfig();
		ModelDefinition md = baseConfig.getModelDefinition();
		int mcIterations = baseConfig.getnMC();
		TimePeriod statPeriod = baseConfig.getStatPeriod();
		
		HistoryManager manager=calibrationConfig.getHistoryManager();
		
		MonteCarloStatistics mcStats = ModelRunner.simulateModel(
				md, mcIterations, false,
				manager.getStatsBean());
		
		//Store simulation values
		SimulationResult simulationResult = new SimulationResult();
		
		simulationResult.loadValuesFromStatistics(mcStats, md.getAgentsRatio(), 
				manager.getStatsBean(), statPeriod);
		
		response.setResult(simulationResult);
		response.setScoreDetails(
				manager.computeTrainingScore(
						mcStats));
		response.done();
		
		if(calibrationConfig.getHoldOut()!=FitnessFunction.NO_HOLD_OUT) {
			response.setHoldOutDetails(
					manager.computeHoldOutScore(
							mcStats));
			response.setHoldOutResult(
					HoldOutResult.splitHoldOut(simulationResult, 
							calibrationConfig.getHoldOut()));
		}
		
		result.setEvaluationResult(response);
	}
	
	/**
	 * Runs a Sensitivity Analysis using provided configuration.
	 */
	public void executeSA() {
		//Read configuration from JSON
		SensitivityAnalysisConfig requestedConfig = gson.fromJson(
				config, SensitivityAnalysisConfig.class);
		referenceConfig = requestedConfig.getSimulationConfig(); 
		SensitivityAnalysisBean[] beans = requestedConfig.getBeans();
		
		int numBeans = beans.length;
		ModelDefinition md = referenceConfig.getModelDefinition();
		StatisticsRecordingBean recordingBean = referenceConfig.getStatisticRecordingConfiguration();
		SimulationResult[][] results = new SimulationResult[numBeans][];
		
		//Estimation values
		nAgents = md.getNumberOfAgents();
		nSteps = md.getNumberOfWeeks();
		nExecutions *=numBeans;
		canEstimate = true;
		
		//Run Sensitivity Analysis
		int iteration = 0;
//		for (int i=0; i<numBeans; i++) {
		while(iteration<numBeans && !stop) {
			SensitivityAnalysisBean analysis=beans[iteration];
			ComparisonResult result = comparator.runSA(
					analysis.getTouchpoint(), 
					requestedConfig.getBrandId(), 
					analysis.getMin(), 
					analysis.getMax(), 
					analysis.getStep(), 
					referenceConfig.getnMC(), 
					md, analysis.getDestFolder(),analysis.getScenarioName(), 
							ScenarioComparison.FROM_CONSOLE, 
								ScenarioComparison.NO_REPORTS, recordingBean);
			results[iteration] = SimulationResult.getResults(result.getStats(), 
					referenceConfig.getStatPeriod(),
					md.getAgentsRatio(), recordingBean);
			
			iteration++;
		}
		
		//Store result
		result.setSaResult(results);
	}
	
	public void executeTP() {
		//Read configuration from JSON
		SensitivityAnalysisConfig requestedConfig = gson.fromJson(
				config, SensitivityAnalysisConfig.class);
		referenceConfig = requestedConfig.getSimulationConfig(); 
		
		ModelDefinition md = referenceConfig.getModelDefinition();
		StatisticsRecordingBean recordingBean = referenceConfig.getStatisticRecordingConfiguration();
		
		//Estimation values
		nAgents = md.getNumberOfAgents();
		nSteps = md.getNumberOfWeeks();
		nExecutions = md.getNumberOfTouchPoints()+1;
		canEstimate = true;
		
		//No reports are generated from the servlet, so names arent needed.
		String emptyString = "";
		
		//Run Tp contribution
		ComparisonResult tpResult = comparator.runContribution(
				requestedConfig.getBrandId(), 
				referenceConfig.getnMC(), 
				md, emptyString,emptyString, 
				ScenarioComparison.FROM_CONSOLE, 
				ScenarioComparison.NO_REPORTS, recordingBean);
		if(!stop) {
			SimulationResult[] results = SimulationResult.getResults(
					tpResult.getStats(), 
					referenceConfig.getStatPeriod(),
					md.getAgentsRatio(), recordingBean);
			
			//Store result
			result.setComparisonResult(results);
		}
	}
	
	public void executeComparison() {
		//Read configuration from JSON
		ComparisonConfig requestedConfig = gson.fromJson(
				config, ComparisonConfig.class);
		SimulationConfig[] simulations = requestedConfig.getSimulations(); 
		int numModels = simulations.length;
		ModelDefinition[] mds = new ModelDefinition[numModels];
		for (int i=0; i<numModels; i++) {
			mds[i] = simulations[i].getModelDefinition();
		}
		
		//Estimation values using first model
		nAgents = mds[0].getNumberOfAgents();
		nSteps = mds[0].getNumberOfWeeks();
		nExecutions = numModels;
		canEstimate = true;
		
		//No reports are generated from the servlet, so names arent needed.
		String emtpyString = "";
		referenceConfig = simulations[0];
		StatisticsRecordingBean recordingBean = referenceConfig.getStatisticRecordingConfiguration();
		
		MonteCarloStatistics[] statistics = comparator.runComparison(
				mds, referenceConfig.getnMC(), emtpyString,
				ScenarioComparison.FROM_CONSOLE, ScenarioComparison.NO_REPORTS, 
					recordingBean);
		if(!stop) {
			SimulationResult[] results = SimulationResult.getResults(statistics, 
					referenceConfig.getStatPeriod(),
					referenceConfig.getRatio(), recordingBean);			
			//Store result
			result.setComparisonResult(results);
		}
	}

	public SimulationType getExecutionType() {
		return executionType;
	}

	public void setExecutionType(SimulationType executionType) {
		this.executionType = executionType;
	}
	
	public long estimateDuration() {
		//If estimation values are not available, return basic estimation.
		if(!canEstimate) {
			switch (executionType) {
			case SIMPLE:
				return BASE_SIMPLE;
			case EVALUATION:
				return BASE_SIMPLE;
			case SENSITIVITY_ANALYSIS:
				return BASE_SA;
			case TP_CONTRIBUTION:
				return BASE_TP;
			case COMPARISON:
				return BASE_COMP;
			default:
				//This should not be reached.
				return Integer.MAX_VALUE;
			}
		}
		
		switch (executionType) {
		case SIMPLE:
			return (long)(nAgents * nSteps * BASE_MILLIS);

		default:
			return (long)(nAgents * nSteps * nExecutions * BASE_MILLIS);
		}
	}
	
	public static long estimateDuration(int nAgents, int nSteps) {
		return (long)(nAgents * nSteps * BASE_MILLIS); 
	}
	
	public void stop() {
		this.stop = true;
		comparator.stop();
	}
	
	@Override
	public String toString() {
		return id+" : "+executionType.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (canEstimate ? 1231 : 1237);
		result = prime * result + ((config == null) ? 0 : config.hashCode());
		result = prime * result + ((executionType == null) ? 0 : executionType.hashCode());
		result = prime * result + ((gson == null) ? 0 : gson.hashCode());
		result = prime * result + id;
		result = prime * result + nAgents;
		result = prime * result + nExecutions;
		result = prime * result + nSteps;
		result = prime * result + ((referenceConfig == null) ? 0 : referenceConfig.hashCode());
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimulationWorker other = (SimulationWorker) obj;
		if (canEstimate != other.canEstimate)
			return false;
		if (config == null) {
			if (other.config != null)
				return false;
		} else if (!config.equals(other.config))
			return false;
		if (executionType != other.executionType)
			return false;
		if (gson == null) {
			if (other.gson != null)
				return false;
		} else if (!gson.equals(other.gson))
			return false;
		if (id != other.id)
			return false;
		if (nAgents != other.nAgents)
			return false;
		if (nExecutions != other.nExecutions)
			return false;
		if (nSteps != other.nSteps)
			return false;
		if (referenceConfig == null) {
			if (other.referenceConfig != null)
				return false;
		} else if (!referenceConfig.equals(other.referenceConfig))
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		return true;
	}
}
