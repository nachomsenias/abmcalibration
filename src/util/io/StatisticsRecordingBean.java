package util.io;

import calibration.fitness.history.MultipleKPIHistoryManager.KPIDetail;

public class StatisticsRecordingBean {

	public final boolean exportSales;
	
	public final boolean exportAwareness;

	public final boolean exportPerceptions;
	
	public final boolean exportTouchPointContributions;
	
	public final boolean exportWomReach;
	
	public final boolean exportWomVolumen;
	
	public final boolean exportWomSentiment;
	
	public final boolean exportWomContributions;

	public final boolean anyWoM;
	
	public final boolean exportReach;
	
	public final boolean anyRecording;
	
	public StatisticsRecordingBean(boolean exportSales,
			boolean exportAwareness, boolean exportPerceptions,
			boolean exportTouchPointContributions, boolean exportWomReach,
			boolean exportWomVolumen, boolean exportWomSentiment,
			boolean exportWomContributions,
			boolean exportReach) {
		this.exportSales = exportSales;
		this.exportAwareness = exportAwareness;
		this.exportPerceptions = exportPerceptions;
		this.exportTouchPointContributions = exportTouchPointContributions;
		this.exportWomReach = exportWomReach;
		this.exportWomVolumen = exportWomVolumen;
		this.exportWomSentiment = exportWomSentiment;
		this.exportWomContributions = exportWomContributions;
		this.exportReach = exportReach;
		
		this.anyRecording = anyRecording();
		this.anyWoM = anyWoM();
	}
	
	private boolean anyRecording() {
		return exportSales || exportAwareness || exportPerceptions 
				|| exportTouchPointContributions || exportWomReach 
				|| exportWomVolumen || exportWomSentiment || exportWomContributions 
					|| exportReach;
	}
	
	private boolean anyWoM() {
		return exportWomReach || exportWomVolumen 
				|| exportWomSentiment || exportWomContributions;
	}
	
	public static StatisticsRecordingBean noStatsBean() {
		StatisticsRecordingBean empty = new StatisticsRecordingBean(
				false,false,false,false,false,false,false,false,false);
		return empty;
	}
	
	public static StatisticsRecordingBean onlySalesBean() {
		StatisticsRecordingBean sales = new StatisticsRecordingBean(
				true,false,false,false,false,false,false,false, false);
		return sales;
	}
	
	public static StatisticsRecordingBean getBeanFromDetail(
			KPIDetail salesDetail, KPIDetail awarenessDetail, 
				KPIDetail perceptionsDetail, KPIDetail womVolumeDetail) {
		
		boolean recordSalesDetail = salesDetail!=KPIDetail.DISABLED;
		
		boolean recordAwareness = awarenessDetail!=KPIDetail.DISABLED;
		
		boolean recordPerceptions = perceptionsDetail!=KPIDetail.DISABLED;
		
		boolean recordWomVolume = womVolumeDetail!=KPIDetail.DISABLED;
		
		StatisticsRecordingBean bean = new StatisticsRecordingBean(
				recordSalesDetail,recordAwareness,recordPerceptions,false,false,
					recordWomVolume,false,false,false);
		return bean;
	}
}
