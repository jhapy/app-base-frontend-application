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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaApplicationInstance;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.dto.registry.Loggers;
import org.jhapy.dto.registry.Loggers.LogLevel;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.utils.TextColor;
import org.jhapy.frontend.utils.UIUtils;
import org.springframework.http.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
@Tag("loggersTabContent")
public class LoggersTabContent extends ActuatorBaseView {

  protected FlexBoxLayout content;
  protected Component component;

  public LoggersTabContent(
      UI ui, String I18N_PREFIX, AuthorizationHeaderUtil authorizationHeaderUtil) {
    super(ui, I18N_PREFIX + "loggers.", authorizationHeaderUtil);
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

  protected Component getLoggers(
      List<Logger> allLoggers, List<Loggers.LogLevel> availableLogLevels) {
    Grid<Logger> loggersGrid = new Grid<>();
    loggersGrid.addColumn(s -> s.name).setKey("application");
    loggersGrid.addComponentColumn(
        logger -> {
          Select<LogLevel> logLevelSelect = new Select<>();
          logLevelSelect.setItems(availableLogLevels);
          logLevelSelect.setValue(logger.effectiveLevel);
          logLevelSelect.setRenderer(
              new ComponentRenderer<>(
                  logLevel -> {
                    if (logLevel.equals(LogLevel.TRACE)) {
                      return UIUtils.createLabel(TextColor.HEADER, LogLevel.TRACE.name());
                    } else if (logLevel.equals(LogLevel.DEBUG)) {
                      return UIUtils.createLabel(TextColor.BODY, LogLevel.DEBUG.name());
                    } else if (logLevel.equals(LogLevel.INFO)) {
                      return UIUtils.createLabel(TextColor.PRIMARY, LogLevel.INFO.name());
                    } else if (logLevel.equals(LogLevel.WARN)) {
                      return UIUtils.createLabel(TextColor.TERTIARY, LogLevel.WARN.name());
                    } else if (logLevel.equals(LogLevel.ERROR)) {
                      return UIUtils.createLabel(TextColor.ERROR, LogLevel.ERROR.name());
                    } else if (logLevel.equals(LogLevel.FATAL)) {
                      return UIUtils.createLabel(TextColor.ERROR_CONTRAST, LogLevel.FATAL.name());
                    } else if (logLevel.equals(LogLevel.OFF)) {
                      return UIUtils.createLabel(TextColor.DISABLED, LogLevel.OFF.name());
                    } else {
                      return UIUtils.createLabel(TextColor.DISABLED, logLevel.name());
                    }
                  }));
          return logLevelSelect;
        });
    loggersGrid
        .getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(
                    getTranslation("element." + I18N_PREFIX + "loggers." + column.getKey()));
                column.setResizable(true);
              }
            });
    loggersGrid.setItems(allLoggers);
    return loggersGrid;
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
              setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            }
          };

      logger()
          .debug(
              "Application : "
                  + eurekaApplication.getName()
                  + ", Loggers Url = "
                  + eurekaApplicationInstance.getMetadata().get("management.url")
                  + "/loggers");
      ResponseEntity<String> aa =
          restTemplate.exchange(
              URI.create(
                  eurekaApplicationInstance.getMetadata().get("management.url") + "/loggers"),
              HttpMethod.GET,
              new HttpEntity<>(httpHeaders),
              String.class);
      String jsonBody = aa.getBody();
      ObjectMapper mapper = new ObjectMapper();
      Loggers loggers = mapper.readValue(jsonBody, Loggers.class);
      logger().debug("Loggers = " + loggers);

      List<LogLevel> availableLogLevels = new ArrayList<>(loggers.getLevels());
      List<Logger> allLoggers = new ArrayList<>();
      loggers
          .getLoggers()
          .keySet()
          .forEach(
              key ->
                  allLoggers.add(
                      new Logger(
                          key,
                          loggers.getLoggers().get(key).getConfiguredLevel() == null
                              ? null
                              : LogLevel.valueOf(
                                  loggers.getLoggers().get(key).getConfiguredLevel()),
                          LogLevel.valueOf(loggers.getLoggers().get(key).getEffectiveLevel()))));

      if (content.getChildren().count() > 1) {
        if (component != null) {
          content.remove(component);
        }
      }

      component = getLoggers(allLoggers, availableLogLevels);
      content.add(component);
      content.setFlex("1", component);

    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  @Data
  @AllArgsConstructor
  static class Logger {

    private String name;
    private LogLevel configuredLevel;
    private LogLevel effectiveLevel;
  }
}