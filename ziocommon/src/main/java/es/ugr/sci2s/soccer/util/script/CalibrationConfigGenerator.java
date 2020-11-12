package es.ugr.sci2s.soccer.util.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import com.google.gson.Gson;

import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;
import util.StringBean;

public class CalibrationConfigGenerator {

	public static void main(String[] args) throws IOException {
		
		if (args.length!=3) {
			throw new IllegalArgumentException("This needs to files: CalibrationConfig + CalibrationResponse + Output");
		}
		
		String calibrationConfig = args[0];
		String calibratedResult = args[1];
		String outputFile = args[2];
		
		Gson gson = new Gson();

		//Read JSON files
		BufferedReader br = new BufferedReader(new FileReader(calibrationConfig));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String stringConfig = buffer.toString();
		
		CalibrationConfig config = gson.fromJson(stringConfig, CalibrationConfig.class);
		
		br = new BufferedReader(new FileReader(calibratedResult));
		
		buffer = new StringBuilder();
		line = "";
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		stringConfig = buffer.toString();
		
		CalibrationResponse resultConfig = gson.fromJson(stringConfig, CalibrationResponse.class);
		config.setSimConfig(resultConfig.getCalibratedModel());
		
		double[][][] initialPerceptions = config.getSimConfig().getInitialPerceptions();
		DecimalFormat dc = new DecimalFormat("#.##");
		
		StringBean[] parameterBeans = config.getCalibrationModelParameters();
		for (int i=0; i<parameterBeans.length; i++) {
			StringBean bean = parameterBeans[i];
			String key = bean.getKey();
			
			if (key.contains("SegmentInitialPerceptions")) {
				String [] splitted = key.split("_");
				int segment = Integer.parseInt(splitted[1]);
				int brand = Integer.parseInt(splitted[2]);
				int att = Integer.parseInt(splitted[3]);
				
				double perceptionValue = initialPerceptions[segment][brand][att];
				
				String newValue = dc.format(perceptionValue-0.5).replace(',', '.')+","+
						dc.format(perceptionValue+0.5).replace(',', '.')+",0.01";
				
				bean.setValue(newValue);
			}
		}
		
		String output = gson.toJson(config, CalibrationConfig.class);
		
		//Export to File
		FileWriter fw = new FileWriter(outputFile);
		fw.write(output);
		fw.close();
	}

}
