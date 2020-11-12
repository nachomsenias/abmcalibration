package es.ugr.sci2s.soccer.util.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.SimulationConfig;

public class ConfigUnwrapper {

	public static void main(String[] args) throws IOException {
		String json = args[0];
		String unwrapped = args[1];
		
		boolean mode = false; //SimulationConfig
		if(args.length>2) {
			String configMode = args[2];
			if(configMode.equals("Calibration")) {
				mode = true; //CalibrationConfig
			}
		}
		
		//Read the JSON file
		BufferedReader br = new BufferedReader(new FileReader(json));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String config = buffer.toString();
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		
		String output;
		
		if(mode) {
			CalibrationConfig simConfig = gson.fromJson(config, CalibrationConfig.class);

			output = gson.toJson(simConfig, CalibrationConfig.class);
		} else {
			SimulationConfig simConfig = gson.fromJson(config, SimulationConfig.class);

			output = gson.toJson(simConfig, SimulationConfig.class);
		}

		FileWriter fw = new FileWriter(unwrapped);
		fw.write(output);
		fw.close();
	}

}
