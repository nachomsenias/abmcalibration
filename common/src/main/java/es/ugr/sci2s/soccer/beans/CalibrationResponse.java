package es.ugr.sci2s.soccer.beans;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import calibration.fitness.history.ScoreBean.ScoreWrapper;
import model.ModelDefinition;

public class CalibrationResponse {

	private SimulationConfig calibratedModel;
	private String fitnessPercentaje;
	
	private long millisecondsLeft = Integer.MAX_VALUE;
	
	private boolean finished = false;
	
	private boolean failed = false;
	private String errorMessage;
	
	private SimulationResult result;
	private HoldOutResult holdOutResult;
	
	private ScoreWrapper trainingDetails;
	
	private ScoreWrapper testDetails;
	
	public CalibrationResponse() {
		
	}

	public SimulationConfig getCalibratedModel() {
		return calibratedModel;
	}

	public void setCalibratedModel(ModelDefinition calibratedModel, 
			SimulationConfig baseConfig) {
		this.calibratedModel = new SimulationConfig();
		this.calibratedModel.loadFromModelDefinition(calibratedModel);
		//Base values
		this.calibratedModel.setnMC(baseConfig.getnMC());
		this.calibratedModel.setStatPeriod(baseConfig.getStatPeriod());
		this.calibratedModel.setNumberOfAgents(baseConfig.getNumberOfAgents());
	}

	public String getFitnessPercentaje() {
		return fitnessPercentaje;
	}
	
	public String printScoreValues() {
		String values = trainingDetails.printGlobalScores();
		
		if(testDetails!=null) {
			values.concat("\n"+testDetails);
		}
				
		return values;
	}

	public void setFitnessPercentaje(double fitnessPercentaje) {
		//Configures the formatter
		DecimalFormat formatter = new DecimalFormat("#.##");
		DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
		newSymbols.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(newSymbols);
		//Formats the value
		this.fitnessPercentaje = formatter.format(fitnessPercentaje);
	}
	
	public void fail() {
		failed=true;
	}
	
	public boolean hasFailed() {
		return failed;
	}
	
	public void done() {
		finished=true;
	}
	
	public boolean isFinished() {
		return finished;
	}

	public long getMillisecondsLeft() {
		return millisecondsLeft;
	}

	public void setMillisecondsLeft(long secondsLeft) {
		this.millisecondsLeft = secondsLeft;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public SimulationResult getResult() {
		return result;
	}

	public void setResult(SimulationResult result) {
		this.result = result;
	}

	public HoldOutResult getHoldOutResult() {
		return holdOutResult;
	}

	public void setHoldOutResult(HoldOutResult holdOutResult) {
		this.holdOutResult = holdOutResult;
	}

	public ScoreWrapper getSalesScoreDetails() {
		return trainingDetails;
	}

	public void setScoreDetails(ScoreWrapper scoreDetails) {
		this.trainingDetails = scoreDetails;
		setFitnessPercentaje(scoreDetails.finalScore);
	}

	public ScoreWrapper getHoldOutDetails() {
		return testDetails;
	}

	public void setHoldOutDetails(ScoreWrapper holdOutDetails) {
		this.testDetails = holdOutDetails;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((calibratedModel == null) ? 0 : calibratedModel.hashCode());
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + (failed ? 1231 : 1237);
		result = prime * result + (finished ? 1231 : 1237);
		result = prime * result + ((fitnessPercentaje == null) ? 0 : fitnessPercentaje.hashCode());
		result = prime * result + ((testDetails == null) ? 0 : testDetails.hashCode());
		result = prime * result + ((holdOutResult == null) ? 0 : holdOutResult.hashCode());
		result = prime * result + (int) (millisecondsLeft ^ (millisecondsLeft >>> 32));
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + ((trainingDetails == null) ? 0 : trainingDetails.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CalibrationResponse other = (CalibrationResponse) obj;
		if (calibratedModel == null) {
			if (other.calibratedModel != null)
				return false;
		} else if (!calibratedModel.equals(other.calibratedModel))
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (failed != other.failed)
			return false;
		if (finished != other.finished)
			return false;
		if (fitnessPercentaje == null) {
			if (other.fitnessPercentaje != null)
				return false;
		} else if (!fitnessPercentaje.equals(other.fitnessPercentaje))
			return false;
		if (testDetails == null) {
			if (other.testDetails != null)
				return false;
		} else if (!testDetails.equals(other.testDetails))
			return false;
		if (holdOutResult == null) {
			if (other.holdOutResult != null)
				return false;
		} else if (!holdOutResult.equals(other.holdOutResult))
			return false;
		if (millisecondsLeft != other.millisecondsLeft)
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		if (trainingDetails == null) {
			if (other.trainingDetails != null)
				return false;
		} else if (!trainingDetails.equals(other.trainingDetails))
			return false;
		return true;
	}
}
