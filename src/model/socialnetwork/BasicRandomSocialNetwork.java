package model.socialnetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Random network generator with SEGMENTS using Erdős and Rényi approach.
 * Each segment has a different connectivity (a probability to connect
 *  to the other node).
 * 
 * G(N,L) model: N labeled nodes are connected with L randomly placed links. 
 * Erdős and Rényi (Erdős & Rényi, 1959) used this definition in their 
 * string of articles on random networks.
 * 
 * @author JB
 *
 */
public class BasicRandomSocialNetwork extends SocialNetwork {
	
	//#########################################################################
	// Logger
	//#########################################################################

	private final static Logger logger = 
		LoggerFactory.getLogger(BasicRandomSocialNetwork.class);
	
	//#########################################################################
	// Attributes
	//#########################################################################
	
	/** Matrix of probabilities for each segment pair (connectivity) */
	protected double[][] probMatrix;
	
	//#########################################################################
	// Protected methods
	//#########################################################################
	
	/**
	 * Resets the matrix of probabilities for each segment pair.
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
			probMatrix == null || 
			probMatrix.length != segmentsCount
		) {
			
			// Probability matrix is only useful with multiple segments
			probMatrix = (segmentsCount == 1)?
				null : new double[segmentsCount][segmentsCount];
			
		} else {
			
			// Check that everything is ok (DEBUG)
			assert(
				(segmentsCount == 1 && probMatrix == null) 
				|| segmentsCount == probMatrix.length
			);
		}
	
	}
	
	
	/** 
	 * Generates a random network with different probabilities 
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
					
		
		if (segmentsCount == 1) {
			
			assert(segmentSizes[FIRST_SEG] == expectedNetworkSize);
			
			// Algorithm for unique segment generation is trivial
			generateUniqueSegmentFast(
				expectedNetworkSize, 
				expectedMaxAvgDegree,
				segmentConnectivities[FIRST_SEG]
			);
			
		} else {
		
			// Pre-compute inter-segment probabilities
			computeProbabilityMatrix(
				expectedNetworkSize,
				expectedMaxAvgDegree,
				segmentConnectivities
			);

			// Generate nodes
			for(; networkSize < expectedNetworkSize; networkSize++){
				nodeSegments[networkSize] = selectSegment();
			}
			
			// Generate connections	(neighbours)	
			for (int i = 0; i < networkSize; i++) {
				for (int j = (i+1); j < networkSize; j++) {
					
					// Randomizer random
					double r = random.nextDouble(); // [0, 1)
					if (logger.isDebugEnabled()) logger.debug(
						"generate() random.nextDouble() " + r
					);
					
					// Add neighbours 
					if(r <= probMatrix[nodeSegments[i]][nodeSegments[j]]) {
						nodeNeighbours[j].add(i);
						nodeNeighbours[i].add(j);
					}
					
				}
			}
		}
	}

	/**
	 * Generates a network when only one segment is set (a fast method).
	 * 
	 * @param expectedNetworkSize - the size of the network
	 * @param expectedMaxAvgDegree - the avg. degree of the network
	 * @param globalConnectivity - the connectivity of the entire network
	 */
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
		int connectionsCount = 0;
		for(int i = 0; i < (expectedNetworkSize - 1); i++) {
			
			for (int j = (i+1); j < expectedNetworkSize; j++) {
				
				// Randomizer random
				double r = random.nextDouble(); // [0, 1)
				if (logger.isDebugEnabled()) logger.debug(
					"generateUniqueSegmentFast() random.nextDouble() " + r
				);
				
				// Add neighbors 
				if(r <= adjustedConnectivity) {
					nodeNeighbours[j].add(i);
					nodeNeighbours[i].add(j);
					connectionsCount += 2;
				}
				
			}
		}	
		if (logger.isDebugEnabled()) logger.debug(
			"generateUniqueSegmentFast() " + expectedConnections + "  " + connectionsCount
		);
	}

	/**
	 * Computes the probability matrix of generating an edge between two nodes.
	 * 
	 * @param expectedNetworkSize - the size of the network
	 * @param expectedMaxAvgDegree - the avg. degree of the network
	 * @param segmentConnectivities - the connectivities of each segment
	 */
	private void computeProbabilityMatrix(
			final int expectedNetworkSize,
			int expectedMaxAvgDegree,
			double[] segmentConnectivities) {
		
		// Compute diagonal (intra-segment probabilities)
		for (int i = 0; i < segmentsCount; i++) {
			probMatrix[i][i] = 
				segmentConnectivities[i] * ((double) expectedMaxAvgDegree/(expectedNetworkSize - 1));
		}
		
		// Compute rest of matrix (symmetric)
		for (int i = 0; i < segmentsCount; i++) {
			for (int j = (i+1); j < segmentsCount; j++) {
				probMatrix[i][j] = probMatrix[j][i] = ((
					segmentConnectivities[i] + 
					segmentConnectivities[j]
					) / 2 ) * ((double) expectedMaxAvgDegree/(expectedNetworkSize - 1));
			}
		}	
	}
}
