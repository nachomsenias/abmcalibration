package es.ugr.sci2s.soccer.beans;

import java.util.Arrays;

import model.ModelDefinition;
import model.socialnetwork.SocialNetwork.NetworkType;
import model.touchpoints.TouchPointOwned;
import model.touchpoints.TouchPointOwned.InvestmentType;
import util.functions.ArrayFunctions;
import util.io.StatisticsRecordingBean;
import util.statistics.Statistics.TimePeriod;

public class SimulationConfig {

	//High level data
	private int numberOfAgents;
	private int populationSize;
	private int nBrands;
	private int nAttributes;
	private int nSegments;
	private int	nTp;
	private int nWeeks;
	private int stepsForWeek = 1;	
	private double emotional;
	private double involved;
	private double 	initialPerceptionsStdDeviation;
	private int nMC;
	private final boolean exportSales = true;
	private boolean exportAwareness;
	private boolean exportPerceptions;
	private boolean exportTouchPointContributions;
	private boolean exportWomReach;
	private boolean exportWomVolumen;
	private boolean exportWomSentiment;
	private boolean exportWomContributions;
	private boolean exportTPReach;
	private TimePeriod statPeriod;
	
	//Segment data
	private double [] segmentSizes;
	private double [] awarenessDecay;
	private double [][] initialAwareness;
	private double [][] drivers;
	private double [][][] initialPerceptions;
	
	//Diffusion data
	private double [] womAwarenessImpact;
	private double [] womDiscussionHeatImpact;
	private double [] womDiscussionHeatDecay;
	private double [] womPerceptionSpeed;
	private double [] womPerceptionDecay;
	private double [] talking;
	private double [][] influence;
	private double [] connectivity;
	private double womSentimentPositive;
	private double womSentimentNegative;
	private NetworkType typeOfNetwork;
	
	//TouchPoints data
	private double [][] 	touchPointsWeeklyReachMax;
	private double [][]		touchPointsAnnualReachMax;
	private double [][] 	touchPointsAnnualReachSpeed;
	private double [][] 	touchPointsPerceptionPotential;
	private double [][] 	touchPointsPerceptionSpeed;
	private double [][]		touchPointsAwarenessImpact;
	private double [][]		touchPointsDiscusionHeatImpact;
	private double [][]		touchPointsPerceptionDecay;
	private double [][]		touchPointsDiscusionHeatDecay;
	private double [][][][] touchPointsGRPMarketingPlan;
	private double [][][][] touchPointsEmphasis;
	private double [][][][] touchPointsQuality;
	private byte [][][]		creativityByStep;
	
	private InvestmentType [] touchPointsInvestment;
	
	//Sales data
	private double [] 	seasonality;
	private double [] 	marketPercentBySegment;
	private double [][] availabilityByBrandAndStep;
	private int buyingDecisionCycle;
	
	//Product Usage data
	private int [] 	usageFrequencies;
	private double [] 	usagePerceptionSpeed;
	private double [] 	usagePerceptionDecay;
	private double [] 	usageAwarenessImpact;
	private double [] 	usageDiscussionHeatImpact;
	private double [] 	usageDiscussionHeatDecay;
	private boolean [] 	intangibleAttributes;
	private double [] brandInitialPenetration;
	private double [][][] brandAttributes;
	
	//Online Posting data
	private double [] onlinePostingProbabilities;
	private double [] onlineReadingProbabilities;
	private double [] onlineAwarenessImpact;
	private double [] onlinePerceptionSpeed;
	private double [] onlinePerceptionDecay;
	private double [] onlineDiscussionHeatImpact;
	private double [] onlineDiscussionHeatDecay;
	
	//Communication settings
	private String responseURL;
	
	private boolean simple;
	
	public ModelDefinition getModelDefinition() {
		ModelDefinition md = new ModelDefinition();
		
		//High level data
		md.setNumberOfAgents(numberOfAgents);
		md.setPopulationsSize(populationSize);
		md.setNumberOfBrands(nBrands);
		md.setNumberOfAttributes(nAttributes);
		md.setNumberOfSegments(nSegments);
		md.setNumberOfTouchtPoints(nTp);
		md.setNumberOfWeeks(nWeeks);
		md.setStepsForWeek(stepsForWeek);
		md.setEmotional(emotional);
		md.setInvolved(involved);
		md.setInitialPerceptionsStdDeviation(initialPerceptionsStdDeviation);
		
		//Segment data
		md.setSegmentSizes(segmentSizes);
		md.setAwarenessDecay(awarenessDecay);
		md.setInitialAwareness(initialAwareness);
		md.setDrivers(drivers);
		md.setInitialPerceptions(initialPerceptions);
		
		//Diffusion data
		md.setWomAwarenessImpact(womAwarenessImpact);
		md.setWomDiscussionHeatImpact(womDiscussionHeatImpact);
		md.setWomDiscussionHeatDecay(womDiscussionHeatDecay);
		md.setWomPerceptionSpeed(womPerceptionSpeed);
		md.setWomPerceptionDecay(womPerceptionDecay);
		md.setWomTalkingProbability(talking);
		md.setWomSegmentInfluences(influence);
		md.setWomSegmentConnectivity(connectivity);
		md.setWomSentimentNegative(womSentimentNegative);
		md.setWomSentimentPositive(womSentimentPositive);
		md.setTypeOfNetwork(typeOfNetwork);
		
		//TouchPoints data
		md.setTouchPointsWeeklyReachMax(touchPointsWeeklyReachMax);
		md.setTouchPointsAnnualReachMax(touchPointsAnnualReachMax);
		md.setTouchPointsAnnualReachSpeed(touchPointsAnnualReachSpeed);
		md.setTouchPointsPerceptionPotential(touchPointsPerceptionPotential);
		md.setTouchPointsPerceptionSpeed(touchPointsPerceptionSpeed);
		md.setTouchPointsPerceptionDecay(touchPointsPerceptionDecay);
		md.setTouchPointsAwarenessImpact(touchPointsAwarenessImpact);
		md.setTouchPointsDiscusionHeatImpact(touchPointsDiscusionHeatImpact);
		md.setTouchPointsDiscusionHeatDecay(touchPointsDiscusionHeatDecay);
		md.setGRP(touchPointsGRPMarketingPlan);
		md.setTouchPointsEmphasis(touchPointsEmphasis);
		md.setTouchPointsQuality(touchPointsQuality);
		md.setCreativityByStep(creativityByStep);
		
		md.setTouchPointsInvestment(touchPointsInvestment);
		
		//Sales data
		md.setSeasonality(seasonality);
		md.setMarketPercentBySegment(marketPercentBySegment);
		md.setAvailabilityByBrandAndStep(availabilityByBrandAndStep);
		//TODO This is a chapuza estelar made-in Nacho.
		md.setDecisionCycle(buyingDecisionCycle*7);
//		md.setDecisionCycle(buyingDecisionCycle);
		
		//Product Usage data
		//TODO Another chapuza estelar made-in Nacho.
		md.setUsageFrequencies(
				ArrayFunctions.scaleIntArray(
						Arrays.copyOf(usageFrequencies,nSegments), 7.0));
//		md.setUsageFrequencies(usageFrequencies);
		md.setUsagePerceptionSpeed(usagePerceptionSpeed);
		md.setUsagePerceptionDecay(usagePerceptionDecay);
		md.setUsageAwarenessImpact(usageAwarenessImpact);
		md.setUsageDiscussionHeatImpact(usageDiscussionHeatImpact);
		md.setUsageDiscussionHeatDecay(usageDiscussionHeatDecay);
		md.setIntangibleAttributes(intangibleAttributes);
		md.setBrandInitialPenetration(brandInitialPenetration);
		md.setBrandAttributes(brandAttributes);
		
		//Online Posting data
		md.setOnlinePostingProbabilities(onlinePostingProbabilities);
		md.setOnlineReadingProbabilities(onlineReadingProbabilities);
		md.setOnlineAwarenessImpact(onlineAwarenessImpact);
		md.setOnlinePerceptionSpeed(onlinePerceptionSpeed);
		md.setOnlinePerceptionDecay(onlinePerceptionDecay);
		md.setOnlineDiscussionHeatDecay(onlineDiscussionHeatDecay);
		md.setOnlineDiscussionHeatImpact(onlineDiscussionHeatImpact);
		
		md.setSimple(simple);
		
		return md;
	}

	public void loadFromModelDefinition(ModelDefinition md) {
		//High level data
		numberOfAgents = md.getNumberOfAgents();
		populationSize = md.getPopulationSize();
		nBrands = md.getNumberOfBrands();
		nAttributes = md.getNumberOfAttributes();
		nSegments = md.getNumberOfSegments();
		nTp = md.getNumberOfTouchPoints();
		nWeeks = md.getNumberOfWeeks();
		stepsForWeek = md.getStepsForWeek();
		emotional = md.getEmotional();
		involved = md.getInvolved();
		initialPerceptionsStdDeviation = md.getInitialPerceptionsStdDeviation();
		
		//Segment data
		segmentSizes = md.getSegmentSizes();
		awarenessDecay = md.getAwarenessDecay();
		initialAwareness = md.getInitialAwareness();
		drivers = md.getDrivers();
		initialPerceptions = md.getInitialPerceptions();
		
		//Diffusion data
		womAwarenessImpact = md.getWomAwarenessImpact();
		womDiscussionHeatImpact = md.getWomDiscussionHeatImpact();
		womDiscussionHeatDecay = md.getWomDiscussionHeatDecay();
		womPerceptionSpeed = md.getWomPerceptionSpeed();
		womPerceptionDecay = md.getWomPerceptionDecay();
		talking = md.getWomTalkingProbability();
		influence = md.getWomSegmentInfluences();
		connectivity = md.getWomSegmentConnectivity();
		womSentimentPositive = md.getWomSentimentPositive();
		womSentimentNegative = md.getWomSentimentNegative();
		typeOfNetwork = md.getTypeOfNetwork();
		
		//TouchPoints data
		touchPointsWeeklyReachMax = md.getTouchPointsWeeklyReachMax(); 
		touchPointsAnnualReachMax = md.getTouchPointsAnnualReachMax();
		touchPointsAnnualReachSpeed = md.getTouchPointsAnnualReachSpeed();
		touchPointsPerceptionPotential = md.getTouchPointsPerceptionPotential();
		touchPointsPerceptionSpeed = md.getTouchPointsPerceptionSpeed();
		touchPointsPerceptionDecay = md.getTouchPointsPerceptionDecay();
		touchPointsAwarenessImpact = md.getTouchPointsAwarenessImpact();
		touchPointsDiscusionHeatImpact = md.getTouchPointsDiscusionHeatImpact();
		touchPointsDiscusionHeatDecay = md.getTouchPointsDiscusionHeatDecay();
		touchPointsGRPMarketingPlan = md.getGRP();
		touchPointsQuality = md.getTouchPointsQuality();
		touchPointsEmphasis = md.getTouchPointsEmphasis();
		creativityByStep = md.getCreativityByStep();
		
		touchPointsInvestment = md.getTouchPointsInvestment();
		if(touchPointsInvestment == null) {
			touchPointsInvestment = TouchPointOwned.getDefaultInvestmentType(nTp);
		}
		
		//Sales data
		seasonality = md.getSeasonality();
		marketPercentBySegment = md.getMarketPercentBySegment();
		availabilityByBrandAndStep = md.getAvailabilityByBrandAndStep();
		//TODO Another chapuza
		buyingDecisionCycle = md.getDecisionCycle()/7;
		
		//Product Usage data
		usageFrequencies = ArrayFunctions.scaleIntArray(
				Arrays.copyOf(md.getUsageFrequencies(), nSegments), 1.0/7.0);
		usagePerceptionSpeed = md.getUsagePerceptionSpeed();
		usagePerceptionDecay = md.getUsagePerceptionDecay();
		usageAwarenessImpact = md.getUsageAwarenessImpact();
		usageDiscussionHeatImpact = md.getUsageDiscussionHeatImpact();
		usageDiscussionHeatDecay = md.getUsageDiscussionHeatDecay();
		intangibleAttributes = md.getIntangibleAttributes();
		brandInitialPenetration = md.getBrandInitialPenetration();
		brandAttributes = md.getBrandAttributes();
		
		//Online Posting data
		onlinePostingProbabilities = md.getOnlinePostingProbabilities();
		onlineReadingProbabilities = md.getOnlineReadingProbabilities();
		onlineAwarenessImpact = md.getOnlineAwarenessImpact();
		onlinePerceptionSpeed = md.getOnlinePerceptionSpeed();
		onlinePerceptionDecay = md.getOnlinePerceptionDecay();
		onlineDiscussionHeatImpact = md.getOnlineDiscussionHeatImpact();
		onlineDiscussionHeatDecay = md.getOnlineDiscussionHeatDecay();
		
		/*
		 * The following values are not contained into MD zio files.
		 * 
		 * By default, statistics are weekly and only record sales.
		 */
		nMC = 30;
		exportAwareness=false;
		exportPerceptions=false;
		exportTouchPointContributions=false;
		exportWomReach=false;
		exportWomVolumen=false;
		exportWomSentiment=false;
		exportWomContributions=false;
		statPeriod=TimePeriod.WEEKLY;
	}
	
	public StatisticsRecordingBean getStatisticRecordingConfiguration() {
		return new StatisticsRecordingBean(exportSales, exportAwareness, 
				exportPerceptions, exportTouchPointContributions, exportWomReach, 
					exportWomVolumen, exportWomSentiment, exportWomContributions, 
						exportTPReach);
	}
	
	public double getRatio() {
		return populationSize / (double) numberOfAgents;
	}

	public int getNumberOfAgents() {
		return numberOfAgents;
	}

	public void setNumberOfAgents(int numberOfAgents) {
		this.numberOfAgents = numberOfAgents;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public int getnBrands() {
		return nBrands;
	}

	public void setnBrands(int nBrands) {
		this.nBrands = nBrands;
	}

	public int getnAttributes() {
		return nAttributes;
	}

	public void setnAttributes(int nAttributes) {
		this.nAttributes = nAttributes;
	}

	public int getnSegments() {
		return nSegments;
	}

	public void setnSegments(int nSegments) {
		this.nSegments = nSegments;
	}

	public int getnTp() {
		return nTp;
	}

	public void setnTp(int nTp) {
		this.nTp = nTp;
	}

	public int getnWeeks() {
		return nWeeks;
	}

	public void setnWeeks(int nWeeks) {
		this.nWeeks = nWeeks;
	}

	public int getStepsForWeek() {
		return stepsForWeek;
	}

	public void setStepsForWeek(int stepsForWeek) {
		this.stepsForWeek = stepsForWeek;
	}

	public double getEmotional() {
		return emotional;
	}

	public void setEmotional(double emotional) {
		this.emotional = emotional;
	}

	public double getInvolved() {
		return involved;
	}

	public void setInvolved(double involved) {
		this.involved = involved;
	}

	public double getInitialPerceptionsStdDeviation() {
		return initialPerceptionsStdDeviation;
	}

	public void setInitialPerceptionsStdDeviation(double initialPerceptionsStdDeviation) {
		this.initialPerceptionsStdDeviation = initialPerceptionsStdDeviation;
	}

	public int getnMC() {
		return nMC;
	}

	public void setnMC(int nMC) {
		this.nMC = nMC;
	}

	public boolean isExportSales() {
		return exportSales;
	}

	public boolean isExportAwareness() {
		return exportAwareness;
	}

	public void setExportAwareness(boolean exportAwareness) {
		this.exportAwareness = exportAwareness;
	}

	public boolean isExportPerceptions() {
		return exportPerceptions;
	}

	public void setExportPerceptions(boolean exportPerceptions) {
		this.exportPerceptions = exportPerceptions;
	}

	public boolean isExportTouchPointContributions() {
		return exportTouchPointContributions;
	}

	public void setExportTouchPointContributions(boolean exportTouchPointContributions) {
		this.exportTouchPointContributions = exportTouchPointContributions;
	}

	public boolean isExportWomReach() {
		return exportWomReach;
	}

	public void setExportWomReach(boolean exportWomReach) {
		this.exportWomReach = exportWomReach;
	}

	public boolean isExportWomVolumen() {
		return exportWomVolumen;
	}

	public void setExportWomVolumen(boolean exportWomVolumen) {
		this.exportWomVolumen = exportWomVolumen;
	}

	public boolean isExportWomSentiment() {
		return exportWomSentiment;
	}

	public void setExportWomSentiment(boolean exportWomSentiment) {
		this.exportWomSentiment = exportWomSentiment;
	}

	public boolean isExportWomContributions() {
		return exportWomContributions;
	}

	public void setExportWomContributions(boolean exportWomContributions) {
		this.exportWomContributions = exportWomContributions;
	}

	public TimePeriod getStatPeriod() {
		return statPeriod;
	}

	public void setStatPeriod(TimePeriod statPeriod) {
		this.statPeriod = statPeriod;
	}

	public double[] getSegmentSizes() {
		return segmentSizes;
	}

	public void setSegmentSizes(double[] segmentSizes) {
		this.segmentSizes = segmentSizes;
	}

	public double[] getAwarenessDecay() {
		return awarenessDecay;
	}

	public void setAwarenessDecay(double[] awarenessDecay) {
		this.awarenessDecay = awarenessDecay;
	}

	public double[][] getInitialAwareness() {
		return initialAwareness;
	}

	public void setInitialAwareness(double[][] initialAwareness) {
		this.initialAwareness = initialAwareness;
	}

	public double[][] getDrivers() {
		return drivers;
	}

	public void setDrivers(double[][] drivers) {
		this.drivers = drivers;
	}

	public double[][][] getInitialPerceptions() {
		return initialPerceptions;
	}

	public void setInitialPerceptions(double[][][] initialPerceptions) {
		this.initialPerceptions = initialPerceptions;
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

	public double[] getTalking() {
		return talking;
	}

	public void setTalking(double[] talking) {
		this.talking = talking;
	}

	public double[][] getInfluence() {
		return influence;
	}

	public void setInfluence(double[][] influence) {
		this.influence = influence;
	}

	public double[] getConnectivity() {
		return connectivity;
	}

	public void setConnectivity(double[] connectivity) {
		this.connectivity = connectivity;
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

	public double[][][][] getTouchPointsGRPMarketingPlan() {
		return touchPointsGRPMarketingPlan;
	}

	public void setTouchPointsGRPMarketingPlan(double[][][][] touchPointsGRPMarketingPlan) {
		this.touchPointsGRPMarketingPlan = touchPointsGRPMarketingPlan;
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

	public InvestmentType[] getTouchPointsInvestment() {
		return touchPointsInvestment;
	}

	public void setTouchPointsInvestment(InvestmentType[] touchPointsInvestment) {
		this.touchPointsInvestment = touchPointsInvestment;
	}
	
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

	public int getBuyingDecisionCycle() {
		return buyingDecisionCycle;
	}

	public void setBuyingDecisionCycle(int buyingDecisionCycle) {
		this.buyingDecisionCycle = buyingDecisionCycle;
	}

	public int[] getUsageFrequencies() {
		return usageFrequencies;
	}

	public void setUsageFrequencies(int[] usageFrequencies) {
		this.usageFrequencies = usageFrequencies;
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

	public String getResponseURL() {
		return responseURL;
	}

	public void setResponseURL(String responseURL) {
		this.responseURL = responseURL;
	}

	public boolean isExportTPReach() {
		return exportTPReach;
	}

	public void setExportTPReach(boolean exportTPReach) {
		this.exportTPReach = exportTPReach;
	}

	public boolean isSimple() {
		return simple;
	}

	public void setSimple(boolean simple) {
		this.simple = simple;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(availabilityByBrandAndStep);
		result = prime * result + Arrays.hashCode(awarenessDecay);
		result = prime * result + Arrays.deepHashCode(brandAttributes);
		result = prime * result + Arrays.hashCode(brandInitialPenetration);
		result = prime * result + buyingDecisionCycle;
		result = prime * result + Arrays.hashCode(connectivity);
		result = prime * result + Arrays.deepHashCode(creativityByStep);
		result = prime * result + Arrays.deepHashCode(drivers);
		long temp;
		temp = Double.doubleToLongBits(emotional);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (exportAwareness ? 1231 : 1237);
		result = prime * result + (exportPerceptions ? 1231 : 1237);
		result = prime * result + (exportSales ? 1231 : 1237);
		result = prime * result + (exportTPReach ? 1231 : 1237);
		result = prime * result + (exportTouchPointContributions ? 1231 : 1237);
		result = prime * result + (exportWomContributions ? 1231 : 1237);
		result = prime * result + (exportWomReach ? 1231 : 1237);
		result = prime * result + (exportWomSentiment ? 1231 : 1237);
		result = prime * result + (exportWomVolumen ? 1231 : 1237);
		result = prime * result + Arrays.deepHashCode(influence);
		result = prime * result + Arrays.deepHashCode(initialAwareness);
		result = prime * result + Arrays.deepHashCode(initialPerceptions);
		temp = Double.doubleToLongBits(initialPerceptionsStdDeviation);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(intangibleAttributes);
		temp = Double.doubleToLongBits(involved);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(marketPercentBySegment);
		result = prime * result + nAttributes;
		result = prime * result + nBrands;
		result = prime * result + nMC;
		result = prime * result + nSegments;
		result = prime * result + nTp;
		result = prime * result + nWeeks;
		result = prime * result + numberOfAgents;
		result = prime * result + Arrays.hashCode(onlineAwarenessImpact);
		result = prime * result + Arrays.hashCode(onlineDiscussionHeatDecay);
		result = prime * result + Arrays.hashCode(onlineDiscussionHeatImpact);
		result = prime * result + Arrays.hashCode(onlinePerceptionDecay);
		result = prime * result + Arrays.hashCode(onlinePerceptionSpeed);
		result = prime * result + Arrays.hashCode(onlinePostingProbabilities);
		result = prime * result + Arrays.hashCode(onlineReadingProbabilities);
		result = prime * result + populationSize;
		result = prime * result + ((responseURL == null) ? 0 : responseURL.hashCode());
		result = prime * result + Arrays.hashCode(seasonality);
		result = prime * result + Arrays.hashCode(segmentSizes);
		result = prime * result + ((statPeriod == null) ? 0 : statPeriod.hashCode());
		result = prime * result + stepsForWeek;
		result = prime * result + Arrays.hashCode(talking);
		result = prime * result + Arrays.deepHashCode(touchPointsAnnualReachMax);
		result = prime * result + Arrays.deepHashCode(touchPointsAnnualReachSpeed);
		result = prime * result + Arrays.deepHashCode(touchPointsAwarenessImpact);
		result = prime * result + Arrays.deepHashCode(touchPointsDiscusionHeatDecay);
		result = prime * result + Arrays.deepHashCode(touchPointsDiscusionHeatImpact);
		result = prime * result + Arrays.deepHashCode(touchPointsEmphasis);
		result = prime * result + Arrays.deepHashCode(touchPointsGRPMarketingPlan);
		result = prime * result + Arrays.hashCode(touchPointsInvestment);
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
		temp = Double.doubleToLongBits(womSentimentNegative);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(womSentimentPositive);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		SimulationConfig other = (SimulationConfig) obj;
		if (!Arrays.deepEquals(availabilityByBrandAndStep, other.availabilityByBrandAndStep))
			return false;
		if (!Arrays.equals(awarenessDecay, other.awarenessDecay))
			return false;
		if (!Arrays.deepEquals(brandAttributes, other.brandAttributes))
			return false;
		if (!Arrays.equals(brandInitialPenetration, other.brandInitialPenetration))
			return false;
		if (buyingDecisionCycle != other.buyingDecisionCycle)
			return false;
		if (!Arrays.equals(connectivity, other.connectivity))
			return false;
		if (!Arrays.deepEquals(creativityByStep, other.creativityByStep))
			return false;
		if (!Arrays.deepEquals(drivers, other.drivers))
			return false;
		if (Double.doubleToLongBits(emotional) != Double.doubleToLongBits(other.emotional))
			return false;
		if (exportAwareness != other.exportAwareness)
			return false;
		if (exportPerceptions != other.exportPerceptions)
			return false;
		if (exportSales != other.exportSales)
			return false;
		if (exportTPReach != other.exportTPReach)
			return false;
		if (exportTouchPointContributions != other.exportTouchPointContributions)
			return false;
		if (exportWomContributions != other.exportWomContributions)
			return false;
		if (exportWomReach != other.exportWomReach)
			return false;
		if (exportWomSentiment != other.exportWomSentiment)
			return false;
		if (exportWomVolumen != other.exportWomVolumen)
			return false;
		if (!Arrays.deepEquals(influence, other.influence))
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
		if (nAttributes != other.nAttributes)
			return false;
		if (nBrands != other.nBrands)
			return false;
		if (nMC != other.nMC)
			return false;
		if (nSegments != other.nSegments)
			return false;
		if (nTp != other.nTp)
			return false;
		if (nWeeks != other.nWeeks)
			return false;
		if (numberOfAgents != other.numberOfAgents)
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
		if (responseURL == null) {
			if (other.responseURL != null)
				return false;
		} else if (!responseURL.equals(other.responseURL))
			return false;
		if (!Arrays.equals(seasonality, other.seasonality))
			return false;
		if (!Arrays.equals(segmentSizes, other.segmentSizes))
			return false;
		if (statPeriod != other.statPeriod)
			return false;
		if (stepsForWeek != other.stepsForWeek)
			return false;
		if (!Arrays.equals(talking, other.talking))
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
		if (!Arrays.equals(touchPointsInvestment, other.touchPointsInvestment))
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
		if (Double.doubleToLongBits(womSentimentNegative) != Double.doubleToLongBits(other.womSentimentNegative))
			return false;
		if (Double.doubleToLongBits(womSentimentPositive) != Double.doubleToLongBits(other.womSentimentPositive))
			return false;
		return true;
	}
}
