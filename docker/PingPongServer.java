import java.rmi.*;
import java.rmi.server.*;

public class PingPongServer extends UnicastRemoteObject implements PingPongServerInterface
{

  private static final String serverName = "pingpongserver";

  public PingPongServer() throws RemoteException
  {
    super();
  }

  // This is actually the starting point that registers the object
  public static void main (String[] args)
  {
    try
    {
      // Instantiate an instance of this class -- create a PingPongServer object
      PingPongServer server = new PingPongServer();

      // Tie the name "pingpongserver" to the PingPongServer object we just created
      Naming.rebind (serverName, server);

      // Just print a console message
      System.out.println ("PingPongServer Server ready");
    }
    catch (Exception e)
    {
      // Bad things happen to good people
      e.printStackTrace();
    }
  }

  // This is the one real method
  public String ping(int idNumber) throws RemoteException
  {
    return "Pong " + idNumber;
  }

}
