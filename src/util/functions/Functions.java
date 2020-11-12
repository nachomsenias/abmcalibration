package util.functions;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JLabel;

import util.random.Randomizer;

/**
 * Function class contains support and utility functions used on several
 * segments of the code.
 * 
 * @author imoya
 *
 */
public class Functions {
	// Include zero when generating random value from 0 to 1
	public static final boolean INCLUDE_ZERO = true;
	// Include one when generating random value from 0 to 1	
	public static final boolean INCLUDE_ONE = true;
	// Upper bound of the perception value
	public static final double PERCEPTION_MAX = 10.0;
	// Lower bound of the perception value	
	public static final double PERCEPTION_MIN = 0.0;	
	/**
	 * Used for some methods that offers scaling. This constant represent
	 * the willing for not modifying the values.
	 */
	public static final double IDENTITY_SCALE=1.0;
	
	/**
	 * Scale value usually used by the perception related components.
	 * Because same sliders are used to represent values between 0 and
	 * 100 or 0 and 10 (like perceptions), some scale is needed for
	 * adapting every component.
	 */
	public static final double PERCEPTION_MULTIPLIER_SCALE=10.0;
	
	/**
	 * The precision value used for comparing double values.
	 */
	public static final double DOUBLE_EQUALS_DELTA = 0.011;
	
	/**
	 * The goal value that the elements to be normalized should satisfy. 
	 */
	public static final double TOTAL_AMOUNT_NORMALIZABLE_VALUE = 1.0;

	/**
	 * Compares two double values. They will be considered as equals if the difference
	 * between their values is less than delta.
	 * @param a The first double value.
	 * @param b The second double value.
	 * @param delta The threshold where the values are considered as equal.
	 * @return wherever the difference between both values is lesser than delta.
	 */
	public final static boolean equals(double a, double b, double delta) {
		double diff=Math.abs(a-b);
		return diff<=delta;
	}
	
	/**
	 * Approximate power function, Math.pow is quite slow and we don't need accuracy.
     * Ref: 
     * http://martin.ankerl.com/2007/10/04/optimized-pow-approximation-for-java-and-c-c/
	 * @param a base value.
	 * @param b exponential value.
	 * @return power approximation.
	 */
	public static double pow(final double a, final double b) {
        final int x = (int) (Double.doubleToLongBits(a) >> 32);
        final int y = (int) (b * (x - 1072632447) + 1072632447);
        return Double.longBitsToDouble(((long) y) << 32);
    }
	
	/**
	 * Calculates the multiplier for marketing plans with the given quality value.
	 * @param input the quality input value.
	 * @return the quality multiplier.
	 */
	public final static double qualityFunction(double input) {
		if(input>=1.0) {
			return 2.0;
		} else if (input<=0.0) {
			return 0.5;
		} else {			
			double b =input*input;
			double a = input*0.5;
			double output=b+a+0.5;
			return output;
		}		
	}
	
	/**
	 * Performs linear combination as: 
	 * ((1 - {@code alpha}) * {@code first}) + (({@code alpha}) * {@code second}) 
	 * 
	 * @param first first value involved in linear combination.
	 * @param second second value involved in linear combination.
	 * @param alpha percentage value guiding linear combination.
	 * @return computed value using linear combination.
	 */
	public static final double linearCombination(
			double first, double second, double alpha) {
		return ((1 - alpha) * first) + ((alpha) * second);
	}
	
	
	/*
	 * 
	 * STATISTICAL
	 * 
	 */
	
	/**
	 * Applies a normal distribution centered on "value" over [0,interval].
	 * @param mean the value used as mean for the distribution.
	 * @param gaussian the value used for a standard deviation of one.
	 * @param interval the top value used for the bounded distribution.
	 * @return the given value normally distributed.
	 */
	public final static double applyNormalDistribution(
			double mean, 
			double gaussian, 
			double interval) 
	{
		return Functions.scaleGaussianValue(mean,gaussian,9.0,0.0,interval);	
	}
	
	/**
	 * Applies a normal distribution centered on "value" with the given standard
	 * deviation, fitting all values into the interval [bottom, top].
	 * @param mean the value used as mean for the distribution.
	 * @param gaussian the value used for a standard deviation of one.
	 * @param stDeviation the value used for standard deviation.
	 * @param bottom the bottom value for the interval.
	 * @param top the top value for the interval.
	 * @return
	 */
	public final static double scaleGaussianValue(
			double mean, 
			double gaussian,	//TODO [KT] We can use gaussian generator inside to avoid errors 
			double stDeviation,
			double bottom,
			double top) 
	{
		if(gaussian==0.0) {
			return mean;
		} else {			
			double result=0.0;			
			if (gaussian>0) {
				if(mean+stDeviation>top) {
					stDeviation=top-mean;
				}				
			} else {
				if(mean-stDeviation<bottom) {
					stDeviation=mean-bottom;
				}
			}
			result=(gaussian*stDeviation)+mean;
			return result;
		}		
	}
	
	/**
	 * Returns numbers between -1 and 1, normally distributed with 0 as
	 * its mean. [KT] Keep in mind that dividing by 3, the stdev is 0.333.
	 * @param randomizer.
	 * @return the next value from the normal distribution.
	 */
	public final static double nextGaussian(Randomizer randomizer) {
		double gaussian;
		
		do {
			gaussian=randomizer.nextGaussian()/3.0;
		} while(gaussian>1.0 || gaussian < -1.0);
		
		return gaussian;
	}
	
	/**
	 * Returns numbers between -boundary and boundary, normally distributed with 0 as
	 * its mean. TODO [KT] I guess that stdev is set to 1.0 ??? Then, when we set the
	 * boundary to 3 (3 times then stdev) we cover 99.7% of the values.
	 * [KT] Keep in mind that dividing by boundary, the stdev is boundary.
	 * @param randomizer
	 * @param boundary - bounds the normal distribution from both sides.
	 * @return - the next value from the normal distribution.
	 */
	public final static double nextGaussian(Randomizer randomizer, double boundary) {
		double gaussian;
		
		do {
			gaussian=randomizer.nextGaussian();
		} while(gaussian>boundary || gaussian < -boundary);

		return gaussian;
	}
	
	
	/*
	 * 
	 * SELECTION METHODS
	 * 
	 * 
	 */
	
	/**
	 * Random Weighted Selection (like roulette wheel selection in GA)
	 * @param probVec - the vector of probability weights.
	 * @param r - the random value, expected in range [0, 1).
	 * @return - the value selected
	 */	
	public final static int randomWeightedSelection(
			final double probVec[], final double r) {
		
		final int n = probVec.length;
		
		// Calculate total weight
		double totalWeight = 0;
		for(int i = 0; i < n; i++) {
			totalWeight += probVec[i];			
		}
		
		double randValue = r * totalWeight;
		
		// Select the output
		for (int i = 0; i < n; i++) {
			if(randValue < probVec[i]) return i;
			randValue -= probVec[i];
		}

		throw new IllegalStateException(Double.toString(randValue));
	}
	
	/**
	 * Random Weighted Selection (like roulette wheel selection in GA)
	 * @param probVec - the vector of probability weights, which are normalized.
	 * @param r - the random value, expected in range [0, 1).
	 * @return - the value selected
	 */	
	public final static int randomWeightedSelectionNormalized(
			final double probVec[], final double r) {
		
		final int n = probVec.length;
		double r_val = r;
		
		// Select the output
		for (int i = 0; i < n; i++) {
			if(r_val < probVec[i]) return i;
			r_val -= probVec[i];
		}
		
		throw new IllegalStateException(Double.toString(r_val));
	}	
	
	/**
	 * Random Weighted Selection (like roulette wheel selection in GA)
	 * @param cumProbVec - the vector of cumulative probability weights.
	 * @param r - the random value, expected in range [0, 1).
	 * @return The index selected
	 */	
	public final static int randomWeightedSelectionFast(
			final double cumProbVec[], double randValue) {
		
		final int n = cumProbVec.length;
		
		// r * totalWeight
		randValue *= cumProbVec[n-1];
		
		// Select the output
		for (int i = 0; i < n; i++) {
			if(randValue < cumProbVec[i]) return i;
		}

		throw new IllegalStateException(Double.toString(randValue));
	}
	
	/**
	 * Random Weighted Selection (like roulette wheel selection in GA)
	 * @param cumProbVec - the vector of cumulative weights.
	 * @param r - the random value, expected in range [0, 1).
	 * @return The index selected
	 */	
	public final static int randomWeightedSelectionFast(
			final int cumProbVec[], double randValue) {
		
		final int n = cumProbVec.length;
		
		// r * totalWeight
		randValue *= cumProbVec[n-1];
		
		// Select the output
		for (int i = 0; i < n; i++) {
			if(randValue < cumProbVec[i]) return i;
		}

		throw new IllegalStateException(Double.toString(randValue));
	}
	
	
	/**
	 * Random Weighted Selection (like roulette wheel selection in GA) with
	 * restricted entries provided by a boolean array. Those entries
	 * marked by the boolean array are not used in calculation. 
	 * @param probVec - the vector of probability weights.
	 * @param excluded - the vector of restricted entries.
	 * @param r - the random value.
	 * @return - the value selected
	 */	
	public final static int randomWeightedSelectionRestricted(
			final double probVec[], final boolean excluded[], final double r) {

		final int n = probVec.length;
		
		// Calculate total weight
		double totalWeight = 0.0;
		for(int i = 0; i < n; i++) {
			if(!excluded[i]) totalWeight += probVec[i];			
		}
		
		double randValue = r * totalWeight;
		
		// Select the output
		for(int i = 0; i < n; i++) {
			if(!excluded[i]) {
				if(randValue < probVec[i]) return i;
				randValue -= probVec[i];				
			}
		}
		
		throw new IllegalStateException(
				String.format("Not satisfible random value %d for total weight %d.",
						randValue,totalWeight)
				);
	}
	
	/**
	 * Randomly selects one index using simple roulette selection.
	 * @param probabilities the normalized to 1.0 probabilities.
	 * @param randomValue the random value.
	 * @return the selected index (between 0 and probabilities.length).
	 */
	public final static int simpleRouletteSelection(double [] probabilities,
			double randomValue) {
		
		int index = 0;
		double accumulatedProbability = 0.0; 
		boolean selected = false;
		
		while(index<probabilities.length && !selected) {
			if(randomValue<probabilities[index]+accumulatedProbability) {
				selected=true;
			} else {
				accumulatedProbability+=probabilities[index];
				index++;
			}				
		}
		
		//This clause avoids random values close to 0.9999 to cause exceptions
		if(Functions.equals(
				accumulatedProbability,
				Functions.IDENTITY_SCALE,
				Functions.DOUBLE_EQUALS_DELTA)) {
			index = probabilities.length-1;
			selected=true;
		}
		
		return index;
	}
	
	
	/*
	 * STRING CONVERSIONS
	 */
	
	/**
	 * Concatenates several values from the map into a string. 
	 * @param map The map containing the values.
	 * @return the values separated by ','.
	 */
	public final static String mapToString(Map<Integer,?> map) {
		String result="";
		int size=map.size();
		if(size>=1) {
			result+=map.get(0);
		}
		int count=1;
		while(count<size) {
			result+=","+map.get(count);
			count++;
		}
		return result;		
	}
	
	/**
	 * Creates a String array with the text of the labels contained at the 
	 * provided map. 
	 * 
	 * @param map the map with the label texts.
	 * @return a String array with the text of the labels contained at the 
	 * provided map. 
	 */
	public final static String[] labelsToString(Map<Integer,JLabel> map) {
		JLabel[] sample = new JLabel[0];
		JLabel[] labelArray = map.values().toArray(sample);
		
		int size =labelArray.length;
		
		String[] array = new String[size];
		
		for (int i=0; i<size; i++) {
			array[i] = labelArray[i].getText();
		}
		
		return array;
	}
	
	/**
	 * Concatenates a list populated with other lists.
	 * @param listOfLists The list containing the other lists.
	 * @return the list values separated by ',' and ';'
	 */
	public final static String twoLevelListToString(List<List<?>> listOfLists) {
		String result="";
		Iterator<List<?>> itList = listOfLists.iterator();
		if(itList.hasNext()) {
			result+=Functions.listToString(itList.next());
		}
		while(itList.hasNext()) {
			result+=";"+itList.next();
		}
		return result;		
	}
	
	/**
	 * Concatenates several values from the list into a string. 
	 * @param list The list containing the values.
	 * @return the list values separated by ','.
	 */
	public final static String listToString(List<?> list) {
		String result="";
		Iterator<?> it=list.iterator();
		if(it.hasNext()) {
			result+=it.next();
		}
		while(it.hasNext()) {
			result+=","+it.next();
		}
		return result;	
	}
	
	/*
	 * 
	 * NUMERICAL CONVERSIONS
	 * 
	 */	
	
	/**
	 * Reduces the decimal digits of a double value to two.
	 * @param val
	 * @return
	 */
	public final static double setTwoDecimalDigits(double val) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("en", "UK"));
		df.applyPattern("#.##");
		return Double.valueOf(df.format(val));
	}
	
	/**
	 * Truncates the given double value to the specified number of decimal digits.
	 * @param value the double value to be truncated
	 * @param decimalDigits the number of decimal digits to truncate the value
	 * @return the resulting truncated value using the given number of decimal digits 
	 */
	public final static double truncateValue(double value, int decimalDigits) {
		double scale = Math.pow(10, (double)decimalDigits);
		int newValue = (int)(value*=scale);
		value = newValue / scale;
		return value;
	}
	
	/**
	 * Scales the given input value from the provided interval [minIn, maxIn]
	 * to the interval [minOut, maxOut].
	 * @param valueIn given input value
	 * @param minIn the minimum value from the input interval
	 * @param maxIn the maximum value from the input interval
	 * @param minOut the minimum value from the output interval
	 * @param maxOut the maximum value from the output interval
	 * @return the scaled input value adjusted to the interval [minOut, maxOut]
	 */
	public final static double normalizeMinMax(
		double valueIn, double minIn, double maxIn, double minOut, double maxOut
	){
		double valueOut = 0;
		valueOut = minOut + ((valueIn - minIn) * (maxOut - minOut)) / (maxIn - minIn);
		return valueOut;
	}

	/**
	 * Generates the array of the indices based on the random weighted order.
	 * It uses the values of the array to weight the probabilities of each
	 * index to be chosen.
	 * @param array - the array of values to be ordered.
	 * @param random - the random number generator.
	 * @return - the array of the indices.
	 */
	public final static int[] getIndicesRandomWeightedOrder(
		double[] array, Randomizer random
	) {
		int size = array.length;
		boolean[] used = new boolean[size];
		int[] results = new int[size];
		int index;
		
		for(int i=0; i<size; i++) {
			index = Functions.randomWeightedSelectionRestricted(
				array, used, random.nextDouble() // [0, 1)
			);
			results[i] = index;
			used[index] = true;
		}
		return results;
	}

	/**
	 * Checks if the value (one dim. array) are in the range 
	 * [{@paramref min},{@paramref max}].
	 * @param value - the value to be checked.
	 * @param min - the minimum value allowed.
	 * @param max - the maximum value allowed.
	 */
	public final static void checkDoubleBoundaries(double value, double min, double max) {			
		assert(value <= max);
		assert(value >= min);				
	}
	
	
	/*
	 * 
	 * BOOLEAN OPERATIONS  
	 *
	 */
	
	/**
	 * Returns the array resulting of performing and operator over given arrays.
	 * @param first - the first boolean array.
	 * @param second - the seconde booolean array.
	 * 
	 * @return the array resulting of applying and operator between every element
	 * for both arrays.
	 */
	public final static boolean [] and(boolean [] first, boolean [] second) {
		int numElements = Math.min(first.length, second.length);
		boolean [] third = new boolean [numElements];
		
		for (int i=0; i<numElements; i++) {
			third[i] = first[i] && second[i];
		}
		
		return third;
	}
	
	public final static boolean [] checkDifferent(double [] values, double value) {
		int numValues = values.length;
		boolean [] checks = new boolean [numValues];
		for (int i=0; i<numValues; i++) {
			checks[i] = values[i]!=value; 
		}
		
		return checks;
	}
	
	public final static boolean [][] checkDifferent(double [][] values, double [] value) {
		int numValues = values.length;
		boolean [][] checks = new boolean [numValues][];
		for (int i=0; i<numValues; i++) {
			checks[i] = checkDifferent(values[i], value[i]); 
		}
		
		return checks;
	}
	
	/**
	 * Checks double probabilities rolling all of them: 
	 * if roll < probabilities, check passes.
	 * 
	 * @param probabilities - double probabilities of enabling i-th flag.
	 * @param randomizer - uniform randomizer used for checking probabilities.
	 * @return flags for every probability. If i-th flag is true, 
	 * i-th check passed.
	 */
	public final static boolean [] checkProbabilities(
			double[] probabilities,
			Randomizer randomizer
		) {
		int items = probabilities.length;
		
		boolean [] result = new boolean [items];
		
		for (int i=0; i<items; i++) {
			double roll = randomizer.nextDouble();
			result[i] = roll< probabilities[i];
		}
		
		return result;
	}
}
