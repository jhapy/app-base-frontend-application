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

package org.jhapy.frontend.customFields;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.CrudI18n;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.shared.Registration;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.EntityTranslationV2;
import org.jhapy.frontend.components.events.CustomListFieldValueChangeEventForTranslationV3;
import org.jhapy.frontend.dataproviders.DefaultBackendForTranslationV2;

import java.io.Serializable;
import java.util.*;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
public abstract class DefaultCustomListFieldForTranslationV3<C extends EntityTranslationV2>
    extends Crud<C>
    implements HasValue<CustomListFieldValueChangeEventForTranslationV3<C>, Map<String, C>>,
        HasLogger,
        Serializable {

  protected final String i18nPrefix;
  private final List<
          ValueChangeListener<? super CustomListFieldValueChangeEventForTranslationV3<C>>>
      changeListeners = new ArrayList<>();
  // protected Crud<C> gridCrud;
  protected Button newButton;
  protected DefaultBackendForTranslationV2<C> dataProvider;
  protected Grid.Column editColumn;

  protected DefaultCustomListFieldForTranslationV3(
      Class<C> beanType, CrudEditor<C> editor, String i18nPrefix) {
    super(beanType, editor);
    this.i18nPrefix = i18nPrefix;
  }

  protected static String createEditColumnTemplate(String crudI18n) {
    return "<vaadin-crud-edit aria-label=\"" + crudI18n + "\"></vaadin-crud-edit>";
  }

  @Override
  public Map<String, C> getValue() {
    var loggerPrefix = getLoggerPrefix("generateModelValue");
    logger().debug(loggerPrefix + "Result =  " + dataProvider.getValues());
    return new HashMap<>(dataProvider.getValues());
  }

  @Override
  public void setValue(Map<String, C> values) {
    var loggerPrefix = getLoggerPrefix("setPresentationValue");
    logger().debug(loggerPrefix + "Param =  " + values);
    if (values != null) {
      dataProvider.setValues(values);
      dataProvider.refreshAll();
    }
  }

  public void refreshItem(C e) {
    dataProvider.refreshItem(e);
  }

  public void refreshAll() {
    dataProvider.refreshAll();
  }

  @Override
  public Registration addValueChangeListener(
      ValueChangeListener<? super CustomListFieldValueChangeEventForTranslationV3<C>>
          valueChangeListener) {
    changeListeners.add(valueChangeListener);
    return () -> changeListeners.remove(valueChangeListener);
  }

  protected CrudI18n createI18n() {
    Locale currentLocal = UI.getCurrent().getLocale();
    CrudI18n i18nGrid = CrudI18n.createDefault();

    i18nGrid.setNewItem(getTranslation("action.global.addButton", currentLocal));
    i18nGrid.setEditItem(getTranslation("action.global.editButton", currentLocal));
    i18nGrid.setSaveItem(getTranslation("action.global.saveButton", currentLocal));
    i18nGrid.setDeleteItem(getTranslation("action.global.deleteButton", currentLocal));
    i18nGrid.setCancel(getTranslation("action.global.cancel", currentLocal));
    i18nGrid.setEditLabel(getTranslation("action.global.editButton", currentLocal));

    i18nGrid
        .getConfirm()
        .getCancel()
        .setTitle(getTranslation("element.global.cancel.title", currentLocal));
    i18nGrid
        .getConfirm()
        .getCancel()
        .setContent(getTranslation("element.global.cancel.content", currentLocal));
    i18nGrid
        .getConfirm()
        .getCancel()
        .getButton()
        .setDismiss(getTranslation("action.global.cancel.dismissButton", currentLocal));
    i18nGrid
        .getConfirm()
        .getCancel()
        .getButton()
        .setConfirm(getTranslation("action.global.cancel.confirmButton", currentLocal));

    i18nGrid
        .getConfirm()
        .getDelete()
        .setTitle(getTranslation("element.global.delete.title", currentLocal));
    i18nGrid
        .getConfirm()
        .getDelete()
        .setContent(getTranslation("element.global.delete.content", currentLocal));
    i18nGrid
        .getConfirm()
        .getDelete()
        .getButton()
        .setDismiss(getTranslation("action.global.delete.dismissButton", currentLocal));
    i18nGrid
        .getConfirm()
        .getDelete()
        .getButton()
        .setConfirm(getTranslation("action.global.delete.confirmButton", currentLocal));

    return i18nGrid;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void setReadOnly(boolean b) {}

  @Override
  public boolean isRequiredIndicatorVisible() {
    return false;
  }

  @Override
  public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {}

  public void updateValue(Map<String, C> oldValues, Map<String, C> newValues) {
    changeListeners.forEach(
        valueChangeListener ->
            valueChangeListener.valueChanged(
                new CustomListFieldValueChangeEventForTranslationV3<>(oldValues, newValues, this)));
  }

  public class Backend extends DefaultBackendForTranslationV2<C> {

    @Override
    public Object getId(C item) {
      return item.getId();
    }

    public void setValues(Map<String, C> values) {
      fieldsMap.clear();
      fieldsMap.putAll(values);
    }

    public void persist(C value) {
      Map<String, C> previousValues = new HashMap<>(fieldsMap);

      if (value.getId() == null) {
        value.setId(uniqueLong.incrementAndGet());
        value.setIsNew(true);
      }
      if (!fieldsMap.containsKey(value.getIso3Language())) {
        fieldsMap.put(value.getIso3Language(), value);
      }
      updateValue(previousValues, fieldsMap);
    }

    public void delete(C value) {
      Map<String, C> previousValues = new HashMap<>(fieldsMap);

      fieldsMap.remove(value.getIso3Language());

      updateValue(previousValues, fieldsMap);
    }
  }
}