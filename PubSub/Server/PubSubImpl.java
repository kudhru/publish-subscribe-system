import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  RMI server class which implements the RMI interface and uses subscription manager as a utility service
 */
public class PubSubImpl extends UnicastRemoteObject implements PubSub {

    private String name;       // Server name
    private SubscriptionManager subscriptionManager = new SubscriptionManager(); // Subscription manager utility handle


    public PubSubImpl(String serverName) throws RemoteException {
        super(CONSTANTS.rmi_port);
        name = serverName;
    }

    // Join RMI implementation
    @Override
    public void Join(String IP, int port) throws RemoteException {
        ClientInfo clientInfo = new ClientInfo(IP, port);
        subscriptionManager.addClient(clientInfo);
    }

    // Leave RMI implementation
    @Override
    public void Leave(String IP, int port) throws RemoteException {
        ClientInfo clientInfo = new ClientInfo(IP, port);
        if(!subscriptionManager.removeClient(clientInfo)){
            System.out.println("ERROR :: Client" + clientInfo.toString() + " is not in the system");
        }
    }

    // Subscribe RMI implementation
    @Override
    public void Subscribe(String IP, int port, String article) throws RemoteException {
        subscriptionManager.subscribe(new ClientInfo(IP, port),article);
    }

    // Un-Subscribe RMI implementation
    @Override
    public void Unsubscribe(String IP, int port, String article) throws RemoteException {
        subscriptionManager.unSubscribe(new ClientInfo(IP, port),article);
    }

    // Publish RMI implementation
    @Override
    public void Publish(String article, String IP, int port) throws RemoteException {
        ClientInfo client = new ClientInfo(IP, port);

        if(subscriptionManager.isArticleValid("Publish", article)) {

            System.out.println(client.toString()+" published "+ article);

            // Create a thread pool of size number of processors on the machine
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            // Execute send UDP message of published article for all subscribed clients
            for (ClientInfo subscribed_client : subscriptionManager.getSubscribedClients(article)) {
                // Check if the client is not the same and client is active
                if (!subscribed_client.equals(client) && subscriptionManager.isClientActive(subscribed_client)) {
                    // Send UDP message parallelly
                    executorService.execute(new SendUDP(article, subscribed_client.getIP(), subscribed_client.getPort()));
                }
            }

            // Tell thread pool to shutdown after all the work is done
            executorService.shutdown();

            // Wait till all the messages have been sent
            while (!executorService.isTerminated()) {
            }
        }

    }

    // Ping RMI implementation to print the string received and return the same
    @Override
    public String Ping(String str) {
        System.out.println("PING  :: " + str);
        return str;
    }


    // Method to send register UDP message to registry server
    public void register() throws UnknownHostException {
        System.out.println("Registering Server");
        InetAddress myadd = InetAddress.getLocalHost();
        InetAddress reg_addr = InetAddress.getByName(CONSTANTS.reg_server);
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            String message = "Register;RMI;" + myadd.getHostAddress() + ";" + CONSTANTS.hearbeat_port + ";" + name + ";" + CONSTANTS.rmi_port;
            DatagramPacket packet = new DatagramPacket(message.getBytes(),message.getBytes().length, reg_addr, CONSTANTS.reg_port);
            datagramSocket.send(packet);
            datagramSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to send de-register UDP message to registry server
    public void deRegister() throws UnknownHostException {
        System.out.println("De-Registering Server");
        InetAddress myadd = InetAddress.getLocalHost();
        InetAddress reg_addr = InetAddress.getByName(CONSTANTS.reg_server);

        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            String message = "Deregister;RMI;" + myadd.getHostAddress() + ";" + CONSTANTS.hearbeat_port ;
            DatagramPacket packet = new DatagramPacket(message.getBytes(),message.getBytes().length, reg_addr, CONSTANTS.reg_port);
            datagramSocket.send(packet);
            datagramSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to send getList UDP message to registry server
    public void getList() throws UnknownHostException {
        System.out.println("Getting list from registry server");
        InetAddress myadd = InetAddress.getLocalHost();
        InetAddress reg_addr = InetAddress.getByName(CONSTANTS.reg_server);

        try {
            DatagramSocket datagramSocket = new DatagramSocket();
                String message = "GetList;RMI;" + myadd.getHostAddress() + ";" + CONSTANTS.hearbeat_port ;
            DatagramPacket packet = new DatagramPacket(message.getBytes(),message.getBytes().length, reg_addr, CONSTANTS.reg_port);
            datagramSocket.send(packet);
            datagramSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
