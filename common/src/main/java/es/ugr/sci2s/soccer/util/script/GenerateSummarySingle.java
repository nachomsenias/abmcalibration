package es.ugr.sci2s.soccer.util.script;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import es.ugr.sci2s.soccer.beans.CalibrationResponse;
import org.apache.commons.math.stat.StatUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.opencsv.CSVWriter;

import util.io.CSVFileUtils;

public class GenerateSummarySingle {

	public static void main(String[] args) throws JsonSyntaxException, IOException {
		String folder = args[0];
		String target = folder+"summary.csv";
		
		
		File directory = new File(folder);
		if(directory.isDirectory()) {
			
			File[] files = directory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.contains("resulting") 
							&& name.endsWith(".json");
				}
			});
			
			String[] headers = new String[files.length+1];
			for (int i=0; i<files.length; i++) {
				headers[i] = "It "+i;
			}
			headers[files.length]= "Avg";
			
			String[] values = new String[files.length+1];
			
			double[] doubleValues = new double[files.length];
			
			for (File result : files) {
				String path = result.getAbsolutePath();
				int index = Integer.parseInt(path.split("_it")[1].split(".js")[0]);
				
				Gson gson = new Gson();
				
				CalibrationResponse calibrationResult = gson.fromJson(CSVFileUtils.readFile(path),
						CalibrationResponse.class);
				if(calibrationResult==null) {
					System.out.println("Warning :: Cant read file: ");
					System.out.println(path);
				} else {
					values[index] = calibrationResult.getFitnessPercentaje();
					doubleValues[index]=Double.parseDouble(calibrationResult.getFitnessPercentaje());
				}
			}
			values[files.length]= String.valueOf(StatUtils.mean(doubleValues));
			
			CSVWriter csw = new CSVWriter(new FileWriter(target),';', CSVWriter.NO_QUOTE_CHARACTER);
			csw.writeNext(headers);
			csw.writeNext(values);
			csw.close();
			
			target = folder+"summary-it.csv";
			csw = new CSVWriter(new FileWriter(target),';');
			
			for (int mcit=0; mcit<files.length; mcit++) {
				String[] line = new String[2];
				line[0] = String.valueOf(mcit);
				line[1] = values[mcit];
				
				csw.writeNext(line);
			}
			csw.close();
			
			
		} else {
			System.out.println("Intput path is not a directory!");
			System.exit(1);
		}

	}

}
