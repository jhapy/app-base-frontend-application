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

package org.jhapy.frontend.views.admin.security;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.AccessLevelEnum;
import org.jhapy.dto.domain.DbTableDTO;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.security.SecurityServices;
import org.jhapy.frontend.dataproviders.DbTableDataProvider;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;

@I18NPageTitle(messageKey = AppConst.TITLE_DB_TABLES)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class DbTablesView
    extends DefaultMasterDetailsView<DbTableDTO, DefaultFilter, SearchQuery, SearchQueryResult> {

  public DbTablesView(MyI18NProvider myI18NProvider) {
    super(
        "table.",
        DbTableDTO.class,
        new DbTableDataProvider(),
        (e) -> SecurityServices.getTableService().save(new SaveQuery<>(e)),
        e -> SecurityServices.getTableService().delete(new DeleteByIdQuery(e.getId())),
        myI18NProvider);
  }

  protected Grid createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));

    grid.setDataProvider(dataProvider);
    grid.setHeight("100%");

    grid.addColumn(DbTableDTO::getName).setKey("name");

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

  protected Component createDetails(DbTableDTO entity) {
    boolean isNew = entity.getId() == null;
    detailsDrawerHeader.setTitle(
        isNew
            ? getTranslation("element.global.new") + " : "
            : getTranslation("element.global.update") + " : " + entity.getName());

    detailsDrawerFooter.setDeleteButtonVisible(!isNew);

    var nameField = new TextField();
    nameField.setWidthFull();

    var descriptionField = new TextField();
    descriptionField.setWidthFull();

    var tableNameField = new TextField();
    tableNameField.setWidthFull();

    var isDeletableField = new Checkbox();

    var isChangeLogField = new Checkbox();

    var isSecurityEnabledField = new Checkbox();

    var accessLevelField = new Select<AccessLevelEnum>();
    accessLevelField.setItems(AccessLevelEnum.values());
    accessLevelField.setItemLabelGenerator(
        item -> getTranslation("element." + I18N_PREFIX + "accessLevel." + item.name()));

    Checkbox isActiveField = new Checkbox();

    // Form layout
    var editingForm = new FormLayout();
    editingForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));
    var nameItem =
        editingForm.addFormItem(nameField, getTranslation("element." + I18N_PREFIX + "name"));
    editingForm.addFormItem(
        descriptionField, getTranslation("element." + I18N_PREFIX + "description"));
    editingForm.addFormItem(tableNameField, getTranslation("element." + I18N_PREFIX + "tableName"));
    editingForm.addFormItem(
        isDeletableField, getTranslation("element." + I18N_PREFIX + "isDeletable"));
    editingForm.addFormItem(
        isChangeLogField, getTranslation("element." + I18N_PREFIX + "isChangeLog"));
    editingForm.addFormItem(
        isSecurityEnabledField, getTranslation("element." + I18N_PREFIX + "isSecurityEnabled"));
    editingForm.addFormItem(
        accessLevelField, getTranslation("element." + I18N_PREFIX + "accessLevel"));

    var isActiveItem =
        editingForm.addFormItem(
            isActiveField, getTranslation("element." + I18N_PREFIX + "isActive"));

    UIUtils.setColSpan(2, nameItem);

    binder.setBean(entity);

    binder.bind(nameField, DbTableDTO::getName, DbTableDTO::setName);
    binder.bind(descriptionField, DbTableDTO::getDescription, DbTableDTO::setDescription);
    binder.bind(tableNameField, DbTableDTO::getDescription, DbTableDTO::setDescription);
    binder.bind(isDeletableField, DbTableDTO::getIsDeletable, DbTableDTO::setIsDeletable);
    binder.bind(isChangeLogField, DbTableDTO::getIsChangeLog, DbTableDTO::setIsChangeLog);
    binder.bind(
        isSecurityEnabledField, DbTableDTO::getIsSecurityEnabled, DbTableDTO::setIsSecurityEnabled);
    binder.bind(accessLevelField, DbTableDTO::getAccessLevel, DbTableDTO::setAccessLevel);

    binder.bind(isActiveField, DbTableDTO::isActive, DbTableDTO::setActive);

    return editingForm;
  }

  protected void filter(String filter) {
    dataProvider.setFilter(
        new DefaultFilter(StringUtils.isBlank(filter) ? null : "*" + filter + "*", Boolean.TRUE));
  }
}
