package util.io.functions;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Workbook;

import util.functions.MatrixFunctions;
import util.io.XLSXFileUtils.REPORT_STRUCTURE;
import util.io.XLSXFileUtils.REPORT_TYPE;
import util.statistics.Statistics;

public class SalesUtils 
	implements StatisticsUtils, UnAttributedStatisticsUtils{

	public SalesUtils() {
	}

	@Override
	public double[][] getValues(Statistics stats) {
		return MatrixFunctions.intToDouble(stats.
				computeScaledSalesByBrandBySegment());
	}

	@Override
	public double[][] getValuesBySegment(Statistics stats, int index) {
		return MatrixFunctions.intToDouble(stats.
				computeScaledSalesByBrandBySegmentByStep()[index]);
	}

	@SuppressWarnings("deprecation")
	@Override
	public CellStyle getCellStyle(Workbook book, DataFormat formatter) {
		CellStyle cellStyle = book.createCellStyle();
		cellStyle.setDataFormat(formatter.getFormat("#,##0"));
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		
		return cellStyle;
	}

	@SuppressWarnings("deprecation")
	@Override
	public CellStyle getCellAvgStyle(Workbook book, DataFormat formatter) {
		CellStyle cellStyle = book.createCellStyle();
		cellStyle.setDataFormat(formatter.getFormat("#,##0.00"));
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		
		return cellStyle;
	}

	@Override
	public REPORT_TYPE getReportType() {
		return REPORT_TYPE.WEEKLY;
	}

	@Override
	public REPORT_STRUCTURE getContentStructure() {
		return REPORT_STRUCTURE.BY_BRAND;
	}

}
