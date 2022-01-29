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
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.provider.DataProvider;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.menubar.ModuleToolbar;
import org.jhapy.frontend.dataproviders.DefaultDataProvider;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.DefaultSliceDataProvider;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BoxSizing;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;

import java.lang.reflect.InvocationTargetException;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 8/27/19
 */
@CssImport(value = "./styles/grids.css", themeFor = "vaadin-grid")
public abstract class DefaultMasterView<T extends BaseEntity, F extends DefaultFilter>
    extends ViewFrame {

  protected final String I18N_PREFIX;
  protected final Class entityViewClass;
  protected final MyI18NProvider myI18NProvider;
  private final Class<T> entityType;
  protected Grid<T> grid;
  protected DefaultDataProvider<T, F> dataProvider;
  protected DefaultSliceDataProvider<T, F> sliceProvider;
  protected ModuleToolbar moduleToolbar;

  public DefaultMasterView(
      String I18N_PREFIX,
      Class<T> entityType,
      Class entityViewClass,
      MyI18NProvider myI18NProvider) {
    this(
        I18N_PREFIX, entityType, (DefaultDataProvider<T, F>) null, entityViewClass, myI18NProvider);
  }

  public DefaultMasterView(
      String I18N_PREFIX,
      Class<T> entityType,
      DefaultDataProvider<T, F> dataProvider,
      Class entityViewClass,
      MyI18NProvider myI18NProvider) {
    super();
    this.I18N_PREFIX = I18N_PREFIX;
    this.entityType = entityType;
    this.dataProvider = dataProvider;
    this.entityViewClass = entityViewClass;
    this.myI18NProvider = myI18NProvider;
  }

  public DefaultMasterView(
      String I18N_PREFIX,
      Class<T> entityType,
      DefaultSliceDataProvider<T, F> sliceProvider,
      Class entityViewClass,
      MyI18NProvider myI18NProvider) {
    super();
    this.I18N_PREFIX = I18N_PREFIX;
    this.entityType = entityType;
    this.sliceProvider = sliceProvider;
    this.entityViewClass = entityViewClass;
    this.myI18NProvider = myI18NProvider;
  }

  protected DataProvider<T, F> getDataProvider() {
    if (dataProvider != null) {
      return dataProvider;
    } else {
      return sliceProvider;
    }
  }

  protected Class<T> getEntityType() {
    return entityType;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    initHeader();

    setViewContent(createContent());

    filter(null, false);
  }

  @Override
  public boolean displayInANewTab() {
    return false;
  }

  protected void initHeader() {
    moduleToolbar = new ModuleToolbar(entityType.getSimpleName(), this);
    moduleToolbar.addGoBackListener(
        () -> {
          if (displayInANewTab()) {
            getParentTab().closeTab();
            return;
          }
          if (super.getGoBackListener() != null) super.getGoBackListener().goBack();
          else super.getMenuBackListener().goBack();
        });

    moduleToolbar.setSearchTextFieldVisible(isShowSearchTextField());
    moduleToolbar.addSearchTextChangedListener(
        searchText -> filter(searchText, moduleToolbar.getActiveFilterValue()));

    moduleToolbar.setShowInactiveButtonVisible(isShowInactiveFilter());
    moduleToolbar.addShowInactiveChangedListener(
        showInactive -> filter(moduleToolbar.getSearchText(), showInactive));

    moduleToolbar.setFilterButtonVisible(false);

    if (canCreateRecord()) {
      moduleToolbar.setAddButtonVisible(true);
      moduleToolbar.addNewRecordListener(
          () -> {
            try {
              showDetails(entityType.getDeclaredConstructor().newInstance());
            } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException ignored) {
            }
          });
    }
    if (dataProvider == null)
      moduleToolbar.addRefreshListener(
          () -> {
            grid.getLazyDataView().refreshAll();
          });
    else moduleToolbar.addRefreshListener(dataProvider::refreshAll);

    setViewHeader(moduleToolbar);
  }

  protected boolean canCreateRecord() {
    return true;
  }

  protected boolean isShowInactiveFilter() {
    return true;
  }

  protected boolean isShowSearchTextField() {
    return true;
  }

  private Component createContent() {
    FlexBoxLayout content = new FlexBoxLayout(createGrid());
    content.setBoxSizing(BoxSizing.BORDER_BOX);
    content.setHeightFull();
    content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

    Label nbRows = UIUtils.createH4Label(getTranslation("element.global.nbRows", 0));
    if (dataProvider == null)
      grid.getLazyDataView()
          .addItemCountChangeListener(
              event ->
                  nbRows.setText(getTranslation("element.global.nbRows", event.getItemCount())));
    else
      dataProvider.setPageObserver(
          tPage ->
              nbRows.setText(getTranslation("element.global.nbRows", tPage.getTotalElements())));

    FooterRow footerRow = grid.appendFooterRow();
    footerRow.getCell(grid.getColumns().get(0)).setComponent(nbRows);
    return content;
  }

  protected abstract Grid createGrid();

  protected void showDetails(T entity) {
    JHapyMainView3.get()
        .displayViewFromParentView(
            this,
            currentViewParams,
            entityViewClass,
            Boolean.FALSE.equals(entity.getPersisted()) ? "-1" : entity.getId().toString());
  }

  protected void filter(String filter, Boolean showInactive) {
    if (dataProvider != null)
      dataProvider.setFilter(
          (F) new DefaultFilter(StringUtils.isBlank(filter) ? null : filter, showInactive));
    else
      throw new RuntimeException(
          "A dataprovider is need or you need to provide custom filter query method");
  }
}
