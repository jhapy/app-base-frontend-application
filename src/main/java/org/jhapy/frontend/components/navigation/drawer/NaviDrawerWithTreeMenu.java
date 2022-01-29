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

package org.jhapy.frontend.components.navigation.drawer;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import elemental.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.components.dragger.Dragger;
import org.jhapy.frontend.dataproviders.MenuHierarchicalDataProvider;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.views.menu.MenuData;
import org.jhapy.frontend.views.menu.MenuEntry;
import org.vaadin.tatu.Tree;

import java.util.Collection;

@CssImport("./styles/components/navi-drawer.css")
@JsModule("./swipe-away.js")
public class NaviDrawerWithTreeMenu extends HorizontalLayout
    implements AfterNavigationObserver, HasLogger {

  private static final String CLASS_NAME = "navi-drawer";
  private static final String RAIL = "rail";
  private static final String OPEN = "open";
  private final MenuHierarchicalDataProvider dataProvider;
  private final Div main = new Div();
  private Div scrim;
  private Div mainContent;
  private TextField search;
  private Div scrollableArea;
  private Button railButton;
  private Tree<MenuEntry> menu;
  private MenuData menuData;
  private Dragger dragger;
  private MenuEntry lastMenuEntry = null;

  public NaviDrawerWithTreeMenu(
      MenuHierarchicalDataProvider menuDataProvider,
      boolean showSearchMenu,
      Component altSearchMenu,
      String version,
      String environment) {
    this.dataProvider = menuDataProvider;
    setClassName(CLASS_NAME);
    getElement().getThemeList().remove("spacing");

    initScrim();
    initMainContent();

    initHeader();
    if (showSearchMenu) {
      initSearch();
    } else if (altSearchMenu != null) {
      mainContent.add(altSearchMenu);
    }

    initScrollableArea();
    initMenu();

    initFooter(version, environment);
    add(main);
    dragger = new Dragger(getMainContent());
    dragger.setClassName(CLASS_NAME + "__dragger");
    add(dragger);
  }

  public MenuHierarchicalDataProvider getDataProvider() {
    return dataProvider;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    var ui = attachEvent.getUI();
    ui.getPage()
        .executeJavaScript(
            "window.addSwipeAway($0,$1,$2,$3)",
            mainContent.getElement(),
            this,
            "onSwipeAway",
            scrim.getElement());
  }

  @ClientCallable
  public void onSwipeAway(JsonObject data) {
    close();
  }

  private void initScrim() {
    // Backdrop on small viewports
    scrim = new Div();
    scrim.addClassName(CLASS_NAME + "__scrim");
    scrim.addClickListener(event -> close());
    main.add(scrim);
  }

  private void initMainContent() {
    mainContent = new Div();
    mainContent.addClassName(CLASS_NAME + "__content");
    main.add(mainContent);
  }

  public Div getMainContent() {
    return mainContent;
  }

  private void initHeader() {
    mainContent.add(new BrandExpression(getTranslation("element.application.title")));
  }

  private void initSearch() {
    search = new TextField();
    search.addValueChangeListener(
        e ->
            menu.getDataProvider()
                .withConvertedFilter(
                    filterValue -> item -> item.getTitle().startsWith(e.getValue())));
    search.setClearButtonVisible(true);
    search.setPlaceholder("Search");
    search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    mainContent.add(search);

    search.setVisible(SecurityUtils.isUserLoggedIn());
  }

  public void toogleSearch() {
    if (search != null) {
      search.setVisible(SecurityUtils.isUserLoggedIn());
    }
  }

  private void initScrollableArea() {
    scrollableArea = new Div();
    scrollableArea.addClassName(CLASS_NAME + "__scroll-area");
    mainContent.add(scrollableArea);
  }

  public void refreshMenu() {
    if (dataProvider != null) {
      dataProvider.setRootMenu(menuData);
      dataProvider.refreshAll();
    } else {
      menu.setItems(menuData.getRootItems(), menuData::getChildItems);
    }
  }

  public void addMenuEntry(MenuEntry menuEntry) {
    menuData.addMenuEntry(menuEntry);
  }

  public MenuEntry getLastMenuEntry() {
    return lastMenuEntry;
  }

  private void initMenu() {
    menuData = new MenuData();

    menu = new Tree<>(MenuEntry::getTitle);
    if (dataProvider != null) {
      menu.setDataProvider(dataProvider);
    }

    menu.setSizeFull();
    menu.setHeightByRows(true);
    menu.setHeightByRows(true);
    menu.setId("treegridbasic");
    menu.getStyle().set("font-size", "0.8em");
    menu.setItemIconProvider(MenuEntry::getVaadinIcon);
    menu.setItemTitleProvider(MenuEntry::getTitle);
    menu.addItemClickListener(
        event -> {
          MenuEntry selectedItem = getSelectedItem();
          if (selectedItem != null) {
            navigate(selectedItem);
          }
        });
    menu.addExpandListener(
        menuEntryTreeGridExpandEvent -> {
          if (!menuEntryTreeGridExpandEvent.isFromClient()) {
            return;
          }

          Collection<MenuEntry> items = menuEntryTreeGridExpandEvent.getItems();

          if (!items.isEmpty()) {
            var item = items.iterator().next();
            menu.select(item);
            if (item.getTargetClass() != null && !item.equals(lastMenuEntry)) {
              navigate(item);
            }
          }
        });
    menu.addCollapseListener(
        menuEntryTreeGridCollapseEvent -> {
          if (!menuEntryTreeGridCollapseEvent.isFromClient()) {
            return;
          }
          var items = menuEntryTreeGridCollapseEvent.getItems();

          if (!items.isEmpty()) {
            var item = items.iterator().next();
            menu.select(item);
            if (item.getTargetClass() != null && !item.equals(lastMenuEntry)) {
              navigate(item);
            }
          }
        });

    var contextMenu = menu.addContextMenu();
    contextMenu.setDynamicContentHandler(
        menuEntry -> {
          if (menuEntry == null) {
            return false;
          }
          if (menuEntry.getContextMenu() == null) {
            return false;
          }

          contextMenu.removeAll();

          menuEntry
              .getContextMenu()
              .forEach(
                  menuItem -> {
                    if (menuItem.getClickListener() == null) {
                      if (menuItem.getComponent() != null) {
                        contextMenu.add(menuItem.getComponent());
                      } else {
                        contextMenu.add(menuItem.getTitle());
                      }
                    } else {
                      if (menuItem.getComponent() != null) {
                        contextMenu.addItem(
                            menuItem.getComponent(),
                            clickedMenu -> menuItem.getClickListener().accept(clickedMenu));
                      } else {
                        contextMenu.addItem(
                            menuItem.getTitle(),
                            clickedMenu -> menuItem.getClickListener().accept(clickedMenu));
                      }
                    }
                  });
          return true;
        });
    scrollableArea.add(menu);
  }

  public MenuEntry getSelectedItem() {
    return menu.getSelectedItems().isEmpty() ? null : menu.getSelectedItems().iterator().next();
  }

  public boolean isSelected(String targetId) {
    var selected = getSelectedItem();
    if (selected == null || StringUtils.isBlank(selected.getTargetId())) {
      return false;
    }
    return selected.getTargetId().equalsIgnoreCase(targetId);
  }

  public void navigate(Class<? extends Component> targetClass) {
    UI.getCurrent().navigate(targetClass);
  }

  public void navigate(MenuEntry menuEntry) {
    if (menuEntry == null) {
      return;
    }

    var loggerPrefix = getLoggerPrefix("navigate", menuEntry.getTitle());
    if (menuEntry.getTargetClass() == null) {
      return;
    }

    if (menuEntry.getTargetParams() != null) {
      debug(loggerPrefix, "Navigate with Target Params");
      var params = menuEntry.getTargetParams().toString();
      debug(
          loggerPrefix, "Target Class = {0}, Parameter = {1}", menuEntry.getTargetClass(), params);
      UI.getCurrent().navigate(menuEntry.getTargetClass(), params);
    } else if (menuEntry.getTargetId() != null) {
      debug(loggerPrefix, "Navigate with Target ID");
      var targetId = menuEntry.getTargetId();
      debug(
          loggerPrefix,
          "Target Class = {0}, Parameter = {1}",
          menuEntry.getTargetClass(),
          targetId);
      UI.getCurrent().navigate(menuEntry.getTargetClass(), targetId);
    } else {
      debug(loggerPrefix, "Navigate without Target Params or Target ID");
      debug(loggerPrefix, "Target Class = {0}", menuEntry.getTargetClass());
      UI.getCurrent().navigate(menuEntry.getTargetClass());
    }
    lastMenuEntry = menuEntry;
  }

  private void initFooter(String version, String environement) {
    if (StringUtils.isNotBlank(version)) {
      var l = UIUtils.createH5Label("Version " + version + " (" + environement + ")");
      l.addClassName(CLASS_NAME + "__footer");
      l.addClassName("version");
      mainContent.add(l);
    }

    railButton = UIUtils.createSmallButton("Collapse", VaadinIcon.CHEVRON_LEFT_SMALL);
    railButton.addClassName(CLASS_NAME + "__footer");
    railButton.addClickListener(event -> toggleRailMode());
    railButton.getElement().setAttribute("aria-label", "Collapse menu");
    mainContent.add(railButton);
  }

  private void toggleRailMode() {
    if (getElement().hasAttribute(RAIL)) {
      getElement().setAttribute(RAIL, false);
      railButton.setIcon(new Icon(VaadinIcon.CHEVRON_LEFT_SMALL));
      railButton.setText("Collapse");
      UIUtils.setAriaLabel("Collapse menu", railButton);
    } else {
      getElement().setAttribute(RAIL, true);
      railButton.setIcon(new Icon(VaadinIcon.CHEVRON_RIGHT_SMALL));
      railButton.setText("Expand");
      UIUtils.setAriaLabel("Expand menu", railButton);
      getUI()
          .get()
          .getPage()
          .executeJs(
              "var originalStyle = getComputedStyle($0).pointerEvents;" //
                  + "$0.style.pointerEvents='none';" //
                  + "setTimeout(function() {$0.style.pointerEvents=originalStyle;}, 170);",
              getElement());
    }
  }

  public void toggle() {
    if (getElement().hasAttribute(OPEN)) {
      close();
    } else {
      open();
    }
  }

  private void open() {
    getElement().setAttribute(OPEN, true);
  }

  private void close() {
    getElement().setAttribute(OPEN, false);
    applyIOS122Workaround();
  }

  private void applyIOS122Workaround() {
    // iOS 12.2 sometimes fails to animate the menu away.
    // It should be gone after 240ms
    // This will make sure it disappears even when the browser fails.
    getUI()
        .ifPresent(
            ui ->
                ui.getPage()
                    .executeJs(
                        "var originalStyle = getComputedStyle($0).transitionProperty;" //
                            + "setTimeout(function() {$0.style.transitionProperty='padding'; requestAnimationFrame(function() {$0.style.transitionProperty=originalStyle})}, 250);",
                        mainContent.getElement()));
  }

  public MenuData getFreshMenu() {
    menuData = new MenuData();
    return menuData;
  }

  public MenuData getMenu() {
    return menuData;
  }

  public Tree<MenuEntry> getMenuComponent() {
    return menu;
  }

  @Override
  public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
    close();
  }

  public void setDragger(Dragger dragger) {
    this.dragger = dragger;
  }
}
