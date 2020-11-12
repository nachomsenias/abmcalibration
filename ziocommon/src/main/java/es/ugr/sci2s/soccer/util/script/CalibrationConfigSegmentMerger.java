package es.ugr.sci2s.soccer.util.script;

import java.io.IOException;

import com.google.gson.Gson;
import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.SimulationConfig;

import model.socialnetwork.SocialNetwork.NetworkType;
import util.io.CSVFileUtils;

public class CalibrationConfigSegmentMerger {

	private static void mergeSalesHistory(CalibrationConfig cc) {
		SimulationConfig sc = cc.getSimConfig();
		
		int segments = sc.getnSegments();
		int brands = sc.getnBrands();
		
		int [][][] targetSalesBySegment = 
				cc.getTargetSalesBySegment();
		
		if(targetSalesBySegment!=null) {
			
			int weeks = targetSalesBySegment[0][0].length;
			
			int [][] history = new int [brands][weeks];
			
			for (int b=0; b<brands; b++) {
				for (int s=0; s<segments; s++) {
					for (int w=0; w<weeks; w++) {
						history[b][w]+=targetSalesBySegment[b][s][w];
					}
				}
			}
			
			cc.setTargetSalesBySegment(null);
			cc.setTargetSales(history);
		}
	}
	
	private static void mergeAwarenessHistory(CalibrationConfig cc) {
		SimulationConfig sc = cc.getSimConfig();
		
		int segments = sc.getnSegments();
		
		double [] segmentSizes = sc.getSegmentSizes();
		
		int brands = sc.getnBrands();
		double [][][] targetAwarenessBySegment = 
				cc.getTargetAwarenessBySegment();
		
		if(targetAwarenessBySegment!=null) {
			
			int steps = targetAwarenessBySegment[0][0].length;
			
			double [][] history = new double [brands][steps];
			
			for (int b=0; b<brands; b++) {
				for (int s=0; s<segments; s++) {
					for (int w=0; w<steps; w++) {
						history[b][w]+=(targetAwarenessBySegment[b][s][w]*segmentSizes[s]);
					}
				}
			}
			
			cc.setTargetAwarenessBySegment(null);
			cc.setTargetAwareness(history);
		}
	}
	
	private static void mergeSegmentData(SimulationConfig sc) {
		
		int segments = sc.getnSegments();
		int brands = sc.getnBrands();
		
		double [] segmentSizes = sc.getSegmentSizes();

		// segmentSizes;
		double[] newSizes = {1.0};
		sc.setSegmentSizes(newSizes);
		
		// awarenessDecay
		double[] newDecay = {sc.getAwarenessDecay()[0]};
		sc.setAwarenessDecay(newDecay);
		
		// initialAwareness
		double[][] oldInitialAwareness = sc.getInitialAwareness();
		double[][] newInitialAwareness = new double[brands][1];
		
		for (int b=0; b<brands; b++) {
			for (int s = 0; s<segments; s++) {
				newInitialAwareness[b][0] = oldInitialAwareness[b][0]*segmentSizes[s];  
			}
		}
		sc.setInitialAwareness(newInitialAwareness);
		
		// drivers
		double[][] oldDrivers = sc.getDrivers();
		double[][] drivers = new double [1][];
		drivers[0] = oldDrivers[0];
		sc.setDrivers(drivers);
		
		// initialPerceptions
		double[][][] oldPerceptions = sc.getInitialPerceptions();
		double[][][] newInitialPerceptions= new double [1][][];
		newInitialPerceptions[0] = oldPerceptions[0]; 
		sc.setInitialPerceptions(newInitialPerceptions);		
	}
	
	private static void mergeDiffusionData(SimulationConfig sc) {
		// womAwarenessImpact
		double[] newAwarenessImpact = {sc.getWomAwarenessImpact()[0]};
		sc.setWomAwarenessImpact(newAwarenessImpact);
		
		// womDiscussionHeatImpact
		double[] newDiscussionHeatImpact = {sc.getWomDiscussionHeatImpact()[0]};
		sc.setWomDiscussionHeatImpact(newDiscussionHeatImpact);
		
		// womDiscussionHeatDecay
		double[] newDiscussionHeatDecay = {sc.getWomDiscussionHeatDecay()[0]};
		sc.setWomDiscussionHeatDecay(newDiscussionHeatDecay);
		
		// womPerceptionSpeed
		double[] newPerceptionSpeed = {sc.getWomPerceptionSpeed()[0]};
		sc.setWomPerceptionSpeed(newPerceptionSpeed);
		
		// womPerceptionDecay
		double[] newPerceptionDecay = {sc.getWomPerceptionDecay()[0]};
		sc.setWomPerceptionDecay(newPerceptionDecay);
		
		// talking
		double[] newTalking = {sc.getTalking()[0]};
		sc.setTalking(newTalking);
		
		// influence :: Fixed to 100%
		double[][] newInfluences = {{1.0},{1.0}};
		sc.setInfluence(newInfluences);
				
		// connectivity 
		double[] newConnectivities = {sc.getConnectivity()[0]};
		sc.setConnectivity(newConnectivities);

		// typeOfNetwork :: Fixed to Scale free
		sc.setTypeOfNetwork(NetworkType.SCALE_FREE_NETWORK);
	}
	
	private static void mergeTouchpointData(SimulationConfig sc) {
		
		int touchpoints = sc.getnTp();
		
		// touchPointsWeeklyReachMax
		double[][] tpWReach = sc.getTouchPointsWeeklyReachMax();
		double[][] newWReach = new double [touchpoints][1];
		
		// touchPointsAnnualReachMax
		double[][] tpAReach = sc.getTouchPointsAnnualReachMax();
		double[][] newAReach = new double [touchpoints][1];
		
		// touchPointsAnnualReachSpeed
		double[][] tpAReachSpeed = sc.getTouchPointsAnnualReachSpeed();
		double[][] newAReachSpeed = new double [touchpoints][1];
		
		// touchPointsPerceptionPotential
		double[][] tpPotential = sc.getTouchPointsPerceptionPotential();
		double[][] newPotential = new double [touchpoints][1];
		
		// touchPointsPerceptionSpeed
		double[][] tpSpeed = sc.getTouchPointsPerceptionSpeed();
		double[][] newSpeed = new double [touchpoints][1];
		
		// touchPointsPerceptionDecay
		double[][] tpDecay = sc.getTouchPointsPerceptionDecay();
		double[][] newDecay = new double [touchpoints][1];
		
		// touchPointsAwarenessImpact
		double[][] tpAImpact = sc.getTouchPointsAwarenessImpact();
		double[][] newAImpact = new double [touchpoints][1];
		
		// touchPointsDiscusionHeatImpact
		double[][] tpDHImpact = sc.getTouchPointsDiscusionHeatImpact();
		double[][] newDHImpact = new double [touchpoints][1];
		
		// touchPointsDiscusionHeatDecay
		double[][] tpDHDecay = sc.getTouchPointsDiscusionHeatDecay();
		double[][] newDHDecay = new double [touchpoints][1];
		
		// touchPointsGRPMarketingPlan
		double[][][][] grp = sc.getTouchPointsGRPMarketingPlan();
		int brands = sc.getnBrands();
		int weeks = sc.getnWeeks();
		int segments = sc.getnSegments();
		
		double[][][][] newGrp = new double[touchpoints][brands][1][weeks];
		
		
		for (int t=0; t<touchpoints; t++) {
			newWReach[t][0] = tpWReach[t][0];
			newAReach[t][0] = tpAReach[t][0];
			newAReachSpeed[t][0] = tpAReachSpeed[t][0];
			
			newPotential[t][0] = tpPotential[t][0];
			newSpeed[t][0] = tpSpeed[t][0];
			newDecay[t][0] = tpDecay[t][0];
			
			newAImpact[t][0] = tpAImpact[t][0];
			newDHImpact[t][0] = tpDHImpact[t][0];
			newDHDecay[t][0] = tpDHDecay[t][0];
			
			for (int b= 0; b<brands; b++) {
				for (int s= 0; s<segments; s++) {
					for (int w= 0; w<weeks; w++) {
						newGrp[t][b][0][w] += grp[t][b][s][w]; 
					}
				}
			}
		}
		
		sc.setTouchPointsWeeklyReachMax(newWReach);
		sc.setTouchPointsAnnualReachMax(newAReach);
		sc.setTouchPointsAnnualReachSpeed(newAReachSpeed);
		
		sc.setTouchPointsPerceptionPotential(newPotential);
		sc.setTouchPointsPerceptionSpeed(newSpeed);
		sc.setTouchPointsPerceptionDecay(newDecay);
		
		sc.setTouchPointsAwarenessImpact(newAImpact);
		sc.setTouchPointsDiscusionHeatImpact(newDHImpact);
		sc.setTouchPointsDiscusionHeatDecay(newDHDecay);
		
		sc.setTouchPointsGRPMarketingPlan(newGrp);
	}
	
	private static void mergeSalesData(SimulationConfig sc) {
		double[] newMarketPercepnt = {1.0};
		sc.setMarketPercentBySegment(newMarketPercepnt);
	}
	
	public static void main(String[] args) {
		if(args.length!=2) {
			System.out.println("Usage: input_json output_json");
			System.exit(1);
		}
		
		try {
			String config = CSVFileUtils.readFile(args[0]);
		
			Gson gson = new Gson();
			
			//Read configuration from JSON
			CalibrationConfig calibrationConfig = gson.fromJson(config, CalibrationConfig.class);

			mergeSalesHistory(calibrationConfig);
			mergeAwarenessHistory(calibrationConfig);

			SimulationConfig sc = calibrationConfig.getSimConfig();
			
			mergeSegmentData(sc);
			mergeDiffusionData(sc);
			mergeTouchpointData(sc);
			mergeSalesData(sc);
			
			sc.setnSegments(1);
			
			
			CSVFileUtils.writeFile(args[1], gson.toJson(calibrationConfig, CalibrationConfig.class));
		
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
