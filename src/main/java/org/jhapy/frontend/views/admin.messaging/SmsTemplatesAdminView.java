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

package org.jhapy.frontend.views.admin.messaging;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.jhapy.dto.domain.notification.SmsTemplateDTO;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.dataproviders.DefaultDataProvider;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.JHapyAbstractDataProvider;
import org.jhapy.frontend.dataproviders.SmsTemplateDataProvider;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterView;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-14
 */
@I18NPageTitle(messageKey = AppConst.TITLE_SMS_TEMPLATES_ADMIN)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class SmsTemplatesAdminView extends DefaultMasterView<SmsTemplateDTO, DefaultFilter> {

  protected final transient QueryGateway queryGateway;
  private final JHapyAbstractDataProvider<SmsTemplateDTO> dataProvider;

  public SmsTemplatesAdminView(
      MyI18NProvider myI18NProvider, CommandGateway commandGateway, QueryGateway queryGateway) {
    super(
        "smsTemplate.",
        SmsTemplateDTO.class,
        (DefaultDataProvider<SmsTemplateDTO, DefaultFilter>) null,
        SmsTemplateAdminView.class,
        myI18NProvider);
    this.queryGateway = queryGateway;
    this.dataProvider = new SmsTemplateDataProvider(queryGateway);
  }

  protected Grid<SmsTemplateDTO> createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));
    grid.setItems(dataProvider);
    grid.setHeightFull();

    grid.addColumn(SmsTemplateDTO::getName).setKey("name");
    grid.addColumn(SmsTemplateDTO::getSmsAction).setKey("smsAction");

    grid.getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                column.setResizable(true);
              }
            });
    return grid;
  }

  @Override
  protected void filter(String filter, Boolean showInactive) {
    dataProvider.setFilter(
        new DefaultFilter(
            StringUtils.isBlank(filter) ? null : "(?i).*" + filter + ".*", showInactive));
  }
}
