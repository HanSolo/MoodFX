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


/**
 * Created by hansolo on 26.02.16.
 */
public class BtnEvent extends Event {
    public static final EventType<BtnEvent> PRESSED  = new EventType(ANY, "pressed");
    public static final EventType<BtnEvent> RELEASED = new EventType(ANY, "released");


    // ******************** Constructors **********************************
    public BtnEvent(final Object SOURCE, final EventTarget TARGET, final EventType<BtnEvent> EVENT_TYPE) {
        super(SOURCE, TARGET, EVENT_TYPE);
    }
}
