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
    try {
      if(RMIException.isRMIMethod(method)) {
        return remoteInvoke(proxy, method, args);
      } else {
        return localInvoke(proxy, method, args);
      }
    } catch (Exception e) {
      throw e;
    }
  }

  // This is to handle a remote invoke
  public Object remoteInvoke(Object proxy, Method method, Object[] args) throws Throwable {
    Socket socket;
    Object obj;
    Integer output;

    try {

      socket = new Socket(serverSocketAddress.getAddress(), serverSocketAddress.getPort());

      ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
      out.flush();

      ObjectInputStream in   = new ObjectInputStream(socket.getInputStream());

      out.writeObject(method.getName());
      out.writeObject(method.getParameterTypes());
      out.writeObject(args);

      obj    = in.readObject();
      output = (Integer) in.readObject();

      socket.close();
    } catch (Exception e) {
      throw new RMIException(e);
    } finally {
      if (socket != null) {
        if (!socket.isClosed()) {
          socket.close();
        }
      }

      if (output == Skeleton.ERR) {
        throw (Throwable) obj;
      }
    }

    return obj;
    }

    public boolean equals (Object proxy, Method method, Object []args) {

      if (args.length != 1) {
        return false;
      }

      Object obj = args[0];
      if (obj == null) {
        return false;
      }

      if (!Proxy.isProxyClass(obj.getClass()) || !proxy.getClass().equals(obj.getClass())) {
        return false;
      }

      if (!(Proxy.getInvocationHandler(obj); instanceof StubInvocationHandler)) {
        return false;
      }

      InvocationHandler ih = Proxy.getInvocationHandler(obj);
      if (!serverSocketAddress.equals(((StubInvocationHandler) ih).serverSocketAddress)) {
        return false;
      }

      return true;

    }

    // This is to handle a local invoke
    public Object localInvoke(Object proxy, Method method, Object[] args) throws Throwable {
      String methodName = method.getName();
      StubInvocationHandler sih = (StubInvocationHandler) Proxy.getInvocationHandler(proxy);

      if (methodName.equals("equals")) {
        equals(proxy, method, args);
      }

      if (methodName.equals("hashCode")) {
        return sih.serverSocketAddress.hashCode() + proxy.getClass().hashCode();
      }

      return method.invoke(sih, args);
    }
}
