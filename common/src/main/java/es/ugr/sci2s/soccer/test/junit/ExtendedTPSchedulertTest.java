package es.ugr.sci2s.soccer.test.junit;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import es.ugr.sci2s.soccer.beans.SimulationConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.gson.Gson;

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
import model.touchpoints.TouchPointOwned.InvestmentType;
import test.junit.TestTPScheduler;
import util.functions.Functions;
import util.functions.MatrixFunctions;
import util.random.RandomizerUtils;

@RunWith(Parameterized.class)
public class ExtendedTPSchedulertTest {

	String bbva = "test/Revised_CV2_wrapped.json";
	
	SimulationConfig sc;
	
	private Model m; 
	
	private int nrAgents;
	private int numSegments;
	private ClientSegments segments;
	private Agent[] agents;
	private byte[][][] schedule;
	private TIntArrayList[][] reached;
	private byte[][] agentsBySegment;
	private int numSchedulers;
	private TouchPointScheduler[] tps;
	
	public ExtendedTPSchedulertTest(Integer iteration) throws IOException {
		
		//Read JSON files
		BufferedReader br = new BufferedReader(new FileReader(bbva));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String stringConfig = buffer.toString();
		
		//Get Values
		Gson gson = new Gson();
		
		sc = gson.fromJson(stringConfig, SimulationConfig.class);
		
		ModelDefinition md = sc.getModelDefinition();
		
		md.setDebug(true);
		
		double[][][][] grp = md.getGRP();
		md.setGRP(TestTPScheduler.truncateGRP(grp));

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
	
	private int getAgentsHitAtSegment(int schedule,int segmentId) {
		return reached[schedule][segmentId].size();
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		
		final int ITERATIONS=30;
		
		ArrayList<Object[]> list = new ArrayList<Object[]>();
	
		for (int i=0; i<ITERATIONS; i++) {
			Object[] otherPath = {i};
			list.add(otherPath);
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
				System.out.println("Test failed: NotAssigned for file "+bbva);
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
			double[][] marketingPlans = MatrixFunctions.truncateMatrix(
					tps[scheduler].getPlan(), 100);
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
				grpGoal = Math.round(totalGRPbySegment[i]);
				grpAchieved = Math.round(hitPerSegments[i] / onepercent);

//				result &= grpGoal == grpAchieved;
				
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
		
		InvestmentType[] investment = sc.getTouchPointsInvestment();
		
		for (int scheduler = 0; scheduler<numSchedulers; scheduler++) {
			
			double onepercent = nrAgents*0.01;
			double[][] marketingPlans = MatrixFunctions.truncateMatrix(
					tps[scheduler].getPlan(), 100);
			int totalHits=0;
			double totalInvestment=0;
			
			for (int i=0; i< marketingPlans.length; i++) {
				for (int j=0; j<marketingPlans[i].length; j++) {
					totalInvestment+=marketingPlans[i][j];
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
			double investedHits  = Math.round(totalInvestment*onepercent);
			
			boolean result = Functions.equals(
					totalHits,
					investedHits,
					Functions.DOUBLE_EQUALS_DELTA
					);
			
			if(result==false) {
				System.out.println("DEBUG:: HITS : "+totalHits
						+":: "+investment[scheduler].toString()+
							" PLANNED: "+investedHits);
				System.out.println("DEBUG:: PLANNED HITS LOST: "+
							((investedHits-totalHits)/(double)investedHits)*100.0+"%");
			}
			
			success &= result;
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

}
