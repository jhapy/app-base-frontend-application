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
import com.vaadin.flow.component.textfield.TextField;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.security.SecurityRole;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.security.SecurityServices;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.SecurityRoleDataProvider;
import org.jhapy.frontend.renderer.BooleanOkRenderer;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;

@I18NPageTitle(messageKey = AppConst.TITLE_SECURITY_ROLES)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class SecurityRolesView
    extends DefaultMasterDetailsView<SecurityRole, DefaultFilter, SearchQuery, SearchQueryResult> {

  public SecurityRolesView(MyI18NProvider myI18NProvider) {
    super(
        "securityRole.",
        SecurityRole.class,
        new SecurityRoleDataProvider(),
        (e) -> SecurityServices.getSecurityRoleService().save(new SaveQuery<>(e)),
        e -> SecurityServices.getSecurityRoleService().delete(new DeleteByIdQuery(e.getId())),
        myI18NProvider);
  }

  protected Grid createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));

    grid.setDataProvider(dataProvider);
    grid.setHeight("100%");

    grid.addColumn(SecurityRole::getName).setKey("name");
    grid.addColumn(new BooleanOkRenderer<>(SecurityRole::getCanLogin)).setKey("canLogin");

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

  protected Component createDetails(SecurityRole securityRole) {
    boolean isNew = securityRole.getId() == null;
    detailsDrawerHeader.setTitle(
        isNew
            ? getTranslation("element.global.new") + " : "
            : getTranslation("element.global.update") + " : " + securityRole.getName());

    detailsDrawerFooter.setDeleteButtonVisible(!isNew);

    TextField name = new TextField();
    name.setWidth("100%");

    TextField description = new TextField();
    description.setWidth("100%");

    Checkbox canLogin = new Checkbox();

    Checkbox isActive = new Checkbox();

    // Form layout
    FormLayout editingForm = new FormLayout();
    editingForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));
    FormLayout.FormItem nameItem =
        editingForm.addFormItem(name, getTranslation("element." + I18N_PREFIX + "name"));
    editingForm.addFormItem(description, getTranslation("element." + I18N_PREFIX + "description"));
    editingForm.addFormItem(canLogin, getTranslation("element." + I18N_PREFIX + "canLogin"));
    FormLayout.FormItem isActiveItem =
        editingForm.addFormItem(isActive, getTranslation("element." + I18N_PREFIX + "isActive"));

    UIUtils.setColSpan(2, nameItem);

    binder.setBean(securityRole);

    binder.bind(name, SecurityRole::getName, SecurityRole::setName);
    binder.bind(description, SecurityRole::getDescription, SecurityRole::setDescription);
    binder.bind(canLogin, SecurityRole::getCanLogin, SecurityRole::setCanLogin);
    binder.bind(isActive, SecurityRole::isActive, SecurityRole::setActive);

    return editingForm;
  }

  protected void filter(String filter) {
    dataProvider.setFilter(
        new DefaultFilter(StringUtils.isBlank(filter) ? null : "*" + filter + "*", Boolean.TRUE));
  }
}
