package es.ugr.sci2s.soccer.workers;

import java.io.File;

import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;

import calibration.CalibrationController;
import calibration.CalibrationResult;
import calibration.CalibrationTask;
import util.StringBean;
import util.exception.calibration.CalibrationException;
import util.exception.simulation.SimulationException;
import util.exception.view.TerminationException;
import util.random.RandomizerUtils;

public class ExperimentWorker extends CalibrationWorker {

	private String folder = ".";
	private String problemName ="UNNAMED";
	
	private int evaluations;
	
	private int iterations;
	private int iterationStart;
	private CalibrationResponse [] responses;
	
	private boolean realCoding;
	private String signature;

	private StringBean[] additionalConfig;
	
	public ExperimentWorker(CalibrationConfig calibrationSetup, 
			CalibrationResponse response, String name, int iterations, 
			int iterationStart, int evaluations, boolean realCoding, 
			String folder) {
		super(calibrationSetup, response);
		problemName = name;
		this.iterations = iterations;
		this.iterationStart = iterationStart;
		responses = new CalibrationResponse[iterations];
		this.evaluations = evaluations;
		this.realCoding= realCoding;
		this.folder = folder;
	}

	@Override
	protected void lauchCalibration(
			String algorithmConfigFile, 
			String masterHost,
			String masterPort
		) {

		//Setup the calibration parameter manager
		setupParameterManager(realCoding);
		
		System.out.println(algorithmConfigFile);
		
		//Create log folder
		File log = new File(folder);
		
		if(!log.exists()) {
			log.mkdir();
		}
		
		signature =problemName +"_"+ new File(calibrationSetup.getAlgorithm()).getName()+
				"_"+String.valueOf(System.currentTimeMillis());
		
		System.out.println(log.toString());
		
		System.out.println("Running "+(iterations)+" repetitions.");
		
		for (int i=iterationStart; i<(iterationStart+iterations); i++) {
			try {
				
				System.out.println("Begining repetition: "+i);
				
				String itSignature = signature+"_it"+i;
				
				CalibrationTask task = new CalibrationTask(
						RandomizerUtils.PRIME_SEEDS[i], 
						itSignature, 
						algorithmConfigFile, 
						folder, 
						paramManager, 
						mcIterations, 
						md,
						manager,
						CalibrationTask.SKIP_VALIDATION
						);
				
				controller = new CalibrationController(task, 
						initialParamValues, evaluations);
				
				controller.setAdditionalAlgorithmParameters(additionalConfig);
				
				/* Computes the initial adjustment simulating the initial model
				 * and gathering score and simulation time.
				 */
				initializeCalibration();
				
				/*
				 * Adjust number of agents during optimization if the parameter was provided.
				 */
				if(calibrationSetup.getCalibrationAgents()!= CalibrationConfig.USE_BASE_VALUE) {
					md.setNumberOfAgents(calibrationSetup.getCalibrationAgents());
				}
				
				CalibrationResponse calibrationResponse = new CalibrationResponse();
				
				CalibrationResult result =controller.execute(masterHost,
						masterPort,baseConfig.getNumberOfAgents());
				
				controller.updateModelDefinition(md, 
						result.getUnconvertedParameters());
				
				calibrationResponse.setCalibratedModel(md, baseConfig);
				
				calibrationResponse.setScoreDetails(
						manager.computeTrainingScore(
								result.getSimulationResultMC()));
				
				System.out.println("Final score: "
						+calibrationResponse.printScoreValues());
				
				calibrationResponse.done();
				
				responses[i-iterationStart] = calibrationResponse;
				
				
			} catch (CalibrationException e) {
				calibrationResponse.fail();
				calibrationResponse.setErrorMessage(e.getMessage());
				e.printStackTrace();
			} catch (SimulationException e) {
				calibrationResponse.fail();
				calibrationResponse.setErrorMessage(e.getMessage());
				e.printStackTrace();
			} catch (TerminationException e) {
				calibrationResponse.fail();
				System.out.println("Calibration with id '"
						+this.id+"' has been canceled.");
			}
		}
	}
	
	public void setAdditionalAlgorithmParameters(StringBean[] pairs) {
		this.additionalConfig = pairs;
	}
	
	public CalibrationResponse[] getResponses() {
		return responses;
	}
	
	public String getSignature() {
		return signature;
	}
}
