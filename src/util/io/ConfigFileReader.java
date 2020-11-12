package util.io;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;


/**
 * Enables reading parameters from the file.
 * 
 * @author ktrawinski
 *
 */
public class ConfigFileReader {

	// ########################################################################
	// Variables
	// ########################################################################
	
	private Properties properties; 
	
	// ######################################################################## 
	// Constructors
	// ########################################################################
	
	/**
	 * Initializes a new instance of the ConfigFileReader class.
	 */
	public ConfigFileReader() {
		properties = new Properties();
	}
	
	// ########################################################################
	// Methods/Functions	
	// ########################################################################	
	
	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public void readConfigFile(File file) {
		InputStream Input = null;
		
		try {
			Input = new FileInputStream(file.getAbsolutePath());
			
			// load a properties file
			properties.load(Input);
	
			
		} catch (IOException ex) {
			ex.printStackTrace();			
		} finally {
			if (Input != null) {
				try {
					Input.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void readConfigFile(String path) {
		InputStream Input = null;
		
		try {
			Input = new FileInputStream(path);
			
			// load a properties file
			properties.load(Input);
	
			
		} catch (IOException ex) {
			ex.printStackTrace();			
		} finally {
			if (Input != null) {
				try {
					Input.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Gets the given parameter.
	 * @param ParameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public String getParameterString(String ParameterName) {
		return properties.getProperty(ParameterName); 
	}
	
	/**
	 * Gets the given parameter as a boolean.
	 * @param ParameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */	
	public boolean getParameterBoolean(String ParameterName) {
		return Boolean.parseBoolean(properties.getProperty(ParameterName));
	}
	
	/**
	 * Gets the given parameter as an integer.
	 * @param ParameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public int getParameterInteger(String ParameterName) {
		return Integer.parseInt(properties.getProperty(ParameterName));
	}
	
	/**
	 * Gets the given parameter as an double.
	 * @param ParameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public double getParameterDouble(String ParameterName) {
		return Double.parseDouble(properties.getProperty(ParameterName));
	}
	
	/**
	 *  Gets the given parameter as an long.
	 * @param ParameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public double getParameterLong(String ParameterName) {
		return Long.parseLong(properties.getProperty(ParameterName));
	}
	
	/**
	 * Gets the given parameter as a double array.
	 * @param ParameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public double[] getParameterDoubleArray(String ParameterName) {
		String[] tmpStr;
		double[] tmpDouble;
		
		// Important: we use "," to divide columns and ";" to divide rows
		tmpStr = properties.getProperty(ParameterName).split(",");
		tmpDouble = new double[tmpStr.length];
		// Transform to double		
		for(int i=0; i<tmpStr.length; i++) {
			tmpDouble[i] = Double.parseDouble(tmpStr[i]);
		}
		return tmpDouble;
	}
	
	/**
	 * Gets the requested parameter as boolean array.
	 * 
	 * @param ParameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public boolean[] getParameterBooleanArray(String ParameterName) {
		String[] tmpStr;
		boolean[] tmpBool;
		
		// Important: we use "," to divide columns and ";" to divide rows
		tmpStr = properties.getProperty(ParameterName).split(",");
		tmpBool = new boolean[tmpStr.length];
		// Transform to double		
		for(int i=0; i<tmpStr.length; i++) {
			tmpBool[i] = Boolean.parseBoolean(tmpStr[i]);
		}
		return tmpBool;
	}
	
	/**
	 * Gets the given parameter as an integer array.
	 * @param ParameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public int[] getParameterIntegerArray(String ParameterName) {
		String[] tmpStr;
		int[] tmpInteger;
		
		// Important: we use "," to divide columns and ";" to divide rows
		tmpStr = properties.getProperty(ParameterName).split(",");
		tmpInteger = new int[tmpStr.length];
		// Transform to integer		
		for(int i=0; i<tmpStr.length; i++) {
			tmpInteger[i] = Integer.parseInt(tmpStr[i]);
		}
		return tmpInteger;
	}	

	/**
	 * Gets the given parameter as an String array.
	 * @param ParameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public String[] getParameterStringArray(String ParameterName) {
		String[] tmpStr;
		
		// Important: we use "," to divide columns and ";" to divide rows
		tmpStr = properties.getProperty(ParameterName).split(",");
		return tmpStr;
	}
	
	/**
	 * Gets the given parameter as an double two dimensional array. 
	 * @param parameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public String[][][] getParameterStringArray3D(String parameterName) {
		String[] tmpStr;
		String[][] tmpStrTwoDim;
		String[][][] tmpStrThreeDim;		
		int count1;
		int count2;		
		
		// Important: we use "," to divide columns and ";" to divide rows
		// The ":" is used to separate the third dimension
		// Split up by the semicolon
		tmpStr = properties.getProperty(parameterName).split(":");
		count1 = org.apache.commons.lang3.StringUtils.countMatches(tmpStr[0], ";");
		count1++;
		tmpStrTwoDim = new String[tmpStr.length][count1];
		
		// Combine split by semicolon and comma (2D)
		for(int i=0; i<tmpStr.length; i++) {
			tmpStrTwoDim[i] = tmpStr[i].split(";");
		}
		
		count2 = org.apache.commons.lang3.StringUtils.countMatches(tmpStrTwoDim[0][0], ",");
		count2++;
		
		tmpStrThreeDim = new String[tmpStr.length][count1][count2];

		// Combine split by colon, semicolon, and comma (3D)
		for(int i=0; i<tmpStr.length; i++) {
			for(int j=0; j<count1; j++) {
				tmpStrThreeDim[i][j] = tmpStrTwoDim[i][j].split(",");
			}
		}
		
		return tmpStrThreeDim;
	}
	
	/**
	 * Gets the given parameter as an double two dimensional array.
	 * @param parameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public double[][] getParameterDoubleArrayTwoDim(String parameterName) {
		String[] tmpStr;
		String[][] tmpStrTwoDim;
		double[][] tmpDoubleTwoDim;		
		int count;
		
		// Important: we use "," to divide columns and ";" to divide rows
		// Split up by the semicolon
		tmpStr = properties.getProperty(parameterName).split(";");
		count = org.apache.commons.lang3.StringUtils.countMatches(tmpStr[0], ",");
		count++;
		tmpStrTwoDim = new String[tmpStr.length][count];
		tmpDoubleTwoDim = new double[tmpStr.length][count];
		
		// Combine split by semicolon and comma (2D)
		for(int i=0; i<tmpStr.length; i++) {
			tmpStrTwoDim[i] = tmpStr[i].split(",");
		}
		// Transform to double
		for(int i=0; i<tmpStr.length; i++) {
			for(int j=0; j<count; j++) {
				tmpDoubleTwoDim[i][j] = Double.parseDouble(tmpStrTwoDim[i][j]);
			}
		}
		return tmpDoubleTwoDim;
		//return parseMatrix(parameterName);
	}
	
	private double[][] parseMatrix(String text) {
		String[]tmpStr = text.split(";");
		String[][] tmpStrTwoDim;
		double[][] tmpDoubleTwoDim;		
		int count;
		
		count = org.apache.commons.lang3.StringUtils.countMatches(tmpStr[0], ",");
		count++;
		tmpStrTwoDim = new String[tmpStr.length][count];
		tmpDoubleTwoDim = new double[tmpStr.length][count];
		
		// Combine split by semicolon and comma (2D)
		for(int i=0; i<tmpStr.length; i++) {
			tmpStrTwoDim[i] = tmpStr[i].split(",");
		}
		// Transform to double
		for(int i=0; i<tmpStr.length; i++) {
			for(int j=0; j<count; j++) {
				tmpDoubleTwoDim[i][j] = Double.parseDouble(tmpStrTwoDim[i][j]);
			}
		}
		return tmpDoubleTwoDim;
	}
	
	/**
	 * Parses a String containing an integer matrix. The given String will contain 
	 * ',' separating elements and ';' separating rows.
	 * 
	 * @param text a String containing an integer matrix.  
	 * @return parsed integer matrix.
	 */
	public static int[][] parseIntMatrix(String text) {
		
		String[] tmpStr = text.split(";");
		
		int count = tmpStr.length;
		int[][] intMatrix = new int [count][];
		
		for (int i=0; i<count; i++) {
			
			String[] rowTmp = tmpStr[i].split(",");
			int rowCount = rowTmp.length;
			intMatrix[i] = new int [rowCount];
			
			for (int j=0; j<rowCount; j++) {
				intMatrix[i][j]=Integer.valueOf(rowTmp[j]);
			}
		}

		return intMatrix;
	}
	
	/**
	 * Gets the given parameter as an double two dimensional array. 
	 * @param parameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public double[][][][] getParameterDoubleArrayFourDim(String parameterName) {
		String[] tmpStr;
		String[][] tmpStrTwoDim;
		String[][][] tmpStrThreeDim;		
		double[][][] tmpDoubleThreeDim;		
		int count1;
		int count2;		
				
		String [] tmp;
		
		String property = properties.getProperty(parameterName);
		int ocurrences = StringUtils.countMatches(property, "$")+1;
		
		double[][][][] finalRepresentation = new double[ocurrences][][][];
		String original = properties.getProperty(parameterName);
		tmp = original.split("$");
		
		for (int q =0; q<ocurrences; q++) {
		
			// Important: we use "," to divide columns and ";" to divide rows
			// The ":" is used to separate the third dimension
			// Split up by the semicolon
			tmpStr = tmp[q].split(":");
			count1 = org.apache.commons.lang3.StringUtils.countMatches(tmpStr[0], ";");
			count1++;
			tmpStrTwoDim = new String[tmpStr.length][count1];
			
			// Combine split by semicolon and comma (2D)
			for(int i=0; i<tmpStr.length; i++) {
				tmpStrTwoDim[i] = tmpStr[i].split(";");
			}
			
			count2 = org.apache.commons.lang3.StringUtils.countMatches(tmpStrTwoDim[0][0], ",");
			count2++;
			
			tmpStrThreeDim = new String[tmpStr.length][count1][count2];
			tmpDoubleThreeDim = new double[tmpStr.length][count1][count2];
	
			// Combine split by colon, semicolon, and comma (3D)
			for(int i=0; i<tmpStr.length; i++) {
				for(int j=0; j<count1; j++) {
					tmpStrThreeDim[i][j] = tmpStrTwoDim[i][j].split(",");
				}
			}
		
			// Transform to double
			for(int i=0; i<tmpStr.length; i++) {
				for(int j=0; j<count1; j++) {
					for(int k=0; k<count2; k++) {
						tmpDoubleThreeDim[i][j][k] = Double.parseDouble(tmpStrThreeDim[i][j][k]);					
					}
				}
			}
			
			finalRepresentation[q] = tmpDoubleThreeDim;			
		}		
		
		return finalRepresentation;
	}
	
	public double[][][] getParameterSeasonableDoubleArrayThreeDim(
			String parameterName, int chunks, int columns
		) {
		String original = properties.getProperty(parameterName);
		if(original.contains(":")) {
			return getParameterDoubleArrayThreeDim(parameterName);
		}
		else if(original.contains(";")){
			double[][] notSeasonable = getParameterDoubleArrayTwoDim(parameterName);
			
			int elements = notSeasonable.length;
			double [][][] values = new double [elements][][];
			
			for (int i = 0; i<elements; i++) {
				int subelements = notSeasonable[i].length;
				values[i] = new double [subelements][columns];
				for (int j = 0; j<subelements; j++) {
					Arrays.fill(values[i][j], notSeasonable[i][j]);
				}
			}
			
			return values;
			
		} else {
			double[] notSeasonable = getParameterDoubleArray(parameterName);
			int elements = notSeasonable.length;
			
			double [][][] values;
			
			if(elements==chunks) {
				values = new double [1][elements][columns];
				
				for (int i = 0; i<elements; i++) {
					Arrays.fill(values[0][i], notSeasonable[i]);
				}
			} else {
				values = new double [1][1][];
				values[0][0]=notSeasonable;
			}
			
			return values;
		}
			
	}
	
	/**
	 * Gets the given parameter as an double two dimensional array. 
	 * @param parameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public double[][][] getParameterDoubleArrayThreeDim(String parameterName) {
		// Important: we use "," to divide columns and ";" to divide rows
		// The ":" is used to separate the third dimension
		// Split up by the semicolon
		String original = properties.getProperty(parameterName);

		String[] tmpStr = original.split(":");
		
		String[][] tmpStrTwoDim = new String[tmpStr.length][];
		
		// Combine split by semicolon and comma (2D)
		for(int i=0; i<tmpStr.length; i++) {
			tmpStrTwoDim[i] = tmpStr[i].split(";");
		}
		
		String[][][] tmpStrThreeDim = new String[tmpStr.length][][];

		// Combine split by colon, semicolon, and comma (3D)
		for(int i=0; i<tmpStr.length; i++) {
			tmpStrThreeDim[i] = new String[tmpStrTwoDim[i].length][];
			
			for(int j=0; j<tmpStrTwoDim[i].length; j++) {
				tmpStrThreeDim[i][j] = tmpStrTwoDim[i][j].split(",");
			}
		}
		
		double[][][] tmpDoubleThreeDim = new double[tmpStrThreeDim.length][][];		
		
		// Transform to double
		for(int i=0; i<tmpStrThreeDim.length; i++) {
			tmpDoubleThreeDim[i] = new double [tmpStrThreeDim[i].length][];
			for(int j=0; j<tmpDoubleThreeDim[i].length; j++) {
				tmpDoubleThreeDim[i][j] = new double [tmpStrThreeDim[i][j].length];
				for(int k=0; k<tmpStrThreeDim[i][j].length; k++) {
					tmpDoubleThreeDim[i][j][k] = Double.parseDouble(tmpStrThreeDim[i][j][k]);					
				}
			}
		}
		return tmpDoubleThreeDim;
	}
	
	/**
	 * Gets the given parameter as an double two dimensional array. 
	 * @param parameterName - the parameter that we are looking for.
	 * @return - the value of the parameter searched.
	 */
	public byte[][][] getParameterByteArrayThreeDim(String parameterName) {
		String[] tmpStr;
		String[][] tmpStrTwoDim;
		String[][][] tmpStrThreeDim;		
		byte[][][] tmpDoubleThreeDim;		
		int count1;
		int count2;		
		
		// Important: we use "," to divide columns and ";" to divide rows
		// The ":" is used to separate the third dimension
		// Split up by the semicolon
		tmpStr = properties.getProperty(parameterName).split(":");
		count1 = org.apache.commons.lang3.StringUtils.countMatches(tmpStr[0], ";");
		count1++;
		tmpStrTwoDim = new String[tmpStr.length][count1];
		
		// Combine split by semicolon and comma (2D)
		for(int i=0; i<tmpStr.length; i++) {
			tmpStrTwoDim[i] = tmpStr[i].split(";");
		}
		
		count2 = org.apache.commons.lang3.StringUtils.countMatches(tmpStrTwoDim[0][0], ",");
		count2++;
		
		tmpStrThreeDim = new String[tmpStr.length][count1][count2];
		tmpDoubleThreeDim = new byte[tmpStr.length][count1][count2];

		// Combine split by colon, semicolon, and comma (3D)
		for(int i=0; i<tmpStr.length; i++) {
			for(int j=0; j<count1; j++) {
				tmpStrThreeDim[i][j] = tmpStrTwoDim[i][j].split(",");
			}
		}
		
		// Transform to double
		for(int i=0; i<tmpStr.length; i++) {
			for(int j=0; j<count1; j++) {
				for(int k=0; k<count2; k++) {
					tmpDoubleThreeDim[i][j][k] = Byte.parseByte(tmpStrThreeDim[i][j][k]);					
				}
			}
		}
		return tmpDoubleThreeDim;
	}
	
	public List<Double[][]> getMatrixList(String parameterName) {
		String [] rawData = properties.getProperty(parameterName).split(":");
		ArrayList<Double[][]> matrixList = new ArrayList<Double[][]>();
		for (String data:rawData) {
			double[][] tempMatrix=parseMatrix(data);
			Double[][] matrix = new Double[tempMatrix.length][tempMatrix[0].length];
			for(int i=0; i<tempMatrix.length;i++) 
				for (int j=0; j<tempMatrix[i].length;j++)
					matrix[i][j]=tempMatrix[i][j];			
			matrixList.add(matrix);
		}
		return matrixList;
	}
}