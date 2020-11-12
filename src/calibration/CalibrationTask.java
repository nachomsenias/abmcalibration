package calibration;

import calibration.fitness.history.HistoryManager;
import model.Model;
import model.ModelBuilder;
import model.ModelDefinition;
import util.exception.calibration.CalibrationException;
import util.random.RandomizerUtils;

/**
 * Defines a calibration scenario: calibration mode (evaluate or optimize),
 * brands weights, target history, fitness function, etc.
 * 
 * @author imoya
 *
 */
public class CalibrationTask {
	
	/**
	 * Activates model validation when launching calibration scenario. This 
	 * kind of validation involves creating a model instance using given model 
	 * definition object.
	 */
	public final static boolean VALIDATE_MODEL = true;
	
	/**
	 * Skips "validating" the model by creating and running a test instance.
	 */
	public final static boolean SKIP_VALIDATION = false;
	
	/**
	 * Activates generation for initial not optimized model snapshots.
	 */
	public final static boolean INIT_SNAPSHOT = true;
	
	/**
	 * Total history weight on only seasonality mode.
	 */
	public final static double ONLY_HISTORY_FITNESS = 0.0;
	/**
	 * Total history weight on only total sales mode.
	 */
	public final static double ONLY_TOTAL_SALES_FITNESS = 1.0;
	
	/**
	 * Enables snapshot generation for not initial not optimized models.
	 */
	private boolean generateInitSnapshot;
	
	/**
	 * Path for configuration file containing optimization algorithm details. 
	 */
	private String calibrationConfigFile;
	/**
	 * Pat for logs folder.
	 */
	private String calibrationLogFolder;
	/**
	 * Experiment signature for current scenario.
	 */
	private String calibrationSignature;

	/**
	 * Number of Monte-Carlo Iterations for current scenario.
	 */
	private int monteCarloIterations;
	/**
	 * Instance containing model definition for current scenario.
	 */
	private ModelDefinition modelDefinition;
	/**
	 * Parameter manager instance.
	 */
	private CalibrationParametersManager paramManager;
	
	/**
	 * Seed for this scenario.
	 */
	private long calibrationSeed;
	
	/**
	 * Manager masking the fitness evaluation.
	 */
	private HistoryManager manager;
	
	/**
	 * Defines the framework of a model evaluation.
	 * 
	 * @param monteCarloIterations number of Monte-Carlo iterations.
	 * @param modelDefinition model definition instance object.
	 * @param manager handles the fitness evaluation.
	 * @param validateModelDefinition enables validation of given model 
	 * definition object.
	 * @param generateInitSnapshot enables snapshot generation for initial 
	 * (not optimized) model.
	 * @throws CalibrationException if validating this scenario fails.
	 */
	public CalibrationTask(
			int monteCarloIterations,
			ModelDefinition modelDefinition,
			HistoryManager manager,
			boolean validateModelDefinition,
			boolean generateInitSnapshot
		) throws CalibrationException {
		
		this.generateInitSnapshot = generateInitSnapshot;
		this.manager=manager;
		this.monteCarloIterations = monteCarloIterations;

		validateModelDefinition(modelDefinition, validateModelDefinition);
	}
	
	/**
	 * Defines the framework of a model calibration.
	 * 
	 * @param calibrationSeed the seed used for this execution.
	 * @param calibrationSignature the signature for this execution.
	 * @param calibrationConfigFile the algorithm configuration file.
	 * @param calibrationLogFolder the name for the log folder.
	 * @param paramManager the parameter manager for this execution.
	 * @param monteCarloIterations the number of Monte-Carlo iterations 
	 * for each individual evaluation.
	 * @param modelDefinition the definition of the model.
	 * @param manager the history manager handling the fitness evaluation.
	 * @param validateModelDefinition if true, validates the model creating
	 * and simulating it.
	 * @throws CalibrationException this exception is thrown if any problem 
	 * rises during the calibration process.
	 */
	public CalibrationTask(
			long calibrationSeed,
			String calibrationSignature,
			String calibrationConfigFile,
			String calibrationLogFolder,
			CalibrationParametersManager paramManager, 
			int monteCarloIterations,
			ModelDefinition modelDefinition,
			HistoryManager manager,
			boolean validateModelDefinition
		) throws CalibrationException {
		
		this(
			monteCarloIterations,
			modelDefinition, 
			manager,
			validateModelDefinition,
			!INIT_SNAPSHOT
		);

		this.calibrationSeed = calibrationSeed;
		
		checkNotEmpty(calibrationSignature);
		checkNotEmpty(calibrationConfigFile);
		checkNotEmpty(calibrationLogFolder);
		
		this.calibrationSignature = calibrationSignature;
		this.calibrationConfigFile = calibrationConfigFile;
		this.calibrationLogFolder = calibrationLogFolder;
		
		validateParamManager(paramManager);
	}
	
	/**
	 * Validates and assigns given parameter manager instance.
	 * 
	 * @param paramManager manager handling model update during optimization 
	 * process.
	 * @throws CalibrationException if validation fails.
	 */
	private void validateParamManager(
			CalibrationParametersManager paramManager
			) throws CalibrationException {
		
		checkNotNull(paramManager);
		
		if (paramManager.getParameters().size() == 0) {
			throw new CalibrationException(
				"Missing calibration parameters"
			);
		}
		
		this.paramManager = paramManager; 
	}
	
	/**
	 * Validates provided model definition instance.
	 * 
	 * @param modelDefinition instance containing model description and 
	 * definition.
	 * @param validateModelDefinition enables model creating during validation.
	 * @throws CalibrationException if validation fails.
	 */
	private void validateModelDefinition(
			ModelDefinition modelDefinition, 
			boolean validateModelDefinition
			) throws CalibrationException {
		
		checkNotNull(modelDefinition);
	
		// Check number of brands
		if (modelDefinition.getNumberOfBrands() <= 0) {
			throw new CalibrationException(
				"Missing brands in model definition"
			);
		}
		
		// Try to create model 
		try {
			if (validateModelDefinition) {
				// TODO [JB] Use log4j logger
				ModelBuilder mb = modelDefinition.createBuilder();
				System.out.println();
				System.out.println("Validating model definition...");
				Model m=mb.build(
						mb.createBean(), RandomizerUtils.PRIME_SEEDS[0]);
				m.runSilent();
				System.out.println("OK");
				System.out.println();
			}
		} catch (Throwable e) {
			throw new CalibrationException("Unable to create model", e);
		}
		
		this.modelDefinition = modelDefinition;
	}
	
	/**
	 * Validates that given object is not pointing to null.
	 * 
	 * @param parameterValue object instance that is going to be checked.
	 * @throws CalibrationException if validation fails.
	 */
	private void checkNotNull(
			Object parameterValue
			) throws CalibrationException {
		
		if (parameterValue == null) {
			throw new CalibrationException(
				"Undefined value (null)"
			);
		}
	}
	
	/**
	 * Validates that given parameter is not an empty string.
	 * 
	 * @param parameterValue an string object.
	 * @throws CalibrationException if validation fails.
	 */
	private void checkNotEmpty(
			String parameterValue
			) throws CalibrationException {
		
		checkNotNull(parameterValue);
		if (parameterValue.length() == 0) {
			throw new CalibrationException(
				"Undefined value (empty)"
			);
		}
	}

	/**
	 * Returns whenever or not initial snapshot flag is active. 
	 * @return initial snapshot flag value.
	 */
	public boolean isGenerateInitSnapshotActive() {
		return generateInitSnapshot;
	}

	/**
	 * Returns algorithm configuration file path.
	 * @return algorithm configuration file path.
	 */
	public String getCalibrationConfigFile() {
		return calibrationConfigFile;
	}
	
	/**
	 * Returns calibration log folder path.
	 * @return calibration log folder path.
	 */
	public String getCalibrationLogFolder() {
		return calibrationLogFolder;
	}
	
	/**
	 * Returns calibration folder name.
	 * @return calibration folder name.
	 */
	public String getCalibrationLogFolderName() {
		String noLines;
		int realLength = calibrationLogFolder.length();
		
		while(calibrationLogFolder.charAt(realLength-1) == '\\'
				|| calibrationLogFolder.charAt(realLength-1) == '/')
			realLength--;
		
		noLines = calibrationLogFolder.substring(0, realLength);

		int offset = noLines.lastIndexOf('\\');
		offset = Math.max(offset, noLines.lastIndexOf('/'));
		
		return noLines.substring(offset);
	}
	
	/**
	 * Returns current experiment signature.
	 * @return current experiment signature.
	 */
	public String getCalibrationSignature() {
		return calibrationSignature;
	}

	/**
	 * Returns model definition instance.
	 * @return model definition instance.
	 */
	public ModelDefinition getModelDefinition() {
		return modelDefinition;
	}
	
	/**
	 * Returns parameter manager instance.
	 * @return parameter manager instance.
	 */
	public CalibrationParametersManager getCalibrationParametersManager() {
		return paramManager;
	}
	
	/**
	 * Returns calibration seed for current scenario.
	 * @return calibration seed for current scenario.
	 */
	public long getCalibrationSeed() {
		return calibrationSeed;
	}

	/**
	 * Returns number of Monte-Carlo iterations.
	 * @return number of Monte-Carlo iterations.
	 */
	public int getMonteCarloIterations() {
		return monteCarloIterations;
	}
	
	/**
	 * Returns the current instance of the history manager. This manager 
	 * handles both history resources and its fitness evaluation.
	 * @return the current instance of the history manager.
	 */
	public HistoryManager getHistoryManager() {
		return manager;
	}
}
