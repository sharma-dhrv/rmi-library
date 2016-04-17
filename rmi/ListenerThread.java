/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

		while (isActive && listenerSocket != null && !listenerSocket.isClosed()) {
			Socket clientConnection = null;
			try {
				clientConnection = listenerSocket.accept();
			} catch (IOException e) {
				System.err.println("Failed to accept client connection: " + "ServerClass: " + serverClass.getName()
						+ ", " + "IPAddress: " + ((InetSocketAddress)listenerSocket.getLocalSocketAddress()).getHostString() + ", " + "Port: "
						+ ((InetSocketAddress)listenerSocket.getLocalSocketAddress()).getPort());
				e.printStackTrace();
				isActive = container.listen_error(e);
				if(!isActive) {
					cause = (Throwable) e;
					try {
						listenerSocket.close();
					} catch (IOException e1) {
						System.err.println("Failed to close listener socket. Ignoring the exception: " + "ServerClass: " + serverClass.getName()
						+ ", " + "IPAddress: " + ((InetSocketAddress)listenerSocket.getLocalSocketAddress()).getHostString() + ", " + "Port: "
						+ ((InetSocketAddress)listenerSocket.getLocalSocketAddress()).getPort());
						e1.printStackTrace();	
					}
					break;
				}
			}
			
			MethodInvocationTask<T> handler = new MethodInvocationTask<T>(container, serverObj, serverClass, clientConnection);
			threadPool.execute(handler);
		}
		
		try {
			// TODO Remove these Sys Out prints in the final version.
			System.out.println("Shutting down thread pool...");
			threadPool.shutdown();
			threadPool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.out.println("Force terminating thread pool...");
			threadPool.shutdownNow();
		}
		System.out.println("Thread pool terminated.");
		container.stopped(cause);
	}
	
	public void terminate() {
		this.isActive = false;
	}

}
