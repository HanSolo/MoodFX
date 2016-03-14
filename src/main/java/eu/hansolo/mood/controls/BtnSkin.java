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

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;


/**
 * Created by hansolo on 26.02.16.
 */
public class BtnSkin extends SkinBase<Btn> implements Skin<Btn> {
    private static final double PREFERRED_WIDTH  = 82;
    private static final double PREFERRED_HEIGHT = 32;
    private static final double MINIMUM_WIDTH    = 82;
    private static final double MINIMUM_HEIGHT   = 32;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private double         width;
    private double         height;
    private Pane           pane;
    private double         aspectRatio;
    private Rectangle      thumb;
    private Text           text;
    private Font           font;
    private FadeTransition pressed;
    private FadeTransition released;


    // ******************** Constructors **************************************
    public BtnSkin(final Btn CONTROL) {
        super(CONTROL);
        aspectRatio = PREFERRED_HEIGHT / PREFERRED_WIDTH;
        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(), 0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }

        if (getSkinnable().getPrefWidth() != PREFERRED_WIDTH || getSkinnable().getPrefHeight() != PREFERRED_HEIGHT) {
            aspectRatio = getSkinnable().getPrefHeight() / getSkinnable().getPrefWidth();
        }
    }

    private void initGraphics() {
        font = Font.font(0.4 * PREFERRED_HEIGHT);

        text = new Text(getSkinnable().getText());
        text.setFont(font);
        text.setFill(getSkinnable().getTextColor());

        thumb = new Rectangle();
        thumb.setFill(getSkinnable().getThumbColor());
        thumb.setOpacity(0.0);
        thumb.setMouseTransparent(true);

        pressed  = new FadeTransition(Duration.millis(100), thumb);
        pressed.setInterpolator(Interpolator.EASE_IN);
        pressed.setFromValue(0.0);
        pressed.setToValue(0.8);

        released = new FadeTransition(Duration.millis(250), thumb);
        released.setInterpolator(Interpolator.EASE_OUT);
        released.setFromValue(0.8);
        released.setToValue(0.0);

        pane = new Pane(thumb, text);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> resize());
        getSkinnable().heightProperty().addListener(o -> resize());
        getSkinnable().roundedProperty().addListener(o -> resize());
        getSkinnable().buttonColorProperty().addListener(o -> redraw());
        getSkinnable().thumbColorProperty().addListener(o -> resize());
        getSkinnable().textColorProperty().addListener(o -> redraw());
        getSkinnable().addEventHandler(ActionEvent.ACTION, e -> {  });
        pane.setOnMousePressed(mouseEvent -> {
            if (getSkinnable().isDisabled()) return;
            getSkinnable().fireEvent(new BtnEvent(getSkinnable(), null, BtnEvent.PRESSED));
            pressed.play();
        });
        pane.setOnMouseReleased(mouseEvent -> {
            if (getSkinnable().isDisabled()) return;
            getSkinnable().fireEvent(new BtnEvent(getSkinnable(), null, BtnEvent.RELEASED));
            released.play();
        });
    }


    // ******************** Private Methods ***********************************
    private void resize() {
        width       = getSkinnable().getWidth();
        height      = getSkinnable().getHeight();
        aspectRatio = height / width;

        if (width > 0 && height > 0) {
            width = height / aspectRatio;

            boolean rounded = getSkinnable().isRounded();

            pane.setMaxSize(width, height);
            pane.relocate((getSkinnable().getWidth() - width) * 0.5, (getSkinnable().getHeight() - height) * 0.5);

            font = Font.font(0.4 * height);

            text.setText(getSkinnable().getText());
            text.setFont(font);
            text.setTextOrigin(VPos.CENTER);
            text.relocate((width - text.getLayoutBounds().getWidth()) * 0.5, (height - text.getLayoutBounds().getHeight()) * 0.5);

            thumb.setWidth(width - (height * 0.25));
            thumb.setHeight(height * 0.75);
            thumb.setArcWidth(height * (rounded ? 0.75 : 0.0625));
            thumb.setArcHeight(height * (rounded ? 0.75 : 0.0625));
            thumb.setTranslateX(height * 0.125);
            thumb.setTranslateY(height * 0.125);

            redraw();
        }
    }

    private void redraw() {
        boolean rounded   = getSkinnable().isRounded();

        pane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(rounded ? height : height * 0.0625), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getButtonColor(), BorderStrokeStyle.SOLID, new CornerRadii(rounded ? height : height * 0.0625), new BorderWidths(height * 0.0625))));

        thumb.setFill(getSkinnable().getThumbColor());
        text.setFill(getSkinnable().getTextColor());
    }
}


