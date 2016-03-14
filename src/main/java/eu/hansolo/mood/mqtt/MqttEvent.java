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

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.EventObject;


/**
 * Created by hansolo on 23.01.15.
 */
public class MqttEvent extends EventObject {
    public enum MqttEventType { CONNECTED, DISCONNECTED, MESSAGE}
    public final MqttEventType TYPE;
    public final String        TOPIC;
    public final MqttMessage   MESSAGE;


    // ******************** Constructors **************************************
    public MqttEvent(final Object SRC, final MqttEventType MQTT_EVENT_TYPE, final String MQTT_TOPIC, final MqttMessage MQTT_MESSAGE) {
        super(SRC);
        TYPE    = MQTT_EVENT_TYPE;
        TOPIC   = MQTT_TOPIC;
        MESSAGE = MQTT_MESSAGE;
    }
}
