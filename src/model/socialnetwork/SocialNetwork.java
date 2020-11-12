package model.socialnetwork;

import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.random.Randomizer;

/**
 * The abstract class defining the social network. It provides option to
 * choose either different random or different scale-free network implementations.
 * 
 * @author JB
 *
 */
public abstract class SocialNetwork {
	
	//#########################################################################
	// Logger
	//#########################################################################

	private final static Logger logger = 
		LoggerFactory.getLogger(SocialNetwork.class);

	//#########################################################################
	// Types of Networks
	//#########################################################################

	public enum NetworkType { 
		SCALE_FREE_NETWORK, RANDOM_NETWORK_SEGMENTS
	}
	
	public static final String SCALE_FREE = "Scale-free";
	public static final String RANDOM = "Random";
	
	public static final String networkTypeToString(NetworkType networkType) {
		switch(networkType) {
			case SCALE_FREE_NETWORK: return SCALE_FREE;
			case RANDOM_NETWORK_SEGMENTS: return RANDOM;
			default: throw new IllegalArgumentException(networkType.toString());
		}
	}
	
	public static NetworkType networkTypeFromString(String strNetworkType) {
		if (strNetworkType.equals(SCALE_FREE)) return NetworkType.SCALE_FREE_NETWORK;
		if (strNetworkType.equals(RANDOM)) return NetworkType.RANDOM_NETWORK_SEGMENTS;
		throw new IllegalArgumentException(strNetworkType);
	}
	
	//#########################################################################
	// Static
	//#########################################################################
	
	public static final int UNDEFINED = -1;
	
	public static final int FIRST_SEG = 0;
	public static final int SECOND_SEG = 1;

	public static final int FIRST_NODE = 0;
	public static final int SECOND_NODE = 1;
	
	public static final int DEFAULT_K_DEGREE_MAX = 16;
		
	// [KT] Use max density instead of max k degree.
	// public static final double DEFAULT_DENSITY_MAX = 0.016;
	
	public static final String DEFAULT_DISTRIBUTION_DEGREE_PATH = 
		"./log/histogram_SN_distribution_degrees.txt";

	public static final NetworkType DEFAULT_NETWORK_TYPE = 
		NetworkType.RANDOM_NETWORK_SEGMENTS;
	
	protected static final int DEFAULT_NEIGHBOUR_CAPACITY = 10;
	
	//#########################################################################
	// Attributes
	//#########################################################################	
	
	/** Index of current node (next to be added / actual count) */
	protected int networkSize;
	
	/** Neighbours list for each node */
	protected TIntList nodeNeighbours[];
	
	/** Array pointing the segment index of each node */
	protected int nodeSegments[];
	
	/** Number of segments */
	protected int segmentsCount;
	
	/** Available segments (not exhausted) */
	protected int availableSegments;
	
	/** Last segment index (one available) */
	protected int lastSegment;
	
	/** Cumulative weight (size) of each segment */
	protected int[] segmentCumulativeWeights;
	
	/** Remaining nodes to add to each segment */
	protected int[] segmentNodePools;
	
	/** Random number generator */
	protected Randomizer random;

	//#########################################################################
	// Public methods
	//######################################################################### 
	
	public int getNetworkSize() {
		return networkSize;
	}
	
	/** Gets segment index for all nodes  */
	public int[] getNodeSegments() {
		return nodeSegments;
	}
	
	/** Gets the segment index for a node  */
	public int getNodeSegmentAt(int nodeId) {
		return nodeSegments[nodeId];
	}
	
	/** Gets the neighbours list for a node  */
	public int[] getNodeNeighboursAt(int nodeId) {
		return nodeNeighbours[nodeId].toArray();
	}
	
	/**
	 * Generates the social network. Previously, it cleans segments nodes.
	 * 
	 * @param expectedNetworkSize - the size of the network
	 * @param expectedMaxAvgDegree - the avg. degree of the network
	 * @param segmentSizes - the sizes of segments
	 * @param segmentConnectivities - the connectivities of each segment
	 * @param random - the random value
	 */
	public final void generateNetwork(
			final int expectedNetworkSize, 
			final int expectedMaxAvgDegree,
			final int[] segmentSizes, 
			final double[] segmentConnectivities, 
			Randomizer random) {
		
		this.random = random;
		
		resetSegments(
			expectedNetworkSize, 
			expectedMaxAvgDegree,
			segmentSizes, 
			segmentConnectivities
		);
			
		resetNodes(expectedNetworkSize);
		
		generate(
			expectedNetworkSize, 
			expectedMaxAvgDegree,
			segmentSizes, 
			segmentConnectivities
		);
	}
	
	//#########################################################################
	// Protected methods
	//######################################################################### 
	
	/**
	 * Generates the social network only.
	 * 
	 * @param expectedNetworkSize - the size of the network
	 * @param expectedMaxAvgDegree - the avg. degree of the network
	 * @param segmentSizes - the sizes of segments
	 * @param segmentConnectivities - the connectivities of each segment
	 */
	protected abstract void generate(
		int expectedNetworkSize,
		int expectedMaxAvgDegree,
		int[] segmentSizes,
		double[] segmentConnectivities
	);
	
	
	
	/**
	 * Compute cumulative weight for each segment.
	 * 
	 * @param segmentSizes Original segment sizes
	 */
	protected void computeSegmentCumulativeWeights(final int[] segmentSizes) {
		
		segmentCumulativeWeights[FIRST_SEG] = segmentSizes[FIRST_SEG];
		for (int i = SECOND_SEG; i < segmentsCount; i++) {
			segmentCumulativeWeights[i] = 
				segmentSizes[i] + segmentCumulativeWeights[i-1];
		}
	}
	
	/**
	 * Recompute cumulative weight excluding exhausted segments 
	 * (which have same cumulative weight as previous segment).
	 * 
	 * @param discarded - Discarded segment index
	 */
	protected void recomputeSegmentCumulativeWeights(final int discarded) {
		
		int value = segmentCumulativeWeights[0];
		if (discarded > 0) {
			value = segmentCumulativeWeights[discarded] -
					segmentCumulativeWeights[discarded-1];
		}
		
		for (int i = discarded; i < segmentsCount; i++) {
			segmentCumulativeWeights[i] -= value;
		}
	}
	
	/**
	 * Selects a segment for the node (roulette wheel based on initial size).
	 * 
	 * It also updates segmentNodePools and availableSegments automatically.
	 * 
	 * When a segment is exhausted (no more nodes available), it also 
	 * recomputes segmentCumulativeWeights.
	 * 
	 * @return - Segment index
	 */
	protected int selectSegment() {
		
		// If we only have 1 segment available
		if (availableSegments == 1) return lastSegment;
		
		// Compute randValue in [0, total)
		final double r = 
			random.nextDouble() * // [0, 1)
			segmentCumulativeWeights[segmentsCount-1];
		
		// Select the segment index
		int segment = -1;
		for (
			segment = 0; 
			r > segmentCumulativeWeights[segment]; 
			segment++
		);
		
		// Update pool of nodes for the selected segment
		if ( (--segmentNodePools[segment]) == 0 ) {
			if ( (--availableSegments) > 1 ) {
				// Update cumulative weights if segment is exhausted
				recomputeSegmentCumulativeWeights(segment);
			} else {
				// Locate last segment if only one remains available
				for (
					lastSegment = 0;
					segmentNodePools[lastSegment] == 0;
					lastSegment++
				);
			}
		} 
		
		if(logger.isDebugEnabled()) logger.debug(
			"selectSegment() " + segment + "  " + segmentNodePools[segment]
		);	
		
		return segment;
	}
	
	/**
	 * Resets all the parameters related with segments needed 
	 * for the network generation.
	 * 
	 * @param expectedNetworkSize - the size of the network
	 * @param expectedMaxAvgDegree - the avg. degree of the network
	 * @param segmentSizes - the sizes of segments
	 * @param segmentConnectivities - the connectivities of each segment
	 */
	protected void resetSegments(
			final int expectedNetworkSize, 
			final int expectedMaxAvgDegree,
			final int[] segmentSizes, 
			final double[] segmentConnectivities) {
		
		availableSegments = segmentsCount = segmentSizes.length;
		
		// Check segment arrays lengths
		if (segmentsCount == 0) {
			throw new IllegalArgumentException("Number of segments can't be 0");
		}
		if (segmentsCount != segmentConnectivities.length) {
			throw new IllegalArgumentException("Segments count mismatch");
		}
		
		// Check that all segments have size & connectivity > 0
		int totalSegmentsSize = 0;
		for(int i = 0; i < segmentsCount; i++) {
			if(segmentSizes[i] == 0) throw new IllegalArgumentException(
					String.format("Size for segment %d needs to be greater than 0.0", i)
			);
			if(segmentConnectivities[i] == 0.0) throw new IllegalArgumentException(
					String.format("Connectivity for segment %d needs to be greater than 0.0", i)
			);
			totalSegmentsSize += segmentSizes[i];
		}
		
		// Check that total size of segments equals expectedNetworkSize 
		if (totalSegmentsSize != expectedNetworkSize) {
			throw new IllegalArgumentException("Bad segments/network sizes");
		}
		
		// Do not reconstruct arrays if not really required
		if (
			segmentNodePools == null || 
			segmentNodePools.length != segmentsCount
		) {
			segmentNodePools = new int[segmentsCount];
			segmentCumulativeWeights = new int[segmentsCount];
			
		} else {
			// Check that everything is ok (DEBUG)
			assert(segmentsCount == segmentNodePools.length);
			assert(segmentsCount == segmentCumulativeWeights.length);
		}

		// Initialize pool of nodes for each segment 
		System.arraycopy(segmentSizes, 0, segmentNodePools, 0, segmentsCount);
		
		if (availableSegments == 1) {
			// If only one segment, pre-select it
			lastSegment = FIRST_SEG;
		} else {
			// If more than one, pre-compute cumulative weights
			lastSegment = UNDEFINED;
			computeSegmentCumulativeWeights(segmentSizes);
		}
	}
	
	/**
	 * Resets all the parameters related to nodes of the network.
	 * 
	 * @param expectedNetworkSize - the size of the network
	 */
	protected void resetNodes(final int expectedNetworkSize) {
		
		if (expectedNetworkSize < 2) {
			throw new IllegalArgumentException("Minimum network size is 2");
		}
		
		// Reset network size (used as counter during building)
		networkSize = 0; 
		
		// Do not reconstruct node arrays if not really required
		if (
			nodeSegments == null || 
			nodeSegments.length != expectedNetworkSize
		) {

			nodeSegments = new int[expectedNetworkSize];
			nodeNeighbours = new TIntLinkedList[expectedNetworkSize];
			
			for (int i = 0; i < expectedNetworkSize; i++) {
				nodeNeighbours[i] = 
					new TIntLinkedList(DEFAULT_NEIGHBOUR_CAPACITY);
			}
			
		// Reset values without reconstructing arrays
		} else {
			
			// Check that everything is ok (DEBUG)
			assert(expectedNetworkSize == nodeSegments.length);
			assert(expectedNetworkSize == nodeNeighbours.length);
			
			for (int i = 0; i < expectedNetworkSize; i++) {	 	
				nodeNeighbours[i].clear();
			}
		}
	}
}
