package es.ugr.sci2s.soccer.util.script;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.ugr.sci2s.soccer.beans.CalibrationResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;

import com.google.gson.Gson;

import util.io.CSVFileUtils;

public class CheckResults {
	
	public static void main(String[] args) throws IOException {
		String root = args[0];
		
//		String method = "IPOP-CMA-ES-HC-real";
		String method = "IPOP-CMA-ES-OPT-real";
		
//		String method = "CMA-ES-OPT-real";
//		String method = "LSHADE-real";
		int iterations = 20;
		
		Gson gson = new Gson();
		
		for (int p=0; p<GenerateReports.problems.length; p++) {
			String folder = root+GenerateReports.problems[p]+"/"+method;
			List<Double> values = new ArrayList<Double>();
			
			File directory = new File(folder);
			if(directory.isDirectory()) {
				
				File[] files = directory.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.contains("resulting") 
								&& name.endsWith(".json");
					}
				});
				
				for (File result : files) {
					String path = result.getAbsolutePath();
					int index = Integer.parseInt(path.split("_it")[1].split(".js")[0]);
					
					CalibrationResponse calibrationResult = gson.fromJson(CSVFileUtils.readFile(path),
							CalibrationResponse.class);
					if(calibrationResult==null) {
						System.out.println("Warning :: Cant read file: ");
						System.out.println(path);
					} else if(index<iterations) {
						values.add(calibrationResult.getSalesScoreDetails().finalScore);
					}
				}
				Double[] empty = {};
				Double[] arrayValues = values.toArray(empty);
				double[] basic = ArrayUtils.toPrimitive(arrayValues);
								
				System.out.println("Problem: "+GenerateReports.labels[p]);
				System.out.println("Average: "+StatUtils.mean(basic));
				
				if(values.size()!=iterations) {
					System.out.println("Mismatch warning! "+values.size()+" results found.");
				}
				
				System.out.println("================================");
			}
		}
	}

}
