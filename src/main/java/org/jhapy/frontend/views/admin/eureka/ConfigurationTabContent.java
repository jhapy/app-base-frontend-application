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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaApplicationInstance;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.dto.registry.actuate.ConfigProps;
import org.jhapy.dto.registry.actuate.ConfigProps.Contexts;
import org.jhapy.dto.registry.actuate.ConfigProps.Contexts.Context;
import org.jhapy.dto.registry.actuate.ConfigProps.Contexts.Context.Bean;
import org.jhapy.dto.registry.actuate.Env;
import org.jhapy.dto.registry.actuate.Env.PropertySource;
import org.jhapy.dto.registry.actuate.Env.PropertySource.Property;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.utils.UIUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.*;
import org.vaadin.tabs.PagedTabs;

import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
@Tag("apiTabContent")
public class ConfigurationTabContent extends ActuatorBaseView {

  protected FlexBoxLayout content;
  protected Component component;

  protected Grid<Bean> beansGrid;
  protected ListDataProvider<Bean> beanListDataProvider;
  protected Map<String, Grid<PropertySource>> envGrid;
  protected FlexBoxLayout envRows;

  public ConfigurationTabContent(
      UI ui, String I18N_PREFIX, AuthorizationHeaderUtil authorizationHeaderUtil) {
    super(ui, I18N_PREFIX + "configurations.", authorizationHeaderUtil);
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

    PagedTabs tabs = new PagedTabs(content);
    content.add(tabs);

    tabs.add(getTranslation("element." + I18N_PREFIX + "tab.beans"), getBeans(), false);
    tabs.add(getTranslation("element." + I18N_PREFIX + "tab.env"), getEnv(), false);

    // getDetails( null, null);

    return content;
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
                  + ", Config Props Url = "
                  + eurekaApplicationInstance.getMetadata().get("management.url")
                  + "/configprops");
      ResponseEntity<String> configprops =
          restTemplate.exchange(
              URI.create(
                  eurekaApplicationInstance.getMetadata().get("management.url") + "/configprops"),
              HttpMethod.GET,
              new HttpEntity<>(httpHeaders),
              String.class);
      String configpropsBody = configprops.getBody();

      logger().debug("Config Props = " + configpropsBody);

      JSONParser jsonParser = new JSONParser();

      ObjectMapper objectMapper = new ObjectMapper();

      ConfigProps configPropsObj = new ConfigProps();
      Contexts contextsObj = new Contexts();
      contextsObj.setContexts(new HashMap<>());
      configPropsObj.setContexts(contextsObj);

      JSONObject configpropsJsonObject = (JSONObject) jsonParser.parse(configpropsBody);

      JSONObject contextsJsonObject = (JSONObject) configpropsJsonObject.get("contexts");
      contextsJsonObject.forEach(
          (key, contexts) -> {
            ConfigProps.Contexts.Context contextObj = new Context();
            contextObj.setBeans(new HashMap<>());
            JSONObject beansJsonObject = (JSONObject) ((JSONObject) contexts).get("beans");

            beansJsonObject.forEach(
                (o, o2) -> {
                  Bean beanObj = new Bean();
                  beanObj.setPrefix(((JSONObject) o2).get("prefix").toString());
                  try {
                    beanObj.setProperties(
                        objectMapper.readValue(
                            ((JSONObject) o2).get("properties").toString(),
                            new TypeReference<>() {}));
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                  contextObj.getBeans().put(o.toString(), beanObj);
                });
            contextsObj.getContexts().put(key.toString(), contextObj);
          });

      // logger().debug("Config Props Converted = " + configPropsObj);
      beanListDataProvider = new ListDataProvider<>(configPropsObj.getBeans());
      beansGrid.setDataProvider(beanListDataProvider);

      logger()
          .debug(
              "Application : "
                  + eurekaApplication.getName()
                  + ", Env Url = "
                  + eurekaApplicationInstance.getMetadata().get("management.url")
                  + "/env");
      ResponseEntity<String> env =
          restTemplate.exchange(
              URI.create(eurekaApplicationInstance.getMetadata().get("management.url") + "/env"),
              HttpMethod.GET,
              new HttpEntity<>(httpHeaders),
              String.class);
      String envBody = env.getBody();

      // logger().debug("Env = " + envBody);

      Env env1 = new Env();

      JSONObject envJsonObject = (JSONObject) jsonParser.parse(envBody);
      JSONArray activeProfilesJsonObject = (JSONArray) envJsonObject.get("activeProfiles");
      env1.setActiveProfiles(
          (String[])
              activeProfilesJsonObject.stream().map(Object::toString).toArray(String[]::new));
      JSONArray propertySourcesJsonObject = (JSONArray) envJsonObject.get("propertySources");
      env1.setPropertySources(
          (PropertySource[])
              propertySourcesJsonObject.stream()
                  .map(
                      o -> {
                        PropertySource propertySource = new PropertySource();
                        JSONObject propertySourceJsonObject = (JSONObject) o;
                        propertySource.setName(propertySourceJsonObject.get("name").toString());
                        propertySource.setProperties(new HashMap<>());
                        JSONObject properties =
                            (JSONObject) propertySourceJsonObject.get("properties");
                        properties.forEach(
                            (o1, o2) -> {
                              Property property = new Property();
                              JSONObject propertyJsonObject = (JSONObject) o2;
                              if (propertyJsonObject.get("value") != null) {
                                property.setValue(propertyJsonObject.get("value").toString());
                              }
                              if (propertyJsonObject.get("origin") != null) {
                                property.setOrigin(propertyJsonObject.get("origin").toString());
                              }
                              propertySource.getProperties().put(o1.toString(), property);
                            });
                        return propertySource;
                      })
                  .toArray(PropertySource[]::new));

      envRows.removeAll();

      List<ListDataProvider<Property>> dataProviders = new ArrayList<>();

      TextField filterTextField = new TextField();
      filterTextField.setLabel(getTranslation("element." + I18N_PREFIX + "filter"));
      filterTextField.addValueChangeListener(
          event ->
              dataProviders.forEach(
                  dataProvider ->
                      dataProvider.addFilter(
                          data ->
                              StringUtils.containsIgnoreCase(
                                      data.getName(), filterTextField.getValue())
                                  || StringUtils.containsIgnoreCase(
                                      data.getValue(), filterTextField.getValue()))));
      filterTextField.setValueChangeMode(ValueChangeMode.EAGER);

      envRows.add(filterTextField);

      for (PropertySource propertySource : env1.getPropertySources()) {
        envRows.add(UIUtils.createH5Label(propertySource.getName()));

        Grid<Property> gridProperty = new Grid();
        ListDataProvider<Property> listDataProvider =
            new ListDataProvider<>(propertySource.getPropertyList());
        dataProviders.add(listDataProvider);

        gridProperty.setDataProvider(listDataProvider);

        Column propertyColumn =
            gridProperty.addColumn(Property::getName).setWidth("150px").setKey("property");
        Column valueColumn =
            gridProperty.addColumn(Property::getValue).setAutoWidth(true).setKey("value");

        gridProperty
            .getColumns()
            .forEach(
                column -> {
                  if (column.getKey() != null) {
                    column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                    column.setResizable(true);
                  }
                });

        beansGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        envRows.add(gridProperty);
      }
      // ogger().debug("Env Converted = " + env1);

    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  protected Component getBeans() {
    beansGrid = new Grid<>();
    beansGrid.setWidthFull();
    beanListDataProvider = new ListDataProvider<>(Collections.emptyList());
    beansGrid.setDataProvider(beanListDataProvider);

    Column prefixColumn = beansGrid.addColumn(Bean::getPrefix).setWidth("150px").setKey("prefix");
    Column propertiesColumn =
        beansGrid.addComponentColumn(BeanComponent::new).setAutoWidth(true).setKey("properties");

    beansGrid
        .getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                column.setResizable(true);
              }
            });

    HeaderRow filterRow = beansGrid.appendHeaderRow();

    TextField prefixFilter = new TextField();
    prefixFilter.addValueChangeListener(
        event ->
            beanListDataProvider.addFilter(
                data -> StringUtils.containsIgnoreCase(data.getPrefix(), prefixFilter.getValue())));

    prefixFilter.setValueChangeMode(ValueChangeMode.EAGER);

    filterRow.getCell(prefixColumn).setComponent(prefixFilter);
    prefixFilter.setSizeFull();
    prefixFilter.setPlaceholder("Filter");

    TextField propertiesFilter = new TextField();
    propertiesFilter.addValueChangeListener(
        event ->
            beanListDataProvider.addFilter(
                data ->
                    StringUtils.containsIgnoreCase(
                        StringUtils.join(data.getProperties()), propertiesFilter.getValue())));

    propertiesFilter.setValueChangeMode(ValueChangeMode.EAGER);

    filterRow.getCell(propertiesColumn).setComponent(propertiesFilter);
    propertiesFilter.setSizeFull();
    propertiesFilter.setPlaceholder("Filter");

    beansGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    return beansGrid;
  }

  protected Component getEnv() {
    envRows = new FlexBoxLayout();
    envRows.setFlexDirection(FlexDirection.COLUMN);
    envRows.setWidthFull();

    return envRows;
  }

  public static class BeanComponent extends Div {

    public BeanComponent(Bean bean) {
      FlexBoxLayout rows = new FlexBoxLayout();
      rows.setFlexDirection(FlexDirection.COLUMN);
      rows.setWidthFull();

      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      bean.getProperties()
          .keySet()
          .forEach(
              s -> {
                FlexBoxLayout row = new FlexBoxLayout();
                row.setFlexDirection(FlexDirection.ROW);
                row.setJustifyContentMode(JustifyContentMode.BETWEEN);
                Label label = UIUtils.createH5Label(s);
                label.setWidth("33%");
                row.add(label);

                String v = gson.toJson(bean.getProperties().get(s));
                if (v.split("\n").length > 1) {
                  TextArea val = new TextArea();
                  val.setReadOnly(true);
                  val.setWidth("66%");
                  val.setValue(v);
                  row.add(val);

                  row.setFlex("1", val);
                } else {
                  TextField val = new TextField();
                  val.setReadOnly(true);
                  val.setWidth("66%");
                  val.setValue(v);
                  row.add(val);

                  row.setFlex("1", val);
                }
                rows.add(row);
              });

      add(rows);
    }
  }
}
