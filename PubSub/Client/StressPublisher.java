import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;

/**
 *  Test client where the client will only publish some valid and invalid articles
 */

public class StressPublisher extends Thread {

    private String name;    // Client name
    private int port;       // Port on which client USD server will run to get published articles

    StressPublisher(String name, int port) {
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

            // Publish for 20 seconds
            int n = 40;
            while (n-- > 0) {
                // Publishing some valid articles
                server.Publish("Science;CS;UMN;Colloquium on Monday" , InetAddress.getLocalHost().getHostAddress(),port);
                server.Publish("Sports;;UMN;SuperBowl Activities", InetAddress.getLocalHost().getHostAddress(),port);
                server.Publish("Technology;;;SpaceX Heavy Falcon Lands successfully", InetAddress.getLocalHost().getHostAddress(),port);
                server.Publish("Business;CARLSON;;DOW stocks crashed over weekend", InetAddress.getLocalHost().getHostAddress(),port);

                // More generic case on above
                server.Publish(";;UMN;Snow is still here", InetAddress.getLocalHost().getHostAddress(),port);

                // Publish without contents -- negative test case
                server.Publish(";;UMN;", InetAddress.getLocalHost().getHostAddress(),port);
                Thread.sleep(500);
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
