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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.shared.Registration;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.notification.SmsTemplate;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.notification.NotificationServices;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.dataproviders.DefaultDataProvider;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BoxSizing;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.views.JHapyMainView3;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-08-14
 */
@Tag("sms-template-admin-view")
@I18NPageTitle(messageKey = AppConst.TITLE_SMS_TEMPLATE_ADMIN)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class SmsTemplateAdminView extends ViewFrame implements HasUrlParameter<String> {

  private final Binder<SmsTemplate> binder = new Binder<>();
  private SmsTemplate smsTemplate;
  private DefaultDataProvider<SmsTemplate, DefaultFilter> securityUserDataProvider;
  private final Registration contextIconRegistration = null;

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    setViewContent(createContent());
    setViewFooter(getFooter());
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    if (contextIconRegistration != null) {
      contextIconRegistration.remove();
    }
  }

  protected Component getFooter() {
    Button saveButton = new Button(getTranslation("action.global.saveButton"));
    saveButton.addClickListener(event -> save());

    Button cancelButton = new Button(getTranslation("action.global.cancelButton"));
    cancelButton.addClickListener(event -> cancel());

    HorizontalLayout footer = new HorizontalLayout(saveButton, cancelButton);
    footer.setPadding(true);
    footer.setSpacing(true);

    return footer;
  }

  private Component createContent() {
    FlexBoxLayout content = new FlexBoxLayout(getSettingsForm());
    content.setBoxSizing(BoxSizing.BORDER_BOX);
    content.setHeightFull();
    content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
    return content;
  }

  public Component getSettingsForm() {
    FormLayout formLayout = new FormLayout();
    formLayout.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    formLayout.setResponsiveSteps(
        new ResponsiveStep("26em", 1, ResponsiveStep.LabelsPosition.TOP),
        new ResponsiveStep("32em", 2, ResponsiveStep.LabelsPosition.TOP));

    TextField nameField = new TextField();
    nameField.setWidthFull();

    TextArea bodyField = new TextArea();
    bodyField.setWidthFull();

    TextField smsActionField = new TextField();
    smsActionField.setWidthFull();

    formLayout.addFormItem(nameField, getTranslation("element.smsTemplate.name"));
    formLayout.addFormItem(smsActionField, getTranslation("element.smsTemplate.action"));
    FormItem bodyFieldItem =
        formLayout.addFormItem(bodyField, getTranslation("element.smsTemplate.body"));

    UIUtils.setColSpan(2, bodyFieldItem);

    binder
        .forField(nameField)
        .asRequired(getTranslation("message.error.nameRequired"))
        .bind(SmsTemplate::getName, SmsTemplate::setName);
    binder
        .forField(bodyField)
        .asRequired(getTranslation("message.error.bodyRequired"))
        .bind(SmsTemplate::getBody, SmsTemplate::setBody);
    binder
        .forField(smsActionField)
        .asRequired(getTranslation("message.error.smsAction"))
        .bind(SmsTemplate::getSmsAction, SmsTemplate::setSmsAction);

    Div content = new Div(formLayout);
    content.addClassName("grid-view");
    return content;
  }

  protected void save() {
    String id = binder.getBean().getId();

    boolean isNew = id == null;

    if (binder.writeBeanIfValid(smsTemplate)) {
      ServiceResult<SmsTemplate> _smsTemplate =
          NotificationServices.getSmsTemplateService().save(new SaveQuery<>(smsTemplate));
      if (_smsTemplate.getIsSuccess() && _smsTemplate.getData() != null) {
        JHapyMainView3.get()
            .displayInfoMessage(getTranslation("message.global.recordSavedMessage"));
        smsTemplate = _smsTemplate.getData();
        if (isNew) {
          JHapyMainView3.get()
              .displayViewFromParentView(
                  this, getParameter(), SmsTemplateAdminView.class, smsTemplate.getId());
        } else {
          binder.readBean(smsTemplate);
        }
      } else {
        JHapyMainView3.get()
            .displayErrorMessage(getTranslation("message.global.error", _smsTemplate.getMessage()));
        // Notification.show(getTranslation("message.global.error", _user.getMessage()), 3000,
        // Position.MIDDLE);
      }
    } else {
      JHapyMainView3.get()
          .displayErrorMessage(getTranslation("message.global.validationErrorMessage"));
      // Notification.show(getTranslation("message.global.validationErrorMessage"), 3000,
      // Notification.Position.BOTTOM_CENTER);
    }
  }

  protected void cancel() {
    binder.readBean(smsTemplate);
  }

  @Override
  public void setParameter(BeforeEvent event, String viewParameters) {
    super.setParameter(event, viewParameters);
    if (StringUtils.isBlank((String) viewParameters)) {
      smsTemplate = new SmsTemplate();
    } else {
      smsTemplate =
          NotificationServices.getSmsTemplateService()
              .getById(new GetByStrIdQuery((String) viewParameters))
              .getData();
      if (smsTemplate == null) {
        smsTemplate = new SmsTemplate();
      }
    }

    binder.setBean(smsTemplate);
  }

  private void goBack() {
    UI.getCurrent().navigate(SmsTemplatesAdminView.class);
  }
}