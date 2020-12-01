package es.ugr.sci2s.soccer.test.junit;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import es.ugr.sci2s.soccer.beans.SimulationConfig;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.ModelDefinition;

public class TranslationUnitTest {
	
	private ModelDefinition mdZio;
	private ModelDefinition mdJson;
	
	private String config;
	
	public TranslationUnitTest() throws IOException {
		//Zio File
		String simpleZio = "test/ZioTest.zio";
		mdZio = new ModelDefinition();
		File target = new File(simpleZio);
		mdZio.loadValuesFromFile(target);
		
		//Read the JSON file
		String jsonFile = "test/ZioTest.json";
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		config = buffer.toString();
		
		//Get ModelDefinition
		Gson gson = new Gson();
		
		SimulationConfig givenConfig = gson.fromJson(config, SimulationConfig.class);
		
		mdJson = givenConfig.getModelDefinition();
	}

	@Test
	public void equalsTest() {
		
		boolean equals = mdZio.equals(mdJson);
		
		assertTrue(equals);
	}
	
	@Test
	public void generationTest() {
		
		SimulationConfig simConfig = new SimulationConfig();
		simConfig.loadFromModelDefinition(mdZio);
		
		Gson gson = new GsonBuilder().create();
		String translated = gson.toJson(simConfig, SimulationConfig.class);
		
		SimulationConfig givenConfig = gson.fromJson(translated, SimulationConfig.class);
		
		ModelDefinition generatedDefinition = givenConfig.getModelDefinition();
		
		boolean same = mdZio.equals(generatedDefinition);
		
		assertTrue(same);
	}

}
