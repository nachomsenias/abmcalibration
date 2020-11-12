package test.junit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import gnu.trove.list.array.TIntArrayList;
import model.ClientSegments;
import model.Model;
import model.ModelBean;
import model.ModelBuilder;
import model.ModelDefinition;
import model.customer.Agent;
import model.sales.SalesScheduler;
import model.touchpoints.TouchPointOwnedRegistry;
import model.touchpoints.TouchPointScheduler;
import util.exception.sales.SalesScheduleError;
import util.functions.Functions;
import util.random.RandomizerUtils;

/**
 * Unit test defined for TouchPointScheduler class using the JUnit
 * framework.
 * 
 * Its tests contains: 
 * 		* exposure assignment checking
 * 		* GRP distribution testing
 * 		* Reach checking. 
 * 
 * @author imoya
 *
 */
@RunWith(Parameterized.class)
public class TestTPScheduler {

	private Model m;
	
	private String path; 
	
	private int nrAgents;
	private int numSegments;
	private ClientSegments segments;
	private Agent[] agents;
	private byte[][][] schedule;
	private TIntArrayList[][] reached;
	private byte[][] agentsBySegment;
	private int numSchedulers;
	private TouchPointScheduler[] tps;
	
	/**
	 * Some models have GRP values that can't be successfully hit with a certain 
	 * population size. For example, if the agent population equals 100, 10.1 GRP
	 * may not be properly applied.
	 *  
	 * @param grp - not truncated GRP values.
	 * @return truncated GRP values
	 */
	public static double[][][][] truncateGRP(double[][][][] grp) {
		
		double[][][][] finalGRP;
		int touchpoint = grp.length;
		finalGRP = new double[touchpoint][][][];
		
		for (int i=0; i<touchpoint; i++) {
			
			int brands = grp[i].length;
			finalGRP[i] = new double[brands][][];			
			
			for (int j=0; j<brands; j++) {
				
				int segments = grp[i][j].length;
				finalGRP[i][j] = new double [segments][];
				
				for (int k=0; k<segments; k++) {
					
					int steps = grp[i][j][k].length;
					finalGRP[i][j][k] = new double [steps];
					
					for (int s=0; s<steps; s++) {
						int grpint = (int)(grp[i][j][k][s]*100);
						finalGRP[i][j][k][s]=grpint*0.01;
					}
				}
			}
		}
		
		return finalGRP;
	}
	
	private int getAgentsHitAtSegment(int schedule,int segmentId) {
		return reached[schedule][segmentId].size();
	}

	public TestTPScheduler(
			String path,
			Integer iteration,
			Boolean weekly
			) throws SalesScheduleError {
		
		this.path = path;
		ModelDefinition md = new ModelDefinition();
		md.loadValuesFromFile(new File(path));
		md.setDebug(true);
		
		double[][][][] grp = md.getGRP();
		md.setGRP(truncateGRP(grp));
		

		if(!weekly) {
			md.setStepsForWeek(Model.DAYS_OF_WEEK);
		}

		ModelBuilder mb = md.createBuilder();
		ModelBean bean = mb.createBean();
		m = mb.build(bean, RandomizerUtils.PRIME_SEEDS[iteration]);
		
		SalesScheduler ss = m.getSalesScheduler();
		ss.enableTest();
		
		nrAgents = md.getNumberOfAgents();
		numSegments = md.getNumberOfSegments();
		
		TouchPointOwnedRegistry tpor = m.getTPORegistry();
		
		segments=m.getSegments();
		agents = m.getAgents();
		tps = new TouchPointScheduler[0];		
		tps = tpor.getSchedulers().toArray(tps);
		
		numSchedulers = tps.length;
		
		schedule = new byte[numSchedulers][][];
		reached = new TIntArrayList[numSchedulers][];
		agentsBySegment = new byte[numSchedulers][];
		
		for (int i=0; i<numSchedulers; i++) {
			TouchPointScheduler scheduler = tps[i];
			
			schedule[i] = scheduler.getSchedule();
			reached[i] = scheduler.getAgentsReached();
			agentsBySegment[i] = scheduler.getAgentsAtSegment();
		}
	}
	
	
	@Parameters
	public static Collection<Object[]> data() {
		
		final int ITERATIONS=10;
		
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		
		String directoryPath = "./test/tpscheduler";
		
		File directory = new File(directoryPath);
		
		for (File f:directory.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(".zio");
				}
			})) {		
			for (int i=0; i<ITERATIONS; i++) {
				String path = f.getPath();
				Object[] filePath = {path,i,new Boolean(false)};
				list.add(filePath);

				Object[] otherPath = {path,i,new Boolean(true)};
				list.add(otherPath);
			}
		}
		
		return list;
	}

	/**
	 * The purpose of this test is to verify that the agents reached have
	 * hits noted.
	 */
	@Test
	public void testAssigned() {
		
		boolean success = true;
		
		for (int scheduler = 0; scheduler<numSchedulers; scheduler++) {
			int count = 0;

			for (int i=0; i<numSegments; i++) {
				for (int j=0; j < nrAgents; j++) {
					if(reached[scheduler][i].contains(j)) count++;
				}
			}

			int countSchedule=0;
			for (int i=0; i<nrAgents; i++) {
				for (int j = 0; j < schedule[scheduler][i].length; j++) {
					if (schedule[scheduler][i][j] != 0) {
						countSchedule++;
						break;
					}
				}
			}
			success &= count==countSchedule;
		}
		
		assertTrue(success);
	}
	
	/**
	 * The purpose of this test is to verify that the agents reached have
	 * hits noted. => Alternate approach
	 */
	@Test
	public void testAlternateAssigned() {
		
		boolean success = true;
		
		for (int scheduler = 0; scheduler<numSchedulers; scheduler++) {
			int count = 0;
			for (int i=0; i<numSegments; i++) {
				count+=reached[scheduler][i].size();
			}
			
			int countSchedule=0;
			for (int i=0; i<nrAgents; i++) {
				for (int j = 0; j < schedule[scheduler][i].length; j++) {
					if (schedule[scheduler][i][j] != 0) {
						countSchedule++;
						break;
					}
				}
			}
			success &= count==countSchedule;
		}
		
		assertTrue(success);
	}

	/**
	 * The purpose of this test is to verify that not reached agents have no
	 * hits noted.
	 */
	@Test
	public void testNotAssigned() {
		
		boolean success = true;
		
		for (int scheduler = 0; scheduler<numSchedulers; scheduler++) {
			int count = 0;
			for (int i = 0; i < nrAgents; i++) {
				boolean equalsZero = true;
				for (int j = 0; j < schedule[scheduler][i].length; j++) {
					if (schedule[scheduler][i][j] != 0) {
						equalsZero = false;
						break;
					}
				}
				if (equalsZero) {
					count++;
				}
			}
			
			int countNotReached=0;
			for (int segment=0; segment<numSegments; segment++) {
				for (int i=0; i < nrAgents; i++) {
					if(!reached[scheduler][segment].contains(i)
							&& agentsBySegment[scheduler][i]==segment) countNotReached++;
				}
			}
			
			success &= count == countNotReached;
			if(!success) {
				System.out.println("Test failed: NotAssigned for file "+path);
			}
		}
		
		assertTrue(success);
	}
	
	/**
	 * The purpose of this test is to verify that not reached agents have no
	 * hits noted. => Alternate version
	 */
	@Test
	public void testAlternateNotAssigned() {
		
		boolean success = true;
		
		for (int scheduler = 0; scheduler<numSchedulers; scheduler++) {
			int count = 0;
			for (int i = 0; i < nrAgents; i++) {
				boolean equalsZero = true;
				for (int j = 0; j < schedule[scheduler][i].length; j++) {
					if (schedule[scheduler][i][j] != 0) {
						equalsZero = false;
						break;
					}
				}
				if (equalsZero) {
					count++;
				}
			}
			
			int countNotReached=nrAgents;
			for (int segment=0; segment<numSegments; segment++) {
				countNotReached-=reached[scheduler][segment].size();
			}
			
			success &= count == countNotReached;
		}
		assertTrue(success);
	}

	/**
	 * This test checks if resulting hits dispatched to the agents 
	 * actually fit the GRP intended.
	 */
	@Test
	public void testGRPCorrectlyAssigned() {
		
		boolean success = true;
		
		for (int scheduler = 0; scheduler<numSchedulers; scheduler++) {
			
			double onepercent = nrAgents*0.01;
			int numSegments = segments.getNumSegments();
			int[] hitPerSegments = new int[numSegments];
			int[] agentsHitBySegment = new int[numSegments];
	
			for (int i = 0; i < agents.length; i++) {
				assert(i == agents[i].clientId);
				double grp = 0.0;
				for (int j = 0; j < schedule[scheduler][i].length; j++) {
					grp += schedule[scheduler][i][j];
				}
				int segmentid= agents[i].segmentId;
				
				hitPerSegments[segmentid] += grp;
				
				if (reached[scheduler][segmentid].contains(i)) {
					agentsHitBySegment[segmentid]++;
				}
			}
			
			// How many GRP where planned?
			double[][] marketingPlans = tps[scheduler].getPlan();
			double[] totalGRPbySegment = new double[numSegments];
			for (int i = 0; i < marketingPlans.length; i++)
				for (int j = 0; j < marketingPlans[i].length; j++) {
					totalGRPbySegment[i] += marketingPlans[i][j];
				}
			/*
			 * What was the effective reach?
			 */
			boolean result = true;
			for (int i = 0; i < numSegments; i++) {
				double grpGoal, grpAchieved;
				grpGoal = totalGRPbySegment[i];
				grpAchieved = hitPerSegments[i] / onepercent;

				result &= Functions.equals(
						grpGoal,grpAchieved,Functions.DOUBLE_EQUALS_DELTA);
				
				if(result==false) {
					System.out.println("DEBUG:: GPR : "+grpGoal+" EFFECTIVE GRP: "+grpAchieved);
				}
			}
			success &= result;
		}
		assertTrue(success);
	}
	
	/**
	 * This method checks GRP using a bottom up approach: hits scheduled 
	 * are counted and then the final amount is compared to the GRPs. 
	 */
	@Test
	public void testGRPBottomUP() {
		
		boolean success = true;
		
		for (int scheduler = 0; scheduler<numSchedulers; scheduler++) {
			
			double onepercent = nrAgents*0.01;
			double[][] marketingPlans = tps[scheduler].getPlan();
			int totalHits=0;
			double grpPlanned=0;
			
			for (int i=0; i< marketingPlans.length; i++) {
				for (int j=0; j<marketingPlans[i].length; j++) {
					grpPlanned+=marketingPlans[i][j];
				}
			}
			
			for (int i=0; i< schedule[scheduler].length; i++) {
				for (int j=0; j<schedule[scheduler][i].length; j++) {
					totalHits+=schedule[scheduler][i][j];
				}
			}
			/*
			 * The total number of impacts depends on the
			 * total population and the total number of GRP.
			 */
			grpPlanned*=onepercent;
			
			success &= Functions.equals(
					totalHits,
					grpPlanned,
					Functions.DOUBLE_EQUALS_DELTA
					);
			
			if(success==false) {
				System.out.println("DEBUG:: HITS : "+totalHits
						+" GRP PLANNED: "+grpPlanned);
			}
		}
		assertTrue(success);
	}
	
	/**
	 * The purpose of this test is to ensure that the agents that
	 * should be reached according to its reach parameter, are
	 * effectively reached.
	 */
	@Test
	public void testSegmentReach() {
		
		boolean success = true;
		
		for (int scheduler = 0; scheduler<numSchedulers; scheduler++) {
			
			int numSegments = segments.getNumSegments();
			boolean result=true;
			for(int i=0; i<numSegments;i++) {
				int agentsOfSegmentFound=getAgentsHitAtSegment(scheduler,i);
				double agentsAtSegment = nrAgents * segments.getSegmentSize(i) * tps[scheduler].getActualRM()[i]; 
				result&=Functions.equals(
						(double)agentsOfSegmentFound,
						agentsAtSegment, 
						Functions.DOUBLE_EQUALS_DELTA
						);
			}
			success &= result;
		}
		
		assertTrue(success);
	}
	
	/**
	 * The purpose of this test is ensuring that the Actual Reach 
	 * parameter is calculated correctly.
	 */
	@Test
	public void testReachValue() {
		
		boolean success = true;
		
		for (int scheduler = 0; scheduler<numSchedulers; scheduler++) {
			
			int numSegments = segments.getNumSegments();
			boolean result=true;
			
			for(int i=0; i<numSegments;i++) {
				
				int agentsOfSegmentFound=getAgentsHitAtSegment(scheduler,i);
				double percentAgentsReached=(double)agentsOfSegmentFound/((double) nrAgents * segments.getSegmentSize(i));
				
				result&=Functions.equals(
						tps[scheduler].getActualRM()[i],
						percentAgentsReached,
						Functions.DOUBLE_EQUALS_DELTA
					);
			}
			success &= result;
		}
		assertTrue(success);
	}
	
	/**
	 * This test runs the simulation in order to detect errors.
	 */
	@Test
	public void runTest() {
		try {
			m.runSilent();
		} catch (SalesScheduleError e) {
			fail();
		}
	}
}
