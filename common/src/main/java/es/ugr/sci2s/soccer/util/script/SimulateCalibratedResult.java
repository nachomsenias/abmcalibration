package es.ugr.sci2s.soccer.util.script;

import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;
import es.ugr.sci2s.soccer.beans.SimulationConfig;

import es.ugr.sci2s.soccer.beans.SimulationResult;
import util.exception.simulation.SimulationException;
import util.io.CSVFileUtils;

public class SimulateCalibratedResult {

	/**
	 * Simulates a calibrated model from a CalibrationResponse object and stores it
	 * as a JSON representation of a SimulationResult.
	 * @param args
	 * @throws IOException
	 * @throws SimulationException
	 */
	public static void main(String[] args) throws IOException, SimulationException {
		if(args.length!=1 && args.length!=2) {
			throw new IllegalArgumentException(
					"1 argument(json file) or 2 (output file) are required.");
		}
		
		String jsonFile = args[0];
		String content = CSVFileUtils.readFile(jsonFile);
		
		Gson gson = new Gson();
		
		CalibrationResponse givenConfig = gson.fromJson(content,
				CalibrationResponse.class);
		SimulationConfig sc = givenConfig.getCalibratedModel();
		
		SimulationResult result = RunJSON.runSimulationConfig(sc);
		
		String output = gson.toJson(result, SimulationResult.class);
		
		//Export to File
		FileWriter fw = new FileWriter(args[1]);
		fw.write(output);
		fw.close();
	}
}
