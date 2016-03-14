/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.mood.mqtt;

import eu.hansolo.mood.MainView;
import eu.hansolo.mood.mqtt.MqttEvent.MqttEventType;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by hansolo on 18.08.15.
 */
public enum MqttManager implements MqttCallback {
    INSTANCE;

    public static final  int                QOS_0            = 0;
    public static final  int                QOS_1            = 1;
    public static final  int                QOS_2            = 2;
    public static final  boolean            RETAINED         = true;
    public static final  boolean            NOT_RETAINED     = false;
    public static final  MqttEvent          CONNECT_EVENT    = new MqttEvent(MqttManager.INSTANCE, MqttEventType.CONNECTED, null, null);
    public static final  MqttEvent          DISCONNECT_EVENT = new MqttEvent(MqttManager.INSTANCE, MqttEventType.DISCONNECTED, null, null);

    private              String             brokerAddress;
    private              int                brokerPort;
    private              String             clientId;
    private              String             userName;
    private              String             password;
    private              Topic              huzzahIncoming;
    private              Topic              huzzahOutgoing;

    private              MqttClient         client;
    private              MqttConnectOptions clientConnectOptions;

    private              List<Topic>        subscribedTopics;

    // Reconnection
    private              Thread             reconnectionThread;
    private              int                randomBase;
    private              boolean            connected;

    // Event Handling
    private CopyOnWriteArrayList<MqttEventListener> listenerList = new CopyOnWriteArrayList<>();


    // ******************* Constructors ***************************************
    MqttManager() {
        brokerAddress    = "tcp://iot.eclipse.org";
        brokerPort       = 1883;
        clientId         = UUID.randomUUID().toString();
        userName         = "";
        password         = "";
        huzzahIncoming   = MainView.HUZZAH_INCOMING;
        huzzahOutgoing   = MainView.HUZZAH_OUTGOING;
        subscribedTopics = new ArrayList<>(8);
        subscribedTopics.add(huzzahIncoming);
        subscribedTopics.add(huzzahOutgoing);

        randomBase = new Random().nextInt(11) + 5; // between 5 and 15 seconds
        init();
    }


    // ******************* Initialization *************************************
    private void init() {
        clientConnectOptions = new MqttConnectOptions();
        clientConnectOptions.setCleanSession(true);
        clientConnectOptions.setKeepAliveInterval(1200);
        connected = false;
    }
    public void reInit() {
        if (connected) {
            unSubscribeTopics();
            disconnect(2000);
        }
        init();
        connect();
    }


    // ******************** Methods *******************************************
    public void connect() {
        try {
            if (!userName.isEmpty()) {
                clientConnectOptions.setUserName(userName);
                clientConnectOptions.setPassword(password.toCharArray());
            }
            client = new MqttClient(brokerAddress + ":" + brokerPort, clientId, new MemoryPersistence());
            client.setCallback(this);
            client.connect(clientConnectOptions);
            connected = true;
            fireMqttEvent(CONNECT_EVENT);
            subscribeToTopics();
        } catch (MqttException exception) {
            connected = false;
            fireMqttEvent(DISCONNECT_EVENT);
            reconnect();
        }
    }
    public boolean isConnected() { return connected; }
    public void disconnect(final long TIMEOUT) {
        if (null == client || !client.isConnected()) return;
        try {
            if (null != reconnectionThread && reconnectionThread.isAlive()) { reconnectionThread.interrupt(); }
            client.disconnect(TIMEOUT);
        } catch (MqttException exception) {
        }
    }

    public void subscribeTo(final Topic TOPIC) {
        if (subscribedTopics.contains(TOPIC)) return;
        try {
            client.subscribe(TOPIC.NAME, TOPIC.QOS);
            subscribedTopics.add(TOPIC);
        } catch (Exception exception) {}
    }
    public void unSubscribeFrom(final Topic TOPIC) {
        if (!subscribedTopics.contains(TOPIC)) return;
        try {
            client.unsubscribe(TOPIC.NAME);
            subscribedTopics.remove(TOPIC);
        } catch (Exception exception) {}
    }

    private void subscribeToTopics() {
        try {
            unSubscribeTopics();
            for(Topic topic : subscribedTopics) {
                client.subscribe(topic.NAME, topic.QOS);
            }
        } catch (Exception exception) {
        }
    }
    private void unSubscribeTopics() {
        try {
            for (Topic topic : subscribedTopics) { client.unsubscribe(topic.NAME); }
        } catch (Exception exception) {}
    }
    public List<Topic> getSubscribedTopics() { return subscribedTopics; }

    public void publish(String MESSAGE) { publish(huzzahIncoming.QOS, NOT_RETAINED, huzzahIncoming.NAME, MESSAGE); }
    public void publish(final int QOS, final boolean RETAINED, final String TOPIC, final String MESSAGE) {
        publish(QOS, RETAINED, TOPIC, MESSAGE.getBytes());
    }
    public void publish(final int QOS, final boolean RETAINED, final String TOPIC, final byte[] PAYLOAD) {
        if (null == client) connect();
        if (null != client && client.isConnected()) {
            try {
                MqttTopic topic = client.getTopic(TOPIC);
                MqttMessage message = new MqttMessage(PAYLOAD);
                message.setQos(QOS);
                message.setRetained(RETAINED);
                MqttDeliveryToken token = topic.publish(message);
                token.waitForCompletion(100);
                Thread.sleep(10);
            } catch (MqttException exception) {} catch (InterruptedException exception) {}
        } else {
            connected = false;
            fireMqttEvent(DISCONNECT_EVENT);
        }
    }

    public String getBrokerAddress() { return brokerAddress; }
    public void setBrokerAddress(final String ADDRESS) {
        if (!ADDRESS.startsWith("tcp://")) {
            brokerAddress = "tcp://" + ADDRESS;
        } else {
            brokerAddress = ADDRESS;
        }
    }

    public int getBrokerPort() { return brokerPort; }
    public void setBrokerPort(final int PORT) { brokerPort = PORT; }

    public String getClientId() { return clientId; }
    public void setClientId(final String ID) { clientId = ID; }

    public String getUserName() { return userName; }
    public void setUserName(final String NAME) { userName = NAME; }

    public String getPassword() { return password; }
    public void setPassword(final String PASSWORD) { password = PASSWORD; }

    public Topic getHuzzahIncomingTopic() { return huzzahIncoming; }
    public void setHuzzahIncomingTopic(final Topic TOPIC) {
        huzzahIncoming = TOPIC;
        subscribeTo(TOPIC);
        subscribeToTopics();
    }

    public Topic getHuzzahOutgoingTopic() { return huzzahOutgoing; }
    public void setHuzzahOutgoing(final Topic TOPIC) {
        huzzahOutgoing = TOPIC;
        subscribeTo(TOPIC);
        subscribeToTopics();
    }


    // ******************** Reconnection **************************************
    private boolean isReconnectionAllowed() { return !client.isConnected(); }

    synchronized protected void reconnect() {
        if (client.isConnected() && reconnectionThread != null && reconnectionThread.isAlive()) return;

        reconnectionThread = new Thread() {

            private int attempts = 0;

            /**
             * Returns the number of seconds until the next reconnection attempt.
             * @return the number of seconds until the next reconnection attempt.
             */
            private int timeDelay() {
                attempts++;
                if (attempts > 13) {
                    return randomBase * 6 * 5; // between 2.5 and 7.5 minutes (~5 minutes)
                }
                if (attempts > 7) {
                    return randomBase * 6;     // between 30 and 90 seconds (~1 minutes)
                }
                return randomBase;             // 10 seconds
            }

            /**
             * The process will try the reconnection until the connection
             * succeed or the user cancel it
             */
            public void run() {
                while (MqttManager.this.isReconnectionAllowed()) {
                    int remainingSeconds = timeDelay();

                    while (MqttManager.this.isReconnectionAllowed() && remainingSeconds > 0) {
                        try {
                            Thread.sleep(1000);
                            remainingSeconds--;
                        } catch (InterruptedException exception) {
                            connected = false;
                            fireMqttEvent(DISCONNECT_EVENT);
                        }
                    }

                    // Makes a reconnection attempt
                    try {
                        if (MqttManager.this.isReconnectionAllowed()) {
                            client.connect(clientConnectOptions);
                            if (client.isConnected()) {
                                connected = true;
                                fireMqttEvent(CONNECT_EVENT);
                                subscribeToTopics();
                            }
                        }
                    } catch (MqttException exception) {
                        // Fires the failed reconnection notification
                        connected = false;
                        fireMqttEvent(DISCONNECT_EVENT);
                    }
                }
            }
        };
        reconnectionThread.setName("MQTT Reconnection Manager");
        reconnectionThread.setDaemon(false);
        reconnectionThread.start();
    }


    // ******************** Event handling ************************************
    @Override public void connectionLost(final Throwable CAUSE) {
        fireMqttEvent(DISCONNECT_EVENT);
        reconnect();
    }
    @Override public void messageArrived(final String TOPIC, final MqttMessage MQTT_MESSAGE) {
        fireMqttEvent(new MqttEvent(this, MqttEventType.MESSAGE, TOPIC, MQTT_MESSAGE));
    }
    @Override public void deliveryComplete(final IMqttDeliveryToken TOKEN) {}

    public final void setOnMessageReceived(final MqttEventListener LISTENER) { addMqttEventListener(LISTENER); }
    public final void addMqttEventListener(final MqttEventListener LISTENER) { if (!listenerList.contains(LISTENER)) listenerList.add(LISTENER); }
    public final void removeMqttEventListener(final MqttEventListener LISTENER) { if (listenerList.contains(LISTENER)) listenerList.remove(LISTENER); }

    public void fireMqttEvent(final MqttEvent EVENT) { for (MqttEventListener listener : listenerList) { listener.onMqttEvent(EVENT); } }
}
