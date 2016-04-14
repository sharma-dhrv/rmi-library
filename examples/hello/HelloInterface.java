import java.rmi.*;
import java.rmi.server.*;

interface HelloInterface extends Remote
{
  public String ping(int idNumber) throws RemoteException;
}
