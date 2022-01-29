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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaApplicationInstance;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.frontend.components.Badge;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.ListItem;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Right;
import org.jhapy.frontend.layout.size.Vertical;
import org.jhapy.frontend.utils.BoxShadowBorders;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.lumo.BadgeColor;

import java.util.Collections;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
@Tag("eurekaInstancesTabContent")
public class EurekaInstancesTabContent extends ActuatorBaseView {

  protected FlexBoxLayout content;
  protected Component component;

  protected EurekaInfo eurekaInfo;

  public EurekaInstancesTabContent(
      UI ui, String I18N_PREFIX, AuthorizationHeaderUtil authorizationHeaderUtil) {
    super(ui, I18N_PREFIX + "eurekaInstances.", authorizationHeaderUtil);
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
      EurekaApplication _eurekaApplication, EurekaApplicationInstance _eurekaApplicationInstance) {
    Grid<EurekaApplicationInstance> eurekaApplicationInstanceGrid = new Grid();
    eurekaApplicationInstanceGrid.setWidthFull();

    int idx = 0;
    Div items = new Div();
    items.addClassNames(BoxShadowBorders.BOTTOM, LumoStyles.Padding.Bottom.L);
    for (EurekaApplication eurekaApplication : eurekaInfo.getApplicationList()) {
      ListItem eurekaApplicationItem =
          new ListItem(
              eurekaApplication.getName(),
              new Badge(
                  String.valueOf(eurekaApplication.getInstances().size()), BadgeColor.SUCCESS));
      eurekaApplicationItem.addClickListener(
          flexLayoutClickEvent ->
              eurekaApplicationInstanceGrid.setItems(eurekaApplication.getInstances()));
      eurekaApplicationItem.setDividerVisible(++idx < eurekaApplication.getInstances().size());
      items.add(eurekaApplicationItem);
    }
    content.add(items);

    eurekaApplicationInstanceGrid.addColumn(EurekaApplicationInstance::getInstanceId);
    eurekaApplicationInstanceGrid.addComponentColumn(this::getInstanceStatus);

    eurekaApplicationInstanceGrid.setItems(Collections.EMPTY_LIST);

    content.add(eurekaApplicationInstanceGrid);

    if (content.getChildren().count() > 1) {
      if (component != null) {
        content.remove(component);
      }
    }

    component = eurekaApplicationInstanceGrid;
    content.add(component);
    content.setFlex("1", component);
  }

  protected Component getInstanceStatus(EurekaApplicationInstance eurekaApplicationInstance) {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setFlexDirection(FlexDirection.ROW);
    content.setPadding(Horizontal.RESPONSIVE_L, Vertical.S);
    content.setSpacing(Right.S);
    content.setAlignItems(Alignment.BASELINE);
    content.add(
        new Badge(
            eurekaApplicationInstance.getStatus(),
            eurekaApplicationInstance.getStatus().equalsIgnoreCase("UP")
                ? BadgeColor.SUCCESS
                : BadgeColor.ERROR));
    eurekaApplicationInstance
        .getMetadata()
        .keySet()
        .forEach(
            s -> {
              content.add(new Badge(s, BadgeColor.NORMAL));
              content.add(UIUtils.createH6Label(eurekaApplicationInstance.getMetadata().get(s)));
            });

    return content;
  }
}
