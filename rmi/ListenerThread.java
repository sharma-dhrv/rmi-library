package rmi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ListenerThread<T> extends Thread {

	public static final int maxQueueLength = 50;

	private Skeleton<T> container;
	private T serverObj;
	private Class<T> serverClass;
	private InetSocketAddress bindAddress;
	private boolean isActive;
	private ServerSocket listenerSocket;
	private Throwable cause;

	private ExecutorService threadPool = Executors.newCachedThreadPool();

	public ListenerThread(Skeleton<T> container, Class<T> serverClass, T serverObj, InetSocketAddress bindAddress) {
		this.container = container;
		this.serverObj = serverObj;
		this.serverClass = serverClass;
		this.bindAddress = bindAddress;
		this.isActive = false;
		this.listenerSocket = null;
		this.cause = null;
	}

	public void run() {

		try {
			listenerSocket = new ServerSocket(bindAddress.getPort(), maxQueueLength, bindAddress.getAddress());
		} catch (IOException e) {
			System.err.println("Failed to bind Skeleton listener: " + "ServerClass: " + serverClass.getName() + ", "
					+ "IPAddress: " + bindAddress.getAddress().toString() + ", " + "Port: " + bindAddress.getPort());
			e.printStackTrace();
			isActive = container.listen_error(e);
			if(!isActive) {
				cause = (Throwable) e;
			}
		}

		while (isActive) {
			Socket clientConnection = null;
			try {
				clientConnection = listenerSocket.accept();
			} catch (IOException e) {
				System.err.println("Failed to accept client connection: " + "ServerClass: " + serverClass.getName()
						+ ", " + "IPAddress: " + bindAddress.getAddress().toString() + ", " + "Port: "
						+ bindAddress.getPort());
				e.printStackTrace();
				isActive = container.listen_error(e);
				if(!isActive) {
					cause = (Throwable) e;
					listenerSocket.close();
					break;
				}
			}
			
			ServerInvocationHandler<T> handler = new ServerInvocationHandler<T>(container, serverObj, serverClass, clientConnection);
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
