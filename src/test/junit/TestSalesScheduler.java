package test.junit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import model.Model;
import model.ModelBean;
import model.ModelBuilder;
import model.ModelDefinition;
import model.sales.SalesScheduler;
import util.exception.sales.SalesScheduleError;
import util.functions.ArrayFunctions;
import util.functions.Functions;
import util.functions.MatrixFunctions;
import util.random.RandomizerUtils;
import util.statistics.Statistics;
import util.statistics.Statistics.TimePeriod;

/**
 * This class contains the unit tests defined for SalesScheduler class.
 * The test implementation uses the JUnit framework.
 * 
 * The tests implemented for sales scheduling includes: decision cycle 
 * and total sales testing.
 * 
 * @author imoya
 *
 */
@RunWith(Parameterized.class)
public class TestSalesScheduler {	

	int iteration;
	int decisionCycle;
	
	Model m;
	
	String scenario;
	
	double ratio;
	double salesLeft;
	
	double[] seasonality;
	
	int[][] totalSalesByBrandBySegment;
	
	BitSet[] schedule;

	public TestSalesScheduler(
			String path, 
			int iteration,
			Boolean usePeriod,
			TimePeriod period,
			Boolean weekly
			) throws SalesScheduleError {
		
		ModelDefinition md = new ModelDefinition();
		
		scenario = path;
		md.loadValuesFromFile(new File(path));
		
		this.iteration=iteration;
		
		if(usePeriod) {
			int decisionCycle = Statistics.calculateWeeksPerPeriod(period);
			md.setDecisionCycle(decisionCycle);
		}
		
		if(!weekly) {
			md.setStepsForWeek(Model.DAYS_OF_WEEK);
		}
		
		ratio=md.getAgentsRatio();
		seasonality = md.getSeasonality();
		
		ModelBuilder mb = md.createBuilder();
		ModelBean bean = mb.createBean();
		m = mb.build(bean, RandomizerUtils.PRIME_SEEDS[iteration]);
		
		
		SalesScheduler ss = m.getSalesScheduler();
		ss.enableTest();
		try {
		m.runSilent();
		} catch (SalesScheduleError e) {
			System.out.println("Scheduling error :'(");
			System.out.println("Zio File: "+scenario);
			fail();
		}
		
		decisionCycle=m.getSalesScheduler().getDecisionCycle();
		
		totalSalesByBrandBySegment = 
			m.getStatistics().computeScaledSalesByBrandBySegment();
		
		salesLeft=ss.getCarryOverSales();
		schedule = ss.getSalesHistoryRecord();
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		final int ITERATIONS=10;
		
		String directoryPath = "./test/sales";
		
		File directory = new File(directoryPath);
		
		for (File f:directory.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(".zio");
				}
			})) {
			for (int i=0; i<ITERATIONS; i++) {
				Object[] filePath = {f.getPath(),i,new Boolean(false),null,new Boolean(false)};
				list.add(filePath);
				Object[] moarfilePath = {f.getPath(),i,new Boolean(false),null,new Boolean(true)};
				list.add(moarfilePath);
				for (TimePeriod period : TimePeriod.values()) {
					Object[] otherFilePath = {f.getPath(),i,new Boolean(true),null,new Boolean(false)};
					otherFilePath[3] = period;
					list.add(otherFilePath);
					Object[] evenMoarFilePath = {f.getPath(),i,new Boolean(true),null,new Boolean(true)};
					evenMoarFilePath[3] = period;
					list.add(evenMoarFilePath);
				}
			}
		}
		
		return list;
	}
	
	/**
	 * This test checks that the number of sales scheduled matches
	 * the resulting sales from the agents / population relationships.
	 */
	@Test
	public void testTotalSales() {
		
		double totalSales = 0;
		for (int i=0; i<seasonality.length; i++) {
			totalSales+=seasonality[i];
		}
		
		int totalSegmentSales = MatrixFunctions.addMatrix(totalSalesByBrandBySegment);

		boolean result = Functions.equals(
				totalSales, 
				totalSegmentSales, 
				ratio*0.5) 
				&& salesLeft<=ratio*0.5;
		if(!result) {
			System.out.println("ERROR:: Total Sales test failed!");
			System.out.println("Found: "+totalSegmentSales+" sales, expected "+totalSales);
			System.out.println("Seasonality: "+ArrayFunctions.arrayToString(seasonality));
			System.out.println("Seed value: "+RandomizerUtils.PRIME_SEEDS[iteration]);
			System.out.println("Sales left equals: "+salesLeft+"\n");
			System.out.println("Zio File: "+scenario+"\n");
		}
		assertTrue(result);
	}
	
	/**
	 * The test is concerned with ensuring that decision cycle restrictions
	 * are respected. This is done checking if the number of steps between
	 * purchases made by the same agent are at least equal to the decision
	 * cycle parameter. 
	 */
	@Test
	public void testSchedule() {
		int steps = schedule.length;
		for (int i=0; i<steps; i++) {
			BitSet b = schedule[i];
			for (int j=i+1; j<steps; j++) {				
				BitSet c = schedule[j];
				boolean intersection = b.intersects(c);
				if(intersection) {
					if(j-i<decisionCycle) {
						BitSet bc = (BitSet) b.clone();
						bc.and(c);
						System.out.println("ERROR:: Disabled check failed!");
						System.out.println("AgentID: "+bc.toString()
								+" is buying while being disabled at step "+j);
						System.out.println("Zio File: "+scenario+"\n");
						fail();
					}					
				}
			}
		}
	}
}
