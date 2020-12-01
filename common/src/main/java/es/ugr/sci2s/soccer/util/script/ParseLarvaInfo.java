package es.ugr.sci2s.soccer.util.script;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class ParseLarvaInfo {

	public static void main(String[] args) throws IOException {
		String folder = args[0];
		String target = folder+"larva-overtime.csv";
		
		
		File directory = new File(folder);
		if(directory.isDirectory()) {
			
			File[] files = directory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith("_larvaeLog.csv");
				}
			});
			
			/*
			 *  In other works applying the CRO-SL there are some graphs which 
			 *  show the percentage of times that a given substrate provides the 
			 *  best solution in a generation, and also the number of larvae 
			 *  introduced in the reef per substrate.
			 *  
			 *  Histograma best: step sustrato mc
			 *  
			 */
			
					
			int mcs= 20;
			//At max
			int steps = 140;
			
			double[][] s0Values = new double[steps][mcs];
			double[][] s1Values = new double[steps][mcs];
			double[][] s2Values = new double[steps][mcs];
			double[][] s3Values = new double[steps][mcs];
			
			double[][] p0Values = new double[steps][mcs];
			double[][] p1Values = new double[steps][mcs];
			double[][] p2Values = new double[steps][mcs];
			double[][] p3Values = new double[steps][mcs];
			
			// Best	S0	S1	S2	S3	P0	P1	P2	P3 			
			for (File log : files) {
				String path = log.getAbsolutePath();
				int index = Integer.parseInt(path.split("_it")[1].split("_larvaeLog.csv")[0]);
				
				CSVReader reader = new CSVReader(new FileReader(log),';');
				
				List<String[]> lines = reader.readAll();
				int i=0;
				while(i<lines.size()-1 && i<steps) {
//				for (int i=0; i<lines.size()-1; i++) {
					String[] line = lines.get(i+1);
					double s0 = Double.parseDouble(line[1]);
					double s1 = Double.parseDouble(line[2]);
					double s2 = Double.parseDouble(line[3]);
					double s3 = Double.parseDouble(line[4]);
					
					double p0 = Double.parseDouble(line[5]);
					double p1 = Double.parseDouble(line[6]);
					double p2 = Double.parseDouble(line[7]);
					double p3 = Double.parseDouble(line[8]);
					
					
					
					s0Values[i][index] = s0;
					s1Values[i][index] = s1;
					s2Values[i][index] = s2;
					s3Values[i][index] = s3;
					
					p0Values[i][index] = p0;
					p1Values[i][index] = p1;
					p2Values[i][index] = p2;
					p3Values[i][index] = p3;
					
					i++;
				}
				
				reader.close();
			}

			CSVWriter csw = new CSVWriter(new FileWriter(new File(target)), ';',
					CSVWriter.NO_QUOTE_CHARACTER);
			
			String[] header = {"Step",
					"S0","S0-std","S1","S1-std","S2","S2-std","S3","S3-std",
					"P0","P0-std","P1","P1-std","P2","P2-std","P3","P3-std"};
			csw.writeNext(header);
			
			for (int i=0; i<steps; i++) {
				
				StandardDeviation sd = new StandardDeviation();
				
				String[] line = {String.valueOf(i),
						String.valueOf(StatUtils.mean(s0Values[i])),
						String.valueOf(sd.evaluate(s0Values[i])),
						String.valueOf(StatUtils.mean(s1Values[i])),
						String.valueOf(sd.evaluate(s1Values[i])),
						String.valueOf(StatUtils.mean(s2Values[i])),
						String.valueOf(sd.evaluate(s2Values[i])),
						String.valueOf(StatUtils.mean(s3Values[i])),
						String.valueOf(sd.evaluate(s3Values[i])),
						String.valueOf(StatUtils.mean(p0Values[i])),
						String.valueOf(sd.evaluate(p0Values[i])),
						String.valueOf(StatUtils.mean(p1Values[i])),
						String.valueOf(sd.evaluate(p1Values[i])),
						String.valueOf(StatUtils.mean(p2Values[i])),
						String.valueOf(sd.evaluate(p2Values[i])),
						String.valueOf(StatUtils.mean(p3Values[i])),
						String.valueOf(sd.evaluate(p3Values[i]))
						};
				csw.writeNext(line);
			}
			
			csw.close();
			
			
			// Second file :: Boxplot
			target = folder+"larva-boxplot.csv";
						
			csw = new CSVWriter(new FileWriter(new File(target)), ';',
					CSVWriter.NO_QUOTE_CHARACTER);
			
//			header = new String[mcs+1];
//			
//			header[0] = "Sustrate";
//			for (int i=0; i<mcs; i++) {
//				header[i+1] = "MC-"+i;
//			}
//			
//			csw.writeNext(header);
//			
//			// P0
//			String[] line = new String[mcs+1];
//			line[0] = "P0";
//			
//			for (int i=0; i<mcs; i++) {
//				line[i+1] = String.valueOf(p0Values[steps-1][i]);
//			}
//			csw.writeNext(line);
//			
//			// P1
//			line = new String[mcs+1];
//			line[0] = "P1";
//			
//			for (int i=0; i<mcs; i++) {
//				line[i+1] = String.valueOf(p1Values[steps-1][i]);
//			}
//			csw.writeNext(line);
//			
//			// P2
//			line = new String[mcs+1];
//			line[0] = "P2";
//			
//			for (int i=0; i<mcs; i++) {
//				line[i+1] = String.valueOf(p2Values[steps-1][i]);
//			}
//			csw.writeNext(line);
//			
//			// P3
//			line = new String[mcs+1];
//			line[0] = "P3";
//			
//			for (int i=0; i<mcs; i++) {
//				line[i+1] = String.valueOf(p3Values[steps-1][i]);
//			}
//			csw.writeNext(line);
//			
//			// END
//			csw.close();
			
			
			
			header = new String[mcs];
			
			for (int i=0; i<mcs; i++) {
				header[i] = "MC-"+i;
			}
			
			csw.writeNext(header);
			
			// P0
			String[] line = new String[mcs];
			
			for (int i=0; i<mcs; i++) {
				line[i] = String.valueOf(p0Values[steps-1][i]);
			}
			csw.writeNext(line);
			
			// P1
			line = new String[mcs];
			
			for (int i=0; i<mcs; i++) {
				line[i] = String.valueOf(p1Values[steps-1][i]);
			}
			csw.writeNext(line);
			
			// P2
			line = new String[mcs];
			
			for (int i=0; i<mcs; i++) {
				line[i] = String.valueOf(p2Values[steps-1][i]);
			}
			csw.writeNext(line);
			
			// P3
			line = new String[mcs];
			
			for (int i=0; i<mcs; i++) {
				line[i] = String.valueOf(p3Values[steps-1][i]);
			}
			csw.writeNext(line);
			
			// END
			csw.close();
			
		} else {
			System.out.println("Intput path is not a directory!");
			System.exit(1);
		}
	}

}
