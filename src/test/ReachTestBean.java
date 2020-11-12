package test;

import gnu.trove.list.array.TIntArrayList;
import model.customer.Agent;

/**
 * ReachTestBean contains the data needed to inspect the results
 * of touch point scheduling.
 * 
 * Its main purpose is provide the View module with an object
 * containing most of the scheduling information.
 * 
 * @author imoya
 *
 */
public class ReachTestBean {

	private Agent[] customers;
	private byte[][] schedule;
	private byte[][] debugSchedule;
	private double[] actualRM;
	
	private boolean [] agentsReached;
	private int numSegments;

	private double [][] reachEvolution;
	private double [][] actualRMByStep;
	
	public ReachTestBean(
			Agent[] customers, 
			byte[][] schedule,
			byte[][] debugSchedule,
			double[] actualRM,
			TIntArrayList [] agentsReached,
			int numSegments,
			double [][] carm,
			double [][] actualRMByStep
			) {
		
		super();
		this.customers = customers;
		this.schedule = schedule;
		this.debugSchedule = debugSchedule;
		this.actualRM = actualRM;
		
		this.agentsReached = new boolean [customers.length];
		for (int i=0; i<agentsReached.length; i++) {
			int[] indexes = agentsReached[i].toArray();
			for (int index : indexes) {
				this.agentsReached[index] = true;
			}
		}
		
		this.actualRMByStep=actualRMByStep;
		
		this.numSegments=numSegments;
		reachEvolution = carm;
	}

	public Agent[] getCustomers() {
		return customers;
	}
	
	public byte[][] getSchedule() {
		return schedule;
	}
	
	public byte[][] getDebugSchedule() {
		return debugSchedule;
	}

	public double[] getActualRM() {
		return actualRM;
	}

	public boolean [] getAgentsReached() {
		return agentsReached;
	}

	public int getNumSegments() {
		return numSegments;
	}
	
	public double [][] getReachEvolution() {
		return reachEvolution;
	}

	public double[][] getActualRMByStep() {
		return actualRMByStep;
	}

}
