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

import com.flowingcode.vaadin.addons.errorwindow.ErrorManager;
import com.hazelcast.core.HazelcastInstance;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.*;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.lumo.Lumo;
import de.codecamp.vaadin.components.messagedialog.MessageDialog;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.commons.utils.HasLoggerStatic;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.messageQueue.NewSession;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.utils.AppContext;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.client.audit.AuditServices;
import org.jhapy.frontend.components.AppCookieConsent;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.menubar.*;
import org.jhapy.frontend.components.search.overlay.SearchOverlayButton;
import org.jhapy.frontend.config.AppProperties;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.utils.*;
import org.jhapy.frontend.utils.css.Overflow;
import org.jhapy.frontend.utils.css.Shadow;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.admin.MonitoringAdminView;
import org.jhapy.frontend.views.admin.audit.SessionView;
import org.jhapy.frontend.views.admin.i18n.ActionsView;
import org.jhapy.frontend.views.admin.i18n.ElementsView;
import org.jhapy.frontend.views.admin.i18n.MessagesView;
import org.jhapy.frontend.views.admin.messaging.MailAdminView;
import org.jhapy.frontend.views.admin.messaging.MailTemplatesAdminView;
import org.jhapy.frontend.views.admin.messaging.SmsAdminView;
import org.jhapy.frontend.views.admin.messaging.SmsTemplatesAdminView;
import org.jhapy.frontend.views.admin.references.CountriesView;
import org.jhapy.frontend.views.admin.security.ClientsView;
import org.jhapy.frontend.views.admin.security.SecurityKeycloakGroupsView;
import org.jhapy.frontend.views.admin.security.SecurityKeycloakRolesView;
import org.jhapy.frontend.views.admin.security.SecurityKeycloakUsersView;
import org.springframework.core.env.Environment;

import javax.servlet.http.Cookie;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

import static org.jhapy.frontend.utils.AppConst.*;
import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

@NpmPackage(value = "lumo-css-framework", version = "^4.0.10")
@NpmPackage(value = "line-awesome", version = "1.3.0")
@CssImport(
    value = "./styles/components/charts.css",
    themeFor = "vaadin-chart",
    include = "vaadin-chart-default-theme")
@CssImport(value = "./styles/components/floating-action-button.css", themeFor = "vaadin-button")
@CssImport(value = "./styles/components/grid.css", themeFor = "vaadin-grid")
@CssImport("./styles/lumo/border-radius.css")
@CssImport("./styles/lumo/icon-size.css")
@CssImport("./styles/lumo/margin.css")
@CssImport("./styles/lumo/padding.css")
@CssImport("./styles/lumo/shadow.css")
@CssImport("./styles/lumo/spacing.css")
@CssImport("./styles/lumo/typography.css")
@CssImport("./styles/misc/box-shadow-borders.css")
@CssImport(value = "./styles/styles.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge")
@CssImport("./menubar/main-ui-styles.css")
@CssImport("./menubar/window-tab-styles.css")
@CssImport("./menubar/menu-styles.css")
public abstract class JHapyMainView3 extends FlexBoxLayout implements HasLogger, RouterLayout {

  private static final String CLASS_NAME = "root";
  private final ConfirmDialog confirmDialog;
  private final HazelcastInstance hazelcastInstance;
  private final List<AttributeContextListener> contextListeners = new ArrayList<>();
  private final MyI18NProvider myI18NProvider;
  private final Div header = new Div();
  private final Tabs tabs = new Tabs();
  private final ModuleTabAdd newTabButton = new ModuleTabAdd();
  private final List<ModuleTab> tabList = new ArrayList<>();
  private final List<View> views = new ArrayList<>();
  protected FlexBoxLayout viewContainer;
  protected AppProperties appProperties;
  private Div appHeaderOuter;
  private FlexBoxLayout content;
  private Div appHeaderInner;
  private Div appFooterInner;
  private Environment environment;
  private Div appFooterOuter;
  private List<Menu> menuList;
  private List<TopMenuItem> topMenuItemList;

  protected JHapyMainView3(
      MyI18NProvider myI18NProvider,
      HazelcastInstance hazelcastInstance,
      Environment environment,
      AppProperties appProperties,
      boolean hasGlobalSearch,
      boolean hasGlobalNotification) {
    String loggerPrefix = getLoggerPrefix("JHapyMainView3");
    this.hazelcastInstance = hazelcastInstance;
    this.appProperties = appProperties;
    this.myI18NProvider = myI18NProvider;
    UI.getCurrent()
        .getElement()
        .executeJs("return window.matchMedia('(prefers-color-scheme: dark)').matches")
        .then(
            jsonValue -> {
              logger().debug(jsonValue.asBoolean());
              var themeList = UI.getCurrent().getElement().getThemeList();
              if (!jsonValue.asBoolean()) {
                themeList.remove(Lumo.DARK);
                VaadinSession.getCurrent().setAttribute(THEME_ATTRIBUTE, null);
              } else {
                themeList.add(Lumo.DARK);
                VaadinSession.getCurrent().setAttribute(THEME_ATTRIBUTE, Lumo.DARK);
              }
            });

    afterLogin();

    this.confirmDialog = new ConfirmDialog();
    confirmDialog.setCancelable(true);
    confirmDialog.setConfirmButtonTheme("raised tertiary error");
    confirmDialog.setCancelButtonTheme("raised tertiary");

    getElement().appendChild(confirmDialog.getElement());

    addClassName(CLASS_NAME);
    // setBackgroundColor(LumoStyles.Color.Contrast._5);
    setFlexDirection(FlexDirection.COLUMN);
    setSizeFull();

    // Initialise the UI building blocks
    initStructure();

    if (Session.getAppMenu() == null) {
      AppMenu appMenu = new AppMenu();
      Session.setAppMenu(appMenu);
    }

    // Populate the navigation drawer
    initNaviItems();

    setMenuList(Session.getAppMenu().getMenuList());

    // Configure the headers and footers (optional)
    initHeadersAndFooters(hasGlobalSearch, hasGlobalNotification);

    initTabs();
    getElement().appendChild(new AppCookieConsent().getElement());

    /*UI.getCurrent().addAfterNavigationListener(event -> {
      debug(loggerPrefix, "Navigator event {0}", event.getLocation() );
    });*/

    /*
    History history = UI.getCurrent().getPage().getHistory();
    history.setHistoryStateChangeHandler(
        event -> {
          debug(loggerPrefix, "History changed {0}", event.getLocation());
        });
    getUI()
        .ifPresent(
            ui ->
                ui.addBeforeLeaveListener(
                    event -> {
                      debug(loggerPrefix, "Before Leave event {0}", event.getLocation());
                    }));

    UnloadObserver unloadObserver = UnloadObserver.get();
    unloadObserver.setQueryingOnUnload(false);
    unloadObserver.addUnloadListener(
        event -> {
          debug(loggerPrefix, "UnloadEvent event {0}", event.isBecauseOfQuerying());
          debug(loggerPrefix, "Current Tab : " + tabs.getSelectedTab().getLabel());
        });
    getElement().appendChild(unloadObserver.getElement());

     */
  }

  public static JHapyMainView3 get() {
    return (JHapyMainView3)
        UI.getCurrent()
            .getChildren()
            .filter(component -> JHapyMainView3.class.isAssignableFrom(component.getClass()))
            .findFirst()
            .orElse(null);
  }

  public static void displayErrorMessageStatic(ServiceResult errorResult) {
    var loggerPrefix = HasLoggerStatic.getLoggerPrefix("displayErrorMessageStatic");
    JHapyMainView3 current = get();
    if (current != null) {
      current.displayErrorMessage(errorResult);
    } else {
      HasLoggerStatic.error(
          JHapyMainView3.class,
          loggerPrefix,
          "MainView is null, cannot display message : " + errorResult.getMessage());
    }
  }

  @Override
  public void showRouterLayoutContent(HasElement content) {
    String loggerPrefix = getLoggerPrefix("showRouterLayoutContent");
    debug(loggerPrefix, "Show content ...");
    displayViewFromNavigation((View) content);
  }

  public void addAttributeContextListener(AttributeContextListener contextListener) {
    contextListeners.add(contextListener);
  }

  public void removeAttributeContextListener(AttributeContextListener contextListener) {
    contextListeners.remove(contextListener);
  }

  public void fireAttributeContextChanged(String attributeName, Object attributeValue) {
    contextListeners.parallelStream()
        .forEach(
            contextListener ->
                contextListener.onAttributeContextChanged(attributeName, attributeValue));
  }

  protected Component getAltSearchMenu() {
    return null;
  }

  protected ConcurrentMap<String, SessionInfo> retrieveMap() {
    return hazelcastInstance.getMap("userSessions");
  }

  public SearchOverlayButton<? extends SearchQueryResult, ? extends SearchQuery> getSearchButton() {
    return null;
  }

  public Class getHomePage() {
    return null;
  }

  public View getHomePage2() {
    return null;
  }

  public Class getUserSettingsView() {
    return null;
  }

  public boolean hasLanguageSelect() {
    return true;
  }

  public Locale getDefaultLocale() {
    return Locale.ENGLISH;
  }

  public StoredFile getLoggedUserAvatar(SecurityUser securityUser) {
    return null;
  }

  protected String getCurrentUser() {
    return org.jhapy.commons.security.SecurityUtils.getCurrentUserLogin().get();
  }

  public void afterLogin() {
    var loggerPrefix = getLoggerPrefix("afterLogin");
    if (VaadinSession.getCurrent() == null) {
      return;
    }

    if (!hasLanguageSelect()) {
      UI.getCurrent().getSession().setLocale(getDefaultLocale());
    }

    var currentSecurityUser =
        (SecurityUser) VaadinSession.getCurrent().getAttribute(SECURITY_USER_ATTRIBUTE);
    if (currentSecurityUser == null) {
      currentSecurityUser = SecurityUtils.getSecurityUser();
      if (currentSecurityUser != null) {
        VaadinSession currentSession = VaadinSession.getCurrent();
        VaadinRequest currentRequest = VaadinRequest.getCurrent();

        // 5 minutes
        logger()
            .info(
                loggerPrefix
                    + "Max Inactive Interval = "
                    + currentSession.getSession().getMaxInactiveInterval());
        // currentSession.getSession().setMaxInactiveInterval( 2 * 60);

        logger()
            .info(
                loggerPrefix
                    + "Create remote session, Session ID = "
                    + currentSession.getSession().getId());
        AuditServices.getAuditServiceQueue()
            .newSession(
                new NewSession(
                    currentSession.getSession().getId(),
                    currentSecurityUser.getUsername(),
                    currentRequest.getRemoteAddr(),
                    Instant.now(),
                    true,
                    null));

        var sessionInfo = new SessionInfo();
        sessionInfo.setJSessionId(currentSession.getSession().getId());
        sessionInfo.setLoginDateTime(LocalDateTime.now());
        sessionInfo.setLastContact(LocalDateTime.now());
        sessionInfo.setSourceIp(currentRequest.getRemoteAddr());
        sessionInfo.setUsername(currentSecurityUser.getUsername());
        retrieveMap().put(sessionInfo.getJSessionId(), sessionInfo);
      }
    }
  }

  /** Initialise the required components and containers. */
  private void initStructure() {

    viewContainer = new FlexBoxLayout();
    viewContainer.addClassName(CLASS_NAME + "__view-container");
    viewContainer.setOverflow(Overflow.HIDDEN);

    content = new FlexBoxLayout(viewContainer);
    content.addClassName(CLASS_NAME + "__column");
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setFlexGrow(1, viewContainer);
    content.setOverflow(Overflow.HIDDEN);
    add(content);
    setFlexGrow(1, content);
  }

  protected int addToMainMenu(List<Menu> menuList, int currentMenuIdx) {
    return currentMenuIdx;
  }

  protected int addToSettingsMenu(List<Menu> menuList, int currentMenuIdx, Menu settingsMenu) {
    return currentMenuIdx;
  }

  protected boolean hasSettingsMenuEntries() {
    return false;
  }

  protected int addToReferencesMenu(List<Menu> menuList, int currentMenuIdx, Menu referenceMenu) {
    return currentMenuIdx;
  }

  protected boolean hasReferencesMenuEntries() {
    return false;
  }

  protected int addToSecurityMenu(List<Menu> menuList, int currentMenuIdx, Menu securityMenu) {
    return currentMenuIdx;
  }

  protected boolean hasSecurityMenuEntries() {
    return false;
  }

  /** Initialise the navigation items. */
  private int initNaviItems() {
    var currentUI = UI.getCurrent();

    var menuList = new ArrayList<Menu>();
    var rootMenuIdx = 1;
    rootMenuIdx = addToMainMenu(menuList, rootMenuIdx);
    Session.getAppMenu().initMenuList();

    if (SecurityUtils.isUserLoggedIn()) {

      boolean isSettingsDisplayed =
          hasSettingsMenuEntries()
              || SecurityUtils.isAccessGranted(ActionsView.class)
              || SecurityUtils.isAccessGranted(ElementsView.class)
              || SecurityUtils.isAccessGranted(MessagesView.class)
              || SecurityUtils.isAccessGranted(CountriesView.class)
              || SecurityUtils.isAccessGranted(SecurityKeycloakUsersView.class)
              || SecurityUtils.isAccessGranted(SecurityKeycloakRolesView.class)
              || SecurityUtils.isAccessGranted(SecurityKeycloakGroupsView.class);

      if (isSettingsDisplayed) {
        var settingsMenuIdx = rootMenuIdx++;
        var settingMenu =
            new Menu(
                settingsMenuIdx,
                0,
                currentUI.getTranslation(AppConst.TITLE_SETTINGS),
                VaadinIcon.EDIT.name(),
                null,
                null);
        menuList.add(settingMenu);

        rootMenuIdx = addToSettingsMenu(menuList, rootMenuIdx, settingMenu);

        boolean isDisplayI18n =
            SecurityUtils.isAccessGranted(ActionsView.class)
                || SecurityUtils.isAccessGranted(ElementsView.class)
                || SecurityUtils.isAccessGranted(MessagesView.class);

        if (isDisplayI18n) {
          var settingsi18NMenuIdx = rootMenuIdx++;

          menuList.add(
              new Menu(
                  settingsi18NMenuIdx,
                  settingsMenuIdx,
                  currentUI.getTranslation(AppConst.TITLE_I18N),
                  VaadinIcon.SITEMAP.name(),
                  null,
                  null));

          if (SecurityUtils.isAccessGranted(ActionsView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    settingsi18NMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_ACTIONS),
                    VaadinIcon.QUESTION.name(),
                    ActionsView.class,
                    null));
          }

          if (SecurityUtils.isAccessGranted(ElementsView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    settingsi18NMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_ELEMENTS),
                    VaadinIcon.QUESTION.name(),
                    ElementsView.class,
                    null));
          }

          if (SecurityUtils.isAccessGranted(MessagesView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    settingsi18NMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_MESSAGES),
                    VaadinIcon.QUESTION.name(),
                    MessagesView.class,
                    null));
          }
        }

        /*
         * Reference
         */
        boolean isReferenceMenuDisplay =
            hasReferencesMenuEntries() || SecurityUtils.isAccessGranted(CountriesView.class);

        if (isReferenceMenuDisplay) {
          var referenceMenuIdx = rootMenuIdx++;

          var referenceMenu =
              new Menu(
                  referenceMenuIdx,
                  settingsMenuIdx,
                  currentUI.getTranslation(AppConst.TITLE_REFERENCES),
                  VaadinIcon.SITEMAP.name(),
                  null,
                  null);
          menuList.add(referenceMenu);

          if (hasReferencesMenuEntries()) {
            rootMenuIdx = addToReferencesMenu(menuList, rootMenuIdx, referenceMenu);
          }
        }

        /*
         * Notification
         */
        boolean isDisplayNotifications =
            SecurityUtils.isAccessGranted(MailTemplatesAdminView.class)
                || SecurityUtils.isAccessGranted(SmsTemplatesAdminView.class)
                || SecurityUtils.isAccessGranted(SmsAdminView.class)
                || SecurityUtils.isAccessGranted(MailAdminView.class);

        if (isDisplayNotifications) {
          var notificationMenuIdx = rootMenuIdx++;

          menuList.add(
              new Menu(
                  notificationMenuIdx,
                  settingsMenuIdx,
                  currentUI.getTranslation(AppConst.TITLE_NOTIFICATION_ADMIN),
                  VaadinIcon.SITEMAP.name(),
                  null,
                  null));

          if (SecurityUtils.isAccessGranted(MailTemplatesAdminView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    notificationMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_MAIL_TEMPLATES_ADMIN),
                    VaadinIcon.QUESTION.name(),
                    MailTemplatesAdminView.class,
                    null));
          }

          if (SecurityUtils.isAccessGranted(SmsTemplatesAdminView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    notificationMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_SMS_TEMPLATES_ADMIN),
                    VaadinIcon.QUESTION.name(),
                    SmsTemplatesAdminView.class,
                    null));
          }

          if (SecurityUtils.isAccessGranted(SmsAdminView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    notificationMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_SMS),
                    VaadinIcon.QUESTION.name(),
                    SmsAdminView.class,
                    null));
          }

          if (SecurityUtils.isAccessGranted(MailAdminView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    notificationMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_MAILS),
                    VaadinIcon.QUESTION.name(),
                    MailAdminView.class,
                    null));
          }
        }
        /*
         * Security
         */
        boolean isDisplaySecurity =
            hasSecurityMenuEntries()
                || SecurityUtils.isAccessGranted(SecurityKeycloakUsersView.class)
                || SecurityUtils.isAccessGranted(SecurityKeycloakRolesView.class)
                || SecurityUtils.isAccessGranted(SecurityKeycloakGroupsView.class);

        if (isDisplaySecurity) {
          var securityMenuIdx = rootMenuIdx++;
          var securityMenu =
              new Menu(
                  securityMenuIdx,
                  settingsMenuIdx,
                  currentUI.getTranslation(AppConst.TITLE_SECURITY),
                  VaadinIcon.KEY.name(),
                  null,
                  null);
          menuList.add(securityMenu);

          if (SecurityUtils.isAccessGranted(SecurityKeycloakUsersView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    securityMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_SECURITY_USERS),
                    VaadinIcon.QUESTION.name(),
                    SecurityKeycloakUsersView.class,
                    null));
          }

          if (SecurityUtils.isAccessGranted(SecurityKeycloakRolesView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    securityMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_SECURITY_ROLES),
                    VaadinIcon.QUESTION.name(),
                    SecurityKeycloakRolesView.class,
                    null));
          }

          if (SecurityUtils.isAccessGranted(SecurityKeycloakGroupsView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    securityMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_SECURITY_GROUPS),
                    VaadinIcon.QUESTION.name(),
                    SecurityKeycloakGroupsView.class,
                    null));
          }

          if (SecurityUtils.isAccessGranted(ClientsView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    securityMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_CLIENTS),
                    VaadinIcon.QUESTION.name(),
                    ClientsView.class,
                    null));
          }

          if (SecurityUtils.isAccessGranted(SessionView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    securityMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_SESSIONS_ADMIN),
                    VaadinIcon.QUESTION.name(),
                    SessionView.class,
                    null));
          }
          if (SecurityUtils.isAccessGranted(MonitoringAdminView.class)) {
            menuList.add(
                new Menu(
                    rootMenuIdx++,
                    securityMenuIdx,
                    currentUI.getTranslation(AppConst.TITLE_ACTUAL_SESSIONS_ADMIN),
                    VaadinIcon.QUESTION.name(),
                    MonitoringAdminView.class,
                    null));
          }
          addToSecurityMenu(menuList, rootMenuIdx, securityMenu);
        }
      }
    }
    Session.getAppMenu().setMenuList(menuList);

    return rootMenuIdx;
  }

  /** Configure the app's inner and outer headers and footers.micrometer-core.version */
  protected void initHeadersAndFooters(boolean hasGlobalSearch, boolean hasGlobalNotification) {
    // setAppHeaderOuter();
    // setAppFooterOuter();

    // setAppFooterInner();

    // UIUtils.setTheme(Lumo.DARK, appBar);
    setAppHeaderInner(initHeader());
  }

  private Component initHeader() {
    MenuBar menuBar = new MenuBar();
    menuBar.getStyle().set("padding-bottom", "2px");
    menuBar.setOpenOnHover(true);
    menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

    Image icon = new Image(UIUtils.IMG_PATH + "logo-32x32.png", "");
    icon.addClassName("header-icon");

    Span appTitle = new Span(getTranslation("element.application.title"));
    appTitle.addClassName("header-text");

    MenuItem mainMenu = menuBar.addItem(appTitle);
    mainMenu.addComponentAsFirst(icon);
    mainMenu.addComponentAsFirst(new Icon(VaadinIcon.MENU));

    buildMenu(0, mainMenu);

    mainMenu.getSubMenu().add(new Hr());

    var languageButton =
        UIUtils.createButton(
            getTranslation("action.settings.language"),
            VaadinIcon.GLOBE,
            ButtonVariant.LUMO_TERTIARY_INLINE);
    var currentLocale = UI.getCurrent().getSession().getLocale();
    var languageMenu = mainMenu.getSubMenu().addItem(languageButton);

    List<MenuItem> menuItems = new ArrayList<>();
    MyI18NProvider.getAvailableLanguagesInDB(getLocale())
        .forEach(
            locale -> {
              MenuItem menu =
                  languageMenu
                      .getSubMenu()
                      .addItem(
                          UIUtils.createLabel(
                              TextColor.PRIMARY, locale.getDisplayLanguage(getLocale())));
              menu.setCheckable(true);
              menu.setChecked(currentLocale.getLanguage().equals(locale.getLanguage()));
              menu.addClickListener(
                  event -> {
                    setLanguage(locale);
                    menuItems.forEach(
                        menuItem -> {
                          if (!menuItem.equals(menu)) {
                            menuItem.setChecked(false);
                          }
                        });
                  });
              menuItems.add(menu);
            });

    var switchDarkThemeButton =
        UIUtils.createButton(
            getTranslation("action.global.darkTheme"),
            VaadinIcon.CIRCLE_THIN,
            ButtonVariant.LUMO_TERTIARY_INLINE);
    if (VaadinSession.getCurrent().getAttribute(THEME_ATTRIBUTE) != null) {
      switchDarkThemeButton.setIcon(VaadinIcon.CHECK_CIRCLE_O.create());
    }

    mainMenu
        .getSubMenu()
        .addItem(
            switchDarkThemeButton,
            event -> {
              var themeList = UI.getCurrent().getElement().getThemeList();
              if (VaadinSession.getCurrent().getAttribute(THEME_ATTRIBUTE) != null) {
                themeList.remove(Lumo.DARK);
                switchDarkThemeButton.setIcon(VaadinIcon.CIRCLE_THIN.create());
                VaadinSession.getCurrent().setAttribute(THEME_ATTRIBUTE, null);
              } else {
                themeList.add(Lumo.DARK);
                switchDarkThemeButton.setIcon(VaadinIcon.CHECK_CIRCLE_O.create());
                VaadinSession.getCurrent().setAttribute(THEME_ATTRIBUTE, Lumo.DARK);
              }
            });

    mainMenu.getSubMenu().add(new Hr());
    if (SecurityUtils.isUserLoggedIn()) {
      Avatar ownAvatar = new Avatar();
      ownAvatar.setName((String) VaadinSession.getCurrent().getAttribute(NICKNAME_ATTRIBUTE));
      String userId = (String) VaadinSession.getCurrent().getAttribute(USER_ID_ATTRIBUTE);
      StoredFile storedFile =
          ((StoredFile) VaadinSession.getCurrent().getAttribute(AVATAR_ATTRIBUTE));
      if (storedFile != null) {
        StreamResource streamResource =
            new StreamResource(
                "avatar_" + userId, () -> new ByteArrayInputStream(storedFile.getContent()));
        streamResource.setContentType(storedFile.getMimeType());
        ownAvatar.setImageResource(streamResource);
      } else {
        ownAvatar.setImage(UIUtils.IMG_PATH + "icons8-question-mark-64.png");
      }
      HorizontalLayout hLayout = new HorizontalLayout();
      hLayout.setWidthFull();
      hLayout.setDefaultVerticalComponentAlignment(Alignment.START);
      hLayout.add(
          ownAvatar,
          UIUtils.createLabel(TextColor.PRIMARY, AppContext.getInstance().getCurrentUsername()));
      hLayout.setAlignItems(Alignment.CENTER);
      mainMenu
          .getSubMenu()
          .addItem(
              hLayout,
              event -> {
                if (JHapyMainView3.get().getUserSettingsView() != null) {
                  getUI().ifPresent(ui -> ui.navigate(JHapyMainView3.get().getUserSettingsView()));
                }
              });
      var logoutButton =
          UIUtils.createButton(
              getTranslation("action.global.logout"),
              VaadinIcon.EXIT,
              ButtonVariant.LUMO_TERTIARY_INLINE);
      var anchor = new Anchor("/logout", logoutButton);
      anchor.setTarget(AnchorTarget.TOP);
      mainMenu
          .getSubMenu()
          .addItem(
              anchor,
              event -> {
                UI.getCurrent()
                    .access(
                        () -> getUI().get().getPushConfiguration().setPushMode(PushMode.DISABLED));
              });
    } else {
      var loginButton =
          UIUtils.createButton(
              getTranslation("action.global.login"),
              VaadinIcon.USER,
              ButtonVariant.LUMO_TERTIARY_INLINE);
      var anchor =
          new Anchor(
              appProperties.getAuthorization().getLoginRootUrl()
                  + DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
                  + "/"
                  + appProperties.getSecurity().getRealm(),
              loginButton);
      anchor.setTarget(AnchorTarget.TOP);
      mainMenu.getSubMenu().addItem(anchor);
    }

    header.removeAll();
    header.addClassName("header");
    header.add(menuBar);

    UIUtils.setTheme(Lumo.DARK, header);
    return header;
  }

  private void setLanguage(Locale language) {
    UI.getCurrent().getSession().setLocale(language);
    var languageCookie = new Cookie("PreferredLanguage", language.getLanguage());
    languageCookie.setMaxAge(31449600);
    languageCookie.setPath("/");
    languageCookie.setSecure(true);
    VaadinService.getCurrentResponse().addCookie(languageCookie);

    UI.getCurrent().getPage().reload();
  }

  private void buildMenu(long parentMenuIdx, MenuItem parentMenu) {
    Session.getAppMenu().getMenuList().stream()
        .filter(menu -> menu.getParentId() == parentMenuIdx)
        .forEach(
            menu -> {
              var menuButtonItem =
                  UIUtils.createButton(
                      menu.getMenuName(),
                      VaadinIcon.valueOf(menu.getIconName()),
                      ButtonVariant.LUMO_TERTIARY_INLINE);

              MenuItem menuItem = parentMenu.getSubMenu().addItem(menuButtonItem);

              if (menu.getBrowser() != null)
                menuItem.addClickListener(
                    event -> {
                      ModuleTab lastTab = null;
                      for (ModuleTab moduleTab : tabList) {
                        if (moduleTab.isLastTab()) {
                          lastTab = moduleTab;
                          break;
                        }
                      }
                      ViewMenu viewMenu =
                          new ViewMenu(getMenuList(), 0, getTopMenuItemList(), lastTab);
                      views.add(viewMenu);

                      displayViewFromMenu(
                          menu.getBrowser(),
                          menu.getMenuName(),
                          menu.getParentId(),
                          viewMenu,
                          lastTab,
                              menu.getNewViewParams());
                    });
              buildMenu(menu.getId(), menuItem);
            });
  }

  private void initTabs() {
    ModuleTab newTab = new ModuleTab(getTranslation("element.dashboard.home"), true);
    newTab.addCloseButtonClickListener(() -> closeTab(newTab));
    tabList.add(newTab);
    tabs.setHeightFull();
    tabs.add(newTab);
    tabs.add(newTabButton);

    ViewMenu viewMenu = new ViewMenu(getMenuList(), 0, getTopMenuItemList(), newTab);
    viewMenu.setSizeFull();
    viewMenu.addModuleSelectedListener(
        (className, menuName, menuParentId, currentViewMenu, newViewParams) ->
            displayViewFromMenu(className, menuName, menuParentId, viewMenu, newTab, newViewParams));
    viewMenu.addCaptionChangedListener(newTab::setCaption);
    views.add(viewMenu);

    viewContainer.getChildren().forEach(e -> e.setVisible(false));
    viewContainer.add(viewMenu);

    tabs.addClassName("tab-container");

    tabs.addSelectedChangeListener(
        listener -> {
          if (views.size() < 1) return;
          if (listener.getSource().getSelectedIndex() < 0) return;
          if (listener.getSource().getSelectedIndex() >= views.size()) return;

          View view = views.get(listener.getSource().getSelectedIndex());

          viewContainer.getChildren().forEach(e -> e.setVisible(false));

          if (views.contains(view)) {
            view.setVisible(true);

          } else {
            view.add(view);
            viewContainer.add(view);
          }
        });

    newTabButton.addClickListener(listener -> addNewTab());
    header.add(tabs);
  }

  private void closeTab(ModuleTab tab) {

    View browser = views.get(tabs.indexOf(tab));
    viewContainer.remove(browser);
    views.remove(browser);

    tabs.remove(newTabButton);
    tabs.remove(tab);
    tabList.remove(tab);

    if (tabList.size() < 1) {
      addNewTab();
    } else {
      tabs.setSelectedTab(tabList.get(tabList.size() - 1));

      View view = views.get(tabList.size() - 1);

      viewContainer.getChildren().forEach(e -> e.setVisible(false));

      if (views.contains(view)) {
        view.setVisible(true);

      } else {
        view.add(view);
        viewContainer.add(view);
      }
    }

    tabs.add(newTabButton);
  }

  private ModuleTab addNewTab() {
    return addNewTab(true);
  }
  private ModuleTab addNewTab(boolean isDisplayMenu ) {

    tabs.remove(newTabButton);

    for (ModuleTab tab : tabList) {
      tab.setLastTab(false);
    }

    ModuleTab lastTab = new ModuleTab(getTranslation("element.dashboard.home"), true);
    lastTab.addCloseButtonClickListener(() -> closeTab(lastTab));
    tabList.add(lastTab);
    tabs.add(lastTab);
    tabs.add(newTabButton);
    tabs.setSelectedTab(lastTab);

    if (isDisplayMenu) {
      ViewMenu viewMenu = new ViewMenu(getMenuList(), 0, getTopMenuItemList(), lastTab);
      viewMenu.setSizeFull();
      viewMenu.addModuleSelectedListener(
          (className, menuName, menuParentId, currentViewMenu, newViewParams) ->
              displayViewFromMenu(
                  className, menuName, menuParentId, viewMenu, lastTab, newViewParams));
      viewMenu.addCaptionChangedListener(lastTab::setCaption);
      views.add(viewMenu);

      viewContainer.getChildren().forEach(e -> e.setVisible(false));
        viewContainer.add(viewMenu);
    } else {
      viewContainer.getChildren().forEach(e -> e.setVisible(false));
    }
    return lastTab;
  }

  public boolean displayViewFromNavigation(View view) {
    String loggerPrefix = getLoggerPrefix("displayView");
    try {
      ModuleTab currentTab = (ModuleTab) tabs.getSelectedTab();
      currentTab.getBreadcrumb().push(UIUtils.createLabel(TextColor.PRIMARY, "xxx"));
      UI.getCurrent().getPage().getHistory().pushState(null, "/");
      view.setMenuBackListener(
          () -> {
            viewContainer.remove(view);
            ViewMenu viewMenu = new ViewMenu(getMenuList(), 0, getTopMenuItemList(), currentTab);
            currentTab.getBreadcrumb().pull();
            viewMenu.addModuleSelectedListener(
                (className1, menuName1, menuParentId1, currentViewMenu1, newViewParams1) ->
                    displayViewFromMenu(
                        className1, menuName1, menuParentId1, currentViewMenu1, currentTab, newViewParams1));
            viewMenu.addCaptionChangedListener(currentTab::setCaption);
            viewContainer.getChildren().forEach(e -> e.setVisible(false));
            views.set(views.indexOf(view), viewMenu);
            viewContainer.add(viewMenu);
            UI.getCurrent().getPage().getHistory().pushState(null, "/");
            Menu parentMenu = null;

            for (Menu menu : getMenuList()) {
              if (menu.getId() == 0) {
                parentMenu = menu;
              }
            }

            if (parentMenu != null) currentTab.setCaption(parentMenu.getMenuName());
          });

      var currentMenu = views.get(0);
      viewContainer.remove(currentMenu);
      viewContainer.getChildren().forEach(e -> e.setVisible(false));
      views.set(views.indexOf(currentMenu), view);
      viewContainer.add(view);
      // currentTab.setCaption(menuName);
      return true;
    } catch (Exception ex) {
      String error = ex.toString();
      if (error.contains("java.lang.ClassNotFoundException")) {
        error = "Module not yet available.";
        error(loggerPrefix, ex, "Module not yet available.");
        ErrorManager.showError(ex, error);
      } else {
        error = "Failed to load module " + "menuName" + ": \n" + ex;
        error(loggerPrefix, ex, "Failed to load module {0}", "menuName");
        // Dialogs.notifyError("MenuView", 0, error);
        ErrorManager.showError(ex, error);
      }

      return false;
    }
  }

  public boolean displayViewFromParentView(
          View parentView,
          String parentParams,
          Class<? extends View> newViewClass,
          String newViewParams) {
    return displayViewFromParentView(parentView, parentParams, newViewClass, newViewParams, false );
  }

  public boolean displayViewFromParentView(
      View defaultParentView,
      String defaultParentParams,
      Class<? extends View> newViewClass,
      String newViewParams,
      Boolean newTab) {
    String loggerPrefix = getLoggerPrefix("displayView");

    debug(
        loggerPrefix,
        "New View : {0} with parameter(s) {1}, Parent View : {2} with parameter(s) : {3}",
        newViewClass.getSimpleName(),
        newViewParams == null ? "None" : newViewParams,
            defaultParentView == null ? "None" : defaultParentView.getClass().getSimpleName(),
            defaultParentParams == null ? "None" : defaultParentParams);
    try {
      if (newViewClass == null) {
        Dialogs.notifyError("Module not yet available.");
        return false;
      }
      ModuleTab currentTab;

      View parentView = null;
      String parentParams;
      if ( newTab ) {
        tabs.remove(newTabButton);

        for (ModuleTab tab : tabList) {
          tab.setLastTab(false);
        }

        currentTab = new ModuleTab(getTranslation("element.dashboard.home"), true);
        currentTab.addCloseButtonClickListener(() -> closeTab(currentTab));
        tabList.add(currentTab);
        tabs.add(currentTab);
        tabs.add(newTabButton);
        tabs.setSelectedTab(currentTab);

        parentParams = null;
      } else {
        parentView = defaultParentView;
        parentParams = defaultParentParams;
        currentTab = parentView.getParentTab();
      }

      View view = newViewInstance(newViewClass);
      if ( parentView != null )
        view.setParentTab(parentView.getParentTab());
      else
        view.setParentTab(currentTab);

      if (newViewParams != null) {
        debug(loggerPrefix, "New View has parameter, set them");
        view.setParameter(null, newViewParams);
      }
      if (currentTab != null)
        currentTab.getBreadcrumb().push(UIUtils.createLabel(TextColor.PRIMARY, view.getTitle()));
      String route = null;
      for (RouteData routeData : RouteConfiguration.forApplicationScope().getAvailableRoutes()) {
        if (routeData.getNavigationTarget().equals(newViewClass)) route = routeData.getTemplate();
      }
      if (route != null) {
        if (newViewParams == null) route = route.replace(":___url_parameter", "");
        else route = route.replace(":___url_parameter", newViewParams);
        UI.getCurrent().getPage().getHistory().pushState(null, route);
      } else {
        UI.getCurrent().getPage().getHistory().pushState(null, "/");
      }
if ( parentView != null ) {
  View finalParentView = parentView;
  view.setGoBackListener(
      () -> {
        if (finalParentView.getNavigationRootClass() != null
            && finalParentView.getNavigationRootClass().equals(newViewClass)) {

          long menuParentId = -1;
          for (Menu menu1 : getMenuList()) {
            if (menu1.getBrowser() != null
                && menu1.getBrowser().equals(finalParentView.getNavigationRootClass())) {
              menuParentId = menu1.getParentId();
              break;
            }
          }
          viewContainer.remove(view);
          ViewMenu viewMenu =
              new ViewMenu(getMenuList(), menuParentId, getTopMenuItemList(), currentTab);
          currentTab.getBreadcrumb().pull();
          viewMenu.addModuleSelectedListener(
              (className1, menuName1, menuParentId1, currentViewMenu1, newViewParams1) ->
                  displayViewFromMenu(
                      className1, menuName1, menuParentId1, currentViewMenu1, currentTab, newViewParams1));
          viewMenu.addCaptionChangedListener(currentTab::setCaption);
          viewContainer.getChildren().forEach(e -> e.setVisible(false));
          views.set(views.indexOf(view), viewMenu);
          viewContainer.add(viewMenu);

          Menu parentMenu = null;

          for (Menu menu : getMenuList()) {
            if (menu.getId() == menuParentId) {
              parentMenu = menu;
            }
          }

          if (parentMenu != null) currentTab.setCaption(parentMenu.getMenuName());
        } else {
          if (currentTab.getParents().containsKey(view.getClass())) {
            View.ViewParent viewParent = currentTab.getParents().get(view.getClass());
            currentTab.getBreadcrumb().pull();
            currentTab.getBreadcrumb().pull();
            currentTab.getParents().remove(view.getClass());
            displayViewFromParentView(
                view,
                newViewParams,
                viewParent.getParentClass(),
                viewParent.getParentParameters());
          } else {
            // Seems to be never use...
            displayViewFromParentView(view, newViewParams, finalParentView.getClass(), parentParams);
          }
        }
      });
}
      if (parentView != null && currentTab != null) {
        var parentMap = currentTab.getParents();
        if (!parentMap.containsKey(newViewClass)) {
          View.ViewParent viewParent = new View.ViewParent();
          viewParent.setParentParameters(parentParams);
          viewParent.setParentClass(parentView.getClass());
          debug(
              loggerPrefix,
              "Set parent {0} for {1}",
              parentView.getClass().getSimpleName(),
              newViewClass.getSimpleName());
          currentTab.putParent(newViewClass, viewParent);
        }
      }
      if (parentView != null && parentView.getNavigationRootClass() != null)
        view.setNavigationRootClass(parentView.getNavigationRootClass());
      if (parentView != null && parentView.getMenuBackListener() != null)
        view.setMenuBackListener(parentView.getMenuBackListener());

      if ( parentView != null )
      viewContainer.remove(parentView);

      viewContainer.getChildren().forEach(e -> e.setVisible(false));
      if (parentView != null && views.contains(parentView)) views.set(views.indexOf(parentView), view);
      else
        views.add( view );
      viewContainer.add(view);
      if (currentTab != null )
        currentTab.setCaption(view.getTitle());
      return true;
    } catch (Exception ex) {
      String error = ex.toString();
      if (error.contains("java.lang.ClassNotFoundException")
          || error.contains("java.lang.NoSuchMethodException")) {
        error = "Module not yet available.";
        error(loggerPrefix, ex, "Module not yet available.");
        Dialogs.notifyWarning(error);
        // ErrorManager.showError(ex, error);
      } else {
        error = "Failed to load module " + newViewClass.getSimpleName() + ": \n" + ex;
        error(loggerPrefix, ex, "Failed to load module {0}", newViewClass.getSimpleName());
        Dialogs.notifyError("MenuView", 0, error);
        // ErrorManager.showError(ex, error);
      }

      return false;
    }
  }

  private View newViewInstance(Class<? extends View> newView)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException,
          IllegalAccessException {
    var constructors = newView.getDeclaredConstructors();
    for (Constructor<?> constructor : constructors) {
      if (constructor.getParameterCount() == 1
          && constructor.getParameterTypes()[0].equals(MyI18NProvider.class)) {
        return newView.getDeclaredConstructor(MyI18NProvider.class).newInstance(myI18NProvider);
      }
    }
    for (Constructor<?> constructor : constructors) {
      if (constructor.getParameterCount() == 0) {
        return newView.getDeclaredConstructor().newInstance();
      }
    }
    throw new NoSuchMethodException("No valid constructor found");
  }

  public boolean displayViewFromMenu(
          Class<? extends View> newViewClass,
          String menuName,
          long menuParentId,
          View parentView,
          ModuleTab currentTab) {
    return displayViewFromMenu( newViewClass, menuName, menuParentId, parentView, currentTab, null);
  }

  public boolean displayViewFromMenu(
      Class<? extends View> newViewClass,
      String menuName,
      long menuParentId,
      View parentView,
      ModuleTab currentTab,
      String newViewParams) {
    String loggerPrefix = getLoggerPrefix("displayView");
    try {
      if (newViewClass == null) {
        error(loggerPrefix, "Module is not set (newViewClass)");
        Dialogs.notifyError("Module not yet available.");
        return false;
      }

      View view = newViewInstance(newViewClass);
      view.setParentTab(currentTab);
      view.setNavigationRootClass(newViewClass);
      if (newViewParams != null) {
        debug(loggerPrefix, "New View has parameter, set them");
        view.setParameter(null, newViewParams);
      }

      currentTab.getBreadcrumb().push(UIUtils.createLabel(TextColor.PRIMARY, menuName));
      String route = null;
      for (RouteData routeData : RouteConfiguration.forApplicationScope().getAvailableRoutes()) {
        if (routeData.getNavigationTarget().equals(newViewClass)) route = routeData.getTemplate();
      }
      if (route != null) {
        route = route.replace(":___url_parameter", "");
        UI.getCurrent().getPage().getHistory().pushState(null, route);
      } else {
        UI.getCurrent().getPage().getHistory().pushState(null, "/");
      }
      view.setMenuBackListener(
          () -> {
            viewContainer.remove(view);
            ViewMenu viewMenu =
                new ViewMenu(getMenuList(), menuParentId, getTopMenuItemList(), currentTab);
            currentTab.getBreadcrumb().pull();
            viewMenu.addModuleSelectedListener(
                (className1, menuName1, menuParentId1, currentViewMenu1, newViewParams1) ->
                    displayViewFromMenu(
                        className1, menuName1, menuParentId1, currentViewMenu1, currentTab, newViewParams1));
            viewMenu.addCaptionChangedListener(currentTab::setCaption);
            viewContainer.getChildren().forEach(e -> e.setVisible(false));
            views.set(views.indexOf(view), viewMenu);
            viewContainer.add(viewMenu);
            UI.getCurrent().getPage().getHistory().pushState(null, "/");
            Menu parentMenu = null;

            for (Menu menu : getMenuList()) {
              if (menu.getId() == menuParentId) {
                parentMenu = menu;
                break;
              }
            }

            if (parentMenu != null) currentTab.setCaption(parentMenu.getMenuName());
          });

      viewContainer.remove(parentView);
      viewContainer.getChildren().forEach(e -> e.setVisible(false));
      views.set(views.indexOf(parentView), view);
      viewContainer.add(view);
      currentTab.setCaption(menuName);

      return true;
    } catch (Exception ex) {
      String error = ex.toString();
      if (error.contains("java.lang.ClassNotFoundException")) {
        error = "Module not yet available.";
        error(loggerPrefix, ex, "Module not yet available.");
        ErrorManager.showError(ex, error);
      } else {
        error = "Failed to load module " + menuName + ": \n" + ex;
        error(loggerPrefix, ex, "Failed to load module {0}", menuName);
        // Dialogs.notifyError("MenuView", 0, error);
        ErrorManager.showError(ex, error);
      }

      return false;
    }
  }

  public List<Menu> getMenuList() {
    return menuList;
  }

  public void setMenuList(List<Menu> menuList) {
    this.menuList = menuList;
  }

  public List<TopMenuItem> getTopMenuItemList() {
    return topMenuItemList;
  }

  protected void setAppHeaderOuter(Component... components) {
    if (appHeaderOuter == null) {
      appHeaderOuter = new Div();
      appHeaderOuter.addClassName("app-header-outer");
      getElement().insertChild(0, appHeaderOuter.getElement());
    }
    appHeaderOuter.removeAll();
    appHeaderOuter.add(components);
  }

  protected void setAppHeaderInner(Component... components) {
    if (appHeaderInner == null) {
      appHeaderInner = new Div();
      appHeaderInner.addClassName("app-header-inner");
      content.getElement().insertChild(0, appHeaderInner.getElement());
    }
    appHeaderInner.removeAll();
    appHeaderInner.add(components);
  }

  protected void setAppFooterInner(Component... components) {
    if (appFooterInner == null) {
      appFooterInner = new Div();
      appFooterInner.addClassName("app-footer-inner");
      content
          .getElement()
          .insertChild(content.getElement().getChildCount(), appFooterInner.getElement());
    }
    appFooterInner.removeAll();
    appFooterInner.add(components);
  }

  protected void setAppFooterOuter(Component... components) {
    if (appFooterOuter == null) {
      appFooterOuter = new Div();
      appFooterOuter.addClassName("app-footer-outer");
      getElement().insertChild(getElement().getChildCount(), appFooterOuter.getElement());
    }
    appFooterOuter.removeAll();
    appFooterOuter.add(components);
  }

  // @Override
  public void configurePage(InitialPageSettings settings) {
    settings.addMetaTag("apple-mobile-web-app-capable", "yes");
    settings.addMetaTag("apple-mobile-web-app-status-bar-style", "black");

    settings.addFavIcon("icon", "icons/icon-192x192.png", "192x192");
  }

  public void displayInfoMessage(String title, String message) {
    var icon = UIUtils.createIcon(IconSize.S, TextColor.PRIMARY, VaadinIcon.INFO);
    MessageDialog okDialog = new MessageDialog().setTitle(title, icon).setMessage(message);
    okDialog
        .addButton()
        .text(getTranslation("action.global.close"))
        .primary()
        .closeOnClick()
        .clickShortcutEnter()
        .clickShortcutEscape()
        .closeOnClick();
    okDialog.open();
  }

  public void displayInfoMessage(String message) {
    var icon = UIUtils.createIcon(IconSize.S, TextColor.SUCCESS, VaadinIcon.CHECK);
    var label = UIUtils.createLabel(FontSize.XS, TextColor.BODY, message);

    var messageLayout = new FlexLayout(icon, label);

    // Set the alignment
    messageLayout.setAlignItems(Alignment.CENTER);

    // Add spacing and padding
    messageLayout.addClassNames(LumoStyles.Spacing.Right.S, LumoStyles.Padding.Wide.M);

    var notification = new Notification(messageLayout);
    notification.setDuration(3000);
    notification.setPosition(Position.TOP_CENTER);

    UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, notification);
    UIUtils.setShadow(Shadow.M, notification);

    notification.open();
  }

  public void displayErrorMessage(Throwable t) {
    ErrorManager.showError(t);
  }

  public void displayErrorMessage(String title, String message) {
    var icon = UIUtils.createIcon(IconSize.S, TextColor.ERROR, VaadinIcon.WARNING);
    MessageDialog okDialog = new MessageDialog().setTitle(title, icon).setMessage(message);
    okDialog
        .addButton()
        .text(getTranslation("action.global.close"))
        .primary()
        .closeOnClick()
        .clickShortcutEnter()
        .clickShortcutEscape()
        .closeOnClick();
    okDialog.open();
  }

  protected boolean isProductionMode() {
    return "true".equals(System.getProperty("productionMode"));
  }

  public void displayErrorMessage(ServiceResult errorResult) {
    displayErrorMessage(
        errorResult.getMessageTitle(), errorResult.getMessage(), errorResult.getExceptionString());
  }

  public void displayErrorMessage(String title, String message, String stacktrace) {
    var icon = UIUtils.createIcon(IconSize.S, TextColor.ERROR, VaadinIcon.WARNING);
    MessageDialog okDialog =
        new MessageDialog()
            .setTitle(
                StringUtils.isNotBlank(title) ? title : getTranslation("message.global.error"),
                icon)
            .setMessage(message);
    okDialog
        .addButton()
        .text(getTranslation("action.global.close"))
        .primary()
        .closeOnClick()
        .clickShortcutEnter()
        .clickShortcutEscape()
        .closeOnClick();
    if (!isProductionMode() && StringUtils.isNotBlank(stacktrace)) {
      var detailsText = new TextArea();
      detailsText.setWidthFull();
      detailsText.setMaxHeight("15em");
      detailsText.setReadOnly(true);
      detailsText.setValue(stacktrace);
      okDialog
          .addButtonToLeft()
          .text(getTranslation("action.global.showErrorDetails"))
          .icon(VaadinIcon.ARROW_DOWN)
          .toggleDetails();
      okDialog.getDetails().add(detailsText);
    }
    okDialog.open();
  }

  public void displayErrorMessage(String message) {
    var icon = UIUtils.createIcon(IconSize.S, TextColor.ERROR, VaadinIcon.WARNING);
    MessageDialog okDialog =
        new MessageDialog()
            .setTitle(getTranslation("message.global.error"), icon)
            .setMessage(message);
    okDialog
        .addButton()
        .text(getTranslation("action.global.close"))
        .primary()
        .closeOnClick()
        .clickShortcutEnter()
        .clickShortcutEscape()
        .closeOnClick();

    okDialog.open();
  }

  public void displayWarningMessage(String message) {
    var icon = UIUtils.createIcon(IconSize.S, TextColor.ERROR, VaadinIcon.EXCLAMATION_CIRCLE);
    MessageDialog okDialog =
        new MessageDialog()
            .setTitle(getTranslation("message.global.warning"), icon)
            .setMessage(message);
    okDialog
        .addButton()
        .text(getTranslation("action.global.close"))
        .primary()
        .closeOnClick()
        .clickShortcutEnter()
        .clickShortcutEscape()
        .closeOnClick();

    okDialog.open();
  }

  public abstract void onLogout();

  public void beforeLogin() {}
}