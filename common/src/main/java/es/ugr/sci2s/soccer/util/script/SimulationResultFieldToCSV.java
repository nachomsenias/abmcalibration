package es.ugr.sci2s.soccer.util.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;

import com.google.gson.Gson;

import es.ugr.sci2s.soccer.beans.SimulationResult;
import util.io.CSVFileUtils;

public class SimulationResultFieldToCSV {

	public static void main(String[] args) {
		String simulationResultFile = args[0];
		String key = args[1];
		String outputFile = args[2];
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(simulationResultFile));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
			//Get the desired key from the result
			Field field = result.getClass().getDeclaredField(key);
			field.setAccessible(true);
			
			Class<?> targetType = field.getType();
			
			Object value = field.get(result);
			
			//XXX Using id = 3
			int brandId = 0;
			
			//3D Double matrix
			if(targetType.equals(double[][][].class)) {
				double[][][] matrix = (double[][][])value;
				
				CSVFileUtils.writeDoubleTwoDimArrayToCSV(outputFile, matrix[brandId], ';');
			} else if (targetType.equals(double[][][][].class)) {
				double[][][][] matrix = (double[][][][])value;
				
				CSVFileUtils.writeDoubleThreeDimArrayToCSV(outputFile, matrix[brandId], ';');
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
