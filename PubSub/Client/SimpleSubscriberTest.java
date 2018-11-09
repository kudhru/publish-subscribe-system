import java.net.InetAddress;
import java.rmi.RMISecurityManager;
import java.rmi.Naming;

/**
 * Simple client which will do these steps
 *  1. Start Listener
 *  2. Join Server
 *  3. Start heart beat / ping calls
 *  4. Subscribe to bunch of articles both valid and invalid
 *  5. Unsubscribe, Leave and Join Server couple of times [It will not receive message after leave until it joins again]
 *  6. Leave server finally
 *  7. Close Listener and Ping threads
 *  8. Exit
 */
public class SimpleSubscriberTest extends Thread {

    private String name;    // Client name
    private int port;       // Port on which client USD server will run to get published articles

    SimpleSubscriberTest(String name, int port) {
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


            // Subscribing to some valid articles
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "Sports;;UMN;");
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "Business;CARLSON;;");

            // A generic case of above subscription, no duplicate messages should be received
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, ";;UMN;");

            // Wrong Category subscription
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "XYZ;;;");

            // at least one filter should be present during subscribe
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, ";;;");

            // No Contents should be given - Negative test case
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "Science;;;ABCD");

            // Message format is not complete
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, ";;");



            Thread.sleep(5000);

            // Unsubscribe to an article. Will not get published articles related to this
            server.Unsubscribe(InetAddress.getLocalHost().getHostAddress(),port, "Business;CARLSON;;");

            Thread.sleep(5000);

            // Subscribe to previously unsubscribed article. Will start getting published articles related to this
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "Business;CARLSON;;");

            Thread.sleep(5000);

            // Leave and Join server
            server.Leave(InetAddress.getLocalHost().getHostAddress(), port);

            // Client cannot subscribe when they are not joined
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, ";;Minneapolis;");

            Thread.sleep(5000);

            server.Join(InetAddress.getLocalHost().getHostAddress(), port);

            Thread.sleep(5000);

            // Leave finally
            server.Leave(InetAddress.getLocalHost().getHostAddress(), port);

            // Closing all the child threads
            clientHeartbeatCall.close();
            listener.close();

            return;
        }
        catch (Exception e)
        {
            System.out.println(name+" exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
