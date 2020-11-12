package test.junit;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.BooleanUtils;
import org.junit.Test;

import calibration.CalibrationTask;
import calibration.fitness.FitnessFunction;
import calibration.fitness.history.HistoryManager;
import calibration.fitness.history.SalesHistoryManager;
import model.ModelDefinition;
import model.ModelRunner;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics;
import util.statistics.Statistics.TimePeriod;

public class TestParallelizedResults {
	
	ModelDefinition dhtest;
	ModelDefinition initialPerceptions;
	
	double dhtestFitness;
	double initialPerceptionsFitness;
	
	CalibrationTask dhTask;
	CalibrationTask ipTask;
	
	StatisticsRecordingBean noRecording;
	
	HistoryManager manager;
	
	int[][] history;
	
	final Statistics.TimePeriod period = TimePeriod.QUARTERLY;
	
	public TestParallelizedResults() throws IOException, CalibrationException {
		dhtest = new ModelDefinition();
		dhtest.loadValuesFromFile(new File("./test/parallelized/DH_TEST.zio"));
		
		noRecording = StatisticsRecordingBean.noStatsBean();
		
		MonteCarloStatistics dhtestResults=ModelRunner.simulateModel(
				dhtest, 30, false, noRecording);
		
		history = CSVFileUtils.readRawHistoryFromCSV(
				"./test/parallelized/BaseLinePiggy_sales.csv");
		
		manager = new SalesHistoryManager(period, history, 0.5, 
				new FitnessFunction(), FitnessFunction.NO_HOLD_OUT);
		
		dhTask = new CalibrationTask(30, dhtest, manager, false, false);
		
		dhtestFitness = dhTask.getHistoryManager().computeTrainingScore(
				dhtestResults).finalScore;
		
		System.out.println("DH_TEST expected fitness: "+dhtestFitness);
		
		initialPerceptions = new ModelDefinition();
		initialPerceptions.loadValuesFromFile(
				new File("./test/parallelized/Model_with_initial_perceptions.zio"));
		MonteCarloStatistics initialPerceptionsResults=
				ModelRunner.simulateModel(initialPerceptions, 30, false, noRecording);
		
		ipTask = new CalibrationTask(30, initialPerceptions, manager, false, false);
		
		initialPerceptionsFitness = ipTask.getHistoryManager().computeTrainingScore(
				initialPerceptionsResults).finalScore;
		System.out.println("Model_with_initial_perceptions expected fitness: "+initialPerceptionsFitness);
	}
	
	@Test
	public void testUnchanged() throws CalibrationException {

		boolean[] failed = new boolean[2];
		
		for (int i=0; i<50; i++) {
			MonteCarloStatistics newdhresults = ModelRunner.simulateModel(dhtest, 30, 
					false, noRecording);
			double fitness = dhTask.getHistoryManager().computeTrainingScore(
					newdhresults).finalScore;
			if(dhtestFitness!=fitness) {
				failed[0] = true;
				System.out.println("Expecte fitnes:: "+dhtestFitness
						+" received::"+fitness+" at iteration "+i);
			} else {
				System.out.println(fitness);
			}
		}
		
		if(failed[0]) {
			System.out.println("Results found different from expected for DH_TEST model");
		}
		
		for (int i=0; i<50; i++) {
			MonteCarloStatistics newinitialPerceptionsResults = 
					ModelRunner.simulateModel(initialPerceptions, 30, false, noRecording);
			double fitness = ipTask.getHistoryManager().computeTrainingScore(
					newinitialPerceptionsResults).finalScore;
			if(initialPerceptionsFitness!=fitness) {
				failed[1] = true;
				System.out.println("Expecte fitnes:: "+initialPerceptionsFitness
						+" received::"+fitness+" at iteration "+i);
			} else {
				System.out.println(fitness);
			}
		}
		
		if(failed[1]) {
			System.out.println("Results found different from expected for Model_with_initial_perceptions model");
		}
		
		if(BooleanUtils.or(failed)) {
			fail();
		} else {
			System.out.println("Everything fine.");
		}
	}

	/**
	 * Main test intended for running outside of eclipse environment.
	 * @param args
	 */
	public static void main (String args[]) {
		try {
			TestParallelizedResults test = new TestParallelizedResults();
			test.testUnchanged();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CalibrationException e) {
			e.printStackTrace();
		}
	}
}
