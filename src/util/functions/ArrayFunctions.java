package util.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.stat.StatUtils;

import gnu.trove.list.array.TIntArrayList;
import util.random.Randomizer;

public class ArrayFunctions {

	/**
	 * Computes the mean of the values contained in the provided integer array
	 * as a double value.
	 * @param array an integer array
	 * @return the mean of the values contained in the provided integer array
	 * as a double value.
	 */
	public final static double mean(int[] array) {
		double avg = 0;
		for(int i=0; i<array.length; i++) {
			avg += array[i];
		}
		return avg / array.length;
	}
	
	/**
	 * Performs linear combination to every pair of elements as: 
	 * ((1 - {@code alpha}) * {@code first}) + (({@code alpha}) * {@code second}) 
	 * 
	 * @param first first array involved in linear combination.
	 * @param second second array involved in linear combination.
	 * @param alpha percentage value guiding linear combination.
	 * @return computed array using linear combination.
	 */
	public static final double[] linearCombination(
			double[] first, double[] second, double alpha) {
		
		assert(first.length == second.length);
		
		double[] combined = new double[first.length];
		
		for (int i = 0; i<first.length; i++) {
			combined[i]=Functions.linearCombination(first[i], second[i], alpha);
		}
		
		return combined;
	}
	
	/**
	 * Returns the indexes with all the appearances of the given integer value.
	 * @param array an integer array where the appearances are being looked for 
	 * @param value an integer value whose appearances are being looked for
	 * @return the indexes with all the  appearances of the given integer value.
	 */
	public final static int[] getAllIndexesOf(int[] array, int value) {
		TIntArrayList list = new TIntArrayList();
		
		for (int pos =0; pos<array.length; pos++) {
			if(array[pos]==value) {
				list.add(pos);
			}
		}
		
		return list.toArray();
	}
	
	/**
	 * Selects a random index among the values where values[i]=true. If there 
	 * is no element that satisfies the condition, -1 is returned.
	 * 
	 * @param values the boolean values.
	 * @param random randomizer used during the simulation.
	 * @return a random index.
	 */
	public static final int selectRandomIndex(boolean[] values, Randomizer random) {
        
    	int numBrands = values.length;    	
    	int[] brandIndexes = new int[numBrands];
    	
    	int counter = 0;
    	
        for(int i=0; i<numBrands; i++) {
            if(values[i]) {
            	brandIndexes[counter]=i;                			
                counter++;
            }
        }       

        if(counter == 0) {
            return -1;
        } else {
            return brandIndexes[random.nextInt(counter)];                
        }
    }
	
	/**
	 * Returns the index of the biggest element in the provided double array.
	 * @param array an double array
	 * @return the index of the biggest element in the provided double array
	 */
	public final static int getIndexOfMax(double[] array) {
		double max = Double.MIN_VALUE;
		int index = -1;
		
		for (int i=0; i<array.length; i++) {
			if(array[i]!=0.0 && array[i]>max) {
				max = array[i];
				index = i;
			}
		}
		
		return index;
	}
	
	/**
	 * Sub array from begin position, excluding end position.
	 * @param array
	 * @param emptyArray
	 * @param begin
	 * @param end
	 * @return subarray
	 */
	public final static < E > E[]  getSubArray(E[] array, E[] emptyArray, int begin, int end) {
		List<E> subArray = new ArrayList<E>();
		for (int i=begin; i<end; i++) {
			subArray.add(array[i]);
		}
		return subArray.toArray(emptyArray);
	}
	
	
	// ########################################################################	
	// Check Methods 	
	// ########################################################################
	
	/**
	 * Checks if the array (one dim. array) are in the range 
	 * [{@paramref min},{@paramref max}].
	 * @param array - the array to be checked.
	 * @param min - the minimum value allowed.
	 * @param max - the maximum value allowed.
	 */
	public final static void checkArrayBoundaries(double[] array, double min, double max) {
		for(int i=0; i<array.length; i++) {
			// TODO [KT] Extend the exception thrown by assertion??				
			assert(array[i] <= max);
			assert(array[i] >= min);				
		}
	}
	
	
	// ########################################################################	
	// Shuffle Methods 	
	// ########################################################################
	
	/**
	 * Constructs and initializes a full permutation over an array 
	 * of length > 1, with values from 0 to length - 1.
	 * 
	 * @param permutation 	Length of array to shuffle (greater than 1)
	 * @param random 		Random generator
	 */
	public final static byte[] shuffleFast(
			final byte length, Randomizer random) {		
		
		assert(length > 1);
		
		int irand;
		byte[] permutation = new byte[length];
		
		// Combination of shuffle with the initialization of array
		//   If irand = i, then first assignment is useless, 
		//   but second will overwrite it with correct value i
		for (byte i = 1; i < length; i++) {
			irand = random.nextInt(i + 1); 			// rand in [0, i]
			permutation[i] = permutation[irand]; 	// swap value
			permutation[irand] = i; 				// new value
		}
		
		return permutation;
	}
	
	/**
	 * Constructs and initializes a full permutation over an array 
	 * of length > 1, with values from 0 to length - 1.
	 * 
	 * @param permutation 	Length of array to shuffle (greater than 1)
	 * @param random 		Random generator
	 */
	public final static int[] shuffleFast(
			final int length, Randomizer random) {		
		
		assert(length > 1);
		
		int irand;
		int[] permutation = new int[length];
		
		// Combination of shuffle with the initialization of array
		//   If irand = i, then first assignment is useless, 
		//   but second will overwrite it with correct value i
		for (int i = 1; i < length; i++) {
			irand = random.nextInt(i + 1); 			// rand in [0, i]
			permutation[i] = permutation[irand]; 	// swap value
			permutation[irand] = i; 				// new value
		}
		
		return permutation;
	}
	
	/**
	 * Performs a partial shuffle into a percentage of the given integer 
	 * array. This shuffle is done using a given percentage probability 
	 * and a uniform number generator.
	 * @param array the integer array to be partially shuffled
	 * @param random a uniform number generator
	 * @param percentage the percentage elements to be shuffled
	 * @return the resulting array after applying the partial shuffle
	 */
	public final static int[] partialShuffle(
			int[] array, Randomizer random, double percentage) {		
		
		int size = array.length;
		
		assert(array.length > 1);
		
		int numSwaps = (int) (percentage * size);

		/*
		 * Swaps the first element N times with random elements.
		 * This is more efficient than getting two different indexes. 
		 */
		for (int i = 1; i < numSwaps; i++) {
			// irand in [0, size-1]
			int irand = random.nextInt(size);
			final int temp = array[0];
			array[0] = array[irand];
			array[irand] = temp;
		}
		
		return array;
	}
	
	/**
	 * End value is inclusive.
	 * @param array
	 * @param random
	 * @param begin
	 * @param end
	 */
	public final static void partialShuffle(
			int[] array, Randomizer random, int begin, int end) {
		
		int irand;
		int temp;
		for (int index = end; index > begin; index--) {
			irand = random.nextInt(index + 1-begin)+ begin; 	// rand in [0, i]
			temp = array[index];	
			array[index] = array[irand];
			array[irand] = temp;
		}
	}
	
	/**
	 * Performs a full permutation over the array (length > 2). 
	 * 
	 * @param permutation 	Array to shuffle (length > 2)
	 * @param random 		Random generator
	 */
	public final static void shuffleArrayFast(
			final byte[] permutation, Randomizer random) {
		
		assert(permutation.length > 1);
		
		int irand;
		byte temp;
		for (int index = (permutation.length - 1); index > 0; index--) {
			irand = random.nextInt(index + 1); 	// rand in [0, i)
			temp = permutation[index];	
			permutation[index] = permutation[irand];
			permutation[irand] = temp;
		}
	}
	
	/**
	 * Performs a full permutation over the array (length > 2). 
	 * 
	 * @param permutation 	Array to shuffle (length > 2)
	 * @param random 		Random generator
	 */
	public final static void shuffleArrayFast(
			final int[] permutation, Randomizer random) {
		
		assert(permutation.length > 1);
		
		int irand;
		int temp;
		for (int index = (permutation.length - 1); index > 0; index--) {
			irand = random.nextInt(index + 1); 	// rand in [0, i)
			temp = permutation[index];	
			permutation[index] = permutation[irand];
			permutation[irand] = temp;
		}
	}
	
	public static double[] getRandomDoubleArray(Randomizer rnd, int numParams) {
		double[] ind = new double[numParams];
		for (int i=0; i<numParams; i++) {
			ind[i] = rnd.nextDouble();
		}
		return ind;
	}
	
	public static int[] getRandomIntArray(Randomizer rnd, int numParams, int max) {
		int[] ind = new int[numParams];
		for (int i=0; i<numParams; i++) {
			ind[i] = rnd.nextInt(max);
		}
		return ind;
	}
	
	// ########################################################################	
	// Manipulation Methods 	
	// ########################################################################
	
	/**
	 * Chunks the given double array into the specified number of pieces.
	 * @param array the double array to get chunk
	 * @param pieces the number of pieces
	 * @return a double matrix containing all the pieces resulting from 
	 * chunking the provided double array
	 */
	public final static double[][] chunkDoubleArray(double[] array, int pieces) {
		int numberOfChunkedRows = array.length / pieces;
		double [][] chunked = new double [pieces][numberOfChunkedRows];
		
		int counter = 0;
		
		for (int i=0; i<pieces; i++) {
			for (int j=0; j<numberOfChunkedRows; j++) {
				chunked[i][j] = array[counter];
				counter++;
			}
		}
		
		return chunked;
	}
	
	/**
	 * This method expects a squared matrix.
	 * @param array - array values.
	 * @return flatted array.
	 */
	public final static double[] flatDoubleArray(double[][] array) {
		
		int numElements = array.length * array[0].length;
		
		double[] flatted = new double [numElements];
		
		int counter =0;
		
		for (int i=0; i<array.length; i++) {
			for (int j=0; j<array[i].length; j++) {
				flatted[counter] = array[i][j];
				counter++;
			}
		}

		return flatted;
	}
	
	/**
	 * Flats the given 3d double matrix into a 2d one using the mean value of 
	 * the deepest array.
	 * @param array a 3d double matrix
	 * @return the resulting flat matrix after reducing one dimension as an 
	 * average value.
	 */
	public static double [][] flatDoubleArrayUsingAverage(double[][][] array) {
		double [][] result = new double [array.length][];
		
		for (int i=0; i<array.length; i++) {
			result[i] = new double [array[i].length];
			for (int j=0; j<result[i].length; j++) {
				result[i][j]=StatUtils.mean(array[i][j]);
			}
		}
		
		return result;
	}
	
	/**
	 * Fills the given double matrix with the provided double array. The resulting
	 * matrix is returned containing each raw with the value of each position of 
	 * the provided array.
	 * @param matrix a double matrix
	 * @param array a double array
	 * @return the resulting matrix after filling each row with each of the 
	 * elements of the given array
	 */
	public final static double [][] fillArray(double [][] matrix, double [] array) {
		for (int i=0; i<matrix.length; i++) {
			Arrays.fill(matrix[i], array[i]);
		}
		return matrix;
	}
	
	/**
	 * Sets each position of the given double matrix to the provided double value.
	 * @param matrix a double matrix
	 * @param value a double value
	 * @return the resulting matrix after setting each position of the given 
	 * double matrix to the provided double value.
	 */
	public final static double [][] fillArray(double [][] matrix, double value) {
		for (int i=0; i<matrix.length; i++) {
			Arrays.fill(matrix[i], value);
		}
		return matrix;
	}
	
	/**
	 * Sets each position of the given 3d double matrix to the provided double value.
	 * @param matrix a 3d double matrix
	 * @param value a double value
	 * @return the resulting 3d matrix after setting each position of the given 
	 * double matrix to the provided double value.
	 */
	public final static double [][][] fillArray(double [][][] matrix, double value) {
		for (int i=0; i<matrix.length; i++) {
			fillArray(matrix[i], value);
		}
		return matrix;
	}
	
	// ########################################################################	
	// Calculation Methods 	
	// ########################################################################
	
	/**
	 * Adds the given int array.
	 * @param array the integer array.
	 * @return the result of adding every element in the array.
	 */
	public final static int addArray(int[] array) {
		int begin = 0;
		int end = array.length;
		return addArraySegment(array, begin, end);
	}
	
	/**
	 * End is exclusive.
	 * @param array
	 * @param begin
	 * @param end
	 * @return
	 */
	public final static int addArraySegment(int[] array, int begin, int end) {
		int sum =0;
		for (int i=begin; i<end; i++) {
			sum+=array[i];
		}
		return sum;
	}
	
	/**
	 * End is exclusive.
	 * @param array
	 * @param begin
	 * @param end
	 * @return
	 */
	public final static int addArraySegment(double[] array, int begin, int end) {
		int sum =0;
		for (int i=begin; i<end; i++) {
			sum+=array[i];
		}
		return sum;
	}
	
	/**
	 * Counts true ocurrences in a boolean array.
	 * 
	 * End is exclusive.
	 * @param array
	 * @param begin
	 * @param end
	 * @return
	 */
	public final static int addArraySegment(boolean[] array, int begin, int end) {
		int sum =0;
		for (int i=begin; i<end; i++) {
			if(array[i]) {
				sum++;
			}
		}
		return sum;
	}
	
	// ########################################################################	
	// Scale Methods
	// ########################################################################
	
	/**
	 * Scales each position of the provided double array using the given scale
	 *  value.
	 * @param array the provided double array
	 * @param scaleValue the double scale value to be applied
	 */
	public final static void scaleDoubleArray(double[] array, double scaleValue) {
		for (int i=0; i<array.length; i++) {
			array[i]*=scaleValue;
		}
	}
	
	/**
	 * Scales each position of the provided integer array using the given scale
	 * value. The resulting array is returned. 
	 * @param array the provided double array
	 * @param scaleValue the double scale value to be applied
	 * @return the scaled integer array
	 */
	public final static int[] scaleIntArray(int[] array, double scaleValue) {
		for (int i=0; i<array.length; i++) {
			array[i]=(int)(array[i]*scaleValue);
		}
		return array;
	}
	
	/**
	 * Scales each position of the provided integer array using the given scale
	 * value. The resulting new array is returned. 
	 * @param array the provided int array
	 * @param scaleValue the double scale value to be applied
	 * @return the new scaled integer array
	 */
	public final static int[] scaleCopyIntArray(int[] array, double scaleValue) {
		int[] scaled = new int [array.length]; 
		for (int i=0; i<array.length; i++) {
			scaled[i]=(int)(array[i]*scaleValue);
		}
		return scaled;
	}
	
	/**
	 * Copies the provided double array and scales each position using the 
	 * given scale value. The resulting array is returned. 
	 * @param array the provided double array
	 * @param scaleValue the double scale value to be applied
	 * @return a new double array containing the scaled double array
	 */
	public final static double[] scaleCopyOfDoubleArray(double[] array, double scaleValue) {
		double[] copy = new double[array.length];
		for (int i=0; i<array.length; i++) {
			double a=array[i];
			copy[i]=a*scaleValue;
		}
		return copy;
	}
	
	/**
	 * Copies, scales and truncates to integer values the provided double 
	 * array using given scale value.
	 * @param array a double array
	 * @param scaleValue a double value used to scale the array
	 * @return a new integer array containing the scaled and truncated values 
	 * from given double array
	 */
	public final static int[] scaleCopyOfDoubleArrayAndTruncateToInt(double[] array, double scaleValue) {
		int[] copy = new int[array.length];
		for (int i=0; i<array.length; i++) {
			double a=array[i];
			copy[i]=(int)(a*scaleValue);
		}
		return copy;
	}
	
	// ########################################################################	
	// Conversion Methods
	// ########################################################################
	
	/**
	 * Converts the given integer array into a double one with the equivalent 
	 * values.
	 * @param array an integer array
	 * @return the converted integer array with the equivalent double values
	 */
	public final static double[] intToDouble(int[] array) {
		double[] doubleArray = new double[array.length];
		for(int i=0; i<array.length; i++) {
			doubleArray[i]=array[i];
		}
		return doubleArray;	
	}
	
	/**
	 * Converts the given String array into a double one with the represented 
	 * values.
	 * @param array the String array to be converted into a double one
	 * @return the converted String array as a double one with the represented 
	 * values.
	 */
	public final static double [] stringToDouble(String[] array) {
		double[] doubleArray = new double[array.length];
		for(int i=0; i<array.length; i++) {
			doubleArray[i]=Double.valueOf(array[i]);
		}
		return doubleArray;	
	}
	
	/**
	 * Converts the given String array into a double one with the represented 
	 * values.
	 * @param array the String array to be converted into a double one
	 * @return the converted String array as a double one with the represented 
	 * values.
	 */
	public final static String [] doubleToString(double[] array) {
		String[] doubleArray = new String[array.length];
		for(int i=0; i<array.length; i++) {
			doubleArray[i]=String.valueOf(array[i]);
		}
		return doubleArray;	
	}
	
	// ########################################################################	
	// To String Methods
	// ########################################################################
	
	/**
	 * Concatenates an array of String into a single one, separating
	 * them with ','.
	 * @param chains the String containing all the chains.
	 * @return the String containing all the chains.
	 */
	public final static String arrayToString(String[] chains) {
		return arrayToString(chains, ',');
	}
	
	/**
	 * Concatenates an array of String into a single one, separating each 
	 * element with the given separator.
	 * @param chains the array containing the String elements
	 * @param separator the given separator to separate the String elements
	 * @return the String containing all the chains.
	 */
	public final static String arrayToString(String[] chains, char separator) {
		String result="";
		
		if(chains.length>0) {
			result+=chains[0];
		}
		for (int i=1; i<chains.length; i++) {
			result+=separator+chains[i];
		}
		
		return result;
	}
	
	/**
	 * Represents an array of double values into a String, separating
	 * every element with a ',' character.
	 * 
	 * @param values - the double array to be represented as a String
	 * @return The String representing double the array.
	 */
	public final static String arrayToString(double[] values) {
		String result="";
		if(values.length>0) {
			result+=values[0];
		}
		for (int i=1; i<values.length; i++) {
			result+=","+values[i];
		}
		return result;
	}
	
	/**
	 * Represents an array of int values into a String, separating
	 * every element with the given character.
	 * 
	 * @param values - the int array to be represented as a String
	 * @param separator - the char selected for separating the arrays values.
	 * @return The String representing the int array.
	 */
	public final static String arrayToString(int[] values, String separator) {
		String result="";
		if(values.length>0) {
			result+=values[0];
		}
		for (int i=1; i<values.length; i++) {
			result+=separator+values[i];
		}
		return result;
	}
	
	/**
	 * Represents an array of byte values into a String, separating
	 * every element with the given character.
	 * 
	 * @param values - the byte array to be represented as a String
	 * @param separator - the char selected for separating the arrays values.
	 * @return The String representing the byte array.
	 */
	public final static String arrayToString(byte[] values, char separator) {
		String result="";
		if(values.length>0) {
			result+=values[0];
		}
		for (int i=1; i<values.length; i++) {
			result+=separator+String.valueOf(values[i]);
		}
		return result;
	}
	
	/**
	 * Represents an array of boolean values into a String, separating
	 * every element with the given character.
	 * 
	 * @param values - the boolean array to be represented as a String
	 * @param separator - the char selected for separating the arrays values.
	 * @return The String representing the boolean array.
	 */
	public final static String arrayToString(boolean[] values, char separator) {
		String result="";
		if(values.length>0) {
			result+=values[0];
		}
		for (int i=1; i<values.length; i++) {
			result+=separator+String.valueOf(values[i]);
		}
		return result;
	}
}
