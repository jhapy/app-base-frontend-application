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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.tabs.Tab;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.dto.registry.EurekaStatus;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.registry.RegistryServices;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.springframework.core.env.Environment;

@I18NPageTitle(messageKey = AppConst.TITLE_EUREKA_ADMIN)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class EurekaView extends ViewFrame implements HasLogger {

  private static final String I18N_PREFIX = "eureka.";
  private final Environment env;
  private final AuthorizationHeaderUtil authorizationHeaderUtil;
  protected UI ui;
  private EurekaInfo eurekaInfo;
  private ActuatorBaseView homeTabContent;
  private ActuatorBaseView eurekaInstancesTabContent;
  private ActuatorBaseView eurekaHistoryTabContent;
  private ActuatorBaseView loggersTabContent;
  private ActuatorBaseView logsTabContent;
  private ActuatorBaseView configurationsTabContent;
  private ActuatorBaseView meticsTabContent;
  private ActuatorBaseView healthTabContent;
  private ActuatorBaseView apisTabContent;
  private Tab home;
  private Tab eurekaHistory;
  private Tab instances;
  private Tab metrics;
  private Tab healths;
  private Tab configurations;
  private Tab loggers;
  private Tab logs;
  private Tab apis;

  public EurekaView(Environment env, AuthorizationHeaderUtil authorizationHeaderUtil) {
    this.env = env;
    this.authorizationHeaderUtil = authorizationHeaderUtil;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    ui = attachEvent.getUI();

    this.homeTabContent = new HomeTabContent(ui, I18N_PREFIX, authorizationHeaderUtil);
    this.eurekaInstancesTabContent =
        new EurekaInstancesTabContent(ui, I18N_PREFIX, authorizationHeaderUtil);
    this.eurekaHistoryTabContent =
        new EurekaHistoryTabContent(ui, I18N_PREFIX, authorizationHeaderUtil);
    this.healthTabContent = new HealthTabContent(ui, I18N_PREFIX, authorizationHeaderUtil);
    this.configurationsTabContent =
        new ConfigurationTabContent(ui, I18N_PREFIX, authorizationHeaderUtil);
    this.loggersTabContent = new LoggersTabContent(ui, I18N_PREFIX, authorizationHeaderUtil);
    this.logsTabContent = new LogsTabContent(ui, I18N_PREFIX, authorizationHeaderUtil);
    this.meticsTabContent = new MetricsTabContent(ui, I18N_PREFIX, authorizationHeaderUtil);
    this.apisTabContent = new ApiTabContent(ui, I18N_PREFIX, authorizationHeaderUtil);

    lookupData();

    initAppBar();
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);

    homeTabContent.setRefreshRate(null);
    eurekaInstancesTabContent.setRefreshRate(null);
    loggersTabContent.setRefreshRate(null);
    logsTabContent.setRefreshRate(null);
    meticsTabContent.setRefreshRate(null);
    apisTabContent.setRefreshRate(null);
  }

  private void initAppBar() {
    /*AppBar appBar = JHapyMainView3.get().getAppBar();
    home = appBar.addTab(getTranslation("element." + I18N_PREFIX + "tab.home"));
    eurekaHistory = appBar
        .addTab(getTranslation("element." + I18N_PREFIX + "tab.eurekaHistory"));
    instances = appBar.addTab(getTranslation("element." + I18N_PREFIX + "tab.instances"));
    metrics = appBar.addTab(getTranslation("element." + I18N_PREFIX + "tab.metrics"));
    healths = appBar.addTab(getTranslation("element." + I18N_PREFIX + "tab.healths"));
    configurations = appBar
        .addTab(getTranslation("element." + I18N_PREFIX + "tab.configurations"));
    loggers = appBar.addTab(getTranslation("element." + I18N_PREFIX + "tab.loggers"));
    logs = appBar.addTab(getTranslation("element." + I18N_PREFIX + "tab.logs"));
    // apis = appBar.addTab(getTranslation("element." + I18N_PREFIX + "tab.apis"));

    appBar.addTabSelectionListener(e -> {
      if (e.getPreviousTab() != null) {
        getTab(e.getPreviousTab()).setRefreshRate(null);
      }

      setViewContent(getTab(e.getSelectedTab()).getContent(eurekaInfo));

    });
    setViewContent(homeTabContent.getContent(eurekaInfo));
    appBar.centerTabs();

     */
  }

  protected ActuatorBaseView getTab(Tab tab) {
    if (tab.equals(home)) {
      return homeTabContent;
    } else if (tab.equals(eurekaHistory)) {
      return eurekaHistoryTabContent;
    } else if (tab.equals(instances)) {
      return eurekaInstancesTabContent;
    } else if (tab.equals(metrics)) {
      return meticsTabContent;
    } else if (tab.equals(healths)) {
      return healthTabContent;
    } else if (tab.equals(configurations)) {
      return configurationsTabContent;
    } else if (tab.equals(loggers)) {
      return loggersTabContent;
    } else if (tab.equals(logs)) {
      return logsTabContent;
    } else if (tab.equals(apis)) {
      return apisTabContent;
    } else {
      return null;
    }
  }

  protected void lookupData() {
    ServiceResult<EurekaInfo> applicationServiceResult =
        RegistryServices.getEurekaService().getApplications(new BaseRemoteQuery());

    if (applicationServiceResult.getIsSuccess() && applicationServiceResult.getData() != null) {
      eurekaInfo = applicationServiceResult.getData();

      ServiceResult<EurekaStatus> statusResult =
          RegistryServices.getEurekaService().status(new BaseRemoteQuery());
      if (statusResult.getIsSuccess() && statusResult.getData() != null) {
        eurekaInfo.setStatus(statusResult.getData());
      }
    }
  }

  protected Component createDetails(EurekaApplication eurekaApplication) {
    return new FormLayout();
  }
}
