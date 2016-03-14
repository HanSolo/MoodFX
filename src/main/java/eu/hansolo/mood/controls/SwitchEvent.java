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

package eu.hansolo.mood.controls;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;


public class SwitchEvent extends Event {
    public static final EventType<SwitchEvent> ON  = new EventType(ANY, "on");
    public static final EventType<SwitchEvent> OFF = new EventType(ANY, "off");


    // ******************** Constructors **********************************
    public SwitchEvent(final Object SOURCE, final EventTarget TARGET, final EventType<SwitchEvent> EVENT_TYPE) {
        super(SOURCE, TARGET, EVENT_TYPE);
    }
}
