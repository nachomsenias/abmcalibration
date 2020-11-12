package es.ugr.sci2s.soccer.beans;

import java.util.Arrays;

import es.ugr.sci2s.soccer.workers.SimulationWorker;

public class ResultContainer {

	private SimulationResult simpleResult;
	private SimulationResult[] comparisonResult;
	private SimulationResult[][] saResult;
	private CalibrationResponse evaluationResult;
	
	private boolean finished;
	private boolean failed;
	private boolean delivered = false;
	
	private boolean isSA = false;
	private boolean isContribution = false;
	private boolean isEvaluation = false;
	
	private String errorMessage;
	
	private SimulationWorker worker;
	
	public SimulationResult getSimpleResult() {
		return simpleResult;
	}

	public void setSimpleResult(SimulationResult simpleResult) {
		this.simpleResult = simpleResult;
	}

	public SimulationResult[] getComparisonResult() {
		return comparisonResult;
	}

	public void setComparisonResult(SimulationResult[] comparisonResult) {
		this.comparisonResult = comparisonResult;
		isContribution = true;
	}

	public SimulationResult[][] getSaResult() {
		return saResult;
	}

	public void setSaResult(SimulationResult[][] saResult) {
		this.saResult = saResult;
		isSA = true;
	}

	public boolean isContribution() {
		return isContribution;
	}
	
	public boolean isSA() {
		return isSA;
	}
	
	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isFailed() {
		return failed;
	}

	public void fail() {
		this.failed = true;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public SimulationWorker getWorker() {
		return worker;
	}

	public void setWorker(SimulationWorker worker) {
		this.worker = worker;
	}
	
	public void delivered() {
		this.delivered = true;
	}
	
	public boolean isDelivered() {
		return delivered;
	}

	public CalibrationResponse getEvaluationResult() {
		return evaluationResult;
	}

	public void setEvaluationResult(CalibrationResponse evaluationResult) {
		isEvaluation = true;
		this.evaluationResult = evaluationResult;
	}

	public boolean isEvaluation() {
		return isEvaluation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(comparisonResult);
		result = prime * result + (delivered ? 1231 : 1237);
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((evaluationResult == null) ? 0 : evaluationResult.hashCode());
		result = prime * result + (failed ? 1231 : 1237);
		result = prime * result + (finished ? 1231 : 1237);
		result = prime * result + (isContribution ? 1231 : 1237);
		result = prime * result + (isEvaluation ? 1231 : 1237);
		result = prime * result + (isSA ? 1231 : 1237);
		result = prime * result + Arrays.deepHashCode(saResult);
		result = prime * result + ((simpleResult == null) ? 0 : simpleResult.hashCode());
		result = prime * result + ((worker == null) ? 0 : worker.hashCode());
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
		ResultContainer other = (ResultContainer) obj;
		if (!Arrays.equals(comparisonResult, other.comparisonResult))
			return false;
		if (delivered != other.delivered)
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (evaluationResult == null) {
			if (other.evaluationResult != null)
				return false;
		} else if (!evaluationResult.equals(other.evaluationResult))
			return false;
		if (failed != other.failed)
			return false;
		if (finished != other.finished)
			return false;
		if (isContribution != other.isContribution)
			return false;
		if (isEvaluation != other.isEvaluation)
			return false;
		if (isSA != other.isSA)
			return false;
		if (!Arrays.deepEquals(saResult, other.saResult))
			return false;
		if (simpleResult == null) {
			if (other.simpleResult != null)
				return false;
		} else if (!simpleResult.equals(other.simpleResult))
			return false;
		if (worker == null) {
			if (other.worker != null)
				return false;
		} else if (!worker.equals(other.worker))
			return false;
		return true;
	}
}
