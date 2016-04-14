package rmi.proxy;

import java.io.*;
import java.net.*;
import java.lang.reflect.*;

public class StubInvocationHandler implements InvocationHandler {
  private InetSocketAddress serverSocketAddress;

  public StubInvocationHandler(InetSocketAddress address) {
    this.serverSocketAddress = address;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Socket socket = new Socket(serverSocketAddress.getAddress(), serverSocketAddress.getPort());

    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

    oos.writeObject(method);
    oos.writeObject(args);

    Object obj = ois.readObject();

    return obj;
  }
}
