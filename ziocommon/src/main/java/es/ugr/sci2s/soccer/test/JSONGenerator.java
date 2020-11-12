package es.ugr.sci2s.soccer.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import es.ugr.sci2s.soccer.beans.ComparisonConfig;
import es.ugr.sci2s.soccer.beans.SensitivityAnalysisConfig;
import es.ugr.sci2s.soccer.beans.SimulationConfig;
import model.ModelDefinition;
import util.io.SensitivityAnalysisBean;

public class JSONGenerator {

	public static void main(String[] args) {
		String jsonFile = "test/SensitivityConfig.json";
		String targetFile = "test/Model_with_initial_perceptions.zio";
		String comparisonFile = "test/ComparisonConfig.json";
		
		String simpleZio = "test/ZioTest.zio";
		String simpleJson = "test/ZioTest.json";
		////////////////////////
		///Simulacion simple
		ModelDefinition md = new ModelDefinition();
		File target = new File(simpleZio);
		md.loadValuesFromFile(target);
		SimulationConfig config = new SimulationConfig();
		config.loadFromModelDefinition(md);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String translated = gson.toJson(config, SimulationConfig.class);
		
		try {
			FileWriter fw = new FileWriter(simpleJson);
			fw.write(translated);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//////////////////////
		
		target = new File(targetFile);
		md.loadValuesFromFile(target);
		
		config = new SimulationConfig();
		config.loadFromModelDefinition(md);
		
		SensitivityAnalysisConfig saConfig = new SensitivityAnalysisConfig();
		saConfig.setSimulationConfig(config);
		saConfig.setBrandId(0);
		
		SensitivityAnalysisBean[] beans = new SensitivityAnalysisBean[1];
		beans[0] = new SensitivityAnalysisBean(0, 0.1, 0.2, 0.05);
		saConfig.setBeans(beans);
		
		gson = new GsonBuilder().setPrettyPrinting().create();
		translated = gson.toJson(saConfig, SensitivityAnalysisConfig.class);
		
		try {
			FileWriter fw = new FileWriter(jsonFile);
			fw.write(translated);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SimulationConfig[] simulations = new SimulationConfig[2];
		simulations[0]=config;
		simulations[1]=new SimulationConfig();
		
		simulations[1].loadFromModelDefinition(md);
		
		ComparisonConfig comparison = new ComparisonConfig();
		comparison.setSimulations(simulations);
		
		translated = gson.toJson(comparison, ComparisonConfig.class);
		
		try {
			FileWriter fw = new FileWriter(comparisonFile);
			fw.write(translated);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
