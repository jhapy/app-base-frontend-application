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

package org.jhapy.frontend.views.admin.configServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.dto.registry.EurekaStatus;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.frontend.client.registry.RegistryServices;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.utils.UIUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.vaadin.tabs.PagedTabs;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
@Tag("apiTabContent")
public class CloudConfigurationTabContent extends CloudConfigBaseView {

  protected FlexBoxLayout content;
  protected Component component;

  protected TextArea yamlConfigurationTextArea;
  protected TextArea propertiesConfigurationTextArea;
  protected TextArea jsonConfigurationTextArea;
  protected Grid<String[]> tableConfigurationGrid;
  protected Grid<String[]> configurationSourcesGrid;

  protected TextField applicationTextField;
  protected TextField profileTextField;
  protected TextField labelTextField;

  public CloudConfigurationTabContent(
      Environment env, UI ui, String I18N_PREFIX, AuthorizationHeaderUtil authorizationHeaderUtil) {
    super(env, ui, I18N_PREFIX + "cloudConfiguration.", authorizationHeaderUtil);
  }

  @Override
  public void refresh() {
    getDetails(
        applicationTextField.getValue(), profileTextField.getValue(), labelTextField.getValue());
  }

  public Component getContent() {
    content =
        new FlexBoxLayout(
            createHeader(
                VaadinIcon.SEARCH, getTranslation("element." + I18N_PREFIX + "title"), getMenu()));
    content.setAlignItems(FlexComponent.Alignment.CENTER);
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setSizeFull();

    applicationTextField = new TextField(getTranslation("element." + I18N_PREFIX + "application"));
    applicationTextField.setValue("application");
    applicationTextField.setWidthFull();
    applicationTextField.addValueChangeListener(event -> refresh());
    applicationTextField.setValueChangeMode(ValueChangeMode.ON_CHANGE);

    profileTextField = new TextField(getTranslation("element." + I18N_PREFIX + "profile"));
    profileTextField.setValue("prod");
    profileTextField.setWidthFull();
    profileTextField.addValueChangeListener(event -> refresh());
    profileTextField.setValueChangeMode(ValueChangeMode.ON_CHANGE);

    labelTextField = new TextField(getTranslation("element." + I18N_PREFIX + "label"));
    labelTextField.setValue("master");
    labelTextField.setWidthFull();
    labelTextField.addValueChangeListener(event -> refresh());
    labelTextField.setValueChangeMode(ValueChangeMode.ON_CHANGE);

    content.add(applicationTextField, profileTextField, labelTextField);

    Label configurationSourcesLabel =
        UIUtils.createH2Label(getTranslation("element." + I18N_PREFIX + "configurationSources"));
    content.add(configurationSourcesLabel, getConfigurationSources());

    FlexBoxLayout configs = new FlexBoxLayout();
    configs.setAlignItems(FlexComponent.Alignment.CENTER);
    configs.setFlexDirection(FlexDirection.COLUMN);
    configs.setSizeFull();

    PagedTabs tabs = new PagedTabs(configs);
    Label configContentLabel =
        UIUtils.createH2Label(getTranslation("element." + I18N_PREFIX + "configuration"));
    configs.add(configContentLabel, tabs);

    tabs.add(getTranslation("element." + I18N_PREFIX + "tab.yaml"), getYamlConfig(), false);
    tabs.add(
        getTranslation("element." + I18N_PREFIX + "tab.properties"), getPropertiesConfig(), false);
    tabs.add(getTranslation("element." + I18N_PREFIX + "tab.json"), getJsonConfig(), false);
    tabs.add(getTranslation("element." + I18N_PREFIX + "tab.table"), getTableConfig(), false);

    content.add(configs);

    refresh();

    return content;
  }

  protected Component getYamlConfig() {
    yamlConfigurationTextArea = new TextArea();
    yamlConfigurationTextArea.setSizeFull();
    yamlConfigurationTextArea.getStyle().set("display", "block");
    yamlConfigurationTextArea.setReadOnly(true);

    return yamlConfigurationTextArea;
  }

  protected Component getPropertiesConfig() {
    propertiesConfigurationTextArea = new TextArea();
    propertiesConfigurationTextArea.setReadOnly(true);
    propertiesConfigurationTextArea.setSizeFull();
    propertiesConfigurationTextArea.getStyle().set("display", "block");

    return propertiesConfigurationTextArea;
  }

  protected Component getJsonConfig() {
    jsonConfigurationTextArea = new TextArea();
    jsonConfigurationTextArea.setReadOnly(true);
    jsonConfigurationTextArea.setSizeFull();
    jsonConfigurationTextArea.getStyle().set("display", "block");

    return jsonConfigurationTextArea;
  }

  protected Component getTableConfig() {
    tableConfigurationGrid = new Grid<>();

    tableConfigurationGrid.setWidthFull();

    tableConfigurationGrid.addColumn(property -> property[0]).setKey("property");
    tableConfigurationGrid
        .addColumn(property -> property.length > 1 ? property[1] : "")
        .setKey("value");

    tableConfigurationGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    tableConfigurationGrid
        .getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                column.setResizable(true);
                column.setSortable(true);
              }
            });

    return tableConfigurationGrid;
  }

  protected Component getConfigurationSources() {
    configurationSourcesGrid = new Grid<>();
    configurationSourcesGrid.setMinHeight("150px");
    configurationSourcesGrid.setWidthFull();

    configurationSourcesGrid.addColumn(property -> property[0]).setWidth("50px").setKey("index");
    configurationSourcesGrid.addColumn(property -> property[1]).setWidth("100px").setKey("type");
    configurationSourcesGrid.addColumn(property -> property[2]).setAutoWidth(true).setKey("search");

    configurationSourcesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    configurationSourcesGrid
        .getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                column.setResizable(true);
                column.setSortable(true);
              }
            });

    return configurationSourcesGrid;
  }

  protected void getDetails(String application, String profile, String label) {
    try {
      final HttpHeaders httpHeaders =
          new HttpHeaders() {
            {
              set("Authorization", authorizationHeaderUtil.getAuthorizationHeader().get());
              setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            }
          };

      ServiceResult<EurekaStatus> statusResult =
          RegistryServices.getEurekaService().status(new BaseRemoteQuery());
      if (statusResult.getIsSuccess() && statusResult.getData() != null) {
        String infoUrl = statusResult.getData().getManagementUrl() + "/info";
        logger().debug("Info Url = " + infoUrl);
        ResponseEntity<String> info =
            restTemplate.exchange(
                URI.create(infoUrl), HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);

        JSONParser jsonParser = new JSONParser();
        String infoBody = info.getBody();
        logger().debug("Info Result = " + infoBody);

        JSONObject infoJsonObject = (JSONObject) jsonParser.parse(infoBody);

        JSONArray cloudConfigServerConfiguration =
            (JSONArray) infoJsonObject.get("cloud-config-server-configuration-sources");

        List<String[]> cloudConfigServer = new ArrayList<>();
        AtomicInteger idx = new AtomicInteger(1);
        cloudConfigServerConfiguration.forEach(
            o -> {
              JSONObject jo = ((JSONObject) o);
              cloudConfigServer.add(
                  new String[] {
                    Integer.toString(idx.getAndIncrement()),
                    jo.get("type").toString(),
                    jo.get("search") != null
                        ? jo.get("search").toString()
                        : jo.get("uri").toString()
                  });
            });

        configurationSourcesGrid.setItems(cloudConfigServer);

        String url =
            env.getProperty("spring.cloud.config.uri")
                + "/"
                + label
                + "/"
                + application
                + "-"
                + profile
                + ".yml";
        logger().debug("Config YAML Url = " + url);
        ResponseEntity<String> configprops =
            restTemplate.exchange(
                URI.create(url), HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
        String configpropsBody = configprops.getBody();
        logger().debug("Config YAML = " + configpropsBody);

        yamlConfigurationTextArea.setValue(configpropsBody);

        url =
            env.getProperty("spring.cloud.config.uri")
                + "/"
                + label
                + "/"
                + application
                + "-"
                + profile
                + ".properties";
        logger().debug("Config Properties Url = " + url);
        configprops =
            restTemplate.exchange(
                URI.create(url), HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
        configpropsBody = configprops.getBody();
        logger().debug("Config Properties = " + configpropsBody);

        propertiesConfigurationTextArea.setValue(configpropsBody);

        List<String[]> tableConfig = new ArrayList<>();
        Arrays.asList(configpropsBody.split("\n")).forEach(s -> tableConfig.add(s.split(": ")));
        tableConfigurationGrid.setItems(tableConfig);

        url =
            env.getProperty("spring.cloud.config.uri")
                + "/"
                + label
                + "/"
                + application
                + "-"
                + profile
                + ".json";
        logger().debug("Config JSON Url = " + url);
        configprops =
            restTemplate.exchange(
                URI.create(url), HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
        configpropsBody = configprops.getBody();
        logger().debug("Config JSON = " + configpropsBody);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        jsonConfigurationTextArea.setValue(gson.toJson(gson.fromJson(configpropsBody, Map.class)));
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}