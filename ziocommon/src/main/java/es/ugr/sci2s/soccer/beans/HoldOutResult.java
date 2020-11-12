package es.ugr.sci2s.soccer.beans;

public class HoldOutResult {
	public SimulationResult trainingResult;
	public SimulationResult testResult;

	public HoldOutResult() {
		trainingResult = new SimulationResult();
		testResult = new SimulationResult();
	}
	
	public SimulationResult getTrainingResult() {
		return trainingResult;
	}
	public void setTrainingResult(SimulationResult trainingResult) {
		this.trainingResult = trainingResult;
	}
	public SimulationResult getHoldOutResult() {
		return testResult;
	}
	public void setHoldOutResult(SimulationResult holdOutResult) {
		this.testResult = holdOutResult;
	}
	
	private static void splitSales(HoldOutResult holdOutResult, 
			SimulationResult result, int holdOutSteps) {
		double[][][] salesByBrandBySegmentByStepAvg = result.salesByBrandBySegmentByStepAvg;
		
		int brands = salesByBrandBySegmentByStepAvg.length;
		int segments = salesByBrandBySegmentByStepAvg[0].length;
		int steps = salesByBrandBySegmentByStepAvg[0][0].length;

		int trainingSteps = steps - holdOutSteps;
		
		double[][][] salesByBrandBySegmentByStepAvgTraining = 
				new double [brands][segments][trainingSteps];
		double[][][] salesByBrandBySegmentByStepAvgHoldOut = 
				new double [brands][segments][holdOutSteps];
		
		double[][] salesByBrandBySegmentAvgTraining = 
				new double [brands][segments];
		double[][] salesByBrandBySegmentAvgHoldOut = 
				new double [brands][segments];
		
		for (int b = 0; b < brands; b++) {
			for (int seg = 0; seg < segments; seg++) {
				for (int it = 0; it<trainingSteps; it++) {
					double value = salesByBrandBySegmentByStepAvg[b][seg][it];
					
					salesByBrandBySegmentByStepAvgTraining[b][seg][it] = value;
					salesByBrandBySegmentAvgTraining[b][seg] += value;
				}
				for (int ih = 0; ih<holdOutSteps; ih++) {
					double value = salesByBrandBySegmentByStepAvg[b][seg][ih+trainingSteps];
					
					salesByBrandBySegmentByStepAvgHoldOut[b][seg][ih] = value;
					salesByBrandBySegmentAvgHoldOut[b][seg] += value;
				}
			}
		}
		
		holdOutResult.trainingResult.salesByBrandBySegmentByStepAvg = 
				salesByBrandBySegmentByStepAvgTraining;
		holdOutResult.trainingResult.salesByBrandBySegmentAvg = 
				salesByBrandBySegmentAvgTraining;
		
		holdOutResult.testResult.salesByBrandBySegmentByStepAvg = 
				salesByBrandBySegmentByStepAvgHoldOut;
		holdOutResult.testResult.salesByBrandBySegmentAvg = 
				salesByBrandBySegmentAvgHoldOut;
	}
	
	private static void splitAwareness(HoldOutResult holdOutResult, 
			SimulationResult result, int holdOutSteps) {
		double[][][] awarenessByBrandBySegByStepAvg = result.awarenessByBrandBySegByStepAvg;
		
		int brands = awarenessByBrandBySegByStepAvg.length;
		int segments = awarenessByBrandBySegByStepAvg[0].length;
		int steps = awarenessByBrandBySegByStepAvg[0][0].length;
		
		
		int trainingSteps = steps - holdOutSteps;
		
		double[][][] awarenessByBrandBySegByStepAvgTraining = 
				new double [brands][segments][trainingSteps];
		
		double[][][] awarenessByBrandBySegByStepAvgHoldOut = 
				new double [brands][segments][holdOutSteps];
		
		for (int b = 0; b < brands; b++) {
			for (int seg = 0; seg < segments; seg++) {
				for (int it = 0; it<trainingSteps; it++) {
					awarenessByBrandBySegByStepAvgTraining[b][seg][it] = 
							awarenessByBrandBySegByStepAvg[b][seg][it];

				}
				for (int ih = 0; ih<holdOutSteps; ih++) {
					
					awarenessByBrandBySegByStepAvgHoldOut[b][seg][ih] = 
							awarenessByBrandBySegByStepAvg[b][seg][ih+trainingSteps];
				}
			}
		}
		
		holdOutResult.trainingResult.awarenessByBrandBySegByStepAvg = 
				awarenessByBrandBySegByStepAvgTraining;
		
		holdOutResult.testResult.awarenessByBrandBySegByStepAvg = 
				awarenessByBrandBySegByStepAvgHoldOut;
	}
	
	private static void splitPerceptions(HoldOutResult holdOutResult, 
			SimulationResult result, int holdOutSteps) {
		
		double[][][][] perceptionsByDriverByBrandBySegmentByStepAvg = 
				result.perceptionsByDriverByBrandBySegmentByStepAvg;
		
		int atts = perceptionsByDriverByBrandBySegmentByStepAvg.length;
		int brands = perceptionsByDriverByBrandBySegmentByStepAvg[0].length;
		int segments = perceptionsByDriverByBrandBySegmentByStepAvg[0][0].length;
		int steps = perceptionsByDriverByBrandBySegmentByStepAvg[0][0][0].length;
		
		int trainingSteps = steps - holdOutSteps;
		
		double[][][][] perceptionsByDriverByBrandBySegmentByStepAvgTraining = 
				new double [atts][brands][segments][trainingSteps];
		
		double[][][][] perceptionsByDriverByBrandBySegmentByStepAvgHoldOut = 
				new double [atts][brands][segments][holdOutSteps];
		
		for (int a = 0; a < atts; a++) {
			for (int b = 0; b < brands; b++) {
				for (int seg = 0; seg < segments; seg++) {
					for (int it = 0; it<trainingSteps; it++) {
						perceptionsByDriverByBrandBySegmentByStepAvgTraining[a][b][seg][it] = 
								perceptionsByDriverByBrandBySegmentByStepAvg[a][b][seg][it];
					}
					for (int ih = 0; ih<holdOutSteps; ih++) {
						perceptionsByDriverByBrandBySegmentByStepAvgHoldOut[a][b][seg][ih] = 
								perceptionsByDriverByBrandBySegmentByStepAvg[a][b][seg][ih+trainingSteps];
					}
				}
			}
		}
		
		holdOutResult.trainingResult.perceptionsByDriverByBrandBySegmentByStepAvg = 
				perceptionsByDriverByBrandBySegmentByStepAvgTraining;
		
		holdOutResult.testResult.perceptionsByDriverByBrandBySegmentByStepAvg = 
				perceptionsByDriverByBrandBySegmentByStepAvgHoldOut;
	}
	
	public static HoldOutResult splitHoldOut(SimulationResult result, double holdOut) {
		HoldOutResult hr = new HoldOutResult();
		
		int totalSteps = result.getOverTimeSteps();
		
		int holdOutSteps = (int) Math.ceil( totalSteps * holdOut);
		
		splitSales(hr, result, holdOutSteps);
		if(result.awarenessByBrandBySegByStepAvg!=null)
			splitAwareness(hr, result, holdOutSteps);
		if(result.perceptionsByDriverByBrandBySegmentByStepAvg!=null)
			splitPerceptions(hr, result, holdOutSteps);
		
		return hr;
	}
}
