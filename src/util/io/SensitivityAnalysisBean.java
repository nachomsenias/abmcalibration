package util.io;


public class SensitivityAnalysisBean {

	private int touchpoint;
	private String destFolder;
	private String scenarioName;
	private double min;
	private double max;
	private double step;
	
	public SensitivityAnalysisBean(int index, double min, double max, double step) {
		this.touchpoint = index;
		this.min = min;
		this.max = max;
		this.step = step;
	}
	
	public SensitivityAnalysisBean(int index, String destFolder,
			String scenarioName, double min, double max, double step) {
		this.touchpoint = index;
		this.destFolder = destFolder;
		this.scenarioName = scenarioName;
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public int getTouchpoint() {
		return touchpoint;
	}

	public void setTouchpoint(int touchpoint) {
		this.touchpoint = touchpoint;
	}

	public String getDestFolder() {
		return destFolder;
	}

	public void setDestFolder(String destFolder) {
		this.destFolder = destFolder;
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		this.step = step;
	}
}
