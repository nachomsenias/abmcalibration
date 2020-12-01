package es.ugr.sci2s.soccer.util.script;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.opencsv.CSVWriter;
import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;

import calibration.CalibrationParameter;
import calibration.CalibrationParametersManager;
import calibration.fitness.history.HistoryManager;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import es.ugr.sci2s.soccer.beans.SimulationConfig;
import model.ModelDefinition;
import model.ModelManager;
import model.ModelRunner;
import util.StringBean;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;
import util.statistics.MonteCarloStatistics;

public class GenerateHM {

	public static void main(String[] args) 
			throws JsonSyntaxException, IOException, CalibrationException, 
				IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		if (args.length != 5) {
			throw new IllegalArgumentException(
					"Format: input.json best.json output.csv Aparameter Bparameter");
		}
		
		String problem = args[0];
		String bestSolution = args[1];
		String csvOutput = args[2];

		String parameterA = args[3];
		String parameterB = args[4];
		
		//Read files
		Gson gson = new Gson();
		
		CalibrationConfig cc = gson.fromJson(
				CSVFileUtils.readFile(problem), CalibrationConfig.class);
		
		CalibrationResponse solution = gson.fromJson(
				CSVFileUtils.readFile(bestSolution), CalibrationResponse.class);
		
		SimulationConfig simConfig = solution.getCalibratedModel();
				
		int mcIterations = simConfig.getnMC();
		ModelDefinition md = simConfig.getModelDefinition();
		StringBean[] parameters = cc.getCalibrationModelParameters();
		CalibrationParametersManager paramManager = 
				new CalibrationParametersManager(parameters, md, false);
		HistoryManager manager = cc.getHistoryManager();
		ModelManager modelManager = new ModelManager(md, paramManager.getInvolvedDrivers());
		
		List<CalibrationParameter> modelParams = paramManager.getParameters();
		CalibrationParameter first = fetchByKey(parameterA, modelParams);
		CalibrationParameter second = fetchByKey(parameterB, modelParams);
		
		double valueA = 0.0;
		
		List<String[]> result = new ArrayList<String[]>();
		String[] header = {"Avalue","Bvalue","Fitness"};
		result.add(header);
		
		while(valueA<1.0) {
			//First parameter
			invoke(modelManager,first,valueA);
			
			double valueB = 0.0;
			while(valueB<1.0) {
				//Second parameter
				invoke(modelManager,second,valueB);
				
				MonteCarloStatistics mcStats = ModelRunner.simulateModel(md, mcIterations, false,
						manager.getStatsBean());
				ScoreWrapper wrapper = manager.computeTrainingScore(mcStats);
				
				//Done! Write result!
				String[] output = {
						String.valueOf(valueA),String.valueOf(valueB),
						String.valueOf(-wrapper.finalScore),
						};
				result.add(output);
				
				//Next iterations
				valueB+=0.02;
			}
			valueA+=0.02;
		}
		
		CSVWriter csvw = new CSVWriter(new FileWriter(new File(csvOutput)), ',',
				CSVWriter.NO_QUOTE_CHARACTER);
		csvw.writeAll(result);
		csvw.close();
	}
	
	private static CalibrationParameter fetchByKey(
			String key, List<CalibrationParameter> params) {
		for(CalibrationParameter param : params) {
			if(key.equals(param.parameterName)) {
				return param;
			}
		}
		throw new IllegalArgumentException("Uknown key: "+key);
	}
	
	protected static void invoke(
			ModelManager modelManager, CalibrationParameter param, double value)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		int[] indexes = param.indexes;
		Method setter = param.setterMethod;

		if (indexes == null) {
			setter.invoke(modelManager, value);
		} else {
			switch (indexes.length) {
			case 0:
				throw new IllegalStateException(
						"Undefined indexes for parameter: " + param.parameterName);
			case 1:
				setter.invoke(modelManager, indexes[0], value);
				break;
			case 2:
				setter.invoke(modelManager, indexes[0], indexes[1], value);
				break;
			case 3:
				setter.invoke(modelManager, indexes[0], indexes[1], indexes[2], value);
				break;
			case 4:
				setter.invoke(modelManager, indexes[0], indexes[1], indexes[2],
						indexes[3], value);
				break;
			default:
				throw new UnsupportedOperationException(
						"Cannot invoke setter with " + indexes.length + " indexes");
			}
		}
	}

}
