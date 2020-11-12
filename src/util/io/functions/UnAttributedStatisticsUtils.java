package util.io.functions;

import util.statistics.Statistics;

public interface UnAttributedStatisticsUtils {
	
	public abstract double[][] getValues(Statistics stats);
	
	public abstract double[][] getValuesBySegment(Statistics stats, int index);
}
