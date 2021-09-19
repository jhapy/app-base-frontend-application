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

import com.vaadin.componentfactory.MultipleSelect;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.security.SecurityKeycloakGroup;
import org.jhapy.dto.domain.security.SecurityKeycloakRole;
import org.jhapy.dto.domain.security.SecurityKeycloakUser;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.security.SecurityServices;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.SecurityGroupKeycloakDataProvider;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 12/06/2020
 */
@I18NPageTitle(messageKey = AppConst.TITLE_SECURITY_GROUPS)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class SecurityKeycloakGroupsView
    extends DefaultMasterDetailsView<
        SecurityKeycloakGroup, DefaultFilter, SearchQuery, SearchQueryResult> {

  public SecurityKeycloakGroupsView(MyI18NProvider myI18NProvider) {
    super(
        "securityGroup.",
        SecurityKeycloakGroup.class,
        new SecurityGroupKeycloakDataProvider(),
        (e) -> SecurityServices.getKeycloakClient().saveGroup(new SaveQuery<>(e)),
        e -> SecurityServices.getKeycloakClient().deleteGroup(new DeleteByStrIdQuery(e.getId())),
        myI18NProvider);
  }

  @Override
  protected void initHeader() {
    super.initHeader();

    /*
    AppBar appBar = JHapyMainView3.get().getAppBar();

    Button clearCacheButton = new Button(getTranslation("action.sync.clearCache"));
    clearCacheButton.addClickListener(buttonClickEvent -> {
      SecurityServices.getKeycloakClient().cleanGroupCache();
      dataProvider.refreshAll();
    });

    appBar.addActionItem(clearCacheButton);

     */
  }

  protected Grid createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));

    grid.setDataProvider(dataProvider);
    grid.setHeight("100%");

    grid.addColumn(SecurityKeycloakGroup::getName).setKey("name");

    grid.addColumn(
            new TextRenderer<>(
                securityGroup ->
                    securityGroup.getRoles() == null
                        ? ""
                        : securityGroup.getRoles().stream()
                            .map(SecurityKeycloakRole::getName)
                            .reduce((a, b) -> a.concat(", ").concat(b))
                            .orElse("")))
        .setKey("roles");

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

  protected Component createDetails(SecurityKeycloakGroup securityKeycloakGroup) {
    boolean isNew = securityKeycloakGroup.getId() == null;
    detailsDrawerHeader.setTitle(
        isNew
            ? getTranslation("element.global.new") + " : "
            : getTranslation("element.global.update") + " : " + securityKeycloakGroup.getName());

    detailsDrawerFooter.setDeleteButtonVisible(!isNew);

    TextField name = new TextField();
    name.setWidth("100%");

    TextField description = new TextField();
    description.setWidth("100%");

    MultipleSelect<SecurityKeycloakRole> rolesField = new MultipleSelect<>();
    rolesField.setItemLabelGenerator(
        (ItemLabelGenerator<SecurityKeycloakRole>) SecurityKeycloakRole::getName);
    ServiceResult<List<SecurityKeycloakRole>> allRolesServiceResult =
        SecurityServices.getKeycloakClient().getRoles();
    if (allRolesServiceResult.getIsSuccess() && allRolesServiceResult.getData() != null) {
      rolesField.setItems(allRolesServiceResult.getData());
    }
    rolesField.setWidthFull();

    TextArea effectiveRolesField = new TextArea();
    effectiveRolesField.setWidthFull();

    TextArea membersField = new TextArea();
    membersField.setWidthFull();

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

    FormLayout.FormItem rolesItem =
        editingForm.addFormItem(rolesField, getTranslation("element." + I18N_PREFIX + "roles"));

    FormLayout.FormItem effectiveRolesItem =
        editingForm.addFormItem(
            effectiveRolesField, getTranslation("element." + I18N_PREFIX + "effectiveRoles"));

    FormLayout.FormItem membersItem =
        editingForm.addFormItem(membersField, getTranslation("element." + I18N_PREFIX + "members"));

    UIUtils.setColSpan(2, nameItem, rolesItem, effectiveRolesItem, membersItem);

    binder.setBean(securityKeycloakGroup);

    binder.bind(name, SecurityKeycloakGroup::getName, SecurityKeycloakGroup::setName);
    binder.bind(
        description, SecurityKeycloakGroup::getDescription, SecurityKeycloakGroup::setDescription);
    binder.bind(
        rolesField,
        securityKeycloakUser ->
            securityKeycloakUser.getRoles() != null
                ? new HashSet<>(securityKeycloakUser.getRoles())
                : null,
        (securityKeycloakUser, securityKeycloakRoles) ->
            securityKeycloakUser.setRoles(new ArrayList<>(securityKeycloakRoles)));
    binder.bind(
        effectiveRolesField,
        securityKeycloakUser ->
            securityKeycloakUser.getEffectiveRoles() != null
                ? securityKeycloakUser.getEffectiveRoles().stream()
                    .map(SecurityKeycloakRole::getName)
                    .reduce((a, b) -> a.concat(", ").concat(b))
                    .orElse("")
                : null,
        null);
    binder.bind(
        membersField,
        securityKeycloakUser ->
            securityKeycloakUser.getMembers() != null
                ? securityKeycloakUser.getMembers().stream()
                    .map(SecurityKeycloakUser::getUsername)
                    .reduce((a, b) -> a.concat(", ").concat(b))
                    .orElse("")
                : null,
        null);

    return editingForm;
  }

  protected void filter(String filter) {
    dataProvider.setFilter(
        new DefaultFilter(StringUtils.isBlank(filter) ? null : "*" + filter + "*", Boolean.TRUE));
  }
}