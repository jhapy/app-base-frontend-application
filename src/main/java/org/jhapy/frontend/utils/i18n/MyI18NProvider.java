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
import com.vaadin.flow.i18n.I18NProvider;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.AxonException;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.NoHandlerForQueryException;
import org.axonframework.queryhandling.QueryGateway;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.command.i18n.CreateActionCommand;
import org.jhapy.cqrs.command.i18n.CreateElementCommand;
import org.jhapy.cqrs.command.i18n.CreateMessageCommand;
import org.jhapy.cqrs.query.i18n.*;
import org.jhapy.dto.domain.i18n.*;
import org.jhapy.dto.messageQueue.I18NUpdateTypeEnum;
import org.jhapy.frontend.client.i18n.I18NServices;
import org.jhapy.frontend.utils.AppConst;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-18
 */
@Component
public class MyI18NProvider implements I18NProvider, HasLogger {

  private static Locale[] availableLanguages = null;

  protected final transient QueryGateway queryGateway;
  protected final transient CommandGateway commandGateway;

  private Map<String, ElementTrlDTO> elementMap = new HashMap<>();
  private Map<String, ActionTrlDTO> actionMap = new HashMap<>();
  private Map<String, MessageTrlDTO> messageMap = new HashMap<>();

  private String loadedLocale;

  public MyI18NProvider(QueryGateway queryGateway, CommandGateway commandGateway) {
    this.queryGateway = queryGateway;
    this.commandGateway = commandGateway;
  }

  public static Locale[] getAvailableLanguages() {
    if (availableLanguages == null) {
      Set<String> langs = new HashSet<>();
      for (Locale l : Locale.getAvailableLocales()) {
        if (StringUtils.isNotBlank(l.getLanguage())) {
          langs.add(l.getLanguage());
        }
      }
      Set<Locale> locals = new HashSet<>();
      langs.forEach(lang -> locals.add(new Locale(lang)));

      availableLanguages = locals.toArray(Locale[]::new);
    }

    return availableLanguages;
  }

  public static Locale[] getAvailableLanguages(Locale currentLanguage) {
    Locale[] locales = getAvailableLanguages();
    List<Locale> localeList = Arrays.asList(locales);
    return localeList.stream()
        .sorted(Comparator.comparing(o -> o.getDisplayLanguage(currentLanguage)))
        .toArray(Locale[]::new);
  }

  public List<Locale> getAvailableLanguagesInDB(Locale currentLanguage) {
    GetExistingLanguagesResponse result =
        I18NServices.getI18NService()
            .getExistingLanguages(new GetExistingLanguagesQuery())
            .getData();
    return result.getData().stream()
        .map(Locale::new)
        .sorted(Comparator.comparing(o -> o.getDisplayLanguage(currentLanguage)))
        .toList();
  }

  @Override
  public List<Locale> getProvidedLocales() {

    return List.of(Locale.getAvailableLocales());
  }

  public void reload(String iso3Lang) {
    if (loadedLocale != null && loadedLocale.equalsIgnoreCase(iso3Lang)) {
      reloadElements();
      reloadMessages();
      reloadActions();
    }
  }

  public void reloadElements() {
    elementMap = new HashMap<>();
  }

  public void reloadMessages() {
    messageMap = new HashMap<>();
  }

  public void reloadActions() {
    actionMap = new HashMap<>();
  }

  @Override
  public String getTranslation(String s, Locale locale, Object... objects) {
    var loggerPrefix = getLoggerPrefix("getTranslation", locale, objects);
    String iso3Language = locale.getLanguage();
    if (StringUtils.isBlank(iso3Language)) {
      iso3Language = AppConst.APP_LOCALE.getLanguage();
    }

    loadRemoteLocales(iso3Language);

    if (s.startsWith("element.")) {
      return getElementTranslation(s.substring(s.indexOf('.') + 1), iso3Language, objects);
    } else if (s.startsWith("action.")) {
      return getActionTranslation(s.substring(s.indexOf('.') + 1), iso3Language, objects);
    } else if (s.startsWith("message.")) {
      return getMessageTranslation(s.substring(s.indexOf('.') + 1), iso3Language, objects);
    } else {
      error(loggerPrefix, "Translation do not have a correct prefix : {0}", s);
      return s;
    }
  }

  public String getTooltip(String s) {
    var loggerPrefix = getLoggerPrefix("getTooltip", s);
    String iso3Language = UI.getCurrent().getLocale().getLanguage();
    if (StringUtils.isBlank(iso3Language)) {
      iso3Language = AppConst.APP_LOCALE.getLanguage();
    }

    loadRemoteLocales(iso3Language);

    if (s.startsWith("element.")) {
      String val = getElementTooltip(s.substring(s.indexOf('.') + 1), iso3Language);
      if (StringUtils.isBlank(val)) {
        return s;
      } else {
        return val;
      }
    } else if (s.startsWith("action.")) {
      String val = getActionTooltip(s.substring(s.indexOf('.') + 1), iso3Language);
      if (StringUtils.isBlank(val)) {
        return s;
      } else {
        return val;
      }
    } else {
      error(loggerPrefix, "Tooltip do not have a correct prefix : {0}", s);
      return s;
    }
  }

  protected String getElementTranslation(String s, String iso3Language, Object... objects) {
    var loggerPrefix = getLoggerPrefix("getElementTranslation", s, iso3Language);
    ElementTrlDTO elementTrl;
    try {
      elementTrl = getElementTrl(s, iso3Language);
    } catch (AxonException e) {
      error(loggerPrefix, "Element Translation not found for {0} in {1}", s, iso3Language);
      elementTrl = null;
    }
    if (elementTrl != null) {
      if (objects.length > 0) {
        return String.format(
            Objects.requireNonNull(elementTrl.getValue(), "Translation value cnn"), objects);
      } else {
        return elementTrl.getValue();
      }
    } else {
      debug(loggerPrefix, "Translation for {0} in {1}", s, iso3Language);
      return s;
    }
  }

  protected String getElementTooltip(String s, String iso3Language) {
    var loggerPrefix = getLoggerPrefix("getElementTooltip", s, iso3Language);
    ElementTrlDTO elementTrl;
    try {
      elementTrl = getElementTrl(s, iso3Language);
    } catch (AxonException e) {
      error(loggerPrefix, "Element Translation not found for {0} in {1}", s, iso3Language);
      elementTrl = null;
    }

    if (elementTrl != null) {
      return elementTrl.getTooltip();
    } else {
      debug(loggerPrefix, "Tooltip for {0} in {1} not found", s, iso3Language);
      return s;
    }
  }

  protected String getActionTranslation(String s, String iso3Language, Object... objects) {
    var loggerPrefix = getLoggerPrefix("getActionTranslation", s, iso3Language);
    ActionTrlDTO actionTrl;

    try {
      actionTrl = getActionTrl(s, iso3Language);
    } catch (AxonException e) {
      error(loggerPrefix, "Action Translation not found for {0} in {1}", s, iso3Language);
      actionTrl = null;
    }

    if (actionTrl != null) {
      if (StringUtils.isNotBlank(actionTrl.getValue()) && objects.length > 0) {
        return String.format(actionTrl.getValue(), objects);
      } else {
        return actionTrl.getValue();
      }
    } else {
      debug(loggerPrefix, "Translation for {0} in {1}", s, iso3Language);
      return s;
    }
  }

  protected String getActionTooltip(String s, String iso3Language) {
    var loggerPrefix = getLoggerPrefix("getActionTooltip", s, iso3Language);
    ActionTrlDTO actionTrl;

    try {
      actionTrl = getActionTrl(s, iso3Language);
    } catch (AxonException e) {
      error(loggerPrefix, "Action Translation not found for {0} in {1}", s, iso3Language);
      actionTrl = null;
    }

    if (actionTrl != null) {
      return actionTrl.getTooltip();
    } else {
      debug(loggerPrefix, "Tooltip for {0} in {1} not found", s, iso3Language);
      return s;
    }
  }

  protected String getMessageTranslation(String s, String iso3Language, Object... objects) {
    var loggerPrefix = getLoggerPrefix("getMessageTranslation", s, iso3Language);
    MessageTrlDTO messageTrl;

    try {
      messageTrl = getMessageTrl(s, iso3Language);
    } catch (AxonException e) {
      error(loggerPrefix, "Message Translation not found for {0} in {1}", s, iso3Language);
      messageTrl = null;
    }

    if (messageTrl != null) {
      if (StringUtils.isNotBlank(messageTrl.getValue()) && objects.length > 0) {
        return String.format(messageTrl.getValue(), objects);
      } else {
        return messageTrl.getValue();
      }
    } else {
      debug(loggerPrefix, "Translation for {0} in {1}", s, iso3Language);
      return s;
    }
  }

  public synchronized void loadRemoteLocales(String iso3Language) {
    var loggerPrefix = getLoggerPrefix("loadRemoteLocales", iso3Language);

    if (loadedLocale != null && loadedLocale.equals(iso3Language)) {
      return;
    }

    debug(loggerPrefix, "Bootstrap {0}", iso3Language);

    try {
      elementMap.clear();
      queryGateway
          .query(
              new GetElementTrlsByIso3LanguageQuery(iso3Language),
              ResponseTypes.instanceOf(GetElementTrlsByIso3LanguageQuery.Response.class))
          .whenComplete(
              (response, throwable) -> {
                if (throwable == null) {
                  response
                      .getData()
                      .forEach(
                          elementTrlDTO -> elementMap.put(elementTrlDTO.getName(), elementTrlDTO));
                  debug(loggerPrefix, "{0} elements loaded", response.getData().size());
                } else {
                  error(loggerPrefix, throwable, "Unexpected error {0}", throwable.getMessage());
                }
              });

      actionMap.clear();
      queryGateway
          .query(
              new GetActionTrlsByIso3LanguageQuery(iso3Language),
              ResponseTypes.instanceOf(GetActionTrlsByIso3LanguageQuery.Response.class))
          .whenComplete(
              (response, throwable) -> {
                if (throwable == null) {
                  response
                      .getData()
                      .forEach(actionTrlDTO -> actionMap.put(actionTrlDTO.getName(), actionTrlDTO));
                  debug(loggerPrefix, "{0} actions loaded", response.getData().size());
                } else {
                  error(loggerPrefix, throwable, "Unexpected error {0}", throwable.getMessage());
                }
              });

      messageMap.clear();
      queryGateway
          .query(
              new GetMessageTrlsByIso3LanguageQuery(iso3Language),
              ResponseTypes.instanceOf(GetMessageTrlsByIso3LanguageQuery.Response.class))
          .whenComplete(
              (response, throwable) -> {
                if (throwable == null) {
                  response
                      .getData()
                      .forEach(
                          messageTrlDTO -> messageMap.put(messageTrlDTO.getName(), messageTrlDTO));
                  debug(loggerPrefix, "{0} messages loaded", response.getData().size());
                } else {
                  error(loggerPrefix, throwable, "Unexpected error {0}", throwable.getMessage());
                }
              });

      loadedLocale = iso3Language;
    } catch (NoHandlerForQueryException e) {
      error(loggerPrefix, e, "No handler found..., try later");
    }
    debug(loggerPrefix, "Bootstrap {0} done", iso3Language);
  }

  private ElementTrlDTO getElementTrl(String name, String iso3Language) {
    var loggerPrefix = getLoggerPrefix("getElementTrl");

    if (!loadedLocale.equals(iso3Language)) {
      loadRemoteLocales(iso3Language);
    }

    ElementTrlDTO element = elementMap.get(name);
    String altName = "baseEntity" + name.substring(name.indexOf('.') == -1 ? 0 : name.indexOf('.'));
    if (element == null) {
      element = elementMap.get(altName);
    }

    if (element == null) {
      warn(loggerPrefix, "Element {0} not found locally, check on the server", name);
      element =
          queryGateway
              .query(
                  new GetElementTrlByNameAndIso3LanguageQuery(name, iso3Language),
                  ResponseTypes.instanceOf(GetElementTrlByNameAndIso3LanguageQuery.Response.class))
              .exceptionally(t -> new GetElementTrlByNameAndIso3LanguageQuery.Response(null))
              .join()
              .getData();
      if (element != null) {
        elementMap.put(name, element);

        return element;
      } else {
        warn(loggerPrefix, "Element {0} not found on the server, create a new one", name);

        ElementDTO elementDTO = new ElementDTO();
        elementDTO.setName(name);
        elementDTO.setTranslated(false);

        ElementTrlDTO elementTrlDTO = new ElementTrlDTO();
        elementTrlDTO.setValue(name);
        elementTrlDTO.setIso3Language(iso3Language);
        elementTrlDTO.setTranslated(false);
        elementDTO.getTranslations().add(elementTrlDTO);

        var command = new CreateElementCommand(elementDTO);

        UUID elementId = commandGateway.sendAndWait(command);
        element =
            queryGateway
                .query(
                    new GetElementTrlByElementIdAndIso3LanguageQuery(elementId, iso3Language),
                    ResponseTypes.instanceOf(
                        GetElementTrlByElementIdAndIso3LanguageQuery.Response.class))
                .exceptionally(t -> new GetElementTrlByElementIdAndIso3LanguageQuery.Response(null))
                .join()
                .getData();
        if (element != null) {
          elementMap.put(name, element);

          return element;
        } else {
          return null;
        }
      }
    } else {
      return element;
    }
  }

  private ActionTrlDTO getActionTrl(String name, String iso3Language) {
    var loggerPrefix = getLoggerPrefix("getActionTrl");

    if (!loadedLocale.equals(iso3Language)) {
      loadRemoteLocales(iso3Language);
    }

    ActionTrlDTO action = actionMap.get(name);

    if (action == null) {
      warn(loggerPrefix, "Action {0} not found locally, check on the server", name);
      action =
          queryGateway
              .query(
                  new GetActionTrlByNameAndIso3LanguageQuery(name, iso3Language),
                  ResponseTypes.instanceOf(GetActionTrlByNameAndIso3LanguageQuery.Response.class))
              .exceptionally(t -> new GetActionTrlByNameAndIso3LanguageQuery.Response(null))
              .join()
              .getData();
      if (action != null) {
        actionMap.put(name, action);

        return action;
      } else {
        warn(loggerPrefix, "Action {0} not found on the server, create a new one", name);

        ActionDTO actionDTO = new ActionDTO();
        actionDTO.setName(name);
        actionDTO.setTranslated(false);

        ActionTrlDTO actionTrlDTO = new ActionTrlDTO();
        actionTrlDTO.setValue(name);
        actionTrlDTO.setIso3Language(iso3Language);
        actionTrlDTO.setTranslated(false);
        actionDTO.getTranslations().add(actionTrlDTO);

        var command = new CreateActionCommand(actionDTO);

        UUID actionId = commandGateway.sendAndWait(command);
        action =
            queryGateway
                .query(
                    new GetActionTrlByActionIdAndIso3LanguageQuery(actionId, iso3Language),
                    ResponseTypes.instanceOf(
                        GetActionTrlByActionIdAndIso3LanguageQuery.Response.class))
                .exceptionally(t -> new GetActionTrlByActionIdAndIso3LanguageQuery.Response(null))
                .join()
                .getData();
        if (action != null) {
          actionMap.put(name, action);

          return action;
        } else {
          return null;
        }
      }
    } else {
      return action;
    }
  }

  private MessageTrlDTO getMessageTrl(String name, String iso3Language) {
    var loggerPrefix = getLoggerPrefix("getMessageTrl");

    if (!loadedLocale.equals(iso3Language)) {
      loadRemoteLocales(iso3Language);
    }

    MessageTrlDTO message = messageMap.get(name);
    String altName = "baseEntity" + name.substring(name.indexOf('.'));
    if (message == null) {
      message = messageMap.get(altName);
    }

    if (message == null) {
      warn(loggerPrefix, "Message {0} not found locally, check on the server", name);
      message =
          queryGateway
              .query(
                  new GetMessageTrlByNameAndIso3LanguageQuery(name, iso3Language),
                  ResponseTypes.instanceOf(GetMessageTrlByNameAndIso3LanguageQuery.Response.class))
              .exceptionally(t -> new GetMessageTrlByNameAndIso3LanguageQuery.Response(null))
              .join()
              .getData();
      if (message != null) {
        messageMap.put(name, message);

        return message;
      } else {
        warn(loggerPrefix, "Message {0} not found on the server, create a new one", name);

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setName(name);
        messageDTO.setTranslated(false);

        MessageTrlDTO messageTrlDTO = new MessageTrlDTO();
        messageTrlDTO.setValue(name);
        messageTrlDTO.setIso3Language(iso3Language);
        messageTrlDTO.setTranslated(false);
        messageDTO.getTranslations().add(messageTrlDTO);

        var command = new CreateMessageCommand(messageDTO);

        UUID messageId = commandGateway.sendAndWait(command);
        message =
            queryGateway
                .query(
                    new GetMessageTrlByMessageIdAndIso3LanguageQuery(messageId, iso3Language),
                    ResponseTypes.instanceOf(
                        GetMessageTrlByMessageIdAndIso3LanguageQuery.Response.class))
                .exceptionally(t -> new GetMessageTrlByMessageIdAndIso3LanguageQuery.Response(null))
                .join()
                .getData();
        if (message != null) {
          messageMap.put(name, message);

          return message;
        } else {
          return null;
        }
      }
    } else {
      return message;
    }
  }

  public void init(Locale locale) {
    loadRemoteLocales(locale.getISO3Language());
  }

  public void elementUpdate(I18NUpdateTypeEnum updateType, ElementDTO element) {
    var loggerPrefix = getLoggerPrefix("elementUpdate", updateType, element);

    if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
      debug(loggerPrefix, "Delete record");
      elementMap.remove(element.getName());
    }
  }

  public void elementTrlUpdate(I18NUpdateTypeEnum updateType, ElementTrlDTO elementTrl) {
    var loggerPrefix = getLoggerPrefix("elementTrlUpdate", updateType, elementTrl);

    if (loadedLocale != null && loadedLocale.equalsIgnoreCase(elementTrl.getIso3Language())) {
      if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
        debug(loggerPrefix, "Delete record");
        elementMap.remove(elementTrl.getName());
      } else {
        debug(loggerPrefix, "Create or Update record");
        elementMap.put(elementTrl.getName(), elementTrl);
      }
    }
  }

  public void actionUpdate(I18NUpdateTypeEnum updateType, ActionDTO action) {
    var loggerPrefix = getLoggerPrefix("actionUpdate", updateType, action);

    if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
      debug(loggerPrefix, "Delete record");
      actionMap.remove(action.getName());
    }
  }

  public void actionTrlUpdate(I18NUpdateTypeEnum updateType, ActionTrlDTO actionTrl) {
    var loggerPrefix = getLoggerPrefix("actionTrlUpdate", updateType, actionTrl);

    if (loadedLocale != null && loadedLocale.equalsIgnoreCase(actionTrl.getIso3Language())) {
      if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
        debug(loggerPrefix, "Delete record");
        actionMap.remove(actionTrl.getName());
      } else {
        debug(loggerPrefix, "Create or Update record");
        actionMap.put(actionTrl.getName(), actionTrl);
      }
    }
  }

  public void messageUpdate(I18NUpdateTypeEnum updateType, MessageDTO message) {
    var loggerPrefix = getLoggerPrefix("messageUpdate", updateType, message);

    if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
      debug(loggerPrefix, "Delete record");
      messageMap.remove(message.getName());
    }
  }

  public void messageTrlUpdate(I18NUpdateTypeEnum updateType, MessageTrlDTO messageTrl) {
    var loggerPrefix = getLoggerPrefix("messageTrlUpdate", updateType, messageTrl);

    if (loadedLocale != null && loadedLocale.equalsIgnoreCase(messageTrl.getIso3Language())) {
      if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
        debug(loggerPrefix, "Delete record");
        messageMap.remove(messageTrl.getName());
      } else {
        debug(loggerPrefix, "Create or Update record");
        messageMap.put(messageTrl.getName(), messageTrl);
      }
    }
  }
}
