import java.rmi.RemoteException;

/**
 * Client heartbeat class to send Ping to server every 5 seconds
 */
public class ClientHeartbeatCall extends Thread {

    private PubSub server;                  // Server handle
    private String clientName;              // client name [send in ping]
    private volatile Boolean done = false;

    ClientHeartbeatCall (PubSub server, String clientName) {
        this.server = server;
        this.clientName = clientName;
    }

    // Method to close the thread
    public void close() {
        done = true;
    }

    public synchronized void run(){
        try {
            while (!done) {
                // Do RMI call and print the result
                System.out.println("Got ping back :: " + server.Ping(clientName));
                Thread.sleep(5000);

            }
        } catch (RemoteException e) {
            System.out.println("EXCEPTION :: Heartbeat failed. Server is down");
            // re throw remote exception so as to be caught in parent
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
