package util.exception.view;

public class TerminationException extends Exception {

	// TODO: [JB] Implement richer JNI exceptions 
	// http://monochrome.sutic.nu/2013/09/01/nice-jni-exceptions.html
	
	private static final long serialVersionUID = -8833310591755656941L;
	
	public TerminationException(String message) {
		super(message);
	}
	
	public TerminationException(Throwable cause) {
		super(cause);
	}

	public TerminationException(String message, Throwable cause) {
		super(message, cause);
	}
}
