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

package org.jhapy.frontend.views.admin.audit;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.textfield.TextField;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.audit.Session;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.SessionDataProvider;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.i18n.DateTimeFormatter;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;

@I18NPageTitle(messageKey = AppConst.TITLE_SESSIONS_ADMIN)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class SessionView
    extends DefaultMasterDetailsView<Session, DefaultFilter, SearchQuery, SearchQueryResult> {

  public SessionView(MyI18NProvider myI18NProvider) {
    super("session.", Session.class, new SessionDataProvider(), myI18NProvider);
  }

  protected Grid createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));

    grid.setDataProvider(dataProvider);
    grid.setHeight("100%");

    grid.addColumn(Session::getUsername).setKey("username");
    grid.addColumn(Session::getSourceIp).setKey("sourceIp");
    grid.addColumn(session -> DateTimeFormatter.format(session.getSessionStart(), getLocale()))
        .setKey("sessionStart");
    grid.addColumn(session -> DateTimeFormatter.format(session.getSessionEnd(), getLocale()))
        .setKey("sessionEnd");

    grid.getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                column.setResizable(true);
                column.setSortable(true);
                column.setAutoWidth(true);
              }
            });
    grid.addColumn(Session::getJsessionId).setKey("jsessionId");
    return grid;
  }

  protected Component createDetails(Session session) {
    boolean isNew = session.getId() == null;
    detailsDrawerHeader.setTitle(
        isNew
            ? getTranslation("element.global.new") + " : "
            : getTranslation("element.global.update") + " : " + session.getUsername());

    detailsDrawerFooter.setDeleteButtonVisible(false);

    TextField usernameField = new TextField();
    usernameField.setWidth("100%");

    TextField sourceIpField = new TextField();
    sourceIpField.setWidth("100%");

    TextField sessionStartField = new TextField();
    sessionStartField.setWidth("100%");

    TextField sessionEndField = new TextField();
    sessionEndField.setWidth("100%");

    TextField sessionDurationField = new TextField();
    sessionDurationField.setWidth("100%");

    Checkbox isSuccessField = new Checkbox();

    TextField errorField = new TextField();
    errorField.setWidth("100%");

    TextField jSessionIdField = new TextField();
    jSessionIdField.setWidth("100%");

    // Form layout
    FormLayout editingForm = new FormLayout();
    editingForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

    editingForm.addFormItem(usernameField, getTranslation("element." + I18N_PREFIX + "username"));
    editingForm.addFormItem(sourceIpField, getTranslation("element." + I18N_PREFIX + "sourceIp"));
    editingForm.addFormItem(
        sessionStartField, getTranslation("element." + I18N_PREFIX + "sessionStart"));
    editingForm.addFormItem(
        sessionEndField, getTranslation("element." + I18N_PREFIX + "sessionEnd"));
    editingForm.addFormItem(
        sessionDurationField, getTranslation("element." + I18N_PREFIX + "sessionDuration"));
    editingForm.addFormItem(isSuccessField, getTranslation("element." + I18N_PREFIX + "isSuccess"));
    editingForm.addFormItem(errorField, getTranslation("element." + I18N_PREFIX + "error"));
    editingForm.addFormItem(
        jSessionIdField, getTranslation("element." + I18N_PREFIX + "jsessionId"));

    binder.setBean(session);

    binder.bind(usernameField, Session::getUsername, null);
    binder.bind(sourceIpField, Session::getSourceIp, null);
    binder.bind(
        sessionStartField,
        entity1 ->
            entity1.getSessionStart() == null
                ? ""
                : DateTimeFormatter.format(entity1.getSessionStart(), getLocale()),
        (a, b) -> {});
    binder.bind(
        sessionEndField,
        entity1 ->
            entity1.getSessionEnd() == null
                ? ""
                : DateTimeFormatter.format(entity1.getSessionEnd(), getLocale()),
        (a, b) -> {});
    binder.bind(
        sessionDurationField,
        (e) -> e.getSessionDuration() == null ? null : e.getSessionDuration().toString(),
        null);
    binder.bind(isSuccessField, Session::getIsSuccess, null);
    binder.bind(errorField, Session::getError, null);
    binder.bind(jSessionIdField, Session::getJsessionId, null);

    return editingForm;
  }

  protected void filter(String filter) {
    dataProvider.setFilter(
        new DefaultFilter(StringUtils.isBlank(filter) ? null : ".*" + filter + ".*", Boolean.TRUE));
  }
}