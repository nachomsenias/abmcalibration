package es.ugr.sci2s.soccer.workers;

import java.util.Map;

import es.ugr.sci2s.soccer.beans.ResultContainer;

public class ResultTimer implements Runnable {

	public static final int WAIT_MILLIS = 1800000;
	
	private int id;
	private Map<Integer,ResultContainer> resultsTable;
	
	public ResultTimer(int id, Map<Integer, ResultContainer> resultsTable) {
		this.id = id;
		this.resultsTable = resultsTable;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Sleep failure :: "+e.getMessage());
		} finally {
			
			ResultContainer container = resultsTable.get(id);
			
			resultsTable.remove(id);
			
			if(!container.isFinished()) {
				try {
					container.getWorker().stop();
				}catch (Exception e) {
					e.printStackTrace();
					System.out.println(
						"Timer error :: Errors found when stopping execution: " 
							+ e.getMessage());
				}
			}

			//Invoke gc for cleanup
			System.gc();
		}

	}

}
