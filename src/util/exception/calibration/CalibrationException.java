package util.exception.calibration;


public class CalibrationException extends Exception {

	private static final long serialVersionUID = 4502811984713670161L;

	public CalibrationException(String message) {
		super(message);
	}

	public CalibrationException(Throwable cause) {
		super(cause);
	}

	public CalibrationException(String message, Throwable cause) {
		super(message, cause);
	}
}
