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

package org.jhapy.frontend.views.admin.eureka;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.textfield.TextArea;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaApplicationInstance;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.springframework.http.*;

import java.net.URI;
import java.util.Collections;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
@Tag("logsTabContent")
public class LogsTabContent extends ActuatorBaseView {

  protected FlexBoxLayout content;
  protected Component component;

  public LogsTabContent(
      UI ui, String I18N_PREFIX, AuthorizationHeaderUtil authorizationHeaderUtil) {
    super(ui, I18N_PREFIX + "logs.", authorizationHeaderUtil);
  }

  public Component getContent(EurekaInfo eurekaInfo) {
    content =
        new FlexBoxLayout(
            createHeader(
                VaadinIcon.SEARCH,
                getTranslation("element." + I18N_PREFIX + "title"),
                getEurekaInstancesList(true, eurekaInfo.getApplicationList(), this::getDetails)));
    content.setAlignItems(FlexComponent.Alignment.CENTER);
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setSizeFull();

    return content;
  }

  @Override
  public void refresh() {
    var loggerPrefix = getLoggerPrefix("refresh");
    if (currentEurekaApplicationInstance != null && currentEurekaApplication != null) {
      logger().debug(loggerPrefix + "Refresh content");
      getDetails(currentEurekaApplication, currentEurekaApplicationInstance);
    } else {
      logger().warn(loggerPrefix + "No application or application instance set, nothing to do");
    }
  }

  protected Component getLogs(String logFile) {
    TextArea loggerArea = new TextArea();
    loggerArea.setValue(logFile);
    loggerArea.setSizeFull();

    return loggerArea;
  }

  protected void getDetails(
      EurekaApplication eurekaApplication, EurekaApplicationInstance eurekaApplicationInstance) {
    titleLabel.setText(
        getTranslation("element." + I18N_PREFIX + "title")
            + " - "
            + eurekaApplicationInstance.getInstanceId());
    try {
      final HttpHeaders httpHeaders =
          new HttpHeaders() {
            {
              set("Authorization", authorizationHeaderUtil.getAuthorizationHeader().get());
              setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
            }
          };

      logger()
          .debug(
              "Application : "
                  + eurekaApplication.getName()
                  + ", Loggers Url = "
                  + eurekaApplicationInstance.getMetadata().get("management.url")
                  + "/logfile");
      ResponseEntity<String> aa =
          restTemplate.exchange(
              URI.create(
                  eurekaApplicationInstance.getMetadata().get("management.url") + "/logfile"),
              HttpMethod.GET,
              new HttpEntity<>(httpHeaders),
              String.class);
      String logFile = aa.getBody();

      logger().debug("Logs = " + logFile);

      if (content.getChildren().count() > 1) {
        if (component != null) {
          content.remove(component);
        }
      }

      component = getLogs(logFile);
      content.add(component);
      content.setFlex("1", component);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}