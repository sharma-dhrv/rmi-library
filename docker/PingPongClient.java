import java.io.*;
import java.net.*;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

public class PingPongClient {

  public static void main(String[] args) {
    String host = (args.length < 1) ? null : args[0];
    String serverName = (args.length < 2) ? null : args[1];
    try {
      Registry registry = LocateRegistry.getRegistry(host);
      PingPongServerInterface stub = (PingPongServerInterface) registry.lookup(serverName);
      String response = stub.ping(1);
      System.out.println("response: " + response);
    } catch (Exception e) {
      System.err.println("Client exception: " + e.toString());
      e.printStackTrace();
    }
  }

}
