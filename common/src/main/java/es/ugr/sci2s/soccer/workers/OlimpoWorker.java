package es.ugr.sci2s.soccer.workers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;

import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;

public class OlimpoWorker extends CalibrationWorker {

	private static final String MASTER_CONFIG_FILE="configGA_Master.ecj";
	
	private static final int NUMBER_OF_CONNECTION_ATTEMPTS = 5;
	
	private static final long CONNECTION_WAITING_DELAY = 5000;
	
	private String masterHost;
	
	private String masterPort;
	
	private Collection<String> slaves;
	
	private String json;

	public OlimpoWorker(
			CalibrationConfig calibrationSetup, 
			CalibrationResponse response,
			String config,
			String masterHost,
			String masterPort
			) {
		super(calibrationSetup,response);
		this.masterHost=masterHost;
		this.masterPort=masterPort;
		this.slaves = new HashSet<String>();
		this.json=config;
	}
	
	/**
	 * Attemps to connected to created node using a POST requests to its 
	 * Tomcat platform.
	 * @param node the node to connect
	 * @return if the connection was successful (true) or not (false).
	 */
	private boolean connectToSlave(String node) {
		
		int attemptsLeft = NUMBER_OF_CONNECTION_ATTEMPTS;
		
		while(attemptsLeft>0) {
			try {
				Request.Post("http://"+node+"/ziocalibrate/slavecalibration/")
					.bodyForm(Form.form()
							.add("masterhost", masterHost)
							.add("masterport", masterPort)
							.add("config",json).build())
					.execute();
				//Connection successful
				System.out.println("Connected to "+node);
				return true;
			} catch (ClientProtocolException e) {
				System.out.println("HTTP Error connecting to node: "
										+node+" \n" + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IO Error connecting to node: "
										+node+" \n" + e.getMessage());
				e.printStackTrace();
			}
			try {
				System.out.println("Waiting "
						+TimeUnit.MILLISECONDS.toSeconds(CONNECTION_WAITING_DELAY)
							+" seconds before next connection attempt.");
				
				Thread.sleep(CONNECTION_WAITING_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			attemptsLeft--;
		}
		//No attempts left, failed to start
		System.out.println("Failed to connect to slave "+node+" after "
				+NUMBER_OF_CONNECTION_ATTEMPTS+" attempts.");
		System.out.println("Host: "+masterHost);
		System.out.println("Port: "+masterPort);
		
		return false;
	}
	
	private boolean stopSlave(String ip) {
		int attemptsLeft = NUMBER_OF_CONNECTION_ATTEMPTS;
		
		while(attemptsLeft>0) {
			try {
				Request.Delete("http://"+ip+"/ziocalibrate/slavecalibration/")
					.execute();
				return true;
			} catch (ClientProtocolException e) {
				System.out.println("HTTP Error connecting to node: "
						+ip+" \n" + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IO Error connecting to node: "
						+ip+" \n" + e.getMessage());
				e.printStackTrace();
			}
			attemptsLeft--;
		}
		return false;
	}
	
	private void cancelSlavesExecution(){
		for(String node : slaves) {
			stopSlave(node);
		}
	}
	
	private void startSlaves() {
		for (String node : slaves) {
			connectToSlave(node);
		}
	}
	
	public Collection<String> getSlaves() {
		return slaves;
	}
	
	/**
	 * Takes the number of slaves from the list.
	 * @param slaves
	 * @param numSlaves
	 */
	public void setSlaves(Stack<String> slaves, int numSlaves) {
		this.slaves = new ArrayList<String>();
		
		while(this.slaves.size()!=numSlaves && !slaves.isEmpty()) {
			String slave = slaves.pop();
			
			this.slaves.add(slave);
		}
	}
	
	@Override
	public void run() {
		startSlaves();
		System.out.println("Beginning MS calibration.");
		lauchCalibration(MASTER_CONFIG_FILE, masterHost, masterPort);
		System.out.println("Finished MS calibration.");
		cancelSlavesExecution();
	}
	
	@Override
	public long estimateTimeLeft() {
		if(controller!=null)
			return controller.estimateTimeLeftUsingNumberOfGenerations();
		else return Long.MAX_VALUE;
	}
	
	@Override
	public void terminate() {
		try {
			controller.terminateCalibration();
		} catch (Exception e) {
			//Do nothing
		} finally {
			cancelSlavesExecution();
		}
	}
}
