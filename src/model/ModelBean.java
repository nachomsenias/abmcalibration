package model;

import model.touchpoints.earned.ProductUsage;

public class ModelBean {
	
	final Brand[] brands;
	
	final ProductUsage usage;

	public ModelBean(
			Brand[] brands, ProductUsage usage
			) {
		super();
		this.brands = brands;
		this.usage = usage;
	}
}
