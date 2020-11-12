package es.ugr.sci2s.soccer.test.junit;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import es.ugr.sci2s.soccer.util.script.RunJSON;
import org.junit.Test;

import util.exception.sales.SalesScheduleError;
import util.exception.simulation.SimulationException;

public class ExceptionUnitTest {
	
	private String failingCreating = "test/exception/failingmodel-creating.json";
	private String failingRunning = "test/exception/failingmodel-running.json";
	private String failingRunningBuyingCycle = 
			"test/exception/failingmodel-running-cycle.json";
	
	private String notFailing = "test/exception/not-failing.json";

	@Test
	public void notFailing() throws IOException, SalesScheduleError {
		
		System.out.println("Running not-failing model...");
		
		String[] args = {notFailing};
		
		RunJSON.main(args);
	}
	
	@Test
	public void failCreatingModel() {
		
		System.out.println("Running a model that fails during creation ...");
		
		String[] args = {failingCreating};
		
		boolean exceptionThrown = false;
		
		try {
			RunJSON.main(args);
		} catch (IOException | SimulationException e) {
			System.out.println(e.getMessage());
			exceptionThrown = true;
		}
		
		assertTrue(exceptionThrown);
	}

	@Test
	public void failRunningModel() {
		
		System.out.println("Running a model that fails running ...");
		
		String[] args = {failingRunning};
		
		boolean exceptionThrown = false;
		
		try {
			RunJSON.main(args);
		} catch (IOException | SimulationException e) {
			System.out.println(e.getMessage());
			exceptionThrown = true;
		}
		
		assertTrue(exceptionThrown);
	}
	
	@Test
	public void failBuyingCycle() {
		
		System.out.println("Running a model with a really high buying cycle value ...");
		
		String[] args = {failingRunningBuyingCycle};
		
		boolean exceptionThrown = false;
		
		try {
			RunJSON.main(args);
		} catch (IOException | SimulationException e) {
			System.out.println(e.getMessage());
			exceptionThrown = true;
		}
		
		assertTrue(exceptionThrown);
	}
}
