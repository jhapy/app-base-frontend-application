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
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.shared.Registration;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.notification.MailTemplate;
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
@Tag("mail-template-admin-view")
@I18NPageTitle(messageKey = AppConst.TITLE_MAIL_TEMPLATE_ADMIN)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class MailTemplateAdminView extends ViewFrame implements HasUrlParameter<String> {

  private final Binder<MailTemplate> binder = new Binder<>();
  private MailTemplate mailTemplate;
  private DefaultDataProvider<MailTemplate, DefaultFilter> securityUserDataProvider;
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

    TextField subjectField = new TextField();
    subjectField.setWidthFull();

    EmailField fromField = new EmailField();
    fromField.setWidthFull();

    EmailField copyToField = new EmailField();
    copyToField.setWidthFull();

    RichTextEditor bodyField = new RichTextEditor();
    bodyField.setWidthFull();

    TextField bodyAsHtml = new TextField();
    bodyAsHtml.setVisible(false);
    bodyField.addValueChangeListener(
        value -> {
          try {
            bodyAsHtml.setValue(bodyField.getHtmlValue());
          } catch (Exception ignored) {
          }
        });

    TextField mailActionField = new TextField();
    mailActionField.setWidthFull();

    formLayout.addFormItem(nameField, getTranslation("element.mailTemplate.name"));
    formLayout.addFormItem(mailActionField, getTranslation("element.mailTemplate.action"));
    formLayout.addFormItem(subjectField, getTranslation("element.mailTemplate.subject"));
    formLayout.addFormItem(fromField, getTranslation("element.mailTemplate.from"));
    formLayout.addFormItem(copyToField, getTranslation("element.mailTemplate.copyTo"));

    FormItem bodyFieldItem =
        formLayout.addFormItem(bodyField, getTranslation("element.mailTemplate.body"));

    UIUtils.setColSpan(2, bodyFieldItem);

    binder
        .forField(nameField)
        .asRequired(getTranslation("message.error.nameRequired"))
        .bind(MailTemplate::getName, MailTemplate::setName);
    binder
        .forField(subjectField)
        .asRequired(getTranslation("message.error.subjectRequired"))
        .bind(MailTemplate::getSubject, MailTemplate::setSubject);
    binder
        .forField(fromField)
        .asRequired(getTranslation("message.error.fromRequired"))
        .bind(MailTemplate::getFrom, MailTemplate::setFrom);
    binder.bind(copyToField, MailTemplate::getCopyTo, MailTemplate::setCopyTo);
    binder
        .forField(bodyField.asHtml())
        .asRequired(getTranslation("message.error.bodyRequired"))
        .bind(MailTemplate::getBody, MailTemplate::setBody);
    binder.forField(bodyAsHtml).bind(MailTemplate::getBodyHtml, MailTemplate::setBodyHtml);
    binder
        .forField(mailActionField)
        .asRequired(getTranslation("message.error.mailAction"))
        .bind(MailTemplate::getMailAction, MailTemplate::setMailAction);

    Div content = new Div(formLayout);
    content.addClassName("grid-view");
    return content;
  }

  protected void save() {
    String id = binder.getBean().getId();

    boolean isNew = id == null;

    if (binder.writeBeanIfValid(mailTemplate)) {
      ServiceResult<MailTemplate> _mailTemplate =
          NotificationServices.getMailTemplateService().save(new SaveQuery<>(mailTemplate));
      if (_mailTemplate.getIsSuccess() && _mailTemplate.getData() != null) {
        JHapyMainView3.get()
            .displayInfoMessage(getTranslation("message.global.recordSavedMessage"));
        mailTemplate = _mailTemplate.getData();
        if (isNew) {
          JHapyMainView3.get()
              .displayViewFromParentView(
                  this, getParameter(), MailTemplateAdminView.class, mailTemplate.getId());
        } else {
          binder.readBean(mailTemplate);
        }
      } else {
        JHapyMainView3.get()
            .displayErrorMessage(
                getTranslation("message.global.error", _mailTemplate.getMessage()));
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
    binder.readBean(mailTemplate);
  }

  @Override
  public void setParameter(BeforeEvent event, String viewParameters) {
    super.setParameter(event, viewParameters);
    if (StringUtils.isBlank((String) viewParameters) || "-1".equals(viewParameters)) {
      mailTemplate = new MailTemplate();
    } else {
      mailTemplate =
          NotificationServices.getMailTemplateService()
              .getById(new GetByStrIdQuery((String) viewParameters))
              .getData();
      if (mailTemplate == null) {
        mailTemplate = new MailTemplate();
      }
    }

    binder.setBean(mailTemplate);
  }

  private void goBack() {
    UI.getCurrent().navigate(MailTemplatesAdminView.class);
  }
}