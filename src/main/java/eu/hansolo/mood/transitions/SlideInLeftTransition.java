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

package eu.hansolo.mood.transitions;

import com.gluonhq.charm.glisten.animation.CachedTimelineTransition;
import com.gluonhq.charm.glisten.animation.HideableTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;


public class SlideInLeftTransition extends CachedTimelineTransition implements HideableTransition {

    // ******************** Constructors **************************************
    public SlideInLeftTransition(Node node) {
        this(node, false);
    }
    public SlideInLeftTransition(Node node, boolean opacityInterpolated) {
        super(node, null, true, opacityInterpolated);
        setCycleDuration(Duration.seconds(0.5D));
        setDelay(Duration.seconds(0.2D));
    }


    // ******************** Methods *******************************************
    protected void starting() {
        double var1 = -node.localToScene(0.0D, 0.0D).getX() - node.getBoundsInParent().getWidth();
        timeline = new Timeline();
        timeline.getKeyFrames().addAll(new KeyFrame[]{ new KeyFrame(Duration.millis(0.0D), new KeyValue[]{ new KeyValue(node.opacityProperty(), Integer.valueOf(isOpacityInterpolated()?0:1), WEB_EASE),
                                                                                                           new KeyValue(node.translateXProperty(), Double.valueOf(var1), WEB_EASE) }),
                                                       new KeyFrame(Duration.millis(500.0D), new KeyValue[]{ new KeyValue(node.opacityProperty(), Integer.valueOf(1), WEB_EASE),
                                                                                                              new KeyValue(node.translateXProperty(), Integer.valueOf(0), WEB_EASE) }) });
        super.starting();
    }
}

