package test;

import java.io.File;
import java.util.Enumeration;

import util.io.ConfigFileReader;

public class ReadExperimentConfig {
	
	// ########################################################################
	// Variables
	// ########################################################################
	
	private ConfigFileReader config; 
	private static String FILENAME = "./examples/ReadExperimentConfig.test";
	
	// ######################################################################## 
	// Constructors
	// ########################################################################
	
	/**
	 * Initializes a new instance of the ConfigFileReader class.
	 */
	public ReadExperimentConfig(String configFile) {
		config = new ConfigFileReader();
		config.readConfigFile(new File(configFile));
	}	
	
	// ########################################################################
	// Methods/Functions	
	// ########################################################################	
	
	// These are methods printing out Doubles and String arrays, 
	// but also the other methods can be used to obtain the values:
	// read one String - config.getParameterString(ParameterName)
	// read one double - config.getParameterDouble(ParameterName)
	// read one integer - config.getParameterInteger(ParameterName)

	public void printParameterInteger(String ParameterName) {
		// This is the method needed to read parameters
		int inputInteger = config.getParameterInteger(ParameterName);
		
		System.out.print(ParameterName + "\t");
		System.out.print(inputInteger + "\t");
		System.out.println();
	}

	public void printParameterString(String ParameterName) {
		// This is the method needed to read parameters
		String inputString = config.getParameterString(ParameterName);
		
		System.out.print(ParameterName + "\t");
		System.out.print(inputString + "\t");
		System.out.println();
	}
	
	public void printParameterDoubleArray(String ParameterName) {
		// This is the method needed to read parameters
		double[] inputDouble = config.getParameterDoubleArray(ParameterName);
		
		System.out.print(ParameterName + "\t");
		for(int i=0; i<inputDouble.length; i++) {
			System.out.print(inputDouble[i] + "\t");
		}	
		System.out.println();
	}
	
	public void printParameterIntegerArray(String ParameterName) {
		// This is the method needed to read parameters
		int[] inputInteger = config.getParameterIntegerArray(ParameterName);
		
		System.out.print(ParameterName + "\t");
		for(int i=0; i<inputInteger.length; i++) {
			System.out.print(inputInteger[i] + "\t");
		}	
		System.out.println();
	}	
	
	public void printParameterStringArray(String ParameterName) {
		// This is the method needed to read parameters		
		String[] inputString = config.getParameterStringArray(ParameterName);
		
		System.out.print(ParameterName + "\t");
		for(int i=0; i<inputString.length; i++) {
			System.out.print(inputString[i] + "\t");
		}	
		System.out.println();
	}	
	
	public void printParameters() {
		String[] variables;
		
		Enumeration<?> e = config.getProperties().propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = config.getProperties().getProperty(key);
//			System.out.println(key + "\t" + value);
			
			//Print Name
			System.out.print(key + "\t");
			// Print parameters
			variables = this.getParameterArray(value);
			for(int i=0; i<variables.length; i++) {
				System.out.print(variables[i] + "\t");		
			}		
			System.out.println();		
		}		
	}
	
	public String[] getParameterArray(String Values) {
		String[] tmpStr;
		
		// Important: we use "," to divide columns and ";" to divide rows
		tmpStr = Values.split(",");
		return tmpStr;
	}
	
	public static void main(String[] args) {
		ReadExperimentConfig readCalibrationConfig = new ReadExperimentConfig(FILENAME);

		readCalibrationConfig.printParameterString("calibrationMode");
		readCalibrationConfig.printParameterInteger("nrSteps");
		readCalibrationConfig.printParameterInteger("repetitions");
		readCalibrationConfig.printParameterString("algorithms");
		readCalibrationConfig.printParameterString("scenarios");
		readCalibrationConfig.printParameterString("tasks");	
//		readCalibrationConfig.printParameters();
	}
}
