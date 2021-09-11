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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import org.jhapy.frontend.utils.TextColor;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.views.JHapyMainView3;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewMenu extends View {

  private final Div mainMenu = new Div();
  private final Div topMenu = new Div();
  // private final Div breadcrumb = new Div();
  private final Div menuItemContainer = new Div();
  private final Div content = new Div();
  private long parentId = 0;
  private final List<CaptionChangedListener> captionChangedListeners = new ArrayList<>();
  private final List<ModuleSelectedListener> moduleSelectedListeners = new ArrayList<>();
  private final List<Menu> menuList;
  private List<TopMenuItem> topMenuList;
  private View menuDefaultContent;

  public interface CaptionChangedListener {
    void captionChanged(String newCaption);
  }

  public interface ModuleSelectedListener {
    boolean moduleSelected(Class browser, String menuName, long menuParentId, ViewMenu currentMenu);
  }

  public ViewMenu(List<Menu> menuList, long parentId, ModuleTab parentTab) {
    setParentTab(parentTab);
    this.menuList = menuList;
    this.parentId = parentId;
  }

  public ViewMenu(
      List<Menu> menuList, long parentId, List<TopMenuItem> topMenuList, ModuleTab parentTab) {
    setParentTab(parentTab);
    this.menuList = menuList;
    this.parentId = parentId;
    this.topMenuList = topMenuList;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    initLayout();
  }

  @Override
  public String getTitle() {
    return null;
  }

  private void initLayout() {
    setFlexDirection(FlexDirection.COLUMN);
    // setPadding(Top.L);
    mainMenu.addClassName("main-menu");
    topMenu.addClassName("top-menu");
    menuItemContainer.addClassName("menu-item-container");
    // breadcrumb.addClassName("top-menu");

    // mainMenu.add(breadcrumb);

    if (topMenuList != null && !topMenuList.isEmpty()) initTopMenu();

    loadMenu(this.parentId);

    mainMenu.add(menuItemContainer);
    setSizeFull();
    getStyle().set("display", "flex");
    add(mainMenu);

    content.addClassName("module-container");
    add(content);
    setAlignItems(FlexComponent.Alignment.CENTER);
  }

  private void initTopMenu() {

    Div topMenuSpacer = new Div();
    topMenuSpacer.addClassName("top-menu-spacer");

    topMenu.add(topMenuSpacer);

    if (topMenuList != null) {
      for (TopMenuItem item : topMenuList) {
        topMenu.add(item);
      }
    }

    mainMenu.add(topMenu);
  }

  private void loadMenu(long parentId) {

    List<Menu> menus = new ArrayList<>();

    for (Menu m : menuList) {
      if (m.getParentId() == parentId) {
        menus.add(m);
      }
    }

    if ((menus == null) || (menus.size() < 1)) {

      Menu menu = null;

      for (Menu m : menuList) {
        if (m.getId() == parentId) {
          menu = m;
          break;
        }
      }

      if (menu == null) return;

      for (ModuleSelectedListener listener : moduleSelectedListeners) {
        listener.moduleSelected(menu.getBrowser(), menu.getMenuName(), menu.getParentId(), this);
      }

      return;
    }

    List<Component> breadcrumbs = new ArrayList();
    if (parentId > 0) {
      Menu menu = null;

      for (Menu m : menuList) {
        if (m.getId() == parentId) {
          menu = m;
          break;
        }
      }

      if (menu == null) {
        menu = new Menu();
      }
      Menu parentMenu = menu;

      var p = menu;
      var label = UIUtils.createLabel(TextColor.PRIMARY, p.getMenuName());
      // label.setEnabled(false);
      Menu finalP = p;
      breadcrumbs.add(label);
      while (p.getParentId() != 0) {
        for (Menu m : menuList) {
          if (m.getId() == p.getParentId()) {
            p = m;
            break;
          }
        }
        var button = UIUtils.createLabel(TextColor.PRIMARY, p.getMenuName());
        Menu finalP1 = p;
        // button.addClickListener(event -> loadMenu(finalP1.getId()));
        breadcrumbs.add(button);
      }

      menuItemContainer.removeAll();
      Menu menuBack = new Menu(-1, 0, "Back", VaadinIcon.BACKWARDS.name(), null, null);
      MenuItem menuItem = new MenuItem(menuBack);

      menuItem.addClickListener(listener -> loadMenu(parentMenu.getParentId()));
      menuItemContainer.add(menuItem);

      changeCaption(menu.getMenuName());

      content.removeAll();

      if (menu.getMenuView() != null) {
        try {
          menuDefaultContent = menu.getMenuView().getDeclaredConstructor().newInstance();
          menuDefaultContent.setParentView(this);
          content.add(menuDefaultContent);
          setFlexGrow(1, menuDefaultContent);

        } catch (InstantiationException
            | IllegalAccessException
            | InvocationTargetException
            | NoSuchMethodException e) {
          e.printStackTrace();
        }
      }

    } else {
      menuItemContainer.removeAll();
      content.removeAll();
      changeCaption(getTranslation("element.dashboard.home"));
    }

    for (Menu menu : menus) {
      MenuItem menuItem = new MenuItem(menu);

      menuItem.addClickListener(
          listener -> {
            loadMenu(menu.getId());
          });

      menuItemContainer.add(menuItem);
    }
    if (!breadcrumbs.isEmpty()) {
      var button = UIUtils.createLabel(TextColor.PRIMARY, getTranslation("element.dashboard.home"));
      // button.addClickListener(event -> loadMenu(0));
      breadcrumbs.add(button);
    } else {
      var label = UIUtils.createLabel(TextColor.PRIMARY, getTranslation("element.dashboard.home"));
      // label.setEnabled(false);
      breadcrumbs.add(label);
    }
    Collections.reverse(breadcrumbs);
    getParentTab().getBreadcrumb().setBreadcrumbs(breadcrumbs);
    /*breadcrumb.removeAll();
    Collections.reverse(breadcrumbs);
    for (var i = 0; i < breadcrumbs.size(); i++) {
      breadcrumb.add(breadcrumbs.get(i));
      if ((i + 1) < breadcrumbs.size()) {
        breadcrumb.add(new Spacer());
        breadcrumb.add(UIUtils.createLabel(TextColor.PRIMARY, ">"));
        breadcrumb.add(new Spacer());
      }
    }
     */
    if (parentId == 0 && JHapyMainView3.get() != null) {
      View homePage = JHapyMainView3.get().getHomePage2();
      content.add(homePage);
    }
  }

  public void addCaptionChangedListener(CaptionChangedListener listener) {
    captionChangedListeners.add(listener);
  }

  private void changeCaption(String caption) {
    for (CaptionChangedListener listener : captionChangedListeners) {
      listener.captionChanged(caption);
    }
  }

  public void addModuleSelectedListener(ModuleSelectedListener listener) {
    moduleSelectedListeners.add(listener);
  }
}