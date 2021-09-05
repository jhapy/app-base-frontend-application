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

package org.jhapy.frontend.utils.i18n;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.server.*;
import org.jhapy.commons.utils.HasLogger;
import org.rapidpm.frp.functions.CheckedFunction;

import static org.rapidpm.frp.matcher.Case.match;
import static org.rapidpm.frp.matcher.Case.matchCase;
import static org.rapidpm.frp.model.Result.failure;
import static org.rapidpm.frp.model.Result.success;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-18
 */
public class I18NPageTitleEngine
    implements VaadinServiceInitListener, UIInitListener, BeforeEnterListener, HasLogger {

  public static final String ERROR_MSG_NO_LOCALE =
      "no locale provided and i18nProvider #getProvidedLocales()# list is empty !! ";
  public static final String ERROR_MSG_NO_ANNOTATION = "no annotation found at class ";

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    Class<?> navigationTarget = event.getNavigationTarget();
    I18NPageTitle annotation = navigationTarget.getAnnotation(I18NPageTitle.class);

    match(
            matchCase(() -> success(annotation.messageKey())),
            matchCase(
                () -> annotation == null,
                () -> failure(ERROR_MSG_NO_ANNOTATION + navigationTarget.getName())),
            matchCase(
                () -> annotation.messageKey().isEmpty(), () -> success(annotation.defaultValue())))
        .ifPresentOrElse(
            msgKey -> {
              var i18NProvider = VaadinService.getCurrent().getInstantiator().getI18NProvider();
              var locale = event.getUI().getLocale();
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
                                              title -> UI.getCurrent().getPage().setTitle(title),
                                              failed -> logger().info(failed)),
                                  failed -> logger().info(failed)),
                      failed -> logger().info(failed));
            },
            failed -> logger().info(failed));
  }

  @Override
  public void uiInit(UIInitEvent event) {
    var ui = event.getUI();
    ui.addBeforeEnterListener(this);
    // addListener(ui, PermissionsChangedEvent.class, e -> ui.getPage().reload());
  }

  @Override
  public void serviceInit(ServiceInitEvent event) {
    event.getSource().addUIInitListener(this);
  }
}