package model.simple;

import java.util.Arrays;

import model.ClientSegments;
import model.ModelDefinition;
import model.customer.SimpleAgent;
import model.decisionmaking.DecisionMaking;
import model.sales.SalesScheduler;
import model.socialnetwork.BasicScaleFreeSocialNetwork;
import model.socialnetwork.SocialNetwork;
import model.touchpoints.MarketingPlan;
import model.touchpoints.TouchPointOwned;
import model.touchpoints.TouchPointOwned.InvestmentType;
import model.touchpoints.TouchPointOwnedRegistry;
import util.random.Randomizer;
import util.statistics.Statistics;

public class SimpleModelBuilder {
	
	private ModelDefinition md;
	
	/**
	 * ModelBuilder instances take ModelDefinition and ModelStepTranslator
	 * instances and use them to build a Model instance.
	 * 
	 * @param md - a ModelDefinition instance.
	 * @param translator - a ModelStepTranslator instance.
	 */
	public SimpleModelBuilder(
			ModelDefinition md
		) {
		this.md = md;
	}
	
	/**
	 * Builds a Model instance using ModelDefinition and ModelStepTranslator
	 * parameters without printing times.
	 * 
	 * @return a Model instance
	 */
	public SimpleModel buildSimple(long seed) {
		SimpleModel m = new SimpleModel(
				md.getStepsForWeek(),
				md.getNumberOfAgents(),
				md.getNumberOfSegments(), 
				md.getNumberOfBrands(), 
				md.getNumberOfAttributes(),
				md.getNumberOfWeeks(), 
				md.getNumberOfTouchPoints(),
				seed,
				md.isAwarenessFilter(),
				md.getWomSentimentPositive(),
				md.getWomSentimentNegative(),
				md.getAgentsRatio()
			);
		
		ClientSegments segments = createSegments();
		m.setSegments(segments);
		
		//Randomized components
		
		Randomizer random = m.random;
		
		TouchPointOwnedRegistry registry = new TouchPointOwnedRegistry(
				md.getNumberOfTouchPoints(), 
				md.getNumberOfBrands(),
				random
			);
		m.setTouchPointRegistry(registry);

		DecisionMaking dm = new DecisionMaking(
			random, md.getDrivers(), md.getInvolved(), 
			1-md.getInvolved(), md.getEmotional(), 1-md.getEmotional(), 
			md.getNumberOfAttributes(), md.getNumberOfBrands()
		);
		m.setDecisionMaking(dm);
		
//---------------------------------------------------------------------------//

		SocialNetwork socialNetwork = createSocialNetwork(
				segments.getSegmentSizesInt(), random);
		m.setSocialNetwork(socialNetwork);
		
//---------------------------------------------------------------------------//
		
		SimpleAgent[] agents = createAgents(segments, socialNetwork, registry, random);
		m.setAgents(agents);
		
//---------------------------------------------------------------------------//

		m.setSalesScheduler(configSalesScheduler(agents));
		
//---------------------------------------------------------------------------//
		Statistics stats = m.getStatistics();
		scheduleTouchPoints(registry,agents, segments, stats);

//---------------------------------------------------------------------------//
		return m;
	}
	
	/**
	 * Creates and sets up a ClientSegments object using ModelDefinition and
	 * ModelStepTranslator values.
	 * 
	 * @return a ClientSegments instance
	 */
	private ClientSegments createSegments() {
		ClientSegments segments = new ClientSegments();
		
		segments.setNrSegments(md.getNumberOfSegments());
		segments.setSegmentSizes(md.getSegmentSizes());
		segments.generateSegmentSizesInteger(md.getNumberOfAgents());
		segments.setSegmentInitialPerceptions(md.getInitialPerceptions());
		segments.setSegmentInitialAwarenesses(md.getInitialAwareness());
		segments.setSegmentAwarenessDecays(md.getAwarenessDecay());
		segments.setDrivers(md.getDrivers());
		
		//WOM
		segments.setSegmentAwarenessImpact(md.getWomAwarenessImpact());
		segments.setSegmentDiscussionHeatImpacts(md.getWomDiscussionHeatImpact());
		segments.setSegmentPerceptionSpeeds(md.getWomPerceptionSpeed());
		segments.setSegmentTalkingProbabilities(md.getWomTalkingProbability());
		
		segments.setSegmentsConnectivity(md.getWomSegmentConnectivity());
		segments.setSegmentInfluences(md.getWomSegmentInfluences());
		
		return segments;
	}
	
	/**
	 * Creates a SocialNetwork instance using ModelDefinition values, number
	 * of nodes by segment and current randomizer. 
	 * 
	 * @param numberOfNodes - number of nodes by segment
	 * @param random - current randomizer
	 * @return a SocialNetwork instance
	 */
	private SocialNetwork createSocialNetwork(
			int[] numberOfNodes, Randomizer random) {
		
		SocialNetwork socialNetwork= new BasicScaleFreeSocialNetwork();
		
		socialNetwork.generateNetwork(
			md.getNumberOfAgents(), 
			SocialNetwork.DEFAULT_K_DEGREE_MAX, 
			numberOfNodes, 
			md.getWomSegmentConnectivity(), 
			random
		);
		
		return socialNetwork;
	}
	
	/**
	 * Creates ClientAgent array instance using ModelDefinition, 
	 * ClientSegments and SocialNetwork values, along with the
	 * current randomizer.
	 * 
	 * @param segments - a ClientSegments object
	 * @param socialNetwork - a SocialNetwork object
	 * @param random - current randomizer instance
	 * @return a ClientAgent array
	 */
	private SimpleAgent[] createAgents(
			ClientSegments segments, 
			SocialNetwork socialNetwork, 
			TouchPointOwnedRegistry registry,
			Randomizer random) {
		
		SimpleAgent[] agentsBag = new SimpleAgent[md.getNumberOfAgents()];
		
		for (int i = 0; i < md.getNumberOfAgents(); i++) {
			int segmentId = socialNetwork.getNodeSegmentAt(i);
			int[] neighbours = socialNetwork.getNodeNeighboursAt(i);
			
			agentsBag[i] = new SimpleAgent(
				segments.getSegmentTalkingProbability(segmentId),
				segments.getSegmentAwarenessDecay(segmentId),
				segments.getSegmentAwarenessImpact(segmentId),
				segments.getSegmentDiscussionHeatImpact(segmentId),
				segments.getSegmentPerceptionSpeed(segmentId),
				new boolean[md.getNumberOfBrands()],
				i, 
				segmentId,
				segments.generateInitialPerceptions(
					segmentId,
					md.getInitialPerceptionsStdDeviation(),
					random
				), 
				segments.generateInitialAwareness(md.getNumberOfBrands(), 
						segmentId, random), 
				neighbours,
				md.getNumberOfBrands(),
				registry.getNumberOfTouchpoints()
			);
			segments.addAgentToSegment(segmentId, i);
		}
		return agentsBag;
	}
	
	/**
	 * Creates a SalesScheduler instance using a ClientAgent array, 
	 * ModelDefinition and ModelStepTranslator values. 
	 * 
	 * @param agents - a ClientAgent array instance
	 * @return a SalesScheduler instance
	 */
	private SalesScheduler configSalesScheduler (
			SimpleAgent[] agents) {
		
		double[] seasonality;		
		int numberOfSteps = md.getNumberOfSteps();
		
		//Adjust provided seasonality to current number of steps.
		if(md.getSeasonality().length!= numberOfSteps) {
			seasonality = new double[numberOfSteps];
			double proportion = (double) numberOfSteps / md.getSeasonality().length;
			for (int i=0; i<md.getSeasonality().length; i++) {
				for (int j=0; j<proportion; j++) {
					seasonality[(int)(i*proportion)+j]=md.getSeasonality()[i]/proportion;
				}
			}
		} else {
			seasonality=md.getSeasonality();
		}
		
		//We ignore the availability value and set it to 100%
		int brands = md.getNumberOfBrands();
		double [][] availability = new double [brands][numberOfSteps];
		for (int b = 0; b<brands; b++) {
			Arrays.fill(availability[b], 1.0);
		}
		
		//Consecuence of the chapuza from SimulationConfig (line 166).
		double adjustedCycle = md.getBuyingDecisionCycle() / 7.0; 
		
		SalesScheduler ss = new SalesScheduler(
			seasonality,
			availability,
			md.getBuyingDecisionCycle(),
			md.getMarketPercentBySegment(),
			(int)Math.round(adjustedCycle), 
			numberOfSteps,
			md.getAgentsRatio(), 
			agents
		);
		
		return ss;
	}

	/**
	 * Creates a new instance of touch point registry and creates every 
	 * touch point with its associated marketing plans. When every touch point
	 * has been instantiated, all of them are scheduled.
	 * 
	 * This method uses ModelDefinition and ModelStepTranslator values along
	 * with other parameters supplied.
	 * 
	 * @param registry centralized touch point registry.
	 * @param agents a ClientAgent array instance.
	 * @param segments a ClientSegments object.
	 * @param stats model statistics object.
	 */
	private void scheduleTouchPoints(
			TouchPointOwnedRegistry registry,
			SimpleAgent[] agents, 
			ClientSegments segments,
			Statistics stats
		) {
		
		//Retrieve tochpoint investment.
		InvestmentType[] touchPointsInvestment = md.getTouchPointsInvestment();
		if(touchPointsInvestment==null) {
			touchPointsInvestment 
				= TouchPointOwned.getDefaultInvestmentType(md.getNumberOfTouchPoints());
		}
		
		//Create touchpoints and its marketing plan
		for (int i = 0; i < md.getNumberOfTouchPoints(); i++) {
			
			SimpleTouchPointOwned tp = new SimpleTouchPointOwned(
				i,
				md.getNumberOfBrands(),
				md.getTouchPointsPerceptionSpeed()[i],
				md.getTouchPointsAwarenessImpact()[i],
				md.getTouchPointsDiscusionHeatImpact()[i],
				md.getTouchPointsPerceptionPotential()[i],
				md.getTouchPointsWeeklyReachMax()[i],
				md.getTouchPointsAnnualReachMax()[i],
				md.getTouchPointsAnnualReachSpeed()[i],
				touchPointsInvestment[i]
			);
			
			for (int j=0; j<md.getNumberOfBrands(); j++) {
				
				MarketingPlan mp = new MarketingPlan(
					i, 
					j, 
					md.getGRP()[i][j], 
					null,//md.getTouchPointsEmphasis()[i][j],  
					null,//md.getTouchPointsQuality()[i][j],
					null//md.getCreativityByStep()[i][j]
					);
				tp.addMarketingPlan(mp,j);
			}
			
			registry.addTouchPoint(i, tp);
		}
		
		//Schedule touch points
		double [][][] reach = registry.scheduleTouchPoints(
				md.getSegmentSizes(), 
				agents, 
				segments, 
				md.getNumberOfWeeks(),
				md.getStepsForWeek(),
				md.getNumberOfAgents()/(double)md.getPopulationSize()
		);
		
		//Store reach values.
		stats.setReachByTouchpointByBrandBySegment(reach);
		
		registry.storeDecays(
				md.getWomPerceptionDecay(), 
				md.getUsagePerceptionDecay(), 
				md.getOnlinePerceptionDecay(), 
				md.getTouchPointsPerceptionDecay()
			);
	}
}
