import java.net.InetAddress;
import java.rmi.RMISecurityManager;
import java.rmi.Naming;

/**
 *  Test client where the client will only subscribe to some valid and invalid articles
 */
public class StressSubscriber extends Thread {

    private String name;    // Client name
    private int port;       // Port on which client USD server will run to get published articles

    StressSubscriber(String name, int port) {
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
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "Science;CS;UMN;");
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "Sports;;UMN;");
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "Technology;;;");
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "Business;CARLSON;;");

            // A generic case of above subscription, no duplicate messages should be received
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, ";;UMN;");

            // Wrong Category subscription
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "XYZ;;;");
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, ";;;");

            // No Contents should be given - Negative test case
            server.Subscribe(InetAddress.getLocalHost().getHostAddress(),port, "Science;;;ABCD");

            // Will receive published articles for 10 seconds
            int n = 10;
            while(n > 0) {
                Thread.sleep(1000);
                n--;
            }

            // Leaving the server before closing client
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
