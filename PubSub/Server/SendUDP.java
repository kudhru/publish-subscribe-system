import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Utility class to send a published article as UDP message to client
 */
public class SendUDP extends Thread {

    private String data;    // Data to send
    private String host;    // host to send to
    private int port;       // port of host to send to

    SendUDP(String data, String host, int port) {
        this.data = data;
        this.host = host;
        this.port = port;
    }

    public void run() {
        try {
            // Get host address
            InetAddress addr = InetAddress.getByName(host);
            // create a socket
            DatagramSocket datagramSocket = new DatagramSocket();
            // create a packet with data in it
            DatagramPacket packet = new DatagramPacket(data.getBytes(), data.getBytes().length, addr, port);
            // send the packet
            datagramSocket.send(packet);
            // close the socket
            datagramSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
