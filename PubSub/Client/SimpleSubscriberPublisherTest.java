import java.net.InetAddress;
import java.rmi.RMISecurityManager;
import java.rmi.Naming;

/**
 * Simple client which will do these steps
 *  1. Start Listener
 *  2. Join Server
 *  3. Start heart beat / ping calls
 *  4. Subscribe to bunch of valid articles
 *  5. Publish some articles. Should not get articles which it published
 *  6. Leave server
 *  7. Close Listener and Ping threads
 *  8. Exit
 */
public class SimpleSubscriberPublisherTest extends Thread {

    private String name;    // Client name
    private int port;       // Port on which client USD server will run to get published articles

    SimpleSubscriberPublisherTest(String name, int port) {
        this.name = name;
        this.port = port;
    }

    @Override
    public void run() {
        try
        {
            System.setProperty("java.security.policy", "file:./security.policy");
            System.setSecurityManager(new RMISecurityManager());

            // Lookup the RMI server by name, , hostname/IP and RMI registry port
            PubSub server = (PubSub) Naming.lookup( "rmi://" + CONSTANTS.server_host + ":" + CONSTANTS.rmi_port + "/PubSubServer");

            // Creating UDP server as Listener to published articles
            Listener listener = new Listener(port,name);
            listener.start();

            // RMI call to join the RMI server
            server.Join(InetAddress.getLocalHost().getHostAddress(), port);

            // Client heart beat thread which will periodically send Ping RMI call to server
            ClientHeartbeatCall clientHeartbeatCall = new ClientHeartbeatCall(server, name);

            // Catching remote exception from Ping in case server is down here so as we can get back control
            clientHeartbeatCall.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    System.out.println(t + " received exception : "+ e.getMessage());
                    System.exit(1);
                }
            });
            clientHeartbeatCall.start();

            // Subscribing to articles
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "Sports;;UMN;");
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "Business;CARLSON;;");

            // Publish some articles
            int n = 20;
            while (n-- > 0) {
                server.Publish("Business;CARLSON;;All persons needs to pay taxes", InetAddress.getLocalHost().getHostAddress(),port);
                // More generic case
                server.Publish(";;UMN;Wait for summer", InetAddress.getLocalHost().getHostAddress(),port);

                server.Publish("Sports;;UMN;Super bowl ended", InetAddress.getLocalHost().getHostAddress(),port);
                Thread.sleep(2000);
            }

            // RMI call to leave the server
            server.Leave(InetAddress.getLocalHost().getHostAddress(), port);

            // Closing all the child threads
            listener.close();
            clientHeartbeatCall.close();
            return;
        }
        catch (Exception e)
        {
            System.out.println(name+" exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
