package es.ugr.sci2s.soccer.util.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;

import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.SimulationConfig;
import util.StringBean;
import util.io.CSVFileUtils;

public class AwarenessConfigGenerator {
	public static void main(String args[]) throws IOException {
		String json = args[0];
		
//		String history1 = args[1];
//		String history2 = args[2];
		
		//Read the JSON file
		BufferedReader br = new BufferedReader(new FileReader(json));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String config = buffer.toString();
		
		//Get ModelDefinition
		Gson gson = new Gson();
		
		SimulationConfig givenConfig = gson.fromJson(config, SimulationConfig.class);
		
//		int[][] historyValues1 = CSVFileUtils.readRawHistoryFromCSV(history1);
//		int[][] historyValues2 = CSVFileUtils.readRawHistoryFromCSV(history2);
		
		int segments = givenConfig.getnSegments();
		double[][][] historyValues = new double [segments][][];
		
		for (int s = 0; s<segments; s++) {
			double [][] history = CSVFileUtils.readDoubleTwoDimArrayFromCSV(args[s+1]);
			historyValues[s] = history;
		}
		
		int brands = givenConfig.getnBrands();
		double[][][] historyValuesByBrand = new double [brands][segments][];
		
		for (int b = 0; b<brands; b++) {
			for (int s = 0; s<segments; s++) {
				historyValuesByBrand[b][s]=historyValues[s][b];
			}
		}
		
		
//		historyValues[0]=historyValues1;
//		historyValues[1]=historyValues2;
		
		CalibrationConfig calibration = new CalibrationConfig();
		
		givenConfig.setnMC(15);
		givenConfig.setNumberOfAgents(1000);
		
		calibration.setSimConfig(givenConfig);
		
//		calibration.setTargetSales(historyValues);
		calibration.setTargetAwarenessBySegment(historyValuesByBrand);
//		calibration.setHistorySalesPeriod("WEEKLY");
		calibration.setHistorySalesPeriod("MONTHLY");
		calibration.setTargetAwarenessPeriod("MONTHLY");
		calibration.setTotalSalesWeight(0.6);
//		calibration.setSalesWeight(1.0);
		calibration.setAwarenessWeight(1.0);
		
		calibration.setId(12);
		
//		StringBean[] beans = new StringBean[2+(givenConfig.getnTp()*4)+givenConfig.getnSegments()
//		                                    +(givenConfig.getnSegments()
//		                                    		*givenConfig.getnBrands()
//		                                    		*givenConfig.getnAttributes())
//		                                    ];
		StringBean[] beans = new StringBean[(givenConfig.getnTp()*3)//+givenConfig.getnSegments()
									        +(givenConfig.getnSegments()*6)
									        ];
	
		
		int count = 0;
		for (int tp=0; tp<givenConfig.getnTp(); tp++) {
			beans[count] = new StringBean("TouchPointAwarenessImpact_"+tp,"0.001,0.1,0.001");
			count++;
			beans[count] = new StringBean("TouchPointDiscusionHeatImpact_"+tp,"0.0,1.0,0.1");
			count++;
			beans[count] = new StringBean("TouchPointDiscusionHeatDecay_"+tp,"0.0,1.0,0.1");
			count++;
		}
		
		for (int seg = 0; seg<givenConfig.getnSegments(); seg++) {
			beans[count] = new StringBean("SegmentAwarenessDecay_"+seg,"0.001,0.1,0.001");
			count++;
			beans[count] = new StringBean("SegmentTalking_"+seg,"0.0,0.1,0.001");
			count++;
			beans[count] = new StringBean("WOMAwarenessImpact_"+seg,"0.0,0.1,0.001");
			count++;
			beans[count] = new StringBean("WOMDiscusionHeatDecay_"+seg,"0.0,1.0,0.1");
			count++;
			beans[count] = new StringBean("WOMDiscusionHeatImpact_"+seg,"0.0,1.0,0.1");
			count++;
			beans[count] = new StringBean("SocialNetworkSegmentConnectivity_"+seg,"0.01,0.5,0.01");
			count++;
		}

		
//		DecimalFormat dc = new DecimalFormat("#.##");
//		
//		for (int seg = 0; seg<givenConfig.getnSegments(); seg++) {
//			
//		}
		
		calibration.setCalibrationModelParameters(beans);
		
		String output = gson.toJson(calibration, CalibrationConfig.class);
		
		//Export to File
//		FileWriter fw = new FileWriter(args[3]);
		FileWriter fw = new FileWriter(args[segments+1]);
		fw.write(output);
		fw.close();
	}
}
