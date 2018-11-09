/**
 * Class to hold client info in PubSub System implements functions to hash objects of this class
 */
public class ClientInfo {
    private String IP;
    private int port;

    ClientInfo(String IP , int port) {
        this.IP = IP;
        this.port = port;
    }

    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof ClientInfo)) {
            return false;
        }

        ClientInfo client = (ClientInfo) o;

        return client.IP.equals(IP) &&
                client.port == port;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + IP.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "ClientInfo :: IP : " + this.IP + " PORT : " + this.port;
    }

    public static String toString(String IP , int port) {
        return "ClientInfo :: IP : " + IP + " PORT : " + port;
    }
}
