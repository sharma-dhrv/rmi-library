import java.rmi.*;
import java.rmi.server.*;

public interface PingPongServerInterface extends Remote {
  public String ping(int idNumber) throws RemoteException;
}
