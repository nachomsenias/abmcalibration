package es.ugr.sci2s.soccer.util.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;

import es.ugr.sci2s.soccer.beans.SimulationConfig;
import model.ModelDefinition;

public class JSONtoZio {

	public static void main(String[] args) throws IOException {
		
		if(args.length!=2) {
			throw new IllegalArgumentException("JSON to Zio uses 2 arguments.");
		}
		
		String jsonFile = args[0];
		String zioFile = args[1];
		
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
		
		SimulationConfig givenConfig = gson.fromJson(config, SimulationConfig.class);
		
		ModelDefinition md = givenConfig.getModelDefinition();
		
		//Export to Zio
		FileWriter fw = new FileWriter(zioFile);
		fw.write(md.export());
		fw.close();

	}
}
