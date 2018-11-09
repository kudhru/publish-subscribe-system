/**
 * Driver program to start and join 2 simple clients
 */
public class SimpleTestCaseDriver {
    public static void main(String[] args) {

        // Start Client 1
        Thread client1 = new SimpleSubscriberTest("Client 1"  , 4050);
        client1.start();

        // Start Client 2
        Thread client2 = new SimpleSubscriberPublisherTest("Client 2"  , 4060);
        client2.start();


        try {
            // Wait for clients to end
            client1.join();
            client2.join();

            System.out.println("All Test cases ran");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
