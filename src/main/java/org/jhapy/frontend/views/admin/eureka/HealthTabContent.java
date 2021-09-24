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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.dto.registry.ActuatorHealth;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaApplicationInstance;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.dto.registry.actuate.*;
import org.jhapy.frontend.components.Badge;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.lumo.BadgeColor;
import org.springframework.http.*;

import java.net.URI;
import java.text.DecimalFormat;
import java.util.Collections;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
@Tag("apiTabContent")
public class HealthTabContent extends ActuatorBaseView {

  protected FlexBoxLayout content;
  protected Component component;
  protected Grid<AbstractHealthIndicator> grid;

  public HealthTabContent(UI ui, String I18N_PREFIX,
      AuthorizationHeaderUtil authorizationHeaderUtil) {
    super(ui, I18N_PREFIX + "health.", authorizationHeaderUtil);
  }

  public static String getValue(double value) {
    if (value <= 0) {
      return "0";
    }
    final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
    int digitGroups = (int) (Math.log10(value) / Math.log10(1024));
    return new DecimalFormat("#,##0.##").format(value / Math.pow(1024, digitGroups)) + " "
        + units[digitGroups];
  }

  public Component getContent(EurekaInfo eurekaInfo) {
    content = new FlexBoxLayout(createHeader(VaadinIcon.SEARCH,
        getTranslation("element." + I18N_PREFIX + "title"),
        getEurekaInstancesList(true, eurekaInfo.getApplicationList(), this::getDetails)));
    content.setAlignItems(FlexComponent.Alignment.CENTER);
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setSizeFull();

    FlexBoxLayout actionsLayout = new FlexBoxLayout();
    actionsLayout.setAlignItems(Alignment.START);
    actionsLayout.setFlexDirection(FlexDirection.ROW);
    actionsLayout.setWidthFull();

    Button rebootButton = UIUtils
        .createErrorButton(getTranslation("action." + I18N_PREFIX + "reboot"));
    rebootButton.addClickListener(buttonClickEvent -> {
          logger().debug(
              "Application : " + currentEurekaApplication.getName() + ", Env Url = "
                  + currentEurekaApplicationInstance.getMetadata().get("management.url")
                  + "/restart");
          final HttpHeaders httpHeaders = new HttpHeaders() {{
            set("Authorization", authorizationHeaderUtil.getAuthorizationHeader().get());
            setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            setContentType(MediaType.APPLICATION_JSON);
          }};

          ResponseEntity<String> env = restTemplate.exchange(URI.create(
              currentEurekaApplicationInstance.getMetadata().get("management.url") + "/restart"),
              HttpMethod.POST,
              new HttpEntity<>(httpHeaders), String.class);
          String result = env.getBody();
          logger().debug(result);
        }
    );

    Button shutdownButton = UIUtils
        .createTertiaryButton(getTranslation("action." + I18N_PREFIX + "shutdown"));
    shutdownButton.addClickListener(buttonClickEvent -> {
          logger().debug(
              "Application : " + currentEurekaApplication.getName() + ", Env Url = "
                  + currentEurekaApplicationInstance.getMetadata().get("management.url")
                  + "/shutdown");
          final HttpHeaders httpHeaders = new HttpHeaders() {{
            set("Authorization", authorizationHeaderUtil.getAuthorizationHeader().get());
            setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            setContentType(MediaType.APPLICATION_JSON);
          }};

          ResponseEntity<String> env = restTemplate.exchange(URI.create(
              currentEurekaApplicationInstance.getMetadata().get("management.url") + "/shutdown"),
              HttpMethod.POST,
              new HttpEntity<>(httpHeaders), String.class);
          String result = env.getBody();
          logger().debug(result);
        }
    );
    actionsLayout.add(rebootButton, shutdownButton);

    content.add(actionsLayout);
    content.add(getServiceList());
    return content;
  }

  protected Component getServiceList() {
    grid = new Grid<>();
    grid.setWidthFull();

    ComponentRenderer<Badge, AbstractHealthIndicator> badgeRenderer = new ComponentRenderer<>(
        healthIndicator -> {
          return switch (healthIndicator.getStatus()) {
            case "UP" -> new Badge("UP", BadgeColor.SUCCESS);
            case "DOWN" -> new Badge("DOWN", BadgeColor.ERROR);
            case "UNKNOWN" -> new Badge("UNKNOWN", BadgeColor.NORMAL);
            default -> new Badge(healthIndicator.getStatus(), BadgeColor.SUCCESS_PRIMARY);
          };
        }
    );

    grid.addColumn(AbstractHealthIndicator::getName).setKey("serviceName");
    grid.addColumn(badgeRenderer).setKey("status");
    grid.addComponentColumn(healthIndicator -> {
      if (healthIndicator.hasComponents() || healthIndicator.hasDetails()) {
        Button displayHealthDetails = UIUtils
            .createButton("Details", ButtonVariant.LUMO_SMALL);
        displayHealthDetails.addClickListener(buttonClickEvent -> grid
            .setDetailsVisible(healthIndicator, !grid.isDetailsVisible(healthIndicator)));
        return displayHealthDetails;
      } else {
        return new Label("");
      }
    }).setTextAlign(ColumnTextAlign.CENTER).setWidth("150px");
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    grid.setItemDetailsRenderer(TemplateRenderer.<AbstractHealthIndicator>of(
        "<div inner-h-t-m-l='[[item.orderItems]]'></div>")
        .withProperty("orderItems", t -> {
          if (t.hasDetails()) {
            if (t instanceof DiskSpaceHealthIndicator) {
              DiskSpaceHealthIndicator.Details details = (DiskSpaceHealthIndicator.Details) t
                  .getDetails();
              StringBuilder result = new StringBuilder();
              result.append("<span style=\"color:#e83e8c;\">Total : </span>");
              result.append(getValue(details.getTotal()));
              result.append("<br/>");

              result.append("<span style=\"color:#e83e8c;\">Free : </span>");
              result.append(getValue(details.getFree()));
              result.append("<br/>");

              result.append("<span style=\"color:#e83e8c;\">Threshold : </span>");
              result.append(getValue(details.getThreshold()));

              return result.toString();
            } else if (t instanceof ConfigServerHealthIndicator) {
              ConfigServerHealthIndicator.Details details = (ConfigServerHealthIndicator.Details) t
                  .getDetails();
              StringBuilder result = new StringBuilder();

              result.append("<span style=\"color:#e83e8c;\">Repositories : </span>");
              result.append("<pre>");
              result.append(gson.toJson(details.getRepositories()));
              result.append("</pre>");

              return result.toString();
            } else if (t instanceof ClientConfigServerHealthIndicator) {
              ClientConfigServerHealthIndicator.Details details = (ClientConfigServerHealthIndicator.Details) t
                  .getDetails();
              StringBuilder result = new StringBuilder();

              result.append("<span style=\"color:#e83e8c;\">Property Sources : </span>");
              result.append("<pre>");
              result.append(gson.toJson(details.getPropertySources()));
              result.append("</pre>");

              return result.toString();
            } else if (t instanceof DiscoveryClientHealthIndicator) {
              DiscoveryClientHealthIndicator.Details details = (DiscoveryClientHealthIndicator.Details) t
                  .getDetails();
              StringBuilder result = new StringBuilder();

              result.append("<span style=\"color:#e83e8c;\">Services : </span>");
              result.append("<pre>");
              result.append(gson.toJson(details.getServices()));
              result.append("</pre>");

              return result.toString();
            } else if (t instanceof HazelcastHealthIndicator) {
              HazelcastHealthIndicator.Details details = (HazelcastHealthIndicator.Details) t
                  .getDetails();
              StringBuilder result = new StringBuilder();

              result.append("<span style=\"color:#e83e8c;\">Name : </span>");
              result.append(details.getName());
              result.append("<br/>");

              result.append("<span style=\"color:#e83e8c;\">UUID : </span>");
              result.append(details.getUuid());

              return result.toString();
            } else if (t instanceof JmsHealthIndicator) {
              JmsHealthIndicator.Details details = (JmsHealthIndicator.Details) t
                  .getDetails();
              StringBuilder result = new StringBuilder();

              result.append("<span style=\"color:#e83e8c;\">Provider : </span>");
              result.append(details.getProvider());

              return result.toString();
            } else if (t instanceof DataSourceHealthIndicator) {
              DataSourceHealthIndicator.Details details = (DataSourceHealthIndicator.Details) t
                  .getDetails();
              StringBuilder result = new StringBuilder();

              result.append("<span style=\"color:#e83e8c;\">Database : </span>");
              result.append(details.getDatabase());
              result.append("<br/>");

              result.append("<span style=\"color:#e83e8c;\">Validation Query : </span>");
              result.append(details.getValidationQuery());

              if (details.getResult() != null) {
                result.append("<br/>");
                result.append("<span style=\"color:#e83e8c;\">Result : </span>");
                result.append(details.getResult());
              }

              return result.toString();
            } else if (t instanceof MongoHealthIndicator) {
              MongoHealthIndicator.Details details = (MongoHealthIndicator.Details) t
                  .getDetails();
              StringBuilder result = new StringBuilder();

              result.append("<span style=\"color:#e83e8c;\">Version : </span>");
              result.append(details.getVersion());

              return result.toString();
            } else if (t instanceof Neo4jHealthIndicator) {
              Neo4jHealthIndicator.Details details = (Neo4jHealthIndicator.Details) t
                  .getDetails();
              StringBuilder result = new StringBuilder();

              result.append("<span style=\"color:#e83e8c;\">Edition : </span>");
              result.append(details.getEdition());
              result.append("<br/>");

              result.append("<span style=\"color:#e83e8c;\">Version : </span>");
              result.append(details.getVersion());

              if (details.getNodes() != null) {
                result.append("<br/>");
                result.append("<span style=\"color:#e83e8c;\">Nodes : </span>");
                result.append(details.getNodes());
              }

              return result.toString();
            } else if (t instanceof LdapHealthIndicator) {
              LdapHealthIndicator.Details details = (LdapHealthIndicator.Details) t
                  .getDetails();
              StringBuilder result = new StringBuilder();

              result.append("<span style=\"color:#e83e8c;\">Version : </span>");
              result.append(details.getVersion());

              return result.toString();
            } else {
              return gson.toJson(t.getDetails());
            }
          } else if (t.hasComponents()) {
            if (t instanceof DiscoveryServerHealthIndicator) {
              DiscoveryServerHealthIndicator.Components components = (DiscoveryServerHealthIndicator.Components) t
                  .getComponents();
              StringBuilder result = new StringBuilder();

              result.append("<span style=\"color:#e83e8c;\">Discovery Client : </span>");
              result.append("<pre>");
              result.append(gson.toJson(components.getDiscoveryClient()));
              result.append("</pre>");
              result.append("<br/>");

              result.append("<span style=\"color:#e83e8c;\">Eureka : </span>");
              result.append("<pre>");
              result.append(gson.toJson(components.getEureka()));
              result.append("</pre>");
              result.append("<br/>");

              return result.toString();
            } else {
              return gson.toJson(t.getComponents());
            }
          } else {
            return "";
          }
        })
        // This is now how we open the details
        .withEventHandler("handleClick", person -> grid.getDataProvider().refreshItem(person)));
    grid.setDetailsVisibleOnClick(false);
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    grid.getColumns().forEach(column -> {
      if (column.getKey() != null) {
        column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
        column.setResizable(true);
        column.setSortable(true);
      }
    });

    return grid;
  }

  protected void getDetails(EurekaApplication eurekaApplication,
      EurekaApplicationInstance eurekaApplicationInstance) {
    titleLabel.setText(
        getTranslation("element." + I18N_PREFIX + "title") + " - " + eurekaApplicationInstance
            .getInstanceId());

    final HttpHeaders httpHeaders = new HttpHeaders() {{
      set("Authorization", authorizationHeaderUtil.getAuthorizationHeader().get());
      setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }};

    try {
      logger().debug(
          "Application : " + eurekaApplication.getName() + ", Health Url = "
              + eurekaApplicationInstance.getHealthCheckUrl());
      ResponseEntity<String> aa = restTemplate.exchange(URI.create(
          eurekaApplicationInstance.getHealthCheckUrl()), HttpMethod.GET,
          new HttpEntity<>(httpHeaders), String.class);
      String jsonBody = aa.getBody();
      logger().debug("Actuator Health before Convert = " + jsonBody);
      ObjectMapper mapper = new ObjectMapper();
      ActuatorHealth actuatorHealth = mapper.readValue(jsonBody, ActuatorHealth.class);
      logger().debug("Actuator Health = " + actuatorHealth);

      grid.setItems(actuatorHealth.getComponentsList());

    } catch (Throwable t) {
      t.printStackTrace();
    }

  }
}