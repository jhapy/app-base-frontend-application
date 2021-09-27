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

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jhapy.frontend.views.BaseDashboardView;

@Data
@NoArgsConstructor
public class Menu {

  private long id;
  private long parentId;
  private String menuName;
  private String iconName;
  private Class browser;
  private String badge;
  private Class<? extends BaseDashboardView> menuView = null;
  private String newViewParams;

  public Menu(
      long id, long parentId, String menuName, String iconName, Class browser, String badge) {
    this(id, parentId, menuName, iconName, browser, badge, null, null);
  }

  public Menu(
          long id, long parentId, String menuName, String iconName, Class browser, String badge, String newViewParams) {
    this(id, parentId, menuName, iconName, browser, badge, null, newViewParams);
  }

  public Menu(
      long id,
      long parentId,
      String menuName,
      String iconName,
      Class browser,
      String badge,
      Class<? extends BaseDashboardView> menuView,
      String newViewParams) {
    this.id = id;
    this.parentId = parentId;
    this.menuName = menuName;
    this.iconName = iconName;
    this.browser = browser;
    this.badge = badge;
    this.menuView = menuView;
    this.newViewParams = newViewParams;
  }
}