package es.ugr.sci2s.soccer.workers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.jclouds.compute.RunNodesException;

import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;
import es.ugr.sci2s.soccer.util.cloud.Node;
import es.ugr.sci2s.soccer.util.cloud.gce.GCEWrapper;

public class MasterWorker extends CalibrationWorker {
	
	private static final String MASTER_CONFIG_FILE="configGA_Master.ecj";
	
	private static final int DEFAULT_NUMBER_OF_SLAVES = 2;
	
	private static final int NUMBER_OF_CONNECTION_ATTEMPTS = 5;
	
	private static final long CONNECTION_WAITING_DELAY = 5000;
	
	private String masterHost;
	
	private String masterPort;
	
	private Collection<Node> slaves;
	
	private String json;

	public MasterWorker(CalibrationConfig calibrationSetup, 
			CalibrationResponse response, 
			String config,
			String masterHost,
			String masterPort
			) {
		super(calibrationSetup,response);
		this.masterHost=masterHost;
		this.masterPort=masterPort;
		this.slaves = new HashSet<Node>();
		this.json=config;
	}
	
	/**
	 * Creates new nodes and connects to them.
	 */
	private void startUpSlaves() {
		//XXX Dynamic number of slaves depending on the problem size?
		int numberOfSlaves = DEFAULT_NUMBER_OF_SLAVES;
		
		//If some slaves are already created, we try to connect to them. 
		//This may happen if a calibration experiment was restarted, for example.
		if(slaves.size()>0) {
			for (Node n: slaves) {
				//Starts the calibration servlet
				boolean connectionSuccesful = connectToSlave(n);
				
				//If connection failed
				if(!connectionSuccesful) {
					//Remove the current node.
					slaves.remove(n);
					
					//Destroy it.
					destroyNode(n);
				}
			}
		}
		
		//Create slaves until reaching the quota.
		while(slaves.size()<numberOfSlaves) {
			Node newNode;
			try {
				newNode = GCEWrapper.createNode();
			} catch (IOException | RunNodesException e) {
				System.out.println("Error creating node: "+e.getMessage());
				e.printStackTrace();
				continue;
			}
			
			//Starts the calibration servlet
			if(connectToSlave(newNode)) {
				slaves.add(newNode);
			} else {
				//Destroy it.
				destroyNode(newNode);
			}
		}
	}
	
	/**
	 * Destroys the given node printing log messages.
	 * @param node the node to be destroyed.
	 */
	private void destroyNode(Node node) {
		System.out.println(
				"Too many errors connecting to node:"+node.ip
					+"\n Attempting to destroy it.");
		try {
			GCEWrapper.destroyNode(node.id);
			System.out.println(node.ip+": node destroyed.");
			
		} catch (IOException e1) {
			System.out.println(
					"Couldnt destroy failed node with id: "
							+node.id+" and ip:"+node.ip);
			e1.printStackTrace();
		}
	}
	
	/**
	 * Attemps to connected to created node using a POST requests to its 
	 * Tomcat platform.
	 * @param node the created new node.
	 * @return if the connection was succesful (true) or not (false).
	 */
	private boolean connectToSlave(Node node) {
		
		int attemptsLeft = NUMBER_OF_CONNECTION_ATTEMPTS;
		
		while(attemptsLeft>0) {
			try {
				Request.Post("http://"+node.ip+"/ziocalibrate/slavecalibration/")
					.bodyForm(Form.form()
							.add("masterhost", masterHost)
							.add("masterport", masterPort)
							.add("config",json).build())
					.execute();
				return true;
			} catch (ClientProtocolException e) {
				System.out.println("HTTP Error connecting to node: "
										+node.ip+" \n" + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IO Error connecting to node: "
										+node.ip+" \n" + e.getMessage());
				e.printStackTrace();
			}
			try {
				System.out.println("Waiting "
						+TimeUnit.MILLISECONDS.toSeconds(CONNECTION_WAITING_DELAY)
							+" seconds before next connection attempt.");
				
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			attemptsLeft--;
		}
		return false;
	}
//	
//	private void startUpSlaves() {
//		
//		//XXX Dynamic number of slaves depending on the problem size?
//		int slaveCount = slaves.size();
//		int numberOfSlaves = DEFAULT_NUMBER_OF_SLAVES;
//		
//		if(slaveCount<DEFAULT_NUMBER_OF_SLAVES) {
//			while(slaveCount<numberOfSlaves) {
//				Node newNode;
//				try {
//					newNode = GCEWrapper.createNode();
//				} catch (IOException | RunNodesException e) {
//					System.out.println("Error creating node: "+e.getMessage());
//					e.printStackTrace();
//					continue;
//				}
//				
//				slaves.add(newNode);
//				slaveCount++;
//			}
//		}
//
//		
//		
//		while(slaveCount<numberOfSlaves) {
//			Node newNode;
//			try {
//				newNode = GCEWrapper.createNode();
//			} catch (IOException | RunNodesException e) {
//				System.out.println("Error creating node: "+e.getMessage());
//				e.printStackTrace();
//				continue;
//			}
//			
//			//Starts the calibration servlet
//			try {
//				Request.Post("http://"+newNode.ip+"/ziocalibrate/slavecalibration/")
//				    .bodyForm(Form.form()
//				    		.add("masterhost", masterHost)
//				    		.add("masterport", masterPort)
//				    		.add("config",json).build())
//				    .execute();
//			} catch (Exception e) {
//				System.out.println("Error connecting to node: "+e.getMessage());
//				e.printStackTrace();
//				try {
//					GCEWrapper.destroyNode(newNode.id);
//				} catch (IOException e1) {
//					System.out.println("Couldnt destroy failed node with id: "+newNode.id);
//					e1.printStackTrace();
//				}
//				continue;
//			}
//			slaves.add(newNode);
//			slaveCount++;
//		}
//	}
	
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
		
		for(Node node : slaves) {
			if(!stopSlave(node.ip)) {
				//Remove the current node.
				slaves.remove(node);
				
				//Destroy it.
				destroyNode(node);
			}
		}
	}
	
	@Override
	public void run() {
		try {
			startUpSlaves();
			lauchCalibration(MASTER_CONFIG_FILE,masterHost,masterPort);
			shutDownSlaves();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public long estimateTimeLeft() {
		if(controller!=null)
			return controller.estimateTimeLeftUsingNumberOfGenerations();
		else return Long.MAX_VALUE;
	}
	
	@Override
	public void terminate() {
		super.terminate();
		cancelSlavesExecution();
	}
	
	public void shutDownSlaves() throws IOException {
		for(Node node : slaves) {
			GCEWrapper.destroyNode(node.id);
		}
		slaves.clear();
	}
	
	public Collection<Node> getSlaves() {
		return slaves;
	}
	
	public void setSlaves(Collection<Node> slaves) {
		this.slaves=slaves;
	}

}
