/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi;

import java.lang.reflect.Method;
import java.lang.*;

public final class RemoteInterfacePattern {
	
	public static boolean isRemoteInterface(Class clazz) {
		boolean allThrowRMIException = true;
		for(Method method : clazz.getDeclaredMethods()) {
			boolean hasRMIException = false;
			for(Class exceptionClass : method.getExceptionTypes()) {
				if(exceptionClass.equals(RMIException.class)) {
					hasRMIException = true;
					break;
				}
			}
			if(!hasRMIException) {
				allThrowRMIException = false;
				break;
			}
		}
		
		return allThrowRMIException;
	}
	
	public static boolean isRemoteMethod(Method method) {
		for(Class exceptionClass : method.getExceptionTypes()) {
			if(exceptionClass.equals(RMIException.class)) {
				return true;
			}
		}
		
		return false;
	}
}
