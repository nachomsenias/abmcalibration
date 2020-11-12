package model;

import java.util.Arrays;

/**
 * ModelStepTranslator objects translate time-dependent semantics. This
 * translation is needed to ensure that Model execution is comparable
 * when stepping with different number of steps for week.
 * 
 * @author imoya
 *
 */
public class ModelStepTranslator {
	
	/*
	 * Attributes for this class are intended to be used at ModelBuilder class,
	 * so the level of visibility chosen is package visibility.
	 */
	
	//Awareness decay
	double[] 	awarenessDecay;
	//WoM
	double [] 	womDiscussionHeatDecay;	
	double [] 	womPerceptionDecay;
	double [] 	womTalkingProbability;
	//ProductUsage
	double [] 	usagePerceptionDecay;
	double [] 	usageDiscussionHeatDecay;
	//OnlinePosting&Reading
	double[] 	onlinePostingProbabilities;
	double[] 	onlineReadingProbabilities;

	double[] 	onlinePerceptionDecay;
	double[] 	onlineDiscussionHeatDecay;	
	//PaidTouchPoints
	double [][]	touchPointsPerceptionDecay;
	double [][]	touchPointsDiscussionHeatDecay;

	byte [][][]	creativityByStep;
	
	double [][] availability;
	
	//Brands
	double [][][]	brandAttributes;
	
	//Sales
	int 		buyingDecisionCycle;
	
	/**
	 * Translates time-dependent semantics for given step length values.
	 * 
	 * @param md
	 * @param stepsByWeek
	 */
	public ModelStepTranslator(ModelDefinition md, int stepsByWeek) {
		
		int segments = md.numberOfSegments;
		
		int brands = md.numberOfBrands;
		int attributes = md.numberOfAttributes;
		
		awarenessDecay = new double[segments];
		
		womTalkingProbability = new double[segments];
		womDiscussionHeatDecay = new double[segments];
		womPerceptionDecay = new double[segments];
		
		usagePerceptionDecay = new double[segments];
		usageDiscussionHeatDecay = new double[segments];
		
		onlinePostingProbabilities= new double[segments];
		onlineReadingProbabilities= new double[segments];
		
		onlinePerceptionDecay= new double[segments];
		onlineDiscussionHeatDecay= new double[segments];
		
		int touchpoints = md.numberOfTouchPoints;
		touchPointsPerceptionDecay= new double[touchpoints][segments];
		touchPointsDiscussionHeatDecay= new double[touchpoints][segments];
		
		//Iterate through segments and touch points.
		translateBySegmentAndTouchPoint(
				md, stepsByWeek, segments, touchpoints);
		
		//Creativities
		
		creativityByStep = new byte[touchpoints][][];
		final int weeks = md.numberOfWeeks;
		final int totalSteps = weeks * stepsByWeek;
		
		//Iterate through touch points, brands and steps.
		translateMediaAndAvailabilityByStep(
				md, stepsByWeek, brands, touchpoints, weeks, totalSteps);
		
		brandAttributes = new double [brands][attributes][totalSteps];
		
		//Iterate through brands, attributes and steps.
		translateBrandAttributes(
				md, stepsByWeek, brands, attributes, weeks, totalSteps);
		
		//Sales
		double stepIncrement = Model.DAYS_OF_WEEK/stepsByWeek;
		buyingDecisionCycle=(int)(md.buyingDecisionCycle/stepIncrement);
	}
	
	private void translateBySegmentAndTouchPoint(
			ModelDefinition md, 
			int stepsByWeek,
			int segments, 
			int touchpoints
		) {
		for (int i=0; i<segments; i++) {
			//Awareness decay
			awarenessDecay[i]=md.awarenessDecay[i]/stepsByWeek;
			//WoM
			womTalkingProbability[i]=md.womTalkingProbability[i]/stepsByWeek;
			womDiscussionHeatDecay[i]=md.womDiscussionHeatDecay[i]/stepsByWeek;	
			womPerceptionDecay[i]=md.womPerceptionDecay[i]/stepsByWeek;

			//ProductUsage
			//Usage frequencies are adjusted by k at product use scheduling.
			usagePerceptionDecay[i]=md.usagePerceptionDecay[i]/stepsByWeek;
			usageDiscussionHeatDecay[i]=md.usageDiscussionHeatDecay[i]/stepsByWeek;
			
			//OnlinePosting&Reading
			onlinePostingProbabilities[i]=md.onlinePostingProbabilities[i]/stepsByWeek;
			onlineReadingProbabilities[i]=md.onlineReadingProbabilities[i]/stepsByWeek;
			
			onlinePerceptionDecay[i]=md.onlinePerceptionDecay[i]/stepsByWeek;
			onlineDiscussionHeatDecay[i]=md.onlineDiscussionHeatDecay[i]/stepsByWeek;
			
			//PaidTouchPoints
			for (int j=0; j<touchPointsPerceptionDecay.length; j++) {
				touchPointsPerceptionDecay[j][i]=md.touchPointsPerceptionDecay[j][i]/stepsByWeek;
				touchPointsDiscussionHeatDecay[j][i]=md.touchPointsDiscusionHeatDecay[j][i]/stepsByWeek;
			}
		}
	}
	
	private void translateMediaAndAvailabilityByStep(
			ModelDefinition md, 
			int stepsByWeek,
			int brands, 
			int touchpoints,
			int weeks,
			int totalSteps
		) {
		for (int touchpoint=0; touchpoint<touchpoints; touchpoint++) {
			
			creativityByStep[touchpoint] = new byte [brands][];
			availability = new double [brands][];
			
			for (int brand=0; brand<brands; brand++) {
				creativityByStep[touchpoint][brand] = new byte [totalSteps];
				availability[brand] = new double [totalSteps];
				
				for (int step=0; step<weeks; step++) {
					
					int firstStep = step * stepsByWeek;
					int lastStep = (step+1) * stepsByWeek;
					
					Arrays.fill(
							creativityByStep[touchpoint][brand], 
							firstStep, 
							lastStep, 
							md.creativityByStep[touchpoint][brand][step]
						);
					
					Arrays.fill(
							availability[brand], 
							firstStep, 
							lastStep, 
							md.availabilityByBrandAndStep[brand][step]
						);
				}
			}
		}
	}
	
	private void translateBrandAttributes(ModelDefinition md, 
			int stepsByWeek,
			int brands, 
			int attributes,
			int weeks,
			int totalSteps
		) {
		for (int brand=0; brand<brands; brand++) {
			for (int att=0; att<attributes; att++) {
				for (int step=0; step<weeks; step++) {
					int firstStep = step * stepsByWeek;
					int lastStep = (step+1) * stepsByWeek;
					
					Arrays.fill(
							brandAttributes[brand][att], 
							firstStep, 
							lastStep, 
							md.brandAttributes[brand][att][step]
						);
				}
			}
		}
	}
}
