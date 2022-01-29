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
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaApplicationInstance;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.frontend.components.FlexBoxLayout;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
@Tag("apiTabContent")
public class ApiTabContent extends ActuatorBaseView {

  protected FlexBoxLayout content;
  protected Component component;

  public ApiTabContent(UI ui, String I18N_PREFIX, AuthorizationHeaderUtil authorizationHeaderUtil) {
    super(ui, I18N_PREFIX + "apis.", authorizationHeaderUtil);
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

  protected void getDetails(
      EurekaApplication eurekaApplication, EurekaApplicationInstance eurekaApplicationInstance) {
    titleLabel.setText(
        getTranslation("element." + I18N_PREFIX + "title")
            + " - "
            + eurekaApplicationInstance.getInstanceId());
  }
}
