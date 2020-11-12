package test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import model.decisionmaking.DecisionMaking;
import util.exception.simulation.NoAwarenessException;
import util.functions.Functions;
import util.random.Randomizer;
import util.random.RandomizerFactory;

/**
 * Provides an experiment of the decision making module. It tests all the four
 * heuristics (utility maximization, majority rule, elimination-by-aspects,
 * and satisficing). Those heuristics are tested in two different environments:
 * 1.) in simulation with three brands and three attributes and 2.) in simulation
 * considering four brands and 5 attributes. Several different scenarios to test
 * the heuristics are provided (e.g. one brand with really high attributes).
 */
public class DecisionMakingHeuristics {
	private static final int ITERATIONS = 1000;
	private static final int NR_BRANDS_EXP_1 = 3;
	private static final int NR_ATTRIBUTES_EXP_1 = 3;
	private static final int NR_BRANDS_EXP_2 = 4;
	private static final int NR_ATTRIBUTES_EXP_2 = 5;
	private static final int NR_BRANDS_EXP_PELAYO = 7;
	private static final int NR_ATTRIBUTES_EXP_PELAYO = 4;	
	private static final int EXPERIMENT_1 = 1;
	private static final int EXPERIMENT_2 = 2;
	private static final int EXPERIMENT_PELAYO = 3;
	
	private static final String FILE_NAME_EXP_1 
		= "../experiments/decisionMaking/parallel/heuristicsExperiment.txt";
	private static final String FILE_NAME_EXP_2 
		= "../experiments/decisionMaking/parallel/heuristicsExperimentLarge.txt";
	private static final String FILE_NAME_EXP_PELAYO 
	= "../experiments/decisionMaking/parallel/heuristicsExperimentPelayo.txt";
	
	private static final String FILE_NAME_EXP_PELAYO_PERCEPTIONS 
	= "../experiments/decisionMaking/parallel/heuristicsExperimentPelayo";
	
	private static final int NR_OF_HEURISTICS = 4;
	private static final String[] HEURISTIC_NAMES 
		= {"utility_maximization", "majority_rule", "elimination_by_aspects", "satisficing"};
	
	private int[][][] results;
	
	// ########################################################################
	// Constructors
	// ######################################################################## 	
	
	public DecisionMakingHeuristics() {
		this.results = null;
	}

	// ########################################################################	
	// Methods/Functions 	
	// ########################################################################
	
	private static Collection<Object[]> data1() {
		List<Object[]> list = new ArrayList<Object[]>();
			Object[] data;
			data= new Object[] {
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {9.0, 9.0, 9.0}, {2.0, 7.0, 5.0}},
				new double[]{7.0, 7.0, 7.0},
			};
			list.add(data);
			
			data= new Object[] {
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {8.0, 8.0, 8.0}, {2.0, 7.0, 5.0}},
				new double[]{7.0, 7.0, 7.0},
				};
			list.add(data);
			
			data= new Object[] {
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {7.0, 7.0, 7.0}, {2.0, 7.0, 5.0}},
				new double[]{7.0, 7.0, 7.0},
				};
			list.add(data);
			
			data= new Object[] {
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {6.0, 6.0, 6.0}, {2.0, 7.0, 5.0}},
				new double[]{7.0, 7.0, 7.0},
				};
			list.add(data);			
			
			data= new Object[] {
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {9.0, 9.0, 9.0}, {9.0, 9.0, 8.0}},
				new double[]{7.0, 7.0, 7.0},
			};
			list.add(data);	
			
			data= new Object[] {
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {8.0, 8.0, 8.0}, {9.0, 9.0, 8.0}},
				new double[]{7.0, 7.0, 7.0},
			};
			list.add(data);	

			data= new Object[] {
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {7.0, 7.0, 7.0}, {9.0, 9.0, 8.0}},
				new double[]{7.0, 7.0, 7.0},
			};
			list.add(data);	
			
			data= new Object[] {
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {6.0, 6.0, 6.0}, {9.0, 9.0, 8.0}},
				new double[]{7.0, 7.0, 7.0},
			};
			list.add(data);
			
			/*data= new Object[] {
				new double[]{0.5, 0.3, 0.2},
				new double[][]{{1.0, 2.0, 3.0}, {5.0, 5.0, 5.0}, {9.0, 9.0, 8.0}},
				new double[]{7.0, 7.0, 7.0},
			};
			list.add(data);			
			
			data= new Object[] {
				new double[]{0.42, 0.33, 0.25},
				new double[][]{{2.0, 4.0, 5.0}, {4.0, 4.0, 4.0}, {9.0, 9.0, 8.0}},
				new double[]{2.0, 2.0, 2.0},
			};
			list.add(data);*/
			
			data= new Object[] {
				new double[]{0.4, 0.35, 0.25},
				new double[][]{{4.0, 2.0, 5.0}, {4.0, 4.0, 4.0}, {2.0, 2.0, 6.0}},
				new double[]{1.0, 1.0, 1.0},
			};
			list.add(data);
			
			data= new Object[] {
				new double[]{0.6, 0.25, 0.15},
				new double[][]{{4.0, 2.0, 5.0}, {4.0, 4.0, 4.0}, {2.0, 2.0, 6.0}},
				new double[]{1.0, 1.0, 1.0},
			};
			list.add(data);
			
			data= new Object[] {
				new double[]{0.8, 0.15, 0.05},
				new double[][]{{4.0, 2.0, 5.0}, {4.0, 4.0, 4.0}, {2.0, 2.0, 6.0}},
				new double[]{1.0, 1.0, 1.0},
			};
			list.add(data);			
			
			data= new Object[] {			
				new double[]{0.333, 0.333, 0.333},
				new double[][]{{9.0, 6.0, 5.0}, {7.0, 4.0, 8.0}, {5.0, 7.0, 7.0}},
				new double[]{5.0, 7.0, 7.0},
			};
			list.add(data);
			
			data= new Object[] {
				new double[]{0.692, 0.154, 0.154},
				new double[][]{{7.0, 4.0, 5.0}, {2.0, 7.0, 8.0}, {8.0, 3.0, 2.0}},
				new double[]{8.0, 2.0, 5.0},
			};
			list.add(data);

			data= new Object[] {
				new double[]{0.444, 0.333, 0.223},
				new double[][]{{6.0, 5.0, 4.0}, {5.0, 4.0, 6.0}, {4.0, 5.0, 6.0}},
				new double[]{4.0, 4.0, 4.0},
			};
			list.add(data);
			
			data= new Object[] {
				new double[]{0.444, 0.333, 0.223},
				new double[][]{{6.0, 5.0, 1.0}, {4.0, 1.0, 6.0}, {1.0, 4.0, 6.0}},
				new double[]{4.0, 4.0, 4.0},
			};
			list.add(data);				
			
			data= new Object[] {
				new double[]{0.444, 0.333, 0.223},
				new double[][]{{8.0, 7.0, 1.0}, {2.0, 1.0, 3.0}, {1.0, 2.0, 3.0}},
				new double[]{4.0, 4.0, 4.0},
			};
			list.add(data);
			
			data= new Object[] {			
				new double[]{0.333, 0.333, 0.333},
				new double[][]{{10.0, 10.0, 10.0}, {10.0, 10.0, 10.0}, {10.0, 10.0, 10.0}},
				new double[]{10.0, 10.0, 10.0},
			};
			list.add(data);	
			
			data= new Object[] {				
				new double[]{0.333, 0.333, 0.333},
				new double[][]{{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}},
				new double[]{0.0, 0.0, 0.0},
			};
			list.add(data);	
			/*data= new Object[] {
				new double[]{0.5, 0.2, 0.1, 0.1, 0.1},
				new double[][]{{1.0, 2.0, 3.0, 3.0, 3.0}, {9.0, 9.0, 9.0, 9.0, 9.0}, {9.0, 9.0, 8.0, 9.0, 9.0}},
				new double[]{7.0, 7.0, 7.0, 7.0, 7.0},
			};
			list.add(data); */		
		return list;
	}
	
	private static Collection<Object[]> data2() {
		List<Object[]> list = new ArrayList<Object[]>();
			Object[] data;
			data= new Object[] {
				new double[]{0.2, 0.2, 0.2, 0.2, 0.2},
				new double[][]{{1.0, 2.0, 3.0, 3.0, 3.0}, {9.0, 9.0, 9.0, 9.0, 9.0}, {2.0, 7.0, 5.0, 6.0, 4.0}, {7.0, 6.0, 5.0, 4.0, 3.0}},
				new double[]{7.0, 7.0, 7.0, 7.0, 7.0},
			};
			list.add(data);
			
			data= new Object[] {
				new double[]{0.2, 0.2, 0.2, 0.2, 0.2},
				new double[][]{{1.0, 2.0, 3.0, 3.0, 3.0}, {8.0, 8.0, 8.0, 8.0, 8.0}, {2.0, 7.0, 5.0, 6.0, 4.0}, {7.0, 6.0, 5.0, 4.0, 3.0}},
				new double[]{7.0, 7.0, 7.0, 7.0, 7.0},
			};
			list.add(data);		
				
			data= new Object[] {
				new double[]{0.2, 0.2, 0.2, 0.2, 0.2},
				new double[][]{{1.0, 2.0, 3.0, 3.0, 3.0}, {7.0, 7.0, 7.0, 7.0, 7.0}, {2.0, 7.0, 5.0, 6.0, 4.0}, {7.0, 6.0, 5.0, 4.0, 3.0}},
				new double[]{7.0, 7.0, 7.0, 7.0, 7.0},
			};
			list.add(data);	
			
			data= new Object[] {
				new double[]{0.2, 0.2, 0.2, 0.2, 0.2},
				new double[][]{{1.0, 2.0, 3.0, 3.0, 3.0}, {6.0, 6.0, 6.0, 6.0, 6.0}, {2.0, 7.0, 5.0, 6.0, 4.0}, {7.0, 6.0, 5.0, 4.0, 3.0}},
				new double[]{7.0, 7.0, 7.0, 7.0, 7.0},
			};
			list.add(data);	
				
			data= new Object[] {
				new double[]{0.2, 0.2, 0.2, 0.2, 0.2},
				new double[][]{{1.0, 2.0, 3.0, 3.0, 3.0}, {9.0, 9.0, 9.0, 9.0, 9.0}, {9.0, 9.0, 8.0, 9.0, 9.0}, {4.0, 1.0, 6.0, 3.0, 6.0}},
				new double[]{7.0, 7.0, 7.0, 7.0, 7.0},
			};
			list.add(data);
			
			data= new Object[] {
				new double[]{0.2, 0.2, 0.2, 0.2, 0.2},
				new double[][]{{1.0, 2.0, 3.0, 3.0, 3.0}, {8.0, 8.0, 8.0, 8.0, 8.0}, {9.0, 9.0, 8.0, 9.0, 9.0}, {4.0, 1.0, 6.0, 3.0, 6.0}},
				new double[]{7.0, 7.0, 7.0, 7.0, 7.0},
			};
			list.add(data);			

			data= new Object[] {
				new double[]{0.2, 0.2, 0.2, 0.2, 0.2},
				new double[][]{{1.0, 2.0, 3.0, 3.0, 3.0}, {7.0, 7.0, 7.0, 7.0, 7.0}, {9.0, 9.0, 8.0, 9.0, 9.0}, {4.0, 1.0, 6.0, 3.0, 6.0}},
				new double[]{7.0, 7.0, 7.0, 7.0, 7.0},
			};
			list.add(data);			

			data= new Object[] {
				new double[]{0.2, 0.2, 0.2, 0.2, 0.2},
				new double[][]{{1.0, 2.0, 3.0, 3.0, 3.0}, {6.0, 6.0, 6.0, 6.0, 6.0}, {9.0, 9.0, 8.0, 9.0, 9.0}, {4.0, 1.0, 6.0, 3.0, 6.0}},
				new double[]{7.0, 7.0, 7.0, 7.0, 7.0},
			};
			list.add(data);
	
			data= new Object[] {
				new double[]{0.2, 0.2, 0.2, 0.2, 0.2},
				new double[][]{{6.0, 5.0, 4.0, 4.0, 7.0}, {5.0, 4.0, 6.0, 5.0, 5.0}, {4.0, 5.0, 6.0, 6.0, 4.0}, {5.0, 2.0, 8.0, 5.0, 5.0}},
				new double[]{6.0, 6.0, 6.0, 6.0, 6.0},
			};
			list.add(data);
			
			data= new Object[] {
				new double[]{0.2, 0.2, 0.2, 0.2, 0.2},
				new double[][]{{6.0, 5.0, 3.0, 3.0, 7.0}, {5.0, 4.0, 6.0, 5.0, 5.0}, {4.0, 5.0, 6.0, 6.0, 4.0}, {5.0, 2.0, 8.0, 5.0, 5.0}},
				new double[]{6.0, 6.0, 6.0, 6.0, 6.0},
			};
			list.add(data);
			
			data= new Object[] {
				new double[]{0.2, 0.2, 0.2, 0.2, 0.2},
				new double[][]{{6.0, 5.0, 2.0, 2.0, 7.0}, {5.0, 4.0, 6.0, 5.0, 5.0}, {4.0, 5.0, 6.0, 6.0, 4.0}, {5.0, 2.0, 8.0, 5.0, 5.0}},
				new double[]{6.0, 6.0, 6.0, 6.0, 6.0},
			};
			list.add(data);			
			
			data= new Object[] {
				new double[]{0.4, 0.15, 0.15, 0.15, 0.15},
				new double[][]{{5.0, 5.0, 5.0, 5.0, 5.0}, {4.0, 2.0, 6.0, 9.0, 1.0}, {4.0, 7.0, 5.0, 6.0, 4.0}, {7.0, 6.0, 5.0, 4.0, 3.0}},
				new double[]{5.0, 5.0, 5.0, 5.0, 5.0},
			};
			list.add(data);			

			data= new Object[] {
				new double[]{0.6, 0.1, 0.1, 0.1, 0.1},
				new double[][]{{5.0, 5.0, 5.0, 5.0, 5.0}, {4.0, 2.0, 6.0, 9.0, 1.0}, {4.0, 7.0, 5.0, 6.0, 4.0}, {7.0, 6.0, 5.0, 4.0, 3.0}},
				new double[]{5.0, 5.0, 5.0, 5.0, 5.0},
			};
			list.add(data);

			data= new Object[] {
				new double[]{0.8, 0.05, 0.05, 0.05, 0.05},
				new double[][]{{5.0, 5.0, 5.0, 5.0, 5.0}, {4.0, 2.0, 6.0, 9.0, 1.0}, {4.0, 7.0, 5.0, 6.0, 4.0}, {7.0, 6.0, 5.0, 4.0, 3.0}},
				new double[]{5.0, 5.0, 5.0, 5.0, 5.0},
			};
			list.add(data);
		return list;
	}	

	/**
	 * Returns the real data of the market obtained from Pelayo
	 * There are 4 drivers:
	 * 1. Precio
	 * 2. Calidad de la oferta
	 * 3. Confianza
	 * 4. Facilidad en la gestion
	 * 
	 * There are 7 brands:
	 * 1. Allianz
	 * 2. Axa
	 * 3. Zurich
	 * 4. Linea Directa 
	 * 5. Mapfre
	 * 6. Mutua Madrileña
	 * 7. Pelayo
	 * @return - 
	 */
	private static Collection<Object[]> dataPelayo() {
		List<Object[]> list = new ArrayList<Object[]>();
		Object[] data;
		// Average from the whole 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.350, 2.584, 2.787, 2.662}, 
				{1.288, 2.349, 2.481, 2.487}, {0.856, 1.908, 2.216, 1.989}, 
				{3.464, 3.330, 3.036, 4.694}, {2.428, 4.762, 4.701, 4.644}, 
				{2.198, 3.264, 3.296, 3.326}, {1.227, 1.969, 1.957, 2.177} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);	
		
		// Average from January 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.389, 2.553, 2.813, 3.250}, 
				{1.107, 2.245, 2.371, 3.206}, {0.873, 1.877, 2.178, 2.418}, 
				{4.143, 3.857, 3.505, 6.028}, {2.501, 4.788, 5.105, 5.630}, 
				{2.076, 3.471, 3.461, 4.114}, {1.215, 1.823, 1.847, 2.664} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);	
	
		// Average from February 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.457, 2.666, 2.956, 3.320}, 
				{1.288, 2.130, 2.219, 2.843}, {0.749, 1.881, 2.043, 2.363}, 
				{3.653, 3.479, 3.175, 6.138}, {2.464, 5.096, 4.589, 5.393}, 
				{2.367, 3.767, 3.704, 4.278}, {0.998, 1.978, 1.869, 2.393} },
		new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);	
	
	// Average from March 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.559, 2.895, 3.154, 3.233}, 
				{1.269, 2.481, 2.529, 3.033}, {0.740, 1.724, 2.286, 2.267}, 
				{3.827, 3.700, 3.321, 6.027}, {2.243, 5.131, 5.050, 5.737}, 
				{2.611, 3.731, 3.608, 4.060}, {1.122, 2.077, 2.103, 2.343} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);	
	
		// Average from April 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.505, 2.787, 2.949, 2.668}, 
				{1.308, 2.320, 2.461, 2.394}, {0.955, 2.119, 2.299, 2.226}, 
				{3.397, 3.415, 3.033, 4.574}, {2.563, 4.928, 4.756, 4.479}, 
				{2.298, 3.323, 3.444, 3.353}, {1.433, 2.198, 2.157, 2.209} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);	

		// Average from May 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.423, 2.758, 2.919, 2.765}, 
				{1.324, 2.508, 2.589, 2.497}, {0.978, 2.106, 2.330, 1.961}, 
				{3.790, 3.644, 3.264, 4.815}, {2.706, 5.109, 4.986, 4.854}, 
				{2.521, 3.676, 3.651, 3.645}, {1.291, 2.046, 1.956, 2.327} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);	
	
		// Average from June 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.480, 2.585, 2.849, 2.383}, 
				{1.215, 2.263, 2.364, 2.325}, {1.031, 1.873, 2.248, 2.016}, 
				{3.648, 3.413, 3.186, 4.699}, {2.585, 5.124, 5.068, 4.803}, 
				{2.328, 3.295, 3.452, 3.140}, {1.093, 1.868, 1.865, 2.088} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);
	
		// Average from July 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.254, 2.494, 2.581, 2.496}, 
				{1.213, 2.228, 2.309, 2.234}, {0.874, 1.984, 2.269, 1.932}, 
				{3.280, 3.256, 2.899, 4.025}, {2.217, 4.378, 4.345, 4.132}, 
				{2.179, 3.098, 2.958, 3.029}, {1.215, 1.859, 1.895, 1.923} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);	
	
		// Average from August 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.254, 2.494, 2.581, 2.496}, 
				{1.213, 2.228, 2.309, 2.234}, {0.874, 1.984, 2.269, 1.932}, 
				{3.280, 3.256, 2.899, 4.025}, {2.217, 4.378, 4.345, 4.132}, 
				{2.179, 3.098, 2.958, 3.029}, {1.215, 1.859, 1.895, 1.923} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);	
	
		// Average from September 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.318, 2.605, 2.819, 2.365}, 
				{1.544, 2.664, 2.786, 2.406}, {0.922, 1.874, 2.106, 1.605}, 
				{3.163, 3.060, 2.649, 3.968}, {2.413, 4.549, 4.553, 4.153}, 
				{2.283, 3.320, 3.433, 3.179}, {1.352, 2.182, 2.181, 2.210} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);	
	
		// Average from October 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.312, 2.553, 2.726, 2.334}, 
				{1.392, 2.367, 2.566, 2.223}, {0.923, 1.982, 2.242, 1.878}, 
				{3.210, 3.073, 2.893, 4.130}, {2.540, 4.789, 4.712, 4.214}, 
				{1.889, 2.746, 2.934, 2.736}, {1.323, 1.976, 1.960, 2.033} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);		
	
		// Average from November 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.035, 2.220, 2.381, 2.210}, 
				{1.267, 2.408, 2.614, 2.216}, {0.638, 1.628, 2.164, 1.478}, 
				{3.164, 3.083, 2.988, 4.056}, {2.156, 4.238, 4.238, 3.949}, 
				{1.803, 2.935, 3.199, 2.721}, {1.358, 1.820, 1.910, 2.010} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);	

		// Average from December 2013 year
		data= new Object[] {
			new double[]{0.48, 0.27, 0.16, 0.09},
			new double[][]{ {1.214, 2.400, 2.718, 2.428}, 
				{1.317, 2.349, 2.659, 2.232}, {0.720, 1.857, 2.165, 1.798}, 
				{3.012, 2.721, 2.618, 3.848}, {2.537, 4.640, 4.667, 4.250}, 
				{1.846, 2.700, 2.754, 2.632}, {1.112, 1.943, 1.842, 2.003} },
			new double[]{2.0, 2.0, 2.0, 2.0},
		};
		list.add(data);		
		return list;
	}			
	
	private void run(
			int expType, 
			int nrAttributes, 
			int nrBrands
			) throws NoAwarenessException {
		
		List<Object[]> listInput;
		Randomizer r = RandomizerFactory.createDefaultRandomizer();
		long seedExp = 1;
		Randomizer randomExp = RandomizerFactory.createDefaultRandomizer(seedExp);
		int res;
		int nrSegments = 1;
		int segmentId = 0;
		double[] heuristicSelectionProb = new double[NR_OF_HEURISTICS];
		double[][] drivers = new double[nrSegments][nrAttributes];
		boolean[] awareness = new boolean[nrBrands];
		double[][] perceptions = new double[nrBrands][nrAttributes];
		double[][] perceptionsCopy = new double[nrBrands][nrAttributes]; 
		boolean normallyDistributed = true;
		
		System.out.println("Running experiments for the decision making module...");		
		
		if(expType == EXPERIMENT_1) {
			listInput = (ArrayList<Object[]>) data1();
		} else if(expType == EXPERIMENT_2) { 
			listInput = (ArrayList<Object[]>) data2();
		} else { // if(experimentType == EXPERIMENT_PELAYO)
			listInput = (ArrayList<Object[]>) dataPelayo();
		}
		
		results = new int[NR_OF_HEURISTICS][listInput.size()][nrBrands];
		
		for(int i=0; i<NR_OF_HEURISTICS; i++) {
			if(i != 0) {
				heuristicSelectionProb[i-1] = 0.0;
			}
			heuristicSelectionProb[i] = 1.0; 
			for(int j=0; j<listInput.size(); j++) {
				drivers[segmentId] = (double[]) listInput.get(j)[0];
							
				// XXX Different meaning of heuristicSelectionProb comparing with 
				// setting separetely emotional, ..., etc.
				DecisionMaking dm = new DecisionMaking(
						r, drivers, heuristicSelectionProb[0], 
						heuristicSelectionProb[1], heuristicSelectionProb[2], 
						heuristicSelectionProb[3], nrAttributes, nrBrands
				);
				dm.setHeuristicSelectionProb(heuristicSelectionProb);
				
				Arrays.fill(awareness, true);
				perceptions = (double[][]) listInput.get(j)[1];
				for(int l=0; l<perceptions.length; l++) {
					for(int m=0; m<perceptions[l].length; m++) {
						perceptionsCopy[l][m] = perceptions[l][m];		
					}
				}
				for(int k=0; k<ITERATIONS; k++) {
					if(normallyDistributed) {
						double lowerBound = 0.0;
						double upperBound = 10.0;
						double stdev = 2.0;
						for(int l=0; l<perceptions.length; l++) {
							for(int m=0; m<perceptions[l].length; m++) {							
								perceptions[l][m] = Functions.scaleGaussianValue(
									perceptionsCopy[l][m], 
									Functions.nextGaussian(randomExp, 6.0), 
									stdev, 
									lowerBound, 
									upperBound
								);
							}
						}
					}
					r.setSeed(k);

					res = dm.buyOneBrand(
						awareness, perceptions, segmentId
					);	

					results[i][j][res]++;
				}
			}
		}
		
		for(int i=0; i<NR_OF_HEURISTICS; i++) {
			System.out.println("Heuristic: " + i);
			for(int j=0; j<listInput.size(); j++) {
				for(int k=0; k<nrBrands; k++) {
					System.out.print(results[i][j][k] + " ");
				}
				System.out.println();
			}
		}
		writeToFile(expType, nrAttributes, nrBrands);
	}

	//----------------------------- I/O methods -----------------------------//
	
	private void writeToFile(
		int expType, int nrAttributes, int nrBrands
	) {
		PrintStream streamFile;
		List<Object[]> listInput;
		if(expType == EXPERIMENT_1) {
			listInput = (ArrayList<Object[]>) data1();
		} else if(expType == EXPERIMENT_2) { 
			listInput = (ArrayList<Object[]>) data2();
		} else { // if(experimentType == EXPERIMENT_PELAYO)
			listInput = (ArrayList<Object[]>) dataPelayo();
		}
		double[] drivers = new double[nrAttributes];
		double[][] perceptions = new double[nrBrands][nrAttributes];
		double[] cutoffs = new double[nrAttributes];		
		
		try {
			if(expType == EXPERIMENT_1) {
				streamFile = new PrintStream(new FileOutputStream(FILE_NAME_EXP_1, false));				
			} else if(expType == EXPERIMENT_2) { 
				streamFile = new PrintStream(new FileOutputStream(FILE_NAME_EXP_2, false));				
			} else { // if(experimentType == EXPERIMENT_PELAYO)
				streamFile = new PrintStream(new FileOutputStream(FILE_NAME_EXP_PELAYO, false));	
			}
			
			// First line
			String aux = "Heuristic drivers ";
			String tmp = "";
			for(int i=0; i<nrAttributes; i++) {
				tmp += " ";
			}
			aux += tmp + "perceptions";
			for(int i=0; i<nrBrands; i++) {
				aux += tmp;
			}
			aux += tmp + "cutoffs " + tmp + "brands";
			streamFile.println(aux);
			for(int i=0; i<NR_OF_HEURISTICS; i++) {
				streamFile.println(HEURISTIC_NAMES[i]);
				for(int j=0; j<listInput.size(); j++) {
					drivers = (double[]) listInput.get(j)[0];
					perceptions = (double[][]) listInput.get(j)[1];
					cutoffs = (double[]) listInput.get(j)[2];					
					
					streamFile.print(" ");
					for(int k=0; k<nrAttributes; k++) {
						streamFile.print(drivers[k] + " ");
					}
					
					streamFile.print(" ");
					for(int k=0; k<nrBrands; k++) {
						for(int l=0; l<nrAttributes; l++) {
							streamFile.print(perceptions[k][l] + " ");							
						}
						streamFile.print(" ");
					}				
					
					for(int k=0; k<nrAttributes; k++) {
						streamFile.print(cutoffs[k] + " ");
					}					
					
					streamFile.print(" ");
					for(int k=0; k<nrBrands; k++) {
						streamFile.print(results[i][j][k] + " ");
					}
					streamFile.println();
				}
			}			
			streamFile.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void writePercToFile(
		int idExperiment, int idHeuristic, double[][] perceptions, int result
	) {
		PrintStream streamFile;
		try {
			String aux = FILE_NAME_EXP_PELAYO_PERCEPTIONS + "_" + idExperiment 
				+ "_" + idHeuristic + ".txt";
			streamFile = new PrintStream(new FileOutputStream(aux, true));

			for(int k=0; k<perceptions.length; k++) {
				for(int l=0; l<perceptions[k].length; l++) {
					streamFile.print(String.format("%.2f",perceptions[k][l]) + " ");							
				}
				streamFile.print(" ");
			}
			streamFile.print(result);
			streamFile.println();
			
			streamFile.close();				
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}	
	
	public static void main(String[] args) {
		DecisionMakingHeuristics experimentDM = new DecisionMakingHeuristics();
		try {
			System.out.println("Test 1");
			experimentDM.run(
				EXPERIMENT_1, NR_ATTRIBUTES_EXP_1, NR_BRANDS_EXP_1
			);
			System.out.println("Test 2");
			experimentDM.run(
				EXPERIMENT_2, NR_ATTRIBUTES_EXP_2, NR_BRANDS_EXP_2
			);
			System.out.println("Test 3");
			experimentDM.run(
				EXPERIMENT_PELAYO, NR_ATTRIBUTES_EXP_PELAYO, NR_BRANDS_EXP_PELAYO
			);	
		} catch (NoAwarenessException e) {
			e.printStackTrace();
		}
			
	}
}
