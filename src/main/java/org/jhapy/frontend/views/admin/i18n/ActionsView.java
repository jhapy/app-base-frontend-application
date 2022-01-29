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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.jhapy.cqrs.command.AbstractBaseCommand;
import org.jhapy.cqrs.command.i18n.CreateActionCommand;
import org.jhapy.cqrs.command.i18n.DeleteActionCommand;
import org.jhapy.cqrs.command.i18n.UpdateActionCommand;
import org.jhapy.cqrs.query.i18n.GetActionByIdQuery;
import org.jhapy.cqrs.query.i18n.GetActionByIdResponse;
import org.jhapy.cqrs.query.i18n.GetActionTrlsByActionIdQuery;
import org.jhapy.dto.domain.i18n.ActionDTO;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.ImportI18NFileQuery;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.i18n.I18NServices;
import org.jhapy.frontend.components.CheckboxColumnComponent;
import org.jhapy.frontend.components.ImportFileDialog;
import org.jhapy.frontend.customFields.ActionTrlListField;
import org.jhapy.frontend.dataproviders.ActionDataProvider;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;
import org.jhapy.frontend.views.JHapyMainView3;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
@I18NPageTitle(messageKey = AppConst.PAGE_ACTIONS)
@RequiresRole({SecurityConst.ROLE_I18N_WRITE, SecurityConst.ROLE_ADMIN})
public class ActionsView
    extends DefaultMasterDetailsView<ActionDTO, DefaultFilter, SearchQuery, SearchQueryResult> {

  public ActionsView(
      MyI18NProvider myI18NProvider, CommandGateway commandGateway, QueryGateway queryGateway) {
    super(
        "action.",
        ActionDTO.class,
        null,
        false,
        e -> {
          AbstractBaseCommand command;
          if (e.getId() == null) {
            command = new CreateActionCommand(e);
          } else {
            command = new UpdateActionCommand(e.getId(), e);
          }
          UUID uuid = commandGateway.sendAndWait(command);
          var response =
              queryGateway
                  .query(
                      new GetActionByIdQuery(e.getId() == null ? uuid : e.getId()),
                      GetActionByIdResponse.class)
                  .join();
          myI18NProvider.reloadActions();
          return new ServiceResult<>(response.getData());
        },
        e -> {
          commandGateway.sendAndWait(new DeleteActionCommand(e.getId()));
          myI18NProvider.reloadActions();
          return new ServiceResult<>();
        },
        myI18NProvider);
    lazyDataProvider = new ActionDataProvider(queryGateway);
    this.queryGateway = queryGateway;
  }

  @Override
  protected boolean beforeSave(ActionDTO entity) {
    var hasDefault = 0;
    var actionTrlList = entity.getTranslations();
    for (var actionTrl : actionTrlList) {
      if (actionTrl != null
          && actionTrl.isDefault() != null
          && Boolean.TRUE.equals(actionTrl.isDefault())) {
        hasDefault++;
      }
    }
    if (hasDefault == 0) {
      JHapyMainView3.get()
          .displayErrorMessage(getTranslation("message.global.translationNeedsDefault"));
    } else if (hasDefault > 1) {
      JHapyMainView3.get()
          .displayErrorMessage(getTranslation("message.global.translationMaxDefault"));
    }
    return hasDefault == 1;
  }

  @Override
  protected void afterSave(ActionDTO entity) {
    lazyDataProvider.refreshItem(entity);
  }

  @Override
  protected void afterDelete() {
    lazyDataProvider.refreshAll();
  }

  protected Grid<ActionDTO> createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));
    grid.setItems(lazyDataProvider);
    grid.setHeightFull();

    var categoryColumn =
        grid.addColumn(ActionDTO::getCategory).setKey("category").setSortable(true);
    var nameColumn = grid.addColumn(ActionDTO::getName).setKey("name").setSortable(true);
    grid.addComponentColumn(action -> new CheckboxColumnComponent(action.getTranslated()))
        .setKey("translated")
        .setSortable(true);

    grid.getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                column.setResizable(true);
              }
            });

    var headerRow = grid.prependHeaderRow();

    var buttonsCell = headerRow.join(categoryColumn, nameColumn);

    var exportI18NButton = new Button(getTranslation("action.i18n.download"));
    exportI18NButton.addClickListener(
        buttonClickEvent -> {
          var result = I18NServices.getI18NService().getI18NFile(new BaseRemoteQuery());
          final var resource =
              new StreamResource(
                  "i18n.xlsx",
                  () -> new ByteArrayInputStream(ArrayUtils.toPrimitive(result.getData())));
          final var registration =
              VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
          UI.getCurrent().getPage().setLocation(registration.getResourceUri());
        });

    var importI18NButton = new Button(getTranslation("action.i18n.upload"));
    importI18NButton.addClickListener(
        buttonClickEvent -> {
          var importFileDialog = new ImportFileDialog<byte[]>();
          importFileDialog.open(
              getTranslation("element.i18n.upload"),
              getTranslation("message.i18n.upload"),
              null,
              getTranslation("action.i18n.upload"),
              bytes -> {
                importFileDialog.close();
                var result =
                    I18NServices.getI18NService()
                        .importI18NFile(
                            new ImportI18NFileQuery("i18n.xlsx", ArrayUtils.toObject(bytes)));
                if (result.getIsSuccess()) {
                  JHapyMainView3.get()
                      .displayInfoMessage(getTranslation("message.fileImport.success"));
                } else {
                  JHapyMainView3.get()
                      .displayInfoMessage(
                          getTranslation("message.fileImport.error", result.getMessage()));
                }
              },
              importFileDialog::close);
        });

    var headerHLayout = new HorizontalLayout(exportI18NButton, importI18NButton);
    buttonsCell.setComponent(headerHLayout);

    return grid;
  }

  protected Component createDetails(ActionDTO action) {
    var isNew = action.getId() == null;
    detailsDrawerHeader.setTitle(
        isNew
            ? getTranslation("element.global.new") + " : "
            : getTranslation("element.global.update") + " : " + action.getName());

    detailsDrawerFooter.setDeleteButtonVisible(!isNew);

    var nameField = new TextField();
    nameField.setWidthFull();

    var categoryField = new TextField();
    categoryField.setWidthFull();

    var isActiveField = new Checkbox();

    var translatedField = new Checkbox();

    var actionTrlField = new ActionTrlListField();
    actionTrlField.setReadOnly(false);
    actionTrlField.setWidthFull();

    // Form layout
    var editingForm = new FormLayout();
    editingForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

    var nameItem =
        editingForm.addFormItem(nameField, getTranslation("element." + I18N_PREFIX + "name"));
    var categoryItem =
        editingForm.addFormItem(
            categoryField, getTranslation("element." + I18N_PREFIX + "category"));
    var translationsItem =
        editingForm.addFormItem(
            actionTrlField, getTranslation("element." + I18N_PREFIX + "translations"));
    editingForm.addFormItem(
        translatedField, getTranslation("element." + I18N_PREFIX + "translated"));
    editingForm.addFormItem(isActiveField, getTranslation("element." + I18N_PREFIX + "isActive"));

    UIUtils.setColSpan(2, nameItem, categoryItem, translationsItem);

    if (action.getTranslations().isEmpty() && action.getId() != null) {
      var response =
          queryGateway
              .query(
                  new GetActionTrlsByActionIdQuery(action.getId()),
                  ResponseTypes.instanceOf(GetActionTrlsByActionIdQuery.Response.class))
              .join();
      if (response != null) action.setTranslations(response.getData());
    }

    binder.setBean(action);

    binder.bind(nameField, ActionDTO::getName, ActionDTO::setName);
    binder.bind(categoryField, ActionDTO::getCategory, ActionDTO::setCategory);
    binder.bind(isActiveField, ActionDTO::isActive, ActionDTO::setActive);
    binder.bind(translatedField, ActionDTO::getTranslated, ActionDTO::setTranslated);
    binder.bind(actionTrlField, ActionDTO::getTranslations, ActionDTO::setTranslations);

    return editingForm;
  }

  @Override
  protected void filter(String filter, Boolean showInactive) {
    lazyDataProvider.setFilter(
        new DefaultFilter(StringUtils.isBlank(filter) ? null : "%" + filter + "%", showInactive));
    lazyDataProvider.refreshAll();
  }
}
