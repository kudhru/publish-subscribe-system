import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

/**
 * Server Driver Code which starts and stops the RMI server
 */
public class Server {

    private Registry registry;      // RMI registry handle
    private PubSubImpl server;      // RMI server handle

    // Method to start the server
    public void start() {
        try {
            // Create Registry
            registry = LocateRegistry.createRegistry(CONSTANTS.rmi_port);
            System.setProperty("java.security.policy", "file:./security.policy");
            System.setSecurityManager(new RMISecurityManager());

            // create server
            server = new PubSubImpl("PubSubServer");

            // bind rmi server ny ma name
            Naming.rebind("PubSubServer", server);

            System.out.println("Server Started");

            // Start the heart listener thread
            HeartBeatListener heartBeatListener = new HeartBeatListener(CONSTANTS.hearbeat_port);
            heartBeatListener.start();
            server.register();
        } catch (Exception e) {
            System.out.println("HelloImpl err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to stop the server [Type stop in terminal of server to stop]
    public void stop() {
        try {
            server.deRegister();
            UnicastRemoteObject.unexportObject(registry,true);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }
    }

    // Main function of driver program
    public static void main(String args[]) {
        // Initialing server
        Server server = new Server();

        // start server
        server.start();

        // check if needs to be stopped
        Scanner scanner = new Scanner(System.in);
        while(true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("Stop")) {
                break;
            }
        }

        // stop the server
        server.stop();

        System.exit(0);
        return;
    }
}
