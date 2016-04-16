/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

import rmi.io.RMIRequest;
import rmi.io.RMIResponse;

public class MethodInvocationTask<T> implements Runnable {

	private Skeleton<T> container;
	private T serverObj;
	private Class<T> serverClass;
	private Socket clientConnection;

	public MethodInvocationTask(Skeleton<T> container, T serverObj, Class<T> serverClass, Socket clientConnection) {
		this.container = container;
		this.serverObj = serverObj;
		this.serverClass = serverClass;
		this.clientConnection = clientConnection;
	}
	
	private Method getMatchingMethod(Method method, Object[] arguments) {
		for (Method m : serverClass.getDeclaredMethods()) {
			Class[] paramTypes = m.getParameterTypes();
			if(method.getName().equals(m.getName()) && arguments.length == paramTypes.length) {
				boolean found = true;
				for (int i = 0; i < arguments.length; i++) {
					if(!paramTypes[i].equals(arguments[i])) {
						found = false;
					}
				}
				
				if(found) {
					return m;
				}
			}
		}
		
		return null;
	}

	@Override
	public void run() {
		ObjectOutputStream outStream;
		ObjectInputStream inStream;
		
		try {
			outStream = new ObjectOutputStream(clientConnection.getOutputStream());
		} catch (IOException e) {
			System.err.println("Failed to get OutputStream from client connection: " + "ServerClass: " + serverClass.getName() + ", "
					+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", " + "Port: " + container.getBindAddress().getPort());
			
			container.service_error(new RMIException(e));
			
			closeConnection();
			return;		// Nothing can be done so simply exit.
		}
		try {
			inStream = new ObjectInputStream(clientConnection.getInputStream());
		} catch (IOException e) {
			System.err.println("Failed to get IntputStream from client connection: " + "ServerClass: " + serverClass.getName() + ", "
					+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", " + "Port: " + container.getBindAddress().getPort());
			
			container.service_error(new RMIException(e));
			
			closeConnection();
			return;		// Nothing can be done so simply exit.
		}
		
		RMIRequest request;
		try {
			request = (RMIRequest) inStream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.err.println("Failed to get read request from client connection: " + "ServerClass: " + serverClass.getName() + ", "
					+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", " + "Port: " + container.getBindAddress().getPort());
			container.service_error(new RMIException(e));
			
			closeConnection();
			return;		// Nothing can be done so simply exit.
		}
		
		
		Class objectClass = request.getObjectClass();
		if(!objectClass.equals(serverClass)) {
			// TODO throw exception as incorrect mapping of skeleton has taken place. Not sure if this exception must also be marshaled.
		}
		
		Method method = request.getMethod();
		Object[] arguments = request.getArguments();
		
		Method matchingMethod = getMatchingMethod(method, arguments);
		if(matchingMethod == null) {
			// TODO throw exception as no method could be found matching the signature. Not sure if this exception must also be marshaled.
		}
		
		RMIResponse response;
		try {
			Object returnValue = matchingMethod.invoke(serverObj, arguments);
			response = new RMIResponse(returnValue);
		} catch (Exception e) {
			response = new RMIResponse(e);
		}
		
		try {
			outStream.writeObject(response);
		} catch (IOException e) {
			System.err.println("Failed to write response to client connection: " + "ServerClass: " + serverClass.getName() + ", "
					+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", " + "Port: " + container.getBindAddress().getPort() + ", "
					+ "Method: " + ", " + "Arguments: ");
			container.service_error(new RMIException(e));
			e.printStackTrace();
		}
		
		closeConnection();
	}
	
	private void closeConnection() {
		try {
			clientConnection.close();
		} catch (IOException e) {
			System.err.println("Failed to close client connection: " + "ServerClass: " + serverClass.getName() + ", "
					+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", " + "Port: " + container.getBindAddress().getPort());
			
			container.service_error(new RMIException(e));
		}
	}

}
