package test.calibration;

import java.io.File;
import java.util.List;

import calibration.CalibrationConsole;
import calibration.CalibrationController;
import calibration.CalibrationParameter;
import calibration.CalibrationParametersManager;
import calibration.CalibrationTask;
import calibration.fitness.FitnessFunction;
import calibration.fitness.history.HistoryManager;
import calibration.fitness.history.SalesHistoryManager;
import model.ModelDefinition;
import model.ModelManager;
import util.io.CSVFileUtils;
import util.statistics.Statistics;

/**
 * Toy-like calibration console used for testing console-based 
 * calibration environment.
 * 
 * @author jbarranquero
 */
public class CalibrationConsoleTest {

	/**
	 * Launches calibration console with example values. The goal is to display 
	 * final "Done" message as a probe of no errors arising during test. 
	 * 
	 * @param args test main does not need any special program argument.
	 */
	public static void main(String[] args) {
		
		try {
			
			ModelDefinition modelDefinition = new ModelDefinition(); 
			modelDefinition.loadValuesFromFile(
					new File("./examples/example_calibration.zio")
			);
			
			CalibrationParametersManager paramManager = 
				new CalibrationParametersManager(
					"./examples/example_calibration_params1.clb"
			);
			
			int[][] salesHistory = CSVFileUtils.readRawHistoryFromCSV(
				"./examples/example_calibration_sales.csv"
			);
			
			HistoryManager manager = new SalesHistoryManager(
					Statistics.TimePeriod.WEEKLY, 
					salesHistory, CalibrationTask.ONLY_HISTORY_FITNESS, 
					new FitnessFunction(),
					FitnessFunction.NO_HOLD_OUT
					);

			CalibrationTask taskdef = new CalibrationTask(
				73553,	// calibration seed
				Long.toString(System.currentTimeMillis()),
				"./config/configGA.xml", "./log/",
				paramManager,
				1, // monte carlo iterations
				modelDefinition,
				manager,
//				Statistics.TimePeriod.WEEKLY,
//				salesHistory,
//				new FitnessFunctionRMSE(),
//				CalibrationTask.UNIFORM_BRAND_WEIGHTS,
//				CalibrationTask.ONLY_HISTORY_FITNESS,
				CalibrationTask.VALIDATE_MODEL
			);			
			
			// Store the parameters for including them in the optimization process
			List<CalibrationParameter> params = paramManager.getParameters();
			final int nParams = params.size();

			paramManager.setModelManager(new ModelManager(modelDefinition, 
				paramManager.getInvolvedDrivers()));
		
			double [] initialParams = new double[nParams];
			for (int i = 0; i < nParams; i++)
				initialParams[i] = paramManager.getParameterValue(i);
			
			new CalibrationController(taskdef, initialParams, 10).execute(
					CalibrationConsole.NO_MASTER_HOST, CalibrationConsole.NO_MASTER_PORT,
					modelDefinition.getNumberOfAgents());
			
			System.out.println("[JAVA/CalibrationConsoleTest] Done");
			
		} catch (Exception e) {
			System.err.print("[JAVA/CalibrationConsoleTest] ");
			e.printStackTrace();
		}  
	}
}
