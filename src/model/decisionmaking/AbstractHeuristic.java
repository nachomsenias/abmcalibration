package model.decisionmaking;

import util.random.Randomizer;

/**
 * The abstract class defining the functionality for a given heuristic. All
 * heuristics implemented inherits from this class.
 * @author ktrawinski
 *
 */
public abstract class AbstractHeuristic {
	/*
	 * Perception maximum value.
	 */
	public static final double PERCEPTION_MAX = 10.0;
	/*
	 * Perception middle value.
	 */
	public static final double PERCEPTION_MIDDLE = 5.0;
	/*
	 * Perception minimum value.
	 */
	public static final double PERCEPTION_MIN = 0.0;
	
	public static final boolean INCLUDE_ZERO = true;
	public static final boolean INCLUDE_ONE = true;
	public static final double CUTOFF_DECREASE = 1.0;
	public static final double PERCEPTION_TALK_POST_SCALE_MAX = 5.0;
	
	/**
	 * The randomizer instance used by the heuristic.
	 */
	protected Randomizer random;
	/**
	 * Driver weights for every segment and attribute.
	 */
	protected double[][] drivers;
	
	/**
	 * Absolute perceptions flag: if talking or posting, perceptions are 
	 * normalized to a [5, 10] interval.
	 */
	boolean absoluteValPerceptions;
	
	/**
	 * Perceptions array containing the "absolute perceptions" values.
	 */
	double[][] perc;

	// ########################################################################
	// Constructors
	// ######################################################################## 	
	
	/**
	 * Initializes an instance of the AbstractHeuristic class (when inherited).
	 */
	protected AbstractHeuristic() {
		this.random = null;
		this.drivers = null;
		absoluteValPerceptions = false;
	}	
	
	// ########################################################################	
	// Gettes / Setters
	// ########################################################################		

	public Randomizer getRandom() {
		return random;
	}

	public void setRandom(Randomizer random) {
		this.random = random;
	}

	public double[][] getDrivers() {
		return drivers;
	}

	public void setDrivers(double[][] drivers) {
		this.drivers = drivers;
	}

	public boolean isAbsoluteValPerceptions() {
		return absoluteValPerceptions;
	}

	public void setAbsoluteValPerceptions(boolean absoluteValPerceptions) {
		this.absoluteValPerceptions = absoluteValPerceptions;
	}
	
	public double[][] getPerc() {
		return perc;
	}

	public void setPerc(double[][] perc) {
		this.perc = perc;
	}
	
	// ########################################################################	
	// Methods / Functions 	
	// ########################################################################	

	/**
	 * Calculates which brand is going to be chosen by the heuristic.
	 * @param awareness - the awareness of the current client agent.
	 * @param perceptions - the perceptions of the current client agent.
	 * @param segment - the index of the segment that the client agent is a member.
	 * @return - the index of the selected brand.
	 */
	public abstract int calculate(
		boolean[] awareness, double[][] perceptions, int segment
	);	

	/**
	 * Transforms the perception values in order to allow the client agent
	 * talking and posting. 
	 * Since we tend to talk about the products having attributes with
	 * boundary values (very good and very bad either!), the "absolute values"
	 * (10-val) are taken for the negative attribute values (<5.0).
	 * It results with values in a scale [5,10], which is transformed back
	 * to the perceptions scale [1,10].
	 * @param perceptions - the perceptions of the current client agent.
	 * @return - the transformed perceptions.
	 */
	protected double[][] absoluteValuePerceptions(double[][] perceptions) {
		
		for(int i=0; i<perc.length; i++) {
			for(int j=0; j<perc[i].length; j++) {
				// optimized version 2
				if(perceptions[i][j] < PERCEPTION_MIDDLE) {
					perc[i][j] = 2 * (PERCEPTION_MAX - perceptions[i][j] - 5);
				} else {
					perc[i][j] = 2 * (perceptions[i][j] - 5);
				}
			}
		}
		return perc;
	}
}
