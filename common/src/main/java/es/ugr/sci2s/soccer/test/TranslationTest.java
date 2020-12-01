package es.ugr.sci2s.soccer.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import es.ugr.sci2s.soccer.beans.SimulationConfig;
import model.ModelDefinition;

public class TranslationTest {

	public static void main(String[] args) {
		String targetFile = "test/Model_with_initial_perceptions.zio";
		String jsonFile = "test/Model_with_initial_perceptions.json";
		String testFile = "test/Model_with_initial_perceptions2.zio";
		ModelDefinition md = new ModelDefinition();
		
		File target = new File(targetFile);
		md.loadValuesFromFile(target);
		
		SimulationConfig config = new SimulationConfig();
		config.loadFromModelDefinition(md);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String translated = gson.toJson(config, SimulationConfig.class);
		
		try {
			FileWriter fw = new FileWriter(jsonFile);
			fw.write(translated);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ModelDefinition newMD = config.getModelDefinition();
		try {
			FileWriter fw = new FileWriter(testFile);
			fw.write(newMD.export());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
