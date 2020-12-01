package es.ugr.sci2s.soccer.util.script;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import es.ugr.sci2s.soccer.beans.CalibrationResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;

import util.io.CSVFileUtils;
import util.io.XLSXFileUtils;

public class GenerateReports {

	public static final String[] labels = {
			"P1(24)", "P2(39)", "P3(45)", "P4(54)", "P5(60)", "P6(69)",
			"P7(75)", "P8(84)", "P9(90)", "P10(99)", "P11(114)", "P12(129)"
			};
	public static final String[] problems = {
			"0TP","5TP","7TP","10TP","12TP","15TP",
			"17TP", "20TP", "22TP", "25TP", "30TP", "35TP"};
	public static final String[] methods = {
//			"RANDOM",  "HC", "SSGA", "SSGA_HC", "CRO", "CRO_HC", "CRO-SL",
//			"CRO-SL_HC", "CMA-ES-INT", "CMA-ES-HC-INT", "LSHADE", "LSHADE_HC"
			"CMA-ES-OPT-INT", "CMA-ES-OPT-HC-INT"
		}; 
		//"JFDE", "JFDE-real", "CMAES-real", "CRO-SL_2k", "CRO-SL_2k-real", 
		//"CRO-SL_HC_2k-real", "DE_BEST", "DE_BEST_HC", "DE_BEST-real", "DE_BEST_HC-real"
		//"IPOP-CMA-ES-real", "LSHADE-real", "LSHADE_HC-real" 
		//"RANDOM-real","HC-real", "SSGA-real", "SSGA_HC-real","CRO-real", "CRO_HC-real",
		//"CRO-SL-real", "CRO-SL_HC-real", "IPOP-CMA-ES-OPT-real", "IPOP-CMA-ES-HC-real",
	
//	public static int iterations = 20;
	public static int iterations = 5;
	public static char separator = ';';
	
	public static void main(String[] args) throws IOException {
		String root = args[0];
		
		Workbook book = new XSSFWorkbook();
		
		Font bold = book.createFont();
		CellStyle boldStyle = book.createCellStyle();
		bold.setBold(true);
		bold.setFontName("Liberation Sans");
		bold.setFontHeight((short)(200));
		boldStyle.setFont(bold);
		
		//Summary
		Sheet summary = book.createSheet("Summary");
		
		//Problem method it
		double[][][] results = loadResults(root); 
		
		//Write Sheets
		for (int p=0; p<problems.length; p++) {
			Sheet problemSheet = book.createSheet(labels[p]);
			
			int rowoffset = 2;
			int coloffset = 2;
			
			Row row = problemSheet.createRow(rowoffset);
			//Method headers
			for (String method : methods) {
				Cell cell = row.createCell(coloffset);
				cell.setCellValue(method);
				coloffset++;
			}
			rowoffset++;
			
			for (int mc = 0; mc<iterations; mc++) {
				coloffset = 1;
				row = problemSheet.createRow(rowoffset);
				Cell cell = row.createCell(coloffset);
				cell.setCellValue("MC"+mc);
				coloffset++;
				for (int m = 0; m<methods.length; m++) {
					cell = row.createCell(coloffset);
					cell.setCellValue(results[p][m][mc]);
					coloffset++;
				}
				rowoffset++;
			}
			
			coloffset = 2;
			
			row = problemSheet.createRow(rowoffset);
			//Method headers
			for (String method : methods) {
				Cell cell = row.createCell(coloffset);
				cell.setCellValue(method);
				coloffset++;
			}
			rowoffset++;
			
			//Avg
			coloffset = 1;
			row = problemSheet.createRow(rowoffset);
			Cell cell = row.createCell(coloffset);
			cell.setCellValue("Avg");
			coloffset++;
			for (int m = 0; m<methods.length; m++) {
				cell = row.createCell(coloffset);
				cell.setCellValue(StatUtils.mean(results[p][m]));
				coloffset++;
			}
			rowoffset++;
			
			//Std.Dev
			coloffset = 1;
			row = problemSheet.createRow(rowoffset);
			cell = row.createCell(coloffset);
			cell.setCellValue("Std.Dev");
			coloffset++;
			StandardDeviation sd = new StandardDeviation();
			for (int m = 0; m<methods.length; m++) {
				cell = row.createCell(coloffset);
				cell.setCellValue(sd.evaluate(results[p][m]));
				coloffset++;
			}
			rowoffset++;
			
			//Std.Dev
			coloffset = 1;
			row = problemSheet.createRow(rowoffset);
			cell = row.createCell(coloffset);
			cell.setCellValue("Min");
			coloffset++;
			for (int m = 0; m<methods.length; m++) {
				cell = row.createCell(coloffset);
				cell.setCellValue(StatUtils.min(results[p][m]));
				coloffset++;
			}
			rowoffset++;
		}
		
		//Write summary
		int rowoffset = 2;
		int coloffset = 2;
		
		Row row = summary.createRow(rowoffset);
		//Method headers
		for (String method : methods) {
			Cell cell = row.createCell(coloffset);
			cell.setCellValue(method);
			coloffset++;
		}
		rowoffset++;
		
		for (int p=0; p<problems.length; p++) {
			row = summary.createRow(rowoffset);
			coloffset = 0;
			Cell cell = row.createCell(coloffset);
			//Set label value instead of problem
			cell.setCellValue(labels[p]);
			coloffset++;
			
			int startRow = rowoffset;
			
			cell = row.createCell(coloffset);
			cell.setCellValue("Avg");
			coloffset++;
			
			double[] means = new double[methods.length];
			Cell[] cells = new Cell[methods.length];
			
			for (int m = 0; m<methods.length; m++) {
				
				double mean = StatUtils.mean(results[p][m]);
				
				cell = row.createCell(coloffset);
				cell.setCellValue(mean);
				
				cells[m] = cell;
				means[m] = mean;
				
				coloffset++;
			}
			rowoffset++;
			
			coloffset = 1;
			
			double bestAvg = StatUtils.min(means);
			int bestAvgIndex = ArrayUtils.indexOf(means, bestAvg);
			//Set bold style
			cells[bestAvgIndex].setCellStyle(boldStyle);
			//getCellStyle().setFont(bold);
			
			
			row = summary.createRow(rowoffset);
			cell = row.createCell(coloffset);
			cell.setCellValue("Std.Dev");
			coloffset++;
			
			StandardDeviation sd = new StandardDeviation();
			for (int m = 0; m<methods.length; m++) {
				cell = row.createCell(coloffset);
				cell.setCellValue(sd.evaluate(results[p][m]));
				coloffset++;
			}
			rowoffset++;
			
			coloffset = 1;
			row = summary.createRow(rowoffset);
			cell = row.createCell(coloffset);
			cell.setCellValue("Min");
			coloffset++;
			
//			double[] mins = new double[methods.length];
//			Cell[] minCells = new Cell[methods.length];
			
			for (int m = 0; m<methods.length; m++) {
				
				double min = StatUtils.min(results[p][m]);
				
				cell = row.createCell(coloffset);
				cell.setCellValue(min);
				coloffset++;
				
//				minCells[m] = cell;
//				mins[m] = min;
			}
			
//			double bestMin = StatUtils.min(mins);
//			int bestMinIndex = ArrayUtils.indexOf(mins, bestMin);
			//Set bold style
//			cells[bestMinIndex].setCellStyle(boldStyle);
			//getCellStyle().setFont(bold);
			
			int endRow = rowoffset;
			
			summary.addMergedRegion(
					new CellRangeAddress(startRow, endRow, 0, 0));
			
			rowoffset+=3;
			
		}
		
		XLSXFileUtils.saveWorkbook(book, root+"/report");
	}
	
	private static double[][][] loadResults(String root) throws IOException {
		//Problem method it
		double[][][] values = new double [problems.length][methods.length][iterations];
		Gson gson = new Gson();
		
		for (int p=0; p<problems.length; p++) {
			for (int m =0; m<methods.length; m++) {
				String folder = root+problems[p]+"/"+methods[m];
				
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
							values[p][m][index] = 
									calibrationResult.getSalesScoreDetails().finalScore;
						}
					}
				}
			}
		}
		
		return values;
	}
}
