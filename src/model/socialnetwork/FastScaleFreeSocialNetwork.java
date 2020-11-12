package model.socialnetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scale-free graph generator with SEGMENTS using the preferential attachment rule as 
 * defined in the Barabasi-Albert model.
 * 
 * <p>
 * This is a very simple graph generator that imitates the generation of a graph,
 * which uses the preferential attachment rule defined in the Barabai-Albert model.
 * It is a fast implementation that selects neighbour candidate by means of the
 * stochastic acceptance. Notice that the resulting network does not follow 
 * exactly the power law distribution.
 * </p>
 * 
 * @reference Albert-László Barabási & Réka Albert
 *            "Emergence of scaling in random networks", Science 286: 509–512.
 *            October 1999. doi:10.1126/science.286.5439.509.
 */
public class FastScaleFreeSocialNetwork extends BasicScaleFreeSocialNetwork {
	
	//#########################################################################
	// Logger
	//#########################################################################

	private final static Logger logger = 
		LoggerFactory.getLogger(FastScaleFreeSocialNetwork.class);
	
	//#########################################################################
	// Protected methods
	//######################################################################### 
	
	/**
	 * Selects the neighbors for the given node (there are more than 
	 * a degree of the node) from the social network using an approach
	 * based on the stochastic acceptance.
	 * @param nodeDegree - the degree of the node (a number of neighbors)
	 */
	@Override 
	protected void selectNeighbours(final int nodeDegree) {
		
		// Initialize auxiliar data
		double availableSum = totalDegree;
		for (int i = 0; i < networkSize; i++) connected[i] = false;
		
		// Choose the nodes to attach to.
		for (int i = 0, j = 0; i < nodeDegree; i++) {
		
			// Search neighbour candidate with stochastic acceptance
			for (;;) {
				// move to next candidate 
				j = random.nextInt(networkSize); 
				// only checks probability if not already connected
				if (!connected[j]) {
					// stochastic acceptance
					if (
						random.nextDouble()
						< (nodeNeighbours[j].size() / availableSum)
					) break;
				}
			}
			
			// Update available sum (before updating neighbour lists)
			availableSum -= nodeNeighbours[j].size();
			
			// Add connection (undirected)
			nodeNeighbours[j].add(networkSize);
			nodeNeighbours[networkSize].add(j);
			connected[j] = true;
			
			if (logger.isDebugEnabled()) logger.debug(
				"addNeighboursFast() " + networkSize + " " + j
			);
		}
		
		// Update total degree (undirected graph)
		totalDegree += 2 * nodeDegree;
	}
	
}
