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
import com.vaadin.flow.component.crud.CrudI18n;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.shared.Registration;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.BaseInnerEntity;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.events.CustomListFieldFreeValueChangeEvent;
import org.jhapy.frontend.dataproviders.DefaultBackendFree;

import java.io.Serializable;
import java.util.*;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
public abstract class DefaultCustomListFieldFree<C extends BaseInnerEntity> extends FlexBoxLayout
    implements HasValue<CustomListFieldFreeValueChangeEvent<C>, List<C>>, HasLogger, Serializable {

  protected final String i18nPrefix;
  private final List<ValueChangeListener<? super CustomListFieldFreeValueChangeEvent<C>>>
      changeListeners = new ArrayList<>();
  protected Crud<C> gridCrud;
  protected Button newButton;
  protected DefaultBackendFree<C> dataProvider;
  protected Grid.Column editColumn;

  protected DefaultCustomListFieldFree(String i18nPrefix) {
    this.i18nPrefix = i18nPrefix;
  }

  protected static String createEditColumnTemplate(String crudI18n) {
    return "<vaadin-crud-edit aria-label=\"" + crudI18n + "\"></vaadin-crud-edit>";
  }

  public DefaultBackendFree<C> getDataProvider() {
    return dataProvider;
  }

  @Override
  public List<C> getValue() {
    var loggerPrefix = getLoggerPrefix("generateModelValue");
    logger().debug(loggerPrefix + "Result =  " + dataProvider.getValues());
    return new ArrayList<>(dataProvider.getValues());
  }

  @Override
  public void setValue(List<C> values) {
    var loggerPrefix = getLoggerPrefix("setPresentationValue");
    logger().debug(loggerPrefix + "Param =  " + values);
    if (values != null) {
      dataProvider.setValues(values);
    }
  }

  @Override
  public Registration addValueChangeListener(
      ValueChangeListener<? super CustomListFieldFreeValueChangeEvent<C>> valueChangeListener) {
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

  public void updateValue(List<C> oldValues, List<C> newValues) {
    changeListeners.forEach(
        valueChangeListener ->
            valueChangeListener.valueChanged(
                new CustomListFieldFreeValueChangeEvent<>(oldValues, newValues, this)));
  }

  public class Backend extends DefaultBackendFree<C> {

    @Override
    public Object getId(C item) {
      return item.getTemporaryId();
    }

    public void setValues(Collection<C> values) {
      fieldsMap.clear();
      values.forEach(
          value -> {
            if (value.getTemporaryId() == null) {
              value.setTemporaryId(UUID.randomUUID());
            }
          });
      fieldsMap.addAll(values);
    }

    public void persist(C value) {
      List<C> previousValues = new ArrayList<>(fieldsMap);

      if (value.getTemporaryId() == null) {
        value.setTemporaryId(UUID.randomUUID());
      }
      if (!fieldsMap.contains(value)) {
        fieldsMap.add(value);
      }
      updateValue(previousValues, fieldsMap);
    }

    public void delete(C value) {
      List<C> previousValues = new ArrayList<>(fieldsMap);

      fieldsMap.remove(value);

      updateValue(previousValues, fieldsMap);
    }
  }
}