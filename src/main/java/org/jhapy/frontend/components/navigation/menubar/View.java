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
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.server.VaadinService;
import lombok.Data;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.TitleFormatter;
import org.rapidpm.frp.functions.CheckedFunction;

import static org.rapidpm.frp.matcher.Case.match;
import static org.rapidpm.frp.matcher.Case.matchCase;
import static org.rapidpm.frp.model.Result.failure;
import static org.rapidpm.frp.model.Result.success;

@CssImport("./menubar/module-styles.css")
public abstract class View extends FlexBoxLayout implements HasLogger {
  public static final String ERROR_MSG_NO_LOCALE =
      "no locale provided and i18nProvider #getProvidedLocales()# list is empty !! ";
  public static final String ERROR_MSG_NO_ANNOTATION = "no annotation found at class ";
  protected String currentViewParams;
  protected ModuleTab parentTab;
  private ModuleToolbar.GoBackListener goBackListener;
  private ModuleToolbar.GoBackListener menuBackListener;
  private String title = "-- Unknown --";
  private Class<? extends View> navigationRootClass;
  private View parentView;

  public View() {
    addClassName("module-container");
    lookupTitle();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    if (parentTab != null) {
      parentTab.setCaption(getTitle());
      addComponentAsFirst(parentTab.getBreadcrumb());
    }
  }

  public ModuleTab getParentTab() {
    return parentTab;
  }

  public void setParentTab(ModuleTab parentTab) {
    this.parentTab = parentTab;
  }

  public View getParentView() {
    return parentView;
  }

  public void setParentView(View parentView) {
    this.parentView = parentView;
  }

  public ModuleToolbar.GoBackListener getGoBackListener() {
    return goBackListener;
  }

  public void setGoBackListener(ModuleToolbar.GoBackListener listener) {
    goBackListener = listener;
  }

  public ModuleToolbar.GoBackListener getMenuBackListener() {
    return menuBackListener;
  }

  public void setMenuBackListener(ModuleToolbar.GoBackListener listener) {
    menuBackListener = listener;
  }

  public Class<? extends View> getNavigationRootClass() {
    return navigationRootClass;
  }

  public void setNavigationRootClass(Class<? extends View> navigationRootClass) {
    this.navigationRootClass = navigationRootClass;
  }

  public void hideContent() {}

  public void refreshContent() {}

  public String getTitle() {
    return title;
  }

  public void setParameter(BeforeEvent event, String parameter) {
    currentViewParams = parameter;
  }

  public String getParameter() {
    return currentViewParams;
  }

  private void lookupTitle() {
    String loggerPrefix = getLoggerPrefix("getTitle");
    I18NPageTitle annotation = getClass().getAnnotation(I18NPageTitle.class);

    match(
            matchCase(() -> success(annotation.messageKey())),
            matchCase(
                () -> annotation == null,
                () -> failure(ERROR_MSG_NO_ANNOTATION + getClass().getSimpleName())),
            matchCase(
                () -> annotation.messageKey().isEmpty(), () -> success(annotation.defaultValue())))
        .ifPresentOrElse(
            msgKey -> {
              var i18NProvider = VaadinService.getCurrent().getInstantiator().getI18NProvider();
              var locale = getLocale();
              var providedLocales = i18NProvider.getProvidedLocales();
              match(
                      matchCase(() -> success(providedLocales.get(0))),
                      matchCase(
                          () -> locale == null && providedLocales.isEmpty(),
                          () -> failure(ERROR_MSG_NO_LOCALE + i18NProvider.getClass().getName())),
                      matchCase(() -> locale == null, () -> success(providedLocales.get(0))),
                      matchCase(() -> providedLocales.contains(locale), () -> success(locale)))
                  .ifPresentOrElse(
                      finalLocale ->
                          ((CheckedFunction<Class<? extends TitleFormatter>, TitleFormatter>)
                                  f -> f.getDeclaredConstructor().newInstance())
                              .apply(annotation.formatter())
                              .ifPresentOrElse(
                                  formatter ->
                                      formatter
                                          .apply(i18NProvider, finalLocale, msgKey)
                                          .ifPresentOrElse(
                                              title -> this.title = title,
                                              failed -> logger().info(failed)),
                                  failed -> info(loggerPrefix, failed)),
                      failed -> info(loggerPrefix, failed));
            },
            failed -> info(loggerPrefix, failed));
  }

  @Data
  public static class ViewParent {
    private Class<? extends View> parentClass;
    private String parentParameters;
  }
}