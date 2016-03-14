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

package eu.hansolo.mood;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import eu.hansolo.mood.mqtt.MqttEvent.MqttEventType;
import eu.hansolo.mood.mqtt.MqttManager;
import eu.hansolo.mood.controls.ColorRegulator;
import eu.hansolo.mood.controls.ColorRegulatorBuilder;
import eu.hansolo.mood.mqtt.Topic;
import eu.hansolo.mood.transitions.SlideInLeftTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by hansolo on 26.02.16.
 */
public class MainView extends View {
    public  static final Topic   HUZZAH_INCOMING = new Topic("huzzah/1", MqttManager.QOS_0);
    public  static final Topic   HUZZAH_OUTGOING = new Topic("huzzah/1/msg", MqttManager.QOS_0);
    private static final String  MOOD_COMMAND    = "mood";
    private static final Pattern RGB_PATTERN     = Pattern.compile("^([0-9]{1,3}),([0-9]{1,3}),([0-9]{1,3})$");
    private static final Matcher RGB_MATCHER     = RGB_PATTERN.matcher("");
    private StackPane            mainPane;
    private ColorRegulator       colorRegulator;


    // ******************** Constructors **************************************
    public MainView(final String NAME) {
        super(NAME);
        init();
        registerListeners();

        setBackground(new Background(new BackgroundFill(MoodFX.BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        setCenter(mainPane);
        setShowTransitionFactory(SlideInLeftTransition::new);
    }

    @Override protected void updateAppBar(AppBar appBar) {
        //appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> System.out.println("Menu")));
        appBar.setTitleText("MoodFX");
        appBar.getActionItems().add(MaterialDesignIcon.SETTINGS.button(e -> getApplication().switchView(MoodFX.CONFIG_VIEW)));
    }


    // ******************** Initialization ************************************
    private void init() {
        colorRegulator = ColorRegulatorBuilder.create()
                                              .onButtonOnPressed(e -> {
                                                  colorRegulator.setAutomatic(false);
                                                  sendUpdate(colorRegulator.getCurrentColor());
                                              })
                                              .onButtonAutoClicked(e -> {
                                                  if (colorRegulator.isAutomatic()) {
                                                      sendUpdate(colorRegulator.getCurrentColor());
                                                  } else {
                                                      colorRegulator.setAutomatic(true);
                                                      sendMoodMessage(true);
                                                  }
                                              })
                                              .onButtonOffPressed(e -> {
                                                  sendOffMessage();
                                                  colorRegulator.setOn(false);
                                                  colorRegulator.setAutomatic(false);
                                              })
                                              .onTargetSet(o -> sendUpdate(colorRegulator.getTargetColor()))
                                              .build();
        colorRegulator.setDisable(true);
        colorRegulator.setOn(false);

        mainPane = new StackPane(colorRegulator);
        StackPane.setAlignment(colorRegulator, Pos.TOP_CENTER);
        mainPane.setPadding(new Insets(10));
        mainPane.setBackground(new Background(new BackgroundFill(MoodFX.BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private void registerListeners() {
        MqttManager.INSTANCE.addMqttEventListener(e -> {
            final MqttEventType TYPE = e.TYPE;
            switch(TYPE) {
                case CONNECTED   : Platform.runLater(() -> colorRegulator.setDisable(false)); break;
                case DISCONNECTED: Platform.runLater(() -> colorRegulator.setDisable(true)); break;
                case MESSAGE     : handleMqttMessage(e.TOPIC, e.MESSAGE.toString()); break;
            }
        });
    }


    // ******************** Methods *******************************************
    private void handleMqttMessage(final String TOPIC, final String MESSAGE) {
        if (MqttManager.INSTANCE.getHuzzahOutgoingTopic().NAME.equals(TOPIC)) {
            if (!colorRegulator.isAutomatic()) { colorRegulator.setAutomatic(true); }
            RGB_MATCHER.reset(MESSAGE);
            Platform.runLater(() -> {
                int red   = 0;
                int green = 0;
                int blue  = 0;
                while(RGB_MATCHER.find()) {
                    try {
                        red   = Integer.parseInt(RGB_MATCHER.group(1));
                        green = Integer.parseInt(RGB_MATCHER.group(2));
                        blue  = Integer.parseInt(RGB_MATCHER.group(3));
                    } catch (NumberFormatException e) {
                        red   = 0;
                        green = 0;
                        blue  = 0;
                    }
                }
                //Brighten up the color because max value will be 190 instead of 255
                colorRegulator.setTargetColor(Color.rgb(red, green, blue).deriveColor(0, 1, 2, 1));
            });
        } else if ((MqttManager.INSTANCE.getHuzzahIncomingTopic().NAME).equals(TOPIC)) {
            if (MOOD_COMMAND.equals(MESSAGE)) {
                if (!colorRegulator.isAutomatic()) { colorRegulator.setAutomatic(true); }
            } else {
                if (colorRegulator.isAutomatic()) { colorRegulator.setAutomatic(false); }
                colorRegulator.setTargetColor(Color.web("#" + MESSAGE));
            }
        }
    }

    private void sendMoodMessage(final boolean ON) { MqttManager.INSTANCE.publish(ON ? MOOD_COMMAND : colorRegulator.getTargetColor().toString().substring(2, 8)); }
    private void sendOffMessage() { MqttManager.INSTANCE.publish("000000"); }
    private void sendUpdate(final Color COLOR) { MqttManager.INSTANCE.publish(COLOR.toString().substring(2, 8)); }
}
