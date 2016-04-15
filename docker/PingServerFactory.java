import java.rmi.*;

class PingServerFactory {
  public PingPongServer makePingServer() throws RemoteException
  {
    return new PingPongServer();
  }

}
