package util.io.functions;

import util.statistics.Statistics;

public interface AttributedStatisticsUtils {

	public abstract double[][][] getValues(Statistics stats);
	
	public abstract double[][][] getValuesBySegment(Statistics stats, int index);
}
