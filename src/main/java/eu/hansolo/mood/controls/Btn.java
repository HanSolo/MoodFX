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
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;


/**
 * Created by hansolo on 26.02.16.
 */
public class Btn extends Control {
    private BooleanProperty       rounded;
    private ObjectProperty<Color> buttonColor;
    private ObjectProperty<Color> thumbColor;
    private StringProperty        text;
    private ObjectProperty<Color> textColor;


    // ******************** Constructors **************************************
    public Btn() {
        this("");
    }
    public Btn(final String TEXT) {
        getStyleClass().setAll("btn");
        rounded     = new BooleanPropertyBase(false) {
            @Override public Object getBean() { return Btn.this; }
            @Override public String getName() { return "rounded"; }
        };
        buttonColor = new ObjectPropertyBase<Color>(Color.WHITE) {
            @Override public Object getBean() { return Btn.this; }
            @Override public String getName() { return "buttonColor"; }
        };
        thumbColor  = new ObjectPropertyBase<Color>(Color.WHITE) {
            @Override public Object getBean() { return Btn.this; }
            @Override public String getName() { return "thumbColor"; }
        };
        textColor   = new ObjectPropertyBase<Color>(Color.WHITE) {
            @Override public Object getBean() { return Btn.this; }
            @Override public String getName() { return "textColor"; }
        };
        text        = new StringPropertyBase(TEXT) {
            @Override public Object getBean() { return Btn.this; }
            @Override public String getName() { return "textOn"; }
        };

        disabledProperty().addListener(o -> setOpacity(isDisabled() ? 0.4 : 1.0));
    }


    // ******************** Methods *******************************************
    public final boolean isRounded() { return rounded.get(); }
    public final void setRounded(final boolean ROUNDED) { rounded.set(ROUNDED); }
    public final BooleanProperty roundedProperty() { return rounded; }

    public final String getText() { return text.get(); }
    public final void setText(final String TEXT) { text.set(TEXT); }
    public final StringProperty textProperty() { return text; }

    public final Color getButtonColor() { return buttonColor.get(); }
    public final void setButtonColor(final Color COLOR) { buttonColor.set(COLOR); }
    public final ObjectProperty<Color> buttonColorProperty() { return buttonColor; }

    public final Color getThumbColor() { return thumbColor.get(); }
    public final void setThumbColor(final Color COLOR) { thumbColor.set(COLOR); }
    public final ObjectProperty<Color> thumbColorProperty() { return thumbColor; }

    public final Color getTextColor() { return textColor.get(); }
    public final void setTextColor(final Color COLOR) { textColor.set(COLOR); }
    public final ObjectProperty<Color> textColorProperty() { return textColor; }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() { return new BtnSkin(this); }

    @Override public String getUserAgentStylesheet() { return getClass().getResource("btn.css").toExternalForm(); }


    // ******************** Event handling*************************************
    public final void setOnButtonPressed(final EventHandler<BtnEvent> HANDLER) { addEventHandler(BtnEvent.PRESSED, HANDLER); }
    public final void setOnButtonReleased(final EventHandler<BtnEvent> HANDLER) { addEventHandler(BtnEvent.RELEASED, HANDLER); }
    public final void removeOnButtonPressed(final EventHandler<BtnEvent> HANDLER) { removeEventHandler(BtnEvent.PRESSED, HANDLER); }
    public final void removeOnButtonReleased(final EventHandler<BtnEvent> HANDLER) { removeEventHandler(BtnEvent.RELEASED, HANDLER); }
}

