package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.math.NumberUtils;

import model.decisionmaking.DecisionMaking;
import model.socialnetwork.SocialNetwork.NetworkType;
import model.touchpoints.TouchPointOwned.InvestmentType;
import model.touchpoints.earned.AbstractTouchPoint;
import util.functions.ArrayFunctions;
import util.functions.MatrixFunctions;
import util.io.ConfigFileReader;

/**
 * Contains the parameters defining the model and that are needed to create 
 * model instances. It also defines an API to load those parameters from file 
 * or view module. 
 * 
 * @author imoya
 *
 */
public class ModelDefinition {
	
	// ########################################################################
	// Attributes ::	Most attributes have package visibility for easing 
	// 					interaction with other Model "something" classes.
	// ########################################################################	
	
	boolean debug = false;
	
	/**
	 * Model name. Only used GUI displaying.
	 */
	private String name = "NONAME";
	/**
	 * Model description. Only used GUI displaying.
	 */
	private String description = "";

	// Market parameters & Simulation Parameters
	
	/**
	 * Number of agents for modeled market.
	 */
	int numberOfAgents = Model.DEFAULT_NUMBER_OF_AGENTS;
	
	/**
	 * Number of people belonging to modeled market.
	 */
	private int populationSize = Model.DEFAULT_POPULATION_SIZE;
	
	/**
	 * Number of brands for modeled market.
	 */
	int numberOfBrands = Model.DEFAULT_NUMBER_OF_BRANDS;
	
	/**
	 *  Number of attributes for modeled market.
	 */
	int numberOfAttributes = Model.DEFAULT_NUMBER_OF_ATTRIBUTES;
	
	/**
	 * Number of segments for modeled market.
	 */
	int numberOfSegments = Model.DEFAULT_NUMBER_OF_SEGMENTS;
	
	/**
	 * Number of touch points for modeled market.
	 */
	int	numberOfTouchPoints = Model.DEFAULT_NUMBER_OF_TOUCHPOINTS;
	
	/**
	 * Number of weeks for modeled market.
	 */
	int numberOfWeeks = Model.DEFAULT_NUMBER_OF_WEEKS;
	
	/**
	 * Number of steps by week for modeled market.
	 */
	int stepsForWeek = Model.STEPS_FOR_WEEK_WEEKLY;
	
	// Diffusion model
	
	/**
	 * Enables awareness filter during perception diffusion. 
	 */
	boolean awarenessFilter = Model.NO_AWARENESS_FILTER;
	
	// Seed
	long calibrationSeed = Model.DEFAULT_SEED;
	// Decision Making
	
	/**
	 * Emotional value [0,1]
	 */
	double emotional = DecisionMaking.DEFAULT_EMOTIONAL_VALUE;
	
	/**
	 * Involved value [0,1]
	 */
	double involved = DecisionMaking.DEFAULT_INVOLVED_VALUE;
	
	// Segments values
	
	/**
	 * Segment proportion values [0,1]. This array should be normalized at 1.0.
	 */
	double [] segmentSizes = null;
	
	/**
	 * Initial perception value [0, 10] by segment, brand and attribute.
	 */
	double [][][] initialPerceptions = null;
	
	/**
	 * Initial awareness value [0,1] by brand and segment.
	 */
	double [][] initialAwareness = null;
	
	/**
	 * Awareness decay value [0,1] by segment.
	 */
	double [] awarenessDecay = null;
	
	/**
	 * Weights value [0,1] by segment and attribute. This array should be 
	 * normalized at 1.0.
	 */
	double [][] drivers = null;
	
	/**
	 * Initial perceptions will be generated using a normal distribution. That 
	 * normal distribution will be adjusted by this standard deviation value.
	 */
	double 	initialPerceptionsStdDeviation = 0;
	
	// WOM
	
	/**
	 * Word of Mouth awareness impact by segment.
	 */
	double [] womAwarenessImpact = null;
	
	/**
	 * Word of Mouth discussion heat impact by segment.
	 */
	double [] womDiscussionHeatImpact = null;
	
	/**
	 * Word of Mouth discussion heat decay by segment.
	 */
	double [] womDiscussionHeatDecay = null;
	
	/**
	 * Word of Mouth perception speed by segment.
	 */
	double [] womPerceptionSpeed = null;
	
	/**
	 * Word of Mouth perception decay by segment.
	 */
	double [] womPerceptionDecay = null;
	
	/**
	 * Word of Mouth talking probability by segment.
	 */
	double [] womTalkingProbability = null;
	
	/**
	 * Word of Mouth segments influence by segment.
	 */
	double [][] womSegmentInfluences = null;
	
	/**
	 * Social Network connectivity value by segment.
	 */
	double [] womSegmentConnectivity = null;

	/**
	 * Word of Mouth sentiment upper bound value. 
	 */
	double womSentimentPositive = Model.DEFAULT_WOM_SENTIMENT_POSITIVE;
	
	/**
	 * Word of Mouth sentiment lower bound value. 
	 */
	double womSentimentNegative = Model.DEFAULT_WOM_SENTIMENT_NEGATIVE;
	
	/**
	 * Structure followed by social network (Random, Scale-free, etc).
	 */
	NetworkType typeOfNetwork = Model.DEFAULT_NETWORK_TYPE;	
	
	// Touch Points Owned
	
	/**
	 * Touch point owned weekly reach maximum value [0,1] by touch point 
	 * and segment.
	 */
	double [][] 	touchPointsWeeklyReachMax = null;
	
	/**
	 * Touch point owned annual reach maximum value [0,1] by touch point 
	 * and segment.
	 */
	double [][]		touchPointsAnnualReachMax = null;
	
	/**
	 * Touch point owned annual reach speed value [0,1] by touch point 
	 * and segment.
	 */
	double [][] 	touchPointsAnnualReachSpeed = null;
	
	/**
	 * Touch point owned perception potential value by touch point 
	 * and segment.
	 */
	double [][] 	touchPointsPerceptionPotential = null;
	
	/**
	 * Touch point owned perception speed value [0,1] by touch point 
	 * and segment.
	 */
	double [][] 	touchPointsPerceptionSpeed = null;

	/**
	 * Touch point owned awareness impact value [0,1] by touch point 
	 * and segment.
	 */
	double [][]		touchPointsAwarenessImpact = null;
	
	/**
	 * Touch point owned discussion heat impact value [0,1] by touch point 
	 * and segment.
	 */
	double [][]		touchPointsDiscusionHeatImpact = null;

	/**
	 * Touch point owned GRP values by touch point, brand, segment and week.
	 */
	double [][][][] touchPointsGRPMarketingPlan = null;
	
	/**
	 * Touch point perception decay by touch point and segment. This array 
	 * also contains values for earned touch points, that have reserved 
	 * positions.
	 */
	double [][]		touchPointsPerceptionDecay = null;
	
	/**
	 * Touch point discussion heat decay by touch point and segment. This 
	 * array also contains values for earned touch points, that have reserved 
	 * positions.
	 */
	double [][]		touchPointsDiscusionHeatDecay = null;
	
	/**
	 * Metric followed by each touch point.
	 */
	InvestmentType[] touchPointsInvestment = null;
	
	/**
	 * Touch point owned emphasis values by touch point, brand, creativity and 
	 * attribute.
	 */
	double [][][][] touchPointsEmphasis = null;
	/**
	 * Touch point owned quality values by touch point, brand, creativity and 
	 * attribute.
	 */
	double [][][][] touchPointsQuality = null;
	
	/**
	 * Touch point owned creativity id values by touch point, brand and week.
	 */
	byte [][][]		creativityByStep = null;
	
	/**
	 * Touch point owned values joined in the same 3D Matrix as follows:
	 * 		* Weekly Reach Maximum
	 *		* Annual Reach Maximum
	 *		* Annual Reach Speed
	 *		* Perception Potential
	 *		* Perception Speed
	 *		* Perception Decay
	 *		* Awareness Impact
	 *		* Discussion Heat Impact
	 *		* Discussion Heat Decay
	 */
	private double [][][] 	touchPointValues = null;
	
	// Sales
	
	/**
	 * Number of real sales at modeled market by week.
	 */
	double [] 	seasonality = null;
	
	/**
	 * Share value [0,1] by segment.
	 */
	double [] 	marketPercentBySegment = null;
	
	/**
	 * Availability value [0,1] by brand and week.
	 */
	double [][] availabilityByBrandAndStep = null;
	
	/**
	 * Market decision cycle value in number of steps.
	 */
	int buyingDecisionCycle = Model.DEFAULT_DECISION_CYCLE;
	
	/**
	 * Number of steps separating each check point.
	 */
	int salesCheckpoint = Model.DEFAULT_DECISION_CYCLE;
	
	/**
	 * Decision cycle as a string representation.
	 * 
	 * For example, 3#Weeks
	 */
	private String	decisionCycleString = null;

	// Product Usage
	
	/**
	 * Usage frequency in number of steps for each segment.
	 */
	int [] 	usageFrequencies = null;
	
	/**
	 * Usage frequencies string representation.
	 * 
	 * For example, 15#Months;10#Days
	 */
	private String	usageFrequenciesString = null;
	
	/**
	 * Product usage perception speed by segment.
	 */
	double [] 	usagePerceptionSpeed = null;
	
	/**
	 * Product usage perception decay by segment.
	 */
	double [] 	usagePerceptionDecay = null;
	
	/**
	 * Product usage awareness impact by segment.
	 */
	double [] 	usageAwarenessImpact = null;
	
	/**
	 * Product usage discussion heat impact by segment.
	 */
	double [] 	usageDiscussionHeatImpact = null;
	
	/**
	 * Product usage discussion heat decay by segment.
	 */
	double [] 	usageDiscussionHeatDecay = null;
	
	/**
	 * Intangible attributes by attribute id. Intangible attributes 
	 * are those that are skipped by product usage.
	 */
	boolean [] 	intangibleAttributes = null;

	// Posting/Reading Online
	
	/**
	 * Posting online probability by segment.
	 */
	double [] onlinePostingProbabilities = null;
	
	/**
	 * Reading online probability by segment.
	 */
	double [] onlineReadingProbabilities = null;
	
	/**
	 * Posting online awareness impact by segment.
	 */
	double [] onlineAwarenessImpact = null;
	
	/**
	 * Posting online perception speed by segment.
	 */
	double [] onlinePerceptionSpeed = null;
	
	/**
	 * Posting online perception decay by segment.
	 */
	double [] onlinePerceptionDecay = null;
	
	/**
	 * Posting online discussion heat impact by segment.
	 */
	double [] onlineDiscussionHeatImpact = null;
	
	/**
	 * Posting online discussion heat decay by segment.
	 */
	double [] onlineDiscussionHeatDecay = null;	
	
	//Brands
	
	/**
	 * Initial penetration value [0,1] by brand.
	 */
	double [] brandInitialPenetration = null;
	
	/**
	 * Impartial attribute value [0,10] by brand, attribute and week.
	 */
	double [][][] brandAttributes = null;
	
	// Tags
	
	/**
	 * Name for each segment.
	 */
	private String [] segmentNames = {};
	/**
	 * Name for each brand.
	 */
	private String [] brandNames = {};
	
	/**
	 * Name for each attribute.
	 */
	private String [] attributeNames = {};
	
	/**
	 * Name for each touch point.
	 */
	private String [] touchPointNames = {};
	
	/**
	 * Name for media campaign by touch point, brand and creativity.
	 */
	private String [][][] creativityNames = {};
	
	private boolean simple;
	
	// ########################################################################
	// Methods
	// ########################################################################
	
	/**
	 * Crates a new ModelDefinition instance.
	 */
	public ModelDefinition() {
		
	}
	
	// ########################################################################
	// Methods
	// ########################################################################
	
	/**
	 * Import model values from given ZIO file object using a 
	 * {@code ConfigFileReader}. ZIO files are Java property files.
	 * 
	 * @param file ZIO file that is trying to be imported.
	 */
	public void loadValuesFromFile(File file) {
		ConfigFileReader config = new ConfigFileReader();
		config.readConfigFile(file);
		
		//Info
		loadInfoFromFile(config);

		//Market parameters
		loadMarketValuesFromFile(config);
		
		//Decision Making
		loadDecisionMakingFromFile(config);

		//Segments
		loadSegmentValuesFromFile(config);

		//WOM
		loadWOMValuesFromFile(config);

		//TouchPoints
		loadTouchPointValuesFromFile(config);

		//Sales
		loadSalesValuesFromFile(config);

		//Product Usage
		loadProductUsageValuesFromFile(config);
		
		//Posting/Reading Online
		loadPostReadOnlineValuesFromFile(config);

		//Brands
		loadBrandValuesFromFile(config);
		
		//Tags
		loadTagsFromFile(config);
	}
	
	/**
	 * Loads model information from given {@code ConfigFileReader} instance.
	 * @param config file reader managing load process.
	 */
	private void loadInfoFromFile(ConfigFileReader config) {
		name = config.getParameterString("name");
		name = (name != null)? name : Model.DEFAULT_NAME;
		description = config.getParameterString("description");
		description = (description != null)? 
			description : Model.DEFAULT_DESCRIPTION;
	}
	
	/**
	 * Loads model market values from given {@code ConfigFileReader} instance.
	 * @param config file reader managing load process.
	 */
	private void loadMarketValuesFromFile(ConfigFileReader config) {
		//Market parameters
		numberOfAgents = config.getParameterInteger("numberOfAgents");
		populationSize = config.getParameterInteger("populationSize");
		numberOfBrands = config.getParameterInteger("nBrands");
		numberOfAttributes=config.getParameterInteger("nAttributes");
		//Seed
//		seed = Long.parseLong(config.getParameterString("seed"));
		//Steps
		try {
			stepsForWeek = config.getParameterInteger("stepsForWeek");
		} catch (NumberFormatException e) {
			//If file does not contain the step parameter, default is taken.
			stepsForWeek = Model.STEPS_FOR_WEEK_DAILY;
		}
		
		try {
			numberOfWeeks=config.getParameterInteger("nWeeks");
		} catch (NumberFormatException e) {
			//If file does not contain the number of steps parameter, 
			//default is taken.
			numberOfWeeks = Model.DEFAULT_NUMBER_OF_WEEKS;
		}
		// Awareness filter
		try {
			awarenessFilter = config.getParameterBoolean("awarenessFilter");
		} catch (NumberFormatException e) {
			//If file does not contain the step parameter, default is taken.
			awarenessFilter = Model.NO_AWARENESS_FILTER;
		}
	}
	
	/**
	 * Loads model decision making values from given {@code ConfigFileReader} 
	 * instance.
	 * @param config file reader managing load process.
	 */
	private void loadDecisionMakingFromFile(ConfigFileReader config) {
		involved = config.getParameterDouble("involved");
		emotional = config.getParameterDouble("emotional");
	}
	
	/**
	 * Loads model segment values from given {@code ConfigFileReader} instance.
	 * @param config file reader managing load process.
	 */
	private void loadSegmentValuesFromFile(ConfigFileReader config) {
		numberOfSegments=config.getParameterInteger("numberOfSegments");
		segmentSizes=config.getParameterDoubleArray("segmentSizes");
		initialPerceptions = config
				.getParameterDoubleArrayThreeDim("initialPerceptions");
		
		initialAwareness = config.getParameterDoubleArrayTwoDim("initialAwareness");
		
		awarenessDecay=config.getParameterDoubleArray("awarenessDecay");
		drivers = config.getParameterDoubleArrayTwoDim("drivers");
		
		initialPerceptionsStdDeviation=Double.parseDouble(
				config.getParameterString("initialPerceptionsStandardDeviation"));
	}
	
	/**
	 * Loads model word of mouth values from given {@code ConfigFileReader} 
	 * instance.
	 * @param config file reader managing load process.
	 */
	private void loadWOMValuesFromFile(ConfigFileReader config) {
		womAwarenessImpact = config.getParameterDoubleArray("womAwarenessImpact");
		womDiscussionHeatImpact = config.getParameterDoubleArray("womDiscussionHeatImpact");
		womDiscussionHeatDecay = config.getParameterDoubleArray("womDiscussionHeatDecay");
		womPerceptionSpeed = config.getParameterDoubleArray("womPerceptionSpeed");
		womPerceptionDecay = config.getParameterDoubleArray("womPerceptionDecay");
		womTalkingProbability = config.getParameterDoubleArray("talking");
		womSegmentInfluences = config.getParameterDoubleArrayTwoDim("influence");
		womSegmentConnectivity = config.getParameterDoubleArray("connectivity");
		typeOfNetwork = NetworkType.valueOf(config.getParameterString("networkType"));
	}
	
	/**
	 * Loads model touch point owned values from given {@code ConfigFileReader} 
	 * instance.
	 * @param config file reader managing load process.
	 */
	private void loadTouchPointValuesFromFile(ConfigFileReader config) {
		numberOfTouchPoints = config.getParameterInteger("numTp");
		if(numberOfTouchPoints>0) {
			touchPointValues = config.getParameterDoubleArrayThreeDim("tpValues");
			
			loadTouchPointValues();
			
			touchPointsGRPMarketingPlan = new double [numberOfTouchPoints][][][];
			this.touchPointsEmphasis = new double [numberOfTouchPoints][numberOfBrands][][];
			this.touchPointsQuality = new double [numberOfTouchPoints][numberOfBrands][][];

			for (int i=0; i<numberOfTouchPoints;i++) {
				touchPointsGRPMarketingPlan[i]= config.getParameterDoubleArrayThreeDim("weeklyPlanT"+i);
			}
			double [][][] touchPointsEmphasis = config.getParameterDoubleArrayThreeDim("emphasis");
			double [][][] touchPointsQuality = config.getParameterDoubleArrayThreeDim("quality");
			
			try {
				creativityByStep = config.getParameterByteArrayThreeDim("creativityByStep");
			}catch (NullPointerException nullP) {
				creativityByStep = new byte[numberOfTouchPoints][numberOfBrands][numberOfWeeks];
				for (int i=0; i<numberOfTouchPoints; i++) {
					for (int j=0; j<numberOfBrands; j++) {
						Arrays.fill(creativityByStep[i][j], (byte)0);
					}
				}
			}
			
			for (int i=0; i<numberOfTouchPoints; i++) {
				for (int j=0; j<numberOfBrands; j++) {
					this.touchPointsEmphasis[i][j] = ArrayFunctions.chunkDoubleArray(
							touchPointsEmphasis[i][j],
							NumberUtils.max(creativityByStep[i][j])+1
						);
					this.touchPointsQuality[i][j] = ArrayFunctions.chunkDoubleArray(
							touchPointsQuality[i][j],
							NumberUtils.max(creativityByStep[i][j])+1
						);
				}
			}
		}		
	}
	
	/**
	 * Splits row touch point values matrix into separate arrays.
	 */
	public void loadTouchPointValues() {
		touchPointsWeeklyReachMax = new double [numberOfTouchPoints][];
		touchPointsAnnualReachMax = new double [numberOfTouchPoints][];
		touchPointsAnnualReachSpeed = new double [numberOfTouchPoints][];
		touchPointsPerceptionPotential = new double [numberOfTouchPoints][];
		touchPointsPerceptionSpeed = new double [numberOfTouchPoints][];
		touchPointsPerceptionDecay = new double [numberOfTouchPoints][];
		touchPointsAwarenessImpact = new double [numberOfTouchPoints][];
		touchPointsDiscusionHeatImpact = new double [numberOfTouchPoints][];
		touchPointsDiscusionHeatDecay = new double [numberOfTouchPoints][];
		
		for (int i=0; i<numberOfTouchPoints;i++) {			
			touchPointsWeeklyReachMax[i] = new double [numberOfSegments];
			for (int j=0; j<numberOfSegments; j++) {
				touchPointsWeeklyReachMax[i][j]=touchPointValues[i][j][0];
			}
			touchPointsAnnualReachMax[i] = new double [numberOfSegments];
			for (int j=0; j<numberOfSegments; j++) {
				touchPointsAnnualReachMax[i][j]=touchPointValues[i][j][1];
			}
			touchPointsAnnualReachSpeed[i] = new double [numberOfSegments];
			for (int j=0; j<numberOfSegments; j++) {
				touchPointsAnnualReachSpeed[i][j]=touchPointValues[i][j][2];
			}
			touchPointsPerceptionPotential[i] = new double [numberOfSegments];
			for (int j=0; j<numberOfSegments; j++) {
				touchPointsPerceptionPotential[i][j]=touchPointValues[i][j][3];
			}
			touchPointsPerceptionSpeed[i] = new double [numberOfSegments];
			for (int j=0; j<numberOfSegments; j++) {
				touchPointsPerceptionSpeed[i][j]=touchPointValues[i][j][4];
			}
			touchPointsPerceptionDecay[i] = new double [numberOfSegments];
			for (int j=0; j<numberOfSegments; j++) {
				touchPointsPerceptionDecay[i][j]=touchPointValues[i][j][5];
			}
			touchPointsAwarenessImpact[i] = new double [numberOfSegments];
			for (int j=0; j<numberOfSegments; j++) {
				touchPointsAwarenessImpact[i][j]=touchPointValues[i][j][6];
			}
			touchPointsDiscusionHeatImpact[i] = new double [numberOfSegments];
			for (int j=0; j<numberOfSegments; j++) {
				touchPointsDiscusionHeatImpact[i][j]=touchPointValues[i][j][7];
			}
			touchPointsDiscusionHeatDecay[i] = new double [numberOfSegments];
			for (int j=0; j<numberOfSegments; j++) {
				touchPointsDiscusionHeatDecay[i][j]=touchPointValues[i][j][8];
			}
		}
	}
	
	/**
	 * Joins touch point separate arrays into a single 3D matrix.
	 */
	private void storeTouchPointValues() {
		
		if(touchPointValues == null) {
			touchPointValues = new double [numberOfTouchPoints][numberOfSegments][9];
		}
		
		for (int i=0; i<numberOfTouchPoints;i++) {
			for (int j=0; j<numberOfSegments; j++) {
				touchPointValues[i][j][0]=touchPointsWeeklyReachMax[i][j];
				touchPointValues[i][j][1]=touchPointsAnnualReachMax[i][j];
				touchPointValues[i][j][2]=touchPointsAnnualReachSpeed[i][j];
				
				touchPointValues[i][j][3]=touchPointsPerceptionPotential[i][j];
				touchPointValues[i][j][4]=touchPointsPerceptionSpeed[i][j];
				touchPointValues[i][j][5]=touchPointsPerceptionDecay[i][j];
				
				touchPointValues[i][j][6]=touchPointsAwarenessImpact[i][j];
				touchPointValues[i][j][7]=touchPointsDiscusionHeatImpact[i][j];
				touchPointValues[i][j][8]=touchPointsDiscusionHeatDecay[i][j];
			}
		}
	}
	
	/**
	 * Loads model sales from given {@code ConfigFileReader} 
	 * instance.
	 * @param config file reader managing load process.
	 */
	private void loadSalesValuesFromFile(ConfigFileReader config) {
		seasonality=config.getParameterDoubleArray("seasonality");
		
		try {
			availabilityByBrandAndStep=
					config.getParameterDoubleArrayTwoDim("availability");
		} catch (NullPointerException e) {
			availabilityByBrandAndStep = new double [numberOfBrands][numberOfWeeks];			
			for (double [] availabilityByStep : availabilityByBrandAndStep) {
				Arrays.fill(availabilityByStep, 1.0);
			}
		}
		
		marketPercentBySegment = config.getParameterDoubleArray("segmentShare");
		
		decisionCycleString = config.getParameterString("decisionCycle");
		String [] frequency = decisionCycleString.split("#");
		double period = 1;
		if(frequency[1].equals("Weeks")) {
			period = 7;
		} else if (frequency[1].equals("Months")) {
			period = 30.417;
		} else if(!frequency[1].equals("Days")) {
			throw new IllegalArgumentException("Invalid value for decision Cycle");
		}
		buyingDecisionCycle = (int)(period*Double.parseDouble(frequency[0]));
	}
	
	/**
	 * Loads model product usage values from given {@code ConfigFileReader} 
	 * instance.
	 * @param config file reader managing load process.
	 */
	private void loadProductUsageValuesFromFile(ConfigFileReader config) {
		usageFrequenciesString=config.getParameterString("frecuencies");
		String[] frequencies = usageFrequenciesString.split(";");
		usageFrequencies = new int[numberOfSegments];
		for (int i=0; i<usageFrequencies.length; i++) {
			String [] frequencyOfUse = frequencies[i].split("#");
			double periodOfUse = 1;
			if(frequencyOfUse[1].equals("Weeks")) {
				periodOfUse = 7;
			} else if (frequencyOfUse[1].equals("Months")) {
				periodOfUse = 30.417;
			}else if(!frequencyOfUse[1].equals("Days")) {
				throw new IllegalArgumentException("Invalid value for frecuency");
			}
			usageFrequencies[i] = (int)(Integer.parseInt(frequencyOfUse[0])*periodOfUse);
		}
		usagePerceptionSpeed = config.getParameterDoubleArray("usagePerceptionSpeed");
		usagePerceptionDecay = config.getParameterDoubleArray("usagePerceptionDecay");
		usageAwarenessImpact = config.getParameterDoubleArray("usageAwarenessImpact");
		usageDiscussionHeatImpact = config.getParameterDoubleArray("discussionHeatImpact");
		usageDiscussionHeatDecay = config.getParameterDoubleArray("discussionHeatDecay");
		
		try {
			intangibleAttributes = config.getParameterBooleanArray("intangibleAttributes");
		} catch (NullPointerException e) {
			intangibleAttributes = new boolean [numberOfAttributes];
		}
	}
	
	/**
	 * Loads model posting and reading online values from given 
	 * {@code ConfigFileReader} instance.
	 * @param config file reader managing load process.
	 */
	private void loadPostReadOnlineValuesFromFile(ConfigFileReader config) {
		onlinePostingProbabilities = 
			config.getParameterDoubleArray("onlinePostingProbability");
		onlineReadingProbabilities = 
			config.getParameterDoubleArray("onlineReadingProbability");
		onlineAwarenessImpact = 
			config.getParameterDoubleArray("onlineAwarenessImpact");
		onlinePerceptionSpeed = 
			config.getParameterDoubleArray("onlinePerceptionSpeed");
		onlinePerceptionDecay = 
			config.getParameterDoubleArray("onlinePerceptionDecay");
		onlineDiscussionHeatImpact = 
			config.getParameterDoubleArray("onlineDiscussionHeatImpact");
		onlineDiscussionHeatDecay = 
			config.getParameterDoubleArray("onlineDiscussionHeatDecay");		
	}
	
	/**
	 * Loads model brand values from given {@code ConfigFileReader} 
	 * instance.
	 * @param config file reader managing load process.
	 */
	private void loadBrandValuesFromFile(ConfigFileReader config) {
		brandInitialPenetration = 
				config.getParameterDoubleArray("initialPenetration");
		brandAttributes = 
				config.getParameterSeasonableDoubleArrayThreeDim(
						"attValues",numberOfAttributes,numberOfWeeks);
	}
	
	/**
	 * Loads model name tags from given {@code ConfigFileReader} 
	 * instance.
	 * @param config file reader managing load process.
	 */
	private void loadTagsFromFile(ConfigFileReader config) {
		segmentNames = config.getParameterStringArray("segmentNames");		
		attributeNames = config.getParameterStringArray("attributeNames");
		brandNames = config.getParameterStringArray("brandNames");
		if(numberOfTouchPoints>0) {
			touchPointNames = config.getParameterStringArray("tpNames");
		}
		
		try {
		creativityNames = config.getParameterStringArray3D("creativityNames");
		
		} catch (NullPointerException e) {
			//Create names			
			creativityNames = new String [numberOfTouchPoints][numberOfBrands][];			
			for (int i=0; i<numberOfTouchPoints; i++) {
				for (int j=0; j<numberOfBrands; j++) {
					int creativities = creativityByStep[i][j].length;
					creativityNames[i][j] = new String [creativities];
					for (int k=0; k<creativityByStep[i][j].length; k++) {
						creativityNames[i][j][k] = "Creativity "+(char)(65+k);
					}
				}
			}
		}
	}
	
	/**
	 * Creates a new model builder instance using stored model definition values.
	 * Model building will display times on console.
	 * 
	 * @return generated model instance.
	 */
	public ModelBuilder createBuilder() {
		
		ModelStepTranslator translator = 
			new ModelStepTranslator(this, stepsForWeek);
		
		ModelBuilder mb = new ModelBuilder(this, translator);

		return mb;
	}
	
	/**
	 * Generates a new model instance that only contains the agent social 
	 * network.
	 * 
	 * @return model instance only containing social network.
	 */
	public Model createNetwork() {
		ModelStepTranslator translator = 
			new ModelStepTranslator(this, stepsForWeek);
		
		ModelBuilder mb = new ModelBuilder(this, translator);
		
		return mb.buildNetwork(Model.DEFAULT_SEED);
	}
	
	// ########################################################################
	// Export values.
	// ########################################################################
	
	/**
	 * Export model definition values into a string representation. Every model 
	 * parameter is represented as coupled key:values.
	 * 
	 * @return a string representation containing model definition values.
	 */
	public String export() {
		String values ="";
		//Info
		values += exportInfoValues();
		//Market parameters
		values += exportMarketValues();
		//Decision Making
		values += exportDecisionMakingValues();
		//Segments
		values += exportSegmentValues();
		//WOM
		values += exportWOMValues();
		//TouchPoints
		values += exportTouchPointValues();
		//Sales
		values += exportSalesValues();
		//Product Usage
		values += exportProductUsageValues();
		//Posting/Reading Online
		values += exportPostReadOnlineValues();
		//Brands
		values += exportBrandValues();
		//Tags
		values += exportTagNames();
		
		return values;
	}
	
	/**
	 * Exports model information into a string representation. Parameters are 
	 * represented as coupled key:values.
	 * 
	 * @return a string representation containing model information.
	 */
	private String exportInfoValues() {
		String result = "";
		
		result += "name=" + name + "\n";
		result += "description=";
		
		
		if (description.trim().isEmpty()) {
			
			result += ""; // empty description
			
		} else {
		
			// Split lines from (usually) a TextArea
			// 	Windows: \r\n
			// 	Unix/Linux: \n
			// 	Mac (old ones): \r
			String[] lines = description.split("\r?\n|\r");
		
			// First line
			result += lines[0];
			
			// Additional lines
			for (int i = 1; i < lines.length; i++) {
				// special line break within field of properties file
				result += "\\n\\" + "\n\t" + lines[i];
			}
		}
		return result += "\n";
	}
	
	/**
	 * Exports market values into a string representation. Parameters are 
	 * represented as coupled key:values.
	 * 
	 * @return a string representation containing market values.
	 */
	private String exportMarketValues() {
		String result="";
		
		result+="numberOfAgents="+numberOfAgents+"\n";
		result+="numberOfSegments="+numberOfSegments+"\n";
		result+="nBrands="+numberOfBrands+"\n";
		result+="nAttributes="+numberOfAttributes+"\n";
		result+="nWeeks="+numberOfWeeks+"\n";
		result+="populationSize="+populationSize+"\n";
		
		result+="seed="+calibrationSeed+"\n";
		result+="stepsForWeek="+stepsForWeek+"\n";
		
		return result;
	}
	
	/**
	 * Exports decision making values into a string representation. Parameters 
	 * are represented as coupled key:values.
	 * 
	 * @return a string representation containing decision making values.
	 */
	private String exportDecisionMakingValues() {
		String result="";
		
		result+="involved="+involved+"\n";
		result+="emotional="+emotional+"\n";
		
		return result;
	}
	
	/**
	 * Exports segment values into a string representation. Parameters are 
	 * represented as coupled key:values.
	 * 
	 * @return a string representation containing segment values.
	 */
	private String exportSegmentValues() {
		String result="";
		
		result += "segmentSizes="+ArrayFunctions
				.arrayToString(segmentSizes) + "\n";
		result += "awarenessDecay="+ArrayFunctions
				.arrayToString(awarenessDecay)+"\n";
		result += "initialAwareness="+MatrixFunctions
				.matrixToString(initialAwareness)+"\n";
		result += "initialPerceptions="+MatrixFunctions
				.matrix3dToString(initialPerceptions)+"\n";
		result += "initialPerceptionsStandardDeviation="
				+initialPerceptionsStdDeviation+"\n";
		result += "drivers="+MatrixFunctions
				.matrixToString(drivers)+"\n";
		
		return result;
	}
	
	/**
	 * Exports word of mouth values into a string representation. Parameters 
	 * are represented as coupled key:values.
	 * 
	 * @return a string representation containing word of mouth values.
	 */
	private String exportWOMValues() {
		String result="";
		
		result += "womAwarenessImpact="+ArrayFunctions
				.arrayToString(womAwarenessImpact) + "\n";
		result += "womDiscussionHeatImpact="+ArrayFunctions
				.arrayToString(womDiscussionHeatImpact) + "\n";
		result += "womDiscussionHeatDecay="+ArrayFunctions
				.arrayToString(womDiscussionHeatDecay) + "\n";
		result += "womPerceptionSpeed="+ArrayFunctions
				.arrayToString(womPerceptionSpeed) + "\n";
		result += "womPerceptionDecay="+ArrayFunctions
				.arrayToString(womPerceptionDecay) + "\n";
		result += "talking="+ArrayFunctions
				.arrayToString(womTalkingProbability) + "\n";
		
		result += "influence="+MatrixFunctions
				.matrixToString(womSegmentInfluences) + "\n";
		result += "connectivity="+ArrayFunctions
				.arrayToString(womSegmentConnectivity) + "\n";
		
		result += "networkType="+typeOfNetwork.toString()+ "\n";

		return result;
	}
	
	/**
	 * Exports touch point values into a string representation. Parameters are 
	 * represented as coupled key:values.
	 * 
	 * @return a string representation containing touch point values.
	 */
	private String exportTouchPointValues() {
		String result="";
		
		result+="numTp="+numberOfTouchPoints+"\n";
		if(numberOfTouchPoints>0) {
			//Restore 3D value matrix
			storeTouchPointValues();
			
			result+="tpValues="+MatrixFunctions
					.matrix3dToString(touchPointValues)+"\n";
			
			double [][][] touchPointsQuality = 
					new double [numberOfTouchPoints][numberOfBrands][];
			double [][][] touchPointsEmphasis = 
					new double [numberOfTouchPoints][numberOfBrands][];
			
			for (int i=0; i<numberOfTouchPoints; i++) {
				for (int j=0; j<numberOfBrands; j++) {
					touchPointsQuality[i][j] = ArrayFunctions.flatDoubleArray(
							this.touchPointsQuality[i][j]
									);
					touchPointsEmphasis[i][j] = ArrayFunctions.flatDoubleArray(
							this.touchPointsEmphasis[i][j]
									);
				}
			}
			
			result+="quality="+MatrixFunctions
					.matrix3dToString(touchPointsQuality)+"\n";
			result+="emphasis="+MatrixFunctions
					.matrix3dToString(touchPointsEmphasis)+"\n";
			
			result+="creativityByStep="+MatrixFunctions
					.matrix3dToString(creativityByStep,':')+"\n";
			
			for (int i=0; i<numberOfTouchPoints; i++) {
				result+="weeklyPlanT"+i+"="+MatrixFunctions.
						matrix3dToString(
								touchPointsGRPMarketingPlan[i])+"\n";
			}
		}		
		
		return result;
	}
	
	/**
	 * Exports sales values into a string representation. Parameters are 
	 * represented as coupled key:values.
	 * 
	 * @return a string representation containing sales values.
	 */
	private String exportSalesValues() {
		String result="";
		
		result+="decisionCycle="+decisionCycleString+"\n";		
		result+="seasonality="+ArrayFunctions
				.arrayToString(seasonality)+"\n";
		if(availabilityByBrandAndStep!=null) {
			result+="availability="+MatrixFunctions
					.matrixToString(availabilityByBrandAndStep)+"\n";
		}
		result+="segmentShare="+ArrayFunctions
				.arrayToString(marketPercentBySegment)+"\n";

		return result;
	}
	
	/**
	 * Exports product usage values into a string representation. Parameters 
	 * are represented as coupled key:values.
	 * 
	 * @return a string representation containing product usage values.
	 */
	private String exportProductUsageValues() {
		String result="";
		
		result+="frecuencies="+usageFrequenciesString+"\n";		
		result+="usagePerceptionSpeed="+ArrayFunctions
				.arrayToString(usagePerceptionSpeed)+"\n";
		result+="usagePerceptionDecay="+ArrayFunctions
				.arrayToString(usagePerceptionDecay)+"\n";
		result+="usageAwarenessImpact="+ArrayFunctions
				.arrayToString(usageAwarenessImpact)+"\n";
		result+="discussionHeatImpact="+ArrayFunctions
				.arrayToString(usageDiscussionHeatImpact)+"\n";
		result+="discussionHeatDecay="+ArrayFunctions
				.arrayToString(usageDiscussionHeatDecay)+"\n";
		result+="intangibleAttributes="+ArrayFunctions
				.arrayToString(intangibleAttributes,',')+"\n";
		
		return result;
	}
	
	/**
	 * Exports posting and reading online values into a string representation. 
	 * Parameters are represented as coupled key:values.
	 * 
	 * @return a string representation containing posting and reading online 
	 * values.
	 */
	private String exportPostReadOnlineValues() {
		String result="";
		result+="onlinePostingProbability="+ArrayFunctions.arrayToString(onlinePostingProbabilities)+"\n";		
		result+="onlineReadingProbability="+ArrayFunctions.arrayToString(onlineReadingProbabilities)+"\n";
		result+="onlineAwarenessImpact="+ArrayFunctions.arrayToString(onlineAwarenessImpact)+"\n";		
		result+="onlinePerceptionSpeed="+ArrayFunctions.arrayToString(onlinePerceptionSpeed)+"\n";
		result+="onlinePerceptionDecay="+ArrayFunctions.arrayToString(onlinePerceptionDecay)+"\n";
		result+="onlineDiscussionHeatImpact="+ArrayFunctions.arrayToString(onlineDiscussionHeatImpact)+"\n";
		result+="onlineDiscussionHeatDecay="+ArrayFunctions.arrayToString(onlineDiscussionHeatDecay)+"\n";
		return result;
	}	
	
	/**
	 * Exports brands values into a string representation. Parameters are 
	 * represented as coupled key:values.
	 * 
	 * @return a string representation containing brands values.
	 */
	private String exportBrandValues() {
		String result="";
		
		result+="initialPenetration="+ArrayFunctions
				.arrayToString(brandInitialPenetration)+"\n";
		result+="attValues="+MatrixFunctions
				.matrix3dToString(brandAttributes)+"\n";
		
		return result;
	}
	
	/**
	 * Exports name tags into a string representation. Parameters are 
	 * represented as coupled key:values.
	 * 
	 * @return a string representation containing name tags.
	 */
	private String exportTagNames() {
		String result="";
		
		result+="segmentNames="+ArrayFunctions
				.arrayToString(segmentNames)+"\n";
		result+="brandNames="+ArrayFunctions
				.arrayToString(brandNames)+"\n";
		result+="attributeNames="+ArrayFunctions
				.arrayToString(attributeNames)+"\n";
		result+="tpNames="+ArrayFunctions
				.arrayToString(touchPointNames)+"\n";
		result+="creativityNames="+MatrixFunctions
				.matrix3dToString(creativityNames)+"\n";		
		
		return result;
	}	

	/*
	 * 
	 * GETTERS & SETTERS
	 * 
	 */
	
	/**
	 * Returns name tag for each segment.
	 * @return name tag for each segment.
	 */
	public String[] getSegmentNames() {
		return segmentNames;
	}

	/**
	 * Returns name tag for each brand.
	 * @return name tag for each brand.
	 */
	public String[] getBrandNames() {
		return brandNames;
	}

	/**
	 * Returns name tag for each attribute.
	 * @return name tag for each attribute.
	 */
	public String[] getAttributeNames() {
		return attributeNames;
	}

	/**
	 * Returns name tag for each touch point.
	 * @return name tag for each touch point.
	 */
	public String[] getTouchPointNames() {
		return touchPointNames;
	}
	
	/**
	 * Returns name tag for each touch point, including the earned ones.
	 * @return name tag for each touch point.
	 */
	public String[] getAllTouchPointNames() {
		
		String [] allNames = new String[numberOfTouchPoints 
		                                + AbstractTouchPoint.NUM_EARNED_TPS];
		allNames[AbstractTouchPoint.USE] = "USE";
		allNames[AbstractTouchPoint.WOM] = "WOM";
		allNames[AbstractTouchPoint.POST] = "POST";
		for (int i = 0; i < numberOfTouchPoints; i++) {
			allNames[i + AbstractTouchPoint.NUM_EARNED_TPS] = touchPointNames[i];
		}
		return allNames;
	}
	
	/**
	 * Number of brands considered at modeled market.
	 * @return number of brands at modeled market.
	 */
	public int getNumberOfBrands() {
		return numberOfBrands;
	}
	
	public void setNumberOfBrands(int brands) {
		numberOfBrands = brands;
	}

	/**
	 * Number of attributes considered at modeled market.
	 * @return number of attributes at modeled market.
	 */
	public int getNumberOfAttributes() {
		return numberOfAttributes;
	}
	
	public void setNumberOfAttributes(int attributes) {
		numberOfAttributes = attributes;
	}

	/**
	 * Number of segments considered at modeled market.
	 * @return number of segments at modeled market.
	 */
	public int getNumberOfSegments() {
		return numberOfSegments;
	}
	
	public void setNumberOfSegments(int segments) {
		numberOfSegments = segments;
	}
	
	/**
	 * Number of touch points considered at modeled market.
	 * @return number of touch points at modeled market.
	 */
	public int getNumberOfTouchPoints() {
		return numberOfTouchPoints;
	}
	
	public void setNumberOfTouchtPoints(int touchpoints) {
		numberOfTouchPoints = touchpoints;
	}
	
	/**
	 * Number of agents considered at modeled market.
	 * @return number of agents at modeled market.
	 */
	public int getNumberOfAgents() {
		return numberOfAgents;
	}
	
	public void setNumberOfAgents(int newNumber) {
		numberOfAgents=newNumber;
	}

	/**
	 * Size of the population considered at modeled market.
	 * @return size of the population at modeled market.
	 */
	public int getPopulationSize() {
		return populationSize;
	}
	
	public void setPopulationsSize(int newNumber) {
		populationSize = newNumber;
	}
	
	/**
	 * Number of steps considered at current simulation.
	 * @return number of weeks at current simulation.
	 */
	public int getNumberOfSteps() {
		return numberOfWeeks*stepsForWeek;
	}
	
	/**
	 * Number of weeks considered at current simulation.
	 * @return number of weeks at current simulation.
	 */
	public int getNumberOfWeeks() {
		return numberOfWeeks;
	}
	
	public void setNumberOfWeeks(int newNumber) {
		numberOfWeeks = newNumber;
	}
	
	/**
	 * Number of steps by week considered at current simulation.
	 * @return number of steps by week at current simulation.
	 */
	public int getStepsForWeek() {
		return stepsForWeek;
	}
	
	/**
	 * Returns the emotional value for current market.
	 * @return emotional value.
	 */
	public double getEmotional() {
		return emotional;
	}
	
	public void setEmotional(double emotional) {
		this.emotional = emotional;
	}
	
	/**
	 * Returns the involved value for current market.
	 * @return involved value.
	 */
	public double getInvolved() {
		return involved;
	}
	
	public void setInvolved(double involved) {
		this.involved = involved;
	}
	
	/**
	 * Returns the seed used by calibration algorithms.
	 * @return calibration seed.
	 */
	public long getCalibrationSeed() {
		return calibrationSeed;
	}
	
	/**
	 * Tunes the number of steps by week for current simulation to given value.
	 * @param stepsForWeek new number of steps by week for current simulation.
	 */
	public void setStepsForWeek(int stepsForWeek) {
		this.stepsForWeek = stepsForWeek;
	}

	/**
	 * Returns the number of steps for current decision cycle value.
	 * @return the number of steps for current decision cycle value.
	 */
	public int getDecisionCycle() {
		return buyingDecisionCycle;
	}
	
	/**
	 * Sets the number of steps for current decision cycle value.
	 * @param decisionCycle new decision cycle value in steps.
	 */
	public void setDecisionCycle(int decisionCycle) {
		buyingDecisionCycle=decisionCycle;
	}
	
	/**
	 * Returns usage frequency in number of steps for every segment.
	 * @return usage frequency in number of steps for every segment.
	 */
	public int[] getUsageFrequencies() {
		return usageFrequencies;
	}
	
	/**
	 * Sets usage frequencies for every segment to given values.
	 * @param frequencies new usage frequency for every segment.
	 */
	public void setUsageFrequencies(int[] frequencies) {
		usageFrequencies=frequencies;
	}

	/**
	 * Sets a new seed to be used by calibration module.
	 * @param seed new seed for calibration module.
	 */
	public void setCalibrationSeed(long seed) {
		this.calibrationSeed = seed;
	}
	
	/**
	 * Returns model transformation ratio population/agent proportion.
	 * @return ratio population/agent proportion.
	 */
	public double getAgentsRatio() {
		return populationSize / (double) numberOfAgents;
	}
	
	/**
	 * Sets a new value for standard deviation, used while initial perception 
	 * generation.
	 * 
	 * @param initialPerceptionsStdDeviation standard deviation for initial 
	 * perceptions.
	 */
	public void setInitialPerceptionsStdDeviation(
			double initialPerceptionsStdDeviation) {
		this.initialPerceptionsStdDeviation 
			= initialPerceptionsStdDeviation;
	}
	
	/**
	 * Returns standard deviation for initial perceptions.
	 * 
	 * @return standard deviation for initial perceptions.
	 */
	public double getInitialPerceptionsStdDeviation() {
		return initialPerceptionsStdDeviation;
	}
	
	/**
	 * Returns initial perception values by segment, brand and attribute.
	 * 
	 * @return initial perception values by segment, brand and attribute.
	 */
	public double[][][] getInitialPerceptions() {
		return initialPerceptions;
	}
	
	public void setInitialPerceptions(double[][][] initialPerceptions) {
		this.initialPerceptions = initialPerceptions;
	}
	
	public double[] getSegmentSizes() {
		return segmentSizes;
	}

	public void setSegmentSizes(double[] segmentSizes) {
		this.segmentSizes = segmentSizes;
	}

	public double[][] getInitialAwareness() {
		return initialAwareness;
	}

	public void setInitialAwareness(double[][] initialAwareness) {
		this.initialAwareness = initialAwareness;
	}

	public double[] getAwarenessDecay() {
		return awarenessDecay;
	}

	public void setAwarenessDecay(double[] awarenessDecay) {
		this.awarenessDecay = awarenessDecay;
	}

	public double[][] getDrivers() {
		return drivers;
	}

	public void setDrivers(double[][] drivers) {
		this.drivers = drivers;
	}

	public double[] getWomAwarenessImpact() {
		return womAwarenessImpact;
	}

	public void setWomAwarenessImpact(double[] womAwarenessImpact) {
		this.womAwarenessImpact = womAwarenessImpact;
	}

	public double[] getWomDiscussionHeatImpact() {
		return womDiscussionHeatImpact;
	}

	public void setWomDiscussionHeatImpact(double[] womDiscussionHeatImpact) {
		this.womDiscussionHeatImpact = womDiscussionHeatImpact;
	}

	public double[] getWomDiscussionHeatDecay() {
		return womDiscussionHeatDecay;
	}

	public void setWomDiscussionHeatDecay(double[] womDiscussionHeatDecay) {
		this.womDiscussionHeatDecay = womDiscussionHeatDecay;
	}

	public double[] getWomPerceptionSpeed() {
		return womPerceptionSpeed;
	}

	public void setWomPerceptionSpeed(double[] womPerceptionSpeed) {
		this.womPerceptionSpeed = womPerceptionSpeed;
	}

	public double[] getWomPerceptionDecay() {
		return womPerceptionDecay;
	}

	public void setWomPerceptionDecay(double[] womPerceptionDecay) {
		this.womPerceptionDecay = womPerceptionDecay;
	}

	public double[] getWomTalkingProbability() {
		return womTalkingProbability;
	}

	public void setWomTalkingProbability(double[] womTalkingProbability) {
		this.womTalkingProbability = womTalkingProbability;
	}

	public double[][] getWomSegmentInfluences() {
		return womSegmentInfluences;
	}

	public void setWomSegmentInfluences(double[][] womSegmentInfluences) {
		this.womSegmentInfluences = womSegmentInfluences;
	}

	public double[] getWomSegmentConnectivity() {
		return womSegmentConnectivity;
	}

	public void setWomSegmentConnectivity(double[] womSegmentConnectivity) {
		this.womSegmentConnectivity = womSegmentConnectivity;
	}

	public double getWomSentimentPositive() {
		return womSentimentPositive;
	}

	public void setWomSentimentPositive(double womSentimentPositive) {
		this.womSentimentPositive = womSentimentPositive;
	}

	public double getWomSentimentNegative() {
		return womSentimentNegative;
	}

	public void setWomSentimentNegative(double womSentimentNegative) {
		this.womSentimentNegative = womSentimentNegative;
	}

	public NetworkType getTypeOfNetwork() {
		return typeOfNetwork;
	}

	public void setTypeOfNetwork(NetworkType typeOfNetwork) {
		this.typeOfNetwork = typeOfNetwork;
	}
	
	public double[][][] getTouchPointValues() {
		return touchPointValues;
	}

	public void setTouchPointValues(double[][][] touchPointValues) {
		this.touchPointValues = touchPointValues;
	}

	public double[][] getTouchPointsWeeklyReachMax() {
		return touchPointsWeeklyReachMax;
	}

	public void setTouchPointsWeeklyReachMax(double[][] touchPointsWeeklyReachMax) {
		this.touchPointsWeeklyReachMax = touchPointsWeeklyReachMax;
	}

	public double[][] getTouchPointsAnnualReachMax() {
		return touchPointsAnnualReachMax;
	}

	public void setTouchPointsAnnualReachMax(double[][] touchPointsAnnualReachMax) {
		this.touchPointsAnnualReachMax = touchPointsAnnualReachMax;
	}

	public double[][] getTouchPointsAnnualReachSpeed() {
		return touchPointsAnnualReachSpeed;
	}

	public void setTouchPointsAnnualReachSpeed(double[][] touchPointsAnnualReachSpeed) {
		this.touchPointsAnnualReachSpeed = touchPointsAnnualReachSpeed;
	}

	public double[][] getTouchPointsPerceptionPotential() {
		return touchPointsPerceptionPotential;
	}

	public void setTouchPointsPerceptionPotential(double[][] touchPointsPerceptionPotential) {
		this.touchPointsPerceptionPotential = touchPointsPerceptionPotential;
	}

	public double[][] getTouchPointsPerceptionSpeed() {
		return touchPointsPerceptionSpeed;
	}

	public void setTouchPointsPerceptionSpeed(double[][] touchPointsPerceptionSpeed) {
		this.touchPointsPerceptionSpeed = touchPointsPerceptionSpeed;
	}

	public double[][] getTouchPointsAwarenessImpact() {
		return touchPointsAwarenessImpact;
	}

	public void setTouchPointsAwarenessImpact(double[][] touchPointsAwarenessImpact) {
		this.touchPointsAwarenessImpact = touchPointsAwarenessImpact;
	}

	public double[][] getTouchPointsDiscusionHeatImpact() {
		return touchPointsDiscusionHeatImpact;
	}

	public void setTouchPointsDiscusionHeatImpact(double[][] touchPointsDiscusionHeatImpact) {
		this.touchPointsDiscusionHeatImpact = touchPointsDiscusionHeatImpact;
	}

	public double[][] getTouchPointsPerceptionDecay() {
		return touchPointsPerceptionDecay;
	}

	public void setTouchPointsPerceptionDecay(double[][] touchPointsPerceptionDecay) {
		this.touchPointsPerceptionDecay = touchPointsPerceptionDecay;
	}

	public double[][] getTouchPointsDiscusionHeatDecay() {
		return touchPointsDiscusionHeatDecay;
	}

	public void setTouchPointsDiscusionHeatDecay(double[][] touchPointsDiscusionHeatDecay) {
		this.touchPointsDiscusionHeatDecay = touchPointsDiscusionHeatDecay;
	}

	public InvestmentType[] getTouchPointsInvestment() {
		return touchPointsInvestment;
	}

	public void setTouchPointsInvestment(InvestmentType[] touchPointsInvestment) {
		this.touchPointsInvestment = touchPointsInvestment;
	}

	public double[][][][] getTouchPointsEmphasis() {
		return touchPointsEmphasis;
	}

	public void setTouchPointsEmphasis(double[][][][] touchPointsEmphasis) {
		this.touchPointsEmphasis = touchPointsEmphasis;
	}

	public double[][][][] getTouchPointsQuality() {
		return touchPointsQuality;
	}

	public void setTouchPointsQuality(double[][][][] touchPointsQuality) {
		this.touchPointsQuality = touchPointsQuality;
	}

	public byte[][][] getCreativityByStep() {
		return creativityByStep;
	}

	public void setCreativityByStep(byte[][][] creativityByStep) {
		this.creativityByStep = creativityByStep;
	}

	/**
	 * Returns sales history by week.
	 * 
	 * @return sales history by week.
	 */
	public double[] getSeasonality() {
		return seasonality;
	}
	
	public void setSeasonality(double[] seasonality) {
		this.seasonality = seasonality;
	}
	
	public double[] getMarketPercentBySegment() {
		return marketPercentBySegment;
	}

	public void setMarketPercentBySegment(double[] marketPercentBySegment) {
		this.marketPercentBySegment = marketPercentBySegment;
	}

	public double[][] getAvailabilityByBrandAndStep() {
		return availabilityByBrandAndStep;
	}

	public void setAvailabilityByBrandAndStep(double[][] availabilityByBrandAndStep) {
		this.availabilityByBrandAndStep = availabilityByBrandAndStep;
	}

	/**
	 * Returns media investment by touch point owned, brand, segment and week.
	 * 
	 * @return GRP media investment by touch point owned, brand, segment and 
	 * week.
	 */
	public double [][][][] getGRP() {
		return touchPointsGRPMarketingPlan;
	}
	
	/**
	 * Sets GRP media investment to new values for touchpoint, brand, segment 
	 * and week.
	 * @param grp new media investment values.
	 */
	public void setGRP(double[][][][] grp) {
		touchPointsGRPMarketingPlan = grp;
	}
	
	public int getBuyingDecisionCycle() {
		return buyingDecisionCycle;
	}

	public void setBuyingDecisionCycle(int buyingDecisionCycle) {
		this.buyingDecisionCycle = buyingDecisionCycle;
	}

	public double[] getUsagePerceptionSpeed() {
		return usagePerceptionSpeed;
	}

	public void setUsagePerceptionSpeed(double[] usagePerceptionSpeed) {
		this.usagePerceptionSpeed = usagePerceptionSpeed;
	}

	public double[] getUsagePerceptionDecay() {
		return usagePerceptionDecay;
	}

	public void setUsagePerceptionDecay(double[] usagePerceptionDecay) {
		this.usagePerceptionDecay = usagePerceptionDecay;
	}

	public double[] getUsageAwarenessImpact() {
		return usageAwarenessImpact;
	}

	public void setUsageAwarenessImpact(double[] usageAwarenessImpact) {
		this.usageAwarenessImpact = usageAwarenessImpact;
	}

	public double[] getUsageDiscussionHeatImpact() {
		return usageDiscussionHeatImpact;
	}

	public void setUsageDiscussionHeatImpact(double[] usageDiscussionHeatImpact) {
		this.usageDiscussionHeatImpact = usageDiscussionHeatImpact;
	}

	public double[] getUsageDiscussionHeatDecay() {
		return usageDiscussionHeatDecay;
	}

	public void setUsageDiscussionHeatDecay(double[] usageDiscussionHeatDecay) {
		this.usageDiscussionHeatDecay = usageDiscussionHeatDecay;
	}

	public boolean[] getIntangibleAttributes() {
		return intangibleAttributes;
	}

	public void setIntangibleAttributes(boolean[] intangibleAttributes) {
		this.intangibleAttributes = intangibleAttributes;
	}

	public double[] getOnlinePostingProbabilities() {
		return onlinePostingProbabilities;
	}

	public void setOnlinePostingProbabilities(double[] onlinePostingProbabilities) {
		this.onlinePostingProbabilities = onlinePostingProbabilities;
	}

	public double[] getOnlineReadingProbabilities() {
		return onlineReadingProbabilities;
	}

	public void setOnlineReadingProbabilities(double[] onlineReadingProbabilities) {
		this.onlineReadingProbabilities = onlineReadingProbabilities;
	}

	public double[] getOnlineAwarenessImpact() {
		return onlineAwarenessImpact;
	}

	public void setOnlineAwarenessImpact(double[] onlineAwarenessImpact) {
		this.onlineAwarenessImpact = onlineAwarenessImpact;
	}

	public double[] getOnlinePerceptionSpeed() {
		return onlinePerceptionSpeed;
	}

	public void setOnlinePerceptionSpeed(double[] onlinePerceptionSpeed) {
		this.onlinePerceptionSpeed = onlinePerceptionSpeed;
	}

	public double[] getOnlinePerceptionDecay() {
		return onlinePerceptionDecay;
	}

	public void setOnlinePerceptionDecay(double[] onlinePerceptionDecay) {
		this.onlinePerceptionDecay = onlinePerceptionDecay;
	}

	public double[] getOnlineDiscussionHeatImpact() {
		return onlineDiscussionHeatImpact;
	}

	public void setOnlineDiscussionHeatImpact(double[] onlineDiscussionHeatImpact) {
		this.onlineDiscussionHeatImpact = onlineDiscussionHeatImpact;
	}

	public double[] getOnlineDiscussionHeatDecay() {
		return onlineDiscussionHeatDecay;
	}

	public void setOnlineDiscussionHeatDecay(double[] onlineDiscussionHeatDecay) {
		this.onlineDiscussionHeatDecay = onlineDiscussionHeatDecay;
	}

	public double[] getBrandInitialPenetration() {
		return brandInitialPenetration;
	}

	public void setBrandInitialPenetration(double[] brandInitialPenetration) {
		this.brandInitialPenetration = brandInitialPenetration;
	}

	public double[][][] getBrandAttributes() {
		return brandAttributes;
	}

	public void setBrandAttributes(double[][][] brandAttributes) {
		this.brandAttributes = brandAttributes;
	}

	/**
	 * Returns model name.
	 * @return model name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns model description.
	 * @return model description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets model name to a new value.
	 * @param name a new name for the model.
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String[][][] getCreativityNames() {
		return creativityNames;
	}

	public void setCreativityNames(String[][][] creativityNames) {
		this.creativityNames = creativityNames;
	}
	
	public String getDecisionCycleString() {
		return decisionCycleString;
	}

	public void setDecisionCycleString(String decisionCycleString) {
		this.decisionCycleString = decisionCycleString;
	}

	public String getUsageFrequenciesString() {
		return usageFrequenciesString;
	}

	public void setUsageFrequenciesString(String usageFrequenciesString) {
		this.usageFrequenciesString = usageFrequenciesString;
	}

	/**
	 * Sets model description to a new value.
	 * @param description a new description for the model.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public boolean isSimple() {
		return simple;
	}

	public void setSimple(boolean simple) {
		this.simple = simple;
	}

	public void setSegmentNames(String[] segmentNames) {
		this.segmentNames = segmentNames;
	}

	public void setBrandNames(String[] brandNames) {
		this.brandNames = brandNames;
	}

	public void setAttributeNames(String[] attributeNames) {
		this.attributeNames = attributeNames;
	}

	public void setTouchPointNames(String[] touchPointNames) {
		this.touchPointNames = touchPointNames;
	}

	public boolean isAwarenessFilter() {
		return awarenessFilter;
	}

	public void setAwarenessFilter(boolean awarenessFilter) {
		this.awarenessFilter = awarenessFilter;
	}

	public void writeModelDefinition(
			File modelFile
			) throws IOException {
		
		FileWriter fw;
		fw = new FileWriter(modelFile);
		String properties = export();
		fw.write(properties);
		fw.close();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(availabilityByBrandAndStep);
		result = prime * result + Arrays.hashCode(awarenessDecay);
		result = prime * result + (awarenessFilter ? 1231 : 1237);
		result = prime * result + Arrays.deepHashCode(brandAttributes);
		result = prime * result + Arrays.hashCode(brandInitialPenetration);
		result = prime * result + buyingDecisionCycle;
		result = prime * result + (int) (calibrationSeed ^ (calibrationSeed >>> 32));
		result = prime * result + Arrays.deepHashCode(creativityByStep);
		result = prime * result + (debug ? 1231 : 1237);
		result = prime * result + Arrays.deepHashCode(drivers);
		long temp;
		temp = Double.doubleToLongBits(emotional);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.deepHashCode(initialAwareness);
		result = prime * result + Arrays.deepHashCode(initialPerceptions);
		temp = Double.doubleToLongBits(initialPerceptionsStdDeviation);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(intangibleAttributes);
		temp = Double.doubleToLongBits(involved);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(marketPercentBySegment);
		result = prime * result + numberOfAgents;
		result = prime * result + numberOfAttributes;
		result = prime * result + numberOfBrands;
		result = prime * result + numberOfSegments;
		result = prime * result + numberOfTouchPoints;
		result = prime * result + numberOfWeeks;
		result = prime * result + Arrays.hashCode(onlineAwarenessImpact);
		result = prime * result + Arrays.hashCode(onlineDiscussionHeatDecay);
		result = prime * result + Arrays.hashCode(onlineDiscussionHeatImpact);
		result = prime * result + Arrays.hashCode(onlinePerceptionDecay);
		result = prime * result + Arrays.hashCode(onlinePerceptionSpeed);
		result = prime * result + Arrays.hashCode(onlinePostingProbabilities);
		result = prime * result + Arrays.hashCode(onlineReadingProbabilities);
		result = prime * result + populationSize;
		result = prime * result + salesCheckpoint;
		result = prime * result + Arrays.hashCode(seasonality);
		result = prime * result + Arrays.hashCode(segmentSizes);
		result = prime * result + stepsForWeek;
		result = prime * result + Arrays.deepHashCode(touchPointsAnnualReachMax);
		result = prime * result + Arrays.deepHashCode(touchPointsAnnualReachSpeed);
		result = prime * result + Arrays.deepHashCode(touchPointsAwarenessImpact);
		result = prime * result + Arrays.deepHashCode(touchPointsDiscusionHeatDecay);
		result = prime * result + Arrays.deepHashCode(touchPointsDiscusionHeatImpact);
		result = prime * result + Arrays.deepHashCode(touchPointsEmphasis);
		result = prime * result + Arrays.deepHashCode(touchPointsGRPMarketingPlan);
		result = prime * result + Arrays.deepHashCode(touchPointsPerceptionDecay);
		result = prime * result + Arrays.deepHashCode(touchPointsPerceptionPotential);
		result = prime * result + Arrays.deepHashCode(touchPointsPerceptionSpeed);
		result = prime * result + Arrays.deepHashCode(touchPointsQuality);
		result = prime * result + Arrays.deepHashCode(touchPointsWeeklyReachMax);
		result = prime * result + ((typeOfNetwork == null) ? 0 : typeOfNetwork.hashCode());
		result = prime * result + Arrays.hashCode(usageAwarenessImpact);
		result = prime * result + Arrays.hashCode(usageDiscussionHeatDecay);
		result = prime * result + Arrays.hashCode(usageDiscussionHeatImpact);
		result = prime * result + Arrays.hashCode(usageFrequencies);
		result = prime * result + Arrays.hashCode(usagePerceptionDecay);
		result = prime * result + Arrays.hashCode(usagePerceptionSpeed);
		result = prime * result + Arrays.hashCode(womAwarenessImpact);
		result = prime * result + Arrays.hashCode(womDiscussionHeatDecay);
		result = prime * result + Arrays.hashCode(womDiscussionHeatImpact);
		result = prime * result + Arrays.hashCode(womPerceptionDecay);
		result = prime * result + Arrays.hashCode(womPerceptionSpeed);
		result = prime * result + Arrays.hashCode(womSegmentConnectivity);
		result = prime * result + Arrays.deepHashCode(womSegmentInfluences);
		temp = Double.doubleToLongBits(womSentimentNegative);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(womSentimentPositive);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(womTalkingProbability);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelDefinition other = (ModelDefinition) obj;
		if (!Arrays.deepEquals(availabilityByBrandAndStep, other.availabilityByBrandAndStep))
			return false;
		if (!Arrays.equals(awarenessDecay, other.awarenessDecay))
			return false;
		if (awarenessFilter != other.awarenessFilter)
			return false;
		if (!Arrays.deepEquals(brandAttributes, other.brandAttributes))
			return false;
		if (!Arrays.equals(brandInitialPenetration, other.brandInitialPenetration))
			return false;
		if (buyingDecisionCycle != other.buyingDecisionCycle)
			return false;
		if (!Arrays.deepEquals(creativityByStep, other.creativityByStep))
			return false;
		if (debug != other.debug)
			return false;
		if (!Arrays.deepEquals(drivers, other.drivers))
			return false;
		if (Double.doubleToLongBits(emotional) != Double.doubleToLongBits(other.emotional))
			return false;
		if (!Arrays.deepEquals(initialAwareness, other.initialAwareness))
			return false;
		if (!Arrays.deepEquals(initialPerceptions, other.initialPerceptions))
			return false;
		if (Double.doubleToLongBits(initialPerceptionsStdDeviation) != Double
				.doubleToLongBits(other.initialPerceptionsStdDeviation))
			return false;
		if (!Arrays.equals(intangibleAttributes, other.intangibleAttributes))
			return false;
		if (Double.doubleToLongBits(involved) != Double.doubleToLongBits(other.involved))
			return false;
		if (!Arrays.equals(marketPercentBySegment, other.marketPercentBySegment))
			return false;
		if (numberOfAgents != other.numberOfAgents)
			return false;
		if (numberOfAttributes != other.numberOfAttributes)
			return false;
		if (numberOfBrands != other.numberOfBrands)
			return false;
		if (numberOfSegments != other.numberOfSegments)
			return false;
		if (numberOfTouchPoints != other.numberOfTouchPoints)
			return false;
		if (numberOfWeeks != other.numberOfWeeks)
			return false;
		if (!Arrays.equals(onlineAwarenessImpact, other.onlineAwarenessImpact))
			return false;
		if (!Arrays.equals(onlineDiscussionHeatDecay, other.onlineDiscussionHeatDecay))
			return false;
		if (!Arrays.equals(onlineDiscussionHeatImpact, other.onlineDiscussionHeatImpact))
			return false;
		if (!Arrays.equals(onlinePerceptionDecay, other.onlinePerceptionDecay))
			return false;
		if (!Arrays.equals(onlinePerceptionSpeed, other.onlinePerceptionSpeed))
			return false;
		if (!Arrays.equals(onlinePostingProbabilities, other.onlinePostingProbabilities))
			return false;
		if (!Arrays.equals(onlineReadingProbabilities, other.onlineReadingProbabilities))
			return false;
		if (populationSize != other.populationSize)
			return false;
		if (salesCheckpoint != other.salesCheckpoint)
			return false;
		if (!Arrays.equals(seasonality, other.seasonality))
			return false;
		if (!Arrays.equals(segmentSizes, other.segmentSizes))
			return false;
		if (stepsForWeek != other.stepsForWeek)
			return false;
		if (!Arrays.deepEquals(touchPointsAnnualReachMax, other.touchPointsAnnualReachMax))
			return false;
		if (!Arrays.deepEquals(touchPointsAnnualReachSpeed, other.touchPointsAnnualReachSpeed))
			return false;
		if (!Arrays.deepEquals(touchPointsAwarenessImpact, other.touchPointsAwarenessImpact))
			return false;
		if (!Arrays.deepEquals(touchPointsDiscusionHeatDecay, other.touchPointsDiscusionHeatDecay))
			return false;
		if (!Arrays.deepEquals(touchPointsDiscusionHeatImpact, other.touchPointsDiscusionHeatImpact))
			return false;
		if (!Arrays.deepEquals(touchPointsEmphasis, other.touchPointsEmphasis))
			return false;
		if (!Arrays.deepEquals(touchPointsGRPMarketingPlan, other.touchPointsGRPMarketingPlan))
			return false;
		if (!Arrays.deepEquals(touchPointsPerceptionDecay, other.touchPointsPerceptionDecay))
			return false;
		if (!Arrays.deepEquals(touchPointsPerceptionPotential, other.touchPointsPerceptionPotential))
			return false;
		if (!Arrays.deepEquals(touchPointsPerceptionSpeed, other.touchPointsPerceptionSpeed))
			return false;
		if (!Arrays.deepEquals(touchPointsQuality, other.touchPointsQuality))
			return false;
		if (!Arrays.deepEquals(touchPointsWeeklyReachMax, other.touchPointsWeeklyReachMax))
			return false;
		if (typeOfNetwork != other.typeOfNetwork)
			return false;
		if (!Arrays.equals(usageAwarenessImpact, other.usageAwarenessImpact))
			return false;
		if (!Arrays.equals(usageDiscussionHeatDecay, other.usageDiscussionHeatDecay))
			return false;
		if (!Arrays.equals(usageDiscussionHeatImpact, other.usageDiscussionHeatImpact))
			return false;
		if (!Arrays.equals(usageFrequencies, other.usageFrequencies))
			return false;
		if (!Arrays.equals(usagePerceptionDecay, other.usagePerceptionDecay))
			return false;
		if (!Arrays.equals(usagePerceptionSpeed, other.usagePerceptionSpeed))
			return false;
		if (!Arrays.equals(womAwarenessImpact, other.womAwarenessImpact))
			return false;
		if (!Arrays.equals(womDiscussionHeatDecay, other.womDiscussionHeatDecay))
			return false;
		if (!Arrays.equals(womDiscussionHeatImpact, other.womDiscussionHeatImpact))
			return false;
		if (!Arrays.equals(womPerceptionDecay, other.womPerceptionDecay))
			return false;
		if (!Arrays.equals(womPerceptionSpeed, other.womPerceptionSpeed))
			return false;
		if (!Arrays.equals(womSegmentConnectivity, other.womSegmentConnectivity))
			return false;
		if (!Arrays.deepEquals(womSegmentInfluences, other.womSegmentInfluences))
			return false;
		if (Double.doubleToLongBits(womSentimentNegative) != Double.doubleToLongBits(other.womSentimentNegative))
			return false;
		if (Double.doubleToLongBits(womSentimentPositive) != Double.doubleToLongBits(other.womSentimentPositive))
			return false;
		if (!Arrays.equals(womTalkingProbability, other.womTalkingProbability))
			return false;
		return true;
	}
	
	
}
