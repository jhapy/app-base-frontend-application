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

package org.jhapy.frontend.views.admin.swagger;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.registry.RegistryServices;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.css.BoxSizing;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.views.JHapyMainView3;

@I18NPageTitle(messageKey = AppConst.TITLE_SWAGGERS_ADMIN)
@RequiresRole(SecurityConst.ROLE_SWAGGER)
public class SwaggersAdminView extends ViewFrame {

  private static final String I18N_PREFIX = "swagger.";
  protected Grid<EurekaApplication> grid;
  private EurekaInfo eurekaInfo;

  public SwaggersAdminView() {}

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    setViewContent(createContent());

    ServiceResult<EurekaInfo> applicationServiceResult =
        RegistryServices.getEurekaService().getApplications(new BaseRemoteQuery());

    if (applicationServiceResult.getIsSuccess() && applicationServiceResult.getData() != null) {
      eurekaInfo = applicationServiceResult.getData();
      grid.setItems(eurekaInfo.getApplicationList());
    }
  }

  private Component createContent() {
    FlexBoxLayout content = new FlexBoxLayout(createGrid());
    content.setBoxSizing(BoxSizing.BORDER_BOX);
    content.setHeightFull();
    content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
    return content;
  }

  protected void showDetails(EurekaApplication app) {
    JHapyMainView3.get()
        .displayView(this, getParameter(), SwaggerAdminView.class, app.getName().toLowerCase());
  }

  protected Grid createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));

    grid.setHeight("100%");

    grid.addColumn(EurekaApplication::getName).setKey("name");

    grid.getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                column.setResizable(true);
                column.setSortable(true);
              }
            });
    return grid;
  }
}