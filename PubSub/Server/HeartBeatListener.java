import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * UDP server used on server side to receive heart beats from registry sever and other UDP messages
 */
public class HeartBeatListener extends Thread {

    private int callBackPort;  // Port on which UDP server will listen
    private int len = 120;     // article length

    HeartBeatListener(int port){
        callBackPort = port;
    }

    public void run() {
        try {

            byte[] article = new byte[len];

            // Start Listening
            DatagramPacket dataGramPacket = new DatagramPacket(article, article.length);
            DatagramSocket dataGramSocket = new DatagramSocket(callBackPort);

            // Looping to receive any messages
            while(true) {
                dataGramSocket.receive(dataGramPacket);
                String data = new String(dataGramPacket.getData(), 0, dataGramPacket.getLength());

                // Print received message
                System.out.println("Message Received : " + data);

                DatagramPacket packet = new DatagramPacket( data.getBytes(), data.getBytes().length,
                                                            dataGramPacket.getAddress(), dataGramPacket.getPort());
                // Resend the same message
                dataGramSocket.send(packet);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
