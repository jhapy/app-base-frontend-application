/*
 * Copyright (c) 2021. Sasak UI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.jhapy.frontend.components.navigation.menubar;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import org.jhapy.frontend.utils.UIUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@CssImport("./menubar/module-styles.css")
@CssImport("./menubar/module-toolbar.css")
public class ModuleToolbar extends FlexLayout {
  private static final String CLASS_NAME = "module-toolbar";

  private String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
  private boolean filterButtonVisible = true;
  private boolean searchTextFieldVisible = true;
  private boolean refreshButtonVisible = true;
  private boolean showInactiveButtonVisible = true;
  private boolean addButtonVisible = true;

  private final Div menuBack = new Div();
  private final ModuleSearchTextField searchTextField = new ModuleSearchTextField();
  private final Div menuRefresh = new Div();
  private final Div menuAddRecord = new Div();
  private final Div menuFilter = new Div();
  private final Div otherButtons = new Div();

  private final String moduleName;
  private final View parentLayout;

  private final List<GoBackListener> goBackListeners = new ArrayList<>();
  private final List<FilterClickListener> filterClickListeners = new ArrayList<>();
  private final List<SearchTextChangedListener> searchTextChangedListeners = new ArrayList<>();
  private final List<RefreshListener> refreshListeners = new ArrayList<>();
  private final List<ShowInactiveChangedListener> showInactiveListeners = new ArrayList<>();
  private final List<NewRecordListener> newRecordListeners = new ArrayList<>();

  private final Label lblYear;
  private final Icon filterIcon;

  private final Checkbox activeFilter;

  public interface GoBackListener {
    void goBack();
  }

  public interface SearchTextChangedListener {
    void searchTextChanged(String searchText);
  }

  public interface ShowInactiveChangedListener {
    void showInactiveChanged(Boolean showInactive);
  }

  public interface FilterClickListener {
    void filterButtonClicked();
  }

  public interface RefreshListener {
    void refresh();
  }

  public interface NewRecordListener {
    void newRecord();
  }

  public ModuleToolbar(String moduleName, View parentLayout) {
    this.moduleName = moduleName;
    this.parentLayout = parentLayout;

    setClassName(CLASS_NAME);

    // setWidth("calc(100% - 10px)");
    setHeight("45px");
    setAlignItems(Alignment.CENTER);
    setFlexGrow(1);
    getStyle().set("font-size", "small");
    // getStyle().set("margin-left", "5px");
    // getStyle().set("margin-right", "5px");

    Button lblBack =
        UIUtils.createButton("Back", VaadinIcon.ARROW_LEFT, ButtonVariant.LUMO_TERTIARY);
    lblBack.getStyle().set("margin-left", "10px");

    menuBack.add(lblBack);
    menuBack.getStyle().set("align-items", "center");
    menuBack.addClickListener(
        listener -> {
          for (GoBackListener goBackListener : goBackListeners) {
            goBackListener.goBack();
          }
        });

    searchTextField.getStyle().set("flex-grow", "1");
    searchTextField.getStyle().set("margin", "0px 5px");
    searchTextField.getStyle().set("align-items", "center");
    searchTextField.addValueChangeListener(
        listener -> {
          for (SearchTextChangedListener searchTextChangedListener : searchTextChangedListeners) {
            searchTextChangedListener.searchTextChanged(searchTextField.getValue());
          }
        });

    Button newRecordButton = UIUtils.createTertiaryButton(VaadinIcon.PLUS);
    menuAddRecord.add(newRecordButton);
    menuAddRecord.getStyle().set("align-items", "center");
    menuAddRecord.getStyle().set("margin-right", "5px");
    newRecordButton.addClickListener(
        listener -> {
          if (newRecordListeners != null) {
            for (NewRecordListener newRecordListener : newRecordListeners) {
              newRecordListener.newRecord();
            }
          }
        });

    Button refreshButton =
        UIUtils.createButton(
            /*getTranslation("action.global.refresh"),*/
            VaadinIcon.REFRESH, ButtonVariant.LUMO_TERTIARY);
    menuRefresh.add(refreshButton);
    menuRefresh.getStyle().set("align-items", "center");
    menuRefresh.getStyle().set("margin-right", "5px");
    refreshButton.addClickListener(
        listener -> {
          if (refreshListeners != null) {
            for (RefreshListener refreshListener : refreshListeners) {
              refreshListener.refresh();
            }
          }
        });

    activeFilter = new Checkbox(getTranslation("action.search.showInactive"));
    activeFilter.setValue(false);
    activeFilter.addValueChangeListener(
        listener -> {
          if (showInactiveListeners != null) {
            for (ShowInactiveChangedListener showInactiveChangedListener : showInactiveListeners) {
              showInactiveChangedListener.showInactiveChanged(listener.getValue());
            }
          }
        });
    lblYear = new Label(year);

    filterIcon = VaadinIcon.FILTER.create();
    filterIcon.setSize("15px");
    filterIcon.setColor("inherit");
    filterIcon.getStyle().set("margin-left", "20px");

    menuFilter.addClassName("module-button");

    menuFilter.add(filterIcon);
    menuFilter.addClickListener(
        listener -> {
          if (filterClickListeners != null) {
            for (FilterClickListener filterClickListener : filterClickListeners) {
              filterClickListener.filterButtonClicked();
            }
          }
        });

    otherButtons.getStyle().set("align-items", "center");
    otherButtons.getStyle().set("margin-right", "5px");

    refreshButtons();
  }

  public void refreshButtons() {
    removeAll();
    add(menuBack);
    if (searchTextFieldVisible) {
      add(searchTextField);
    } else {
      add(new FullWidthSpacer());
    }

    if (showInactiveButtonVisible) add(activeFilter);
    if (addButtonVisible) add(menuAddRecord);
    if (refreshButtonVisible) add(menuRefresh);

    menuFilter.removeAll();
    if (year != null) {
      menuFilter.add(lblYear);
      menuFilter.setWidth("65px");
    } else {
      menuFilter.getStyle().remove("width");
    }
    menuFilter.add(filterIcon);
    if (filterButtonVisible) add(menuFilter);
    if (otherButtons.getChildren().count() > 0) add(otherButtons);
  }

  public void addOtherButton(Button otherButton) {
    otherButtons.add(otherButton);
    refreshButtons();
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
    refreshButtons();
  }

  public boolean isFilterButtonVisible() {
    return filterButtonVisible;
  }

  public void setFilterButtonVisible(boolean filterButtonVisible) {
    this.filterButtonVisible = filterButtonVisible;
    refreshButtons();
  }

  public boolean isSearchTextFieldVisible() {
    return searchTextFieldVisible;
  }

  public void setSearchTextFieldVisible(boolean searchTextFieldVisible) {
    this.searchTextFieldVisible = searchTextFieldVisible;
    refreshButtons();
  }

  public boolean isRefreshButtonVisible() {
    return refreshButtonVisible;
  }

  public void setRefreshButtonVisible(boolean refreshButtonVisible) {
    this.refreshButtonVisible = refreshButtonVisible;
    refreshButtons();
  }

  public boolean isShowInactiveButtonVisible() {
    return showInactiveButtonVisible;
  }

  public void setShowInactiveButtonVisible(boolean showInactiveButtonVisible) {
    this.showInactiveButtonVisible = showInactiveButtonVisible;
  }

  public boolean isAddButtonVisible() {
    return addButtonVisible;
  }

  public void setAddButtonVisible(boolean addButtonVisible) {
    this.addButtonVisible = addButtonVisible;
  }

  public void addGoBackListener(GoBackListener listener) {
    goBackListeners.add(listener);
  }

  public void addSearchTextChangedListener(SearchTextChangedListener listener) {
    searchTextChangedListeners.add(listener);
  }

  public void addFilterClickedListener(FilterClickListener listener) {
    filterClickListeners.add(listener);
  }

  public void addRefreshListener(RefreshListener listener) {
    refreshListeners.add(listener);
  }

  public void addNewRecordListener(NewRecordListener listener) {
    newRecordListeners.add(listener);
  }

  public void addShowInactiveChangedListener(ShowInactiveChangedListener listener) {
    showInactiveListeners.add(listener);
  }

  public String getCurrentYear() {
    return lblYear.getText();
  }

  public String getSearchText() {
    return searchTextField.getValue();
  }

  public Boolean getActiveFilterValue() {
    return activeFilter.getValue();
  }
}