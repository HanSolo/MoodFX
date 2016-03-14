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

import com.gluonhq.charm.down.common.JavaFXPlatform;
import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import eu.hansolo.mood.controls.Switch;
import eu.hansolo.mood.mqtt.MqttEvent;
import eu.hansolo.mood.mqtt.MqttEvent.MqttEventType;
import eu.hansolo.mood.mqtt.MqttManager;
import eu.hansolo.mood.mqtt.Topic;
import eu.hansolo.mood.transitions.SlideInRightTransition;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import static com.gluonhq.charm.glisten.visual.GlistenStyleClasses.LIGHT;
import static com.gluonhq.charm.glisten.visual.GlistenStyleClasses.TOGGLE_BUTTON_SWITCH;
import static com.gluonhq.charm.glisten.visual.GlistenStyleClasses.applyStyleClass;


/**
 * Created by hansolo on 26.02.16.
 */
public class ConfigView extends View {
    private static final String PROPERTIES_FILE_NAME = "settings.properties";
    private static final Random RND                  = new Random();
    private File                localStoragePath;
    private Properties          properties;
    private Switch              connectButton;
    private TextField           brokerAddressField;
    private TextField           brokerPortField;
    private TextField           clientIdField;
    private TextField           userNameField;
    private PasswordField       passwordField;
    private TextField           lampIdField;
    private TextField           topicField;
    private AnchorPane          configPane;


    // ******************** Constructors **************************************
    public ConfigView(final String NAME) {
        super(NAME);

        try {
            localStoragePath = PlatformFactory.getPlatform().getPrivateStorage();
        } catch (IOException e) {
            String tmp = System.getProperty("java.io.tmpdir");
            localStoragePath = new File(tmp);
        }
        properties = createProperties();
        retrieveConfig();

        init();
        registerListeners();

        setBackground(new Background(new BackgroundFill(MoodFX.BACKGROUND_COLOR.brighter(), CornerRadii.EMPTY, Insets.EMPTY)));
        setCenter(configPane);
        setShowTransitionFactory(SlideInRightTransition::new);
    }

    @Override protected void updateAppBar(AppBar appBar) {
        appBar.setTitleText("MoodFX Config");
        appBar.getActionItems().add(MaterialDesignIcon.ARROW_BACK.button(e -> getApplication().switchView(MoodFX.MAIN_VIEW)));
    }


    // ******************** Initialization ************************************
    private void init() {
        Label brokerAddressLabel = new Label("BROKER ADDRESS");
        brokerAddressField = new TextField();
        brokerAddressField.focusedProperty().addListener((ov, wasFocused, isFocused) -> { if (wasFocused && !isFocused) { saveConfig(); } });

        Label brokerPortLabel = new Label("BROKER PORT");
        brokerPortField = new TextField();
        brokerPortField.focusedProperty().addListener((ov, wasFocused, isFocused) -> { if (wasFocused && !isFocused) { saveConfig(); } });

        Label clientIdLabel = new Label("CLIENT ID");
        clientIdField = new TextField("");
        clientIdField.focusedProperty().addListener((ov, wasFocused, isFocused) -> { if (wasFocused && !isFocused) { saveConfig(); } });

        Separator separator1 = new Separator(Orientation.HORIZONTAL);
        separator1.setValignment(VPos.CENTER);
        separator1.setPrefHeight(40);

        Label userNameLabel = new Label("USER NAME");
        userNameField = new TextField("");
        userNameField.focusedProperty().addListener((ov, wasFocused, isFocused) -> { if (wasFocused && !isFocused) { saveConfig(); } });

        Label passwordLabel = new Label("PASSWORD");
        passwordField = new PasswordField();
        passwordField.focusedProperty().addListener((ov, wasFocused, isFocused) -> { if (wasFocused && !isFocused) { saveConfig(); } });

        Separator separator2 = new Separator(Orientation.HORIZONTAL);
        separator2.setValignment(VPos.CENTER);
        separator2.setPrefHeight(40);

        Label lampIdLabel = new Label("LAMP ID");
        lampIdField = new TextField();
        lampIdField.focusedProperty().addListener((ov, wasFocused, isFocused) -> { if (wasFocused && !isFocused) { saveConfig(); } });

        Label topicLabel = new Label("TOPIC");
        topicField = new TextField();
        topicField.focusedProperty().addListener((ov, wasFocused, isFocused) -> { if (wasFocused && !isFocused) { saveConfig(); } });
        applyStyleClass(topicField, LIGHT);

        Separator separator0 = new Separator(Orientation.HORIZONTAL);
        separator0.setValignment(VPos.CENTER);
        separator0.setPrefHeight(40);

        Label connectionLabel = new Label("CONNECTION");
        connectButton = new Switch();
        connectButton.setRounded(true);
        connectButton.setSelectedText("CONNECTED");
        connectButton.setText("DISCONNECTED");
        connectButton.setSelectedTextColor(Color.rgb(51, 51, 51));
        applyStyleClass(connectButton, TOGGLE_BUTTON_SWITCH);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(10));

        gridPane.add(connectionLabel, 0, 0);
        gridPane.add(connectButton, 1, 0);

        gridPane.add(separator0, 0, 1);
        separator0.setPadding(new Insets(5, 0, 5, 0));
        GridPane.setColumnSpan(separator0, 2);

        gridPane.add(brokerAddressLabel, 0, 2);
        gridPane.add(brokerAddressField, 1, 2);

        gridPane.add(brokerPortLabel, 0, 3);
        gridPane.add(brokerPortField, 1, 3);

        gridPane.add(clientIdLabel, 0, 4);
        gridPane.add(clientIdField, 1, 4);

        gridPane.add(separator1, 0, 5);
        separator1.setPadding(new Insets(5, 0, 5, 0));
        GridPane.setColumnSpan(separator1, 2);

        gridPane.add(userNameLabel, 0, 6);
        gridPane.add(userNameField, 1, 6);

        gridPane.add(passwordLabel, 0, 7);
        gridPane.add(passwordField, 1, 7);

        gridPane.add(separator2, 0, 8);
        separator2.setPadding(new Insets(5, 0, 5, 0));
        GridPane.setColumnSpan(separator2, 2);

        gridPane.add(lampIdLabel, 0, 9);
        gridPane.add(lampIdField, 1, 9);

        gridPane.add(topicLabel, 0, 10);
        gridPane.add(topicField, 1, 10);

        gridPane.setAlignment(Pos.CENTER);

        setFieldsFromProperties();

        AnchorPane.setTopAnchor(gridPane, 0d);
        AnchorPane.setRightAnchor(gridPane, 0d);
        //AnchorPane.setBottomAnchor(gridPane, 0d);
        AnchorPane.setLeftAnchor(gridPane, 0d);

        configPane = new AnchorPane(gridPane);
        configPane.setPadding(new Insets(10));
        configPane.setBackground(new Background(new BackgroundFill(MoodFX.BACKGROUND_COLOR.brighter(), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private void registerListeners() {
        connectButton.setOnMouseClicked(e -> {
            if (connectButton.isSelected()) {
                MqttManager.INSTANCE.setBrokerAddress(brokerAddressField.getText());
                MqttManager.INSTANCE.setBrokerPort(Integer.parseInt(brokerPortField.getText()));
                MqttManager.INSTANCE.setClientId(clientIdField.getText());
                MqttManager.INSTANCE.setUserName(userNameField.getText());
                MqttManager.INSTANCE.setPassword(passwordField.getText());
                MqttManager.INSTANCE.setHuzzahIncomingTopic(new Topic(new StringBuilder(topicField.getText()).append("/")
                                                                                                             .append(lampIdField.getText())
                                                                                                             .toString()));
                saveConfig();
                new Thread(() -> { if (MqttManager.INSTANCE.isConnected()) { MqttManager.INSTANCE.reInit(); } else { MqttManager.INSTANCE.connect(); }}).start();
            } else {
                if (MqttManager.INSTANCE.isConnected()) MqttManager.INSTANCE.disconnect(2000);
                MqttManager.INSTANCE.fireMqttEvent(MqttManager.DISCONNECT_EVENT);
            }
        });
    }

    private String getUniqueId() {
        String uniqueId;
        if (JavaFXPlatform.isDesktop()) {
            try {
                InetAddress      ip      = InetAddress.getLocalHost();
                NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                byte[]           mac     = network.getHardwareAddress();
                StringBuilder    sb      = new StringBuilder();
                for (int i = 0; i < mac.length; i++) { sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "")); }
                uniqueId = sb.toString();
            } catch (UnknownHostException | SocketException e) {
                uniqueId = "MoodFXDesktop" + RND.nextInt(1000);
            }
        } else if (JavaFXPlatform.isAndroid()) {
            uniqueId = "MoodFXAndroid" + RND.nextInt(1000);
        } else if (JavaFXPlatform.isIOS()) {
            uniqueId = "MoodFXiOS" + RND.nextInt(1000);
        } else {
            uniqueId = "MoodFX" + RND.nextInt(1000);
        }
        return uniqueId;
    }


    // ******************** Properties/Config *********************************
    private void retrieveConfig() {
        Reader reader = null;
        try {
            File file = new File(localStoragePath, PROPERTIES_FILE_NAME);
            reader = new FileReader(file);
            properties.load(reader);
        } catch (IOException ex) {
        } finally {
            try { if (reader != null) { reader.close(); } } catch (IOException ex) {}
        }
    }
    private void saveConfig() {
        if (null == properties) properties = new Properties();
        try {
            properties.setProperty("broker_address", brokerAddressField.getText());
            properties.setProperty("broker_port", brokerPortField.getText());
            properties.setProperty("client_id", clientIdField.getText());
            properties.setProperty("user_name", userNameField.getText());
            properties.setProperty("password", passwordField.getText());
            properties.setProperty("lamp_id", lampIdField.getText());
            properties.setProperty("topic", topicField.getText());
            File file = new File(localStoragePath, PROPERTIES_FILE_NAME);
            properties.store(new FileWriter(file), PROPERTIES_FILE_NAME);
        } catch (IOException ex) {}
    }
    private Properties createProperties() {
        Properties p = new Properties();
        p.setProperty("broker_address", "iot.eclipse.org");
        p.setProperty("broker_port", "1883");
        p.setProperty("client_id", getUniqueId());
        p.setProperty("user_name", "");
        p.setProperty("password", "");
        p.setProperty("lamp_id", "1");
        p.setProperty("topic", "huzzah");
        return p;
    }
    private void setFieldsFromProperties() {
        brokerAddressField.setText(properties.getProperty("broker_address"));
        brokerPortField.setText(properties.getProperty("broker_port"));
        clientIdField.setText(properties.getProperty("client_id"));
        userNameField.setText(properties.getProperty("user_name"));
        passwordField.setText(properties.getProperty("password"));
        lampIdField.setText(properties.getProperty("lamp_id"));
        topicField.setText(properties.getProperty("topic"));

        MqttManager.INSTANCE.setBrokerAddress(properties.getProperty("broker_address"));
        MqttManager.INSTANCE.setBrokerPort(Integer.parseInt(properties.getProperty("broker_port")));
        MqttManager.INSTANCE.setClientId(properties.getProperty("client_id"));
        MqttManager.INSTANCE.setUserName(properties.getProperty("user_name"));
        MqttManager.INSTANCE.setPassword(properties.getProperty("password"));
        MqttManager.INSTANCE.setHuzzahIncomingTopic(new Topic(new StringBuilder(properties.getProperty("topic")).append("/")
                                                                                                                .append(properties.getProperty("lamp_id"))
                                                                                                                .toString()));
    }
}
