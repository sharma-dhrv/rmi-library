package rmi.server;

import rmi.RMIException;

public interface IPingServer {
	
	public String ping(int idNumber) throws RMIException;
	
}
