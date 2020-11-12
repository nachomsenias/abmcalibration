package test.calibration;

import java.io.File;
import java.util.List;

import calibration.CalibrationParameter;
import calibration.CalibrationParametersManager;
import model.ModelDefinition;
import model.ModelManager;
import model.ModelRunner;
import util.exception.calibration.CalibrationException;
import util.exception.sales.SalesScheduleError;
import util.exception.simulation.SimulationException;
import util.exception.view.TerminationException;
import util.functions.MatrixFunctions;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;

/**
 * Test calibration controller used for environment test.
 * 
 * @author imoya
 *
 */
public class CalibrationControllerTest {
	
	/**
	 * Launches an example test.
	 * 
	 * @param args no special argument is needed for this text.
	 */
	public static void main(String[] args) {
		
		try {
			
			new CalibrationControllerTest(
				new CalibrationParametersManager(
					"./examples/example_calibration_params1.clb"
				)
			).executeTest();
			
		} catch (Exception e) {
			System.err.print("[JAVA/CalibrationControllerTest] ");
			e.printStackTrace();
		}  
	}

	/**
	 * Parameter manager instance for this test.
	 */
	private CalibrationParametersManager paramManager;

	/**
	 * Creates an using this manager.
	 * 
	 * @param pm given parameter manager for test purposes.
	 */
	public CalibrationControllerTest(CalibrationParametersManager pm) {
		this.paramManager = pm;
	}
	
	/**
	 * Runs the test.
	 * 
	 * @throws CalibrationException if JNI exceptions arise or final result 
	 * points to null.
	 */
	public void executeTest() throws CalibrationException {
		
		// Create native type arguments
		List<CalibrationParameter> params = paramManager.getParameters();
		int n = params.size();
		double[] mins = new double[n];
		double[] maxs = new double[n];
		for (int i = 0; i < n; i++) {
			mins[i] = params.get(i).minValue;
			maxs[i] = params.get(i).maxValue;
		}
		String names = params.get(0).signature;
		for (int i = 1; i < n; i++) {
			names += ";" + params.get(i).signature;
		}
		
		// Run!!!
		int[] result;
		try {
			result = new JNIWrapperTest(this).testRun(
				"./config.xml", "./log/", names, mins, maxs
			);
		} catch (TerminationException e) { throw new CalibrationException(e); }

		// Check result
		if (result == null) throw new CalibrationException(
			"Null result from JNIWrapperTest"
		);
		
		// Log
		System.out.print("[JAVA/CalibrationControllerTest] ");
		System.out.print("Calibrated parameters: { " + result[0]);
		for (int i = 1; i < result.length; i++) {
			System.out.print(", " + result[i]);
		}
		System.out.println(" }");
	}

	/**
	 * Example callback for testing JNI communications.
	 * 
	 * @param parameters parameter values for this fitness example 
	 * calculations.
	 * @return test fitness value.
	 * @throws CalibrationException if problems are detected during model 
	 * update.
	 * @throws SalesScheduleError if problems arise during model instance 
	 * simulation.
	 */
	public double testCallback(int[] parameters) throws CalibrationException, SimulationException {
		
		System.out.print("[JAVA/CalibrationControllerTest] ");
		System.out.print("Computing fitness for parameters { ");
		System.out.print(parameters[0]);
		for (int i = 1; i < parameters.length; i++) {
			System.out.print(", " + parameters[i]);
		}
		System.out.println(" }");
		
		// Create model
		System.out.println("[JAVA/CalibrationControllerTest] Creating model manager...");
		
		ModelDefinition md = new ModelDefinition();
		md.loadValuesFromFile(new File("./examples/example_calibration.zio"));
		
		MonteCarloStatistics results=ModelRunner.simulateModel(
				md, 1, false,
					StatisticsRecordingBean.noStatsBean());
	
		ModelManager modelManager = new ModelManager(md);
		
		System.out.println("[JAVA/CalibrationControllerTest] Model created and simulated");
		
		// Update parameters
		System.out.println("[JAVA/CalibrationControllerTest] Updating model...");
		paramManager.setModelManager(modelManager);
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] != -1) {
				paramManager.setParameterValue(i, parameters[i]);
			} else {
				System.out.println("[JAVA/CalibrationControllerTest] Ignoring parameter " + i);
			}
		}
		System.out.println("[JAVA/CalibrationControllerTest] Model updated");

		// Compute fitness
		System.out.println("[JAVA/CalibrationControllerTest] Total sales by brand: ");
		
		int[][] totalSales =
			results.getStatistics()[0]
				.computeScaledSalesByBrandBySegment();
		
		double fitness = MatrixFunctions.addMatrix(totalSales);
		System.out.print(fitness);
		
		return fitness;	
	}
}
