package model.touchpoints.earned;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Brand;
import model.Model;
import model.customer.Agent;
import util.functions.ArrayFunctions;
import util.functions.Functions;
import util.random.Randomizer;

/**
 * ProductUsage bean class carries the values related to this touch 
 * point.
 * 
 * Brands experiencing is also included at this level.
 * 
 * @author imoya
 *
 */
public class ProductUsage extends AbstractTouchPoint {
	
	/** 
	 * Logger instance for ProductUsage class.
	 */
	private final static Logger logger = 
		LoggerFactory.getLogger(ProductUsage.class);
	
	/**
	 * Logging flag for info level.
	 */
	private final static boolean LOG_INFO = logger.isInfoEnabled();

	/**
	 * The minimum step frequency is the daily one, that equals to 
	 * one simulation step.
	 */
	public static final int DAILY_FREQUENCY = 1;
	
	/**
	 * Usage Frequency stores the number of steps that define the
	 * interval between product uses for agents from different 
	 * segments.
	 */
	public final int[] usageFrequency;
	
	/**
	 * Brand beans stored by brand id.
	 */
	public final Brand[] brands;
	
	/**
	 * Product attributes that will be skipped by product usage.
	 */
	public final boolean[] intangible;
	
	/**
	 * Creates a Product usage bean storing provided parameters.
	 * 
	 * @param usageFrequency - product usage frequencies by segment.
	 * @param perceptionSpeed - perception speed by segment.
	 * @param perceptionDecay - perception decay by segment.
	 * @param awarenessImpact - awareness impact by segment.
	 * @param discussionHeatImpact - discussion heat impact by segment.
	 * @param brands - Brand beans by brand id.
	 * @param tangibleAttributes - intangible attributes.
	 */
	public ProductUsage(
			int[] usageFrequency, 
			double[] perceptionSpeed,
			double[] perceptionDecay,
			double[] awarenessImpact,
			double[] discussionHeatImpact,
			Brand[] brands,
			boolean[] tangibleAttributes
			) {
		super(
			perceptionSpeed,
			awarenessImpact,
			discussionHeatImpact
		);
		this.usageFrequency = usageFrequency;
		this.brands=brands;
		this.intangible = tangibleAttributes;
	}

	/**
	 * Randomly generates the initial items for calling agent using the
	 * initial penetration parameter and the instance of randomizer used 
	 * at current simulation.
	 * 
	 * @param initialPenetration - values for initial brand penetration.
	 * @param randomizer - randomizer instance used by the current model.
	 * @return the probabilistic starting brands for the costumer
	 */
	public final static boolean[] generateInitialItems(
			double[] initialPenetration,
			Randomizer randomizer
			) {
		return Functions.checkProbabilities(
				initialPenetration, randomizer);
	}

	/**
	 * Creates an usage planning based on the start step and the 
	 * frequency of use. Then, use events are grouped using the 
	 * maximum number of steps by week for current simulation.
	 * 
	 * @param frecuencyOfUse - frequency of use for this agent's 
	 * segment.
	 * @param randomizer - current randomizer for the simulation.
	 * @param startStep - initial step when product experience is 
	 * being planned.
	 * @param simulationStepLength - total number of simulation steps.
	 * @param stepsByWeek - number of steps for every week for current
	 * simulation.
	 * @return the use planning as a byte array.
	 */
	public static byte[] generateUsagePlanning(
			int frecuencyOfUse, 
			Randomizer randomizer, 
			int startStep, 
			int simulationStepLength,
			int stepsByWeek
		) {
		boolean[] useEvents= new boolean[simulationStepLength];

		/*
		 * Schedule product usage by day.
		 */
		if(frecuencyOfUse>DAILY_FREQUENCY) {
			while(startStep<simulationStepLength) {
				int step=startStep+randomizer.nextInt(frecuencyOfUse);
				if(step<simulationStepLength) {
					useEvents[step]=true;
				}
				startStep+=frecuencyOfUse;
			} 
		}
		else {
			Arrays.fill(useEvents, true);
		}
		/*
		 * Group uses by stepsByWeek
		 */
		double stepIncrement = Model.DAYS_OF_WEEK/(double)stepsByWeek;
		int simulationSteps =(int) (simulationStepLength/stepIncrement);
		byte[] usePlanning = new byte[simulationSteps];
		
		for (int i=0; i<simulationSteps; i++) {
			int begin = (int)(i*stepIncrement);
			int end = begin + (int)stepIncrement;
			usePlanning[i] = (byte)ArrayFunctions.addArraySegment(
					useEvents, begin, end);
		}
		
		return usePlanning;
	}
	
	/**
	 * The given customer experiences the product 'hits' times.
	 * 
	 * @param customer - the customer experiencing the product.
	 * @param m - current model instance.
	 * @param step - current step when product is experienced.
	 * @param hits - number of times the product is experienced.
	 */
	public void useProduct(
			Agent customer, 
			Model m, 
			int step, 
			byte hits
		) {
		for (int i=0; i<hits; i++) {
			int brandPurchased = ArrayFunctions.selectRandomIndex(
					customer.hasBrand(), m.random);
			modifyAgent(customer,m,step,brandPurchased);
		}
	}

	/**
	 * The agent selects one of the brands he owns and experiences it, 
	 * changing his perceptions, awareness and talking probability. 
	 * 
	 * @param customer - the agent experiencing the possessed brand.
	 * @param m - current model instance.
	 * @param step - current simulation step.
	 */
	@Override
	public void modifyAgent(
			Agent customer, 
			Model m, 
			int step,
			int brandId
		) {
		//Customer variables
//		
//		boolean [] inventory = customer.hasBrand();
//		
//		
		int segmentId = customer.segmentId;
		Brand product = brands[brandId];
		
		//Log dependent variables
		String logHeader = null;
		String logBody = null;		
		if(LOG_INFO) {
			logHeader = " [" + brandId + ":";
			logBody = "";
		}
		
		//Check perception structures
		customer.checkTouchpointPerceptionIncrement(
				AbstractTouchPoint.USE, 
				brandId
			);
		
		//For every brand attribute.
		for (int i=0; i<product.attributeValues.length; i++) {
			//Intangible attributes do not modify agent state
			if(intangible[i]) {
				continue;
			}
				
			double productValue = product.attributeValues[i][step];
			double currentValue = customer.getPerceptions()[brandId][i];

			double perceptionChange = (productValue-currentValue) 
					* perceptionSpeed[segmentId];
			if(perceptionChange!= 0.0) {
				customer.changePerceptions(
						brandId, 
					i, 
					perceptionChange, 
					AbstractTouchPoint.USE
				);
				// If there is a perception change, log it.
				if(LOG_INFO) {
					logHeader += " " + i;
					logBody += " " + String.format("%.3f", perceptionChange);			
				}
			}
			if(discussionImpact[segmentId]!= 0.0) {
				customer.applyDiscussionHeat(
					step,
					discussionImpact[segmentId],
					brandId,
					AbstractTouchPoint.USE
				);
			}			
		}

		// Awareness
		boolean awarenessChange = customer.getAwarenessOfBrand(brandId);
		// Check if the customer has awareness of that product
		// If not, process...
		if(!awarenessChange) {
			awarenessChange = awarenessChange != 
					checkAwarenessImpact(customer,m,brandId, 
							awarenessImpact[segmentId], step);	
		}
		
		if(LOG_INFO) {
			String logAwareness;
			if(awarenessChange) {
				logAwareness=" AGENT HAS AWARENESS";
			} else {
				logAwareness=" NO IMPACT";
			}
			// Write awareness log to the file
			String awarenessMessage = "Step " + step + " PRODUCT USAGE AWARENESS" 
					+ " agent " + customer.clientId + " segment " 
					+ customer.segmentId + " --> brand " + brandId + logAwareness;		
			logger.info(awarenessMessage);
			// Write perception log to the file
			if(!logBody.equals("")) {
				String logText = logHeader + ";" + logBody + "]";
				String logMessage = "Step " + step + " PRODUCT USAGE PERCEPTION agent "
					+ customer.clientId + " segment " + customer.segmentId
					+ " --> [brand: attributes; values]" + logText;
				logger.info(logMessage);
			} else {
				String logMessage = "Step " + step + " PRODUCT USAGE PERCEPTION agent " 
					+ customer.clientId + " segment " + customer.segmentId 
					+ " --> brand "	+ brandId + " NO PERCEPTIONS CHANGE";
				logger.info(logMessage);		
			}
		}
	}
}
