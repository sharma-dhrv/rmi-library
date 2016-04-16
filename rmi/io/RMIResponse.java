/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi.io;

public class RMIResponse {
	
	private Object returnValue;
	private Exception exception;

	public RMIResponse(Object returnValue) {
		this.returnValue = returnValue;
		this.exception = null;
	}
	
	public RMIResponse(Exception exception) {
		this.returnValue = null;
		this.exception = exception;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public Exception getException() {
		return exception;
	}

}
