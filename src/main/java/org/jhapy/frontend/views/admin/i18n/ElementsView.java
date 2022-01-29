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
import org.jhapy.cqrs.command.i18n.CreateElementCommand;
import org.jhapy.cqrs.command.i18n.DeleteElementCommand;
import org.jhapy.cqrs.command.i18n.UpdateElementCommand;
import org.jhapy.cqrs.query.i18n.GetElementByIdQuery;
import org.jhapy.cqrs.query.i18n.GetElementByIdResponse;
import org.jhapy.cqrs.query.i18n.GetElementTrlsByElementIdQuery;
import org.jhapy.dto.domain.i18n.ElementDTO;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.ImportI18NFileQuery;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.i18n.I18NServices;
import org.jhapy.frontend.components.CheckboxColumnComponent;
import org.jhapy.frontend.components.ImportFileDialog;
import org.jhapy.frontend.customFields.ElementTrlListField;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.ElementDataProvider;
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
@I18NPageTitle(messageKey = AppConst.TITLE_ELEMENTS)
@RequiresRole({SecurityConst.ROLE_I18N_WRITE, SecurityConst.ROLE_ADMIN})
public class ElementsView
    extends DefaultMasterDetailsView<ElementDTO, DefaultFilter, SearchQuery, SearchQueryResult> {
  private final ElementDataProvider lazyDataProvider;
  private final QueryGateway queryGateway;

  public ElementsView(
      MyI18NProvider myI18NProvider, CommandGateway commandGateway, QueryGateway queryGateway) {
    super(
        "element.",
        ElementDTO.class,
        null,
        false,
        e -> {
          AbstractBaseCommand command;
          if (e.getId() == null) {
            command = new CreateElementCommand(e);
          } else {
            command = new UpdateElementCommand(e.getId(), e);
          }
          UUID uuid = commandGateway.sendAndWait(command);
          var response =
              queryGateway
                  .query(
                      new GetElementByIdQuery(e.getId() == null ? uuid : e.getId()),
                      GetElementByIdResponse.class)
                  .join();
          myI18NProvider.reloadElements();
          return new ServiceResult<>(response.getData());
        },
        e -> {
          commandGateway.sendAndWait(new DeleteElementCommand(e.getId()));
          myI18NProvider.reloadElements();
          return new ServiceResult<>();
        },
        myI18NProvider);
    lazyDataProvider = new ElementDataProvider(queryGateway);
    this.queryGateway = queryGateway;
  }

  @Override
  protected boolean beforeSave(ElementDTO entity) {
    var hasDefault = 0;
    var elementTrlList = entity.getTranslations();
    for (var elementTrl : elementTrlList) {
      if (elementTrl != null
          && elementTrl.isDefault() != null
          && Boolean.TRUE.equals(elementTrl.isDefault())) {
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
  protected void afterSave(ElementDTO entity) {
    lazyDataProvider.refreshItem(entity);
  }

  @Override
  protected void afterDelete() {
    lazyDataProvider.refreshAll();
  }

  protected Grid<ElementDTO> createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));
    grid.setItems(lazyDataProvider);
    grid.setHeight("100%");

    var categoryColumn =
        grid.addColumn(ElementDTO::getCategory).setKey("category").setSortable(true);
    var nameColumn = grid.addColumn(ElementDTO::getName).setKey("name").setSortable(true);
    grid.addComponentColumn(element -> new CheckboxColumnComponent(element.getTranslated()))
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

    var exportI18NButton = new Button(getTranslation("element.i18n.download"));
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

    var importI18NButton = new Button(getTranslation("element.i18n.upload"));
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

  protected Component createDetails(ElementDTO element) {
    String loggerPrefix = getLoggerPrefix("createDetails");
    var isNew = element.getId() == null;
    detailsDrawerHeader.setTitle(
        isNew
            ? getTranslation("element.global.new") + " : "
            : getTranslation("element.global.update") + " : " + element.getName());

    detailsDrawerFooter.setDeleteButtonVisible(!isNew);

    var name = new TextField();
    name.setWidth("100%");

    var categoryField = new TextField();
    categoryField.setWidth("100%");

    var active = new Checkbox();

    var translated = new Checkbox();

    var elementTrl = new ElementTrlListField();
    elementTrl.setReadOnly(false);
    elementTrl.setWidth("100%");

    // Form layout
    var editingForm = new FormLayout();
    editingForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

    var nameItem = editingForm.addFormItem(name, getTranslation("element." + I18N_PREFIX + "name"));
    var categoryItem =
        editingForm.addFormItem(
            categoryField, getTranslation("element." + I18N_PREFIX + "category"));
    var translationsItem =
        editingForm.addFormItem(
            elementTrl, getTranslation("element." + I18N_PREFIX + "translations"));
    editingForm.addFormItem(translated, getTranslation("element." + I18N_PREFIX + "translated"));
    editingForm.addFormItem(active, getTranslation("element." + I18N_PREFIX + "active"));

    UIUtils.setColSpan(2, nameItem, categoryItem, translationsItem);

    if (element.getTranslations().isEmpty() && element.getId() != null) {
      var response =
          queryGateway
              .query(
                  new GetElementTrlsByElementIdQuery(element.getId()),
                  ResponseTypes.instanceOf(GetElementTrlsByElementIdQuery.Response.class))
              .join();
      if (response != null) element.setTranslations(response.getData());
    }

    binder.setBean(element);

    binder.bind(name, ElementDTO::getName, ElementDTO::setName);
    binder.bind(categoryField, ElementDTO::getCategory, ElementDTO::setCategory);
    binder.bind(active, ElementDTO::isActive, ElementDTO::setActive);
    binder.bind(translated, ElementDTO::getTranslated, ElementDTO::setTranslated);
    binder.bind(elementTrl, ElementDTO::getTranslations, ElementDTO::setTranslations);

    return editingForm;
  }

  @Override
  protected void filter(String filter, Boolean showInactive) {
    lazyDataProvider.setFilter(
        new DefaultFilter(StringUtils.isBlank(filter) ? null : "%" + filter + "%", showInactive));
    lazyDataProvider.refreshAll();
  }
}
