package es.ugr.sci2s.soccer.util.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import es.ugr.sci2s.soccer.beans.SimulationResult;
import org.apache.commons.math3.stat.StatUtils;

import com.google.gson.Gson;

public class ResultLoader {

	public static void main(String[] args) throws IOException {
		if(args.length!=1) {
			throw new IllegalArgumentException("Loading results requires 1 argument: A JSON result file.");
		}
		
		String jsonFile = args[0];

		
		//Read the JSON file
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String result = buffer.toString();
		
		//Get ModelDefinition
		Gson gson = new Gson();
		
		SimulationResult givenResult = gson.fromJson(result, SimulationResult.class);
		
		double[][][] salesByBrandBySegmentMC = givenResult.salesByBrandBySegmentMC;
		
		double avg = StatUtils.mean(salesByBrandBySegmentMC[5][0]) 
				+ StatUtils.mean(salesByBrandBySegmentMC[5][1]);
		
		System.out.println(avg);
		
		double[][][] salesByBrandBySegmentByStepAvg = givenResult.salesByBrandBySegmentByStepAvg;
		
		avg = StatUtils.sum(salesByBrandBySegmentByStepAvg[5][0]) + StatUtils.sum(salesByBrandBySegmentByStepAvg[5][1]);
		
		System.out.println(avg);
	}

}
