/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.frontend.components.dragger;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import org.jhapy.commons.utils.HasLogger;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 9/12/19
 */
@Tag("drawer-dragger")
@JsModule("./dragger/dragger.js")
public class Dragger extends Component implements HasLogger, HasStyle {

  private final HasStyle drawer;

  public Dragger(HasStyle drawer) {
    this.drawer = drawer;

    addListener(DraggerEvent.class, this::onDraggerEvent);
  }

  @ClientCallable
  public void draggerChanged(String newWidth) {
    var loggerPrefix = getLoggerPrefix("draggerChanged", newWidth);
    logger().debug(loggerPrefix + "Event received, set parent width to " + newWidth + "px");
    drawer.getStyle().set("--navi-drawer-width", newWidth + "px");
    getStyle().set("--navi-drawer-width", newWidth + "px");
  }

  public void onDraggerEvent(DraggerEvent draggerEvent) {
    var loggerPrefix = getLoggerPrefix("onDraggerEvent", draggerEvent);
    logger()
        .debug(
            loggerPrefix + "Event received, set parent width to " + draggerEvent.getWidth() + "px");
    drawer.getStyle().set("--navi-drawer-width", draggerEvent.getWidth() + "px");
    getStyle().set("--navi-drawer-width", draggerEvent.getWidth() + "px");
  }
}