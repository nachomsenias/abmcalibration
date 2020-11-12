package model;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import model.simple.SimpleModelBuilder;
import model.simple.SimpleModelThreadExecuter;
import util.exception.simulation.SimulationException;
import util.exception.simulation.SimulationListener;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;

public class ModelRunner {
	/**
	 * Centralizes a model simulation using a multi-thread strategy: 
	 * creates a thread pool with size = cpus-1 and waits for all the
	 * threads to finish. 
	 * @param md the model definition to be simulated.
	 * @param numMC the number of Monte-Carlo iterations (every 
	 * iteration is simulated in a single thread).
	 * @param fromGUI boolean flag activating logging in the view 
	 * module.
	 * @param statisticSetup the statistics setup to be stored during 
	 * simulation.
	 * @return the monte-carlo statistics resulting from simulating the 
	 * given model definition.
	 * @throws SimulationException if errors are detected during the 
	 * simulation, exceptions are thrown.
	 */
	private static MonteCarloStatistics simulate(ModelDefinition md, int numMC,
			boolean fromGUI, StatisticsRecordingBean statisticSetup) throws SimulationException {
		
		//Create results container
		MonteCarloStatistics mcStats = new MonteCarloStatistics(
				numMC, 
				md.getNumberOfSegments(),
				md.getNumberOfBrands(), 
				md.getNumberOfAttributes(), 
				md.getNumberOfSteps(),
				md.getStepsForWeek()
			);
		
		//Initiate Thread pool
		int cores = Runtime.getRuntime().availableProcessors();
		//If enough cores are available, reduce in one to avoid overloading the computer.
		if(cores > 2) {
			cores--;
		}

		ExecutorService pool = Executors.newFixedThreadPool(cores);
		
		SimulationListener listener = new SimulationListener();
		
		try {
			ModelBuilder mb = md.createBuilder();
			
			//Create shared data
			ModelBean bean = mb.createBean();
			
			for (int i=0; i<numMC; i++) {
				ModelThreadExecuter worker = new ModelThreadExecuter(mb, bean, 
						mcStats, statisticSetup, i, numMC,fromGUI, listener);
				pool.execute(worker);
			}
			//Close pool (no more tasks will be scheduled).
			pool.shutdown();
			
			//Wait for every iteration to finish
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			pool.shutdownNow();
			throw new SimulationException("Execution stopped by user.\n\n");
		} catch (Exception e) {
			pool.shutdownNow();
			throw new SimulationException("Unnexpected error found: " + e.getMessage());
		} finally {
			pool.shutdownNow();
		}
		
		if(listener.errorFound) {
			throw new SimulationException(listener.errorMessage);
		}
		
		return mcStats;
	}
	
	/**
	 * Centralizes a model simulation using a multi-thread strategy: 
	 * creates a thread pool with size = cpus-1 and waits for all the
	 * threads to finish. 
	 * @param md the model definition to be simulated.
	 * @param numMC the number of Monte-Carlo iterations (every 
	 * iteration is simulated in a single thread).
	 * @param fromGUI boolean flag activating logging in the view 
	 * module.
	 * @param statisticSetup the statistics setup to be stored during 
	 * simulation.
	 * @return the monte-carlo statistics resulting from simulating the 
	 * given model definition.
	 * @throws SimulationException if errors are detected during the 
	 * simulation, exceptions are thrown.
	 */
	private static MonteCarloStatistics simulateSimple(ModelDefinition md, int numMC,
			boolean fromGUI, StatisticsRecordingBean statisticSetup) throws SimulationException {
		
		//Create results container
		MonteCarloStatistics mcStats = new MonteCarloStatistics(
				numMC, 
				md.getNumberOfSegments(),
				md.getNumberOfBrands(), 
				md.getNumberOfAttributes(), 
				md.getNumberOfSteps(),
				md.getStepsForWeek()
			);
		
		//Initiate Thread pool
		int cores = Runtime.getRuntime().availableProcessors();
		//If enough cores are available, reduce in one to avoid overloading the computer.
//		if(cores > 2) {
//			cores--;
//		}

		ExecutorService pool = Executors.newFixedThreadPool(cores);
		
		SimulationListener listener = new SimulationListener();
		
		try {
			SimpleModelBuilder mb = new SimpleModelBuilder(md);
			
			for (int i=0; i<numMC; i++) {
				SimpleModelThreadExecuter worker = new SimpleModelThreadExecuter(mb, 
						mcStats, statisticSetup, i, numMC,fromGUI, listener);
				pool.execute(worker);
			}
			//Close pool (no more tasks will be scheduled).
			pool.shutdown();
			
			//Wait for every iteration to finish
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			pool.shutdownNow();
			throw new SimulationException("Execution stopped by user.\n\n");
		} catch (NullPointerException e) {
			pool.shutdownNow();
			e.printStackTrace();
			throw new SimulationException("Null point exception!");
			
		} catch (Exception e) {
			pool.shutdownNow();
			throw new SimulationException("Unnexpected error found: " + e.getMessage());
		} finally {
			pool.shutdownNow();
		}
		
		if(listener.errorFound) {
			throw new SimulationException(listener.errorMessage);
		}
		
		return mcStats;
	}
	
	/**
	 * Centralizes a model simulation using a multi-thread strategy: 
	 * creates a thread pool with size = cpus-1 and waits for all the
	 * threads to finish. 
	 * @param md the model definition to be simulated.
	 * @param numMC the number of Monte-Carlo iterations (every 
	 * iteration is simulated in a single thread).
	 * @param fromGUI boolean flag activating logging in the view 
	 * module.
	 * @param statisticSetup the statistics setup to be stored during 
	 * simulation.
	 * @return the monte-carlo statistics resulting from simulating the 
	 * given model definition.
	 * @throws SimulationException if errors are detected during the 
	 * simulation, exceptions are thrown.
	 */
	public static MonteCarloStatistics simulateModel(ModelDefinition md, int numMC,
			boolean fromGUI, StatisticsRecordingBean statisticSetup) throws SimulationException {
		if(md.isSimple()) {
			return simulateSimple(md, numMC, fromGUI, statisticSetup);
		} else {
			return simulate(md, numMC, fromGUI, statisticSetup);
		}
	}
}


