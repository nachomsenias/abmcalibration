package es.ugr.sci2s.soccer.util.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import com.google.gson.Gson;

import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.SimulationConfig;
import util.StringBean;
import util.io.CSVFileUtils;

public class ConfigGenerator {
	
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
		int[][][] historyValues = new int [segments][][];
		
		for (int s = 0; s<segments; s++) {
			int[][] history = CSVFileUtils.readRawHistoryFromCSV(args[s+1]);
			historyValues[s] = history;
		}
//		historyValues[0]=historyValues1;
//		historyValues[1]=historyValues2;
		
		CalibrationConfig calibration = new CalibrationConfig();
		
		givenConfig.setnMC(15);
		givenConfig.setNumberOfAgents(10000);
		
		calibration.setSimConfig(givenConfig);
		
//		calibration.setTargetSales(historyValues);
		calibration.setTargetSalesBySegment(historyValues);
//		calibration.setHistorySalesPeriod("WEEKLY");
		calibration.setHistorySalesPeriod("MONTHLY");
		calibration.setTotalSalesWeight(0.6);
		calibration.setSalesWeight(1.0);
		
		calibration.setId(12);
		
//		StringBean[] beans = new StringBean[2+(givenConfig.getnTp()*4)+givenConfig.getnSegments()
//		                                    +(givenConfig.getnSegments()
//		                                    		*givenConfig.getnBrands()
//		                                    		*givenConfig.getnAttributes())
//		                                    ];
		StringBean[] beans = new StringBean[2+(givenConfig.getnTp()*3)//+givenConfig.getnSegments()
									        +(givenConfig.getnSegments()
									        		*givenConfig.getnBrands()
									        		*givenConfig.getnAttributes())
									        ];
		
		beans[0] = new StringBean("Involved","0.0,1.0,0.01");
		beans[1] = new StringBean("Emotional","0.0,1.0,0.01");
		
		int count = 2;
		for (int tp=0; tp<givenConfig.getnTp(); tp++) {
			beans[count] = new StringBean("TouchPointPerceptionDecay_"+tp,"0.01,0.6,0.01");
			count++;
			beans[count] = new StringBean("TouchPointPerceptionPotential_"+tp,"0.25,2.0,0.01");
			count++;
			beans[count] = new StringBean("TouchPointPerceptionSpeed_"+tp,"0.05,1.0,0.01");
			count++;
//			beans[count] = new StringBean("TouchPointAwarenessImpact_"+tp,"0.01,0.3,0.01");
//			count++;
		}
		
//		for (int seg = 0; seg<givenConfig.getnSegments(); seg++) {
//			beans[count] = new StringBean("SegmentAwarenessDecay_"+seg,"0.01,0.1,0.01");
//			count++;
//		}
		
		double[][][] perceptions = givenConfig.getInitialPerceptions();
		
		DecimalFormat dc = new DecimalFormat("#.##");
		
		for (int seg = 0; seg<givenConfig.getnSegments(); seg++) {
			for (int brand = 0; brand<givenConfig.getnBrands(); brand++) {
				for (int att = 0; att<givenConfig.getnAttributes(); att++) {
					beans[count] = new StringBean("SegmentInitialPerceptions_"
							+seg+"_"+brand+"_"+att,
								dc.format(notZero(perceptions[seg][brand][att]-0.5)).replace(',', '.')+","+
										dc.format(perceptions[seg][brand][att]+0.5).replace(',', '.')+",0.01");
					count++;
				}
			}
		}
		
		calibration.setCalibrationModelParameters(beans);
		
		String output = gson.toJson(calibration, CalibrationConfig.class);
		
		//Export to File
//		FileWriter fw = new FileWriter(args[3]);
		FileWriter fw = new FileWriter(args[segments+1]);
		fw.write(output);
		fw.close();
	}
	
	private static double notZero(double value) {
		if(value<0.0) return 0.0;
		else return value;
	}
}
