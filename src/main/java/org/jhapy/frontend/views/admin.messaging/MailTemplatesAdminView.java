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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouterLayout;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.notification.MailTemplate;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.bar.AppBar;
import org.jhapy.frontend.components.navigation.bar.AppBar.NaviMode;
import org.jhapy.frontend.dataproviders.DefaultDataProvider;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.MailTemplateDataProvider;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BoxSizing;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.views.JHapyMainView3;
import org.springframework.security.access.annotation.Secured;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-14
 */
@I18NPageTitle(messageKey = AppConst.TITLE_MAIL_TEMPLATES_ADMIN)
@Secured(SecurityConst.ROLE_ADMIN)
public class MailTemplatesAdminView extends ViewFrame implements RouterLayout, HasLogger {

  private final static String I18N_PREFIX = "mailTemplate.";

  private DefaultDataProvider<MailTemplate, DefaultFilter> dataProvider;

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    initHeader();

    setViewContent(createContent());

    filter(null);
  }

  private void initHeader() {
    AppBar appBar = JHapyMainView3.get().getAppBar();
    appBar.setNaviMode(NaviMode.MENU);
    appBar.disableGlobalSearch();
    Button searchButton = UIUtils.createTertiaryButton(VaadinIcon.SEARCH);
    searchButton.addClickListener(event -> appBar.searchModeOn());
    appBar.addSearchListener(event -> filter((String) event.getValue()));
    appBar.setSearchPlaceholder(getTranslation("element.global.search"));
    appBar.addActionItem(searchButton);

    Button newPlaceButton = UIUtils.createTertiaryButton(VaadinIcon.PLUS);
    newPlaceButton.addClickListener(event ->
        viewDetails(new MailTemplate())
    );
    appBar.addActionItem(newPlaceButton);
  }

  private Component createContent() {
    FlexBoxLayout content = new FlexBoxLayout(createGrid());
    content.setBoxSizing(BoxSizing.BORDER_BOX);
    content.setHeightFull();
    content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
    return content;
  }

  private Grid createGrid() {
    Grid<MailTemplate> grid = new Grid<>();

    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem()
        .ifPresent(this::viewDetails));
    dataProvider = new MailTemplateDataProvider();
    grid.setDataProvider(dataProvider);
    grid.setHeight("100%");

    grid.addColumn(MailTemplate::getName).setKey("name");
    grid.addColumn(MailTemplate::getMailAction).setKey("mailAction");

    grid.getColumns().forEach(column -> {
      if (column.getKey() != null) {
        column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
        column.setResizable(true);
      }
    });
    return grid;
  }

  private void filter(String filter) {
    dataProvider
        .setFilter(new DefaultFilter(
            StringUtils.isBlank(filter) ? null
                : "(?i).*" + filter + ".*",
            Boolean.TRUE));
  }

  private void viewDetails(MailTemplate mailTemplate) {
    UI.getCurrent()
        .navigate(MailTemplateAdminView.class,
            mailTemplate.getId() == null ? "-1" : mailTemplate.getId());
  }
}