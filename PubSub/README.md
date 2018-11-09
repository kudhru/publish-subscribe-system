# Publish Subscribe System
Overview
-----
This is publish subscribe system uses two forms of communication: basic messages using UDP and Java RMI for publishing and subscribing. The PubSub system will allow the publishing of simple formatted “articles”.

We have a few coupled interacting components which we would discuss now:-
1. **CONSTANTS file** :- All the shared system variables are put in CONSTANTS file so as it can be updated at one
point instead of changing the code all over the place. This includes server host, registry host, RMI registry port, registry server port. This will be shared by both clients and server.
2. **RMI Interface** :- RMI interface is implemented in class PubSub which will be shared by both server and client programs.
3. **RMI Registry** :- We are creating RMI registry at server startup using port 1099 defined in CONSTANTS file and assuming the RMI registry and server are running on same machine. [This obviously can be scaled to run on different machines but for simplicity we have made such an assumption.]
4. **HeartBeatListener** :- This is a class for UDP server which will get heartbeat message from registry server and print it on standard output.
5. **Server class** is the driver program of server which start and stops the RMI server. During start it will create a RMI registry, start RMI server and register it with registry server. It will also start the Heartbeat listener for the server. During stop it will deregister the server and close the RMI server and driver program.
6. **PubSubImpl** is the RMI server implementation which will have all the methods for Join, Leave, Subscribe, Unsubscribe, Publish, Ping, etc. It uses classes ClientInfo and SubscriptionManager as defined below.
7. **ClientInfo** is the class to store information about the clients which join and leave the system.
8. **SusbcriptionManager** is class which store data about active clients and subscription info for all the clients. It has methods to add client used by Join, remove client used by Leave, subscribe, unsubscribed which are all called from PubSubImpl. It also has a special function to get all the clients info which qualifies for a publish. For example if there is a publish for “Sports;XYZ;ABC;Contents”. We need to find all clients which may have subscribed for 8 combinations of first three values. After we get this list we can send messages in parallel.
9. **ClientHeartbeatCall** is the class used by client to send periodic messages to server via Ping.
10. **Listener** is the class which act as the UDP server for client to get all the published messages.
11. Client programs and driver programs to test the whole system.

System Implementation
------
1. ClientInfo store is IP and port of the client and implements equals and hashCode function to be used elsewhere.
2. SubscriptionManager maintains a HashSet for active clients which is updated on Join and Leave by synchronized calls holding lock for very less period of time. The rationale is that a time very less number of clients will be leaving and joining with respect to subscribing and publishing. It also has a ConcurrentHashMap with subscription as key and HashSet of clients which subscribe to it. This data structure in addition to being scalable as only a part of concurrent hash map is locked for update and reads can happen in parallel, also provides an easy to way to compute all the clients for which a message is to be send as described in point 8 of section above. It also have a method to check article validity for publish, subscribe and unsubscribe. While adding the client the addClient method will check the number of active clients against the max clients allowed.
3. PubSubImpl class is the one which binds all the server components and implements the RMI interface. It used SubscriptionManager as utility to do all the work described in above point.
4. PubSub system maintains the subscription information even if the client leaves the server. So that if the same client joins back it starts receiving articles based on earlier subscriptions.

Test cases attempted
------
#### Simple test cases
1. Checking whether the article is in legal format.
2. Checking whether contents is not present while subscribing / unsubscribing.
3. Checking whether contents is present while publishing.
4. Checking whether at least one filter is present while subscribing / unsubscribing / publishing.
5. Checking whether the duplicates messages are not received when the client is subscribed to both specific and
general filters using the same type/sender/org.
6. Checking whether the clients are receiving the subscribed articles even if they leave and join the server in
between.
7. Checking whether the clients which is not currently joined is not able to do any subscribe/unsubscribe/publish
operations.
8. Checking whether the message is not received when the client unsubscribes.
9. Checking whether the client doesn’t receive the messages after leaving the server and starts receiving the
messages based on previous subscriptions as soon as it is joined.
10. Making sure that client ends as soon as it ping (heartbeat) fails.

#### Stress test cases
The pubsub implementation is stress tested by creating 500 clients in parallel. All the clients performed continuous actions like publish, subscribe, unsubscribe, join and leave. 
