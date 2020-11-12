package model;

import model.customer.Agent;
import model.decisionmaking.DecisionMaking;
import model.sales.SalesScheduler;
import model.socialnetwork.BasicScaleFreeSocialNetwork;
import model.socialnetwork.GeometricRandomSocialNetwork;
import model.socialnetwork.SocialNetwork;
import model.touchpoints.MarketingPlan;
import model.touchpoints.TouchPointOwned;
import model.touchpoints.TouchPointOwned.InvestmentType;
import model.touchpoints.TouchPointOwnedRegistry;
import model.touchpoints.earned.PostReadOnline;
import model.touchpoints.earned.ProductUsage;
import util.random.Randomizer;
import util.statistics.Statistics;

/**
 * ModelBuilder takes both ModelDefinition and ModelStepTranslator objects 
 * and builds a Model instance with them. This functionality was previously
 * carried out by ModelDefinition, but for clarity reasons it was decoupled
 * to its own class.
 *  
 * @author imoya
 *
 */
public class ModelBuilder {
	
	private ModelDefinition md; 
	private ModelStepTranslator translator;

	/**
	 * ModelBuilder instances take ModelDefinition and ModelStepTranslator
	 * instances and use them to build a Model instance.
	 * 
	 * @param md - a ModelDefinition instance.
	 * @param translator - a ModelStepTranslator instance.
	 */
	public ModelBuilder(
			ModelDefinition md, 
			ModelStepTranslator translator
		) {
		this.md = md;
		this.translator = translator;
	}

	/**
	 * Builds a Model instance using ModelDefinition and ModelStepTranslator
	 * parameters without printing times.
	 * 
	 * @return a Model instance
	 */
	public Model build(ModelBean bean, long seed) {
		Model m = new Model(
				md.stepsForWeek,
				md.numberOfAgents,
				md.numberOfSegments, 
				md.numberOfBrands, 
				md.numberOfAttributes,
				md.numberOfWeeks, 
				md.numberOfTouchPoints,
				seed,
				md.awarenessFilter,
				md.womSentimentPositive,	// TODO [KT] Take it from GUI or file!!!
				md.womSentimentNegative,		// TODO [KT] Take it from GUI or file!!!
				md.getAgentsRatio()
			);
		
		ClientSegments segments = createSegments();
		m.setSegments(segments);

		m.setBrands(bean.brands);
		m.setUsage(bean.usage);
		
		PostReadOnline postReadOnline = new PostReadOnline(
				md.numberOfBrands, translator.onlinePostingProbabilities, 
				translator.onlineReadingProbabilities, md.onlineAwarenessImpact, 
				md.onlinePerceptionSpeed, md.onlineDiscussionHeatImpact
		);
		m.setPostReadOnline(postReadOnline);
		
		//Randomized components
		
		Randomizer random = m.random;
		
		TouchPointOwnedRegistry registry = new TouchPointOwnedRegistry(
				md.numberOfTouchPoints, 
				md.numberOfBrands,
				random
			);
		m.setTouchPointRegistry(registry);
		registry.setDebugMode(md.debug);

		DecisionMaking dm = new DecisionMaking(
			random, md.drivers, md.involved, 
			1-md.involved, md.emotional, 1-md.emotional, 
			md.numberOfAttributes, md.numberOfBrands
		);
		m.setDecisionMaking(dm);
		
//---------------------------------------------------------------------------//

		SocialNetwork socialNetwork = createSocialNetwork(
				segments.getSegmentSizesInt(), random);
		m.setSocialNetwork(socialNetwork);
		
//---------------------------------------------------------------------------//
		
		Agent[] agents = createAgents(segments, socialNetwork, registry, random);
		m.setAgents(agents);
		
//---------------------------------------------------------------------------//

		m.setSalesScheduler(configSalesScheduler(agents));
		
//---------------------------------------------------------------------------//
		Statistics stats = m.getStatistics();
		scheduleTouchPoints(registry,agents, segments, stats);

//---------------------------------------------------------------------------//
		return m;
	}
	
	public ModelBean createBean() {
		Brand[] brands = createBrands();
		ProductUsage usage = createProductUsage(brands);
		
		return new ModelBean(brands, usage);
	}

	/**
	 * Builds a new Model instance only containing the agent social network.
	 * @return a new Model instance only containing the agent social network.
	 */
	public Model buildNetwork(long seed) {
		Model m = new Model(
				md.stepsForWeek,
				md.numberOfAgents,
				md.numberOfSegments, 
				md.numberOfBrands, 
				md.numberOfAttributes,
				md.numberOfWeeks, 
				md.numberOfTouchPoints,
				seed,
				md.awarenessFilter,
				md.womSentimentPositive,	// TODO [KT] Take it from GUI or file!!!
				md.womSentimentNegative,		// TODO [KT] Take it from GUI or file!!!
				md.getAgentsRatio()
			);
		
		Randomizer random = m.random;
		
		ClientSegments segments = createSegments();
		m.setSegments(segments);
		
		SocialNetwork socialNetwork = createSocialNetwork(
				segments.getSegmentSizesInt(), random);	
		m.setSocialNetwork(socialNetwork);
		
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
		
		segments.setNrSegments(md.numberOfSegments);
		segments.setSegmentSizes(md.segmentSizes);
		segments.generateSegmentSizesInteger(md.numberOfAgents);
		segments.setSegmentInitialPerceptions(md.initialPerceptions);
		segments.setSegmentInitialAwarenesses(md.initialAwareness);
		segments.setSegmentAwarenessDecays(translator.awarenessDecay);
		segments.setDrivers(md.drivers);
		
		//WOM
		segments.setSegmentAwarenessImpact(md.womAwarenessImpact);
		segments.setSegmentDiscussionHeatImpacts(md.womDiscussionHeatImpact);
		segments.setSegmentPerceptionSpeeds(md.womPerceptionSpeed);
		segments.setSegmentTalkingProbabilities(translator.womTalkingProbability);
		
		segments.setSegmentsConnectivity(md.womSegmentConnectivity);
		segments.setSegmentInfluences(md.womSegmentInfluences);
		
		return segments;
	}
		
	/**
	 * Creates a Brand beans array instance using ModelDefinition values.
	 * 
	 * @return a Brand beans array instance
	 */
	private Brand[] createBrands() {
		Brand[] brandsDefined = new Brand[md.numberOfBrands];
		
		for (int i=0; i<brandsDefined.length; i++) {
			brandsDefined[i] = new Brand(i, translator.brandAttributes[i]);
		}
		
		return brandsDefined;
	}
	
	/**
	 * Creates the ProductUsage module bean using ModelDefinition and 
	 * ModelStepTranslator values, along with the given Brands array
	 * instance.
	 * 
	 * @param brands - current Brand beans array instance
	 * @return a ProductUsage module bean instance
	 */
	private ProductUsage createProductUsage(Brand[] brands) {
		ProductUsage usage = new ProductUsage(
				md.usageFrequencies,
				md.usagePerceptionSpeed,
				translator.usagePerceptionDecay,
				md.usageAwarenessImpact,
				md.usageDiscussionHeatImpact,
				brands,
				md.intangibleAttributes
				);
		return usage;
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
		
		SocialNetwork socialNetwork;
		switch(md.typeOfNetwork) {
			case SCALE_FREE_NETWORK:
				socialNetwork= new BasicScaleFreeSocialNetwork();
				break;
				
			case RANDOM_NETWORK_SEGMENTS:
				socialNetwork= new GeometricRandomSocialNetwork();
				break;
			default:
				throw new IllegalArgumentException("Unknown network type");
		}
		
		socialNetwork.generateNetwork(
			md.numberOfAgents, 
			SocialNetwork.DEFAULT_K_DEGREE_MAX, 
			numberOfNodes, 
			md.womSegmentConnectivity, 
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
	private Agent[] createAgents(
			ClientSegments segments, 
			SocialNetwork socialNetwork, 
			TouchPointOwnedRegistry registry,
			Randomizer random) {
		
		Agent[] agentsBag = new Agent[md.numberOfAgents];
		
		for (int i = 0; i < md.numberOfAgents; i++) {
			int segmentId = socialNetwork.getNodeSegmentAt(i);
			int[] neighbours = socialNetwork.getNodeNeighboursAt(i);
			
			agentsBag[i] = new Agent(
				segments.getSegmentTalkingProbability(segmentId),
				segments.getSegmentAwarenessDecay(segmentId),
				segments.getSegmentAwarenessImpact(segmentId),
				segments.getSegmentDiscussionHeatImpact(segmentId),
				segments.getSegmentPerceptionSpeed(segmentId),
				ProductUsage.generateInitialItems(
					md.brandInitialPenetration, random
				),
				i, 
				segmentId,
				segments.generateInitialPerceptions(
					segmentId,
					md.initialPerceptionsStdDeviation,
					random
				), 
				segments.generateInitialAwareness(md.numberOfBrands, segmentId, random), 
				neighbours,
				md.numberOfBrands,
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
			Agent[] agents) {
		
		double[] seasonality;		
		int numberOfSteps = md.getNumberOfSteps();
		
		//Adjust provided seasonality to current number of steps.
		if(md.seasonality.length!= numberOfSteps) {
			seasonality = new double[numberOfSteps];
			double proportion = (double) numberOfSteps / md.seasonality.length;
			for (int i=0; i<md.seasonality.length; i++) {
				for (int j=0; j<proportion; j++) {
					seasonality[(int)(i*proportion)+j]=md.seasonality[i]/proportion;
				}
			}
		} else {
			seasonality=md.seasonality;
		}
		
		SalesScheduler ss = new SalesScheduler(
			seasonality,
			translator.availability,
			md.salesCheckpoint,
			md.marketPercentBySegment,
			translator.buyingDecisionCycle, 
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
			Agent[] agents, 
			ClientSegments segments,
			Statistics stats
		) {
		
		//Retrieve tochpoint investment.
		InvestmentType[] touchPointsInvestment = md.touchPointsInvestment;
		if(touchPointsInvestment==null) {
			touchPointsInvestment 
				= TouchPointOwned.getDefaultInvestmentType(md.numberOfTouchPoints);
		}
		
		//Create touchpoints and its marketing plan
		for (int i = 0; i < md.numberOfTouchPoints; i++) {
			
			TouchPointOwned tp = new TouchPointOwned(
				i,
				md.numberOfBrands,
				md.touchPointsPerceptionSpeed[i],
				md.touchPointsAwarenessImpact[i],
				md.touchPointsDiscusionHeatImpact[i],
				md.touchPointsPerceptionPotential[i],
				md.touchPointsWeeklyReachMax[i],
				md.touchPointsAnnualReachMax[i],
				md.touchPointsAnnualReachSpeed[i],
				touchPointsInvestment[i]
			);
			
			for (int j=0; j<md.numberOfBrands; j++) {
				
				MarketingPlan mp = new MarketingPlan(
					i, 
					j, 
					md.touchPointsGRPMarketingPlan[i][j], 
					md.touchPointsEmphasis[i][j],  
					md.touchPointsQuality[i][j],
					translator.creativityByStep[i][j]
					);
				tp.addMarketingPlan(mp,j);
			}
			
			registry.addTouchPoint(i, tp);
		}
		
		//Schedule touch points
		double [][][] reach = registry.scheduleTouchPoints(
				md.segmentSizes, 
				agents, 
				segments, 
				md.numberOfWeeks,
				md.stepsForWeek,
				md.numberOfAgents/(double)md.getPopulationSize()
		);
		
		//Store reach values.
		stats.setReachByTouchpointByBrandBySegment(reach);
		
		registry.storeDecays(
				translator.womPerceptionDecay, 
				translator.usagePerceptionDecay, 
				translator.onlinePerceptionDecay, 
				translator.touchPointsPerceptionDecay
			);
		
		registry.storeDiscussionHeatDecay(
				translator.womDiscussionHeatDecay, 
				translator.usageDiscussionHeatDecay, 
				translator.onlineDiscussionHeatDecay, 
				translator.touchPointsDiscussionHeatDecay
			);
	}
}
