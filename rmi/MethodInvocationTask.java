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

	private Method getMatchingMethod(String methodName, Object[] arguments) {
		for (Method m : serverClass.getDeclaredMethods()) {
			Class[] paramTypes = m.getParameterTypes();
			if (methodName.equals(m.getName()) && arguments.length == paramTypes.length) {
				boolean found = true;
				for (int i = 0; i < arguments.length; i++) {
					if (!paramTypes[i].equals(arguments[i])) {
						found = false;
					}
				}

				if (found) {
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
			System.err.println("Failed to get OutputStream from client connection: " + "ServerClass: "
					+ serverClass.getName() + ", " + "IPAddress: " + container.getBindAddress().getAddress().toString()
					+ ", " + "Port: " + container.getBindAddress().getPort());

			container.service_error(new RMIException(e));

			closeConnection();
			return; // Nothing can be done so simply exit.
		}
		try {
			inStream = new ObjectInputStream(clientConnection.getInputStream());
		} catch (IOException e) {
			System.err.println("Failed to get IntputStream from client connection: " + "ServerClass: "
					+ serverClass.getName() + ", " + "IPAddress: " + container.getBindAddress().getAddress().toString()
					+ ", " + "Port: " + container.getBindAddress().getPort());

			container.service_error(new RMIException(e));

			closeConnection();
			return; // Nothing can be done so simply exit.
		}

		RMIRequest request;
		try {
			request = (RMIRequest) inStream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.err.println("Failed to get read request from client connection: " + "ServerClass: "
					+ serverClass.getName() + ", " + "IPAddress: " + container.getBindAddress().getAddress().toString()
					+ ", " + "Port: " + container.getBindAddress().getPort());
			container.service_error(new RMIException(e));

			closeConnection();
			return; // Nothing can be done so simply exit.
		}

		RMIResponse response;
		String className = request.getClassName();
		String methodName = request.getMethodName();
		Object[] arguments = request.getArguments();

		if (className.equals(serverClass.getName())) {

			Method matchingMethod = getMatchingMethod(methodName, arguments);
			if (matchingMethod != null) {
				try {
					Object returnValue = matchingMethod.invoke(serverObj, arguments);
					response = new RMIResponse(returnValue);
				} catch (Exception e) {
					response = new RMIResponse(e);
				}
			} else {
				System.err.println("Failed to find a matching method: " + "ServerClass: " + serverClass.getName() + ", "
						+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", " + "Port: "
						+ container.getBindAddress().getPort() + ", " + "ClientClass: " + className + ", " + "Method: "
						+ methodName + ", " + "Arguments: " + arguments);
				RMIException exception = new RMIException(new NoSuchMethodException("No such remote method."));
				container.service_error(exception);
				response = new RMIResponse(exception);
			}
		} else {
			System.err.println("Failed to find a matching class: " + "ServerClass: " + serverClass.getName() + ", "
					+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", " + "Port: "
					+ container.getBindAddress().getPort() + ", " + "ClientClass: " + className + ", " + "Method: "
					+ methodName + ", " + "Arguments: " + arguments);
			RMIException exception = new RMIException(new ClassNotFoundException("No such remote class."));
			container.service_error(exception);
			response = new RMIResponse(exception);
		}

		try {
			outStream.writeObject(response);
		} catch (IOException e) {
			System.err.println("Failed to write response to client connection: " + "ServerClass: "
					+ serverClass.getName() + ", " + "IPAddress: " + container.getBindAddress().getAddress().toString()
					+ ", " + "Port: " + container.getBindAddress().getPort() + ", " + "ClientClass: " + className + ", "
					+ "Method: " + methodName + ", " + "Arguments: " + arguments);
			container.service_error(new RMIException(e));
		}

		closeConnection();
	}

	private void closeConnection() {
		try {
			clientConnection.close();
		} catch (IOException e) {
			System.err.println("Failed to close client connection: " + "ServerClass: " + serverClass.getName() + ", "
					+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", " + "Port: "
					+ container.getBindAddress().getPort());

			container.service_error(new RMIException(e));
		}
	}

}