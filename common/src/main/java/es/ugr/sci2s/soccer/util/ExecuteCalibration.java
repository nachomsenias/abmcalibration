package es.ugr.sci2s.soccer.util;

import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;
import es.ugr.sci2s.soccer.workers.ExperimentWorker;

import calibration.fitness.FitnessFunction;
import util.io.CSVFileUtils;

public class ExecuteCalibration {

	public static class Options {
		
		public static final boolean INTEGER_CODING_SCHEME=false;
		public static final boolean REAL_CODING_SCHEME=true;
		
		// Monte-Carlo iterations
		public final static String REPEATED_ITERATIONS = "--repeat";
		public int repeatedIterations = 1;
		public final static String START_ITERATIONS = "--start";
		public int repeatedIterationsStart = 0;
		public final static String SINGLE_ITERATION = "--single";
		public final static String NUM_EVALUATIONS = "--evaluations";
		public int evaluations = -1;

		// Real coding optimization
		public final static String REAL_CODING = "--real-coding";
		public boolean realCoding = false;
		public final static String ALGORITHM = "--algorithm";
		public String algorithm = null;
		
		//Hold Out
		public final static String HOLD_OUT = "--hold-out";
		public double holdOut=FitnessFunction.NO_HOLD_OUT;
		
		//Hold Out
		public final static String ALTERNATE = "--alternate";
		public boolean alternate=false;
		
		private static int parseIntOption(String args) {
			String[] splitted = args.split("=");
			if (splitted.length == 2) {
				return Integer.parseInt(splitted[1]);
			} else {
				throw new IllegalArgumentException(args + " is not a valid option.");
			}
		}
		
		private static double parseDoubleOption(String args) {
			String[] splitted = args.split("=");
			if (splitted.length == 2) {
				return Double.parseDouble(splitted[1]);
			} else {
				throw new IllegalArgumentException(args + " is not a valid option.");
			}
		}

		private static String parseStringOption(String args) {
			String[] splitted = args.split("=");
			if (splitted.length == 2) {
				return splitted[1];
			} else {
				throw new IllegalArgumentException(args + " is not a valid option.");
			}
		}

		public static Options parseOptions(String[] args) {
			Options customOptions = new ExecuteCalibration.Options();

			for (int i = 2; i < args.length; i++) {
				if (args[i].contains(Options.REPEATED_ITERATIONS)) {
					customOptions.repeatedIterations = parseIntOption(args[i]);
				} else if (args[i].contains(Options.START_ITERATIONS)) {
					customOptions.repeatedIterationsStart = parseIntOption(args[i]);
				} else if (args[i].contains(Options.SINGLE_ITERATION)) {
					customOptions.repeatedIterationsStart = parseIntOption(args[i]);
					customOptions.repeatedIterations = 1;
				} else if (args[i].contains(Options.REAL_CODING)) {
					customOptions.realCoding = true;
				} else if (args[i].contains(Options.ALGORITHM)) {
					customOptions.algorithm = parseStringOption(args[i]);
					if(customOptions.algorithm.contains("RealCoding")) {
						customOptions.realCoding = true;
					}
				} else if (args[i].contains(Options.NUM_EVALUATIONS)) {
					customOptions.evaluations = parseIntOption(args[i]);
				} else if (args[i].contains(Options.HOLD_OUT)) {
					customOptions.holdOut = parseDoubleOption(args[i]);
				} else if (args[i].contains(Options.ALTERNATE)) {
					customOptions.alternate = true;
				}
			}

			return customOptions;
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println(
					"Usage: input_json output_folder [options: <--algorithm=algorithm-file> "
					+ "<--real-coding> {<--repeate=n> <--start=n0> | <--single=t> }");
			System.exit(1);
		}

		try {
			String config = CSVFileUtils.readFile(args[0]);

			Gson gson = new Gson();

			// Read configuration from JSON
			CalibrationConfig calibrationConfig = gson.fromJson(config,
					CalibrationConfig.class);

			// Options
			Options customOptions = Options.parseOptions(args);

			String name = new File(args[0]).getName();

			CalibrationResponse calibrationResponse = new CalibrationResponse();

			if (customOptions.algorithm != null) {
				calibrationConfig.setAlgorithm(customOptions.algorithm);
			}
			
			String folder;
			
			if(customOptions.realCoding) {
				folder = args[1]+"-real/";
			} else {
				folder = args[1]+"/";
			}

			ExperimentWorker worker = new ExperimentWorker(calibrationConfig,
					calibrationResponse, name, customOptions.repeatedIterations,
						customOptions.repeatedIterationsStart, customOptions.evaluations, 
							customOptions.realCoding, folder);

			worker.run();

			CalibrationResponse[] responses = worker.getResponses();

			for (int i = 0; i < (customOptions.repeatedIterations); i++) {
				String models = folder + "resulting_model_"+worker.getSignature()+"_it"
						+ String.valueOf(customOptions.repeatedIterationsStart + i)
						+ ".json";
				CSVFileUtils.writeFile(models,
						gson.toJson(responses[i], CalibrationResponse.class));

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
