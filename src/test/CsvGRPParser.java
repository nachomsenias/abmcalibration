package test;

import java.io.IOException;

import util.io.CSVFileUtils;


public class CsvGRPParser {

	public static void main(String[] args) {
		
//		int[] increments = {15,30,50,-15,-30,-20,100,-192,23890,415};
		int[] increments = {35,65200,1678};
		
		for (int d:increments) {
			String dir = "/home/ECSC/imoya/anotherabm/"
					+ "abm4marketing/experiments/tpvalues/1seg"+d;
//					+ "abm4marketing/experiments/tpvalues/1seg";
//			double[] proportions = {0.64*(1.0+(d/100.0)), 0.36*(1.0+(d/100.0))};
			double[] proportions = {0.64*(1.0+(d/1000.0)), 0.36*(1.0+(d/1000.0))};
//			double[] proportions = {0.64, 0.36};
			
			try {
				CSVFileUtils.parseGRP(dir, 2, proportions, 26);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
