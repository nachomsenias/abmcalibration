package es.ugr.sci2s.soccer.beans;

import es.ugr.sci2s.soccer.workers.CalibrationWorker;

public class RemoteCalibrationTask {

	private CalibrationWorker worker;
	private Thread thread;
	private CalibrationResponse response;
	
	public RemoteCalibrationTask(CalibrationWorker worker, Thread thread, CalibrationResponse response) {
		super();
		this.worker = worker;
		this.thread = thread;
		this.response = response;
	}
	
	public RemoteCalibrationTask(CalibrationResponse response) {
		super();
		this.worker = null;
		this.thread = null;
		this.response = response;
	}

	public CalibrationWorker getWorker() {
		return worker;
	}

	public void setWorker(CalibrationWorker worker) {
		this.worker = worker;
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public CalibrationResponse getResponse() {
		return response;
	}

	public void setResponse(CalibrationResponse response) {
		this.response = response;
	}
}
