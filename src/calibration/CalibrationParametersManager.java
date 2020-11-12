package calibration;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import gnu.trove.list.array.TIntArrayList;
import model.ModelDefinition;
import model.ModelManager;
import util.StringBean;
import util.exception.calibration.CalibrationException;
import util.io.ConfigFileReader;

/**
 * CalibrationParametersManager uses Java Reflection to handle model updating 
 * during calibration process.
 * 
 * @author imoya
 *
 */
public class CalibrationParametersManager {
	
	///////////////////////////////////////////////////////////////////////////
	// Static 
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * ModelManager class definition is needed for finding parameter setters 
	 * using introspection.
	 */
	private final static Class<?> MODEL_CLASS = ModelManager.class;
	
	/**
	 * Inspects ModelManager class looking for a setter method that fits given 
	 * parameter name. Also, the number of arguments for expected setter is 
	 * specified.
	 *  
	 * @param parameterName the name of the parameter whose setter is desired.
	 * @param nesting the number of arguments that desired setter is expected 
	 * to have.
	 * @return the setter method that fits given parameter name and number of 
	 * arguments.
	 * @throws CalibrationException if problems arise while inspecting the 
	 * class looking for specified setter method.
	 */
	public static Method loadSetterMethod(String parameterName, int nesting) 
			throws CalibrationException {

		String setterName = "set" + parameterName;
		
		try {
			Class<?>[] arguments = new Class<?>[nesting + 1];
			for (int i = 0; i < nesting; i++) {
				arguments[i] = int.class;
			}
			arguments[nesting] = double.class;
			return MODEL_CLASS.getMethod(setterName, arguments);
			
		} catch (NoSuchMethodException e) {
			throw new CalibrationException(
				"Undefined setter for property: " + parameterName 
					+ " with nesting "+nesting, e);
		} catch (Exception e) {
			throw new CalibrationException(
				"Exception while looking for setter: " + setterName, e);
		}
	}

	/**
	 * Inspects ModelManager class looking for a setter method that fits given 
	 * parameter name. Also, the number of arguments for expected setter is 
	 * specified.
	 *  
	 * @param parameterName the name of the parameter whose setter is desired.
	 * @param nesting the number of arguments that desired setter is expected 
	 * to have.
	 * @return the setter method that fits given parameter name and number of 
	 * arguments.
	 * @throws CalibrationException if problems arise while inspecting the 
	 * class looking for specified setter method.
	 */
	public static Method loadGetterMethod(String parameterName, int nesting) 
			throws CalibrationException {

		String getterName = "get" + parameterName;
		
		try {
			Class<?>[] arguments = new Class<?>[nesting];
			for (int i = 0; i < nesting; i++) {
				arguments[i] = int.class;
			}
			return MODEL_CLASS.getMethod(getterName, arguments);
			
		} catch (NoSuchMethodException e) {
			throw new CalibrationException(
				"Undefined setter for property: " + parameterName 
					+ " with nesting "+nesting, e);
		} catch (Exception e) {
			throw new CalibrationException(
				"Exception while looking for getter: " + getterName, e);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Instance
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Model manager instance is used for invoking setter methods over model 
	 * definition object.
	 */
	private ModelManager modelManager;
	
	/**
	 * Model definition being modified by the calibration process.
	 */
	private ModelDefinition md;
	
	/**
	 * Involved drivers during the calibration process by segment and driver.
	 */
	private int[][] involvedDrivers;
	
	/**
	 * Indexes of involved drivers as a list of lists.
	 */
	private List<TIntArrayList> involvedDriversList;
	
	/**
	 * List of parameters that are likely to be modified during calibration. 
	 */
	private List<CalibrationParameter> parameters;
	
	/**
	 * Array of unconverted parameters in a key-value fashion.
	 */
	private StringBean[] unconvertedParameters;
	
	private boolean realCoding = false;
	
	/**
	 * Creates a CalibrationParametersManager loading parameters from file 
	 * with given file path.
	 * @param configFilePath file containing parameter description
	 * @throws CalibrationException if problems are found while loading 
	 * parameters.
	 */
	public CalibrationParametersManager(String configFilePath) 
			throws CalibrationException {
		
		modelManager = null;
		loadFromFile(configFilePath);	
	}
	
	/**
	 * Creates a CalibrationParametersManager using the given parameters
	 * via an array of String <k,v> beans.
	 * @param beans array containing parameter description using <k,v>.
	 * @throws CalibrationException if problems are found while loading 
	 * parameters.
	 */
	public CalibrationParametersManager(StringBean[] beans, ModelDefinition md,
			boolean realCoding) 
			throws CalibrationException {
		this.realCoding = realCoding;
		modelManager = null;
		this.md=md;
		loadFromBeans(beans);
	}
	
	/**
	 * Creates a CalibrationParametersManager with an empty parameter list.
	 * @throws CalibrationException
	 */
	public CalibrationParametersManager() {
		modelManager = null;
		parameters = new ArrayList<CalibrationParameter>();
	}
	
	/**
	 * Sets the model manager instance to given object.
	 * @param modelManager a model manager instance.
	 */
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	
	public ModelManager getModelManager() {
		return modelManager;
	}
	
	/**
	 * Sets drivers involved into calibration process. These drivers are 
	 * stored as indexes.
	 * 
	 * @param drivers drivers involved into calibration process.
	 */
	public void setInvolvedDrivers(int[][] drivers) {
		involvedDrivers=drivers;
	}
	
	public void setRealCoding(boolean mode) {
		this.realCoding = mode;
	}
	
	/**
	 * Returns the parameter list.
	 * @return the parameter list.
	 */
	public List<CalibrationParameter> getParameters() {
		return parameters;
	}
	
	/**
	 * Returns the parameter list as a key-value array.
	 * @return the parameter list as a key-value array.
	 */
	public StringBean[] getUnconvertedParameters() {
		return unconvertedParameters;
	}
	
	/**
	 * Returns driver indexes that are being calibrated.
	 * @return driver indexes that are being calibrated.
	 */
	public int[][] getInvolvedDrivers() {
		return involvedDrivers;
	}
	
	/**
	 * Converts given value using the conversion factor of parameter with 
	 * index paramIndex.
	 * @param paramIndex index of the parameter whose value is being converted.
	 * @param paramValue value to be converted.
	 * @return given value value after applying its parameter conversion factor
	 */
	public double unconvertParameterValue(int paramIndex, double paramValue) {
		if(realCoding) return paramValue; 
		else return paramValue / parameters.get(paramIndex).conversionFactor;
	}
	
	/**
	 * Converts given value using the conversion factor of parameter with 
	 * index paramIndex.
	 * @param paramIndex index of the parameter whose value is being converted.
	 * @param paramValue value to be converted.
	 * @return given value value after applying its parameter conversion factor
	 */
	public double convertParameterValue(int paramIndex, double paramValue) {
		if(realCoding) return paramValue; 
		else return (int)Math.round(paramValue * parameters.get(paramIndex).conversionFactor);
	}
		
	/**
	 * Updates parameter's value with index paramIndex using value paramValue.
	 * @param paramIndex the index of the parameter being modified.
	 * @param paramValue new value for the parameter
	 * @return the actual value that the parameter is taking
	 * @throws CalibrationException if exceptions arise when invoking setter 
	 * method using reflection.
	 */
	public double setParameterValue(int paramIndex, double paramValue) 
			throws CalibrationException {
		
		if (modelManager == null) {
			throw new IllegalStateException(
				"Undefined model manager for parameter: " + paramIndex
			);
		}
		
		if (parameters.get(paramIndex).setterMethod == null) {
			throw new IllegalStateException(
				"Undefined setter for parameter: " + paramIndex
			);
		}
		
		try {
			Method setter = parameters.get(paramIndex).setterMethod;
			Method getter = parameters.get(paramIndex).getterMethod; 
			int[] indexes = parameters.get(paramIndex).indexes;
			double value =  unconvertParameterValue(paramIndex, paramValue);

			if (indexes == null) {
				setter.invoke(modelManager, value);	
				return convertParameterValue(paramIndex, (double)getter.invoke(modelManager));
			} 
			switch(indexes.length) {
				case 0:
					throw new IllegalStateException(
						"Undefined indexes for parameter: " + paramIndex
					);
				case 1:
					setter.invoke(modelManager, 
						indexes[0], value
					);	
					return convertParameterValue(paramIndex,
							(double)getter.invoke(modelManager, indexes[0]));
				case 2:
					setter.invoke(modelManager, 
						indexes[0], indexes[1], value
					);	
					return convertParameterValue(paramIndex, 
							(double)getter.invoke(modelManager, indexes[0], indexes[1]));
				case 3:
					setter.invoke(modelManager, 
						indexes[0], indexes[1], indexes[2], value
					);	
					return convertParameterValue(paramIndex, 
							(double)getter.invoke(modelManager, indexes[0],
									indexes[1], indexes[2]));
				case 4:
					setter.invoke(modelManager, 
						indexes[0], indexes[1], indexes[2], indexes[3], value
					);	
					return convertParameterValue(paramIndex, 
							(double)getter.invoke(modelManager, indexes[0],
									indexes[1], indexes[2], indexes[3]));
				default:
					throw new UnsupportedOperationException(
						"Cannot invoke setter with " + indexes.length + " indexes"
					);
			}
		} catch (Exception e) {
			throw new CalibrationException(
				"Unexpected exception while invoking setter method: " 
				+ parameters.get(paramIndex).setterMethod.getName(), e
			);
		} 
	}
	
	/**
	 * Gets the parameter's value with index paramIndex.
	 * @param paramIndex the index of the parameter being modified.
	 * @return the actual value that the parameter is taking
	 * @throws CalibrationException if exceptions arise when invoking setter 
	 * method using reflection.
	 */
	public double getParameterValue(int paramIndex) 
			throws CalibrationException {

		if (modelManager == null) {
			throw new IllegalStateException(
				"Undefined model manager for parameter: " + paramIndex
			);
		}
		
		if (parameters.get(paramIndex).getterMethod == null) {
			throw new IllegalStateException(
				"Undefined getter for parameter: " + paramIndex
			);
		}
		
		try {
			Method getter = parameters.get(paramIndex).getterMethod; 
			int[] indexes = parameters.get(paramIndex).indexes;

			if (indexes == null) {
				return convertParameterValue(paramIndex, (double)getter.invoke(modelManager));
			} 
			switch(indexes.length) {
				case 0:
					throw new IllegalStateException(
						"Undefined indexes for parameter: " + paramIndex
					);
				case 1:
					return convertParameterValue(paramIndex, (double)getter.invoke(
						modelManager, indexes[0]));
				case 2:
					return convertParameterValue(paramIndex, (double)getter.invoke(
						modelManager, indexes[0], indexes[1]));
				case 3:
					return convertParameterValue(paramIndex, (double)getter.invoke(
						modelManager, indexes[0], indexes[1], indexes[2]));
				case 4:
					return convertParameterValue(paramIndex, (double)getter.invoke(
						modelManager, indexes[0], indexes[1], indexes[2], indexes[3]));
				default:
					throw new UnsupportedOperationException(
						"Cannot invoke setter with " + indexes.length + " indexes"
					);
			}
		} catch (Exception e) {
			throw new CalibrationException(
				"Unexpected exception while invoking setter method: " 
				+ parameters.get(paramIndex).setterMethod.getName(), e
			);
		} 
	}
	
	/**
	 * As the end step of the model actualization, drivers are normalized.
	 */
	public void endStep() {
		modelManager.normalizeDrivers();
	}
	
	/**
	 * Creates a new parameter bean and adds it to parameter list.
	 * @param name new parameter name.
	 * @param min new parameter minimum value.
	 * @param max new parameter maximum value.
	 * @param step distance between parameter possible values.
	 * @param nestingIndices indexes needed for updating the parameter.
	 * @throws CalibrationException if problems arise while creating new 
	 * parameter bean. 
	 */
	public void addCalibrationParameter(
			String name, 
			double min,
			double max, 
			double step,
			int[] nestingIndices
			) throws CalibrationException {
		
		if (nestingIndices != null && nestingIndices.length == 0) {
			throw new IllegalArgumentException(
				"Parameter indexes must be null or non-empty"
			);
		}
		
		if(realCoding) {
			//XXX Float encoding ignores step. 
			parameters.add(new CalibrationParameter(
					name, min, max, nestingIndices
				));
		} else {
			int conversionFactor = (int) (1 / step);
			parameters.add(new CalibrationParameter(
				name, conversionFactor, 
				(int) (conversionFactor * min),  
				(int) (conversionFactor * max), 
				nestingIndices
			));
		}
	}
	
	/**
	 * Creates a new parameter bean and adds it to parameter list.
	 * @param name new parameter name.
	 * @param min new parameter minimum value.
	 * @param max new parameter maximum value.
	 * @param step distance between parameter possible values.
	 * @throws CalibrationException if problems arise while creating new 
	 * parameter bean.
	 */
	public void addCalibrationParameter(
			String name, double min, double max, double step
			) throws CalibrationException {
		addCalibrationParameter(name, min, max, step, null);
	}
	
	/**
	 * Loads parameter list form file.
	 * @param filename path for file containing the parameter list 
	 * representation.
	 * @throws CalibrationException if problems are found when creating new 
	 * parameter beans.
	 */
	private void loadFromFile(String filename) throws CalibrationException {
		ConfigFileReader config = new ConfigFileReader(); 
		config.readConfigFile(new File(filename));
		Properties properties = config.getProperties();
		
		Enumeration<?> keyEnum = properties.propertyNames();
		
		// Sort properties by name
		ArrayList<String> keyList = new ArrayList<String>(properties.size());
	    while(keyEnum.hasMoreElements()){
	       keyList.add((String) keyEnum.nextElement());
	    }
	    //TODO [IM] Why the parameter list is sorted?
//	    Collections.sort(keyList);
	    
	    // Create CalibrationParameter list
	    int nParams = properties.size();
		parameters = new ArrayList<CalibrationParameter>(nParams);
		unconvertedParameters = new StringBean[nParams];
		
		for (int i = 0; i < keyList.size(); i++) {
			
			String name = keyList.get(i);
			String value = properties.getProperty(name);
			
			unconvertedParameters[i]=new StringBean(name,value);
			
			if(name.contains("involvedDrivers")) {
				involvedDrivers = ConfigFileReader.parseIntMatrix(value);
				continue;
			}
			
			double[] paramDef = parseParameterValue(value);
			assert(paramDef.length == 3);
			
			double min = paramDef[0];
			double max = paramDef[1];
			double step = paramDef[2];
			
			if (name.indexOf('_') == -1) {
				addCalibrationParameter(name, min, max, step);
			} else {
				addCalibrationParameter(
					parseParameterName(name), 
					min, max, step,
					parseParameterIndices(name)
				);
			}
		}
	}
	
	/**
	 * Loads parameter list form beans array.
	 * @param beans array containing parameter description using <k,v>.
	 * @throws CalibrationException if problems are found when creating new 
	 * parameter beans.
	 */
	private void loadFromBeans(StringBean [] beans) throws CalibrationException {
		// Create CalibrationParameter list
		parameters = new ArrayList<CalibrationParameter>(beans.length);
		for (StringBean sb:beans) {
			
			String name = sb.getKey();
			String value = sb.getValue();
			
			if(name.contains("SegmentDrivers")) {
				includeDrivers(sb);
			}
			
			double[] paramDef = parseParameterValue(value);
			assert(paramDef.length == 3);
			
			double min = paramDef[0];
			double max = paramDef[1];
			double step = paramDef[2];
			
			if (name.indexOf('_') == -1) {
				addCalibrationParameter(name, min, max, step);
			} else {
				addCalibrationParameter(
					parseParameterName(name), 
					min, max, step,
					parseParameterIndices(name)
				);
			}
		}
		manageIncludedDrivers();
	}
	
	/**
	 * Retrieves the list of drivers considered during the optimization. 
	 * @param bean the string bean containing the drivers considered during 
	 * the optimization.
	 * @throws CalibrationException if the string bean is not properly 
	 * formatted, exceptions are thrown.
	 */
	private void includeDrivers(StringBean bean) throws CalibrationException {
		if(involvedDriversList==null) {
			involvedDriversList = new ArrayList<TIntArrayList>();
			for (int s=0; s<md.getNumberOfSegments(); s++) {
				involvedDriversList.add(
						new TIntArrayList(md.getNumberOfAttributes()));
			}
		}
		
		int[] indexes = parseParameterIndices(bean.getKey());
		
		//Parameter detail by segment and attribute
		if(indexes.length==2) {
			int segment = indexes[0];
			int attribute = indexes[1];
			
			involvedDriversList.get(segment).add(attribute);
			
			//Parameter detail by attribute: include to every segment.
		} else if (indexes.length==1) {
			
			int attribute = indexes[0];
			for (int s=0; s<md.getNumberOfSegments(); s++) {
				involvedDriversList.get(s).add(attribute);
			}
			
		} else {
			throw new CalibrationException("The bean: "+ bean.toString()
						+" is not properly formatted.");
		}
	}
	
	/**
	 * Creates the involved drivers structure using the list of 
	 * involved drivers.
	 */
	private void manageIncludedDrivers() {
		if (involvedDriversList!= null) {
			int numberOfSegments = md.getNumberOfSegments();
			
			involvedDrivers = new int [numberOfSegments][];
			for (int s=0; s<numberOfSegments; s++) {
				involvedDrivers[s]=involvedDriversList.get(s).toArray();
			}
		}
	}
	
	/**
	 * Parses full parameter signature and extracts parameter name.
	 * @param propertyKey full signature parameter.
	 * @return parameter name extracted from given parameter signature.
	 */
	private String parseParameterName(String propertyKey) {
		return propertyKey.substring(0, propertyKey.indexOf('_'));
	}
	
	/**
	 * Extracts parameter indexes from parameter signature. These indexes are 
	 * needed for properly calling setter methods. Indexes are represented as 
	 * integer values separated by '_'.
	 * @param propertyKey full signature parameter.
	 * @return indexes extracted from given parameter signature.
	 */
	private int[] parseParameterIndices(String propertyKey) {
		String[] tmpStr = propertyKey.split("_");
		int[] values = new int[tmpStr.length - 1];
		for(int i=1; i<tmpStr.length; i++) {
			values[i-1] = Integer.parseInt(tmpStr[i]);
		}		
		return values;
	}
	
	/**
	 * Parses parameter values from signature: minimum value, maximum value 
	 * and stepping value are extracted.
	 * @param propertyValue parameter values concatenated with ','.
	 * @return parameter values as an array: [min,max,step]
	 */
	public double[] parseParameterValue(String propertyValue) {
		// Important: we use "," to divide columns and ";" to divide rows
		String[] tmpStr = propertyValue.split(",");
		double[] tmpDouble = new double[tmpStr.length];
		// Transform to double		
		for(int i=0; i < tmpStr.length; i++) {
			tmpDouble[i] = Double.parseDouble(tmpStr[i]);
		}
		return tmpDouble;
	}
}
