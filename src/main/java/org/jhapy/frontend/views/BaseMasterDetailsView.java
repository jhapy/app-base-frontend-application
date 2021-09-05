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

package org.jhapy.frontend.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawer;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerFooter;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerHeader;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.layout.SplitViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.utils.css.BoxSizing;

import java.lang.reflect.InvocationTargetException;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 8/27/19
 */
public abstract class BaseMasterDetailsView<T, F extends DefaultFilter> extends SplitViewFrame {

  protected final String I18N_PREFIX;
  protected Grid<T> grid;
  protected DetailsDrawer detailsDrawer;
  protected DetailsDrawerHeader detailsDrawerHeader;
  protected DetailsDrawerFooter detailsDrawerFooter;
  protected Binder<T> binder;
  private T currentEditing;
  private final Class<T> entityType;

  public BaseMasterDetailsView(String I18N_PREFIX, Class<T> entityType) {
    this.I18N_PREFIX = I18N_PREFIX;
    this.entityType = entityType;
    this.binder = new BeanValidationBinder<>(entityType);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    initHeader();
    setViewContent(createContent());
    setViewDetails(createDetailsDrawer());

    filter(null);

    if (currentEditing != null) {
      showDetails(currentEditing);
    }
  }

  protected T getCurrentEditing() {
    return currentEditing;
  }

  protected void setCurrentEditing(T currentEditing) {
    this.currentEditing = currentEditing;
  }

  protected void initHeader() {
    initSearchBar();
  }

  protected void showDetails() {
    try {
      showDetails(entityType.getDeclaredConstructor().newInstance());

    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException ignored) {
    }
  }

  protected void initSearchBar() {
    /*
    AppBar appBar = JHapyMainView3.get().getAppBar();
    appBar.disableGlobalSearch();
    Button searchButton = UIUtils.createTertiaryButton(VaadinIcon.SEARCH);
    searchButton.addClickListener(event -> appBar.searchModeOn());
    appBar.addSearchListener(event -> filter((String) event.getValue()));
    appBar.setSearchPlaceholder(getTranslation("element.global.search"));
    appBar.addActionItem(searchButton);

     */
  }

  private Component createContent() {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setFlexDirection(FlexDirection.COLUMN);
    Component header = createHeader();
    if (header != null) {
      content.add(header);
    }
    content.add(createGrid());
    content.setBoxSizing(BoxSizing.BORDER_BOX);
    content.setHeightFull();
    content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
    return content;
  }

  protected abstract Grid createGrid();

  protected Component createHeader() {
    return null;
  }

  protected DetailsDrawer.Position getDetailsDrawerPosition() {
    return DetailsDrawer.Position.RIGHT;
  }

  protected Position getSplitViewFramePosition() {
    return Position.RIGHT;
  }

  private DetailsDrawer createDetailsDrawer() {
    detailsDrawer = new DetailsDrawer(getDetailsDrawerPosition());
    setViewDetailsPosition(getSplitViewFramePosition());
    // Header

    detailsDrawerHeader =
        new DetailsDrawerHeader(getTranslation("element." + I18N_PREFIX + "className"));
    detailsDrawerHeader.addCloseListener(
        e -> {
          detailsDrawer.hide();
          currentEditing = null;
        });
    detailsDrawer.setHeader(detailsDrawerHeader);

    // Footer
    detailsDrawerFooter = new DetailsDrawerFooter();
    detailsDrawerFooter.setSaveButtonVisible(false);
    detailsDrawerFooter.setSaveAndNewButtonVisible(false);
    detailsDrawerFooter.setDeleteButtonVisible(false);

    detailsDrawerFooter.addCancelListener(
        e -> {
          detailsDrawer.hide();
          currentEditing = null;
        });
    detailsDrawer.setFooter(detailsDrawerFooter);

    return detailsDrawer;
  }

  protected void showDetails(T entity) {
    this.binder = new BeanValidationBinder<>(entityType);

    currentEditing = entity;
    detailsDrawer.setContent(createDetails(entity));
    detailsDrawer.show();
  }

  protected abstract Component createDetails(T entity);

  protected void filter(String filter) {}
}