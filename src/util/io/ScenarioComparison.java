package util.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import model.ModelDefinition;
import model.ModelRunner;
import util.exception.simulation.SimulationException;
import util.functions.ArrayFunctions;
import util.functions.Functions;
import util.functions.MatrixFunctions;
import util.io.functions.AwarenessUtils;
import util.io.functions.PerceptionsUtils;
import util.io.functions.SalesUtils;
import util.io.functions.TPContributionUtils;
import util.io.functions.WomContributionUtils;
import util.io.functions.WomReachUtils;
import util.io.functions.WomSentimentUtils;
import util.io.functions.WomVolumenByAttUtils;
import util.io.functions.WomVolumenByBrandUtils;
import util.statistics.MonteCarloStatistics;

/**
 * Contains the functionality for the simulation of complex scenarios. In 
 * these scenarios, a target base model is iteratively modified or compared.
 * 
 * @author imoya
 *
 */
public class ScenarioComparison {
	
	/*
	 * Constant flags.
	 */
	
	public static final boolean FROM_GUI = true;
	public static final boolean FROM_CONSOLE = false;
	
	public static final boolean ENABLE_REPORTS = true;
	public static final boolean NO_REPORTS = false;
	
	//Running flag for stopping executions.
	private boolean stop = false;
	
	/**
	 * Runs a sensitivity analysis scenario using both target brand and touch
	 * point.
	 * @param touchpoint the target touch point for the sensitivity analysis.
	 * @param brand the target brand for the sensitivity analysis.
	 * @param min the minimum investment percentage (using 0 as 0% and 1 as 
	 * 100%). This value can be greater than 1 and has no limit.
	 * @param max the maximum investment percentage (using 0 as 0% and 1 as 
	 * 100%). This value can be greater than 1 and has no limit.
	 * @param step the percentage increment for each step using a 0 to 1 scale
	 * (100% would be 1).
	 * @param numMC the number of Monte-Carlo iterations.
	 * @param md the given model for the sensitivity analysis scenario.
	 * @param book the name of the xls file.
	 * @param scenarioName the base name tag for this scenario.
	 * @param fromGUIflag if true, displays the simulation feed using the GUI 
	 * console.
	 * @param exportReports if true, exports the model given model definition 
	 * object.
	 * @param reportSetup the report configuration (sales, awareness, etc).
	 * @return the resulting bean containing the sensitivity analysis results.
	 * @throws SimulationException exceptions are thrown if the simulation
	 * crashes.
	 */
	public ComparisonResult runSA(int touchpoint, int brand, double min, 
			double max, double step, int numMC, ModelDefinition md, String book, 
				String scenarioName, boolean fromGUIflag, boolean exportReports,
					StatisticsRecordingBean reportSetup
		) throws SimulationException {
		
		if(min>max) {
			throw new IllegalArgumentException("Min value should be greater "
					+ "than max value.");
		}
		
		double[][][][] grp=md.getGRP();
		
		double[][] originalPlan = MatrixFunctions.copyMatrix(grp[touchpoint][brand]);
		
		double factor = min;
		
		int numScenarios = (int)(Math.round((max-min)/step)) + 1;
		int[][] salesByScenarioMC = new int [numScenarios][numMC];
		
		MonteCarloStatistics[] mcStats = new MonteCarloStatistics [numScenarios];
		int scenarioCount = 0;
		String[] scenarioNames = new String [numScenarios];
		
		while((factor<max || Functions.equals(factor, max, 
				Functions.DOUBLE_EQUALS_DELTA)) && !stop) {
			double[][] adjustedGRP = 
					MatrixFunctions.scaleCopyOfDoubleMatrix(originalPlan, factor);
			grp[touchpoint][brand]=adjustedGRP;
			md.setGRP(grp);

			String scenarioDef = scenarioName+"_"+String.valueOf((int)(factor*100.0));
			
			scenarioNames[scenarioCount]=scenarioDef;
			
			mcStats[scenarioCount]
					=runAndExportModel(numMC, md, book+scenarioDef+".zio", 
							fromGUIflag, exportReports, reportSetup);
			salesByScenarioMC[scenarioCount] = 
					getScalesTotalSalesByMcForBrand(mcStats[scenarioCount], brand);

			//Next step
			factor+=step;
			scenarioCount++;
		}
		
		//Write XLSX
		if(exportReports) {
			generateReports(md, mcStats, scenarioNames, book, reportSetup);
			
			System.out.println("Sensitivity analisys performed succesfully "
					+ "for scenario "+scenarioName);
		}
		return new ComparisonResult(salesByScenarioMC,scenarioNames,mcStats);
	}
	
	/**
	 * Modifies and returns the GRP matrix setting the investment for target 
	 * brand to given base/initial value.
	 * @param initial the base/initial investment for the scenario.
	 * @param brand the target brand.
	 * @return the GRP matrix modified with the investment for target brand to 
	 * given base/initial value.
	 */
	private double[][][][] getBaseGRP(double[][][][] initial, int brand) {
		for (int tp=0; tp<initial.length; tp++) {
			ArrayFunctions.fillArray(initial[tp][brand], 0.0);
		}
		return initial;
	}
	
	/**
	 * Executes a touch point contribution scenario for given model using target
	 * brand id.
	 * @param brand the id for the target brand.
	 * @param numMC the number of Monte-Carlo iterations.
	 * @param md the given model for the touch point contribution scenario.
	 * @param book the name of the xls file.
	 * @param brandName
	 * @return the resulting bean containing the touch point contribution 
	 * results.
	 * @throws SimulationException exceptions are thrown if the simulation
	 * crashes.
	 */
	public ComparisonResult runContribution(int brand, int numMC, 
			ModelDefinition md, String book, String brandName, boolean fromGUIflag,
			boolean exportReports, StatisticsRecordingBean reportSetup
				) throws SimulationException {
		
		double[][][][] original=md.getGRP();
		double[][][][] empty=getBaseGRP(MatrixFunctions.copyMatrix(original), brand);
		
		int numTouchPoints = md.getNumberOfTouchPoints();
		
		int numScenarios = numTouchPoints+1;
		int[][] salesByScenarioMC = new int [numScenarios][numMC];
		
		MonteCarloStatistics[] mcStats = new MonteCarloStatistics [numScenarios];
		String[] scenarioNames = new String [numScenarios];
		String[] touchPointNames = md.getTouchPointNames();
		
		md.setGRP(empty);
		
		//Base GRP (No GRP for any touchpoint)
		String scenarioDef = brandName+"_BASE";		
		scenarioNames[0]=scenarioDef;

		mcStats[0]=runAndExportModel(numMC, md, book+scenarioDef+".zio", 
				fromGUIflag, exportReports, reportSetup);
		salesByScenarioMC[0]=getScalesTotalSalesByMcForBrand(mcStats[0], brand);
		
		int touchpoint = 0;
		while(touchpoint<numTouchPoints && !stop) {
			empty[touchpoint][brand] = original[touchpoint][brand];
			if(exportReports) {
				scenarioDef = brandName+"_"+touchPointNames[touchpoint];
				scenarioNames[touchpoint+1]=scenarioDef;
			}

			mcStats[touchpoint+1]=runAndExportModel(numMC, md, 
					book+scenarioDef+".zio", fromGUIflag, 
						exportReports, reportSetup);
			
			salesByScenarioMC[touchpoint+1]
					=getScalesTotalSalesByMcForBrand(mcStats[touchpoint+1], brand);
		
			ArrayFunctions.fillArray(empty[touchpoint][brand], 0.0);
			
			touchpoint++;
		}
		
		//Write XLSX
		if(exportReports) {
			generateReports(md, mcStats, scenarioNames, book, reportSetup);
			
			System.out.println("Touch Point contribution performed succesfully "
					+ "for "+brandName);
		}
		return new ComparisonResult(salesByScenarioMC,scenarioNames,mcStats);
	}
	
	/**
	 * Executes a model comparison returning the resulting statistics.
	 * 
	 * @param modelsToBeCompared a Map object containing the name of the 
	 * scenario and the zio file containing the model parameter values.
	 * @param numMC the number of Monte-Carlo iterations.
	 * @param book the name of the xls file.
	 * @param fromGUIflag if true, displayis the simulation feed using the GUI 
	 * console.
	 * @param exportReports if true, exports the simulation results into the
	 * given xls book file.
	 * @param reportSetup the report configuration (sales, awareness, etc).
	 * @throws SimulationException exceptions are thrown if the simulation
	 * crashes.
	 */
	public void runComparison(
			Map<String,File> modelsToBeCompared, int numMC, String book, 
			boolean fromGUIflag, boolean exportReports, 
				StatisticsRecordingBean reportSetup
		) throws SimulationException {

		int numScenarios = modelsToBeCompared.size();
		
		MonteCarloStatistics[] mcStats = new MonteCarloStatistics [numScenarios];
		String[] scenarioNames = new String [numScenarios];

		Iterator<String> scenarioIterator = modelsToBeCompared.keySet().iterator();
		int scenarioCounter = 0;
		ModelDefinition md = null;
		
		while(scenarioIterator.hasNext() && !stop) {
			String scenarioDef = scenarioIterator.next();
			scenarioNames[scenarioCounter]=scenarioDef;

			md = new ModelDefinition();
			md.loadValuesFromFile(modelsToBeCompared.get(scenarioDef));
			
			mcStats[scenarioCounter]=runAndExportModel(numMC, md, 
					book+scenarioDef, fromGUIflag, 
						exportReports, reportSetup);
			scenarioCounter++;
		}

		//Write XLSX
		if(exportReports) {
			generateReports(md, mcStats, scenarioNames, book, reportSetup);
		}
	}
	
	/**
	 * Executes a model comparison returning the resulting statistics.
	 * 
	 * @param mds the model definition objects to be compared.
	 * @param numMC the number of Monte-Carlo iterations.
	 * @param book the name of the xls file to export the simulation results.
	 * @param fromGUIflag if true, the simulation feed is shown in the GUI 
	 * console.
	 * @param exportModel if true, exports the model given model definition 
	 * object.
	 * @param reportSetup the report configuration (sales, awareness, etc).
	 * @return returns an statistics array containing the simulation results. 
	 * The array indexes corresponds with the input model array.
	 * @throws SimulationException exceptions are thrown if the simulation
	 * crashes.
	 */
	public MonteCarloStatistics[] runComparison(
			ModelDefinition[] mds, int numMC, String book, 
			boolean fromGUIflag, boolean exportModel, 
				StatisticsRecordingBean reportSetup
		) throws SimulationException {

		int numModels = mds.length;
		MonteCarloStatistics[] mcStats = new MonteCarloStatistics [numModels];

		int iteration = 0;
		while(iteration<numModels && !stop) {
			ModelDefinition md = mds[iteration];
			mcStats[iteration] = runAndExportModel(numMC, md, 
					book, fromGUIflag, 
					exportModel, reportSetup);
			iteration++;
		}
		
		return mcStats;
	}
	
	/**
	 * Generates the xls files containing the reports.
	 * 
	 * @param md the simulated model definition.
	 * @param mcStats the statistics resulting from the simulation.
	 * @param scenarioNames the names of the simulated scenarios.
	 * @param book the name of the xls file.
	 * @param reportSetup the report configuration (sales, awareness, etc).
	 */
	private void generateReports(
			ModelDefinition md, MonteCarloStatistics[] mcStats, 
			String[] scenarioNames, String book, StatisticsRecordingBean reportSetup
		) {
		if (reportSetup.exportSales) {
			XLSXFileUtils.writeReport("Sales", md, mcStats, 
					new SalesUtils(), scenarioNames, book);
		}
		
		if (reportSetup.exportAwareness) {
			XLSXFileUtils.writeReport("Awareness", md, mcStats, 
					new AwarenessUtils(), scenarioNames, book);
		}
		
		if(reportSetup.exportPerceptions) {
			XLSXFileUtils.writeReport("Perceptions", md, mcStats, 
					new PerceptionsUtils(), scenarioNames, book);
		}
		
		if(reportSetup.exportTouchPointContributions) {
			XLSXFileUtils.writeReport("TouchPoint Perception Contributions", 
					md, mcStats, new TPContributionUtils(), scenarioNames, book);
		}
		
		if(reportSetup.exportWomReach) {
			XLSXFileUtils.writeReport("WoM Reach evolution", md, mcStats, 
					new WomReachUtils(), scenarioNames, book);
		}
		
		if(reportSetup.exportWomVolumen) {
			XLSXFileUtils.writeReport("WoM Volumen by brand evolution", md, 
					mcStats, new WomVolumenByBrandUtils(), scenarioNames, book);
			XLSXFileUtils.writeReport("WoM Volumen by attribute evolution", md, 
					mcStats, new WomVolumenByAttUtils(), scenarioNames, book);
		}
		
		if(reportSetup.exportWomSentiment) {
			XLSXFileUtils.writeReport("WoM Sentiment evolution", md, mcStats, 
					new WomSentimentUtils(), scenarioNames, book);
		}
		
		if(reportSetup.exportWomContributions) {
			XLSXFileUtils.writeReport("Word of Mouth Contributions", md, mcStats, 
					new WomContributionUtils(), scenarioNames, book);
		}
	}
	
	/**
	 * Simulates the given model definition, exporting its values into a zio
	 * file. In addition, the statistics for this simulation are returned.
	 * @param numMC number of Monte-Carlo iterations.
	 * @param md the model definition object.
	 * @param fileName the name where the model is exported.
	 * @param fromGUI if true, writes the output in the GUI console.
	 * @param exportModel if true, exports the model as a zio file.
	 * @param reportSetup report and statistic configuration.
	 * @return the statistics for this simulation using given model and report 
	 * configuration.
	 * @throws SimulationException exceptions are thrown if the simulation
	 * crashes.
	 */
	private MonteCarloStatistics runAndExportModel(int numMC, 
			ModelDefinition md, String fileName, boolean fromGUI, 
			boolean exportModel, StatisticsRecordingBean reportSetup) 
					throws SimulationException {
		
		//Simulation results
		MonteCarloStatistics mcStats = ModelRunner.simulateModel(md, numMC, fromGUI, reportSetup);
		
		if(exportModel) {
			try {
				FileWriter fw=new FileWriter(fileName);
				fw.write(md.export());
				fw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		return mcStats;
	}
	
	private int[] getScalesTotalSalesByMcForBrand(MonteCarloStatistics mcStats, int brand) {
		int numMC = mcStats.getNumberOfMonteCarloRepetitions();
		int[] sales = new int [numMC];
		
		int[][][] baseSales = mcStats.computeScaledSalesByBrandBySegment();
		
		for (int mc=0; mc<numMC; mc++) {
			sales[mc] = ArrayFunctions.addArray(baseSales[mc][brand]);
		}
		
		return sales;
	}
	
	/**
	 * Cancels current execution.
	 */
	public void stop() {
		stop = true;
	}
}
