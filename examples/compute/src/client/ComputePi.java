package client;

import java.math.BigDecimal;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import compute.Compute;

public class ComputePi {
	
	public static String host = "127.0.0.1";
	public static int port = 2001;
	
	public static int decimals = 10;

	public static void main(String[] args) {
		
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		
		try {
			String name = "Compute";
			Registry registry = LocateRegistry.getRegistry(host, port);
			Compute compute = (Compute) registry.lookup(name);
			PiTask task = new PiTask(new Integer(decimals));
			BigDecimal pi = compute.executeTask(task);
			System.out.println("Pi = " + pi);
		} catch (Exception ex) {
			System.out.println("ComputePi exception :-");
			ex.printStackTrace();
		}
		
	}

}
