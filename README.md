# Publish Subscribe System
Google PubSub system Versus Own Implementation of PubSub system

Please find the Read Me in the corresponding PubSub implementation

Comparison of Own PubSub implementation and Google PubSub
------
### Subscription methodology
The subscriber needs to keep track of the object (returned when subscribing) which executes the callback function whenever it receives any new request. This object is required for unsubscribing. This is different from our own PubSub where we don't need to keep track of any object. Whenever we want to unsubscribe, we need to just send the channel name and the server will handle the rest.
#### Google PubSub
Google PubSub has an extra layer called subscription layer which connects with the topics. So, the client should create a subscription object on a topic using the topic name and unique subscription id. All the clients using the same subscription object are treated as a single subscription group.
#### Own PubSub
The client directly uses the topic names to subscribe and each client is treated as a individual subscriber. No subscription groups are formed.

### Delivery semantics
#### Google pubsub
It guarantees delivery to all the attached subscriptions groups. But all the clients in a single subscription group are not guaranteed to receive all the messages published on that subscription. The google pubsub retries the delivery of messages to the subscriptions until at least one client receives it and as soon a message in delivered to one of the clients in the subscription group, it deletes the message.
#### Own Pubsub
We use at most once delivery semantics. The message may/ may not be delivered depending on the UDP messages. No retries are made.

### Scalability
Google PubSub scales horizontally depending the load factors. Whereas, our implementation doesn’t scale after threshold if there the number of subscriptions/clients increases. We need more servers and join them to distribute workload.

### Message storage
Google PubSub stores the messages until is delivered at least to one client in each subscription group. Whereas, our PubSub doesn’t store the messages. As soon as the messages are received, they are sent to all the subscribed clients.


Apart from major differences mentioned above, one overall difference is
Google PubSub was easy to set up as everything was already built. Whereas in the case of own implementation, we had to take care synchronization, race conditions, efficiency and several other factors. Basically google PubSub was done in few hours while it took 2-3 days to build the own system.
   1. Not writing the code for matching a newly published article with its subscribers.
   2. Making sure the message gets delivered (at least once semantic) to the subscriber.
