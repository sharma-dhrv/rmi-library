/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi.io;

import java.io.Serializable;

public class RMIRequest implements Serializable {
	
	private static final long serialVersionUID = -2462856798587765532L;
	
	private String className;
	private String methodName;
	private Object[] arguments;
	private String[] argumentTypes;

	public RMIRequest(String className, String methodName, Object[] arguments, String[] argumentTypes) {
		this.className = className;
		this.methodName = methodName;
		this.arguments = ((arguments == null) ? new Object[]{} : arguments);
		this.argumentTypes = ((argumentTypes == null) ? new String[]{} : argumentTypes);
	}
	
	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public Object[] getArguments() {
		return arguments;
	}
	
	public String[] getArgumentTypes() {
		return argumentTypes;
	}

}
