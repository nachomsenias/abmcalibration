package calibration;

import java.lang.reflect.Method;

import util.exception.calibration.CalibrationException;

/**
 * CalibrationParameter are beans that represent model parameters to be 
 * optimized in a way that can be understood by the calibrator.
 * 
 * @author imoya
 *
 */
public class CalibrationParameter {
	/**
	 * The name of the parameter.
	 */
	public final String 	parameterName;
	/**
	 * The signature of a parameter is the parameter name with its indexes 
	 * appended.
	 */
	public final String		signature;
	/**
	 * Reflective setter method for updating this parameter at model 
	 * definition object.
	 */
	public final Method 	setterMethod;
	/**
	 * Reflective getter method for getting this parameter from model 
	 * definition object.
	 */
	public final Method 	getterMethod;
	/**
	 * Conversion factor for this parameter.
	 */
	public final double		conversionFactor;
	/**
	 * Minimum value that this parameter may reach.
	 */
	public final double		minValue;
	/**
	 * Maximum value that this parameter can reach.
	 */
	public final double		maxValue;
	/**
	 * Indexes needed for the setter method (for example, brand id, 
	 * touchpoint id...).
	 */
	public final int[]		indexes;
	
	/**
	 * Creates a parameter bean.
	 * 
	 * @param parameterName name of the parameter.
	 * @param conversionFactor conversion factor for the parameter.
	 * @param minValue minimum value for this parameter.
	 * @param maxValue maximum value for this parameter.
	 * @param indexes indexes needed for modifying this parameter using its 
	 * reflective setter method.
	 * @throws CalibrationException if the reflective setter method cannot 
	 * be loaded.
	 */
	public CalibrationParameter(
			String parameterName, 
			int conversionFactor,
			int minValue, 
			int maxValue,
			int[] indexes
			) throws CalibrationException {
		
		this.parameterName = parameterName;
		this.conversionFactor = conversionFactor;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.indexes =  (indexes != null)? indexes.clone() : null;
		this.setterMethod = CalibrationParametersManager.loadSetterMethod(
			parameterName, (indexes != null)? indexes.length : 0
		);
		this.getterMethod = CalibrationParametersManager.loadGetterMethod(
			parameterName, (indexes != null)? indexes.length : 0
		);
		this.signature = formatSignature();
	}
	
	public CalibrationParameter(
			String parameterName, 
			double minValue, 
			double maxValue,
			int[] indexes
			) throws CalibrationException {
		
		this.parameterName = parameterName;
		this.conversionFactor = 1.0;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.indexes =  (indexes != null)? indexes.clone() : null;
		this.setterMethod = CalibrationParametersManager.loadSetterMethod(
			parameterName, (indexes != null)? indexes.length : 0
		);
		this.getterMethod = CalibrationParametersManager.loadGetterMethod(
			parameterName, (indexes != null)? indexes.length : 0
		);
		this.signature = formatSignature();
	}
	
	/**
	 * Formats signature using parameter name and appending its indexes.
	 * @return the formated signature for this parameter.
	 */
	private String formatSignature() {
		StringBuilder buffer = new StringBuilder(parameterName);
		if (indexes != null) {
			for (int i = 0; i < indexes.length; i++) {
				buffer.append('_');
				buffer.append(indexes[i]);
			}
		}
		return buffer.toString();
	}
	
	public String toString() {
		return signature;
	}
}
