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
import org.jhapy.cqrs.command.notification.CreateMailCommand;
import org.jhapy.cqrs.command.notification.DeleteMailCommand;
import org.jhapy.cqrs.command.notification.UpdateMailCommand;
import org.jhapy.cqrs.query.notification.GetMailByIdQuery;
import org.jhapy.cqrs.query.notification.GetMailByIdResponse;
import org.jhapy.dto.domain.notification.MailDTO;
import org.jhapy.dto.domain.notification.MailStatusEnum;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.customFields.AttachmentField;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.MailDataProvider;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;

import java.util.UUID;

@I18NPageTitle(messageKey = AppConst.TITLE_MAILS_ADMIN)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class MailAdminView
    extends DefaultMasterDetailsView<MailDTO, DefaultFilter, SearchQuery, SearchQueryResult> {

  public MailAdminView(
      MyI18NProvider myI18NProvider, CommandGateway commandGateway, QueryGateway queryGateway) {
    super(
        "mail.",
        MailDTO.class,
        null,
        false,
        e -> {
          AbstractBaseCommand command;
          if (e.getId() == null) {
            command = new CreateMailCommand(e);
          } else {
            command = new UpdateMailCommand(e.getId(), e);
          }
          UUID uuid = commandGateway.sendAndWait(command);
          var response =
              queryGateway
                  .query(
                      new GetMailByIdQuery(e.getId() == null ? uuid : e.getId()),
                      GetMailByIdResponse.class)
                  .join();
          return new ServiceResult<>(response.getData());
        },
        e -> {
          commandGateway.sendAndWait(new DeleteMailCommand(e.getId()));
          return new ServiceResult<>();
        },
        myI18NProvider);
    lazyDataProvider = new MailDataProvider(queryGateway);
    this.queryGateway = queryGateway;
  }

  @Override
  protected void afterSave(MailDTO entity) {
    lazyDataProvider.refreshItem(entity);
  }

  @Override
  protected void afterDelete() {
    lazyDataProvider.refreshAll();
  }

  protected Grid<MailDTO> createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));
    grid.setItems(lazyDataProvider);
    grid.setHeightFull();

    grid.addColumn(MailDTO::getCreated).setKey("created");
    // grid.addColumn(Mail::getMailAction).setKey("action");
    grid.addColumn(MailDTO::getTo).setKey("to");
    grid.addColumn(MailDTO::getMailStatus).setKey("mailStatus");

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

  protected Component createDetails(MailDTO mail) {
    var isNew = mail.getId() == null;
    detailsDrawerHeader.setTitle(
        isNew
            ? getTranslation("element.global.new") + " : "
            : getTranslation("element.global.update") + " : " + mail.getTo());

    detailsDrawerFooter.setDeleteButtonVisible(false);

    var fromField = new TextField();
    fromField.setWidthFull();

    var toField = new TextField();
    toField.setWidthFull();

    var copyToField = new TextField();
    copyToField.setWidthFull();

    var subjectField = new TextField();
    subjectField.setWidthFull();

    var bodyField = new TextArea();
    bodyField.setWidthFull();

    var attachmentField = new AttachmentField(queryGateway);
    attachmentField.setWidthFull();

    var mailActionField = new TextField();
    mailActionField.setWidthFull();

    var mailStatusField = new ComboBox<>();
    mailStatusField.setItems(MailStatusEnum.values());

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

    var fromFieldItem =
        editingForm.addFormItem(fromField, getTranslation("element." + I18N_PREFIX + "from"));
    var toFieldItem =
        editingForm.addFormItem(toField, getTranslation("element." + I18N_PREFIX + "to"));
    var copyToFieldItem =
        editingForm.addFormItem(copyToField, getTranslation("element." + I18N_PREFIX + "copyTo"));
    var subjectFieldItem =
        editingForm.addFormItem(subjectField, getTranslation("element." + I18N_PREFIX + "subject"));
    var bodyFieldItem =
        editingForm.addFormItem(bodyField, getTranslation("element." + I18N_PREFIX + "body"));
    var attachmentFieldItem =
        editingForm.addFormItem(
            attachmentField, getTranslation("element." + I18N_PREFIX + "attachment"));
    editingForm.addFormItem(
        mailActionField, getTranslation("element." + I18N_PREFIX + "mailAction"));
    editingForm.addFormItem(
        mailStatusField, getTranslation("element." + I18N_PREFIX + "mailStatus"));
    editingForm.addFormItem(
        errorMessageField, getTranslation("element." + I18N_PREFIX + "errorMessage"));
    editingForm.addFormItem(nbRetryField, getTranslation("element." + I18N_PREFIX + "nbRetry"));

    UIUtils.setColSpan(
        2,
        fromFieldItem,
        toFieldItem,
        copyToFieldItem,
        subjectFieldItem,
        bodyFieldItem,
        attachmentFieldItem);

    binder.setBean(mail);

    binder.bind(fromField, MailDTO::getFrom, null);
    binder.bind(toField, MailDTO::getTo, null);
    binder.bind(copyToField, MailDTO::getCopyTo, null);
    binder.bind(subjectField, MailDTO::getSubject, null);
    binder.bind(bodyField, MailDTO::getBody, null);
    // binder.bind(attachmentField, Mail::getAttachements, Mail::setAttachements);
    // binder.bind(mailActionField, Mail::getMailAction, null);
    binder.bind(mailStatusField, MailDTO::getMailStatus, null);
    binder.bind(errorMessageField, MailDTO::getErrorMessage, null);
    binder.bind(nbRetryField, MailDTO::getNbRetry, null);

    return editingForm;
  }

  protected void filter(String filter, Boolean showInactive) {
    lazyDataProvider.setFilter(
        new DefaultFilter(StringUtils.isBlank(filter) ? null : "*" + filter + "*", showInactive));
    lazyDataProvider.refreshAll();
  }
}
