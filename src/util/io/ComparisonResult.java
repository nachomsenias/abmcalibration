package util.io;

import util.statistics.MonteCarloStatistics;

public class ComparisonResult {

	private int[][] salesByScenarioMC;
	
	private MonteCarloStatistics[] stats;
	
	private String[] scenarioNames;

	public ComparisonResult(int[][] salesByScenarioMC, 
			String[] scenarioNames,
			MonteCarloStatistics[] stats) {
		this.salesByScenarioMC = salesByScenarioMC;
		this.scenarioNames = scenarioNames;
		this.stats = stats;
	}

	public int[][] getSalesByScenarioMC() {
		return salesByScenarioMC;
	}

	public void setSalesByScenarioMC(int[][] salesByScenarioMC) {
		this.salesByScenarioMC = salesByScenarioMC;
	}

	public String[] getScenarioNames() {
		return scenarioNames;
	}

	public void setScenarioNames(String[] scenarioNames) {
		this.scenarioNames = scenarioNames;
	}

	public MonteCarloStatistics[] getStats() {
		return stats;
	}

	public void setStats(MonteCarloStatistics[] stats) {
		this.stats = stats;
	}
}
