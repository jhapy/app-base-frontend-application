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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.jhapy.cqrs.command.AbstractBaseCommand;
import org.jhapy.cqrs.command.notification.CreateSmsCommand;
import org.jhapy.cqrs.command.notification.DeleteSmsCommand;
import org.jhapy.cqrs.command.notification.UpdateSmsCommand;
import org.jhapy.cqrs.query.notification.GetSmsByIdQuery;
import org.jhapy.cqrs.query.notification.GetSmsByIdResponse;
import org.jhapy.dto.domain.notification.SmsActionEnum;
import org.jhapy.dto.domain.notification.SmsDTO;
import org.jhapy.dto.domain.notification.SmsStatusEnum;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.SmsDataProvider;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;

import java.util.UUID;

@I18NPageTitle(messageKey = AppConst.TITLE_SMS_ADMIN)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class SmsAdminView
    extends DefaultMasterDetailsView<SmsDTO, DefaultFilter, SearchQuery, SearchQueryResult> {

  public SmsAdminView(
      MyI18NProvider myI18NProvider, CommandGateway commandGateway, QueryGateway queryGateway) {
    super(
        "sms.",
        SmsDTO.class,
        null,
        false,
        e -> {
          AbstractBaseCommand command;
          if (e.getId() == null) {
            command = new CreateSmsCommand(e);
          } else {
            command = new UpdateSmsCommand(e.getId(), e);
          }
          UUID uuid = commandGateway.sendAndWait(command);
          var response =
              queryGateway
                  .query(
                      new GetSmsByIdQuery(e.getId() == null ? uuid : e.getId()),
                      GetSmsByIdResponse.class)
                  .join();
          return new ServiceResult<>(response.getData());
        },
        e -> {
          commandGateway.sendAndWait(new DeleteSmsCommand(e.getId()));
          return new ServiceResult<>();
        },
        myI18NProvider);
    lazyDataProvider = new SmsDataProvider(queryGateway);
    this.queryGateway = queryGateway;
  }

  @Override
  protected void afterSave(SmsDTO entity) {
    lazyDataProvider.refreshItem(entity);
  }

  @Override
  protected void afterDelete() {
    lazyDataProvider.refreshAll();
  }

  protected Grid<SmsDTO> createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));
    grid.setItems(lazyDataProvider);
    grid.setHeightFull();

    grid.addColumn(SmsDTO::getCreated).setKey("created");
    // grid.addColumn(Sms::getSmsAction).setKey("action");
    grid.addColumn(SmsDTO::getPhoneNumber).setKey("phoneNumber");
    grid.addColumn(SmsDTO::getSmsStatus).setKey("smsStatus");

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

  protected Component createDetails(SmsDTO sms) {
    var isNew = sms.getId() == null;
    detailsDrawerHeader.setTitle(
        isNew
            ? getTranslation("element.global.new") + " : "
            : getTranslation("element.global.update") + " : " + sms.getPhoneNumber());

    detailsDrawerFooter.setDeleteButtonVisible(false);

    var phoneNumberField = new TextField();
    phoneNumberField.setWidthFull();

    var bodyField = new TextArea();
    bodyField.setWidthFull();

    var smsActionField = new ComboBox<>();
    smsActionField.setItems(SmsActionEnum.values());

    var smsStatusField = new ComboBox<>();
    smsStatusField.setItems(SmsStatusEnum.values());

    var errorMessageField = new TextArea();
    errorMessageField.setWidthFull();

    var nbRetryField = new NumberField();

    // Form layout
    var editingForm = new FormLayout();
    editingForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

    editingForm.addFormItem(
        phoneNumberField, getTranslation("element." + I18N_PREFIX + "phoneNumber"));
    var bodyFieldItem =
        editingForm.addFormItem(bodyField, getTranslation("element." + I18N_PREFIX + "body"));
    editingForm.addFormItem(smsActionField, getTranslation("element." + I18N_PREFIX + "smsAction"));
    editingForm.addFormItem(smsStatusField, getTranslation("element." + I18N_PREFIX + "smsStatus"));
    editingForm.addFormItem(
        errorMessageField, getTranslation("element." + I18N_PREFIX + "errorMessage"));
    editingForm.addFormItem(nbRetryField, getTranslation("element." + I18N_PREFIX + "nbRetry"));

    UIUtils.setColSpan(2, phoneNumberField, bodyFieldItem);

    binder.setBean(sms);

    binder.bind(phoneNumberField, SmsDTO::getPhoneNumber, null);
    binder.bind(bodyField, SmsDTO::getBody, null);
    // binder.bind(smsActionField, Sms::getSmsAction, null);
    binder.bind(smsStatusField, SmsDTO::getSmsStatus, null);
    binder.bind(errorMessageField, SmsDTO::getErrorMessage, null);

    return editingForm;
  }

  protected void filter(String filter, Boolean showInactive) {
    lazyDataProvider.setFilter(
        new DefaultFilter(StringUtils.isBlank(filter) ? null : "*" + filter + "*", showInactive));
    lazyDataProvider.refreshAll();
  }
}
