/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi.io;

import java.io.Serializable;

public class RMIResponse implements Serializable {
	
	private static final long serialVersionUID = -8899349477943341489L;
	
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
