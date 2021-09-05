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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import org.jhapy.dto.domain.reference.SubRegionTrl;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.frontend.client.reference.ReferenceServices;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;

import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
public abstract class SubRegionTrlListField extends DefaultCustomListField<SubRegionTrl>
    implements Serializable {

  public SubRegionTrlListField() {
    super("subRegionTrl.");

    dataProvider = new MyBackend();

    add(initContent());

    getElement().setAttribute("colspan", "2");
  }

  public void setReadOnly(boolean readOnly) {
    super.setReadOnly(readOnly);
    if (gridCrud != null) {
      gridCrud.getGrid().setEnabled(!readOnly);
      newButton.setEnabled(!readOnly);
      editColumn.setVisible(!readOnly);
    }
  }

  protected Component initContent() {
    Grid<SubRegionTrl> grid = new Grid<>();

    gridCrud = new Crud<>(SubRegionTrl.class, grid, createInterfaceTrlEditor());
    gridCrud.setMinHeight("300px");
    gridCrud.setWidth("100%");
    gridCrud.setI18n(createI18n());
    gridCrud.getGrid().setEnabled(false);
    gridCrud.setDataProvider(dataProvider);
    gridCrud.addSaveListener(
        e -> {
          if (e.getItem().getId() == null) {
            dataProvider.getValues().add(e.getItem());
          }
        });
    gridCrud.addDeleteListener(e -> dataProvider.getValues().remove(e.getItem()));

    editColumn =
        grid.addColumn(TemplateRenderer.of(createEditColumnTemplate("Edit")))
            .setKey("vaadin-crud-edit-column")
            .setWidth("4em")
            .setFlexGrow(0);

    grid.addColumn(SubRegionTrl::getName)
        .setHeader(getTranslation("element." + i18nPrefix + "name"));
    grid.addColumn(
            new TextRenderer<>(row -> row.getIso3Language() == null ? "" : row.getIso3Language()))
        .setHeader(getTranslation("element." + i18nPrefix + "language"));

    newButton = new Button(getTranslation("action.global.addButton"));
    newButton.getElement().setAttribute("theme", "primary");
    newButton.addClickListener(
        event -> gridCrud.getElement().executeJs("$0.__openEditor($1)", gridCrud, "'new'"));
    gridCrud.setToolbar(newButton);

    newButton.setEnabled(false);
    editColumn.setVisible(false);

    return gridCrud;
  }

  protected CrudEditor<SubRegionTrl> createInterfaceTrlEditor() {
    TextField value = new TextField(getTranslation("element." + i18nPrefix + "value"));

    ComboBox<Locale> language =
        new ComboBox<>(
            getTranslation("element." + i18nPrefix + "language"),
            MyI18NProvider.getAvailableLanguages(getLocale()));
    language.setItemLabelGenerator(Locale::getDisplayLanguage);

    FormLayout form = new FormLayout(value, language);

    Binder<SubRegionTrl> binder = new BeanValidationBinder<>(SubRegionTrl.class);
    binder.forField(value).asRequired().bind(SubRegionTrl::getName, SubRegionTrl::setName);
    binder
        .forField(language)
        .asRequired()
        .bind(
            (subRegionTrl) ->
                subRegionTrl.getIso3Language() == null
                    ? null
                    : new Locale(subRegionTrl.getIso3Language()),
            (subRegionTrl, locale) -> subRegionTrl.setIso3Language(locale.getLanguage()));

    return new BinderCrudEditor<>(binder, form);
  }

  class MyBackend extends Backend {

    public void setValues(Collection<SubRegionTrl> values) {
      if (values != null) {
        values.forEach(
            subRegionTrl -> {
              SubRegionTrl _subRegionTrl =
                  ReferenceServices.getSubRegionTrlService()
                      .getById(new GetByIdQuery(subRegionTrl.getId()))
                      .getData();
              if (_subRegionTrl != null) {
                super.setValues(values);
              }
            });
      }
    }
  }
}