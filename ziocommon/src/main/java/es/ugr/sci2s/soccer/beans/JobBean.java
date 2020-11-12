package es.ugr.sci2s.soccer.beans;

public class JobBean {

	private int id;
	
	private CalibrationResponse response;

	public JobBean(int id, CalibrationResponse response) {
		this.id = id;
		this.response = response;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public CalibrationResponse getResponse() {
		return response;
	}

	public void setResponse(CalibrationResponse response) {
		this.response = response;
	}
}
