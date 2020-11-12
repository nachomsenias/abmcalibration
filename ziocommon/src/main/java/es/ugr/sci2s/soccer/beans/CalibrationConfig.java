package es.ugr.sci2s.soccer.beans;

import calibration.fitness.AlternateFitnessFunction;
import calibration.fitness.FitnessFunction;
import calibration.fitness.history.HistoryManager;
import calibration.fitness.history.HistoryManagerFactory;
import util.StringBean;
import util.functions.MatrixFunctions;
import util.statistics.Statistics.TimePeriod;

public class CalibrationConfig {

	public final static int USE_BASE_VALUE = -1; 
	
	private String description = "";
	
	//KPI
	private int [][] targetSales;
	private int [][][] targetSalesBySegment;
	private String targetSalesPeriod;
	private double totalSalesWeight;
	
	private double [][] targetAwareness;
	private double [][][] targetAwarenessBySegment;
	private String targetAwarenessPeriod;
	
	private double [][] targetPerceptions;
	private double [][][] targetPerceptionsBySegment;
	private double [][][][] targetPerceptionsByDriver;
	private String targetPerceptionsPeriod;
	
	private double [][] targetWOMVolume;
	private String targetWOMVolumePeriod;
	
	private int targetEvaluations = 10000;
	private int calibrationAgents = USE_BASE_VALUE;
	
	private double holdOut = FitnessFunction.NO_HOLD_OUT;	
	
	//KPI weights
	
	private double salesWeight;
	private double awarenessWeight;
	private double perceptionsWeight;
	private double womVolumeWeight;
	
	//Model
	private SimulationConfig simConfig;
	
	private StringBean [] calibrationModelParameters;
	
	private int id;
	
	private String fitnessFunction = "MAE";
	
	private String algorithm = "configSSGA_HC.ecj";
	
	//Master - slave parameters.
	
	private String masterHost;
	
	private String masterPort;
	
	/*
	 * GETTERS & SETTERS
	 */
	
	public int[][] getTargetSales() {
		return targetSales;
	}

	public void setTargetSales(double[][] targetSales) {
		
		int [][] sales = new int [targetSales.length][];
		for (int b = 0; b<targetSales.length; b++) {
			for (int seg = 0; seg<targetSales[b].length; seg++) 
				sales[b][seg] = (int) targetSales[b][seg];
		}
		
		this.targetSales = sales;
	}
	
	public void setTargetSales(int[][] targetSales) {		
		this.targetSales = targetSales;
	}

	public int[][][] getTargetSalesBySegment() {
		return targetSalesBySegment;
	}

	public void setTargetSalesBySegment(int[][][] targetSalesBySegment) {
		this.targetSalesBySegment = targetSalesBySegment;
	}

	public SimulationConfig getSimConfig() {
		return simConfig;
	}

	public void setSimConfig(SimulationConfig simConfig) {
		this.simConfig = simConfig;
	}

	public StringBean[] getCalibrationModelParameters() {
		return calibrationModelParameters;
	}

	public void setCalibrationModelParameters(StringBean[] calibrationModelParameters) {
		this.calibrationModelParameters = calibrationModelParameters;
	}

	public String getSalesHistoryPeriod() {
		return targetSalesPeriod;
	}

	public void setHistorySalesPeriod(String historyPeriod) {
		this.targetSalesPeriod = historyPeriod;
	}

	public double getTotalSalesWeight() {
		return totalSalesWeight;
	}

	public void setTotalSalesWeight(double totalSalesWeight) {
		this.totalSalesWeight = totalSalesWeight;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMasterHost() {
		return masterHost;
	}

	public void setMasterHost(String masterHost) {
		this.masterHost = masterHost;
	}

	public String getMasterPort() {
		return masterPort;
	}

	public void setMasterPort(String masterPort) {
		this.masterPort = masterPort;
	}
	
	public String getTargetSalesPeriod() {
		return targetSalesPeriod;
	}

	public void setTargetSalesPeriod(String targetSalesPeriod) {
		this.targetSalesPeriod = targetSalesPeriod;
	}

	public double[][] getTargetAwareness() {
		return targetAwareness;
	}

	public void setTargetAwareness(double[][] targetAwareness) {
		this.targetAwareness = targetAwareness;
	}
	
	public double[][][] getTargetAwarenessBySegment() {
		return targetAwarenessBySegment;
	}

	public void setTargetAwarenessBySegment(double[][][] targetAwarenessBySegment) {
		this.targetAwarenessBySegment = targetAwarenessBySegment;
	}

	public String getTargetAwarenessPeriod() {
		return targetAwarenessPeriod;
	}

	public void setTargetAwarenessPeriod(String targetAwarenessPeriod) {
		this.targetAwarenessPeriod = targetAwarenessPeriod;
	}

	public double[][] getTargetPerceptions() {
		return targetPerceptions;
	}

	public void setTargetPerceptions(double[][] targetPerceptions) {
		this.targetPerceptions = targetPerceptions;
	}

	public double[][][] getTargetPerceptionsBySegment() {
		return targetPerceptionsBySegment;
	}

	public void setTargetPerceptionsBySegment(double[][][] targetPerceptionsBySegment) {
		this.targetPerceptionsBySegment = targetPerceptionsBySegment;
	}

	public double[][][][] getTargetPerceptionsByDriver() {
		return targetPerceptionsByDriver;
	}

	public void setTargetPerceptionsByDriver(double[][][][] targetPerceptionsByDriver) {
		this.targetPerceptionsByDriver = targetPerceptionsByDriver;
	}

	public String getTargetPerceptionsPeriod() {
		return targetPerceptionsPeriod;
	}

	public void setTargetPerceptionsPeriod(String targetPerceptionsPeriod) {
		this.targetPerceptionsPeriod = targetPerceptionsPeriod;
	}

	public double[][] getTargetWOMVolumen() {
		return targetWOMVolume;
	}

	public void setTargetWOMVolumen(double[][] targetWOMVolumen) {
		this.targetWOMVolume = targetWOMVolumen;
	}

	public String getTargetWOMVolumenPeriod() {
		return targetWOMVolumePeriod;
	}

	public void setTargetWOMVolumenPeriod(String targetWOMVolumenPeriod) {
		this.targetWOMVolumePeriod = targetWOMVolumenPeriod;
	}

	public int getTargetEvaluations() {
		return targetEvaluations;
	}

	public void setTargetEvaluations(int targetEvaluations) {
		this.targetEvaluations = targetEvaluations;
	}

	public int getCalibrationAgents() {
		return calibrationAgents;
	}

	public void setCalibrationAgents(int calibrationAgents) {
		this.calibrationAgents = calibrationAgents;
	}

	public double getHoldOut() {
		return holdOut;
	}

	public void setHoldOut(double holdOut) {
		this.holdOut = holdOut;
	}

	public double getSalesWeight() {
		return salesWeight;
	}

	public void setSalesWeight(double salesWeight) {
		this.salesWeight = salesWeight;
	}

	public double getAwarenessWeight() {
		return awarenessWeight;
	}

	public void setAwarenessWeight(double awarenessWeight) {
		this.awarenessWeight = awarenessWeight;
	}

	public double getPerceptionsWeight() {
		return perceptionsWeight;
	}

	public void setPerceptionsWeight(double perceptionsWeight) {
		this.perceptionsWeight = perceptionsWeight;
	}

	public double getWomVolumeWeight() {
		return womVolumeWeight;
	}

	public void setWomVolumeWeight(double womVolumeWeight) {
		this.womVolumeWeight = womVolumeWeight;
	}

	public String getFitnessFunction() {
		return fitnessFunction;
	}

	public void setFitnessFunction(String fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Checks brand and segment indexes.
	 */
	private void checkHistoryValues() {
		
		int segments = simConfig.getnSegments();
		int brands = simConfig.getnBrands();
		
		if(targetSalesBySegment!=null &&
			targetSalesBySegment.length==segments
					&& segments != targetSalesBySegment[0].length) {
			//If sales history is not following [brand][segment][step], transpose.
			
			int[][][] alternateHistory = new int [brands][segments][];
			
			for (int b=0; b<brands; b++) {
				for (int s=0; s<segments; s++) {
					alternateHistory[b][s] = targetSalesBySegment[s][b];
				}
			}
			
			targetSalesBySegment = alternateHistory;
		}
		
		if(targetAwarenessBySegment != null && 
				targetAwarenessBySegment.length==segments
					&& segments != targetAwarenessBySegment[0].length) {
			//If awareness history is not following [brand][segment][step], transpose.
			double[][][] awareness = new double [brands][segments][];
			
			for (int b=0; b<brands; b++) {
				for (int s=0; s<segments; s++) {
					awareness[b][s] = targetAwarenessBySegment[s][b];
				}
			}
			
			targetAwarenessBySegment = awareness;
		}
		
		if(targetPerceptionsBySegment!= null) {
			
			targetPerceptionsBySegment = MatrixFunctions.scaleCopyOfDouble3dMatrix(
					targetPerceptionsBySegment,10.0);
			
			if(targetPerceptionsBySegment.length==segments
					&& segments != targetPerceptionsBySegment[0].length) {
				//If perceptions history is not following [brand][segment][step], transpose.
				double[][][] perceptions = new double [brands][segments][];
				
				for (int b=0; b<brands; b++) {
					for (int s=0; s<segments; s++) {
						perceptions[b][s] = targetPerceptionsBySegment[s][b];
					}
				}
				targetPerceptionsBySegment = perceptions;
			}
			
		} else if (targetPerceptions!=null) {
			targetPerceptions = MatrixFunctions.scaleCopyOfDoubleMatrix(
					targetPerceptions,10.0);
		} else if (targetPerceptionsByDriver!=null) {
			targetPerceptionsByDriver = MatrixFunctions.scaleCopyOfDouble4dMatrix(
					targetPerceptionsByDriver,10.0);
		}
	}
	
	/**
	 * Creates a suitable history manager using the given configuration.
	 * @return a suitable history manager using the given configuration.
	 */
	public HistoryManager getHistoryManager() {
		
		//The string to enum is not tolerant to null values.
		TimePeriod salesPeriod = TimePeriod.WEEKLY;
		if(targetSalesPeriod!=null) {
			salesPeriod=TimePeriod.valueOf(targetSalesPeriod);
		}
		TimePeriod awarenessPeriod = TimePeriod.WEEKLY;
		if(targetAwarenessPeriod!=null) {
			awarenessPeriod=TimePeriod.valueOf(targetAwarenessPeriod);
		}
		TimePeriod perceptionsPeriod = TimePeriod.WEEKLY;
		if(targetPerceptionsPeriod!=null) {
			perceptionsPeriod=TimePeriod.valueOf(targetPerceptionsPeriod);
		}
		TimePeriod womVolumePeriod = TimePeriod.WEEKLY;
		if(targetWOMVolume!=null) {
			womVolumePeriod=TimePeriod.valueOf(targetWOMVolumePeriod);
		}
		
		checkHistoryValues();
		
		return HistoryManagerFactory.getNewManager(
				salesPeriod, 
				awarenessPeriod, 
				perceptionsPeriod, 
				womVolumePeriod,
				targetSales, targetSalesBySegment, 
				targetAwareness, targetAwarenessBySegment, 
				targetPerceptions, targetPerceptionsBySegment, 
				targetPerceptionsByDriver,
				targetWOMVolume,
				totalSalesWeight, salesWeight, awarenessWeight, 
					perceptionsWeight, womVolumeWeight, fitnessFunction, holdOut);
	}
	
public HistoryManager getAlternateHistoryManager() {
		
		//The string to enum is not tolerant to null values.
		TimePeriod salesPeriod = TimePeriod.WEEKLY;
		if(targetSalesPeriod!=null) {
			salesPeriod=TimePeriod.valueOf(targetSalesPeriod);
		}
		TimePeriod awarenessPeriod = TimePeriod.WEEKLY;
		if(targetAwarenessPeriod!=null) {
			awarenessPeriod=TimePeriod.valueOf(targetAwarenessPeriod);
		}
		TimePeriod perceptionsPeriod = TimePeriod.WEEKLY;
		if(targetPerceptionsPeriod!=null) {
			perceptionsPeriod=TimePeriod.valueOf(targetPerceptionsPeriod);
		}
		TimePeriod womVolumePeriod = TimePeriod.WEEKLY;
		if(targetWOMVolume!=null) {
			womVolumePeriod=TimePeriod.valueOf(targetWOMVolumePeriod);
		}
		
		checkHistoryValues();
		
		double threshold = AlternateFitnessFunction.DEFAULT_THRESHOLD;
		
		return HistoryManagerFactory.getAlternateManager(
				salesPeriod, 
				awarenessPeriod, 
				perceptionsPeriod, 
				womVolumePeriod,
				targetSales, targetSalesBySegment, 
				targetAwareness, targetAwarenessBySegment, 
				targetPerceptions, targetPerceptionsBySegment, 
				targetPerceptionsByDriver,
				targetWOMVolume,
				totalSalesWeight, salesWeight, awarenessWeight, 
					perceptionsWeight, womVolumeWeight, fitnessFunction, holdOut, threshold);
	}
}
