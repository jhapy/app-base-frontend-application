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
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.jhapy.cqrs.command.AbstractBaseCommand;
import org.jhapy.cqrs.command.notification.CreateMailTemplateCommand;
import org.jhapy.cqrs.command.notification.DeleteMailTemplateCommand;
import org.jhapy.cqrs.command.notification.UpdateMailTemplateCommand;
import org.jhapy.cqrs.query.notification.GetMailTemplateByIdQuery;
import org.jhapy.cqrs.query.notification.GetMailTemplateByIdResponse;
import org.jhapy.dto.domain.notification.MailTemplateDTO;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultDetailsView;

import java.util.UUID;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-08-14
 */
@Tag("mail-template-admin-view")
@I18NPageTitle(messageKey = AppConst.TITLE_MAIL_TEMPLATE_ADMIN)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class MailTemplateAdminView extends DefaultDetailsView<MailTemplateDTO>
    implements HasUrlParameter<String> {

  private final QueryGateway queryGateway;
  private final CommandGateway commandGateway;

  public MailTemplateAdminView(
      MyI18NProvider myI18NProvider, CommandGateway commandGateway, QueryGateway queryGateway) {
    super(
        "mailTemplate.",
        MailTemplateDTO.class,
        MailTemplatesAdminView.class,
        e -> {
          AbstractBaseCommand command;
          if (e.getId() == null) {
            command = new CreateMailTemplateCommand(e);
          } else {
            command = new UpdateMailTemplateCommand(e.getId(), e);
          }
          UUID uuid = commandGateway.sendAndWait(command);
          var response =
              queryGateway.subscriptionQuery(
                  new GetMailTemplateByIdQuery(e.getId() == null ? uuid : e.getId()),
                  ResponseTypes.instanceOf(GetMailTemplateByIdResponse.class),
                  ResponseTypes.instanceOf(GetMailTemplateByIdResponse.class));

          return new ServiceResult<>(response.initialResult().block().getData());
        },
        e -> commandGateway.sendAndWait(new DeleteMailTemplateCommand(e.getId())),
        myI18NProvider);
    this.queryGateway = queryGateway;
    this.commandGateway = commandGateway;
  }

  @Override
  protected String getTitle(MailTemplateDTO entity) {
    if (entity.getName() != null) {
      return entity.getName();
    } else {
      return "";
    }
  }

  public Component createDetails(MailTemplateDTO entry) {
    var nameField = new TextField();
    nameField.setWidthFull();

    var subjectField = new TextField();
    subjectField.setWidthFull();

    var fromField = new EmailField();
    fromField.setWidthFull();

    var copyToField = new EmailField();
    copyToField.setWidthFull();

    var bodyField = new RichTextEditor();
    bodyField.setWidthFull();

    var bodyAsHtml = new TextField();
    bodyAsHtml.setVisible(false);
    bodyField.addValueChangeListener(
        value -> {
          try {
            bodyAsHtml.setValue(bodyField.getHtmlValue());
          } catch (Exception ignored) {
          }
        });

    var mailActionField = new TextField();
    mailActionField.setWidthFull();

    var editingForm = new FormLayout();
    editingForm.setWidthFull();
    editingForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

    editingForm.addFormItem(nameField, getTranslation("element." + I18N_PREFIX + "name"));
    editingForm.addFormItem(mailActionField, getTranslation("element." + I18N_PREFIX + "action"));
    editingForm.addFormItem(subjectField, getTranslation("element." + I18N_PREFIX + "subject"));
    editingForm.addFormItem(fromField, getTranslation("element." + I18N_PREFIX + "from"));
    editingForm.addFormItem(copyToField, getTranslation("element." + I18N_PREFIX + "copyTo"));

    var bodyFieldItem =
        editingForm.addFormItem(bodyField, getTranslation("element." + I18N_PREFIX + "body"));

    UIUtils.setColSpan(2, bodyFieldItem);

    binder.setBean(entry);

    binder
        .forField(nameField)
        .asRequired(getTranslation("message.error.nameRequired"))
        .bind(MailTemplateDTO::getName, MailTemplateDTO::setName);
    binder
        .forField(subjectField)
        .asRequired(getTranslation("message.error.subjectRequired"))
        .bind(MailTemplateDTO::getSubject, MailTemplateDTO::setSubject);
    binder
        .forField(fromField)
        .asRequired(getTranslation("message.error.fromRequired"))
        .bind(MailTemplateDTO::getFrom, MailTemplateDTO::setFrom);
    binder.bind(copyToField, MailTemplateDTO::getCopyTo, MailTemplateDTO::setCopyTo);
    binder
        .forField(bodyField.asHtml())
        .asRequired(getTranslation("message.error.bodyRequired"))
        .bind(MailTemplateDTO::getBody, MailTemplateDTO::setBody);
    binder.forField(bodyAsHtml).bind(MailTemplateDTO::getBodyHtml, MailTemplateDTO::setBodyHtml);
    binder
        .forField(mailActionField)
        .asRequired(getTranslation("message.error.mailAction"))
        .bind(MailTemplateDTO::getMailAction, MailTemplateDTO::setMailAction);

    return editingForm;
  }

  @Override
  public void setParameter(BeforeEvent event, String viewParameters) {
    super.setParameter(event, viewParameters);
    currentEditing = null;
    var id = viewParameters.equals("-1") ? null : UUID.fromString(viewParameters);
    if (id != null) {
      var mailTemplateDTO =
          queryGateway
              .query(
                  new GetMailTemplateByIdQuery(UUID.fromString(viewParameters)),
                  ResponseTypes.instanceOf(GetMailTemplateByIdResponse.class))
              .join()
              .getData();
      if (mailTemplateDTO != null) {
        currentEditing = mailTemplateDTO;
      }
    }
    if (currentEditing == null) {
      currentEditing = new MailTemplateDTO();
    }
  }
}
