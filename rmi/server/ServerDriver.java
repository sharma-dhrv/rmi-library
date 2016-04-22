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
			System.out.println("Stopping PingServer skeleton...");
			for(Skeleton s : skeletons) {
				s.stop();
			}
			System.out.println("Done. Bye!");
		}
	}

	public static void main(String[] args) {
		
		int port = Integer.parseInt(args[0]);
		IPingServer server = PingServerFactory.makePingServer();
		Skeleton<IPingServer> skeleton = new Skeleton<IPingServer>(IPingServer.class, server, new InetSocketAddress(port));
		
		ArrayList<Skeleton> skeletons = new ArrayList<>();
		skeletons.add(skeleton);
		
		Runtime.getRuntime().addShutdownHook(new ShutdownThread(skeletons));
		
		try {
			skeleton.start();
			System.out.println("PingServer skeleton is listening on port " + port + "...");
		} catch (RMIException e) {
			e.printStackTrace();
		}
		
	}

}
