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
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
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
import org.jhapy.frontend.dataproviders.SecurityUserKeycloakDataProvider;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.*;

@I18NPageTitle(messageKey = AppConst.TITLE_SECURITY_USERS)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class SecurityKeycloakUsersView
    extends DefaultMasterDetailsView<
        SecurityKeycloakUser, DefaultFilter, SearchQuery, SearchQueryResult> {

  public SecurityKeycloakUsersView(MyI18NProvider myI18NProvider) {
    super(
        "securityUser.",
        SecurityKeycloakUser.class,
        new SecurityUserKeycloakDataProvider(),
        false,
        (e) -> SecurityServices.getKeycloakClient().saveUser(new SaveQuery<>(e)),
        e -> SecurityServices.getKeycloakClient().deleteUser(new DeleteByStrIdQuery(e.getId())),
        myI18NProvider);
  }

  @Override
  protected void initHeader() {
    super.initHeader();
    /*
       AppBar appBar = JHapyMainView3.get().getAppBar();

       Button clearCacheButton = new Button(getTranslation("action.sync.clearCache"));
       clearCacheButton.addClickListener(buttonClickEvent -> {
         SecurityServices.getKeycloakClient().cleanUserCache();
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

    grid.addColumn(SecurityKeycloakUser::getUsername).setKey("username");

    grid.addColumn(
            new TextRenderer<>(
                securityUser ->
                    securityUser.getRoles() == null
                        ? ""
                        : securityUser.getRoles().stream()
                            .map(SecurityKeycloakRole::getName)
                            .reduce((a, b) -> a.concat(", ").concat(b))
                            .orElse("")))
        .setKey("roles");

    grid.addColumn(
            new TextRenderer<>(
                securityUser ->
                    securityUser.getGroups() == null
                        ? ""
                        : securityUser.getGroups().stream()
                            .map(SecurityKeycloakGroup::getName)
                            .reduce((a, b) -> a.concat(", ").concat(b))
                            .orElse("")))
        .setKey("groups");

    grid.getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                column.setResizable(true);
                column.setSortable(true);
              }
            });
    return grid;
  }

  protected Component createDetails(SecurityKeycloakUser securityUser) {
    boolean isNew = securityUser.getId() == null;
    detailsDrawerHeader.setTitle(
        isNew
            ? getTranslation("element.global.new") + " : "
            : getTranslation("element.global.update") + " : " + securityUser.getUsername());

    detailsDrawerFooter.setDeleteButtonVisible(!isNew);

    TextField usernameField = new TextField();
    usernameField.setWidthFull();

    PasswordField passwordField = new PasswordField();
    passwordField.setWidthFull();

    TextField firstNameField = new TextField();
    firstNameField.setWidthFull();

    TextField lastNameField = new TextField();
    lastNameField.setWidthFull();

    EmailField emailField = new EmailField();
    emailField.setWidthFull();

    TextField titleField = new TextField();
    titleField.setWidthFull();

    TextField mobileNumberField = new TextField();
    mobileNumberField.setWidthFull();

    Checkbox isEmailVerifiedField = new Checkbox();

    Checkbox isActivatedField = new Checkbox();

    Checkbox isLocalField = new Checkbox();

    List<Locale> locales = Arrays.asList(Locale.getAvailableLocales());
    locales.sort(Comparator.comparing(Locale::getDisplayName));

    ComboBox<Locale> defaultLocaleField = new ComboBox<>();
    defaultLocaleField.setItems(locales);
    defaultLocaleField.setItemLabelGenerator((ItemLabelGenerator<Locale>) Locale::getDisplayName);
    defaultLocaleField.setWidthFull();

    MultiselectComboBox<SecurityKeycloakRole> rolesField = new MultiselectComboBox<>();
    rolesField.setItemLabelGenerator(
        (ItemLabelGenerator<SecurityKeycloakRole>) SecurityKeycloakRole::getName);
    ServiceResult<List<SecurityKeycloakRole>> allRolesServiceResult =
        SecurityServices.getKeycloakClient().getRoles();
    if (allRolesServiceResult.getIsSuccess() && allRolesServiceResult.getData() != null) {
      rolesField.setItems(allRolesServiceResult.getData());
    }
    rolesField.setWidthFull();

    MultiselectComboBox<SecurityKeycloakGroup> groupsField = new MultiselectComboBox<>();
    groupsField.setItemLabelGenerator(
        (ItemLabelGenerator<SecurityKeycloakGroup>) SecurityKeycloakGroup::getName);
    ServiceResult<List<SecurityKeycloakGroup>> allGroupsServiceResult =
        SecurityServices.getKeycloakClient().getGroups();
    if (allGroupsServiceResult.getIsSuccess() && allGroupsServiceResult.getData() != null) {
      groupsField.setItems(allGroupsServiceResult.getData());
    }
    groupsField.setWidthFull();

    TextArea effectiveRolesField = new TextArea();
    effectiveRolesField.setWidthFull();

    Button impressionateButton =
        UIUtils.createButton(getTranslation("action." + I18N_PREFIX + "impressionate"));
    impressionateButton.addClickListener(
        buttonClickEvent ->
            SecurityServices.getKeycloakClient().impressionate(securityUser.getId()));

    // Form layout
    FormLayout editingForm = new FormLayout();
    editingForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));
    editingForm.addFormItem(usernameField, getTranslation("element." + I18N_PREFIX + "username"));
    editingForm.addFormItem(passwordField, getTranslation("element." + I18N_PREFIX + "password"));

    editingForm.addFormItem(firstNameField, getTranslation("element." + I18N_PREFIX + "firstName"));
    editingForm.addFormItem(lastNameField, getTranslation("element." + I18N_PREFIX + "lastName"));

    editingForm.addFormItem(titleField, getTranslation("element." + I18N_PREFIX + "title"));
    editingForm.addFormItem(emailField, getTranslation("element." + I18N_PREFIX + "email"));
    editingForm.addFormItem(
        mobileNumberField, getTranslation("element." + I18N_PREFIX + "mobileNumber"));

    editingForm.addFormItem(isLocalField, getTranslation("element." + I18N_PREFIX + "isLocal"));
    editingForm.addFormItem(
        isEmailVerifiedField, getTranslation("element." + I18N_PREFIX + "isEmailVerified"));
    editingForm.addFormItem(
        isActivatedField, getTranslation("element." + I18N_PREFIX + "isActivated"));

    editingForm.addFormItem(
        defaultLocaleField, getTranslation("element." + I18N_PREFIX + "defaultLanguage"));

    FormLayout.FormItem groupsItem =
        editingForm.addFormItem(groupsField, getTranslation("element." + I18N_PREFIX + "groups"));

    FormLayout.FormItem rolesItem =
        editingForm.addFormItem(rolesField, getTranslation("element." + I18N_PREFIX + "roles"));

    FormLayout.FormItem effectiveRolesItem =
        editingForm.addFormItem(
            effectiveRolesField, getTranslation("element." + I18N_PREFIX + "effectiveRoles"));

    // editingForm.add(impressionateButton );

    UIUtils.setColSpan(2, rolesItem, groupsItem, effectiveRolesItem);

    binder.setBean(securityUser);

    binder
        .forField(usernameField)
        .asRequired(getTranslation("message.securityUser.usernameRequired"))
        .bind(SecurityKeycloakUser::getUsername, SecurityKeycloakUser::setUsername);
    binder
        .forField(passwordField)
        .bind(SecurityKeycloakUser::getPassword, SecurityKeycloakUser::setPassword);
    binder.bind(
        firstNameField, SecurityKeycloakUser::getFirstName, SecurityKeycloakUser::setFirstName);
    binder.bind(
        lastNameField, SecurityKeycloakUser::getLastName, SecurityKeycloakUser::setLastName);
    binder.bind(emailField, SecurityKeycloakUser::getEmail, SecurityKeycloakUser::setEmail);
    binder.bind(titleField, SecurityKeycloakUser::getTitle, SecurityKeycloakUser::setTitle);
    binder.bind(
        mobileNumberField,
        SecurityKeycloakUser::getMobileNumber,
        SecurityKeycloakUser::setMobileNumber);
    binder.bind(
        isEmailVerifiedField,
        SecurityKeycloakUser::getEmailVerified,
        SecurityKeycloakUser::setEmailVerified);
    binder.bind(isLocalField, SecurityKeycloakUser::getIsLocal, SecurityKeycloakUser::setIsLocal);
    binder.bind(
        isActivatedField,
        SecurityKeycloakUser::getIsActivated,
        SecurityKeycloakUser::setIsActivated);
    binder.bind(
        defaultLocaleField,
        SecurityKeycloakUser::getDefaultLocale,
        SecurityKeycloakUser::setDefaultLocale);
    binder.bind(
        rolesField,
        securityKeycloakUser ->
            securityKeycloakUser.getRoles() == null
                ? null
                : new HashSet<>(securityKeycloakUser.getRoles()),
        (securityKeycloakUser, securityKeycloakRoles) ->
            securityKeycloakUser.setRoles(new ArrayList<>(securityKeycloakRoles)));
    binder.bind(
        groupsField,
        securityKeycloakUser ->
            securityKeycloakUser.getGroups() == null
                ? null
                : new HashSet<>(securityKeycloakUser.getGroups()),
        (securityKeycloakUser, securityKeycloakGroups) ->
            securityKeycloakUser.setGroups(new ArrayList<>(securityKeycloakGroups)));
    binder.bind(
        effectiveRolesField,
        securityKeycloakUser ->
            securityKeycloakUser.getEffectiveRoles() == null
                ? null
                : securityKeycloakUser.getEffectiveRoles().stream()
                    .map(SecurityKeycloakRole::getName)
                    .reduce((a, b) -> a.concat(", ").concat(b))
                    .orElse(""),
        null);

    return editingForm;
  }

  protected void filter(String filter) {
    dataProvider.setFilter(
        new DefaultFilter(StringUtils.isBlank(filter) ? null : filter, Boolean.TRUE));
  }
}