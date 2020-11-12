package util.io.functions;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Workbook;

import util.io.XLSXFileUtils.REPORT_STRUCTURE;
import util.io.XLSXFileUtils.REPORT_TYPE;
import util.statistics.Statistics;

public class TPContributionUtils implements StatisticsUtils, AttributedStatisticsUtils{

	@Override
	public double[][][] getValues(Statistics stats) {
		return stats.getContributionByAttByBrandByTp();
	}

	@Override
	public double[][][] getValuesBySegment(Statistics stats, int index) {
		return stats.getContributionBySegByAttByBrandByTp()[index];
	}

	@SuppressWarnings("deprecation")
	@Override
	public CellStyle getCellStyle(Workbook book, DataFormat formatter) {
		CellStyle cellStyle = book.createCellStyle();
		cellStyle.setDataFormat(formatter.getFormat("0.00"));
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		
		return cellStyle;
	}

	@Override
	public CellStyle getCellAvgStyle(Workbook book, DataFormat formatter) {
		return getCellStyle(book, formatter);
	}

	@Override
	public REPORT_TYPE getReportType() {
		return REPORT_TYPE.BY_TOUCHPOINT;
	}

	@Override
	public REPORT_STRUCTURE getContentStructure() {
		return REPORT_STRUCTURE.BY_ATT_BY_BRAND;
	}

}
