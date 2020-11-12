package util.io.functions;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Workbook;

import util.io.XLSXFileUtils.REPORT_STRUCTURE;
import util.io.XLSXFileUtils.REPORT_TYPE;

public interface StatisticsUtils {

	public CellStyle getCellStyle(Workbook book,DataFormat formatter);
	
	public CellStyle getCellAvgStyle(Workbook book,DataFormat formatter);
	
	public REPORT_STRUCTURE getContentStructure();
	
	public REPORT_TYPE getReportType();
}
