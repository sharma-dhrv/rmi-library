/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi.io;

import java.lang.reflect.Method;

public class RMIRequest {

	private Class objectClass;
	private Method method;
	private Object[] arguments;

	public RMIRequest(Class objectClass, Method method, Object[] arguments) {
		this.objectClass = objectClass;
		this.method = method;
		this.arguments = arguments;
	}
	
	public Class getObjectClass() {
		return objectClass;
	}

	public Method getMethod() {
		return method;
	}

	public Object[] getArguments() {
		return arguments;
	}

}
