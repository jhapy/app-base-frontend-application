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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.tabs.Tab;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.springframework.core.env.Environment;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 11/06/2020
 */
@I18NPageTitle(messageKey = AppConst.TITLE_CLOUD_CONFIG_ADMIN)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class CloudConfigView extends ViewFrame implements HasLogger {

  private static final String I18N_PREFIX = "cloudConfig.";

  private final Environment env;
  private final AuthorizationHeaderUtil authorizationHeaderUtil;
  protected UI ui;
  private CloudConfigBaseView configurationTabContent;
  private CloudConfigBaseView encryptionTabContent;
  private Tab configurationTab;
  private Tab encryptionTab;

  public CloudConfigView(Environment env, AuthorizationHeaderUtil authorizationHeaderUtil) {
    this.env = env;
    this.authorizationHeaderUtil = authorizationHeaderUtil;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    ui = attachEvent.getUI();

    this.configurationTabContent =
        new CloudConfigurationTabContent(env, ui, I18N_PREFIX, authorizationHeaderUtil);
    this.encryptionTabContent =
        new EncryptionTabContent(env, ui, I18N_PREFIX, authorizationHeaderUtil);

    initAppBar();
  }

  private void initAppBar() {
    /*
    AppBar appBar = JHapyMainView3.get().getAppBar();
    configurationTab = appBar
        .addTab(getTranslation("element." + I18N_PREFIX + "tab.cloudConfig"));
    encryptionTab = appBar.addTab(getTranslation("element." + I18N_PREFIX + "tab.encryption"));

    appBar.addTabSelectionListener(e -> {
      if (e.getPreviousTab() != null) {
        getTab(e.getPreviousTab()).setRefreshRate(null);
      }

      setViewContent(getTab(e.getSelectedTab()).getContent());

    });
    setViewContent(configurationTabContent.getContent());
    appBar.centerTabs();

     */
  }

  protected CloudConfigBaseView getTab(Tab tab) {
    if (tab.equals(configurationTab)) {
      return configurationTabContent;
    } else if (tab.equals(encryptionTab)) {
      return encryptionTabContent;
    } else {
      return null;
    }
  }
}
