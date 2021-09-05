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
import com.vaadin.flow.component.checkbox.Checkbox;
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
import org.jhapy.dto.domain.i18n.ElementTrlDTO;
import org.jhapy.frontend.renderer.BooleanOkRenderer;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
public class ElementTrlListField extends DefaultCustomListField<ElementTrlDTO>
    implements Serializable {

  public ElementTrlListField() {
    super("elementTrl.");

    dataProvider = new Backend();

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
    Grid<ElementTrlDTO> grid = new Grid<>();

    gridCrud = new Crud(ElementTrlDTO.class, grid, createInterfaceTrlEditor());
    gridCrud.setMinHeight("300px");
    gridCrud.setWidth("100%");
    gridCrud.setI18n(createI18n());
    gridCrud.getGrid().setEnabled(false);
    gridCrud.setDataProvider(dataProvider);
    gridCrud.addSaveListener(e -> dataProvider.persist(e.getItem()));
    gridCrud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

    editColumn =
        grid.addColumn(TemplateRenderer.of(createEditColumnTemplate("Edit")))
            .setKey("vaadin-crud-edit-column")
            .setWidth("4em")
            .setFlexGrow(0);

    grid.addColumn(ElementTrlDTO::getValue)
        .setHeader(getTranslation("element." + i18nPrefix + "value"));
    grid.addColumn(
            new TextRenderer<>(
                row ->
                    row.getIso3Language() == null
                        ? ""
                        : (new Locale(row.getIso3Language())).getDisplayLanguage(getLocale())))
        .setHeader(getTranslation("element." + i18nPrefix + "language"));

    grid.addColumn(new BooleanOkRenderer<>(ElementTrlDTO::getIsDefault))
        .setHeader(getTranslation("element." + i18nPrefix + "isDefault"));

    newButton = new Button(getTranslation("action.global.addButton"));
    newButton.getElement().setAttribute("theme", "primary");
    newButton.addClickListener(
        event -> gridCrud.getElement().executeJs("$0.__openEditor($1)", gridCrud, "'new'"));
    gridCrud.setToolbar(newButton);

    newButton.setEnabled(false);
    editColumn.setVisible(false);

    return gridCrud;
  }

  protected CrudEditor<ElementTrlDTO> createInterfaceTrlEditor() {
    TextField value = new TextField(getTranslation("element." + i18nPrefix + "value"));

    ComboBox<Locale> language =
        new ComboBox<>(
            getTranslation("element." + i18nPrefix + "language"),
            MyI18NProvider.getAvailableLanguages(getLocale()));
    language.setItemLabelGenerator(Locale::getDisplayLanguage);

    Checkbox isDefault = new Checkbox(getTranslation("element." + i18nPrefix + "isDefault"));
    Checkbox isTranslated = new Checkbox(getTranslation("element." + i18nPrefix + "isTranslated"));

    FormLayout form = new FormLayout(value, isDefault, isTranslated, language);

    Binder<ElementTrlDTO> binder = new BeanValidationBinder<>(ElementTrlDTO.class);
    binder.forField(value).asRequired().bind(ElementTrlDTO::getValue, ElementTrlDTO::setValue);
    binder.forField(isDefault).bind(ElementTrlDTO::getIsDefault, ElementTrlDTO::setIsDefault);
    binder
        .forField(isTranslated)
        .bind(ElementTrlDTO::getIsTranslated, ElementTrlDTO::setIsTranslated);
    binder
        .forField(language)
        .asRequired()
        .bind(
            (elementTrl) ->
                elementTrl.getIso3Language() == null
                    ? null
                    : new Locale(elementTrl.getIso3Language()),
            (elementTrl, locale) -> elementTrl.setIso3Language(locale.getLanguage()));

    return new BinderCrudEditor<>(binder, form);
  }
}