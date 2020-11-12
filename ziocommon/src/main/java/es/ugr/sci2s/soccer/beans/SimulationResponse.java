package es.ugr.sci2s.soccer.beans;

public class SimulationResponse {

	private long timeMillis;
	private int id;
	private String errorMessage;
	
	public SimulationResponse() {
	}

	public long getTimeMillis() {
		return timeMillis;
	}

	public void setTimeMillis(long timeMillis) {
		this.timeMillis = timeMillis;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getError() {
		return errorMessage;
	}

	public void setError(String error) {
		this.errorMessage = error;
	}
}
