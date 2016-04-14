import java.rmi.*;
import java.rmi.server.*;

interface HelloInterface extends Remote
{
  public String sayHello(String a) throws RemoteException;
}
