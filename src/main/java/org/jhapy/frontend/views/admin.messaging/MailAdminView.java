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
import org.jhapy.dto.domain.notification.Mail;
import org.jhapy.dto.domain.notification.MailStatusEnum;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.config.AppProperties;
import org.jhapy.frontend.customFields.AttachmentField;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.MailDataProvider;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;

@I18NPageTitle(messageKey = AppConst.TITLE_MAILS_ADMIN)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class MailAdminView
    extends DefaultMasterDetailsView<Mail, DefaultFilter, SearchQuery, SearchQueryResult> {

  protected final AppProperties appProperties;

  public MailAdminView(MyI18NProvider myI18NProvider, AppProperties appProperties) {
    super("mail.", Mail.class, new MailDataProvider(), myI18NProvider);
    this.appProperties = appProperties;
  }

  protected Grid createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));

    grid.setDataProvider(dataProvider);
    grid.setHeight("100%");

    grid.addColumn(Mail::getCreated).setKey("created");
    // grid.addColumn(Mail::getMailAction).setKey("action");
    grid.addColumn(Mail::getTo).setKey("to");
    grid.addColumn(Mail::getMailStatus).setKey("mailStatus");

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

  protected Component createDetails(Mail mail) {
    boolean isNew = mail.getId() == null;
    detailsDrawerHeader.setTitle(
        isNew
            ? getTranslation("element.global.new") + " : "
            : getTranslation("element.global.update") + " : " + mail.getTo());

    detailsDrawerFooter.setDeleteButtonVisible(false);

    TextField fromField = new TextField();
    fromField.setWidth("100%");

    TextField toField = new TextField();
    toField.setWidth("100%");

    TextField copyToField = new TextField();
    copyToField.setWidth("100%");

    TextField subjectField = new TextField();
    subjectField.setWidth("100%");

    TextArea bodyField = new TextArea();
    bodyField.setWidth("100%");

    AttachmentField attachmentField = new AttachmentField();
    attachmentField.setWidth("100%");

    TextField mailActionField = new TextField();
    mailActionField.setWidth("100%");

    ComboBox<MailStatusEnum> mailStatusField = new ComboBox<>();
    mailStatusField.setItems(MailStatusEnum.values());

    TextArea errorMessageField = new TextArea();
    errorMessageField.setWidth("100%");

    NumberField nbRetryField = new NumberField();

    // Form layout
    FormLayout editingForm = new FormLayout();
    editingForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));
    FormLayout.FormItem fromFieldItem =
        editingForm.addFormItem(fromField, getTranslation("element." + I18N_PREFIX + "from"));
    FormLayout.FormItem toFieldItem =
        editingForm.addFormItem(toField, getTranslation("element." + I18N_PREFIX + "to"));
    FormLayout.FormItem copyToFieldItem =
        editingForm.addFormItem(copyToField, getTranslation("element." + I18N_PREFIX + "copyTo"));
    FormLayout.FormItem subjectFieldItem =
        editingForm.addFormItem(subjectField, getTranslation("element." + I18N_PREFIX + "subject"));
    FormLayout.FormItem bodyFieldItem =
        editingForm.addFormItem(bodyField, getTranslation("element." + I18N_PREFIX + "body"));
    FormLayout.FormItem attachmentFieldItem =
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

    binder.bind(fromField, Mail::getFrom, null);
    binder.bind(toField, Mail::getTo, null);
    binder.bind(copyToField, Mail::getCopyTo, null);
    binder.bind(subjectField, Mail::getSubject, null);
    binder.bind(bodyField, Mail::getBody, null);
    // binder.bind(attachmentField, Mail::getAttachements, Mail::setAttachements);
    // binder.bind(mailActionField, Mail::getMailAction, null);
    binder.bind(mailStatusField, Mail::getMailStatus, null);
    binder.bind(errorMessageField, Mail::getErrorMessage, null);
    binder.bind(nbRetryField, (e) -> e.getNbRetry().doubleValue(), null);

    return editingForm;
  }

  protected void filter(String filter) {
    dataProvider.setFilter(
        new DefaultFilter(StringUtils.isBlank(filter) ? null : "*" + filter + "*", Boolean.TRUE));
  }
}