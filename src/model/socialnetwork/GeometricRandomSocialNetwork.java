package model.socialnetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.random.RandomizerUtils;

/**
 * Random network generator (for only 1 SEGMENT!!!) based on the geometric 
 * distribution (calculating a number of fails until first success), used to 
 * calculate the nodes degree. <B>IT DOES NOT IMPLEMENT ANY FUNCTIONS FOR MORE
 * THAN 1 SEGMENT!!!</B>
 * 
 * @author JB
 *
 */
public class GeometricRandomSocialNetwork extends BasicRandomSocialNetwork {
	
	//#########################################################################
	// Logger
	//#########################################################################

	private final static Logger logger = 
		LoggerFactory.getLogger(GeometricRandomSocialNetwork.class);
	
	//#########################################################################
	// Protected methods
	//#########################################################################
	
	/**
	 * Generates a network with only one segment using a geometric
	 * distribution (calculating a number of fails until first success) to get 
	 * a degree of each node.
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
		int i = 0, j = 1, connectionsCount = 0;
		for (;;) {
				
			// Move to next (random) candidate 
			int shift = (int) RandomizerUtils.computeGeometricFails(
				adjustedConnectivity, random
			);
			
			// Compute (i,j) indexes || TODO: [JB] compute (i,j) directly
			while ( shift > 0 ) {
				// if column is out of bounds => next row
				if ( (++j) == expectedNetworkSize ) { 
					// if row reaches penultimate => break
					if ( (++i) == (expectedNetworkSize - 1) ) break; 
					// column starts at i + 1 
					j = (i+1); 
				}
				shift--;
			}
			
			// if row reaches penultimate => break
			if ( i == (expectedNetworkSize - 1) ) break; 
				
			// Add connection (undirected)
			nodeNeighbours[i].add(j);
			nodeNeighbours[j].add(i);
			connectionsCount += 2;
			
		}	
		if (logger.isDebugEnabled()) logger.debug(
			"generateUniqueSegmentFast() " + expectedConnections 
			+ "  " + connectionsCount
		);	
	}
}
