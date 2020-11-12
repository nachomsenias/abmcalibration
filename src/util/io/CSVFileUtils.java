package util.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.opencsv.CSVReader;

public class CSVFileUtils {
	
	public static final char DEFAULT_CSV_SEPARATOR=';';
	
	/**
	 * Imports data into a JTable object. In order to preserve compatibility
	 * with the rest of components, it is distinguished whenever the first
	 * row is editable or not.
	 * @param table - JTable where the values is going to be imported.
	 * @param firstRowEditable - The first row that will receive values.
	 * @param additionalColumns - The columns avoided at the left cells
	 * (used for row labeling).
	 * @throws IOException - Because the method interacts with files, it
	 * is always possible that something fails.
	 */
	public final static File loadFromCSV(
			File path, JTable table, int firstRowEditable, int additionalColumns
			) throws IOException {
		
		JFileChooser fc = new JFileChooser();
		// Set path
		if(path != null) {
			fc.setCurrentDirectory(path);
		}
		// Set filter
		FileNameExtensionFilter filter = new FileNameExtensionFilter("csv", "CSV");
		fc.setFileFilter(filter);
		fc.showOpenDialog(null);
		File selected=fc.getSelectedFile();
		
		if(selected!=null) {
			//Read CSV values
			CSVReader reader = new CSVReader(new FileReader(selected), '\t');
			List<String[]> csvValues=reader.readAll();
			Iterator<String []> csvIterator = csvValues.iterator();
			int rows=csvValues.size();
			
			if(csvIterator.hasNext()) {
				/*
				 * A resize call is not supposed to be needed, because
				 * at this level there is not enough information to do
				 * it in a generic way.
				 */
				int i=firstRowEditable;
				int numColumns=table.getColumnCount();
				
				while(csvIterator.hasNext() && i<=rows) {
					String[] csvLine = csvIterator.next();
					if(csvLine.length==numColumns-additionalColumns) {
						for (int j=0; j<Math.min(numColumns, csvLine.length); j++) {
							table.setValueAt(csvLine[j], i, j+additionalColumns);
						}
						i++;
					}
				}
			}			
			reader.close();
		}
		
		return fc.getCurrentDirectory();
	}
	
	public static int[][] readRawHistoryFromCSV(
			String historyFile) throws IOException {
		
		return readScaledHistoryFromCSV(historyFile, 1.0);
	}

	public final static int[][] readScaledHistoryFromCSV(
			String historyFile, double ratio) throws IOException  {
		
		int[][] historyStatistics = null;

		// Read CSV values
		CSVReader reader = new CSVReader(
			new FileReader(new File(historyFile)), ','
		);
		
		try {
			
			List<String[]> csvValues = reader.readAll();
			Iterator<String []> csvIterator = csvValues.iterator();
			
			int rows = csvValues.size();
			int cols = csvValues.get(0).length;
			
			if (rows == 0 || cols == 0 || csvValues.isEmpty()) {
				throw new IllegalArgumentException("Bad CSV file: " + historyFile);
			}
			
			historyStatistics = new int[rows][cols];

			/*
			 * A resize call is not supposed to be needed, because
			 * at this level there is not enough information to do
			 * it in a generic way.
			 */
			for (int i = 0; csvIterator.hasNext(); i++) {
				String[] csvLine = csvIterator.next();
				assert(csvLine.length == cols);
				double leftOvers = 0;
				for (int j = 0; j < cols; j++) {
					if (ratio != 1.0) {
						double rowSales = Integer.valueOf(csvLine[j]) + leftOvers;
						int salesEvents = (int) (rowSales / ratio);
						historyStatistics[i][j] = salesEvents;
						leftOvers = rowSales - (ratio * salesEvents);
					} else {
						historyStatistics[i][j] = Integer.valueOf(csvLine[j]);
					}
				}
			}
			
		} finally {
			reader.close();
		}
		return historyStatistics;
	}
	
	public final static void writeHistoryToCSV(
			String fileName, int[][] history
			) throws IOException {	

		BufferedWriter fileCSV = 
			new BufferedWriter(new FileWriter(fileName, false));
		
		String line;
		for(int i = 0; i < history.length; i++) {
			line = String.valueOf(history[i][0]);
			for(int j = 1; j < history[i].length; j++) {
				line += "," + String.valueOf(history[i][j]);
			}
			fileCSV.write(line);
			fileCSV.newLine();
		}
		fileCSV.close();
	}
	
	public final static void writeDoubleTwoDimArrayToCSV(
		String fileName, double[][] array, char separator
	) throws IOException {
		
		BufferedWriter fileCSV = 
				new BufferedWriter(new FileWriter(fileName, false));
		
		String line;
		for(int i = 0; i < array.length; i++) {
			line = String.valueOf(array[i][0]);
			for(int j = 1; j < array[i].length; j++) {
				line += separator + String.valueOf(array[i][j]);
			}
			fileCSV.write(line);
			fileCSV.newLine();
		}
		fileCSV.close();	
	}
	
	public final static void writeDoubleThreeDimArrayToCSV(
		String fileName, double[][][] array, char separator
	) throws IOException {
		
		BufferedWriter fileCSV = 
				new BufferedWriter(new FileWriter(fileName, false));
		
		String line;
		for(int i = 0; i < array.length; i++) {
			for (int a = 0; a<array[i].length; a++) {
				line = String.valueOf(array[i][a][0]);
				for(int j = 1; j < array[i][a].length; j++) {
					line += separator + String.valueOf(array[i][a][j]);
				}
				fileCSV.write(line);
				fileCSV.newLine();
			}
		}
		fileCSV.close();	
	}
	
	public final static double[][] readDoubleTwoDimArrayFromCSV(String fileName)
		throws IOException  {
		
		double[][] array = null;
		// Read CSV values
		CSVReader reader = new CSVReader(
			new FileReader(new File(fileName)), DEFAULT_CSV_SEPARATOR
		);
		
		try {
			List<String[]> csvValues = reader.readAll();
			Iterator<String []> csvIterator = csvValues.iterator();
			
			int rows = csvValues.size();
			int cols = csvValues.get(0).length;
			
			if (rows == 0 || cols == 0 || csvValues.isEmpty()) {
				throw new IllegalArgumentException("Bad CSV file: " + fileName);
			}
			
			array = new double[rows][cols];

			for (int i = 0; csvIterator.hasNext(); i++) {
				String[] csvLine = csvIterator.next();
				assert(csvLine.length == cols);
				for (int j = 0; j < cols; j++) {
					array[i][j] = Double.valueOf(csvLine[j]);
				}
				assert(csvIterator.hasNext() || i == rows);
			}
		} finally {
			reader.close();
		}
		return array;
	}
	
	public final static void parseGRP(
			String grpCsvFolder,
			int numSegments, 
			double[] segmentProportions, 
			int intervals
			) throws IOException {
		
		File folder = new File(grpCsvFolder);
		if(folder.isDirectory()) {
			File[] touchpoints = folder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return new File(dir.getAbsolutePath()+"/"+name).isDirectory();
				}
			});
			
			for (File t:touchpoints) {
				
				final String tdir = t.getAbsolutePath();
				
				final String dir2seg = tdir + "/twoSegments/";
				File folder2seg = new File(dir2seg);
				if(!folder2seg.mkdir() && ! folder2seg.exists()) {
					throw new IllegalArgumentException("Cant create dir: "+dir2seg);
				}
				
				final String dirBySemester = tdir + "/by_semester/";
				File folderBySemester = new File(dirBySemester);
				if(!folderBySemester.mkdir() && !folderBySemester.exists()) {
					throw new IllegalArgumentException("Cant create dir: "+dirBySemester);
				}
				
				File[] csvGrp = t.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith("csv");
					}
				});
				
				for (File csv:csvGrp) {
					CSVReader reader = new CSVReader(new FileReader(csv),'\t');
					List<String[]> csvValues=reader.readAll();
					
					parseGRPBySegment(csvValues, csv.getName(), dir2seg, segmentProportions);
					splitGRPByPeriod(csvValues, csv.getName(), dirBySemester, 2);
							
					reader.close();
				}
				
				File[] splittedGRP = folderBySemester.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith("csv");
					}
				});
				final String dirBySegmentAndSemester = dir2seg + "by_semester/";
				File folderBySegmentAndSemester = new File(dirBySegmentAndSemester);
				if(!folderBySegmentAndSemester.mkdir() && !folderBySegmentAndSemester.exists()) {
					throw new IllegalArgumentException("Cant create dir: "+dirBySegmentAndSemester);
				}
				for (File splitted : splittedGRP) {
					
					CSVReader reader = new CSVReader(new FileReader(splitted),'\t');
					List<String[]> csvValues=reader.readAll();
					
					parseGRPBySegment(csvValues, splitted.getName(), dirBySegmentAndSemester, segmentProportions);
							
					reader.close();
				}
			}
		}
	}
	
	
	/**
	 * This method assumes that only one GRP line is supplied.
	 * @param source
	 * @param name
	 * @param folder
	 * @param proportions
	 * @throws IOException
	 */
	private static void parseGRPBySegment(
			List<String[]> source, 
			String name, 
			String folder, 
			double[] proportions			
			) throws IOException {
		
//		for (String[] grpSet: source) {
		String[] grpSet = source.get(0);
		double[][]	newValues = new double [proportions.length][grpSet.length];
		
		for (int i=0; i<grpSet.length; i++) {
			if(grpSet[i].equals("")) {
				continue;
			}
			double grpValue = Double.valueOf(grpSet[i]);
			for (int j=0; j<proportions.length; j++) {
				newValues[j][i] = grpValue * proportions[j];
			}
		}
		writeDoubleTwoDimArrayToCSV(folder+name, newValues,'\t');
//		}
	}

	/**
	 * This method also assumes that only one GRP line is supplied.
	 * @param source
	 * @param name
	 * @param folder
	 * @param proportions
	 * @throws IOException
	 */
	private static void splitGRPByPeriod(
			List<String[]> source, 
			String name, 
			String folder, 
			int period
			) throws IOException {
		
//		for (String[] grpSet: source) {
		String[] grpSet = source.get(0);
		double[][]	newValues = new double [period][grpSet.length];
		int pivotDistance = grpSet.length / period;
		int splitIndex = 0;
		for (int i=0; i<grpSet.length; i++) {
			if(i==(splitIndex+1)*pivotDistance) {
				splitIndex++;
			}
			double grpValue = Double.valueOf(grpSet[i]);
			newValues[splitIndex][i] = grpValue;
		}
		for (int i=0; i<period; i++) {
			double[][] renewValues = new double [1][];
			renewValues[0]=newValues[i];
			writeDoubleTwoDimArrayToCSV(folder+name+"-semester"+i+".csv", renewValues, '\t');
		}
//		}
	}
	
	public static String readFile(String filename) throws IOException {
		//Read the JSON file
		BufferedReader br = new BufferedReader(new FileReader(filename));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		return buffer.toString();
	}
	
	public static void writeFile(String filename, String content) throws IOException {
		FileWriter fw = new FileWriter(filename);
		fw.write(content);
		fw.close();
	}
}
