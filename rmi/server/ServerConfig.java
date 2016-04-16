package rmi.server;

/*
  * Intergace which stores
  * some configuration information
  * of the RMI server
*/
public interface ServerConfig {
  int MINIMUM_THREAD = 5;
  int MAXIMUM_THREAD = 20;
  int MAX_TCP_CONNECTIONS = 20;
}
