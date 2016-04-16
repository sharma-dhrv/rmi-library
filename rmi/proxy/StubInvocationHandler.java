package rmi.proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

import rmi.RMIException;
import rmi.RemoteInterfacePattern;
import rmi.io.RMIRequest;
import rmi.io.RMIResponse;

public class StubInvocationHandler implements InvocationHandler {
	private InetSocketAddress serverSocketAddress;

	public StubInvocationHandler(InetSocketAddress address) {
		this.serverSocketAddress = address;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if (RemoteInterfacePattern.isRemoteMethod(method)) {
				return remoteInvoke(proxy, method, args);
			} else {
				return localInvoke(proxy, method, args);
			}
		} catch (Throwable e) {
			throw e;
		}
	}

	// This is to handle a remote invoke
	public Object remoteInvoke(Object proxy, Method method, Object[] args) throws Throwable {
		Socket socket;
		ObjectOutputStream out;
		ObjectInputStream in;
		RMIRequest request;
		RMIResponse response;

		try {
			socket = new Socket(serverSocketAddress.getAddress(), serverSocketAddress.getPort());
		} catch (IOException e) {
			System.err.println("Failed to connect to server skeleton.");
			throw (Throwable) (new RMIException(e));
		}

		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
		} catch (IOException e) {
			closeConnection(socket);
			System.err.println("Failed to connect to get OutputStream from socket.");
			throw (Throwable) (new RMIException(e));
		}
		try {
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			closeConnection(socket);
			System.err.println("Failed to connect to get InputStream from socket.");
			throw (Throwable) (new RMIException(e));
		}

		System.out.println("Calling Remote Method: " + proxy.getClass().getName() + "." + method.getName() + "(" + args + ")");
		request = new RMIRequest(proxy.getClass().getName(), method.getName(), args);
		try {
		out.writeObject(request);
		} catch (IOException e) {
			closeConnection(socket);
			System.err.println("Failed to write request to socket.");
			throw (Throwable) (new RMIException(e));
		}
		
		try {
			response = (RMIResponse) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			closeConnection(socket);
			System.err.println("Failed to read response from socket.");
			throw (Throwable) (new RMIException(e));
		}
		
		closeConnection(socket);
		
		if(response.getException() == null) {
			return response.getReturnValue();
		} else {
			System.err.println("Remote method execution threw an exception.");
			throw (Throwable) (new RMIException(response.getException()));
		}
	
	}

	// TODO question: where is this required? The logic seemed a bit odd to me. Why the comparison for args length with 1.
	public boolean equals (Object proxy, Method method, Object[] args) {

      if (args.length != 1) {
        return false;
      }

      Object obj = args[0];
      if (obj == null) {
        return false;
      }

      if (!Proxy.isProxyClass(obj.getClass()) || !proxy.getClass().equals(obj.getClass())) {
        return false;
      }

      if (!(Proxy.getInvocationHandler(obj) instanceof StubInvocationHandler)) {
        return false;
      }

      InvocationHandler ih = Proxy.getInvocationHandler(obj);
      if (!serverSocketAddress.equals(((StubInvocationHandler) ih).serverSocketAddress)) {
        return false;
      }

      return true;

    }

	// This is to handle a local invoke
	public Object localInvoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		StubInvocationHandler sih = (StubInvocationHandler) Proxy.getInvocationHandler(proxy);

		if (methodName.equals("equals")) {
			equals(proxy, method, args);
		}

		if (methodName.equals("hashCode")) {
			return sih.serverSocketAddress.hashCode() + proxy.getClass().hashCode();
		}

		return method.invoke(sih, args);
	}
	
	private void closeConnection(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("Failed to close socket.");
		}
	}
}
