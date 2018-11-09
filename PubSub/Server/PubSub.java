import java.rmi.*;

/**
 * RMI interface class shared by both client and server.
 *
 * Server implements this interface and client will get a proxy object extending this interface from Naming Service
 *
 */
public interface PubSub extends java.rmi.Remote{
    void Join (String IP, int port) throws RemoteException;
    void Leave (String IP, int port) throws RemoteException;
    void Subscribe (String IP, int port, String article) throws RemoteException;
    void Unsubscribe (String IP, int port, String article) throws RemoteException;
    void Publish (String article, String IP, int port) throws RemoteException;
    String Ping(String str) throws RemoteException;
}
