package es.ugr.sci2s.soccer.util;

import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;
import es.ugr.sci2s.soccer.beans.SimulationConfig;

import calibration.CalibrationParametersManager;
import calibration.fitness.history.HistoryManager;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import model.ModelDefinition;
import model.ModelManager;
import model.ModelRunner;
import util.StringBean;
import util.exception.calibration.CalibrationException;
import util.functions.ArrayFunctions;
import util.io.CSVFileUtils;
import util.random.Randomizer;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;
import util.statistics.MonteCarloStatistics;

public class ExecuteRandomEvaluations {

	public static void main(String[] args) throws IOException, CalibrationException {
		String config = CSVFileUtils.readFile(args[0]);

		Gson gson = new Gson();

		// Options
		ExecuteCalibration.Options customOptions = ExecuteCalibration.Options.parseOptions(args);
		
		// Read configuration from JSON
		CalibrationConfig calibrationConfig = gson.fromJson(config,
				CalibrationConfig.class);

		//Create result folder
		String folder;
		
		if(customOptions.realCoding) {
			folder = args[1]+"-real/";
		} else {
			folder = args[1]+"/";
		}
		
		File log = new File(folder);
		
		if(!log.exists()) {
			log.mkdir();
		}
		
		SimulationConfig simConfig = calibrationConfig.getSimConfig();
		int mcIterations = simConfig.getnMC();
		ModelDefinition md = simConfig.getModelDefinition();
		StringBean[] parameters = calibrationConfig.getCalibrationModelParameters();

		CalibrationParametersManager paramManager = 
				new CalibrationParametersManager(parameters, md, customOptions.realCoding);
		HistoryManager manager = calibrationConfig.getHistoryManager();
		ModelManager modelManager = new ModelManager(md, paramManager.getInvolvedDrivers());
		
		paramManager.setModelManager(modelManager);
		
		int numParams = calibrationConfig.getCalibrationModelParameters().length;

		for (int run = customOptions.repeatedIterationsStart; run < customOptions.repeatedIterations
				+ customOptions.repeatedIterationsStart; run++) {

			Randomizer random = RandomizerFactory.createRandomizer(
					RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
					RandomizerUtils.PRIME_SEEDS[run]);

			double best = Double.MAX_VALUE;
			double[] bestIndividual = null;
			
			int iteration = 0;
			while (iteration < 10000) {

				double[] individual = getNewInd(random, numParams,customOptions.realCoding);
				ScoreWrapper score = evaluate(individual, modelManager, 
						paramManager, md, mcIterations, manager);
				
				if(score.finalScore<best) {
					best = score.finalScore;
					
					bestIndividual = individual;
				}

				iteration++;
			}

			CalibrationResponse response = new CalibrationResponse();
			
			ScoreWrapper bestScore = evaluate(bestIndividual, modelManager, 
					paramManager, md, mcIterations, manager);
			
			response.setCalibratedModel(md, simConfig);
			response.setScoreDetails(bestScore);

			System.out.println("Final score: "
					+response.printScoreValues());
			
			response.done();
			
			String output = folder+"resulting_random_real_it"+run+".json";
			CSVFileUtils.writeFile(output,
					gson.toJson(response, CalibrationResponse.class));
		}
	}
	
	public static double[] getNewInd(Randomizer rnd, int numParams, boolean real) {
		if(real) {
			return ArrayFunctions.getRandomDoubleArray(rnd, numParams);
		} else {
			return ArrayFunctions.intToDouble(
					ArrayFunctions.getRandomIntArray(rnd, numParams, 1000));
		}
	}
	
	public static ScoreWrapper evaluate(double[] solution, ModelManager modelManager, 
			CalibrationParametersManager paramManager, ModelDefinition md, 
			int mcIterations, HistoryManager manager) {
		int variables = solution.length;
		for (int v = 0; v < variables; v++) {
			try {
//				invoke(modelManager, modelParams.get(v), solution[v]);
				paramManager.setParameterValue(v, solution[v]);
				System.out.print(solution[v] + " ");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (CalibrationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Simulates the model for getting a estimation of its computation time.
		long before = System.currentTimeMillis();

		MonteCarloStatistics mcStats = ModelRunner.simulateModel(md, mcIterations, false,
				manager.getStatsBean());

		long simulationTime = System.currentTimeMillis() - before;

		// Store simulation values
		ScoreWrapper wrapper = manager.computeTrainingScore(mcStats);
		// Print agregated score
		System.out.println(":: " + (wrapper.finalScore) + " ;; " + simulationTime);
		
		return wrapper;
	}
}
