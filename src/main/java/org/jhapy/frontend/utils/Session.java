/*
 * Copyright (c) 2021. Sasak UI
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
 * Code written by Husein Musawa @ 2021
 */

package org.jhapy.frontend.utils;

import com.vaadin.flow.component.UI;

public class Session {

  public static void setLoggedIn(boolean loggedIn) {
    UI.getCurrent().getSession().setAttribute("loggedIn", loggedIn);
  }

  public static void setCurrentUser(ConnectedUser currentUser) {
    UI.getCurrent().getSession().setAttribute("currentUser", currentUser);
  }

  public static void setCurrentYear(int currentYear) {
    UI.getCurrent().getSession().setAttribute("currentYear", currentYear);
  }

  public static void setAppMenu(AppMenu appMenu) {
    UI.getCurrent().getSession().setAttribute("appMenu", appMenu);
  }

  public static void login(ConnectedUser currentUser) {
    setLoggedIn(true);
    setCurrentUser(currentUser);
  }

  public static void logOut() {

    setLoggedIn(false);
    setCurrentUser(null);
    UI.getCurrent().navigate("login");
  }

  public static boolean isLoggedIn() {

    if (UI.getCurrent().getSession().getAttribute("loggedIn") == null) {
      return false;
    }

    return Boolean.valueOf(UI.getCurrent().getSession().getAttribute("loggedIn").toString());
  }

  public static ConnectedUser getCurrentUser() {

    ConnectedUser currentUser =
        (ConnectedUser) UI.getCurrent().getSession().getAttribute("currentUser");

    return currentUser;
  }

  public static AppMenu getAppMenu() {
    AppMenu appMenu = (AppMenu) UI.getCurrent().getSession().getAttribute("appMenu");

    return appMenu;
  }

  public static String getComputerName() {

    return UI.getCurrent().getSession().getBrowser().getAddress();
  }

  public void logout() {

    setLoggedIn(false);
    setCurrentUser(new ConnectedUser());
  }
}