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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by hansolo on 15.02.16.
 */
public class Switch extends Control implements Toggle {
    private BooleanProperty                                         rounded;
    private ObjectProperty<Color>                                   switchColor;
    private StringProperty                                          selectedText;
    private StringProperty                                          text;
    private ObjectProperty<Color>                                   selectedTextColor;
    private ObjectProperty<Color>                                   textColor;
    private ObjectProperty<ToggleGroup>                             toggleGroup;
    private BooleanProperty                                         selected;
    private ConcurrentHashMap<EventHandler<SwitchEvent>, EventType> handlerMap;


    // ******************** Constructors **************************************
    public Switch() {
        getStyleClass().setAll("switch");
        rounded               = new BooleanPropertyBase(false) {
            @Override public Object getBean() { return Switch.this; }
            @Override public String getName() { return "rounded"; }
        };
        selected              = new BooleanPropertyBase(false) {
            @Override protected void invalidated() {
                if (null != getToggleGroup()) {
                    if (get()) {
                        getToggleGroup().selectToggle(Switch.this);
                    } else if (getToggleGroup().getSelectedToggle() == Switch.this) {
                        getToggleGroup().selectToggle(null);
                    }
                }
                fireEvent(new SwitchEvent(Switch.this, null, selected.get() ? SwitchEvent.ON : SwitchEvent.OFF));
            }
            @Override public Object getBean() { return null; }
            @Override public String getName() { return null; }
        };
        toggleGroup           = new ObjectPropertyBase<ToggleGroup>(null) {
            private ToggleGroup oldToggleGroup;
            @Override protected void invalidated() {
                final ToggleGroup toggleGroup = get();
                if (null != toggleGroup && !toggleGroup.getToggles().contains(Switch.this)) {
                    if (oldToggleGroup != null) { oldToggleGroup.getToggles().remove(Switch.this); }
                    toggleGroup.getToggles().add(Switch.this);
                } else if (null == toggleGroup) {
                    oldToggleGroup.getToggles().remove(Switch.this);
                }
                oldToggleGroup = toggleGroup;
            }
            @Override public Object getBean() { return Switch.this; }
            @Override public String getName() { return "toggleGroup"; }
        };
        switchColor           = new ObjectPropertyBase<Color>(Color.WHITE) {
            @Override public Object getBean() { return Switch.this; }
            @Override public String getName() { return "switchColor"; }
        };
        text                  = new StringPropertyBase("OFF") {
            @Override public Object getBean() { return Switch.this; }
            @Override public String getName() { return "text"; }
        };
        selectedText          = new StringPropertyBase("ON") {
            @Override public Object getBean() { return Switch.this; }
            @Override public String getName() { return "selectedText"; }
        };
        textColor             = new ObjectPropertyBase<Color>(Color.WHITE) {
            @Override public Object getBean() { return Switch.this; }
            @Override public String getName() { return "switchColorOff"; }
        };
        selectedTextColor     = new ObjectPropertyBase<Color>(Color.WHITE) {
            @Override public Object getBean() { return Switch.this; }
            @Override public String getName() { return "selectedTextColor"; }
        };
        handlerMap            = new ConcurrentHashMap<>(2);

        disabledProperty().addListener(o -> setOpacity(isDisabled() ? 0.4 : 1.0));
    }


    // ******************** Methods *******************************************
    public final boolean isRounded() { return rounded.get(); }
    public final void setRounded(final boolean ROUNDED) { rounded.set(ROUNDED); }
    public final BooleanProperty roundedProperty() { return rounded; }

    public final boolean isSelected() { return null == selected ? false : selected.get(); }
    public final void setSelected(final boolean SELECTED) { selected.set(SELECTED); }
    public final BooleanProperty selectedProperty() {return selected; }

    public final ToggleGroup getToggleGroup() { return null == toggleGroup ? null : toggleGroup.get(); }
    public final void setToggleGroup(final ToggleGroup GROUP) { toggleGroup.set(GROUP); }
    public final ObjectProperty<ToggleGroup> toggleGroupProperty() { return toggleGroup; }

    public final String getText() { return text.get(); }
    public final void setText(final String TEXT) { text.set(TEXT); }
    public final StringProperty textProperty() { return text; }

    public final String getSelectedText() { return selectedText.get(); }
    public final void setSelectedText(final String TEXT) { selectedText.set(TEXT); }
    public final StringProperty selectedTextProperty() { return selectedText; }

    public final Color getSwitchColor() { return switchColor.get(); }
    public final void setSwitchColor(final Color COLOR) { switchColor.set(COLOR); }
    public final ObjectProperty<Color> switchColorProperty() { return switchColor; }

    public final Color getTextColor() { return textColor.get(); }
    public final void setTextColor(final Color COLOR) { textColor.set(COLOR); }
    public final ObjectProperty<Color> textColorProperty() { return textColor; }

    public final Color getSelectedTextColor() { return selectedTextColor.get(); }
    public final void setSelectedTextColor(final Color COLOR) { selectedTextColor.set(COLOR); }
    public final ObjectProperty<Color> selectedTextColorProperty() { return selectedTextColor; }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() { return new SwitchSkin(this); }

    @Override public String getUserAgentStylesheet() { return getClass().getResource("switch.css").toExternalForm(); }


    // ******************** Event handling*************************************
    public final void setOnSwitchSelected(final EventHandler<SwitchEvent> HANDLER) { addEventHandler(SwitchEvent.ON, HANDLER); }
    public final void setOnSwitchDeselected(final EventHandler<SwitchEvent> HANDLER) { addEventHandler(SwitchEvent.OFF, HANDLER); }
    public final void removeOnSwitchSelected(final EventHandler<SwitchEvent> HANDLER) { removeEventHandler(SwitchEvent.ON, HANDLER); }
    public final void removeOnSwitchDeselected(final EventHandler<SwitchEvent> HANDLER) { removeEventHandler(SwitchEvent.OFF, HANDLER); }
}
