package util.statistics;

import java.util.Arrays;


/**
 * Statistic beans contain Monte-Carlo result matrixes.
 * 
 * @author imoya
 *
 */
public class StatisticsBean {

	/**
	 * Minimum Monte-Carlo result values.
	 */
	public final double [][] minValues;
	
	/**
	 * Maximum Monte-Carlo result values.
	 */
	public final double [][] maxValues;
	
	/**
	 * Average Monte-Carlo result values.
	 */
	public final double [][] averageValues;
	
	/**
	 * Number of iterations.
	 */
	public final boolean mc;
	
	/**
	 * Returns an instance with every array pointing to the same struct. This 
	 * is used for compatibility with simulations with a single iteration.
	 * 
	 * @param values values over time for some property.
	 */
	public StatisticsBean(
			double[][] values
		) {
		super();
		minValues = values;
		maxValues = values;
		averageValues = values;
		
		mc=false;
	}
	
	/**
	 * Returns an instance containing average, minimum and maximum values over 
	 * time for some property.
	 * 
	 * @param averageValues average values over time for some property.
	 * @param minValues minimum values over time for some property.
	 * @param maxValues maximum values over time for some property.
	 */
	public StatisticsBean(
			double[][] averageValues,
			double[][] minValues, 
			double[][] maxValues
		) {
		super();
		this.minValues = minValues;
		this.maxValues = maxValues;
		this.averageValues = averageValues;
		
		mc=true;
	}
	
	/**
	 * Joints contained arrays into a 3d matrix.
	 * 
	 * @return joined arrays into a 3d matrix.
	 */
	public double [][][] join() {
		
		double [][][] joined= new double [3][][];
		joined[0]=averageValues;
		joined[1]=minValues;
		joined[2]=maxValues;
		
		return joined;
	}
	
	
	/**
	 * Transposes given arrays so mc is stored at the second column instead of 
	 * the first one. 
	 * @return
	 */
	public double [][][] traspose() {
		
		//Every array is supposed to have the same longitude.
		int numElements = averageValues.length;
		double [][][] trasposed= new double [numElements][3][];
		
		for (int i=0; i<numElements; i++) {
			trasposed[i][MonteCarloStatistics.MONTE_CARLO_AVG_INDEX]=averageValues[i];
			trasposed[i][MonteCarloStatistics.MONTE_CARLO_MIN_INDEX]=minValues[i];
			trasposed[i][MonteCarloStatistics.MONTE_CARLO_MAX_INDEX]=maxValues[i];
		}
		
		return trasposed;
	}
	
	/**
	 * Joints an array into a 4d matrix of joined beans.
	 * 
	 * @param beans beans to be joined.
	 * @return joined beans as a 4d matrix.
	 */
	public static double [][][][] join (StatisticsBean[] beans) {
		
		int numBeans=beans.length;
		
		double [][][][] joined= new double [numBeans][][][];
		
		for (int i=0; i<numBeans; i++) {
			joined[i]=beans[i].join();
		}
		
		return joined;
	}
	
	/**
	 * Fuses average values for given bean array.
	 * @param beans bean array to be fused.
	 * @return fused average values as a 3d array.
	 */
	public static double [][][] fuseBeanValues(StatisticsBean[] beans) {
		int howmany = beans.length;
		double [][][] values = new double [howmany][][];
		
		for (int i=0; i<howmany; i++) {
			values[i]=beans[i].averageValues;
		}
		
		return values;
	}
	
	@Override
	public String toString() {
		return "StatisticsBean [minValues=" + Arrays.toString(minValues)
				+ ", maxValues=" + Arrays.toString(maxValues)
				+ ", averageValues=" + Arrays.toString(averageValues) + "]";
	}
}
