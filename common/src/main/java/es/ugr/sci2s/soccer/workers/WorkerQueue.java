package es.ugr.sci2s.soccer.workers;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class WorkerQueue implements Runnable{

	private Queue<SimulationWorker> queue;
	
	private boolean stopped;
	
	public WorkerQueue() {
		queue = new ArrayDeque<SimulationWorker>();
		stopped = true;
	}
	
	public void insert(SimulationWorker worker) {
		queue.add(worker);
	}
	
	synchronized private void start() {
		stopped = false;
		while(queue.peek()!=null) {
			SimulationWorker worker = queue.poll();

			worker.run();
		}
		stopped = true;
	}

	@Override
	public void run() {
		start();
	}
	
	public boolean isStopped() {
		return stopped;
	}
	
	public String getJobs() {
		return Arrays.toString(queue.toArray());
	}
}
