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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.HasUrlParameter;

import java.util.List;
import java.util.stream.Collectors;

@CssImport("./styles/components/navi-menu.css")
public class NaviMenu extends Div {

  private final String CLASS_NAME = "navi-menu";

  public NaviMenu() {
    setClassName(CLASS_NAME);
  }

  protected void addNaviItem(NaviItem item) {
    add(item);
  }

  protected void addNaviItem(NaviItem parent, NaviItem item) {
    parent.addSubItem(item);
    addNaviItem(item);
  }

  public void filter(String filter) {
    getNaviItems()
        .forEach(
            naviItem -> {
              boolean matches = naviItem.getText().toLowerCase().contains(filter.toLowerCase());
              naviItem.setVisible(matches);
            });
  }

  public NaviItem addNaviItem(String text, Class<? extends Component> navigationTarget) {
    NaviItem item = new NaviItem(text, navigationTarget);
    addNaviItem(item);
    return item;
  }

  public NaviItem addNaviItem(
      VaadinIcon icon, String text, Class<? extends Component> navigationTarget) {
    NaviItem item = new NaviItem(icon, text, navigationTarget);
    addNaviItem(item);
    return item;
  }

  public NaviItem addNaviItem(
      Image image, String text, Class<? extends Component> navigationTarget) {
    NaviItem item = new NaviItem(image, text, navigationTarget);
    addNaviItem(item);
    return item;
  }

  public NaviItem addNaviItem(
      NaviItem parent, Image image, String text, Class<? extends Component> navigationTarget) {
    NaviItem item = new NaviItem(image, text, navigationTarget);
    addNaviItem(parent, item);
    return item;
  }

  public NaviItem addNaviItem(
      NaviItem parent, VaadinIcon icon, String text, Class<? extends Component> navigationTarget) {
    NaviItem item = new NaviItem(icon, text, navigationTarget);
    addNaviItem(parent, item);
    return item;
  }

  public NaviItem addNaviItem(
      NaviItem parent, String text, Class<? extends Component> navigationTarget) {
    NaviItem item = new NaviItem(text, navigationTarget);
    addNaviItem(parent, item);
    return item;
  }

  public <T, C extends Component & HasUrlParameter<T>> NaviItem addNaviItem(
      VaadinIcon icon, String text, Class<C> navigationTarget, T parameter) {
    NaviItem item = new NaviItem(icon, text, navigationTarget, parameter);
    addNaviItem(item);
    return item;
  }

  public <T, C extends Component & HasUrlParameter<T>> NaviItem addNaviItem(
      NaviItem parent, String text, Class<C> navigationTarget, T parameter) {
    NaviItem item = new NaviItem(text, navigationTarget, parameter);
    addNaviItem(parent, item);
    return item;
  }

  public List<NaviItem> getNaviItems() {
    return (List<NaviItem>) (List) getChildren().collect(Collectors.toList());
  }
}