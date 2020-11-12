package test;

import java.io.File;

import model.ModelDefinition;
import util.exception.simulation.SimulationException;
import util.io.ScenarioComparison;
import util.io.StatisticsRecordingBean;

public class ScenarioComparisonTest {

	static final StatisticsRecordingBean reportSetup = 
			new StatisticsRecordingBean(true, true, true, true, true, 
					true, true, true, true);
	
	public final static void runTest1() {
		
		ModelDefinition md = new ModelDefinition();
		String mdPath = "/home/ECSC/imoya/anotherabm/DH_TEST.zio";
		md.loadValuesFromFile(new File(mdPath));
		
		try {
			new ScenarioComparison().runSA(0, 0, 0.8, 1.1, 0.1, 3, md,
					"/home/ECSC/imoya/anotherabm/", "Test", 
						ScenarioComparison.FROM_CONSOLE, 
							ScenarioComparison.ENABLE_REPORTS, reportSetup);
		} catch (SimulationException e) {
			e.printStackTrace();
		}
	}
	
	public final static void runTest2() {
		
		ModelDefinition md = new ModelDefinition();
		String mdPath = "/home/ECSC/imoya/anotherabm/DH_TEST.zio";
		md.loadValuesFromFile(new File(mdPath));
		
		try {
			new ScenarioComparison().runContribution(0, 3, md, 
					"/home/ECSC/imoya/anotherabm/", "Test", 
						ScenarioComparison.FROM_CONSOLE, 
						ScenarioComparison.ENABLE_REPORTS, 
							reportSetup);
		} catch (SimulationException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		runTest1();
		runTest2();
	}
}
