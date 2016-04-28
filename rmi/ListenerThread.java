/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListenerThread<T> extends Thread {

	private Skeleton<T> container;
	private T serverObj;
	private Class<T> serverClass;
	private ServerSocket listenerSocket;
	private boolean isActive;
	private Throwable cause;

	private ExecutorService threadPool = Executors.newCachedThreadPool();

	public ListenerThread(Skeleton<T> container, Class<T> serverClass, T serverObj, ServerSocket listenerSocket) {
		this.container = container;
		this.serverObj = serverObj;
		this.serverClass = serverClass;
		this.listenerSocket = listenerSocket;
		this.isActive = false;
		this.cause = null;
	}

	public void run() {

		isActive = true;
		while (isActive) {
			Socket clientConnection = null;
			try {
				clientConnection = listenerSocket.accept();
			} catch (SocketException e) {
				if (!listenerSocket.isClosed()) {
					System.err.println("Failed to accept client connection: " + "ServerClass: " + serverClass.getName()
							+ ", " + "IPAddress: "
							+ ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getHostString() + ", "
							+ "Port: " + ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getPort());
					isActive = container.listen_error(e);
					if (!isActive) {
						cause = (Throwable) e;
						closeConnection();
						break;
					}
				} else {
					// Do nothing; it is an expected behaviour.
				}
			} catch (IOException e) {
				System.err.println("Failed to accept client connection: " + "ServerClass: " + serverClass.getName()
						+ ", " + "IPAddress: "
						+ ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getHostString() + ", " + "Port: "
						+ ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getPort());
				isActive = container.listen_error(e);
				if (!isActive) {
					cause = (Throwable) e;
					closeConnection();
					break;
				}
			}

			MethodInvocationTask<T> handler = new MethodInvocationTask<T>(container, serverObj, serverClass,
					clientConnection);
			threadPool.execute(handler);
		}
		

		System.out.println("Shutting down thread pool...");
		threadPool.shutdown();
		if (!threadPool.isTerminated()) {
			System.err.println("Force terminating thread pool...");
			threadPool.shutdownNow();
		}

		
		System.out.println("Thread pool terminated.");
		
		closeConnection();
		container.confirmTermination(cause);
	}

	public void terminate() {
		if (this.isActive) {
			this.isActive = false;
			closeConnection();
		} else {
			closeConnection();
			container.confirmTermination(null);
		}
	}

	private void closeConnection() {
		try {
			listenerSocket.close();
		} catch (IOException e) {
			System.err.println("Failed to close listener socket. Ignoring the exception: " + "ServerClass: "
					+ serverClass.getName() + ", " + "IPAddress: "
					+ ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getHostString() + ", " + "Port: "
					+ ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getPort());
			e.printStackTrace();
		}
	}

}
