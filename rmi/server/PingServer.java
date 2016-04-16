package rmi.server;

import rmi.RMIException;

public class PingServer implements IPingServer {

	public String ping(int idNumber) throws RMIException {
		return ("Pong " + idNumber);
	}

}
