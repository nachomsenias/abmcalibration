package util.exception.simulation;

/**
 * This "listener" is used for displaying the errors wrapped by the
 * runnable threads. This way, the Controller class can manage risen
 * exceptions.  
 * @author imoya
 */
public class SimulationListener {

	/**
	 * Boolean error flag.
	 */
	public boolean errorFound = false;
	
	/**
	 * Error cause message.
	 */
	public String errorMessage = "";
	
	/**
	 * Logs the error found during simulation.
	 * @param cause the cause behind the exception.
	 */
	public void errorFound(String cause) {
		errorMessage = cause;
		errorFound = true;
	}
}
