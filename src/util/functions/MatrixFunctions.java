package util.functions;

import java.util.Arrays;

import org.apache.commons.math.stat.StatUtils;

public class MatrixFunctions {

	/**
	 * Performs linear combination to every pair of elements as: 
	 * ((1 - {@code alpha}) * {@code first}) + (({@code alpha}) * {@code second}) 
	 * 
	 * @param first first array involved in linear combination.
	 * @param second second array involved in linear combination.
	 * @param alpha percentage value guiding linear combination.
	 * @return computed array using linear combination.
	 */
	public static final double[][] linearCombination(
			double[][] first, double[][] second, double alpha) {
		
		assert(first.length == second.length);
		
		double[][] combined = new double[first.length][];
		
		for (int i = 0; i<first.length; i++) {
			combined[i]=ArrayFunctions.linearCombination(first[i], second[i], alpha);
		}
		
		return combined;
	}
	
	public static final double[][] truncateMatrix(double[][] matrix, 
			int decimalPlaces) {
		double inverse = 1.0/decimalPlaces;
		
		int rows = matrix.length;
		double[][] newMatrix = new double[rows][];
		
		for (int i=0; i<rows; i++) {
			int cols = matrix[i].length;
			newMatrix[i] = new double [cols];
			
			for (int j=0; j<cols; j++) {
				int value = (int)(matrix[i][j] * decimalPlaces); 
				newMatrix[i][j] = value * inverse;
			}
		}
		
		return newMatrix;
	}
	
	/**
	 * Creates a String representation of the provided String matrix.
	 * @param chains the provided String matrix.
	 * @return a String representation of the provided String matrix.
	 */
	public final static String matrixToString(String[][] chains) {
		String result="";
		
		if(chains.length>0) {
			result+=ArrayFunctions.arrayToString(chains[0]);
		}
		for (int i=1; i<chains.length; i++) {
			result+=";"+ArrayFunctions.arrayToString(chains[i]);
		}
		
		return result;
	}
	
	/**
	 * Creates a String representation of the provided 3D String matrix.
	 * @param chains the provided String matrix.
	 * @returna String representation of the provided String matrix.
	 */
	public final static String matrix3dToString(String[][][] chains) {
		String result="";
		
		if(chains.length>0) {
			result+=matrixToString(chains[0]);
		}
		for (int i=1; i<chains.length; i++) {
			result+=":"+matrixToString(chains[i]);
		}
		
		return result;
	}
	
	/**
	 * Represents a 4D matrix of double values into a String, separating
	 * every element with a '$' character.
	 * @param values- the 4D double matrix to be represented as a String
	 * @return The String representing the 4D matrix.
	 */
	public final static String matrix4dToString(double[][][][] values) {
		String result="";
		if(values.length>0) {
			result+=matrix3dToString(values[0]);
		}
		for (int i=1; i<values.length; i++) {
			result+="$"+matrix3dToString(values[i]);
		}
		return result;
	}
	
	/**
	 * Represents a 3D matrix of double values into a String, separating
	 * every element with a ':' character.
	 * @param values- the 3D double matrix to be represented as a String
	 * @return The String representing the 3D matrix.
	 */
	public final static String matrix3dToString(double[][][] values) {
		String result="";
		if(values.length>0) {
			result+=matrixToString(values[0]);
		}
		for (int i=1; i<values.length; i++) {
			result+=":"+matrixToString(values[i]);
		}
		return result;
	}
	
	/**
	 * Represents a matrix of double values into a String, separating
	 * every element with a ';' character.
	 * @param values the double matrix to be represented as a String
	 * @return the String representing the matrix.
	 */
	public final static String matrixToString(double[][] values) {
		String result="";
		if(values.length>0) {
			result+=ArrayFunctions.arrayToString(values[0]);
		}
		for (int i=1; i<values.length; i++) {
			result+=";"+ArrayFunctions.arrayToString(values[i]);
		}
		return result;
	}
	
	/**
	 * Represents a matrix of integer values into a String, separating
	 * every element with a ';' character.
	 * @param values the integer matrix to be represented as a String
	 * @return the String representing the matrix.
	 */
	public final static String matrixToString(int[][] values) {
		String result="";
		if(values.length>0) {
			result+=ArrayFunctions.arrayToString(values[0], ",");
		}
		for (int i=1; i<values.length; i++) {
			result+=";"+ArrayFunctions.arrayToString(values[i], ",");
		}
		return result;
	}

	/**
	 * Represents a 3D matrix of byte values into a String, separating
	 * every element with the given character.
	 * @param values - the byte 3D matrix to be represented as a String
	 * @param separator - the char selected for separating the arrays values.
	 * @return The String representing the byte array.
	 */
	public final static String matrix3dToString(byte[][][] values, char separator) {
		String result="";
		if(values.length>0) {
			result+=matrixToString(values[0], ';');
		}
		for (int i=1; i<values.length; i++) {
			result+=separator+matrixToString(values[i], ';');
		}
		return result;
	}
	
	/**
	 * Represents a matrix of byte values into a String, separating
	 * every element with the given character.
	 * @param values the byte matrix to be represented as a String
	 * @param separator the char selected for separating the arrays values.
	 * @return The String representing the byte array.
	 */
	public final static String matrixToString(byte[][] values, char separator) {
		String result="";
		if(values.length>0) {
			result+=ArrayFunctions.arrayToString(values[0], ',');
		}
		for (int i=1; i<values.length; i++) {
			result+=separator+ArrayFunctions.arrayToString(values[i], ',');
		}
		return result;
	}
	
	/**
	 * Scales the provided 3d double matrix using the given scale value. This
	 * value is applied to every position in the matrix.
	 * @param matrix the provided 3d double matrix
	 * @param scaleValue the given scale value
	 * @return the scaled 3d double matrix
	 */
	public final static double [][][] scaleDouble3DMatrix(double[][][] matrix, double scaleValue) {
		for (int i=0; i<matrix.length; i++) {
			scaleDoubleMatrix(matrix[i], scaleValue);
		}
		return matrix;
	}
	
	/**
	 * Scales the provided double matrix using the given scale value. This
	 * value is applied to every position in the matrix.
	 * @param matrix the provided double matrix
	 * @param scaleValue the given scale value
	 */
	public final static double [][] scaleDoubleMatrix(double[][] matrix, double scaleValue) {
		for (int i=0; i<matrix.length; i++) {
			ArrayFunctions.scaleDoubleArray(matrix[i], scaleValue);
		}
		return matrix;
	}
	
	/**
	 * Scales the provided integer matrix using the given scale value. This
	 * value is applied to every position in the matrix.
	 * @param matrix the provided integer matrix
	 * @param scaleValue the given scale value
	 */
	public final static void scaleIntMatrix(int[][] matrix, double scaleValue) {
		for (int i=0; i<matrix.length; i++) {
			ArrayFunctions.scaleIntArray(matrix[i], scaleValue);
		}
	}
	
	/**
	 * Scales a copy of the provided integer matrix using the given scale value. 
	 * This value is applied to every position in the copied matrix.
	 * @param matrix the provided integer matrix
	 * @param scaleValue the given scale value
	 * @return the scaled copy of the matrix
	 */
	public final static int[][] scaleCopyIntMatrix(int[][] matrix, double scaleValue) {
		int[][] scaled = new int[matrix.length][];
		for (int i=0; i<matrix.length; i++) {
			scaled[i]=ArrayFunctions.scaleCopyIntArray(matrix[i], scaleValue);
		}
		return scaled;
	}
	
	/**
	 * Scales the provided integer 3d matrix using the given scale value. This
	 * value is applied to every position in the 3d matrix.
	 * @param matrix the provided integer 3d matrix
	 * @param scaleValue the given scale value
	 * @return the scaled copy of the 3d matrix
	 */
	public final static int[][][] scaleCopyInt3dMatrix(int[][][] matrix, double scaleValue) {
		int [][][] scaled = new int[matrix.length][][];
		for (int i=0; i<matrix.length; i++) {
			scaled[i]=MatrixFunctions.scaleCopyIntMatrix(matrix[i], scaleValue);
		}
		return scaled;
	}
	
	/**
	 * Copies and scales the given 5d double matrix using the given scale value. This
	 * value is applied to every position in the matrix.
	 * @param matrix the provided 5d double matrix
	 * @param scaleValue the given scale value
	 * @return a new 5d double matrix containing the scaled values from the given 
	 * matrix.
	 */
	public final static double[][][][][] scaleCopyOfDouble5dMatrix(double[][][][][] matrix, double scaleValue) {
		double[][][][][] copy = new double[matrix.length][][][][];
		for (int i=0; i<matrix.length; i++) {
			copy[i]=scaleCopyOfDouble4dMatrix(matrix[i], scaleValue);
		}
		return copy;
	}
	
	/**
	 * Copies and scales the given 4d double matrix using the given scale value. This
	 * value is applied to every position in the matrix.
	 * @param matrix the provided 4d double matrix
	 * @param scaleValue the given scale value
	 * @return a new 4d double matrix containing the scaled values from the given 
	 * matrix.
	 */
	public final static double[][][][] scaleCopyOfDouble4dMatrix(double[][][][] matrix, double scaleValue) {
		double[][][][] copy = new double[matrix.length][][][];
		for (int i=0; i<matrix.length; i++) {
			copy[i]=scaleCopyOfDouble3dMatrix(matrix[i], scaleValue);
		}
		return copy;
	}
	
	/**
	 * Copies and scales the given 3d double matrix using the given scale value. This
	 * value is applied to every position in the matrix.
	 * @param matrix the provided 3d double matrix
	 * @param scaleValue the given scale value
	 * @return a new 3d double matrix containing the scaled values from the given 
	 * matrix.
	 */
	public final static double[][][] scaleCopyOfDouble3dMatrix(double[][][] matrix, double scaleValue) {
		double[][][] copy = new double[matrix.length][][];
		for (int i=0; i<matrix.length; i++) {
			copy[i]=scaleCopyOfDoubleMatrix(matrix[i], scaleValue);
		}
		return copy;
	}
	
	/**
	 * Copies and scales the given double matrix using the given scale value. This
	 * value is applied to every position in the matrix.
	 * @param matrix the provided double matrix
	 * @param scaleValue the given scale value
	 * @return a new double matrix containing the scaled values from the given 
	 * matrix.
	 */
	public final static double[][] scaleCopyOfDoubleMatrix(double[][] matrix, double scaleValue) {
		double[][] copy = new double[matrix.length][];
		for (int i=0; i<matrix.length; i++) {
			copy[i]=ArrayFunctions.scaleCopyOfDoubleArray(matrix[i], scaleValue);
		}
		return copy;
	}
	
	/**
	 * Copies and scales the given double matrix using the given scale value. This
	 * value is applied to every position in the matrix.
	 * @param matrix the provided double matrix
	 * @param scaleValue the given scale value
	 * @return a new double matrix containing the scaled values from the given 
	 * matrix.
	 */
	public final static int[][] scaleCopyOfDoubleMatrixAndTruncateToInt(double[][] matrix, double scaleValue) {
		int[][] copy = new int[matrix.length][];
		for (int i=0; i<matrix.length; i++) {
			copy[i]=ArrayFunctions.scaleCopyOfDoubleArrayAndTruncateToInt(matrix[i], scaleValue);
		}
		return copy;
	}
	
	/**
	 * Averages the given matrix using the first index to merge the 3d matrixes.
	 * @param matrix a mc matrix following [mc][][][] structure
	 * @return the averaged mc matrix.
	 */
	public final static double [][][] averageMCDoubleMatrix(double [][][][] matrix) {
		// Matrix dimensions are supposed to be equals for every 3d matrix.
		int mc = matrix.length;
		int a = matrix[0].length;
		int b = matrix[0][0].length;
		int c = matrix[0][0][0].length;
		
		double [][][] averaged = new double [a][b][c];
		
		for (int i = 0; i<a; i++) {
			for (int j = 0; j<b; j++) {
				for (int k = 0; k<c; k++) {
				
					double [] aux = new double [mc]; 
					
					for (int m =0; m<mc; m++) {
						aux[m] = matrix[m][i][j][k];
					}
					
					averaged[i][j][k] = StatUtils.mean(aux);
				}
			}
		}
		
		return averaged;
	}
	
	/**
	 * Averages the given matrix using the first index to merge the 3d matrixes.
	 * @param matrix a mc matrix following [mc][][][] structure
	 * @return the averaged mc matrix.
	 */
	public final static double [][][] averageMCIntMatrix(int [][][][] matrix) {
		// Matrix dimensions are supposed to be equals for every 3d matrix.
		int mc = matrix.length;
		int a = matrix[0].length;
		int b = matrix[0][0].length;
		int c = matrix[0][0][0].length;
		
		double [][][] averaged = new double [a][b][c];
		
		for (int i = 0; i<a; i++) {
			for (int j = 0; j<b; j++) {
				for (int k = 0; k<c; k++) {
				
					double [] aux = new double [mc]; 
					
					for (int m =0; m<mc; m++) {
						aux[m] = matrix[m][i][j][k];
					}
					
					averaged[i][j][k] = StatUtils.mean(aux);
				}
			}
		}
		
		return averaged;
	}
	
	/**
	 * Computes and returns the sum of every element of the given integer matrix.
	 * @param matrix the integer matrix whose sum is being computed
	 * @return the sum of every element of the given integer matrix
	 */
	public final static int addMatrix(int[][] matrix) {
		int sum = 0;
		int numRows = matrix.length;
		for (int i = 0; i < numRows; i++) {
			sum += ArrayFunctions.addArraySegment(matrix[i], 0, matrix[i].length);
		}
		return sum;
	}
	
	public final static int addMatrix(double[][] matrix) {
		int sum = 0;
		int numRows = matrix.length;
		for (int i = 0; i < numRows; i++) {
			sum += ArrayFunctions.addArraySegment(matrix[i], 0, matrix[i].length);
		}
		return sum;
	}
	
	/**
	 * Computes and returns the sum of every element of the given integer matrix.
	 * @param matrix the integer matrix whose sum is being computed
	 * @return the sum of every element of the given integer matrix
	 */
	public final static int add3dMatrix(int[][][] matrix) {
		int sum = 0;
		int numRows = matrix.length;
		for (int i = 0; i < numRows; i++) {
			sum += MatrixFunctions.addMatrix(matrix[i]);
		}
		return sum;
	}
	
	public final static int add3dMatrix(double[][][] matrix) {
		int sum = 0;
		int numRows = matrix.length;
		for (int i = 0; i < numRows; i++) {
			sum += MatrixFunctions.addMatrix(matrix[i]);
		}
		return sum;
	}
	
	/**
	 * Adds a sub array from the given integer matrix using provided column 
	 * identifier and sub array indexes.
	 * @param matrix a integer matrix
	 * @param column the index of the array containing the sub array
	 * @param begin the first index of the sub array
	 * @param end the last index of the sub array
	 * @return the resulting value of adding a sub array from the given integer matrix using provided column 
	 * identifier and sub array indexes.
	 */
	public final static int addMatrixColumnSegment(
			int[][] matrix, int column, int begin, int end) {
		int sum = 0;
		for (int i = begin; i < end; i++) {
			sum += matrix[i][column];
		}
		return sum;
	}
	
	/**
	 * Creates a copy of the given integer matrix.
	 * @param matrix an integer matrix
	 * @return a copy of the given integer matrix.
	 */
	public final static int[][] copyMatrix(int[][] matrix) {
		int rows = matrix.length;
		int[][] newMatrix = new int [rows][];
		
		for (int i=0; i<rows; i++) {
			newMatrix[i] = Arrays.copyOf(matrix[i], matrix[i].length);
		}
		
		return newMatrix;
	}
	
	/**
	 * Creates a copy of the given 3d integer matrix.
	 * @param matrix a 3d integer matrix
	 * @return a copy of the given 3d integer matrix.
	 */
	public final static int[][][] copyMatrix(int[][][] matrix) {
		int rows = matrix.length;
		int[][][] newMatrix = new int [rows][][];
		
		for (int i=0; i<rows; i++) {
			newMatrix[i] = copyMatrix(matrix[i]);
		}
		
		return newMatrix;
	}
	
	/**
	 * Creates a copy of the given double matrix.
	 * @param matrix a double matrix
	 * @return a copy of the given double matrix.
	 */
	public final static double[][] copyMatrix(double[][] matrix) {
		int rows = matrix.length;
		double[][] newMatrix = new double [rows][];
		
		for (int i=0; i<rows; i++) {
			newMatrix[i] = Arrays.copyOf(matrix[i], matrix[i].length);
		}
		
		return newMatrix;
	}
	
	/**
	 * Creates a copy of the given 3d double matrix.
	 * @param matrix a 3d double matrix
	 * @return a copy of the given 3d double matrix.
	 */
	public final static double[][][] copyMatrix(double[][][] matrix) {
		int rows = matrix.length;
		double[][][] newMatrix = new double [rows][][];
		
		for (int i=0; i<rows; i++) {
			newMatrix[i] = copyMatrix(matrix[i]);
		}
		
		return newMatrix;
	}
	
	/**
	 * Creates a copy of the given 4d double matrix.
	 * @param matrix a 4d double matrix
	 * @return a copy of the given 4d double matrix.
	 */
	public final static double[][][][] copyMatrix(double[][][][] matrix) {
		int rows = matrix.length;
		double[][][][] newMatrix = new double [rows][][][];
		
		for (int i=0; i<rows; i++) {
			newMatrix[i] = copyMatrix(matrix[i]);
		}
		
		return newMatrix;
	}
	
	/**
	 * Converts the given 4d integer matrix into a double one.
	 * @param matrix a 4d integer matrix
	 * @return the resulting 4d double matrix
	 */
	public final static double[][][][] intToDouble(int[][][][] matrix) {
		double[][][][] doubleMatrix = new double[matrix.length][][][];
		for(int i = 0; i < matrix.length; i++) {
			doubleMatrix[i]=intToDouble(matrix[i]);
		}
		return doubleMatrix;	
	}
	
	/**
	 * Converts the given 3d integer matrix into a double one.
	 * @param matrix a 3d integer matrix
	 * @return the resulting 3d double matrix
	 */
	public final static double[][][] intToDouble(int[][][] matrix) {
		double[][][] doubleMatrix = new double[matrix.length][][];
		for(int i = 0; i < matrix.length; i++) {
			doubleMatrix[i]=intToDouble(matrix[i]);
		}
		return doubleMatrix;	
	}

	/**
	 * Converts the given integer matrix into a double one.
	 * @param matrix a integer matrix
	 * @return the resulting double matrix
	 */
	public final static double[][] intToDouble(int[][] matrix) {
		double[][] doubleMatrix = new double[matrix.length][];
		for(int i = 0; i < matrix.length; i++) {
			doubleMatrix[i]=ArrayFunctions.intToDouble(matrix[i]);
		}
		return doubleMatrix;	
	}
	
	/**
	 * Converts the given String matrix into a double one.
	 * @param matrix a String matrix
	 * @return the resulting double matrix
	 */
	public final static double[][] stringToDouble(String[][] matrix) {
		double[][] doubleMatrix = new double[matrix.length][];
		for(int i = 0; i < matrix.length; i++) {
			doubleMatrix[i]=ArrayFunctions.stringToDouble(matrix[i]);
		}
		return doubleMatrix;	
	}
	
	/**
	 * Checks if the values of the array (two dim. array) sum up 
	 * {@paramref sum} for each row.
	 * @param array - the array to be checked
	 * @param sum - The array values must sum up to this value.
	 */
	public final static void checkMatrixSum(double[][] array, double sum) {
		double[] sumCalc = new double[array.length];
		for(int i=0; i<array.length; i++) {
			for(int j=0; j<array[i].length; j++) {
				// TODO [KT] Extend the exception thrown by assertion??				
				sumCalc[i] += array[i][j];
			}
			assert(Functions.equals(sumCalc[i],sum, Functions.DOUBLE_EQUALS_DELTA));
		}
	}

	/**
	 * Checks if the array (two dim. array) are in the range 
	 * [{@paramref min},{@paramref max}].
	 * @param array - the array to be checked.
	 * @param min - the minimum value allowed.
	 * @param max - the maximum value allowed.
	 */
	public final static void checkMatrixBoundaries(
		double[][] array, double min, double max
	) {
		for(int i=0; i<array.length; i++) {
			for(int j=0; j<array[i].length; j++) {
				// TODO [KT] Extend the exception thrown by assertion??				
				assert(array[i][j] <= max);
				assert(array[i][j] >= min);				
			}
		}
	}
	
	/**
	 * Checks if the array (three dim. array) are in the range 
	 * [{@paramref min},{@paramref max}].
	 * @param array - the array to be checked.
	 * @param min - the minimum value allowed.
	 * @param max - the maximum value allowed.
	 */
	public final static void checkMatrixBoundaries(
		double[][][] array, double min, double max
	) {
		for(int i=0; i<array.length; i++) {
			for(int j=0; j<array[i].length; j++) {
				for(int k=0; k<array[i][j].length; k++) {	
					// TODO [KT] Extend the exception thrown by assertion??
					assert(array[i][j][k] <= max);
					assert(array[i][j][k] >= min);						
				}
			}
		}
	}
	
	public final static double [][] pasteDoubleMatrixes(
			double[][] first, double[][] second) {
		double[][] result = new double[first.length + second.length][];
		for (int i=0; i<first.length; i++) {
			result[i]=first[i];
		}
		for (int j=first.length; j<first.length+second.length; j++) {
			result[j]=second[j-first.length];
		}
		return result;
	}
	
	public final static double [][][][] pasteDouble4DMatrixes(
			double[][][][] first, double[][][][] second) {
		double[][][][] result = new double[first.length + second.length][][][];
		for (int i=0; i<first.length; i++) {
			result[i]=first[i];
		}
		for (int j=first.length; j<first.length+second.length; j++) {
			result[j]=second[j-first.length];
		}
		return result;
	}
}
