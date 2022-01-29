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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaApplicationInstance;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.dto.registry.InstanceLeases;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.frontend.client.registry.RegistryServices;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.vaadin.tabs.PagedTabs;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
@Tag("apiTabContent")
public class EurekaHistoryTabContent extends ActuatorBaseView {

  protected FlexBoxLayout content;
  protected Component component;
  protected List<InstanceLeases> lastRegisteredLeases;
  protected List<InstanceLeases> cancelledLeases;

  protected Grid<InstanceLeases> lastRegisteredLeasesGrid;
  protected Grid<InstanceLeases> cancelledLeasesGrid;

  public EurekaHistoryTabContent(
      UI ui, String I18N_PREFIX, AuthorizationHeaderUtil authorizationHeaderUtil) {
    super(ui, I18N_PREFIX + "eurekaHistory.", authorizationHeaderUtil);
  }

  public Component getContent(EurekaInfo eurekaInfo) {
    this.eurekaInfo = eurekaInfo;
    content =
        new FlexBoxLayout(
            createHeader(
                VaadinIcon.SEARCH,
                getTranslation("element." + I18N_PREFIX + "title"),
                getEurekaInstancesList(false, eurekaInfo.getApplicationList(), this::getDetails)));
    content.setAlignItems(FlexComponent.Alignment.CENTER);
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setSizeFull();

    PagedTabs tabs = new PagedTabs(content);
    content.add(tabs);

    tabs.add(
        getTranslation(I18N_PREFIX + "lastRegisteredLeases"), getLastRegisteredLeases(), false);
    tabs.add(getTranslation(I18N_PREFIX + "cancelledLeases"), getCancelledLeases(), false);

    getDetails(null, null);

    return content;
  }

  protected void getDetails(
      EurekaApplication eurekaApplication, EurekaApplicationInstance eurekaApplicationInstance) {
    ServiceResult<Map<String, List<String[]>>> lastnResult =
        RegistryServices.getEurekaService().lastn(new BaseRemoteQuery());
    if (lastnResult.getIsSuccess() && lastnResult.getData() != null) {
      List<String[]> canceledMap = lastnResult.getData().get("canceled");
      cancelledLeases = new ArrayList<>();
      canceledMap.forEach(
          strings -> cancelledLeases.add(new InstanceLeases(Long.valueOf(strings[0]), strings[1])));
      List<String[]> registeredMap = lastnResult.getData().get("registered");
      lastRegisteredLeases = new ArrayList<>();
      registeredMap.forEach(
          strings ->
              lastRegisteredLeases.add(new InstanceLeases(Long.valueOf(strings[0]), strings[1])));
      cancelledLeasesGrid.setItems(cancelledLeases);
      lastRegisteredLeasesGrid.setItems(lastRegisteredLeases);
    }
  }

  protected Component getLastRegisteredLeases() {
    lastRegisteredLeasesGrid = new Grid<>();
    lastRegisteredLeasesGrid.setWidthFull();

    lastRegisteredLeasesGrid
        .addColumn(
            instanceLeases ->
                FastDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                    .format(new Date(instanceLeases.getTime())))
        .setWidth("200px")
        .setKey("time");
    lastRegisteredLeasesGrid
        .addColumn(InstanceLeases::getInstance)
        .setKey("instance")
        .setAutoWidth(true);

    lastRegisteredLeasesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    lastRegisteredLeasesGrid
        .getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                column.setResizable(true);
              }
            });

    return lastRegisteredLeasesGrid;
  }

  protected Component getCancelledLeases() {
    cancelledLeasesGrid = new Grid<>();
    cancelledLeasesGrid.setWidthFull();

    cancelledLeasesGrid
        .addColumn(
            instanceLeases ->
                FastDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                    .format(new Date(instanceLeases.getTime())))
        .setWidth("200px")
        .setKey("time");
    cancelledLeasesGrid
        .addColumn(InstanceLeases::getInstance)
        .setKey("instance")
        .setAutoWidth(true);

    cancelledLeasesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    cancelledLeasesGrid
        .getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                column.setResizable(true);
              }
            });

    return cancelledLeasesGrid;
  }
}
