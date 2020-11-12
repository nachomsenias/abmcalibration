package test;

import java.io.File;
import java.util.Enumeration;

import util.io.ConfigFileReader;

public class ReadCalibrationConfig {
	
	// ########################################################################
	// Variables
	// ########################################################################
	
	private ConfigFileReader config; 
	private static String FILENAME = "./examples/ReadCalibrationConfig.test";
	
	// ######################################################################## 
	// Constructors
	// ########################################################################
	
	/**
	 * Initializes a new instance of the ConfigFileReader class.
	 */
	public ReadCalibrationConfig(String configFile) {
		config = new ConfigFileReader();
		config.readConfigFile(new File(configFile));
	}	
	
	// ########################################################################
	// Methods/Functions	
	// ########################################################################	
	
	public int[] parseNameGetIndices(String ParameterName) {
		String[] tmpStr;
		int[] values;
		
		tmpStr = ParameterName.split("_");
		values = new int[tmpStr.length - 1];
		
		for(int i=1; i<tmpStr.length; i++) {
			values[i-1] = Integer.parseInt(tmpStr[i]);
		}		
		return values;
	}

	public String parseNameGetName(String ParameterName) {
		String[] tmpStr;

		tmpStr = ParameterName.split("_");
		return tmpStr[0];
	}
	
	public void printParameter(String ParameterName) {
		double[] input = config.getParameterDoubleArray(ParameterName);
		int[] indices;
		
		System.out.print(ParameterName + "\t");
		for(int i=0; i<input.length; i++) {
			System.out.print(input[i] + "\t");
		}
		indices = parseNameGetIndices(ParameterName);
		for(int i=0; i<indices.length; i++) {
			System.out.print(indices[i] + "\t");
		}		
		System.out.println();
	}

	public void printParameters() {
		double[] variables;
		int[] indices;
		
		Enumeration<?> e = config.getProperties().propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = config.getProperties().getProperty(key);
//			System.out.println(key + "\t" + value);
			
			//Print Name
			System.out.print(parseNameGetName(key) + "\t");
			// Print parameters
			variables = this.getParameterDoubleArray(value);
			for(int i=0; i<variables.length; i++) {
				System.out.print(variables[i] + "\t");				
			}
			// Print indices
			indices = parseNameGetIndices(key);
			for(int i=0; i<indices.length; i++) {
				System.out.print(indices[i] + "\t");
			}		
			System.out.println();		
		}		
	}
	
	public double[] getParameterDoubleArray(String Values) {
		String[] tmpStr;
		double[] tmpDouble;
		
		// Important: we use "," to divide columns and ";" to divide rows
		tmpStr = Values.split(",");
		tmpDouble = new double[tmpStr.length];
		// Transform to double		
		for(int i=0; i<tmpStr.length; i++) {
			tmpDouble[i] = Double.parseDouble(tmpStr[i]);
		}
		return tmpDouble;
	}

	// Transformation val * 1/step
	
	public static void main(String[] args) {
		ReadCalibrationConfig readCalibrationConfig = new ReadCalibrationConfig(FILENAME);
		
		System.out.println("Name\tMin\tMax\tStep\tIndex(ex)");
//		readCalibrationConfig.printParameter("Emotional_0");
//		readCalibrationConfig.printParameter("Involved_0");
//		readCalibrationConfig.printParameter("InitialPenetration_1_0");
//		readCalibrationConfig.printParameter("InitialPenetration_1_1");
//		readCalibrationConfig.printParameter("BrandAttribute_2_0_0");
//		readCalibrationConfig.printParameter("BrandAttribute_2_0_1");
//		readCalibrationConfig.printParameter("BrandAttribute_2_1_0");
//		readCalibrationConfig.printParameter("BrandAttribute_2_1_1");
		readCalibrationConfig.printParameters();
	}
}
