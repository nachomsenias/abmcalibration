package es.ugr.sci2s.soccer.util.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import es.ugr.sci2s.soccer.beans.SimulationConfig;

import es.ugr.sci2s.soccer.beans.SimulationResult;
import model.ModelDefinition;
import model.ModelRunner;
import util.exception.simulation.SimulationException;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;

public class RunJSON {

	public static void main(String[] args) throws IOException, SimulationException {
		if(args.length!=1 && args.length!=2) {
			throw new IllegalArgumentException("1 argument(json file) or 2 (output file) are required.");
		}
		
		String jsonFile = args[0];

		//Start simulation
		long initial=System.currentTimeMillis();
		
		//Read the JSON file
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String config = buffer.toString();
		
		//Get ModelDefinition
		Gson gson = new Gson();
		
		SimulationConfig givenConfig = gson.fromJson(config,
				SimulationConfig.class);
		
		ModelDefinition md = givenConfig.getModelDefinition();
		StatisticsRecordingBean recordingBean = 
				givenConfig.getStatisticRecordingConfiguration();
		
		MonteCarloStatistics stats = ModelRunner.simulateModel(
				md, givenConfig.getnMC(), false, 
				recordingBean
			);
		
		long end = System.currentTimeMillis();
		System.out.println(String.format(
				" Full simulation time: %.3f sec.", 
				(end - initial) / 1000.0
			));
		
		if(args.length==2) {
			SimulationResult newResult = new SimulationResult();
			
			newResult.loadValuesFromStatistics(
					stats,
						md.getAgentsRatio(),
								recordingBean,
								givenConfig.getStatPeriod());
			
			String output = gson.toJson(newResult, SimulationResult.class);
			
			//Export to File
			FileWriter fw = new FileWriter(args[1]);
			fw.write(output);
			fw.close();
		}
	}
	
	public static SimulationResult runSimulationConfig(SimulationConfig sc) {
		ModelDefinition md = sc.getModelDefinition();
		StatisticsRecordingBean recordingBean = 
				sc.getStatisticRecordingConfiguration();
		
		MonteCarloStatistics stats = ModelRunner.simulateModel(
				md, sc.getnMC(), false, recordingBean
			);
		
		SimulationResult newResult = new SimulationResult();
		
		newResult.loadValuesFromStatistics(
				stats,
					md.getAgentsRatio(),
							recordingBean,
							sc.getStatPeriod());
		
		return newResult;
	}

}
