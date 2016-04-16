package rmi.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import rmi.RMIException;
import rmi.Skeleton;

public class ServerDriver {
	
	private static class ShutdownThread extends Thread {
		
		ArrayList<Skeleton> skeletons;
		
		public ShutdownThread(ArrayList<Skeleton> skeletons) {
			this.skeletons = skeletons;
		}
		
		public void run() {
			for(Skeleton s : skeletons) {
				s.stop();
			}
		}
	}

	public static void main(String[] args) {
		
		IPingServer server = PingServerFactory.makePingServer();
		Skeleton<IPingServer> skeleton = new Skeleton<IPingServer>(IPingServer.class, server, new InetSocketAddress(3000));
		
		ArrayList<Skeleton> skeletons = new ArrayList<>();
		skeletons.add(skeleton);
		
		Runtime.getRuntime().addShutdownHook(new ShutdownThread(skeletons));
		
		try {
			skeleton.start();
		} catch (RMIException e) {
			e.printStackTrace();
		}
		
	}

}
