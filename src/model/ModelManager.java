package model;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.util.MathUtils;

import model.socialnetwork.SocialNetwork.NetworkType;
import util.functions.Functions;


/**
 * ModelManager class defines setter methods for model properties that
 * may be chosen for calibration.
 * 
 * Currently, is not clear if every property considered by ModelManager
 * should be considered as calibrable. 
 * 
 * @author imoya
 *
 */
public class ModelManager {

	/**
	 * Model definition object managed by this instance.
	 */
	private ModelDefinition modelDefinitionInstance;
	
	private int[][] involvedDrivers;
	
	/**
	 * Creates a new model manager handling given model definition instance.
	 * @param instance model definition object for this instance.
	 */
	public ModelManager(ModelDefinition instance) {
		modelDefinitionInstance=instance;
		involvedDrivers = null;
	}
	
	/**
	 * Creates a new model manager handling given model definition instance
	 * and involved drivers.
	 * 
	 * @param instance model definition object for this instance.
	 * @param involvedDrivers drivers considered during normalization.
	 */
	public ModelManager(ModelDefinition instance, int[][] involvedDrivers) {
		modelDefinitionInstance=instance;
		this.involvedDrivers = involvedDrivers;
	}
	
	/**
	 * Sets/Gets emotional parameter to given value.
	 * @param emotional new emotional value.
	 */
	public void setEmotional(double emotional) {
		modelDefinitionInstance.emotional=emotional;
	}
	public double getEmotional() {
		return modelDefinitionInstance.emotional;
	}
	
	/**
	 * Sets involved parameter to given value.
	 * @param involved new involved value.
	 */
	public void setInvolved(double involved) {
		modelDefinitionInstance.involved=involved;
	}
	public double getInvolved() {
		return modelDefinitionInstance.involved;
	}

	///////////////////////////////////////////////////////////////////////////
	//Simulation
	///////////////////////////////////////////////////////////////////////////	
	
	/**
	 * Sets number steps for week to given value.
	 * @param stepsForWeek new number steps for week. 
	 */
	public void setStepsForWeek(int stepsForWeek) {
		modelDefinitionInstance.stepsForWeek=stepsForWeek;
	}
	public int getStepsForWeek() {
		return modelDefinitionInstance.stepsForWeek;
	}
	
	/**
	 * Sets the number of agents to given value.
	 * @param nrAgents new number of agents.
	 */
	public void setNrAgents(int nrAgents) {
		modelDefinitionInstance.numberOfAgents = nrAgents;
	}
	public int getNrAgents() {
		return modelDefinitionInstance.numberOfAgents;
	}

	///////////////////////////////////////////////////////////////////////////
	//Brands
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Sets supplied brand and attribute to given value for every simulation 
	 * step.
	 * 
	 * @param brand brand id.
	 * @param attribute attribute id.
	 * @param value new brand's attribute value.
	 */
	public void setBrandAttribute(
			int brand, 
			int attribute, 
			double value
			) {
		Arrays.fill(
				modelDefinitionInstance.brandAttributes[brand][attribute],
				value
			);
	}
	public double getBrandAttribute(
			int brand, 
			int attribute
			) {
		return modelDefinitionInstance.brandAttributes[brand][attribute][0];
	}
	
	/**
	 * Sets supplied brand and attribute to given value for given step.
	 * 
	 * @param brand brand id.
	 * @param attribute attribute id.
	 * @param step number of step.
	 * @param value new brand's attribute value for given step.
	 */
	public void setBrandAttribute(
			int brand, 
			int attribute,
			int step,
			double value
			) {
		modelDefinitionInstance.brandAttributes
			[brand][attribute][step] = value;
	}
	public double getBrandAttribute(
			int brand, 
			int attribute,
			int step
			) {
		return modelDefinitionInstance.brandAttributes
			[brand][attribute][step];
	}
	
	/**
	 * Sets initial penetration value for given brand.
	 * 
	 * @param brand brand id.
	 * @param value new initial penetration value.
	 */
	public void setBrandInitPenetration(
			int brand,
			double value
			) {
		modelDefinitionInstance.brandInitialPenetration[brand]=value;
	}		
	public double getBrandInitPenetration(
			int brand
			) {
		return modelDefinitionInstance.brandInitialPenetration[brand];
	}
	
	///////////////////////////////////////////////////////////////////////////
	//Segments	
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Sets awareness decay for given segment to supplied value.
	 * 
	 * @param segment segment id.
	 * @param value new awareness decay for given segment.
	 */
	public void setSegmentAwarenessDecay(
			int segment, 
			double value
			) {
		modelDefinitionInstance.awarenessDecay[segment]=value;
	}
	public double getSegmentAwarenessDecay(
			int segment
			) {
		return modelDefinitionInstance.awarenessDecay[segment];
	}
	
	/**
	 * Sets awareness decay for every segment to given value.
	 * 
	 * @param value new awareness decay value for every segment.
	 */
	public void setSegmentAwarenessDecay( 
			double value
			) {
		Arrays.fill(modelDefinitionInstance.awarenessDecay, value);
	}
	public double getSegmentAwarenessDecay( ) {
		return modelDefinitionInstance.awarenessDecay[0];
	}
	
	/**
	 * Sets initial awareness for supplied segment and brand to given value.
	 * 
	 * @param segment segment id.
	 * @param brand brand id.
	 * @param value new initial awareness value.
	 */
	public void setSegmentInitialAwareness(
			int segment, 
			int brand, 
			double value
			) {
		modelDefinitionInstance.initialAwareness[brand][segment]=value;
	}
	public double getSegmentInitialAwareness(
			int segment, 
			int brand
			) {
		return modelDefinitionInstance.initialAwareness[brand][segment];
	}	
	
	/**
	 * Sets supplied weight to given segment and attribute. Because drivers 
	 * need to be normalize, weights are normalized after.
	 * 
	 * @param segment segment id.
	 * @param attribute attribute id.
	 * @param value new driver value before normalization.
	 */
	public void setSegmentDrivers(
			int segment, 
			int attribute, 
			double value
		) {
		
		modelDefinitionInstance.drivers[segment][attribute]=value;
	}
	public double getSegmentDrivers(
			int segment, 
			int attribute
		) {
		return modelDefinitionInstance.drivers[segment][attribute];
	}
	
	public void normalizeDrivers() {
		if(involvedDrivers==null) {
			for (int i=0; i<modelDefinitionInstance.numberOfSegments; i++) {
				modelDefinitionInstance.drivers[i] = MathUtils.normalizeArray(
						modelDefinitionInstance.drivers[i], 
						Functions.TOTAL_AMOUNT_NORMALIZABLE_VALUE
					);
			}
		} else {
			
			for (int i=0; i<modelDefinitionInstance.numberOfSegments; i++) {
				if(involvedDrivers[i].length==0) continue;
				
				double amountNormalizable = Functions.TOTAL_AMOUNT_NORMALIZABLE_VALUE;
				
				for (int j=0; j<modelDefinitionInstance.drivers[i].length; j++) {
					if(!ArrayUtils.contains(involvedDrivers[i], j)) {
						amountNormalizable-= modelDefinitionInstance.drivers[i][j];
					}
				}
					
				int intermediate = involvedDrivers[i].length;
				double[] intermediatevalues = new double [intermediate];
				for (int j=0; j<intermediate; j++) {
					int index = involvedDrivers[i][j];
					intermediatevalues[j]=modelDefinitionInstance.drivers[i][index];
				}
				
				intermediatevalues = MathUtils.normalizeArray(
						intermediatevalues, 
						amountNormalizable
					);
				
				for (int j=0; j<intermediate; j++) {
					int index = involvedDrivers[i][j];
					modelDefinitionInstance.drivers[i][index] = intermediatevalues[j];
				}
			}
		}
	}
	
	/**
	 * Sets supplied weight for every segment at given attribute. Because 
	 * drivers need to be normalize, weights are normalized after.
	 * 
	 * @param attribute attribute id.
	 * @param value new driver value for every segment (before normalization).
	 */
	public void setSegmentDrivers(
			int attribute, 
			double value
			) {
		int numSegments = modelDefinitionInstance.numberOfSegments;
		for (int i=0; i<numSegments; i++) {
			setSegmentDrivers(i, attribute, value);
		}
	}
	
	public double getSegmentDrivers(
			int attribute
			) {
		return modelDefinitionInstance.drivers[0][attribute];
	}
	
	/**
	 * Sets initial perception for given segment, brand and attribute to 
	 * supplied value.
	 * 
	 * @param segment segment id.
	 * @param brand brand id.
	 * @param attribute attribute id.
	 * @param value new initial value for given segment, brand and attribute.
	 */
	public void setSegmentInitialPerceptions(
			int segment, 
			int brand, 
			int attribute, 
			double value
			) {
		modelDefinitionInstance.initialPerceptions[segment][brand][attribute]=value;
	}
	
	public double getSegmentInitialPerceptions(
			int segment, 
			int brand, 
			int attribute
			) {
		return modelDefinitionInstance.initialPerceptions[segment][brand][attribute];
	}
	
	/**
	 * Sets initial perception for every segment at given brand and attribute 
	 * to supplied value.
	 * 
	 * @param brand brand id.
	 * @param attribute attribute id.
	 * @param value new initial perception value for every segment.
	 */
	public void setSegmentInitialPerceptions(
			int brand, 
			int attribute, 
			double value
			) {
		int segments = modelDefinitionInstance.initialPerceptions.length;
		for (int i=0; i<segments; i++) {
			modelDefinitionInstance.initialPerceptions[i][brand][attribute]=value;
		}
	}

	public double getSegmentInitialPerceptions(
			int brand, 
			int attribute
			) {
		return modelDefinitionInstance.initialPerceptions[0][brand][attribute];
	}

	
	///////////////////////////////////////////////////////////////////////////
	//Social Network
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Sets the model network type if true, random network, else scale-free 
	 * network.
	 * 
	 * @param networkType if true, random network, else scale-free network.
	 */
	public void setSocialNetworkType(boolean networkType) {
		// if true, random network, else scale-free network
		if(networkType) {
			modelDefinitionInstance.typeOfNetwork = NetworkType.RANDOM_NETWORK_SEGMENTS;			
		} else {
			modelDefinitionInstance.typeOfNetwork = NetworkType.SCALE_FREE_NETWORK;			
		}
	}
	public boolean getSocialNetworkType() {
		return modelDefinitionInstance.typeOfNetwork ==
				NetworkType.RANDOM_NETWORK_SEGMENTS;
	}	
	
	/**
	 * Sets connectivity for given segment to supplied value.
	 * 
	 * @param segment segment id.
	 * @param value connectivity value.
	 */
	public void setSocialNetworkSegmentConnectivity(
			int segment, 
			double value
			) {
		modelDefinitionInstance.womSegmentConnectivity[segment]=value;
	}
	public double getSocialNetworkSegmentConnectivity(
			int segment
			) {
		return modelDefinitionInstance.womSegmentConnectivity[segment];
	}
	
	/**
	 * Sets influence of given first segment over given second segment to given 
	 * value.
	 * 
	 * @param firstSegment influencing segment id.
	 * @param secondSegment influenced segment id.
	 * @param value influence value for first segment over second segment.
	 */
	public void setSegmentInfluences(
			int firstSegment, 
			int secondSegment, 
			double value
			) {
		modelDefinitionInstance.womSegmentInfluences[firstSegment][secondSegment]=value;
	}
	public double getSegmentInfluences(
			int firstSegment, 
			int secondSegment
			) {
		return modelDefinitionInstance.womSegmentInfluences[firstSegment][secondSegment];
	}
	
	/**
	 * Sets given segment talking probability to supplied value.
	 * 
	 * @param segment segment id.
	 * @param value new talking probability for given segment.
	 */
	public void setSegmentTalking(
			int segment, 
			double value
			) {
		modelDefinitionInstance.womTalkingProbability[segment]=value;
	}
	public double getSegmentTalking(
			int segment
			) {
		return modelDefinitionInstance.womTalkingProbability[segment];
	}
	
	/**
	 * Sets every segment talking probability to supplied value.
	 * 
	 * @param value new talking probability for every segment.
	 */
	public void setSegmentTalking(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.womTalkingProbability, value);
	}
	public double getSegmentTalking(
			) {
		return modelDefinitionInstance.womTalkingProbability[0];
	}
	
	/**
	 * Sets Word of Mouth awareness impact for given segment to supplied value.
	 * 
	 * @param segment segment id.
	 * @param value new awareness impact value for given segment.
	 */
	public void setWOMAwarenessImpact(
			int segment, 
			double value
			) {
		modelDefinitionInstance.womAwarenessImpact[segment]=value;
	}
	public double getWOMAwarenessImpact(
			int segment
			) {
		return modelDefinitionInstance.womAwarenessImpact[segment];
	}
	
	/**
	 * Sets Word of Mouth awareness impact for every segment to supplied value.
	 * 
	 * @param value new awareness impact value for every segment.
	 */
	public void setWOMAwarenessImpact(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.womAwarenessImpact,value);
	}
	public double getWOMAwarenessImpact( ) {
		return modelDefinitionInstance.womAwarenessImpact[0];
	}
	
	/**
	 * Sets Word of Mouth discussion heat impact for given segment to supplied 
	 * value.
	 * 
	 * @param segment segment id.
	 * @param value new discussion heat impact value for given segment.
	 */
	public void setWOMDiscusionHeatImpact(
			int segment, 
			double value
			) {
		modelDefinitionInstance.womDiscussionHeatImpact[segment]=value;
	}
	public double getWOMDiscusionHeatImpact(
			int segment
			) {
		return modelDefinitionInstance.womDiscussionHeatImpact[segment];
	}
	
	/**
	 * Sets Word of Mouth discussion heat impact for every segment to supplied 
	 * value.
	 * 
	 * @param value new discussion heat impact value for every segment.
	 */
	public void setWOMDiscusionHeatImpact(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.womDiscussionHeatImpact,value);
	}
	public double getWOMDiscusionHeatImpact( ) {
		return modelDefinitionInstance.womDiscussionHeatImpact[0];
	}
	
	/**
	 * Sets Word of Mouth discussion heat decay for given segment to supplied 
	 * value.
	 * 
	 * @param segment segment id.
	 * @param value new discussion heat decay value for given segment.
	 */
	public void setWOMDiscusionHeatDecay(
			int segment, 
			double value
			) {
		modelDefinitionInstance.womDiscussionHeatDecay[segment]=value;
	}
	public double getWOMDiscusionHeatDecay(
			int segment
			) {
		return modelDefinitionInstance.womDiscussionHeatDecay[segment];
	}
	
	/**
	 * 
	 * 
	 * @param value new discussion heat decay value for every segment.
	 */
	public void setWOMDiscusionHeatDecay(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.womDiscussionHeatDecay,value);
	}
	public double getWOMDiscusionHeatDecay( ) {
		return modelDefinitionInstance.womDiscussionHeatDecay[0];
	}
	
	/**
	 * Sets Word of Mouth perception speed for given segment to supplied value.
	 * 
	 * @param segment segment id.
	 * @param value new perception speed value for given segment.
	 */
	public void setWOMPerceptionSpeed(
			int segment, 
			double value
			) {
		modelDefinitionInstance.womPerceptionSpeed[segment]=value;
	}
	public double getWOMPerceptionSpeed(
			int segment
			) {
		return modelDefinitionInstance.womPerceptionSpeed[segment];
	}
	
	/**
	 * Sets Word of Mouth perception speed for every segment to supplied value.
	 * 
	 * @param value new perception speed value for every segment.
	 */
	public void setWOMPerceptionSpeed(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.womPerceptionSpeed,value);
	}
	public double getWOMPerceptionSpeed( ) {
		return modelDefinitionInstance.womPerceptionSpeed[0];
	}
	
	/**
	 *  Sets Word of Mouth perception decay for given segment to supplied value.
	 *  
	 * @param segment segment id.
	 * @param value new perception decay value for given segment.
	 */
	public void setWOMPerceptionDecay(
			int segment, 
			double value
			) {
		modelDefinitionInstance.womPerceptionDecay[segment]=value;
	}
	public double getWOMPerceptionDecay(
			int segment
			) {
		return modelDefinitionInstance.womPerceptionDecay[segment];
	}
	
	/**
	 * Sets Word of Mouth perception decay for every segment to supplied value.
	 * 
	 * @param value new perception decay value for every segment.
	 */
	public void setWOMPerceptionDecay(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.womPerceptionDecay,value);
	}
	public double getWOMPerceptionDecay( ) {
		return modelDefinitionInstance.womPerceptionDecay[0];
	}
	
	///////////////////////////////////////////////////////////////////////////
	//TouchPointOwned
	///////////////////////////////////////////////////////////////////////////
	
	public void setTouchPointWeeklyReachMax(
			int touchpoint, 
			int segment, 
			double value
			) {
		modelDefinitionInstance.touchPointsWeeklyReachMax[touchpoint][segment]=value;
	}
	public double getTouchPointWeeklyReachMax(
			int touchpoint, 
			int segment
			) {
		return modelDefinitionInstance.touchPointsWeeklyReachMax[touchpoint][segment];
	}
	
	public void setTouchPointWeeklyReachMax(
			int touchpoint,
			double value
			) {
		Arrays.fill(modelDefinitionInstance.touchPointsWeeklyReachMax[touchpoint],value);
	}
	public double getTouchPointWeeklyReachMax(
			int touchpoint
			) {
		return modelDefinitionInstance.touchPointsWeeklyReachMax[touchpoint][0];
	}
	
	public void setTouchPointAnnualReachMax(
			int touchpoint, 
			int segment, 
			double value
			) {
		modelDefinitionInstance.touchPointsAnnualReachMax[touchpoint][segment]=value;
	}
	public double getTouchPointAnnualReachMax(
			int touchpoint, 
			int segment
			) {
		return modelDefinitionInstance.touchPointsAnnualReachMax[touchpoint][segment];
	}
	
	public void setTouchPointAnnualReachMax(
			int touchpoint,
			double value
			) {
		Arrays.fill(modelDefinitionInstance.touchPointsAnnualReachMax[touchpoint],value);
	}
	public double getTouchPointAnnualReachMax(
			int touchpoint
			) {
		return modelDefinitionInstance.touchPointsAnnualReachMax[touchpoint][0];
	}
	
	public void setTouchPointAnnualReachSpeed(
			int touchpoint, 
			int segment, 
			double value
			) {
		modelDefinitionInstance.touchPointsAnnualReachSpeed[touchpoint][segment]=value;
	}
	public double getTouchPointAnnualReachSpeed(
			int touchpoint, 
			int segment
			) {
		return modelDefinitionInstance.touchPointsAnnualReachSpeed[touchpoint][segment];
	}
	
	public void setTouchPointAnnualReachSpeed(
			int touchpoint,
			double value
			) {
		Arrays.fill(modelDefinitionInstance.touchPointsAnnualReachSpeed[touchpoint],value);
	}
	public double getTouchPointAnnualReachSpeed(
			int touchpoint
			) {
		return modelDefinitionInstance.touchPointsAnnualReachSpeed[touchpoint][0];
	}
	
	public void setTouchPointPerceptionPotential(
			int touchpoint, 
			int segment, 
			double value
			) {
		modelDefinitionInstance.touchPointsPerceptionPotential[touchpoint][segment]=value;
	}
	public double getTouchPointPerceptionPotential(
			int touchpoint, 
			int segment
			) {
		return modelDefinitionInstance.touchPointsPerceptionPotential[touchpoint][segment];
	}
	
	public void setTouchPointPerceptionPotential(
			int touchpoint,
			double value
			) {
		Arrays.fill(modelDefinitionInstance.touchPointsPerceptionPotential[touchpoint],value);
	}
	public double getTouchPointPerceptionPotential(
			int touchpoint
			) {
		return modelDefinitionInstance.touchPointsPerceptionPotential[touchpoint][0];
	}
	
	public void setTouchPointPerceptionSpeed(
			int touchpoint, 
			int segment, 
			double value
			) {
		modelDefinitionInstance.touchPointsPerceptionSpeed[touchpoint][segment]=value;
	}
	public double getTouchPointPerceptionSpeed(
			int touchpoint, 
			int segment
			) {
		return modelDefinitionInstance.touchPointsPerceptionSpeed[touchpoint][segment];
	}
	
	public void setTouchPointPerceptionSpeed(
			int touchpoint,
			double value
			) {
		Arrays.fill(modelDefinitionInstance.touchPointsPerceptionSpeed[touchpoint],value);
	}
	public double getTouchPointPerceptionSpeed(
			int touchpoint
			) {
		return modelDefinitionInstance.touchPointsPerceptionSpeed[touchpoint][0];
	}
	
	public void setTouchPointPerceptionDecay(
			int touchpoint, 
			int segment, 
			double value
			) {
		modelDefinitionInstance.touchPointsPerceptionDecay[touchpoint][segment]=value;
	}
	public double getTouchPointPerceptionDecay(
			int touchpoint, 
			int segment
			) {
		return modelDefinitionInstance.touchPointsPerceptionDecay[touchpoint][segment];
	}
	
	public void setTouchPointPerceptionDecay(
			int touchpoint,
			double value
			) {
		Arrays.fill(modelDefinitionInstance.touchPointsPerceptionDecay[touchpoint],value);
	}
	public double getTouchPointPerceptionDecay(
			int touchpoint
			) {
		return modelDefinitionInstance.touchPointsPerceptionDecay[touchpoint][0];
	}
	
	public void setTouchPointAwarenessImpact(
			int touchpoint, 
			int segment, 
			double value
			) {
		modelDefinitionInstance.touchPointsAwarenessImpact[touchpoint][segment]=value;
	}
	public double getTouchPointAwarenessImpact(
			int touchpoint, 
			int segment
			) {
		return modelDefinitionInstance.touchPointsAwarenessImpact[touchpoint][segment];
	}
	
	public void setTouchPointAwarenessImpact(
			int touchpoint,
			double value
			) {
		Arrays.fill(modelDefinitionInstance.touchPointsAwarenessImpact[touchpoint],value);
	}
	public double getTouchPointAwarenessImpact(
			int touchpoint
			) {
		return modelDefinitionInstance.touchPointsAwarenessImpact[touchpoint][0];
	}
	
	public void setTouchPointDiscusionHeatImpact(
			int touchpoint, 
			int segment, 
			double value
			) {
		modelDefinitionInstance.touchPointsDiscusionHeatImpact[touchpoint][segment]=value;
	}
	public double getTouchPointDiscusionHeatImpact(
			int touchpoint, 
			int segment
			) {
		return modelDefinitionInstance.touchPointsDiscusionHeatImpact[touchpoint][segment];
	}
	
	public void setTouchPointDiscusionHeatImpact(
			int touchpoint,
			double value
			) {
		Arrays.fill(modelDefinitionInstance.touchPointsDiscusionHeatImpact[touchpoint],value);
	}
	public double getTouchPointDiscusionHeatImpact(
			int touchpoint
			) {
		return modelDefinitionInstance.touchPointsDiscusionHeatImpact[touchpoint][0];
	}
	
	public void setTouchPointDiscusionHeatDecay(
			int touchpoint, 
			int segment, 
			double value
			) {
		modelDefinitionInstance.touchPointsDiscusionHeatDecay[touchpoint][segment]=value;
	}
	public double getTouchPointDiscusionHeatDecay(
			int touchpoint, 
			int segment
			) {
		return modelDefinitionInstance.touchPointsDiscusionHeatDecay[touchpoint][segment];
	}
	
	public void setTouchPointDiscusionHeatDecay(
			int touchpoint,
			double value
			) {
		Arrays.fill(modelDefinitionInstance.touchPointsDiscusionHeatDecay[touchpoint],value);
	}
	public double getTouchPointDiscusionHeatDecay(
			int touchpoint
			) {
		return modelDefinitionInstance.touchPointsDiscusionHeatDecay[touchpoint][0];
	}
	
	///////////////////////////////////////////////////////////////////////////
	//Marketing Plan
	///////////////////////////////////////////////////////////////////////////
	
	public void setQualityAttribute(
			int tp, 
			int brand,
			int period,
			int attribute, 
			double value
			) {
		modelDefinitionInstance.touchPointsQuality[tp][brand][period][attribute]=value;
	}
	public double getQualityAttribute(
			int tp, 
			int brand,
			int period,
			int attribute
			) {
		return modelDefinitionInstance.touchPointsQuality[tp][brand][period][attribute];
	}
	
	public void setEmphasisAttribute(
			int tp, 
			int brand,
			int period,
			int attribute,			
			double value
			) {
		modelDefinitionInstance.touchPointsEmphasis[tp][brand][period][attribute]=value;
	}
	public double getEmphasisAttribute(
			int tp, 
			int brand,
			int period,
			int attribute
			) {
		return modelDefinitionInstance.touchPointsEmphasis[tp][brand][period][attribute];
	}
	
	///////////////////////////////////////////////////////////////////////////
	//Product Usage
	///////////////////////////////////////////////////////////////////////////
	
	public void setUsageFrequency(
			int segment,
			double value
			) {
		modelDefinitionInstance.usageFrequencies[segment]=(int)value;
	}
	public double getUsageFrequency(
			int segment
			) {
		return (double)modelDefinitionInstance.usageFrequencies[segment];
	}
	
	public void setUsageFrequency(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.usageFrequencies,(int)value);
	}
	public double getUsageFrequency( ) {
		return (double)modelDefinitionInstance.usageFrequencies[0];
	}
	
	public void setUsagePerceptionSpeed(
			int segment,
			double value
			) {
		modelDefinitionInstance.usagePerceptionSpeed[segment]=value;
	}
	public double getUsagePerceptionSpeed(
			int segment
			) {
		return modelDefinitionInstance.usagePerceptionSpeed[segment];
	}
	
	public void setUsagePerceptionSpeed(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.usagePerceptionSpeed,value);
	}
	public double getUsagePerceptionSpeed( ) {
		return modelDefinitionInstance.usagePerceptionSpeed[0];
	}
	
	public void setUsagePerceptionDecay(
			int segment,
			double value
			) {
		modelDefinitionInstance.usagePerceptionDecay[segment]=value;
	}
	public double getUsagePerceptionDecay(
			int segment
			) {
		return modelDefinitionInstance.usagePerceptionDecay[segment];
	}
	
	public void setUsagePerceptionDecay(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.usagePerceptionDecay,value);
	}
	public double getUsagePerceptionDecay( ) {
		return modelDefinitionInstance.usagePerceptionDecay[0];
	}
	
	public void setUsageAwarenessImpact(
			int segment,
			double value
			) {
		modelDefinitionInstance.usageAwarenessImpact[segment]=value;
	}
	public double getUsageAwarenessImpact(
			int segment
			) {
		return modelDefinitionInstance.usageAwarenessImpact[segment];
	}
	
	public void setUsageAwarenessImpact(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.usageAwarenessImpact,value);
	}
	public double getUsageAwarenessImpact( ) {
		return modelDefinitionInstance.usageAwarenessImpact[0];
	}
	
	public void setUsageDiscusionHeatImpact(
			int segment,
			double value
			) {
		modelDefinitionInstance.usageDiscussionHeatImpact[segment]=value;
	}
	public double getUsageDiscusionHeatImpact(
			int segment
			) {
		return modelDefinitionInstance.usageDiscussionHeatImpact[segment];
	}
	
	public void setUsageDiscusionHeatImpact(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.usageDiscussionHeatImpact,value);
	}
	public double getUsageDiscusionHeatImpact( ) {
		return modelDefinitionInstance.usageDiscussionHeatImpact[0];
	}
	
	public void setUsageDiscusionHeatDecay(
			int segment,
			double value
			) {
		modelDefinitionInstance.usageDiscussionHeatDecay[segment]=value;
	}
	public double getUsageDiscusionHeatDecay(
			int segment
			) {
		return modelDefinitionInstance.usageDiscussionHeatDecay[segment];
	}
	
	public void setUsageDiscusionHeatDecay(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.usageDiscussionHeatDecay,value);
	}
	public double getUsageDiscusionHeatDecay( ) {
		return modelDefinitionInstance.usageDiscussionHeatDecay[0];
	}
	
	///////////////////////////////////////////////////////////////////////////
	//Online Posting
	///////////////////////////////////////////////////////////////////////////
	
	public void setSegmentPosting(
			int segment, 
			double value
			) {
		modelDefinitionInstance.onlinePostingProbabilities[segment]=value;
	}
	public double getSegmentPosting(
			int segment
			) {
		return modelDefinitionInstance.onlinePostingProbabilities[segment];
	}
	
	public void setSegmentReading(
			int segment, 
			double value
			) {
		modelDefinitionInstance.onlineReadingProbabilities[segment]=value;
	}
	public double getSegmentReading(
			int segment
			) {
		return modelDefinitionInstance.onlineReadingProbabilities[segment];
	}
	
	public void setOnlinePerceptionSpeed(
			int segment,
			double value
			) {
		modelDefinitionInstance.onlinePerceptionSpeed[segment]=value;
	}
	public double getOnlinePerceptionSpeed(
			int segment
			) {
		return modelDefinitionInstance.onlinePerceptionSpeed[segment];
	}
	
	public void setOnlinePerceptionDecay(
			int segment,
			double value
			) {
		modelDefinitionInstance.onlinePerceptionDecay[segment]=value;
	}
	public double getOnlinePerceptionDecay(
			int segment
			) {
		return modelDefinitionInstance.onlinePerceptionDecay[segment];
	}
	
	public void setOnlineAwarenessImpact(
			int segment,
			double value
			) {
		modelDefinitionInstance.onlineAwarenessImpact[segment]=value;
	}
	public double getOnlineAwarenessImpact(
			int segment
			) {
		return modelDefinitionInstance.onlineAwarenessImpact[segment];
	}
	
	public void setOnlineDiscusionHeatImpact(
			int segment,
			double value
			) {
		modelDefinitionInstance.onlineDiscussionHeatImpact[segment]=value;
	}
	public double getOnlineDiscusionHeatImpact(
			int segment
			) {
		return modelDefinitionInstance.onlineDiscussionHeatImpact[segment];
	}
	
	public void setOnlineDiscusionHeatDecay(
			int segment,
			double value
			) {
		modelDefinitionInstance.onlineDiscussionHeatDecay[segment]=value;
	}
	public double getOnlineDiscusionHeatDecay(
			int segment
			) {
		return modelDefinitionInstance.onlineDiscussionHeatDecay[segment];
	}
	
	public void setSegmentPosting(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.onlinePostingProbabilities,value);
	}
	public double getSegmentPosting( ) {
		return modelDefinitionInstance.onlinePostingProbabilities[0];
	}
	
	public void setSegmentReading(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.onlineReadingProbabilities,value);
	}
	public double getSegmentReading( ) {
		return modelDefinitionInstance.onlineReadingProbabilities[0];
	}
	
	public void setOnlinePerceptionSpeed(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.onlinePerceptionSpeed,value);
	}
	public double getOnlinePerceptionSpeed( ) {
		return modelDefinitionInstance.onlinePerceptionSpeed[0];
	}
	
	public void setOnlinePerceptionDecay(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.onlinePerceptionDecay,value);
	}
	public double getOnlinePerceptionDecay( ) {
		return modelDefinitionInstance.onlinePerceptionDecay[0];
	}
	
	public void setOnlineAwarenessImpact(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.onlineAwarenessImpact,value);
	}
	public double getOnlineAwarenessImpact( ) {
		return modelDefinitionInstance.onlineAwarenessImpact[0];
	}
	
	public void setOnlineDiscusionHeatImpact(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.onlineDiscussionHeatImpact,value);
	}
	public double getOnlineDiscusionHeatImpact( ) {
		return modelDefinitionInstance.onlineDiscussionHeatImpact[0];
	}
	
	public void setOnlineDiscusionHeatDecay(
			double value
			) {
		Arrays.fill(modelDefinitionInstance.onlineDiscussionHeatDecay,value);
	}
	public double getOnlineDiscusionHeatDecay( ) {
		return modelDefinitionInstance.onlineDiscussionHeatDecay[0];
	}
	
	public void setMValue(
			int segment, 
			double value
			) {
		double newConnectivity = (1.0/8.0)*value;
		modelDefinitionInstance.womSegmentConnectivity[segment]=newConnectivity;
	}
	public double getMValue(
			int segment
			) {
		double translated = modelDefinitionInstance.womSegmentConnectivity[segment] * 8; 
		return translated;
	}
	
	/**
	 * Availability
	 */
	
	public void setAverageAvailabilityByBrand(
			int brand,
			double value
			) {
		Arrays.fill(modelDefinitionInstance.availabilityByBrandAndStep[brand],value);
	}
	public double getAverageAvailabilityByBrand(int brand) {
		return modelDefinitionInstance.availabilityByBrandAndStep[brand][0];
	}
}
