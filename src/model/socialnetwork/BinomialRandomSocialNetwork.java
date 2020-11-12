package model.socialnetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.random.RandomizerUtils;

/**
 * Random network generator (for only 1 SEGMENT!!!) based on the binomial 
 * distribution, used to calculate the nodes degree. 
 * <B>IT DOES NOT IMPLEMENT ANY FUNCTIONS FOR MORE THAN 1 SEGMENT!!!</B>
 * 
 * @author JB
 *
 */
public class BinomialRandomSocialNetwork extends BasicRandomSocialNetwork  {
	
	//#########################################################################
	// Logger
	//#########################################################################
	
	private final static Logger logger = 
		LoggerFactory.getLogger(BinomialRandomSocialNetwork.class);
	
	//#########################################################################
	// Atributes
	//#########################################################################
	
	/** Nodes already connected to current node (neighbours) */
	protected boolean[] connected;
	
	//#########################################################################
	// Protected methods
	//#########################################################################

	/**
	 * Generates a network with only one segment using a binomial
	 * distribution to get a degree of each node.
	 * 
	 * @param expectedNetworkSize - the size of the network
	 * @param expectedMaxAvgDegree - the avg. degree of the network
	 * @param globalConnectivity - the connectivity of the entire network
	 */
	@Override
	protected void generateUniqueSegmentFast(
			final int expectedNetworkSize,
			final int expectedMaxAvgDegree,
			final double globalConnectivity) {
		
		resetConnected(expectedNetworkSize);
		
		final double adjustedConnectivity = 
				globalConnectivity * ((double) expectedMaxAvgDegree/(expectedNetworkSize - 1));
		
		// N(N-1)/2 * 2 * probability = N(N-1) * probability
		final int expectedConnections = (int) Math.round(
			(expectedNetworkSize - 1) * expectedNetworkSize * 
			adjustedConnectivity
		);	
		
		// Generate nodes
		for(; networkSize < expectedNetworkSize; networkSize++) {
			// Unique segment => simply assign it
			nodeSegments[networkSize] = FIRST_SEG;
		}
		
		// Generate connections (only compares from i+1 node)
		int nodeDegree, maxComparisons, connectionsCount = 0;
		for(int i = 0; i < (expectedNetworkSize - 1); i++) {
			
			maxComparisons = (expectedNetworkSize - i - 1);
			
			// Compute estimated node degree (random approximate)
			nodeDegree = (int) RandomizerUtils.computeBinomial(
				adjustedConnectivity, maxComparisons, random
			);
			
			// Check that nodeDegree does not exceed maximum
			if ( nodeDegree < maxComparisons ) { 
				
				// Do we search for connected or not connected?
				final boolean connect = 
					( (nodeDegree / 2) < (maxComparisons - nodeDegree) );
				
				// Initialize auxiliary data for each node
				for (int j = (i+1); j < expectedNetworkSize; j++) {
					connected[j] = !connect;
				}				
				
				// Search for candidates
				while (nodeDegree > 0) {
					
					// Move to next (random) candidate in [i+1, n) 
					final int j = random.nextInt(maxComparisons) + (i+1); 
					
					// Change connection if not already changed
					if (connected[j] != connect) {
						connected[j] = connect;
						connectionsCount += 2;
						nodeDegree--;
						if (connect) {
							// Add connection (undirected)
							nodeNeighbours[j].add(i);
							nodeNeighbours[i].add(j);
							
						}
					}
				}
				
				// Establish connections if searching for not connected
				if (!connect) addConnectedNeighbours(i);
			
			// If all connections are required => simply add all
			} else {
				addAllNeighbours(i);
				connectionsCount += (2 * maxComparisons);
			}
				
		}	
		if (logger.isDebugEnabled()) logger.debug(
			"generateUniqueSegmentFast() " + expectedConnections + "  " + connectionsCount
		);
	}

	/**
	 * Adds nodes from the connected array to neighbors of the given node.
	 * @param i - the index of the current node
	 */
	private void addConnectedNeighbours(int i) {
		for (int j = (i+1); j < networkSize; j++) {
			if (connected[j]) {
				// Add connection (undirected)
				nodeNeighbours[j].add(i);
				nodeNeighbours[i].add(j);
			}
		}
	}
	
	/**
	 * Adds all the nodes to neighbors of the given node.
	 * @param i
	 */
	private void addAllNeighbours(int i) {
		for (int j = (i+1); j < networkSize; j++) {
			nodeNeighbours[i].add(j);
			nodeNeighbours[i].add(j);
		}
		
	}

	/**
	 * Resets the array pointing, which nodes are connected.
	 * @param expectedNetworkSize - the new network size
	 */
	private void resetConnected(final int expectedNetworkSize) {
		// Do not reconstruct connected array if not really required
		if (
			connected == null || 
			connected.length != expectedNetworkSize
		) {
			connected = new boolean[expectedNetworkSize];
		
		// Reset values without reconstructing arrays
		} else {
			// Check that everything is ok (DEBUG)
			assert(connected.length == expectedNetworkSize);
		}
	}
}
