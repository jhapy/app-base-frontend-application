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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.jhapy.cqrs.command.AbstractBaseCommand;
import org.jhapy.cqrs.command.notification.CreateSmsTemplateCommand;
import org.jhapy.cqrs.command.notification.DeleteSmsTemplateCommand;
import org.jhapy.cqrs.command.notification.UpdateSmsTemplateCommand;
import org.jhapy.cqrs.query.notification.GetSmsTemplateByIdQuery;
import org.jhapy.cqrs.query.notification.GetSmsTemplateByIdResponse;
import org.jhapy.dto.domain.notification.SmsTemplateDTO;
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
@Tag("sms-template-admin-view")
@I18NPageTitle(messageKey = AppConst.TITLE_SMS_TEMPLATE_ADMIN)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class SmsTemplateAdminView extends DefaultDetailsView<SmsTemplateDTO>
    implements HasUrlParameter<String> {

  private final QueryGateway queryGateway;
  private final CommandGateway commandGateway;

  public SmsTemplateAdminView(
      MyI18NProvider myI18NProvider, CommandGateway commandGateway, QueryGateway queryGateway) {
    super(
        "smsTemplate.",
        SmsTemplateDTO.class,
        SmsTemplatesAdminView.class,
        e -> {
          AbstractBaseCommand command;
          if (e.getId() == null) {
            command = new CreateSmsTemplateCommand(e);
          } else {
            command = new UpdateSmsTemplateCommand(e.getId(), e);
          }
          UUID uuid = commandGateway.sendAndWait(command);
          var response =
              queryGateway.subscriptionQuery(
                  new GetSmsTemplateByIdQuery(e.getId() == null ? uuid : e.getId()),
                  ResponseTypes.instanceOf(GetSmsTemplateByIdResponse.class),
                  ResponseTypes.instanceOf(GetSmsTemplateByIdResponse.class));

          return new ServiceResult<>(response.initialResult().block().getData());
        },
        e -> commandGateway.sendAndWait(new DeleteSmsTemplateCommand(e.getId())),
        myI18NProvider);
    this.queryGateway = queryGateway;
    this.commandGateway = commandGateway;
  }

  @Override
  protected String getTitle(SmsTemplateDTO entity) {
    if (entity.getName() != null) {
      return entity.getName();
    } else {
      return "";
    }
  }

  public Component createDetails(SmsTemplateDTO entry) {
    var nameField = new TextField();
    nameField.setWidthFull();

    var bodyField = new TextArea();
    bodyField.setWidthFull();

    var smsActionField = new TextField();
    smsActionField.setWidthFull();

    var editingForm = new FormLayout();
    editingForm.setWidthFull();
    editingForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

    editingForm.addFormItem(nameField, getTranslation("element." + I18N_PREFIX + "name"));
    editingForm.addFormItem(smsActionField, getTranslation("element." + I18N_PREFIX + "action"));
    var bodyFieldItem =
        editingForm.addFormItem(bodyField, getTranslation("element." + I18N_PREFIX + "body"));

    UIUtils.setColSpan(2, bodyFieldItem);

    binder.setBean(entry);

    binder
        .forField(nameField)
        .asRequired(getTranslation("message.error.nameRequired"))
        .bind(SmsTemplateDTO::getName, SmsTemplateDTO::setName);
    binder
        .forField(bodyField)
        .asRequired(getTranslation("message.error.bodyRequired"))
        .bind(SmsTemplateDTO::getBody, SmsTemplateDTO::setBody);
    binder
        .forField(smsActionField)
        .asRequired(getTranslation("message.error.smsAction"))
        .bind(SmsTemplateDTO::getSmsAction, SmsTemplateDTO::setSmsAction);

    return editingForm;
  }

  @Override
  public void setParameter(BeforeEvent event, String viewParameters) {
    super.setParameter(event, viewParameters);
    currentEditing = null;
    var id = viewParameters.equals("-1") ? null : UUID.fromString(viewParameters);
    if (id != null) {
      var smsTemplateDTO =
          queryGateway
              .query(
                  new GetSmsTemplateByIdQuery(UUID.fromString(viewParameters)),
                  ResponseTypes.instanceOf(GetSmsTemplateByIdResponse.class))
              .join()
              .getData();
      if (smsTemplateDTO != null) {
        currentEditing = smsTemplateDTO;
      }
    }
    if (currentEditing == null) {
      currentEditing = new SmsTemplateDTO();
    }
  }
}
