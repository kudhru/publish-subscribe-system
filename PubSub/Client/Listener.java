import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * UDP server used on client side to receive published articles from server
 */
public class Listener extends Thread {

    private int callBackPort;  // Port on which UDP server will listen
    private int len = 120;     // article length
    private String name;       // Client name
    private volatile Boolean done = false;

    Listener(int port, String name){
        callBackPort = port;
        this.name =  name;
    }

    // Method to close the thread
    public void close() {
        done = true;
    }

    public synchronized void run() {
        try {

            byte[] article = new byte[len];

            // Start Listening
            DatagramPacket dataGramPacket = new DatagramPacket(article, article.length);
            DatagramSocket dataGramSocket = new DatagramSocket(callBackPort);

            // Loop to receive a message
            while(!done) {
                dataGramSocket.receive(dataGramPacket);
                String data = new String(dataGramPacket.getData(), 0, dataGramPacket.getLength());
                // Print received message
                System.out.println(name + " -- " + data.split(";", -1)[3]);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
