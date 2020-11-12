package model;

import util.exception.sales.SalesScheduleError;
import util.exception.simulation.SimulationListener;
import util.io.StatisticsRecordingBean;
import util.random.RandomizerUtils;
import util.statistics.MonteCarloStatistics;

public class ModelThreadExecuter implements Runnable{
	
	private final ModelBuilder mb;	
	private final ModelBean bean;
	
	private final MonteCarloStatistics stats;
	private final StatisticsRecordingBean statRecording;
	
	private final int index;
	
	private final SimulationListener listener;
	
	public ModelThreadExecuter(ModelBuilder mb, ModelBean bean,
			MonteCarloStatistics stats, StatisticsRecordingBean statRecording,
			int index, int totalMC, boolean fromGUI, 
			SimulationListener listener
		) {
		super();
		this.mb = mb;
		
		this.bean = bean;
		this.stats = stats;
		
		this.statRecording = statRecording;
		
		this.index = index;
		this.listener = listener;
	}

	@Override
	public void run() {
		try {
			//Model creation
			Model m = mb.build(bean, RandomizerUtils.PRIME_SEEDS[index]);
			m.enableAdditionalStatistics(
					statRecording.exportSales, statRecording.exportAwareness, 
					statRecording.exportPerceptions, statRecording.anyRecording, 
					statRecording.anyWoM, 
					statRecording.exportTouchPointContributions);
			if(statRecording.anyWoM) {
				m.enableWoMReports();
			}
			//Run Simulation
			m.runSilent();
			stats.saveStatistics(m.getStatistics(), index);
			
		} catch (SalesScheduleError e) {
			listener.errorFound("Error during simulation.\n"
											+e.getMessage()+"\n");
		} catch (IllegalArgumentException e) {
			listener.errorFound("Invalid arguments.\n"
					+e.getMessage()+"\n");
		} catch (Exception e) {
			listener.errorFound("Unknown exception.\n"
					+e.getMessage()+"\n");
		}
	}
}
