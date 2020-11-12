package model.socialnetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scale-free graph generator with SEGMENTS using the preferential attachment rule as 
 * defined in the Barabasi-Albert model.
 * 
 * <p>
 * This is a very simple graph generator that generates a graph using the
 * preferential attachment rule defined in the Barabai-Albert model: nodes are
 * generated one by one, and each time attached by one or more edges other
 * nodes. The other nodes are chosen using a biased random selection giving more
 * chance to a node if it has a high degree.
 * </p>
 * 
 * @reference Albert-László Barabási & Réka Albert
 *            "Emergence of scaling in random networks", Science 286: 509–512.
 *            October 1999. doi:10.1126/science.286.5439.509.
 */
public class BasicScaleFreeSocialNetwork extends SocialNetwork {
	
	//#########################################################################
	// Logger
	//#########################################################################

	private final static Logger logger = 
		LoggerFactory.getLogger(BasicScaleFreeSocialNetwork.class);
	
	//#########################################################################
	// Attributes
	//#########################################################################
	
	/** Nodes already connected to current node (neighbours) */
	protected boolean[] connected;
	
	/** The sum of degrees of all nodes */
	protected int totalDegree;
	
	/** Number of links per node for each segment (connectivity) */
	protected int[] segmentDegrees;
	
	//#########################################################################
	// Protected methods
	//######################################################################### 
	
	/**
	 * Resets the average degrees for every segment.
	 */
	@Override
	protected void resetSegments(
			int expectedNetworkSize,
			int expectedMaxAvgDegree,
			int[] segmentSizes,
			double[] segmentConnectivities) {
		
		super.resetSegments(
			expectedNetworkSize, 
			expectedMaxAvgDegree,
			segmentSizes, 
			segmentConnectivities
		);
		
		// Do not reconstruct arrays if not really required
		if (
			segmentDegrees == null || 
			segmentsCount != segmentDegrees.length
		) {
			segmentDegrees = new int[segmentsCount];
			
		} else {
			// Check that everything is ok (DEBUG)
			assert(segmentsCount == segmentDegrees.length);
		}
	}
	
	/**
	 * Resets the total network degree value and reconstructs node 
	 * arrays if needed. 
	 */
	@Override 
	protected void resetNodes(int expectedNetworkSize) {
		
		super.resetNodes(expectedNetworkSize);
		
		// Reset total degree of network (empty)
		totalDegree = 0;
		
		// Do not reconstruct node arrays if not really required
		if (connected == null || connected.length != expectedNetworkSize) {
			
			connected = new boolean[expectedNetworkSize];
		
		// Reset values without reconstructing arrays
		} else {
			
			// Check that everything is ok (DEBUG)
			assert(expectedNetworkSize == connected.length);
			
		}
	}
	
	/** 
	 * Generates a scale-free network with different probabilities 
	 * for each segment.
	 * 
	 * @param expectedNetworkSize - the size of the network
	 * @param expectedMaxAvgDegree - the avg. degree of the network
	 * @param segmentSizes - the sizes of segments
	 * @param segmentConnectivities - the connectivities of each segment
	 */	
	@Override
	protected final void generate(
			final int expectedNetworkSize, 
			final int expectedMaxAvgDegree,
			final int[] segmentSizes, 
			final double[] segmentConnectivities) {
		
		// Precompute segment degrees
		computeSegmentDegrees(
			expectedMaxAvgDegree, //TODO[KT] Neither this nor density params are used! It is the expectedNetworkSize not used
			segmentConnectivities
		);
				
		// Generate initial nodes (2)
		generateInitialNodes();
		
		// Loop for adding the remaining nodes (from third node)
		for(int nodeDegree; networkSize < expectedNetworkSize; networkSize++) {

			// Assign segment (pool of nodes is already updated)
			nodeSegments[networkSize] = selectSegment();
			
			// Get node degree from segment
			nodeDegree = segmentDegrees[nodeSegments[networkSize]];

			// If there are less nodes than required simply add all
			if (nodeDegree >= networkSize) selectAllNeighbours(); 
			
			// else => there are enough nodes to connect with
			else selectNeighbours(nodeDegree);
			
		}
		assert(networkSize == expectedNetworkSize);
	}
	
	/**
	 * Selects the neighbors for the given node (there are more than 
	 * a degree of the node) from the social network.
	 * @param nodeDegree - the degree of the node (a number of neighbors)
	 */
	protected void selectNeighbours(final int nodeDegree) {
		
		// Initialize auxiliar data
		int availableSum = totalDegree;
		for (int i = 0; i < networkSize; i++) connected[i] = false;
		
		// Choose the nodes to attach to.
		for (int i = 0; i < nodeDegree; i++) {
		
			int j = -1, runningSum = 0;
			
			// Search neighbour candidate with roulette wheel
			double r = random.nextDouble() * availableSum;
			do {
				j++; // move to next candidate node 
				if (!connected[j]) {
					// only adds degree if not already connected
					runningSum += nodeNeighbours[j].size();
				}
			} while (runningSum <= r);
			
			// Check that a valid index is selected
			assert(j < networkSize);
			
			// Update available sum (before updating neighbour list/degree)
			availableSum -= nodeNeighbours[j].size();
			
			// Update neighbour degree and add connection (undirected)
			nodeNeighbours[j].add(networkSize);
			connected[j] = true;
		}
		
		// Add current node neighbours
		for (int i = 0; i < networkSize; i++) {
			if (connected[i]) {
				nodeNeighbours[networkSize].add(i);
			}
		}
		
		// Update total degree (undirected graph)
		totalDegree += 2 * nodeDegree;
	}
	
	//#########################################################################
	// Private methods
	//######################################################################### 
	
	/**
	 * Selects all nodes left to be neighbors of the given node.
	 */
	private void selectAllNeighbours() {
		
		for (int i = 0; i < networkSize; i++) {

			// Add neighbour to current node
			nodeNeighbours[networkSize].add(i);
			
			// Update neighbour connections
			nodeNeighbours[i].add(networkSize);
		}
		
		// Update total degree (undirected graph)
		totalDegree += 2 * networkSize;
	}
	
	/**
	 * Computes degree of each segment.
	 * 
	 * @param expectedMaxAvgDegree - the average degree of the entire network
	 * @param segmentsConnectivities - connectivities of each segment
	 */
	private void computeSegmentDegrees(
			final int expectedMaxAvgDegree,
			double[] segmentsConnectivities) {
		
		// To obtain m, we have to divide the <k> by 2. The equation is
		// following: m = (<k>/2) * connectivitySegment.
		// Refactor note: "m" was renamed 
		for (int i = 0; i < segmentsCount; i++) {
			segmentDegrees[i] = (int) Math.round(
				(expectedMaxAvgDegree / 2)
				* segmentsConnectivities[i]
			);
			// Minimum degree should be always 1
			if (segmentDegrees[i] < 1) segmentDegrees[i] = 1;
			
			if(logger.isDebugEnabled()) logger.debug(
				"computeSegmentDegrees() " + i + " " + segmentDegrees[i]
			);	
		}
	}

	/**
	 * Generates two nodes interconnected between them and 
	 * initializes pool of nodes per segment
	 * 
	 */
	private void generateInitialNodes() {
		
		// Update size and total degree of network
		networkSize = 2;
		totalDegree = 2;
		
		// If there are at least 2 segments
		if (segmentsCount > 1) {
			
			// Assign a segment to each node
			for (int i = 0; i < networkSize; i++) {
				nodeSegments[i] = selectSegment();
			}
			
		// If there is only one segment, assign nodes to it
		} else {
			nodeSegments[FIRST_NODE] = FIRST_SEG;
			nodeSegments[SECOND_NODE] = FIRST_SEG;
			segmentNodePools[FIRST_SEG] -= 2;
		}
			
		// Initialization starts with 2 nodes connected between them
		nodeNeighbours[FIRST_NODE].add(SECOND_NODE);
		nodeNeighbours[SECOND_NODE].add(FIRST_NODE);		
	}
}
