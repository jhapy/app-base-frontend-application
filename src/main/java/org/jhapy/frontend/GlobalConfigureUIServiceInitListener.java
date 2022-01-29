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

package org.jhapy.frontend;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Adds before enter listener to check access to views. Adds the Offline banner.
 *
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 2019-03-26
 */
@SpringComponent
@CssImport(value = "./styles/loading-indicator.css")
public class GlobalConfigureUIServiceInitListener implements VaadinServiceInitListener, HasLogger {

  @Autowired private AppProperties appProperties;

  @Override
  public void serviceInit(ServiceInitEvent event) {
    event
        .getSource()
        .addUIInitListener(
            uiEvent -> {
              var ui = uiEvent.getUI();
              ui.getLoadingIndicatorConfiguration().setApplyDefaultTheme(false);
            });
  }
}
