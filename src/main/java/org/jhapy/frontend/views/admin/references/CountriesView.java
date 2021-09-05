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

package org.jhapy.frontend.views.admin.references;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.jhapy.dto.domain.reference.*;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.reference.intermediateRegionTrl.GetIntermediateRegionTrlQuery;
import org.jhapy.dto.serviceQuery.reference.regionTrl.GetRegionTrlQuery;
import org.jhapy.dto.serviceQuery.reference.subRegionTrl.GetSubRegionTrlQuery;
import org.jhapy.dto.utils.AppContext;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.reference.ReferenceServices;
import org.jhapy.frontend.customFields.CountryTrlListField;
import org.jhapy.frontend.dataproviders.CountryDataProvider;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.renderer.BooleanOkRenderer;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
@I18NPageTitle(messageKey = AppConst.TITLE_COUNTRIES)
@RequiresRole(SecurityConst.ROLE_ADMIN)
public class CountriesView
    extends DefaultMasterDetailsView<Country, DefaultFilter, SearchQuery, SearchQueryResult> {

  public CountriesView(MyI18NProvider myI18NProvider) {
    super(
        "country.",
        Country.class,
        new CountryDataProvider(),
        (e) -> ReferenceServices.getCountryService().save(new SaveQuery<>(e)),
        e -> ReferenceServices.getCountryService().delete(new DeleteByIdQuery(e.getId())),
        myI18NProvider);
  }

  protected Grid createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));

    grid.setDataProvider(dataProvider);
    grid.setHeight("100%");

    grid.addColumn(Country::getName)
        .setKey("name")
        .setSortProperty("name." + AppContext.getInstance().getCurrentIso3Language() + ".value");
    grid.addColumn(Country::getIso2).setKey("iso2").setSortProperty("iso3");
    grid.addColumn(Country::getIso3).setKey("iso3").setSortProperty("iso3");
    grid.addColumn(new BooleanOkRenderer<>(Country::getIsEU)).setKey("isEU");

    grid.addColumn(e -> (e.getRegion() != null) ? e.getRegion().getName() : "")
        .setKey("region")
        .setSortProperty(
            "r.`name." + AppContext.getInstance().getCurrentIso3Language() + ".value`");

    grid.addColumn(e -> (e.getSubRegion() != null ? e.getSubRegion().getName() : ""))
        .setKey("subRegion")
        .setSortProperty(
            "s.`name." + AppContext.getInstance().getCurrentIso3Language() + ".value`");
    grid.addColumn(
            e -> (e.getIntermediateRegion() != null ? e.getIntermediateRegion().getName() : ""))
        .setKey("intermediateRegion")
        .setSortProperty(
            "i.`name." + AppContext.getInstance().getCurrentIso3Language() + ".value`");

    grid.getColumns()
        .forEach(
            column -> {
              if (column.getKey() != null) {
                column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
                column.setResizable(true);
                column.setSortable(true);
              }
            });
    return grid;
  }

  protected Component createDetails(Country country) {
    Div mainView = new Div();

    boolean isNew = country.getId() == null;
    detailsDrawerHeader.setTitle(
        isNew
            ? getTranslation("element.global.new") + " : "
            : getTranslation("element.global.update") + " : " + country.getIso3());

    detailsDrawerFooter.setDeleteButtonVisible(!isNew);

    TextField iso2 = new TextField();
    iso2.setWidth("100%");

    TextField iso3 = new TextField();
    iso3.setWidth("100%");

    Checkbox isEU = new Checkbox();

    TextField dialingCode = new TextField();
    dialingCode.setWidth("100%");

    ComboBox<Region> region = new ComboBox<>();
    region.setItems(ReferenceServices.getRegionService().findAll().getData());
    region.setItemLabelGenerator(
        (ItemLabelGenerator<Region>)
            e -> {
              RegionTrl regionTrl =
                  ReferenceServices.getRegionTrlService()
                      .getRegionTrl(new GetRegionTrlQuery(e.getId(), getLocale().getLanguage()))
                      .getData();
              if (regionTrl != null) {
                return regionTrl.getName();
              } else {
                return "";
              }
            });
    region.setWidth("100%");

    ComboBox<SubRegion> subRegion = new ComboBox<>();
    subRegion.setItems(ReferenceServices.getSubRegionService().findAll().getData());
    subRegion.setItemLabelGenerator(
        (ItemLabelGenerator<SubRegion>)
            e -> {
              SubRegionTrl subRegionTrl =
                  ReferenceServices.getSubRegionTrlService()
                      .getSubRegionTrl(
                          new GetSubRegionTrlQuery(e.getId(), getLocale().getLanguage()))
                      .getData();
              if (subRegionTrl != null) {
                return subRegionTrl.getName();
              } else {
                return "";
              }
            });
    subRegion.setWidth("100%");

    ComboBox<IntermediateRegion> intermediateRegion = new ComboBox<>();
    intermediateRegion.setItems(
        ReferenceServices.getIntermediateRegionService().findAll(new BaseRemoteQuery()).getData());
    intermediateRegion.setItemLabelGenerator(
        (ItemLabelGenerator<IntermediateRegion>)
            e -> {
              IntermediateRegionTrl intermediateRegionTrl =
                  ReferenceServices.getIntermediateRegionTrlService()
                      .getIntermediateRegionTrl(
                          new GetIntermediateRegionTrlQuery(e.getId(), getLocale().getLanguage()))
                      .getData();
              if (intermediateRegionTrl != null) {
                return intermediateRegionTrl.getName();
              } else {
                return "";
              }
            });
    intermediateRegion.setWidth("100%");

    RadioButtonGroup<Boolean> isActive = new RadioButtonGroup<>();
    isActive.setItems(true, false);
    isActive.setRenderer(
        new TextRenderer<>(
            value ->
                value
                    ? getTranslation("element.global.active")
                    : getTranslation("element.global.inactive")));

    CountryTrlListField translations = new CountryTrlListField();
    translations.setReadOnly(false);
    translations.setWidth("100%");

    // Form layout
    FormLayout editingForm = new FormLayout();
    editingForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

    FormLayout.FormItem iso2Item =
        editingForm.addFormItem(iso2, getTranslation("element." + I18N_PREFIX + "iso2"));
    FormLayout.FormItem iso3Item =
        editingForm.addFormItem(iso3, getTranslation("element." + I18N_PREFIX + "iso3"));
    FormLayout.FormItem isEUItem =
        editingForm.addFormItem(isEU, getTranslation("element." + I18N_PREFIX + "isEU"));
    FormLayout.FormItem dialingCodeItem =
        editingForm.addFormItem(
            dialingCode, getTranslation("element." + I18N_PREFIX + "dialingCode"));
    FormLayout.FormItem regionItem =
        editingForm.addFormItem(region, getTranslation("element." + I18N_PREFIX + "region"));
    FormLayout.FormItem subRegionItem =
        editingForm.addFormItem(subRegion, getTranslation("element." + I18N_PREFIX + "subRegion"));
    FormLayout.FormItem intermediateRegionItem =
        editingForm.addFormItem(
            intermediateRegion, getTranslation("element." + I18N_PREFIX + "intermediateRegion"));

    FormLayout.FormItem translationsItem =
        editingForm.addFormItem(
            translations, getTranslation("element." + I18N_PREFIX + "translations"));

    FormLayout.FormItem isActiveItem =
        editingForm.addFormItem(isActive, getTranslation("element." + I18N_PREFIX + "isActive"));

    UIUtils.setColSpan(2, translationsItem, isActiveItem);

    binder.setBean(country);

    binder.bind(iso2, Country::getIso2, Country::setIso2);
    binder.bind(iso3, Country::getIso3, Country::setIso3);
    binder.bind(isEU, Country::getIsEU, Country::setIsEU);
    binder.bind(dialingCode, Country::getDialingCode, Country::setDialingCode);
    binder.bind(region, this::getRegion, Country::setRegion);
    binder.bind(subRegion, this::getSubRegion, Country::setSubRegion);
    binder.bind(intermediateRegion, this::getIntermediateRegion, Country::setIntermediateRegion);
    // binder.bind(translations, country1 -> new ArrayList<>(country1.getTranslations()), (country1,
    // countryTrls) -> country1.setTranslations(new HashSet<>(countryTrls)));

    binder.bind(isActive, Country::getIsActive, Country::setIsActive);

    return editingForm;
  }

  private Region getRegion(Country country) {
    Country c =
        ReferenceServices.getCountryService().getById(new GetByIdQuery(country.getId())).getData();
    if (c == null || c.getRegion() == null) {
      return null;
    } else {
      return c.getRegion();
    }
  }

  private SubRegion getSubRegion(Country country) {
    Country c =
        ReferenceServices.getCountryService().getById(new GetByIdQuery(country.getId())).getData();
    if (c == null || c.getSubRegion() == null) {
      return null;
    } else {
      return c.getSubRegion();
    }
  }

  private IntermediateRegion getIntermediateRegion(Country country) {
    Country c =
        ReferenceServices.getCountryService().getById(new GetByIdQuery(country.getId())).getData();
    if (c == null || c.getIntermediateRegion() == null) {
      return null;
    } else {
      return c.getIntermediateRegion();
    }
  }
}