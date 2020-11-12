package test.junit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import model.decisionmaking.DecisionMaking;
import util.exception.simulation.NoAwarenessException;
import util.random.Randomizer;
import util.random.RandomizerFactory;

@RunWith(Parameterized.class)
public class TestDecisionMaking {
	private static final int ITERATIONS = 100;
	
	static final int NR_SEGMENTS = 1;
	static final int SEGMENT_ID = 0;
	static final int NR_ATTRIBUTES = 3;
	static final int NR_BRANDS = 4;
	
	double involved;
	double nonInvolved;
	double emotional;
	double nonEmotional;
	boolean[] awareness;
	double[][] perceptions;
	double[][] drivers;
	DecisionMaking dm;
	
	Randomizer r;
	
	public TestDecisionMaking(
		int seed, double[] drivers, double[][] perceptions, 
		double involved, double nonInvolved,
		double emotional, double nonEmotional
	) {
		r = RandomizerFactory.createDefaultRandomizer(seed);
		boolean[] awareness = new boolean[perceptions.length];
		Arrays.fill(awareness, true);
		this.drivers = new double[NR_SEGMENTS][drivers.length];
		this.drivers[SEGMENT_ID] = drivers;
		
		this.involved = involved;
		this.nonInvolved = nonInvolved;
		this.emotional = emotional;
		this.nonEmotional = nonEmotional;
		this.awareness = awareness;
		this.perceptions = perceptions;

		dm = new DecisionMaking(r, this.drivers, involved, nonInvolved, emotional, nonEmotional, NR_ATTRIBUTES, NR_BRANDS);
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> list = new ArrayList<Object[]>();
		for (int i=0; i<ITERATIONS; i++) {
			// check majority rule (involved and emotional)
			Object[] data= new Object[] {
				i,
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {9.0, 9.0, 9.0}, {3.0, 4.0, 4.0}, {2.0, 7.0, 5.0}},
				1,
				0,
				1,
				0
			};
			list.add(data);

			// check elimination by aspects (non-involved and non-emotional)
			data= new Object[] {
				i,
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {9.0, 9.0, 9.0}, {3.0, 4.0, 4.0}, {2.0, 7.0, 5.0}},
				0,
				1,
				0,
				1
			};
			list.add(data);
			
			// satisficing
			data= new Object[] {
				i,
				new double[]{0.6, 0.1, 0.3},
				new double[][]{{1.0, 2.0, 3.0}, {9.0, 9.0, 9.0}, {3.0, 4.0, 4.0}, {2.0, 7.0, 5.0}},
				0,
				1,
				1,
				0
			};
			list.add(data);
			
			// utility maximization
			data= new Object[] {
				i,
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {9.0, 9.0, 9.0}, {3.0, 4.0, 4.0}, {2.0, 7.0, 5.0}},
				1,
				0,
				0,
				1
			};
			list.add(data);
			
			data= new Object[] {
					i,
					new double[]{0.5, 0.3, 0.2},
					new double[][]{{1.0, 2.0, 3.0}, {9.0, 9.0, 9.0}, {3.0, 4.0, 4.0}, {9.0, 9.0, 8.0}},
					1,
					0,
					0,
					1
			};
			list.add(data);	
			data= new Object[] {
					i,
					new double[]{0.5, 0.4, 0.1},
					new double[][]{{2.0, 4.0, 5.0}, {4.0, 4.0, 4.0}, {2.0, 2.0, 6.0}, {9.0, 9.0, 8.0}},
					1,
					0,
					0,
					1
			};
			list.add(data);					
		}
		return list;
	}
	
	@Test
	public void testSelectHeuristic() {
		int heuristic;
		int expectedHeuristic = 0;
		boolean correctHeuristic = true;	
		
		heuristic = dm.selectHeuristic();
		if(involved == 1) {
			if(emotional == 1) {
				expectedHeuristic = 1;
			} else if(nonEmotional == 1) {
				expectedHeuristic = 0;
			}	
		} else if (nonInvolved == 1) {
			if(emotional == 1) {
				expectedHeuristic = 3;
			} else if(nonEmotional == 1) {
				expectedHeuristic = 2;
			}			
		}
			
		if(heuristic != expectedHeuristic) {
			correctHeuristic = false;
			System.out.println("ERROR: heuristic: " + heuristic + " expected heuristic: " + expectedHeuristic);
		}		
		assertTrue(correctHeuristic);
	}
	
	@Test
	public void testAwarenessFilter() {
		
		int elements = awareness.length;
		
		for (int i=0; i<ITERATIONS; i++) {
			boolean [] newAwareness = newBooleanArray(elements);
			
			int brand;
			try {
				brand = dm.buyOneBrand(
						newAwareness, perceptions, SEGMENT_ID);
				if(!newAwareness[brand]) {
					fail("Failed at iteration "+i+"\nBrand "+brand
							+" Awareness "+Arrays.toString(newAwareness) 
							+ " | involved: " + involved
							+ " | emotional: " + emotional
							+ " | nonInvolved: " + nonInvolved
							+ " | nonEmotional: " + nonEmotional);
				}
			} catch (NoAwarenessException e) {
				if (BooleanUtils.or(newAwareness)) {
					fail("Excention thrown with existing awareness");
				}
			}		
		}
	}
	
	private boolean [] newBooleanArray(int length) {
		boolean [] array = new boolean [length];
		for (int i=0; i<length; i++) {
			array[i]=r.nextBoolean();
		}
		return array;
	}
}
