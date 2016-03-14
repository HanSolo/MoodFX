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

/**
 * Created by hansolo on 04.03.16.
 */
public class Topic {
    public final String NAME;
    public final int    QOS;


    public Topic(final String NAME) { this(NAME, 0); }
    public Topic(final String NAME, final int QOS) {
        this.NAME = NAME;
        this.QOS  = QOS;
    }

    @Override public String toString() {
        return new StringBuilder("{").append("\n\"topic\":").append(NAME).append("\n\"qos\":").append(QOS).append("\n}").toString();
    }
}
