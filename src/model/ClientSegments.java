package model;

import util.functions.Functions;
import util.random.Randomizer;

/**
 * Defines the static properties of agents grouping them by segments.
 * 
 * @author ktrawinski
 *
 */
public class ClientSegments {

	// ########################################################################
	// Variables
	// ########################################################################
	
	private int numberOfSegments =0;
	
	private int[][] agentsBySegment;
	private int[] agentsCountBySegment;
	
	private double[] segmentSizes;
	
	private int[] segmentSizesInt;
	private double[] segmentsConnectivity; 
	private double[][] segmentInfluence;
	
	private double[] segmentTalkingProbabilities;
	
	private double[][] segmentInitialAwareness;
	private double[] segmentAwarenessImpact;
	private double[] segmentAwarenessDecay;
	private double[] segmentDiscussionHeatImpact;
	private double[] segmentPerceptionSpeed;
	
	/*
	 * Agent's variables: stored at this level in order to its instantiation.
	 */
	private double[][][] segmentInitialPerceptions;	
	private double[][] drivers;
	
	// ########################################################################
	// Constructors
	// ########################################################################
	

	/**
	 * Initializes an instance of Client Segments
	 */
	public ClientSegments() 
	{
		segmentInfluence = null;
		segmentTalkingProbabilities = null;
		segmentSizes = null;
		segmentSizesInt = null;		
		segmentInitialAwareness = null;
		segmentAwarenessDecay = null;
		segmentAwarenessImpact = null;
		
		agentsBySegment=null;
		agentsCountBySegment=null;
	}

	// ########################################################################
	// Methods/Functions
	// ########################################################################

	private void resizeSegments() {
		double totalSize=0.0;
		for(int i=0; i<numberOfSegments;i++) {
			totalSize+=segmentSizes[i];
		}
		double diff = 1.0 - totalSize;
		if (diff>0) {
		//The remaining segment size is assigned to the last segment
		//(Just because)
			segmentSizes[numberOfSegments-1]+=diff;
		}
	}
	
	//--------------------------- Get/Set methods ---------------------------//
	/**
	 * Sets the number of segments.
	 * @param nrSegments
	 */
	public void setNrSegments(int nrSegments) {
		numberOfSegments = nrSegments;		
		this.agentsBySegment=new int[nrSegments][];
		this.agentsCountBySegment=new int[nrSegments];
	}
	
	public int getNumSegments() {
		return numberOfSegments;
	}

	/**
	 * Gets the segment sizes.
	 * @return segment sizes.
	 */
	public double[] getSegmentSizes() {
		return segmentSizes;
	}
	
	/**
	 * Gets the given segment size.
	 * @param ind - segment id
	 * @return size of segment with id ind
	 */
	public double getSegmentSize(int ind) {
		return segmentSizes[ind];
	}	

	/**
	 * Sets the segment sizes. 
	 * @param segmentSizes
	 */
	public void setSegmentSizes(double[] segmentSizes) {
		this.segmentSizes = segmentSizes;
		resizeSegments();
	}
	
	/**
	 * Gets the segment sizes in Integer.
	 * @return segment sizes in integer values.
	 */
	public int[] getSegmentSizesInt() {
		return segmentSizesInt;
	}

	/**
	 * Gets the given segment connectivity.
	 * @param ind - segment id.
	 * @return connectivity for segment with id ind.
	 */
	public double getSegmentConnectivity(int ind) {
		return segmentsConnectivity[ind];
	}

	/**
	 * Sets the segments connectivity.
	 * @param segmentsConnectivity
	 */
	public void setSegmentsConnectivity(double[] segmentsConnectivity) {
		this.segmentsConnectivity = segmentsConnectivity;
	}

	/**
	 * Gets the segment influences.
	 * @param segmentInfluences
	 */
	public void setSegmentInfluences(double[][] segmentInfluences) {
		this.segmentInfluence = segmentInfluences;
	}

	/**
	 * Gets the influence value of one segment to the other.
	 * @param x - the index of the influencing segment.
	 * @param y - the index of the influenced segment.
	 * @return - the influence of one segment to the other [0,1].
	 */
	public double getSegmentInfluenceValue(int x, int y) {
		return segmentInfluence[x][y];
	}
	
	/**
	 * Gets the given segment talking probability. 
	 * @param ind - segment id.
	 * @return return talking probability for segment with id ind.
	 */
	public double getSegmentTalkingProbability(int ind) {
		return segmentTalkingProbabilities[ind];
	}	

	/**
	 *  Sets the segment talking probabilities.
	 * @param segmentTalkingProbabilities
	 */
	public void setSegmentTalkingProbabilities(double[] segmentTalkingProbabilities) {
		this.segmentTalkingProbabilities = segmentTalkingProbabilities;
	}

	/**
	 * Sets the initial awareness of segments.
	 * @param segmentInitialAwarenesses
	 */
	public void setSegmentInitialAwarenesses(double[][] segmentInitialAwarenesses) {
		this.segmentInitialAwareness = segmentInitialAwarenesses;
	}
	
	public double getSegmentAwarenessImpact(int ind) {
		return segmentAwarenessImpact[ind];
	}

	public void setSegmentAwarenessImpact(double[] segmentAwarenessImpacts) {
		this.segmentAwarenessImpact = segmentAwarenessImpacts;
	}

	/**
	 * Gets the decay of segments.
	 * @return return awareness decay for every segment
	 */
	public double[] getSegmentAwarenessDecays() {
		return segmentAwarenessDecay;
	}
	
	/**
	 * Gets the decay of the given segment.
	 * @param ind - segment id
	 * @return decay for segment with id ind
	 */
	public double getSegmentAwarenessDecay(int ind) {
		return segmentAwarenessDecay[ind];
	}	

	/**
	 * Sets the decay of segments.
	 * @param segmentAwarenessDecay
	 */
	public void setSegmentAwarenessDecays(double[] segmentAwarenessDecay) {
		this.segmentAwarenessDecay = segmentAwarenessDecay;
	}

	public double getSegmentDiscussionHeatImpact(int ind) {
		return segmentDiscussionHeatImpact[ind];
	}
	
	public void setSegmentDiscussionHeatImpacts(
			double[] segmentDiscussionHeatImpacts) {
		this.segmentDiscussionHeatImpact = segmentDiscussionHeatImpacts;
	}

	public double[] getSegmentPerceptionSpeeds() {
		return segmentPerceptionSpeed;
	}
	
	public double getSegmentPerceptionSpeed(int ind) {
		return segmentPerceptionSpeed[ind];
	}
	
	public void setSegmentPerceptionSpeeds(double[] segmentPerceptionSpeeds) {
		this.segmentPerceptionSpeed = segmentPerceptionSpeeds;
	}

	public double[][] getSegmentInitialPerceptions(int id) {
		return segmentInitialPerceptions[id];
	}

	public void setSegmentInitialPerceptions(double[][][] segmentInitialPerceptions) {
		this.segmentInitialPerceptions = segmentInitialPerceptions;
	}
	
	public double[][] getDrivers() {
		return drivers;
	}
	
	public void setDrivers(double[][] drivers) {
		this.drivers = drivers;
	}	
	
	public int[][] getAgentsBySegment() {
		return agentsBySegment;
	}
	
	public int[] getAgentCountBySegment() {
		return agentsCountBySegment;
	}
		
	//-------------------------- Functionality --------------------------//
	
	
	/**
	 * Generates the sizes of the segments in Integer.
	 * @param nrNodes
	 */
	public void generateSegmentSizesInteger(int nrNodes) {
		int[] tmpInt = new int [segmentSizes.length];
		int assigned=0;
		for(int i=0; i<segmentSizes.length; i++) {
			int nodes=(int) (segmentSizes[i] * nrNodes);
			tmpInt[i] = nodes;
			assigned+=nodes;
		}
		
		if(assigned<nrNodes) {
			int diff=nrNodes-assigned;
			for (int i=0; i<diff; i++) {
				tmpInt[i]++;
			}
		}
		
		//Adjust agents by segment
		for(int i=0; i<agentsBySegment.length; i++) {
			agentsBySegment[i]= new int[tmpInt[i]];
		}
		
		segmentSizesInt = tmpInt;
	}	
	
	/**
	 * Relates an agent to a given segment, in order to retrieve the
	 * all the agents from a segment as a whole, and define statistics
	 * regarding how agents are distributed among segments.
	 * 
	 * @param segmentid - The identifier of the segment to witch the
	 * agent is assigned to.
	 * @param clientid - The identifier of the agent being assigned.
	 */
	public void addAgentToSegment(int segmentid, int clientid) {
		agentsBySegment[segmentid][agentsCountBySegment[segmentid]]=clientid;
		agentsCountBySegment[segmentid]++;
	}
	
	/**
	 * Generates values for agents perceptions based on the segment
	 * where the agent is assigned. If a normalized value is desired,
	 * additional fields like standard deviation and the instance of
	 * the randomizer used is needed.
	 * 
	 * @param segmentid - The identifier of the segment associated with
	 * the agent.
	 * @param stdDeviation - The standard deviation desired for the
	 * normalized values. If 0.0, perceptions won't be normalized.
	 * @param randomizer - The instance of the randomizer used in the 
	 * simulation.
	 * @return The values for agent initial perceptions based on segment
	 * initial perceptions.
	 */
	public double[][] generateInitialPerceptions(int segmentid,
			double stdDeviation, Randomizer randomizer) {
		double[][] perceptions = new double[segmentInitialPerceptions[segmentid].length]
				[segmentInitialPerceptions[segmentid][0].length];
		
		double gauss = Functions.nextGaussian(randomizer);
		
		for (int i=0; i<segmentInitialPerceptions[segmentid].length; i++) {
			for (int j=0; j<segmentInitialPerceptions[segmentid][i].length; j++) {
				double p=segmentInitialPerceptions[segmentid][i][j];
				
				if(stdDeviation!=0.0) {
					perceptions[i][j]=Functions.scaleGaussianValue(p, gauss, 
							stdDeviation, Model.MINIMUM_PERCEPTION_VALUE, 
							Model.MAXIMUM_PERCEPTION_VALUE);
				} else {
					perceptions[i][j]=p;
				}
			}
		}
		return perceptions;
	}

	/**
	 * Generates initial awareness for a single agent using the segment
	 * initial awareness parameter.
	 * 
	 * @param nrBrands - number of Brands considered by the actual
	 * market.
	 * @return The initial awareness for a single agent.
	 */
	public boolean[] generateInitialAwareness(int nrBrands, int segmentId, 
			Randomizer randomizer) {
		boolean[] tmpBoolean = new boolean[nrBrands];
		double val;
		
		for(int i=0; i<nrBrands; i++) {
			// Check if only one node has been forced
			val = segmentInitialAwareness[i][segmentId];
			
			double check = randomizer.nextDouble();
			
			if(check < val) {
				tmpBoolean[i] = true;
			}
		}
		return tmpBoolean;
	}
}
