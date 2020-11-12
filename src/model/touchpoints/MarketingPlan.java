package model.touchpoints;

/**
 * MarketinPlan bean class stores the values for media pressure used
 * at the touch point level.
 * 
 * @author imoya
 *
 */
public class MarketingPlan {
	
	/**
	 * The id of the touch point related to this bean.
	 */
	public final int touchpointid;
	
	/**
	 * The id of the brand related to this bean.
	 */
	public final int brandId;
	
	/**
	 * GRP investment for this plan by segment and week.
	 */
	public final double[][] weeklyPlan;
	
	/**
	 * Emphasis values by marketing campaign and attribute.
	 */
	public final double[][] emphasis;
	
	/**
	 * Quality values by marketing campaign and attribute.
	 */
	public final double[][] quality;
	
	/**
	 * Campaign id by simulation step.
	 */
	public final byte[] creativityByStep;
	
	/**
	 * Creates a marketing bean instance.
	 * 
	 * @param touchpointid - touch point id.
	 * @param brandId - brand id.
	 * @param weeklyPlan - GRP investment.
	 * @param emphasis - emphasis by campaign and attribute.
	 * @param quality - quality by campaign and attribute.
	 * @param creativityByStep - campaign by step.
	 */
	public MarketingPlan(
			int touchpointid, 
			int brandId, 
			double[][] weeklyPlan,
			double[][] emphasis, 
			double[][] quality,
			byte[] creativityByStep
			) {
		this.touchpointid = touchpointid;
		this.brandId = brandId;
		this.weeklyPlan = weeklyPlan;
		this.emphasis = emphasis;
		this.quality = quality;
		this.creativityByStep = creativityByStep;
	}
}
