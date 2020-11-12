package model.touchpoints.earned;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Model;
import model.customer.Agent;
import util.exception.simulation.NoAwarenessException;
import util.functions.Functions;
import util.random.Randomizer;

/**
 * Defines the posting/reading online part. This singleton class provides 
 * the following features:
 * 1.) posting online - All client agents have a probability to post online 
 * about their perceptions about one selected brand
 * 2.) reading online - All client agents have a probability to read online 
 * about one selected brand. The only condition is that the post is not
 * provided by the client agent.
 * 3.) It stores the list of all the posts containing essential information
 * such as brand index, agent's perceptions, segment index, agent index, and
 * the step in which the post was provided.
 * 
 * @author ktrawinski
 *
 */
public class PostReadOnline {
	
	/** Logger */
	private final static Logger logger =
		LoggerFactory.getLogger(PostReadOnline.class);
	
	private final static boolean LOG_INFO = logger.isInfoEnabled();
	
	//#########################################################################
	// Static
	//#########################################################################
	
	private static final int POST_ATTRIBUTE_VAL_INDEX = 0;	
	private static final int POST_ATTRIBUTE_ID_INDEX = 1;
	private static final int POST_SEGMENT_ID_INDEX = 2;	
	private static final int POST_AGENT_ID_INDEX = 3;
	@SuppressWarnings("unused")
	private static final int POST_STEP_INDEX = 4;

	private int nrBrands = -1;
	private double[] postingProbability = null;
	private double[] readingProbability = null;
	private double[] awarenessImpact = null;
	private double[] perceptionSpeed = null;
	private double[] discussionImpact = null;
	
	// XXX [JB] CHANGE THIS ArrayList<Object[]> !!!!! [KT] Previous comment...
	private List<Object[]>[] onlinePostsByBrand = null; // [att_val, att_id, seg_id, agent_id][brandId]

	// ########################################################################
	// Constructors
	// ######################################################################## 	
	
	/**
	 * Initializes an instance of the posting/reading online class.
	 */
	public PostReadOnline(
			int nrBrands, double[] postingProbability, 
			double[] readingProbability, double[] awarenessImpact, 
			double[] perceptionSpeed, double[] discussionImpact
	) {
		this.nrBrands = nrBrands;
		this.postingProbability = postingProbability;
		this.readingProbability = readingProbability;
		this.awarenessImpact = awarenessImpact;
		this.perceptionSpeed = perceptionSpeed;
		this.discussionImpact = discussionImpact;
	}
	
	// ########################################################################	
	// Methods/Functions 	
	// ########################################################################	
	
	public int getNrBrands() {
		return nrBrands;
	}

	public void setNrBrands(int nrBrands) {
		this.nrBrands = nrBrands;
	}

	public double[] getPostingProbability() {
		return postingProbability;
	}

	public void setPostingProbability(double[] postingProbability) {
		this.postingProbability = postingProbability;
	}

	public double[] getReadingProbability() {
		return readingProbability;
	}

	public void setReadingProbability(double[] readingProbability) {
		this.readingProbability = readingProbability;
	}

	public double[] getAwarenessImpact() {
		return awarenessImpact;
	}

	public void setAwarenessImpact(double[] awarenessImpact) {
		this.awarenessImpact = awarenessImpact;
	}
	
	public double[] getPerceptionSpeed() {
		return perceptionSpeed;
	}

	public void setPerceptionSpeed(double[] perceptionSpeed) {
		this.perceptionSpeed = perceptionSpeed;
	}

	public double[] getDiscussionImpact() {
		return discussionImpact;
	}

	public void setDiscussionImpact(double[] discussionImpact) {
		this.discussionImpact = discussionImpact;
	}
	
	/**
	 * Adds an online post provided by a client agent to the list with 
	 * the necessary information.
	 * @param awareness - awareness of the client agent.
	 * @param perceptions - perceptions of the client agent.
	 * @param segmentId - the index of the segment that the agent belongs to.
	 * @param agentId - the index of the client agent.
	 * @throws NoAwarenessException
	 */
	@SuppressWarnings("unchecked")
	public void postAboutOneBrand(
		boolean[] awareness, double[][] perceptions, double[] drivers, int segmentId, 
		int agentId, Randomizer random, int step, boolean awarenessFilter
	) throws NoAwarenessException {
		
		// Lazy... Check if posts' pool is created
		// and create it if not
		if(onlinePostsByBrand == null) {
			onlinePostsByBrand = 
				(ArrayList<Object[]>[]) new ArrayList[awareness.length];	// nrBrands
				for(int i=0; i<awareness.length; i++) {
					onlinePostsByBrand[i] = new ArrayList<Object[]>();
				}
		}
		// Lazy... Check if nrBrands is set
		if(nrBrands == -1) {
			nrBrands = awareness.length;
		}
		
		final double postingProb = postingProbability[segmentId];
		
		if (postingProb > 0.0) {
			for(int i = 0; i < nrBrands; i++) {	// for each brand
				
				// Perform the following checks:
				// 1. if awareness filter is disabled --> proceed
				// 2. if awareness filter is enabled AND agent has awareness of brand  --> proceed
				// 3. else do not enter the code
				if( !awarenessFilter || (awarenessFilter && awareness[i] == true) ) {
					double r = random.nextDouble(); // [0, 1)
					
					// Check if the agent will post
					// TODO [KT] I provided weekly probability. 
					// Maybe this should be more flexible and
					// give a chance to provide daily probability???
					// [KT] already solved by the ModelStepTranslator
					if(r <= postingProb) {
						int brandId = i;
						r = random.nextDouble(); // [0, 1)
						
						final int talkAttribute = 
							Functions.randomWeightedSelection(drivers, r);
						
						Object[] newPost;
						newPost = new Object[]{ 
							perceptions[brandId][talkAttribute], 	// perception posted
							talkAttribute,							// attribute id
							segmentId, 								// id of segment
							agentId, 								// id of agent
							step									// current step
						};
						onlinePostsByBrand[brandId].add(newPost);
						if (LOG_INFO) {
							// TOD [JB] Refactor all this logging...
							// Log perceptions
							String aux1 = " [" + brandId + ":";
							String aux2 = "";
							for(int j=0; j<perceptions[brandId].length; j++) {
								aux1 += " " + j;
								aux2 += " " + String.format("%.3f", perceptions[brandId][j]);					
							}
							// Log DM heuristic				
//							String heuristicName = DecisionMaking.getLogDM();
//							String aux3 = "Step " + step + " POSTING ONLINE agent " 
//									+ agentId + " segment " + segmentId 
//									+ " --> brand " + brandId + " DM heuristic " + heuristicName;
//							logger.info(aux3);
							// Write awareness log to the file	
							String aux4 = "Step " + step + " POSTING ONLINE AWARENESS " 
									+ "agent " + agentId + " segment " 
									+ segmentId + " --> brand " + brandId;		
							logger.info(aux4);
							// Write perception log to the file
							String aux5 = aux1 + ";" + aux2 + "]";
							String aux6 = "Step " + step + " POSTING ONLINE PERCEPTION "
									+ "agent " + agentId + " segment " + segmentId
									+ " --> [brand: attributes; values]" + aux5;
							logger.info(aux6);
						}
					}					
				}
			}
		}		
	}
	
	/**
	 * Reads an online post about one brand modifying the perceptions of 
	 * the client agent who is reading that post.
	 * @param customer - the client agent reading online.
	 * @param m - current model instance.
	 * @param step - current step.
	 */
	@SuppressWarnings("unchecked")
	public void readAboutOneBrand(
		Agent customer, Model m, int step
	) {
		// Lazy... Check if posts' pool is created
		// and create it if not
		if(onlinePostsByBrand == null) {
			onlinePostsByBrand = 
				(ArrayList<Object[]>[]) new ArrayList[nrBrands];	// nrBrands
				for(int i=0; i<nrBrands; i++) {
					onlinePostsByBrand[i] = new ArrayList<Object[]>();
				}
		}
		
		for(int i = 0; i < nrBrands; i++) {
			double readingProb = readingProbability[customer.segmentId];
			if(readingProb > 0.0) {
				double r = m.random.nextDouble(); // [0, 1)
				// TODO [KT] The same situation here as above. Maybe this should be more 
				// flexible and give a chance to provide daily probability???		
				if(r <= readingProb) {
					//check if there are posts and do not come only from this agent
					if( onlinePostsByBrand[i].size() > 0 ) {
						if( checkPosts(customer.clientId, i) ) {
							modifyAgent(customer, m, step, i);							
						} else if (LOG_INFO){
							logger.info(
									"Step " + step + " READING ONLINE" 
									+ " agent " + customer.clientId + " segment " 
									+ customer.segmentId + " --> NO POSTS AVAILABLE");
						}
					// If not, log it
					} else if (LOG_INFO){
						logger.info(
							"Step " + step + " READING ONLINE" 
							+ " agent " + customer.clientId + " segment " 
							+ customer.segmentId + " --> NO POSTS AVAILABLE");
					}
				}
			}
		}
	}
	
	// TODO [KT] this function can be optimized.
	/**
	 * Checks if there is at least one post in the list that does not belong
	 * to the client agent (A client agent cannot read his own posts). 
	 * @param agentId - index of the client agent.
	 * @return - true if there are posts of other client agents, false if not.
	 */
	private boolean checkPosts(int agentId, int brandId) {
		Iterator<Object[]> it = onlinePostsByBrand[brandId].iterator();
		while(it.hasNext()) {
			// Check if the post comes from this agent
			if((Integer) it.next()[POST_AGENT_ID_INDEX] != agentId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Modifies the parameters of the client agent after reading an online post.
	 * 
	 * @param customer the client agent reading online.
	 * @param random current simulation randomizer.
	 * @param step current simulation step.
	 * @param brandId the brand reading about.
	 */
	private void modifyAgent(
		Agent customer, Model m, int step, int brandId
	) {
		
		//Customer variables
		int postId;
		int clientId;

		//Log dependent variables XXX [KT] Check if gives performance bottleneck
		String aux1 = "";
		String aux2 = "";
		String aux3 = "";
		
		boolean flagLog = false;
		
		// Find a post which is not written by this agent
		do {
			postId = m.random.nextInt(onlinePostsByBrand[brandId].size());
			clientId = (Integer) onlinePostsByBrand[brandId].get(postId)[POST_AGENT_ID_INDEX];
		} while(customer.clientId == clientId);		
		// Set the values
		Object[] currentPost = onlinePostsByBrand[brandId].get(postId);		
		double postedAttributeValue = (double) currentPost[POST_ATTRIBUTE_VAL_INDEX];
		int postedAttributeId = (int) currentPost[POST_ATTRIBUTE_ID_INDEX];
		int postedSegmentId = (Integer) currentPost[POST_SEGMENT_ID_INDEX];
		
		// Check if customer has awareness
		// If not, process...		
		if(!customer.getAwarenessOfBrand(brandId)) {
			double r = m.random.nextDouble(); // [0, 1)
			// If impacts, change awareness
			if(r < awarenessImpact[postedSegmentId]) {
				customer.gainAwareness(m,brandId, step);
			// If no awareness impact, just log it	
			} else if(LOG_INFO){
				aux1 = " NO IMPACT";				
			}
		// If customer has awareness, just log it.	
		} else if(LOG_INFO){
			aux1 = " AGENT HAS AWARENESS";			
		}
		
		//Check agent structures
		customer.checkTouchpointPerceptionIncrement(
				AbstractTouchPoint.POST, 
				brandId
			);
		
		double currentValue = 
				customer.getPerceptions()[brandId][postedAttributeId];
		
		double perceptionChange = 
				(postedAttributeValue-currentValue)
					* perceptionSpeed[postedSegmentId];
		if(perceptionChange!=0) {
			customer.changePerceptions(
				brandId,
				postedAttributeId,
				perceptionChange,
				AbstractTouchPoint.POST
			);
			
			// If there is a perception change, log it.
			if(LOG_INFO) {
				if(!flagLog){
					flagLog = true;
					aux2 = " [" + brandId + ":";
				}
				aux2 += " " + postedAttributeId;
				aux3 += " " + String.format("%.3f", perceptionChange);			
			}
		}
		
		double discussionHeat = discussionImpact[postedSegmentId];
		if(discussionHeat != 0.0) { 
			customer.applyDiscussionHeat(
				step, 
				discussionImpact[postedSegmentId],
				brandId,
				AbstractTouchPoint.POST
			);
		}
		
		if(LOG_INFO) {
			String aux4 = "";
			// Write awareness log to the file
			String logAwareness = "Step " + step + " READING ONLINE AWARENESS" 
					+ " agent " + customer.clientId + " segment " 
					+ customer.segmentId + " --> brand " + brandId + aux1;		
			logger.info(logAwareness);		
			// Write perception log to the file
			if(flagLog) {
				aux4 += aux2 + ";" + aux3 + "]";
				String logMessage = "Step " + step + " READING ONLINE PERCEPTION agent "
					+ customer.clientId + " segment " + customer.segmentId
					+ " --> [brand: attributes; values]" + aux4;
				logger.info(logMessage);
			} else {
				String logMessage = "Step " + step + " READING ONLINE PERCEPTION agent " 
						+ customer.clientId + " segment " + customer.segmentId
						+ " --> brand "	+ brandId + " NO PERCEPTIONS CHANGE";
				logger.info(logMessage);
			}	
		}
	}	
}
