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

package org.jhapy.frontend.views.admin.eureka;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaApplicationInstance;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.layout.size.Bottom;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Right;
import org.jhapy.frontend.utils.IconSize;
import org.jhapy.frontend.utils.TextColor;
import org.jhapy.frontend.utils.UIUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
public abstract class ActuatorBaseView extends Component implements HasLogger {

  protected final String I18N_PREFIX;
  protected final AuthorizationHeaderUtil authorizationHeaderUtil;
  protected final RestTemplate restTemplate = new RestTemplate();

  protected EurekaInfo eurekaInfo;
  protected EurekaApplication currentEurekaApplication;
  protected EurekaApplicationInstance currentEurekaApplicationInstance;

  protected final Authentication authentication;

  protected Label titleLabel;
  protected ScheduledExecutorService timer;
  protected final UI ui;

  public ActuatorBaseView(UI ui, String I18N_PREFIX,
      AuthorizationHeaderUtil authorizationHeaderUtil) {
    this.ui = ui;
    this.I18N_PREFIX = I18N_PREFIX;
    this.authorizationHeaderUtil = authorizationHeaderUtil;
    this.authentication = SecurityContextHolder.getContext().getAuthentication();
  }

  public abstract Component getContent(EurekaInfo eurekaInfo);

  public void refresh() {
  }

  public void setRefreshRate(Integer refreshRate) {
    var loggerPrefix = getLoggerPrefix("setRefreshRate");
    if (refreshRate == null) {
      if (timer != null) {
        logger().debug(loggerPrefix + "Cancel timer");
        timer.shutdown();
      } else {
        logger().debug(loggerPrefix + "No timer set");
      }
    } else {
      if (timer != null) {
        timer.shutdown();
        timer = null;
      }
      logger().debug(loggerPrefix + "Create a new timer");
      timer = Executors.newScheduledThreadPool(0);
      timer.scheduleAtFixedRate(() -> ui.access(this::refresh), 0, refreshRate, TimeUnit.SECONDS);

      logger().debug(loggerPrefix + "Schedule timer");
    }
  }

  protected FlexBoxLayout createHeader(VaadinIcon icon, String title, Component... buttons) {
    FlexBoxLayout header = new FlexBoxLayout(
        UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, icon),
        titleLabel = UIUtils.createH3Label(title));
    header.setWidthFull();
    if (buttons.length > 0) {
      header.add(buttons);
    }
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.setMargin(Bottom.L, Horizontal.RESPONSIVE_L);
    header.setSpacing(Right.L);
    return header;
  }

  protected Component getEurekaInstancesList(boolean displayInstanceListAsMenu,
      List<EurekaApplication> eurekaApplicationList,
      BiConsumer<EurekaApplication, EurekaApplicationInstance> instanceSelectedFunction) {
    MenuBar menuBar = new MenuBar();
    menuBar.addThemeVariants(MenuBarVariant.LUMO_PRIMARY);

    if (displayInstanceListAsMenu) {
      MenuItem applicationInstance = menuBar
          .addItem(getTranslation("element.actuator.eurekaInstance"));
      SubMenu eurekaApplicationSubMenu = applicationInstance.getSubMenu();
      for (EurekaApplication eurekaApplication : eurekaApplicationList) {
        MenuItem eurekaApplicationMenuItem = eurekaApplicationSubMenu
            .addItem(eurekaApplication.getName());
        //eurekaApplicationMenuItem.setCheckable(true);
        //eurekaApplicationMenuItem.addClickListener(event -> setMenuItemChecked( eurekaApplicationSubMenu, eurekaApplicationMenuItem ));
        SubMenu eurekaApplicationInstanceSubMenu = eurekaApplicationMenuItem.getSubMenu();
        for (EurekaApplicationInstance eurekaApplicationInstance : eurekaApplication
            .getInstances()) {
          MenuItem subMenu = eurekaApplicationInstanceSubMenu
              .addItem(eurekaApplicationInstance.getInstanceId());
          subMenu
              .addClickListener(menuItemClickEvent -> {
                setMenuItemChecked(eurekaApplicationInstanceSubMenu, subMenu);
                currentEurekaApplication = eurekaApplication;
                currentEurekaApplicationInstance = eurekaApplicationInstance;
                UI.getCurrent().getSession()
                    .setAttribute(EurekaApplication.class, eurekaApplication);
                UI.getCurrent().getSession()
                    .setAttribute(EurekaApplicationInstance.class,
                        eurekaApplicationInstance);
                instanceSelectedFunction
                    .accept(eurekaApplication, eurekaApplicationInstance);
              });
        }
      }
    }
    MenuItem refreshNow = menuBar.addItem(getTranslation("action.actuator.refreshNow"));
    refreshNow.addClickListener(menuItemClickEvent -> refresh());
    MenuItem refreshRate = menuBar
        .addItem(getTranslation("action.actuator.refreshRate"));
    SubMenu refreshRateSubMenu = refreshRate.getSubMenu();
    refreshRateSubMenu.addItem(getTranslation("action.actuator.refreshRate.title"))
        .setEnabled(false);

    MenuItem disabledMenuItem = refreshRateSubMenu
        .addItem(getTranslation("action.actuator.refreshRate.disabled"));
    disabledMenuItem.addClickListener(menuItemClickEvent -> {
      setMenuItemChecked(refreshRateSubMenu, disabledMenuItem);
      setRefreshRate(null);
    });
    disabledMenuItem.setCheckable(true);
    disabledMenuItem.setChecked(true);

    MenuItem refreshRate5sMenuItem = refreshRateSubMenu
        .addItem(getTranslation("action.actuator.refreshRate.5s"));
    refreshRate5sMenuItem.addClickListener(menuItemClickEvent -> {
      setMenuItemChecked(refreshRateSubMenu, refreshRate5sMenuItem);
      setRefreshRate(5);
    });
    refreshRate5sMenuItem.setCheckable(true);

    MenuItem refreshRate10sMenuItem = refreshRateSubMenu
        .addItem(getTranslation("action.actuator.refreshRate.10s"));
    refreshRate10sMenuItem
        .addClickListener(menuItemClickEvent -> {
          setMenuItemChecked(refreshRateSubMenu, refreshRate10sMenuItem);
          setRefreshRate(10);
        });
    refreshRate10sMenuItem.setCheckable(true);

    MenuItem refreshRate30sMenuItem = refreshRateSubMenu
        .addItem(getTranslation("action.actuator.refreshRate.30s"));
    refreshRate30sMenuItem
        .addClickListener(menuItemClickEvent -> {
          setMenuItemChecked(refreshRateSubMenu, refreshRate30sMenuItem);
          setRefreshRate(30);
        });
    refreshRate30sMenuItem.setCheckable(true);

    MenuItem refreshRate60sMenuItem = refreshRateSubMenu
        .addItem(getTranslation("action.actuator.refreshRate.60s"));
    refreshRate60sMenuItem
        .addClickListener(menuItemClickEvent -> {
          setMenuItemChecked(refreshRateSubMenu, refreshRate60sMenuItem);
          setRefreshRate(60);
        });
    refreshRate60sMenuItem.setCheckable(true);

    MenuItem refreshRate300sMenuItem = refreshRateSubMenu
        .addItem(getTranslation("action.actuator.refreshRate.300s"));
    refreshRate300sMenuItem
        .addClickListener(menuItemClickEvent -> {
          setMenuItemChecked(refreshRateSubMenu, refreshRate300sMenuItem);
          setRefreshRate(300);
        });
    refreshRate300sMenuItem.setCheckable(true);

    return menuBar;
  }

  protected void setMenuItemChecked(SubMenu parent, MenuItem checked) {
    parent.getItems().stream().filter(menuItem -> !menuItem.equals(checked))
        .forEach(menuItem -> menuItem.setChecked(false));
  }

  private static class RefreshThread extends Thread {

    private final UI ui;
    private final ActuatorBaseView view;

    private int count = 0;

    public RefreshThread(UI ui, ActuatorBaseView view) {
      this.ui = ui;
      this.view = view;
    }

    @Override
    public void run() {
      try {
        // Update the data for a while
        while (count < 10) {
          // Sleep to emulate background work
          Thread.sleep(500);
          String message = "This is update " + count++;

          ui.access(view::refresh);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
