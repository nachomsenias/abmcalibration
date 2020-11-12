package es.ugr.sci2s.soccer.beans;

import util.io.SensitivityAnalysisBean;

public class SensitivityAnalysisConfig {

	private SimulationConfig simulationConfig;
	private SensitivityAnalysisBean[] beans;
	private int brandId;
	
	public SimulationConfig getSimulationConfig() {
		return simulationConfig;
	}
	public void setSimulationConfig(SimulationConfig simulationConfig) {
		this.simulationConfig = simulationConfig;
	}
	public SensitivityAnalysisBean[] getBeans() {
		return beans;
	}
	public void setBeans(SensitivityAnalysisBean[] beans) {
		this.beans = beans;
	}
	public int getBrandId() {
		return brandId;
	}
	public void setBrandId(int brandId) {
		this.brandId = brandId;
	}
}
