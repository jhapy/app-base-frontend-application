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

package org.jhapy.frontend.views.admin.i18n;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.i18n.Message;
import org.jhapy.dto.domain.i18n.MessageTrl;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.i18n.messageTrl.FindByMessageQuery;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.i18n.I18NServices;
import org.jhapy.frontend.components.CheckboxColumnComponent;
import org.jhapy.frontend.customFields.MessageTrlListField;
import org.jhapy.frontend.dataproviders.DefaultDataProvider.DefaultFilter;
import org.jhapy.frontend.dataproviders.MessageDataProvider;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;
import org.jhapy.frontend.views.JHapyMainView;
import org.springframework.security.access.annotation.Secured;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */

@I18NPageTitle(messageKey = AppConst.TITLE_MESSAGES)
@Secured({SecurityConst.ROLE_I18N_WRITE, SecurityConst.ROLE_ADMIN})
public class MessagesView extends DefaultMasterDetailsView<Message, DefaultFilter> {

  public MessagesView(MyI18NProvider myI18NProvider) {
    super("message.", Message.class, new MessageDataProvider(),
        (e) -> {
          ServiceResult<Message> _elt = I18NServices.getMessageService().save(new SaveQuery<>(e));
          if (_elt.getIsSuccess()) {
            myI18NProvider.reloadMessages();
          }
          return _elt;
        },
        e -> I18NServices.getMessageService().delete(new DeleteByIdQuery(e.getId())));
  }

  @Override
  protected boolean beforeSave(Message entity) {
    long hasDefault = 0;
    List<MessageTrl> messageTrls = entity.getTranslations();
    for (MessageTrl messageTrl : messageTrls) {
      if (messageTrl != null && messageTrl.getIsDefault() != null && messageTrl.getIsDefault()) {
        hasDefault++;
      }
    }
    if (hasDefault == 0) {
      JHapyMainView.get().displayErrorMessage(getTranslation(
          "message.global.translationNeedsDefault"));
    } else if (hasDefault > 1) {
      JHapyMainView.get().displayErrorMessage(getTranslation(
          "message.global.translationMaxDefault"));
    }
    return hasDefault == 1;
  }

  protected Grid createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem()
        .ifPresent(this::showDetails));

    grid.setDataProvider(dataProvider);
    grid.setHeight("100%");

    grid.addColumn(Message::getCategory).setKey("category").setSortable(true);
    grid.addColumn(Message::getName).setKey("name").setSortable(true);
    grid.addComponentColumn(message -> new CheckboxColumnComponent(message.getIsTranslated()))
        .setKey("isTranslated").setSortable(true);

    grid.getColumns().forEach(column -> {
      if (column.getKey() != null) {
        column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
        column.setResizable(true);
      }
    });
    return grid;
  }

  protected Component createDetails(Message message) {
    boolean isNew = message.getId() == null;
    detailsDrawerHeader.setTitle(isNew ? getTranslation("element.global.new") + " : "
        : getTranslation("element.global.update") + " : " + message.getName());

    detailsDrawerFooter.setDeleteButtonVisible(!isNew);

    TextField name = new TextField();
    name.setWidth("100%");

    TextField categoryField = new TextField();
    categoryField.setWidth("100%");

    Checkbox isActive = new Checkbox();

    Checkbox isTranslated = new Checkbox();

    MessageTrlListField messageTrl = new MessageTrlListField();
    messageTrl.setReadOnly(false);
    messageTrl.setWidth("100%");

    // Form layout
    FormLayout editingForm = new FormLayout();
    editingForm.addClassNames(LumoStyles.Padding.Bottom.L,
        LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1,
            FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("21em", 2,
            FormLayout.ResponsiveStep.LabelsPosition.TOP));
    FormLayout.FormItem nameItem = editingForm
        .addFormItem(name, getTranslation("element." + I18N_PREFIX + "name"));
    FormLayout.FormItem categoryItem = editingForm
        .addFormItem(categoryField, getTranslation("element." + I18N_PREFIX + "category"));
    FormLayout.FormItem translationsItem = editingForm
        .addFormItem(messageTrl, getTranslation("element." + I18N_PREFIX + "translations"));
    editingForm
        .addFormItem(isTranslated, getTranslation("element." + I18N_PREFIX + "isTranslated"));
    editingForm
        .addFormItem(isActive, getTranslation("element." + I18N_PREFIX + "isActive"));

    UIUtils.setColSpan(2, nameItem, categoryItem, translationsItem);

    if (message.getTranslations().size() == 0) {
      message.setTranslations(
          I18NServices.getMessageTrlService().findByMessage(new FindByMessageQuery(message.getId()))
              .getData());
    }

    binder.setBean(message);

    binder.bind(name, Message::getName, Message::setName);
    binder.bind(categoryField, Message::getCategory, Message::setCategory);
    binder.bind(isActive, Message::getIsActive, Message::setIsActive);
    binder.bind(isTranslated, Message::getIsTranslated, Message::setIsTranslated);
    binder.bind(messageTrl, Message::getTranslations, Message::setTranslations);

    return editingForm;
  }

  protected void filter(String filter) {
    dataProvider
        .setFilter(new DefaultFilter(
            StringUtils.isBlank(filter) ? null : "%" + filter + "%",
            Boolean.TRUE));
  }
}
