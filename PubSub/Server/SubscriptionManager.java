import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class which hold all the data structures in the system and implements the core functionality
 */
public class SubscriptionManager {

    HashSet<ClientInfo> active_clients = new HashSet<>(); // Active clients in system [Clients which are currently joined]

    // Concurrent hash set to find for each article which all clients are subscribed. This is a thread safe
    // data structure which is partially locked and hence can be used here in our system
    ConcurrentHashMap<String, HashSet<ClientInfo>> subscriptions  = new ConcurrentHashMap<>();

    // Set of valid categories for article's category to check against
    private Set<String> validCatagories = new HashSet<>(Arrays.asList("Sports", "Lifestyle", "Entertainment",
            "Business", "Technology", "Science",
            "Politics", "Health")
    );

    // check if client is joined
    synchronized boolean isClientActive (ClientInfo clientInfo) {
        return active_clients.contains(clientInfo);
    }

    // add the client joining
    boolean addClient(ClientInfo client) {
        synchronized (active_clients) {
            // check if max number of clients are reached
            if(active_clients.size() > CONSTANTS.MAX_CLIENTS){
                System.out.println("ERROR :: Max Client Exceeded");
                return false;
            }
            // add client
            if (!active_clients.add(client)) {
                System.out.println("INFO :: Client" + client.toString() + " already joined");
                return false;
            }
        }
        System.out.println(client.toString()+" Joined");
        return true;
    }

    // remove leaving client
    boolean removeClient(ClientInfo client) {
        synchronized (active_clients) {
            if (!active_clients.remove(client)) {
                return false;
            }
        }
        System.out.println(client.toString()+" Left");
        return true;
    }

    // Check if an article being published, subscribed or unsubscribed is valid or not
    boolean isArticleValid(String operation, String article) {
        String[] article_array = article.split(";",-1);

        if (article_array.length != 4) {
            System.out.println("ERROR :: Article is not in legal format");
            return false;
        }

        if (article_array[0].isEmpty() && article_array[1].isEmpty() && article_array[2].isEmpty()) {
            System.out.println("ERROR :: All the filters are empty");
            return false;
        }

        if (!article_array[0].isEmpty() && !validCatagories.contains(article_array[0])) {
            System.out.println("ERROR :: " + operation + " category is not valid");
            return false;
        }

        if(operation.equalsIgnoreCase("Publish")) {
            if (article_array[3].isEmpty()) {
                System.out.println("ERROR :: " + operation + " request must contain contents");
                return false;
            }
        } else {
            if (!article_array[3].equals("")) {
                System.out.println("ERROR :: " + operation + " request cannot contain contents");
                return false;
            }
        }

        return true;
    }

    // add client to subscription list of article
    boolean subscribe(ClientInfo client,String article) {
        synchronized (active_clients) {
            // check if client is active
            if (!active_clients.contains(client)) {
                System.out.println("ERROR :: Client" + client.toString() + " not joined");
                return false;
            }
        }

        // check if article is valid
        if (!isArticleValid("Subscription",article)) return false;

        // add subscription
        HashSet<ClientInfo> set = subscriptions.getOrDefault(article, new HashSet<>());
        set.add(client);
        subscriptions.put(article, set);
        System.out.println(client.toString()+" Subscribed to "+article);
        return true;
    }

    // remove client to subscription list of article
    boolean unSubscribe(ClientInfo client,String article) {
        synchronized (active_clients) {
            // check if client is active
            if (!active_clients.contains(client)) {
                System.out.println("ERROR :: Client" + client.toString() + " not joined");
                return false;
            }
        }
        String[] article_array = article.split(";",-1);

        // check if article is valid
        if (!isArticleValid("Un-Subscription",article)) return false;

        // remove subscription
        HashSet<ClientInfo> set = subscriptions.getOrDefault(article, null);
        if(set == null) {
            System.out.println("ERROR :: No subscription found for filters "+article+" in client "+client.toString());
            return false;
        }
        set.remove(client);

        if(set.size() == 0) {
            subscriptions.remove(article);
        }
        System.out.println(client.toString()+" un-subscribed from "+article);
        return true;
    }

    /**
     * Method to generate all 8 possible combinations to match to a more generic subscriptions while we are
     * publishing an article
     */
    HashSet<ClientInfo> getSubscribedClients(String article) {
        HashSet<ClientInfo> result = new HashSet<>();

        String arr[] = article.split(";", -1);
        for (int i = 0; i < (1<<3); i++)
        {
            String item = "";

            for (int j = 0; j < arr.length - 1; j++) {
                if ((i & (1 << j)) > 0) {
                    item += arr[j]+";";
                } else {
                    item += ";";
                }
            }
            if(subscriptions.containsKey(item)) {
                result.addAll(subscriptions.get(item));
            }
        }
        return result;
    }
}
