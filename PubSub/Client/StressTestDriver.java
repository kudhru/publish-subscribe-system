import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *  Client driver to stress test the PubSub Server
 *  It creates equal amount of Client , Client1 , Client2 type clients and run them all parallelly
 */
public class StressTestDriver {

    public static void main(String[] args) {

        // Taking in how many threads in total to create
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter number of clients between 3 and " + CONSTANTS.MAX_CLIENTS);
        int num_clients = Integer.parseInt(scanner.nextLine());
        if(num_clients < 3 || num_clients > CONSTANTS.MAX_CLIENTS) {
            System.out.println("Wrong number of clients");
            System.exit(1);
        }

        // Calculating number of clients of each class
        int first = num_clients/3;
        num_clients -= first;
        int second = num_clients/2;
        int third = num_clients - second;

        List<Thread> idList = new ArrayList<>();
        int port = 6000;

        // Creating and running threads of type Client
        for (int i = 0 ; i < first ; i++) {
            String name = "StressPublisher_";
            Thread thread = new StressSubscriber(name + port  , port);
            thread.start();
            idList.add(thread);
            port++;
        }

        // Creating and running threads of type Client1
        for (int i = 0 ; i < second ; i++) {
            String name = "StressPublisher_";
            Thread thread = new StressPublisher(name + port  , port);
            thread.start();
            idList.add(thread);
            port++;
        }

        // Creating and running threads of type Client2
        for (int i = 0 ; i < third ; i++) {
            String name = "StressSubscriberPublisher_";
            Thread thread = new StressSubscriberPublisher(name + port  , port);
            thread.start();
            idList.add(thread);
            port++;
        }

        // Waiting for all threads to end
        for (Thread thread : idList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("All Test cases completed");
        System.exit(0);
    }
}
