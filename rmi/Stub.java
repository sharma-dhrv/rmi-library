/**
 * @author Karthikeyan Vasuki Balasubramaniam (kvasukib@cs.ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.ArrayList;

import rmi.io.RMIRequest;
import rmi.io.RMIResponse;

/**
 * RMI stub factory.
 *
 * <p>
 * RMI stubs hide network communication with the remote server and provide a
 * simple object-like interface to their users. This class provides methods for
 * creating stub objects dynamically, when given pre-defined interfaces.
 *
 * <p>
 * The network address of the remote server is set when a stub is created, and
 * may not be modified afterwards. Two stubs are equal if they implement the
 * same interface and carry the same remote server address - and would therefore
 * connect to the same skeleton. Stubs are serializable.
 */
public abstract class Stub {
	private static class StubInvocationHandler implements InvocationHandler {
		private InetSocketAddress serverSocketAddress;

		public StubInvocationHandler(InetSocketAddress address) {
			this.serverSocketAddress = address;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			StubInvocationHandler sih = (StubInvocationHandler) Proxy.getInvocationHandler(proxy);

			if (methodName.equals("equals")) {
				equals(proxy);
			}

			if (methodName.equals("hashCode")) {
				return sih.serverSocketAddress.hashCode() + proxy.getClass().hashCode();
			}

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
		@SuppressWarnings("rawtypes")
		private Object remoteInvoke(Object proxy, Method method, Object[] args) throws Throwable {
			Socket socket = null;
			ObjectOutputStream out;
			ObjectInputStream in;
			RMIRequest request;
			RMIResponse response;

			try {
				socket = new Socket();
				socket.connect(serverSocketAddress);
			} catch (IOException e) {
				System.err.println("Failed to connect to server skeleton.");
				closeConnection(socket);
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

			String[] argumentTypes = getArgumentTypes(method);
			//System.out.println("Calling Remote Method: " + method.getDeclaringClass().getName() + "." + method.getName()
			//		+ "(" + args + " : " + argumentTypes.toString() + ")");
			request = new RMIRequest(method.getDeclaringClass().getName(), method.getName(), args, argumentTypes);
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

			if (response.getException() == null) {
				return response.getReturnValue();
			} else {
				//System.out.println("Remote method execution threw an exception." + response.getException().getClass().getName());
				throw (Throwable) response.getException();
			}

		}

		public boolean equals(Object proxy) {

			if (proxy == null) {
				return false;
			}

			if (!Proxy.isProxyClass(proxy.getClass())) {
				//System.out.println("1f");
				return false;
			}

			InvocationHandler sih = Proxy.getInvocationHandler(proxy);
			if (!(sih instanceof StubInvocationHandler)) {
				//System.out.println("2f");
				return false;
			}

			if (!serverSocketAddress.equals(((StubInvocationHandler) sih).serverSocketAddress)) {
				//System.out.println("3f");
				return false;
			}

			return true;

		}

		// This is to handle a local invoke
		private Object localInvoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			StubInvocationHandler sih = (StubInvocationHandler) Proxy.getInvocationHandler(proxy);

			return method.invoke(sih, args);
		}

		private void closeConnection(Socket socket) {
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println("Failed to close socket.");
			}
		}

		private String[] getArgumentTypes(Method method) {
			ArrayList<String> list = new ArrayList<>();
			for (Class clazz : method.getParameterTypes()) {
				list.add(clazz.getName());
			}

			String[] argumentTypes = new String[list.size()];

			return (String[]) list.toArray(argumentTypes);
		}
	}

	/**
	 * Creates a stub, given a skeleton with an assigned adress.
	 *
	 * <p>
	 * The stub is assigned the address of the skeleton. The skeleton must
	 * either have been created with a fixed address, or else it must have
	 * already been started.
	 *
	 * <p>
	 * This method should be used when the stub is created together with the
	 * skeleton. The stub may then be transmitted over the network to enable
	 * communication with the skeleton.
	 *
	 * @param c
	 *            A <code>Class</code> object representing the interface
	 *            implemented by the remote object.
	 * @param skeleton
	 *            The skeleton whose network address is to be used.
	 * @param <T>
	 *						Generic class typeparameter
	 * @return The stub created.
	 * @throws IllegalStateException
	 *             If the skeleton has not been assigned an address by the user
	 *             and has not yet been started.
	 * @throws UnknownHostException
	 *             When the skeleton address is a wildcard and a port is
	 *             assigned, but no address can be found for the local host.
	 * @throws NullPointerException
	 *             If any argument is <code>null</code>.
	 * @throws Error
	 *             If <code>c</code> does not represent a remote interface - an
	 *             interface in which each method is marked as throwing
	 *             <code>RMIException</code>, or if an object implementing this
	 *             interface cannot be dynamically created.
	 */
	public static <T> T create(Class<T> c, Skeleton<T> skeleton) throws UnknownHostException {
		if (c == null || skeleton == null) {
			throw new NullPointerException("Paramaters of create method should be non-null.");
		}

		if (!RemoteInterfacePattern.isRemoteInterface(c)) {
			throw new Error("c is not a remote interface.");
		}

		InetSocketAddress remoteAddress = skeleton.getBindAddress();
		if (remoteAddress == null) {
			throw new IllegalStateException();
		}

		return doCreate(c, remoteAddress);

	}

	/**
	 * Creates a stub, given a skeleton with an assigned address and a hostname
	 * which overrides the skeleton's hostname.
	 *
	 * <p>
	 * The stub is assigned the port of the skeleton and the given hostname. The
	 * skeleton must either have been started with a fixed port, or else it must
	 * have been started to receive a system-assigned port, for this method to
	 * succeed.
	 *
	 * <p>
	 * This method should be used when the stub is created together with the
	 * skeleton, but firewalls or private networks prevent the system from
	 * automatically assigning a valid externally-routable address to the
	 * skeleton. In this case, the creator of the stub has the option of
	 * obtaining an externally-routable address by other means, and specifying
	 * this hostname to this method.
	 *
	 * @param c
	 *            A <code>Class</code> object representing the interface
	 *            implemented by the remote object.
	 * @param skeleton
	 *            The skeleton whose port is to be used.
	 * @param hostname
	 *            The hostname with which the stub will be created.
	 * @param <T>
	 *						Generic class typeparameter
	 * @return The stub created.
	 * @throws IllegalStateException
	 *             If the skeleton has not been assigned a port.
	 * @throws NullPointerException
	 *             If any argument is <code>null</code>.
	 * @throws Error
	 *             If <code>c</code> does not represent a remote interface - an
	 *             interface in which each method is marked as throwing
	 *             <code>RMIException</code>, or if an object implementing this
	 *             interface cannot be dynamically created.
	 */
	public static <T> T create(Class<T> c, Skeleton<T> skeleton, String hostname) {
		if (c == null || skeleton == null || hostname == null) {
			throw new NullPointerException("Paramater of create should be non-null.");
		}

		if (!RemoteInterfacePattern.isRemoteInterface(c)) {
			throw new Error("c is not a remote interface.");
		}

		InetSocketAddress address = skeleton.getBindAddress();
		if (address == null) {
			throw new IllegalStateException("skeleton is not assigned a port.");
		}

		InetSocketAddress remoteAddress = new InetSocketAddress(hostname, address.getPort());

		return doCreate(c, remoteAddress);
		// throw new UnsupportedOperationException("not implemented");
	}

	/**
	 * Creates a stub, given the address of a remote server.
	 *
	 * <p>
	 * This method should be used primarily when bootstrapping RMI. In this
	 * case, the server is already running on a remote host but there is not
	 * necessarily a direct way to obtain an associated stub.
	 *
	 * @param c
	 *            A <code>Class</code> object representing the interface
	 *            implemented by the remote object.
	 * @param address
	 *            The network address of the remote skeleton.
	 * @param <T>
	 *						Generic class typeparameter
	 * @return The stub created.
	 * @throws NullPointerException
	 *             If any argument is <code>null</code>.
	 * @throws Error
	 *             If <code>c</code> does not represent a remote interface - an
	 *             interface in which each method is marked as throwing
	 *             <code>RMIException</code>, or if an object implementing this
	 *             interface cannot be dynamically created.
	 */
	public static <T> T create(Class<T> c, InetSocketAddress address) {
		if (c == null || address == null) {
			throw new NullPointerException("Paramater of create should be non-null.");
		}

		if (!RemoteInterfacePattern.isRemoteInterface(c)) {
			throw new Error("c is not a remote interface.");
		}

		return doCreate(c, address);
	}

	@SuppressWarnings("unchecked")
	private static <T> T doCreate(Class<T> c, InetSocketAddress address) {
		InvocationHandler invocationHandler = new StubInvocationHandler(address);
		T instance = (T) Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[] { c, Serializable.class }, invocationHandler);
		return instance;
	}
}
