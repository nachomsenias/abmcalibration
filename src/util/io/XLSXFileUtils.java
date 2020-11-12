package util.io;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import model.ModelDefinition;
import util.io.functions.AttributedStatisticsUtils;
import util.io.functions.StatisticsUtils;
import util.io.functions.UnAttributedStatisticsUtils;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics;

public class XLSXFileUtils {
	
	private final static int FIRST_SHEET = 0;
	private final static int ALL_SEGMENTS_SHEET = FIRST_SHEET;
	
	public enum REPORT_TYPE {
		WEEKLY, BY_TOUCHPOINT
	};
	
	public enum REPORT_STRUCTURE {
		BY_BRAND, BY_ATT_BY_BRAND, BY_ATT
	};
	
	public final static void writeReport(
			String title,
			ModelDefinition modelDefinition, 
			MonteCarloStatistics[] mcStatistics,
			StatisticsUtils statsFunction,
			String[] scenarioNames,
			String filename
		) {
		
		filename = formatFilename(modelDefinition, filename);
		
		int nrMC = mcStatistics[0].getNumberOfMonteCarloRepetitions();
		
		final String[] mcHeaders = getNamesMC(nrMC);
		
		final int nrSteps;		
		final String[] stepHeaders;
		
		REPORT_TYPE type = statsFunction.getReportType();
		switch (type) {
		case WEEKLY:
			nrSteps = modelDefinition.getNumberOfWeeks();
			stepHeaders = getNamesWeekStep(nrSteps);
			break;
		case BY_TOUCHPOINT:
			stepHeaders = modelDefinition.getAllTouchPointNames();
			nrSteps = stepHeaders.length;
			break;
		default:
			throw new IllegalStateException(
					type+" is not a valid report type value.");
		}		
		
		Workbook book = createWorkbook(
			title, 
			statsFunction.getContentStructure()==REPORT_STRUCTURE.BY_ATT_BY_BRAND, 
			mcHeaders,
			stepHeaders,
			modelDefinition,
			scenarioNames,
			filename,
			statsFunction.getContentStructure()
		);
		
		int numScenarios = scenarioNames.length;

		final int nrSegments = modelDefinition.getNumberOfSegments();
		final int nrBrands = modelDefinition.getNumberOfBrands();
		final int nrAttributes = modelDefinition.getNumberOfAttributes();
		
		CellStyle cellStyle = statsFunction.getCellStyle(book, book.createDataFormat());
		CellStyle avgStyle = statsFunction.getCellAvgStyle(book, book.createDataFormat());
		
		switch (statsFunction.getContentStructure()) {
		case BY_BRAND:
			writeUnAttributed(numScenarios, nrSegments, nrBrands, nrMC, nrSteps, 
					mcStatistics, (UnAttributedStatisticsUtils)statsFunction, book, 
						cellStyle, avgStyle);
			break;

		case BY_ATT_BY_BRAND:
			writeAttributed(numScenarios, nrSegments, nrBrands, nrAttributes, nrMC, nrSteps, 
					mcStatistics, (AttributedStatisticsUtils)statsFunction, book, cellStyle, avgStyle);
			break;
		case BY_ATT:
			writeUnAttributed(numScenarios, nrSegments, nrAttributes, nrMC, nrSteps, 
					mcStatistics, (UnAttributedStatisticsUtils)statsFunction, book, 
						cellStyle, avgStyle);
			break;
		}
		saveWorkbook(book, filename+"_"+title);
	}
	
	private final static void writeUnAttributed(int numScenarios, int nrSegments, 
			int nrBrands, int nrMC, int nrSteps, MonteCarloStatistics[] mcStatistics, 
			UnAttributedStatisticsUtils statsFunction, Workbook book, 
			CellStyle cellStyle, CellStyle avgStyle
				){
		
		double[][][][] sheetData = new double[numScenarios][nrMC][][];
		
		// Allways copy aggregated data (works for 1 and more segments)
		for (int scenario=0; scenario<numScenarios; scenario++) {
			Statistics[] statistics = mcStatistics[scenario].getStatistics();
			for (int mc = 0; mc < nrMC; mc++) {
				sheetData[scenario][mc] = statsFunction.getValues(statistics[mc]);
			}
		}

		writeUnAttributedSheetData(
				book, cellStyle, avgStyle, sheetData, nrMC, 
				nrBrands, nrSteps, ALL_SEGMENTS_SHEET
			);
		
		// If needed also write each segment data
		if (nrSegments > 1) {
			for (int seg = 0; seg < nrSegments; seg++) {
				for (int scenario=0; scenario<numScenarios; scenario++) {
					Statistics[] statistics = mcStatistics[scenario].getStatistics();
					for (int mc = 0; mc < nrMC; mc++) {
						sheetData[scenario][mc] = statsFunction.
								getValuesBySegment(statistics[mc],seg);
					}
					writeUnAttributedSheetData(
						book, cellStyle, avgStyle, sheetData, nrMC, 
						nrBrands, nrSteps, (seg + 1)
					);
				}
			}
		}
	}
	
	private final static void writeAttributed(int numScenarios, int nrSegments, 
			int nrBrands, int attributes, int nrMC, int nrSteps, 
			MonteCarloStatistics[] mcStatistics, AttributedStatisticsUtils statsFunction,
				Workbook book, CellStyle cellStyle, CellStyle avgStyle
				){
		
		double[][][][][] sheetData = new double[numScenarios][nrMC][][][];
		
		// Allways copy aggregated data (works for 1 and more segments)
		for (int scenario=0; scenario<numScenarios; scenario++) {
			Statistics[] statistics = mcStatistics[scenario].getStatistics();
			for (int mc = 0; mc < nrMC; mc++) {
				sheetData[scenario][mc] = statsFunction.getValues(statistics[mc]);
			}
		}
		
		writeAttributedSheetData(
				book, cellStyle, avgStyle, sheetData, nrMC, 
				nrBrands, attributes, nrSteps, ALL_SEGMENTS_SHEET
			);
		
		// If needed also write each segment data
		if (nrSegments > 1) {
			for (int seg = 0; seg < nrSegments; seg++) {
				for (int scenario=0; scenario<numScenarios; scenario++) {
					Statistics[] statistics = mcStatistics[scenario].getStatistics();
					for (int mc = 0; mc < nrMC; mc++) {
						sheetData[scenario][mc] = statsFunction.
								getValuesBySegment(statistics[mc],seg);
					}
					writeAttributedSheetData(
						book, cellStyle, avgStyle, sheetData, nrMC, 
						nrBrands, attributes, nrSteps, (seg + 1)
					);
				}
			}
		}
	}
	
	private final static void writeUnAttributedSheetData(
			Workbook book,
			CellStyle cellStyle,
			CellStyle avgStyle,
			double[][][][] sheetData, 
			final int nrMC, 
			final int nrBrands,
			final int nrSteps, 
			final int sheetOffset) {
		
		final int rowOffset = 4;
		final int colOffset = 3;
		final int mcOffset = (nrMC > 1)? (nrMC + 2) : 2;
		
		Sheet sheet = book.getSheetAt(sheetOffset);
		double[][][] avg = null;
		
		int numScenarios = sheetData.length;
		for (int i=0; i<numScenarios; i++) {
			
			if (nrMC > 1) avg = new double[numScenarios][nrBrands][nrSteps];
			for (int mc = 0; mc < nrMC; mc++) {
				
				for (int b = 0; b < nrBrands; b++) {
					for (int t = 0; t < nrSteps; t++) {
						
						int rowValue = rowOffset
								+ (i * mcOffset * nrBrands)
								+ (b * mcOffset)
								+ mc;
						int colValue = colOffset + t;
						Cell cell = sheet
							.getRow(rowValue)
							.createCell(colValue);
						
						cell.setCellValue(sheetData[i][mc][b][t]);
						cell.setCellStyle(cellStyle);
						
						if (nrMC > 1) avg[i][b][t] += sheetData[i][mc][b][t];
					}
				}
			}
			if (nrMC > 1) {
				for (int b = 0; b < nrBrands; b++) {
					for (int t = 0; t < nrSteps; t++) {
						
						Cell cell = sheet
								.getRow(rowOffset 
										+ (i * mcOffset * nrBrands)
										+ (b * mcOffset)
										+ nrMC)
								.createCell(colOffset + t);
						
						cell.setCellValue(avg[i][b][t] / nrMC);
						cell.setCellStyle(avgStyle);
					}
				}
			}
		}
	}
	
	private final static void writeAttributedSheetData(
			Workbook book,
			CellStyle cellStyle,
			CellStyle avgStyle,
			double[][][][][] sheetData, 
			final int nrMC, 
			final int nrBrands,
			final int nrAttributes,
			final int nrSteps, 
			final int sheetOffset) {
		
		final int rowOffset = 4;
		final int colOffset = 4;
		final int mcOffset = (nrMC > 1)? (nrMC + 2) : 2;
		
		Sheet sheet = book.getSheetAt(sheetOffset);
		double[][][][] avg = null;
		
		int numScenarios = sheetData.length;
		for (int i=0; i<numScenarios; i++) {
			
			if (nrMC > 1) avg = new double[numScenarios][nrAttributes][nrBrands][nrSteps];
			for (int mc = 0; mc < nrMC; mc++) {
				
				for (int att = 0; att < nrAttributes; att++) {
					for (int brand = 0; brand < nrBrands; brand++) {
						for (int step = 0; step < nrSteps; step++) {
							
							int rowValue = rowOffset
									+ (i * mcOffset * nrBrands * nrAttributes)
									+ (brand * mcOffset * nrAttributes)
									+ (att * mcOffset)
									+ mc;
							int colValue = colOffset + step;
							Cell cell = sheet
								.getRow(rowValue)
								.createCell(colValue);
							
							cell.setCellValue(sheetData[i][mc][att][brand][step]);
							cell.setCellStyle(cellStyle);
							
							if (nrMC > 1) avg[i][att][brand][step] 
									+= sheetData[i][mc][att][brand][step];
						}
					}
				}
			}
			if (nrMC > 1) {
				for (int att = 0; att < nrAttributes; att++) {
					for (int brand = 0; brand < nrBrands; brand++) {
						for (int step = 0; step < nrSteps; step++) {
							
							Cell cell = sheet
									.getRow(rowOffset
											+ (i * mcOffset * nrBrands * nrAttributes)
											+ (brand * mcOffset * nrAttributes)
											+ (att * mcOffset)
											+ nrMC)
									.createCell(colOffset + step);
							
							cell.setCellValue(avg[i][att][brand][step] / nrMC);
							cell.setCellStyle(avgStyle);
						}
					}
				}
			}
		}
	}
	
	public final static Workbook createWorkbook(
			String bookName,
			boolean includeDrivers,
			String[] mcHeaders,
			String[] colHeaders,//weeks
			ModelDefinition modelDefinition,
			String[] scenarioNames,
			String filename,
			REPORT_STRUCTURE structure
		) {
		
		Sheet sheet;
		Workbook book = new XSSFWorkbook();
		
		String[] subrows = null;
		
		if(structure==REPORT_STRUCTURE.BY_BRAND 
				|| structure==REPORT_STRUCTURE.BY_ATT_BY_BRAND) {
			subrows = modelDefinition.getBrandNames();
		} else if (structure==REPORT_STRUCTURE.BY_ATT ){
			subrows = modelDefinition.getAttributeNames();
		}
		
		String[] subsubrows = (includeDrivers)? 
				modelDefinition.getAttributeNames() : mcHeaders;

		String[] requetesubrows = (includeDrivers)? 
				mcHeaders : null;
				
		if (modelDefinition.getNumberOfSegments() > 1) {
			// Create summary sheet for all segments
			sheet = book.createSheet("All");
			writeHeaders(
				book, sheet, bookName, 
				modelDefinition.getName(), 
				scenarioNames,
				subrows, subsubrows, requetesubrows,
				colHeaders
			);
		}
		
		for (int i = 0; i < modelDefinition.getNumberOfSegments(); i++) {
			sheet = book.createSheet(modelDefinition.getSegmentNames()[i]);
			writeHeaders(
				book, sheet, bookName, 
				modelDefinition.getName(), 
				scenarioNames,
				subrows, subsubrows, requetesubrows,
				colHeaders
			);
		}
			
		createInfoSheet(book, bookName, modelDefinition);
		
		return book;
	}

	private final static void createInfoSheet(
			Workbook book, String bookName,
			ModelDefinition modelDefinition) {
		
		Sheet sheet = book.createSheet("Info");
		writeHeaders(
			book, sheet, bookName, 
			modelDefinition.getName(), 
			null, null, null, null, null // no row/columns
		);
		
		int rowOffset = 3;
		CreationHelper helper = book.getCreationHelper();
		String[] lines = modelDefinition.getDescription().split("\\r?\\n");
		
		for (int i = 0; i < lines.length; i++) {
			sheet
				.createRow(i + rowOffset)
				.createCell(0)
				.setCellValue(helper.createRichTextString(lines[i]));
		}
	}

	@SuppressWarnings("deprecation")
	private final static void writeHeaders(
			Workbook book, 
			Sheet sheet, 
			String title, 
			String scenario,
			String[] rows,
			String[] subrows,
			String[] subsubrows,
			String[] requetesubrows,
			String[] cols) {
				
		int rowOffset, colOffset;

		if (subsubrows != null) {
			sheet.setColumnWidth(1, 15 * 256);
			sheet.setColumnWidth(2, 6 * 256);
		} else {
			sheet.setColumnWidth(0, 15 * 256);
			sheet.setColumnWidth(1, 6 * 256);
		}
		
		DataFormat formatter = book.createDataFormat();
		CreationHelper helper = book.getCreationHelper();
		
		Font titleFont = book.createFont();
		titleFont.setFontHeightInPoints((short) 20);
		titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		
		Font boldHeaderFont = book.createFont();
		boldHeaderFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		
		CellStyle titleStyle = book.createCellStyle();
		titleStyle.setDataFormat(formatter.getFormat("text"));
		titleStyle.setFont(titleFont);
		
		CellStyle subTitleStyle = book.createCellStyle();
		subTitleStyle.setDataFormat(formatter.getFormat("text"));
		subTitleStyle.setFont(boldHeaderFont);
		
		CellStyle textStyle = book.createCellStyle();
		textStyle.setDataFormat(formatter.getFormat("text"));
		
		CellStyle colHeaderStyle = book.createCellStyle();
		colHeaderStyle.setDataFormat(formatter.getFormat("text"));
		colHeaderStyle.setFont(boldHeaderFont);
		
		// Auxiliar references
		Row row;
		Cell cell;
		
		// Title
		rowOffset = 0;
		colOffset = 0;
		cell = sheet.createRow(rowOffset).createCell(colOffset);
		cell.setCellValue(helper.createRichTextString(
			String.format("%s (%s)", title, sheet.getSheetName())
		));
		cell.setCellStyle(titleStyle);
		
		// Scenario
		rowOffset = 1;
		colOffset = 0;
		cell = sheet.createRow(rowOffset).createCell(colOffset);
		cell.setCellValue(helper.createRichTextString(scenario));
		cell.setCellStyle(subTitleStyle);
		
		if (cols != null) {
			// Column headers
			rowOffset = 3;
			colOffset = (requetesubrows == null)? 3 : 4;
			row = sheet.createRow(rowOffset);
			for (int c = 0; c < cols.length; c++) {
				cell = row.createCell(c + colOffset);
				cell.setCellValue(helper.createRichTextString(cols[c]));
				cell.setCellStyle(colHeaderStyle);
				sheet.setColumnWidth(c + colOffset, 10 * 256);
			}
		}
		
		if (rows != null) {
			// Row headers
			rowOffset = 4;
			colOffset = 0;
			rowOffset = writeRowHeaders(
				sheet, rows, subrows, subsubrows, requetesubrows,
				rowOffset, colOffset, 
				helper, subTitleStyle, textStyle
			);
		}
				
	}
	
	private final static int writeRowHeaders(
			Sheet sheet, 
			String[] rows, 
			String[] subrows, 
			String[] subsubrows, 
			String[] requetesubrows,
			int rowOffset, 
			int colOffset,
			CreationHelper helper, 
			CellStyle boldHeaderStyle,
			CellStyle textStyle) {
		
		Row row;
		Cell cell;
		for (int r = 0; r < rows.length; r++) {
			colOffset = 0;
			row = sheet.createRow(rowOffset);
			
			cell = row.createCell(colOffset);
			cell.setCellValue(helper.createRichTextString(rows[r]));
			cell.setCellStyle(boldHeaderStyle);
			
			
			// Offset for subrow headers
			colOffset = 1;
			
			if (subsubrows != null) {
				
				// Middle row headers (3 levels)
				rowOffset = writeMiddleRowHeader(
					sheet, row, subrows[0], subsubrows, requetesubrows,
					helper,	boldHeaderStyle, textStyle,
					rowOffset, colOffset
				);
				
				for (int r2 = 1; r2 < subrows.length; r2++) {
					row = sheet.createRow(rowOffset);
					
					rowOffset = writeMiddleRowHeader(
						sheet, row, subrows[r2], subsubrows, requetesubrows,
						helper,	boldHeaderStyle, textStyle,
						rowOffset, colOffset 
					);
				}
			} else {
				
				// Last row headers (only 2 levels)
				rowOffset = writeLastRowHeaders(
					sheet, row, subrows, 
					helper, textStyle, 
					rowOffset, colOffset
				);
			}
		}
		return rowOffset;
	}

	private final static int writeMiddleRowHeader(
			Sheet sheet,
			Row row,
			String subheader,
			String[] subsubheaders,
			String[] requetesubrows,
			CreationHelper helper, 
			CellStyle subheaderStyle, 
			CellStyle subsubheaderStyle,
			int rowOffset,
			final int colOffset) {
		
		Cell cell;
		cell = row.createCell(colOffset);
		cell.setCellValue(helper.createRichTextString(subheader));
		cell.setCellStyle(subheaderStyle);
		
		if(requetesubrows==null) {
			// Write last level headers
			rowOffset = writeLastRowHeaders(
				sheet, row, subsubheaders, 
				helper, subsubheaderStyle, 
				rowOffset, (colOffset + 1)
			);
		} else {
			// Middle row headers (3 levels)
			rowOffset = writeMiddleRowHeader(
				sheet, row, subsubheaders[0], requetesubrows, null,
				helper,	subheaderStyle, subsubheaderStyle, rowOffset, colOffset+1
			);
			
			for (int r2 = 1; r2 < subsubheaders.length; r2++) {
				row = sheet.createRow(rowOffset);
				
				rowOffset = writeMiddleRowHeader(
					sheet, row, subsubheaders[r2], requetesubrows, null,
					helper,	subheaderStyle, subsubheaderStyle, rowOffset, colOffset +1
				);
			}
		}
		
		
		return rowOffset; // rowOffset is handled in subsubheaders
	}
	
	private final static int writeLastRowHeaders(
			Sheet sheet,
			Row row,
			String[] lastHeaders,
			CreationHelper helper, 
			CellStyle cellStyle, 
			int rowOffset,
			final int colOffset) {
		
		// First row
		Cell cell = row.createCell(colOffset);
		cell.setCellValue(helper.createRichTextString(lastHeaders[0]));
		cell.setCellStyle(cellStyle);
		
		// Only if more than 1 row
		if (lastHeaders.length > 1) {
			
			for (int i = 1; i < lastHeaders.length; i++) {
				
				rowOffset++; // always advance row in last level
				
				//System.out.println("    " + rowOffset + " " + colOffset);
				row = sheet.createRow(rowOffset);
				
				cell = row.createCell(colOffset);
				cell.setCellValue(helper.createRichTextString(lastHeaders[i]));
				cell.setCellStyle(cellStyle);
			}
			
			// Average row
			rowOffset++;
			//System.out.println("    " + rowOffset + " " + colOffset);
			row = sheet.createRow(rowOffset);
			cell = row.createCell(colOffset);
			cell.setCellValue(helper.createRichTextString("AVG"));
			cell.setCellStyle(cellStyle);
		}
		
		return (rowOffset + 2); // 1 blank line after last row
	}
	
	public final static void saveWorkbook(Workbook book, String filename) {
		// Save
		FileOutputStream out;
		try {
			out = new FileOutputStream(filename + ".xlsx");
			book.write(out);
			out.close();
			book.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private final static String formatFilename(
			ModelDefinition modelDefinition,
			String filename) {
		
		filename = filename + modelDefinition.getName() + 
			"_" + Long.toString(System.currentTimeMillis());
		return filename;
	}
	
	public final static String[] getNamesMC(int nrMC) {
		String[] names = new String[nrMC];
		for (int i = 0; i < nrMC; i++) {
			names[i] = "MC" + i;
		}
		return names;
	}
	
	public final static String[] getNamesWeekStep(int nrSteps) {
		String stepName = "Week ";
		String[] stepHeaders = new String[nrSteps];
		for (int i = 0; i < nrSteps; i++) {
			stepHeaders[i] = stepName + (i+1);
		}
		return stepHeaders;
	}
}
