package es.ugr.sci2s.soccer.workers;

import java.io.File;
import java.net.URL;

import calibration.CalibrationController;
import calibration.CalibrationTask;
import es.ugr.sci2s.soccer.util.CustomSlave;
import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;
import ec.util.ParameterDatabase;
import util.exception.calibration.CalibrationException;
import util.exception.simulation.SimulationException;
import util.random.RandomizerUtils;

public class SlaveWorker extends CalibrationWorker {

	public static final String SLAVE_CONFIG_FILE = "configGA_Slave.ecj";
	
	private String masterHost;	
	private String masterPort;
	
	private CustomSlave slave;
	
	public SlaveWorker(CalibrationConfig calibrationSetup, 
			CalibrationResponse response, String masterHost,
			String masterPort) {
		super(calibrationSetup, response);
		
		this.masterHost=masterHost;
		this.masterPort=masterPort;
	}
	
	public void stop() {
		slave.stop();
	}

	@Override
	public void run() {
		
		//Setup the calibration parameter manager
		// XXX Slave worker ignores real-coding.
		setupParameterManager(false);
		
		//Find algorithm configuration file
		URL algorithmURL = this.getClass().getClassLoader().getResource(SLAVE_CONFIG_FILE);
		String algorithmConfigFile=algorithmURL.getPath();
		System.out.println(algorithmConfigFile);
		
		String logFolder=this.getClass().getResource("/").getPath();		
		System.out.println(logFolder);
		
		StringBuffer sb = new StringBuffer(logFolder);
		logFolder=sb.append(DEFAULT_LOG_FOLDER).toString();
		//Creater log dir
		File log = new File(logFolder);
		log.mkdir();
		
		System.out.println(logFolder);
		
		try {
			CalibrationTask task = new CalibrationTask(
					RandomizerUtils.PRIME_SEEDS[0], 
					String.valueOf(id), 
					algorithmConfigFile, 
					DEFAULT_LOG_FOLDER, 
					paramManager, 
					mcIterations, 
					md,
					manager,
					CalibrationTask.SKIP_VALIDATION
					);
			
			controller = new CalibrationController(task, initialParamValues, 
					calibrationSetup.getTargetEvaluations());
			
			/*
			 * Adjust number of agents during optimization if the parameter was provided.
			 */
			if(calibrationSetup.getCalibrationAgents()!= CalibrationConfig.USE_BASE_VALUE) {
				md.setNumberOfAgents(calibrationSetup.getCalibrationAgents());
			}
			
		    ParameterDatabase parameters = CustomSlave.getParameterDatabase(algorithmConfigFile);

		    slave = new CustomSlave();		    
		    slave.runSlave(controller, parameters, masterHost, masterPort);
		    
		} catch (CalibrationException e) {
			e.printStackTrace();
		} catch (SimulationException e) {
			e.printStackTrace();
		}
	}
}
