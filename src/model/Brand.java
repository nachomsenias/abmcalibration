package model;

/**
 * Brand class is used as a bean for representing the attribute value 
 * for each brand, along with the identifier for every brand.
 * 
 * Details like names are not currently taken into account, because 
 * they are managed at view level.
 * 
 * @author imoya
 *
 */
public class Brand {
	/**
	 * Unique brand bean identifier.
	 */
	public final int brandId;
	/**
	 * Attribute values may change over time, so they are defined
	 * by brand and step.
	 */
	public final double[][] attributeValues;
	
	/**
	 * Creates a brand bean with given id and attribute values.
	 * 
	 * @param id an unique brand id
	 * @param productAtts product values by brand and step.
	 */
	public Brand(
			int id,
			double[][] productAtts
			) {
		brandId = id;
		this.attributeValues = productAtts;
	}
}
