package rmi.client;

import java.net.InetSocketAddress;

import rmi.RMIException;
import rmi.Stub;
import rmi.server.IPingServer;

public class ClientDriver {

	public static void main(String[] args) {
		
		IPingServer server = Stub.create(IPingServer.class, new InetSocketAddress(3000));
		
		int idNumber = 10;
		System.out.println("Ping " + idNumber);
		
		try {
			System.out.println(server.ping(idNumber));
		} catch (RMIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
