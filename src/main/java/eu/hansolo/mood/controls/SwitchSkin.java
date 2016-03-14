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

import javafx.animation.TranslateTransition;
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
 * Created by hansolo on 15.02.16.
 */
public class SwitchSkin extends SkinBase<Switch> implements Skin<Switch> {
    private static final double PREFERRED_WIDTH  = 82;
    private static final double PREFERRED_HEIGHT = 32;
    private static final double MINIMUM_WIDTH    = 82;
    private static final double MINIMUM_HEIGHT   = 32;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private double              width;
    private double              height;
    private Pane                pane;
    private double              aspectRatio;
    private Rectangle           thumb;
    private Text                selectedText;
    private Text                text;
    private Font                font;
    private TranslateTransition moveToDeselected;
    private TranslateTransition moveToSelected;
    private Color               switchColor;
    private Color               switchColorTransp;


    // ******************** Constructors **************************************
    public SwitchSkin(final Switch CONTROL) {
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
        switchColor       = getSkinnable().getSwitchColor();
        switchColorTransp = Color.color(switchColor.getRed(), switchColor.getGreen(), switchColor.getBlue(), 0.7);

        font = Font.font(0.4 * PREFERRED_HEIGHT);

        text = new Text(getSkinnable().getText());
        text.setFont(font);
        text.setFill(getSkinnable().getTextColor());
        text.setVisible(!getSkinnable().isSelected());

        selectedText = new Text(getSkinnable().getSelectedText());
        selectedText.setFont(font);
        selectedText.setFill(getSkinnable().getSelectedTextColor());
        selectedText.setVisible(getSkinnable().isSelected());

        thumb = new Rectangle();
        thumb.setFill(switchColorTransp);
        thumb.setMouseTransparent(true);

        moveToDeselected = new TranslateTransition(Duration.millis(150), thumb);
        moveToSelected   = new TranslateTransition(Duration.millis(150), thumb);

        pane = new Pane(selectedText, text, thumb);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> resize());
        getSkinnable().heightProperty().addListener(o -> resize());
        getSkinnable().roundedProperty().addListener(o -> resize());
        getSkinnable().switchColorProperty().addListener(o -> redraw());
        getSkinnable().selectedTextProperty().addListener(o -> resize());
        getSkinnable().textProperty().addListener(o -> resize());
        getSkinnable().selectedTextColorProperty().addListener(o -> redraw());
        getSkinnable().textColorProperty().addListener(o -> redraw());
        getSkinnable().selectedProperty().addListener(o -> toggle());
        pane.setOnMouseClicked(mouseEvent -> {
            if (getSkinnable().isDisabled()) return;
            if (null == getSkinnable().getToggleGroup() || getSkinnable().getToggleGroup().getToggles().isEmpty()) {
                getSkinnable().setSelected(!getSkinnable().isSelected());
            } else {
                getSkinnable().setSelected(true);
            }
        });
        moveToSelected.setOnFinished(e -> {
            selectedText.setVisible(true);
            text.setVisible(false);
            thumb.setFill(switchColor);
            pane.setBackground(new Background(new BackgroundFill(getSkinnable().isSelected() ? switchColorTransp : Color.TRANSPARENT, new CornerRadii(getSkinnable().isRounded() ? height : height * 0.0625), Insets.EMPTY)));
        });
        moveToDeselected.setOnFinished(e -> {
            text.setVisible(true);
            selectedText.setVisible(false);
            thumb.setFill(switchColorTransp);
            pane.setBackground(new Background(new BackgroundFill(getSkinnable().isSelected() ? switchColorTransp : Color.TRANSPARENT, new CornerRadii(getSkinnable().isRounded() ? height : height * 0.0625), Insets.EMPTY)));
        });
    }


    // ******************** Private Methods ***********************************
    private void toggle() {
        if (getSkinnable().isSelected()) {
            moveToSelected.play();
        } else {
            moveToDeselected.play();
        }
    }

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
            text.relocate(width - height * 0.3125 - text.getLayoutBounds().getWidth(), (height - text.getLayoutBounds().getHeight()) * 0.5);

            selectedText.setText(getSkinnable().getSelectedText());
            selectedText.setFont(font);
            selectedText.setTextOrigin(VPos.CENTER);
            selectedText.relocate(height * 0.3125, (height - selectedText.getLayoutBounds().getHeight()) * 0.5);

            thumb.setWidth(height * 0.75);
            thumb.setHeight(height * 0.75);
            thumb.setArcWidth(height * (rounded ? height : 0.0625));
            thumb.setArcHeight(height * (rounded ? height : 0.0625));
            thumb.setTranslateX(getSkinnable().isSelected() ? height * 1.125 : height * 0.125);
            thumb.setTranslateY(height * 0.125);

            moveToDeselected.setFromX(width - thumb.getLayoutBounds().getWidth() - height * 0.125);
            moveToDeselected.setToX(height * 0.125);

            moveToSelected.setFromX(height * 0.125);
            moveToSelected.setToX(width - thumb.getLayoutBounds().getWidth() - height * 0.125);

            redraw();
        }
    }

    private void redraw() {
        boolean rounded   = getSkinnable().isRounded();

        switchColor       = getSkinnable().getSwitchColor();
        switchColorTransp = Color.color(switchColor.getRed(), switchColor.getGreen(), switchColor.getBlue(), 0.7);

        pane.setBackground(new Background(new BackgroundFill(getSkinnable().isSelected() ? switchColorTransp : Color.TRANSPARENT, new CornerRadii(rounded ? height : height * 0.0625), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(switchColor, BorderStrokeStyle.SOLID, new CornerRadii(rounded ? height : height * 0.0625), new BorderWidths(height * 0.0625))));

        selectedText.setFill(getSkinnable().getSelectedTextColor());
        text.setFill(getSkinnable().getTextColor());

        thumb.setFill(getSkinnable().isSelected() ? switchColor : switchColorTransp);
    }
}
