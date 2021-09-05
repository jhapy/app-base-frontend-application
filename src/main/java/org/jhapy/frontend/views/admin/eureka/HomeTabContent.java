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
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.dto.keycloak.MemoryInfo;
import org.jhapy.dto.keycloak.SystemInfo;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaApplicationInstance;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.frontend.client.registry.RegistryServices;
import org.jhapy.frontend.client.security.SecurityServices;
import org.jhapy.frontend.components.Badge;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.ListItem;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Vertical;
import org.jhapy.frontend.utils.BoxShadowBorders;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.lumo.BadgeColor;

import java.util.List;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
@Tag("apiTabContent")
public class HomeTabContent extends ActuatorBaseView {

  protected FlexBoxLayout content;
  protected Component homeContentFirstRow;
  protected Component homeContentSecondRow;
  protected Component homeContentThirdRow;
  protected EurekaInfo eurekaInfo;
  private List<String> replicas;

  public HomeTabContent(
      UI ui, String I18N_PREFIX, AuthorizationHeaderUtil authorizationHeaderUtil) {
    super(ui, I18N_PREFIX, authorizationHeaderUtil);
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

    getDetails(null, null);
    return content;
  }

  @Override
  public void refresh() {
    getDetails(null, null);
  }

  protected void getDetails(
      EurekaApplication eurekaApplication, EurekaApplicationInstance eurekaApplicationInstance) {
    ServiceResult<List<String>> replicasResult =
        RegistryServices.getEurekaService().replicas(new BaseRemoteQuery());
    if (replicasResult.getIsSuccess() && replicasResult.getData() != null) {
      replicas = replicasResult.getData();
    }

    if (content.getChildren().count() > 1) {
      if (homeContentFirstRow != null) {
        content.remove(homeContentFirstRow);
      }
      if (homeContentSecondRow != null) {
        content.remove(homeContentSecondRow);
      }
      if (homeContentThirdRow != null) {
        content.remove(homeContentThirdRow);
      }
    }

    content.add(homeContentFirstRow = getHomeContentFirstRow());
    content.add(homeContentSecondRow = getHomeContentSecondRow());
    content.add(homeContentThirdRow = getHomeContentThirdRow());
  }

  protected Component getHomeContentFirstRow() {
    Row docs = new Row(createEurekaInfoContent(), createGeneralInfoContent());
    docs.addClassName(LumoStyles.Margin.Top.XL);
    docs.setWidthFull();

    return docs;
  }

  protected Component createEurekaInfoContent() {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
    content.setWidthFull();

    Label header = UIUtils.createH3Label(getTranslation("element." + I18N_PREFIX + "eurekaInfo"));
    header.addClassNames(LumoStyles.Margin.Vertical.L, LumoStyles.Margin.Responsive.Horizontal.L);
    content.add(header);

    Div items = new Div();
    items.addClassNames(BoxShadowBorders.BOTTOM, LumoStyles.Padding.Bottom.L);

    ListItem environmentItem =
        new ListItem(
            getTranslation("element." + I18N_PREFIX + "eurekaInfo.status.environment"),
            UIUtils.createH5Label(eurekaInfo.getStatus().getEnvironment()));
    environmentItem.setDividerVisible(true);
    items.add(environmentItem);

    ListItem datacenterItem =
        new ListItem(
            getTranslation("element." + I18N_PREFIX + "eurekaInfo.status.datacenter"),
            UIUtils.createH5Label(eurekaInfo.getStatus().getDatacenter()));
    datacenterItem.setDividerVisible(true);
    items.add(datacenterItem);

    ListItem currentTimeItem =
        new ListItem(
            getTranslation("element." + I18N_PREFIX + "eurekaInfo.status.currentTime"),
            UIUtils.createH5Label(eurekaInfo.getStatus().getCurrentTime()));
    currentTimeItem.setDividerVisible(true);
    items.add(currentTimeItem);

    ListItem systemUpTimeItem =
        new ListItem(
            getTranslation("element." + I18N_PREFIX + "eurekaInfo.status.systemUpTime"),
            UIUtils.createH5Label(eurekaInfo.getStatus().getUpTime()));
    systemUpTimeItem.setDividerVisible(true);
    items.add(systemUpTimeItem);

    ListItem belowRenewThreshold =
        new ListItem(
            getTranslation("element." + I18N_PREFIX + "eurekaInfo.status.belowRenewThreshold"),
            UIUtils.createH5Label(
                Boolean.toString(eurekaInfo.getStatus().getIsBelowRenewThreshold())));
    belowRenewThreshold.setDividerVisible(false);
    items.add(belowRenewThreshold);

    content.add(items);
    return content;
  }

  protected Component createGeneralInfoContent() {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
    content.setWidthFull();

    Label header = UIUtils.createH3Label(getTranslation("element." + I18N_PREFIX + "generalInfo"));
    header.addClassNames(LumoStyles.Margin.Vertical.L, LumoStyles.Margin.Responsive.Horizontal.L);
    content.add(header);

    Div items = new Div();
    items.addClassNames(BoxShadowBorders.BOTTOM, LumoStyles.Padding.Bottom.L);

    ListItem instanceInfoStatusItem =
        new ListItem(
            getTranslation("element." + I18N_PREFIX + "eurekaInfo.status.instanceInfoStatus"),
            new Badge(
                eurekaInfo.getStatus().getInstanceInfoStatus(),
                eurekaInfo.getStatus().getInstanceInfoStatus().equalsIgnoreCase("UP")
                    ? BadgeColor.SUCCESS
                    : BadgeColor.ERROR));
    instanceInfoStatusItem.setDividerVisible(true);
    items.add(instanceInfoStatusItem);

    ListItem instanceInfoIpAddrItem =
        new ListItem(
            getTranslation("element." + I18N_PREFIX + "eurekaInfo.status.instanceInfoIpAddr"),
            UIUtils.createH5Label(eurekaInfo.getStatus().getInstanceInfoIpAddr()));
    instanceInfoIpAddrItem.setDividerVisible(true);
    items.add(instanceInfoIpAddrItem);

    ListItem numOfCpusItem =
        new ListItem(
            getTranslation("element." + I18N_PREFIX + "eurekaInfo.status.num-of-cpus"),
            UIUtils.createH5Label(eurekaInfo.getStatus().getGeneralStats().get("num-of-cpus")));
    numOfCpusItem.setDividerVisible(true);
    items.add(numOfCpusItem);

    ListItem totalAvailMemoryItem =
        new ListItem(
            getTranslation("element." + I18N_PREFIX + "eurekaInfo.status.total-avail-memory"),
            UIUtils.createH5Label(
                eurekaInfo.getStatus().getGeneralStats().get("total-avail-memory")));
    totalAvailMemoryItem.setDividerVisible(true);
    items.add(totalAvailMemoryItem);

    ListItem currentMemoryUsageItem =
        new ListItem(
            getTranslation("element." + I18N_PREFIX + "eurekaInfo.status.current-memory-usage"),
            UIUtils.createH5Label(
                eurekaInfo.getStatus().getGeneralStats().get("current-memory-usage")));
    currentMemoryUsageItem.setDividerVisible(false);
    items.add(currentMemoryUsageItem);

    content.add(items);

    return content;
  }

  protected Component getHomeContentSecondRow() {
    Row docs = new Row(getEurekaInstances(), getEurekaReplicas());
    docs.addClassName(LumoStyles.Margin.Top.XL);
    docs.setWidthFull();

    return docs;
  }

  protected Component getEurekaInstances() {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setWidthFull();
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);

    Label header = UIUtils.createH3Label(getTranslation("element." + I18N_PREFIX + "instances"));
    header.addClassNames(LumoStyles.Margin.Vertical.L, LumoStyles.Margin.Responsive.Horizontal.L);
    content.add(header);

    Div items = new Div();
    items.addClassNames(BoxShadowBorders.BOTTOM, LumoStyles.Padding.Bottom.L);

    int idx = 0;
    for (EurekaApplication eurekaApplication : eurekaInfo.getApplicationList()) {
      for (EurekaApplicationInstance eurekaApplicationInstance : eurekaApplication.getInstances()) {
        ListItem instanceInfoStatusItem =
            new ListItem(
                eurekaApplication.getName(),
                eurekaApplicationInstance.getInstanceId(),
                new Badge(
                    eurekaApplicationInstance.getStatus(),
                    eurekaApplicationInstance.getStatus().equalsIgnoreCase("UP")
                        ? BadgeColor.SUCCESS
                        : BadgeColor.ERROR));
        instanceInfoStatusItem.setDividerVisible(++idx < eurekaInfo.getApplicationList().size());
        items.add(instanceInfoStatusItem);
      }
    }
    content.add(items);

    return content;
  }

  protected Component getEurekaReplicas() {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setWidthFull();
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);

    Label header = UIUtils.createH3Label(getTranslation("element." + I18N_PREFIX + "replicas"));
    header.addClassNames(LumoStyles.Margin.Vertical.L, LumoStyles.Margin.Responsive.Horizontal.L);
    content.add(header);

    Div items = new Div();
    items.addClassNames(BoxShadowBorders.BOTTOM, LumoStyles.Padding.Bottom.L);

    int idx = 0;
    for (String replica : replicas) {
      ListItem replicaItem = new ListItem(replica);
      replicaItem.setDividerVisible(++idx < replicas.size());
      items.add(replicaItem);
    }
    content.add(items);

    return content;
  }

  protected Component getHomeContentThirdRow() {
    Row docs = new Row(getKeycloakServerInfo(), getKeycloakMemoryInfo());
    docs.addClassName(LumoStyles.Margin.Top.XL);
    docs.setWidthFull();

    return docs;
  }

  protected Component getKeycloakServerInfo() {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setWidthFull();
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);

    Label header =
        UIUtils.createH3Label(getTranslation("element." + I18N_PREFIX + "keycloakServerInfo"));
    header.addClassNames(LumoStyles.Margin.Vertical.L, LumoStyles.Margin.Responsive.Horizontal.L);
    content.add(header);

    Div items = new Div();
    items.addClassNames(BoxShadowBorders.BOTTOM, LumoStyles.Padding.Bottom.L);

    ServiceResult<SystemInfo> systemInfoServiceResult =
        SecurityServices.getKeycloakClient().getServerSystemInfo();
    if (systemInfoServiceResult.getIsSuccess() && systemInfoServiceResult.getData() != null) {
      SystemInfo systemInfo = systemInfoServiceResult.getData();
      ListItem versionItem =
          new ListItem(
              getTranslation("element." + I18N_PREFIX + "keycloakinfo.version"),
              UIUtils.createH5Label(systemInfo.getVersion()));
      versionItem.setDividerVisible(true);
      items.add(versionItem);

      ListItem serverTimeItem =
          new ListItem(
              getTranslation("element." + I18N_PREFIX + "keycloakinfo.serverTime"),
              UIUtils.createH5Label(systemInfo.getServerTime()));
      serverTimeItem.setDividerVisible(true);
      items.add(serverTimeItem);

      ListItem upTimeItem =
          new ListItem(
              getTranslation("element." + I18N_PREFIX + "keycloakinfo.upTime"),
              UIUtils.createH5Label(systemInfo.getUptime()));
      upTimeItem.setDividerVisible(false);
      items.add(upTimeItem);
    }
    content.add(items);

    return content;
  }

  protected Component getKeycloakMemoryInfo() {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setWidthFull();
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);

    Label header =
        UIUtils.createH3Label(getTranslation("element." + I18N_PREFIX + "keycloakMemoryInfo"));
    header.addClassNames(LumoStyles.Margin.Vertical.L, LumoStyles.Margin.Responsive.Horizontal.L);
    content.add(header);

    Div items = new Div();
    items.addClassNames(BoxShadowBorders.BOTTOM, LumoStyles.Padding.Bottom.L);

    ServiceResult<MemoryInfo> memoryInfoServiceResult =
        SecurityServices.getKeycloakClient().getServerMemoryInfo();
    if (memoryInfoServiceResult.getIsSuccess() && memoryInfoServiceResult.getData() != null) {
      MemoryInfo memoryInfo = memoryInfoServiceResult.getData();
      ListItem totalMemoryItem =
          new ListItem(
              getTranslation("element." + I18N_PREFIX + "keycloakinfo.totalMemory"),
              UIUtils.createH5Label(memoryInfo.getTotal()));
      totalMemoryItem.setDividerVisible(true);
      items.add(totalMemoryItem);

      ListItem freeMemoryItem =
          new ListItem(
              getTranslation("element." + I18N_PREFIX + "keycloakinfo.freeMemory"),
              UIUtils.createH5Label(memoryInfo.getFree()));
      freeMemoryItem.setDividerVisible(true);
      items.add(freeMemoryItem);

      ListItem userMemoryItem =
          new ListItem(
              getTranslation("element." + I18N_PREFIX + "keycloakinfo.userMemory"),
              UIUtils.createH5Label(memoryInfo.getUsed()));
      userMemoryItem.setDividerVisible(false);
      items.add(userMemoryItem);
    }
    content.add(items);

    return content;
  }
}