package es.ugr.sci2s.soccer.workers;

import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;

public class EvaluationWorker extends CalibrationWorker {

	public EvaluationWorker(CalibrationConfig calibrationSetup, 
			CalibrationResponse response) {
		super(calibrationSetup, response);
		response.setCalibratedModel(
				calibrationSetup.getSimConfig().getModelDefinition(), 
					baseConfig);
	}

	@Override
	public void run() {
		evaluate();
	}
}
