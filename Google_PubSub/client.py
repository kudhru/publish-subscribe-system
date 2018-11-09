import json
import sys

import os
import uuid

from datetime import datetime
from google.cloud import pubsub_v1

from google.oauth2 import service_account


def join():
    global publisher, subscriber
    try:
        if publisher is None:
            publisher = pubsub_v1.PublisherClient(
                # credentials=credentials,
            )
        if subscriber is None:
            subscriber = pubsub_v1.SubscriberClient(
                # credentials=credentials,
            )
        print '------Successfully connected to the PubSub server emulator------'
        return publisher, subscriber
    except Exception as e:
        print e
        return None, None


def leave():
    print '------Successfully left the PubSub server emulator------'
    return None, None


def create_topic(publisher, project_id, topic):
    if publisher is None:
        print '------First join. Then create a topic.------'
        return

    formatted_topic = 'projects/{0}/topics/{1}'.format(project_id, topic)

    try:
        topic_creation = publisher.create_topic(formatted_topic)
        print '------Successfully created topic \'{0}\'------'.format(topic)
    except Exception as e:
        print e
    return


def subscribe(subscriber, project_id, subscriptions, topic):
    if subscriber is None:
        print '------First join. Then subscribe to a topic.------'
        return

    formatted_topic = 'projects/{0}/topics/{1}'.format(project_id, topic)

    # subscription can be done by multiple clients independently. Therefore, adding a unique identifier at the end of subscription string
    formatted_subscription = 'projects/{0}/subscriptions/{1}'.format(project_id, '{0}_{1}'.format(topic, str(uuid.uuid4())))
    try:
        subscription = subscriptions.get(topic)
        if subscription is not None:
            print '------Already subscribed. Therefore, doing nothing! We only only one subscription per topic.------'
            return subscription
        subscriber.create_subscription(formatted_subscription, formatted_topic)

        def callback(message):
            print message.data
            message.ack()

        subscription = subscriber.subscribe(
            formatted_subscription,
            callback
        )

        print '------Successfully subscribed to the topic:{0}------'.format(topic)
        return subscription
    except Exception as e:
        print e
        return None


def unsubscribe(subscriber, subscriptions, topic):
    if subscriber is None:
        print '------First join. Then unsubscribe to a topic.------'
        return
    try:
        if subscriptions.get(topic) is not None:
            subscriptions[topic].close()
        else:
            print 'It is not subscribed to this topic.'
        print '------Successfully unsubscribed from the topic:{0}------'.format(topic)
    except Exception as e:
        print e
    return


def publish(publisher, project_id, topic, message):

    if publisher is None:
        print '------First join. Then create a topic.------'
        return
    try:
        formatted_topic = 'projects/{0}/topics/{1}'.format(project_id, topic)
        publisher.publish(formatted_topic, b'{0}'.format(message))
        print '------Successfully published {0} to the topic:{1}------'.format(message, topic)
    except Exception as e:
        print e
    return


if __name__ == '__main__':
    os.environ["PUBSUB_EMULATOR_HOST"] = 'localhost:8085'
    publisher = None
    subscriber = None
    project_id = 'random-project-id'
    subscriptions = {}
    while(True):
        try:
            selected_option = int(raw_input('Available choices:\n 0 for joining\n 1 for leaving\n 2 for creating a topic \n '
                                            '3 for subscribing to a topic \n 4 for unsubscribing to a topic \n 5 for publishing to a topic\n'
                                            '6 for ending \n Please enter a choice: '))
        except Exception as e:
            print e
            continue
        if selected_option == 0:
            publisher, subscriber = join()
        elif selected_option == 1:
            publisher, subscriber = leave()
        elif selected_option == 2:
            topic = raw_input("Enter a topic to create: ")
            create_topic(publisher, project_id, topic)
        elif selected_option == 3:
            topic = raw_input("Enter a topic to subscribe: ")
            subscriptions[topic] = subscribe(subscriber, project_id, subscriptions, topic)
        elif selected_option == 4:
            topic = raw_input("Enter a topic to unsubscribe: ")
            unsubscribe(subscriber, subscriptions, topic)
            subscriptions[topic] = None
        elif selected_option == 5:
            topic = raw_input("Enter a topic to publish: ")
            message = raw_input("Enter a string to publish to the above entered topic: ")
            publish(publisher, project_id, topic, message)
        elif selected_option == 6:
            print 'Closing the client application'
            break

