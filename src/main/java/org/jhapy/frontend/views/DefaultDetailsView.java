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

import ch.carnet.kasparscherrer.EmptyFormLayoutItem;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.*;
import com.vaadin.flow.shared.Registration;
import dev.mett.vaadin.tooltip.Tooltips;
import dev.mett.vaadin.tooltip.config.TC_HIDE_ON_CLICK;
import dev.mett.vaadin.tooltip.config.TooltipConfiguration;
import org.axonframework.queryhandling.QueryGateway;
import org.claspina.confirmdialog.ButtonOption;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerFooter;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerHeader;
import org.jhapy.frontend.components.navigation.menubar.ModuleToolbar;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.DateTimeFormatter;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 8/27/19
 */
public abstract class DefaultDetailsView<T extends BaseEntity> extends ViewFrame
    implements HasLogger {

  protected static final Set<String> rtlSet;

  static {

    // Yiddish
    rtlSet =
        Set.of(
            "ar", // Arabic
            "dv", // Divehi
            "fa", // Persian
            "ha", // Hausa
            "he", // Hebrew
            "iw", // Hebrew
            "ji", // Yiddish
            "ps", // Pushto
            "sd", // Sindhi
            "ug", // Uighur
            "ur", // Urdu
            "yi");
  }

  protected final String I18N_PREFIX;
  protected final MyI18NProvider myI18NProvider;
  private final Class<T> entityType;
  private final Consumer<T> deleteHandler;
  protected Binder<T> binder;
  protected T currentEditing;
  protected Registration contextIconRegistration = null;
  protected DefaultDetailsContent detailsDrawer;
  protected DetailsDrawerHeader detailsDrawerHeader;
  protected DetailsDrawerFooter detailsDrawerFooter;
  protected ModuleToolbar moduleToolbar;
  protected Tabs tabs;
  private Function<T, ServiceResult<T>> saveHandler;
  private Class parentViewClassname;
protected QueryGateway queryGateway;

  protected DefaultDetailsView(
      String I18N_PREFIX,
      Class<T> entityType,
      Class parentViewClassname,
      MyI18NProvider myI18NProvider) {
    super();
    this.I18N_PREFIX = I18N_PREFIX;
    this.entityType = entityType;
    this.binder = new BeanValidationBinder<>(entityType);
    this.saveHandler = null;
    this.deleteHandler = null;
    this.parentViewClassname = parentViewClassname;
    this.myI18NProvider = myI18NProvider;
  }

  protected DefaultDetailsView(
      String I18N_PREFIX,
      Class<T> entityType,
      Class parentViewClassname,
      Function<T, ServiceResult<T>> saveHandler,
      Consumer<T> deleteHandler,
      MyI18NProvider myI18NProvider) {
    super();
    this.I18N_PREFIX = I18N_PREFIX;
    this.entityType = entityType;
    this.binder = new BeanValidationBinder<>(entityType);
    this.saveHandler = saveHandler;
    this.deleteHandler = deleteHandler;
    this.parentViewClassname = parentViewClassname;
    this.myI18NProvider = myI18NProvider;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    initHeader();

    setViewContent(createContent());
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    if (contextIconRegistration != null) {
      contextIconRegistration.remove();
    }
  }

  protected void setParentViewClassname(Class parentViewClassname) {
    this.parentViewClassname = parentViewClassname;
  }

  protected void setSaveHandler(Function<T, ServiceResult<T>> saveHandler) {
    this.saveHandler = saveHandler;
  }

  protected T getCurrentEditing() {
    return currentEditing;
  }

  protected void setCurrentEditing(T currentEditing) {
    this.currentEditing = currentEditing;
  }

  protected void initHeader() {
    moduleToolbar = new ModuleToolbar(entityType.getSimpleName(), this);

    moduleToolbar.addGoBackListener(
        () -> {
          if (displayInANewTab()) {
            getParentTab().closeTab();
            return;
          }
          if (super.getGoBackListener() != null) super.getGoBackListener().goBack();
          else super.getMenuBackListener().goBack();
        });

    moduleToolbar.setSearchTextFieldVisible(false);
    moduleToolbar.setFilterButtonVisible(false);
    moduleToolbar.setShowInactiveButtonVisible(false);
    moduleToolbar.setRefreshButtonVisible(false);

    if (canCreateRecord() && saveHandler != null) {
      moduleToolbar.setAddButtonVisible(true);
      moduleToolbar.addNewRecordListener(
          () -> {
            try {
              showDetails(getNewInstance());
            } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException ignored) {
            }
          });
    }

    setViewHeader(moduleToolbar);

    if (isShowTabs()) {
      buildTabs();
    }
  }

  protected T getNewInstance()
      throws NoSuchMethodException, InvocationTargetException, InstantiationException,
          IllegalAccessException {
    return entityType.getDeclaredConstructor().newInstance();
  }

  protected boolean canCreateRecord() {
    return true;
  }

  protected void checkForDetailsChanges(Runnable action) {
    if (currentEditing != null && this.binder.hasChanges()) {
      org.claspina.confirmdialog.ConfirmDialog.createQuestion()
          .withCaption(getTranslation("element.global.unsavedChanged.title"))
          .withMessage(getTranslation("message.global.unsavedChanged"))
          .withOkButton(
              action::run,
              ButtonOption.focus(),
              ButtonOption.caption(getTranslation("action.global.yes")))
          .withCancelButton(ButtonOption.caption(getTranslation("action.global.no")))
          .open();
    } else {
      action.run();
    }
  }

  protected abstract String getTitle(T entity);

  protected abstract Component createDetails(T entity);

  private Component createContent() {
    return getContentTab();
  }

  protected Component buildContent() {
    return new Div();
  }

  protected Tabs buildTabs() {
    Tab details = new Tab(getTranslation("element.title.details"));
    Tab audit = new Tab(getTranslation("element.title.audit"));

    tabs = new Tabs(details, audit);
    tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);
    tabs.addSelectedChangeListener(
        e -> {
          Tab selectedTab = tabs.getSelectedTab();
          if (selectedTab.equals(details)) {
            detailsDrawer.setContent(createDetails(currentEditing));
          } else if (selectedTab.equals(audit)) {
            detailsDrawer.setContent(createAudit(currentEditing));
          }
        });

    return tabs;
  }

  protected boolean isShowTabs() {
    return true;
  }

  protected boolean isShowFooter() {
    return true;
  }

  private Component getContentTab() {
    detailsDrawer = new DefaultDetailsContent(createDetails(currentEditing));

    tabs = buildTabs();

    detailsDrawerHeader =
        new DetailsDrawerHeader(
            getTranslation("element." + I18N_PREFIX + "className"), tabs, false, true);

    FlexBoxLayout contentTab = new FlexBoxLayout();
    contentTab.add(detailsDrawerHeader, detailsDrawer);
    contentTab.setFlexGrow(1, detailsDrawer);
    contentTab.setSizeFull();
    contentTab.setFlexDirection(FlexDirection.COLUMN);

    if (isShowFooter()) {
      detailsDrawerFooter = new DetailsDrawerFooter();
      detailsDrawerFooter.setWidth("");
      if (saveHandler == null || !canSave()) {
        detailsDrawerFooter.setSaveButtonVisible(false);
        detailsDrawerFooter.setSaveAndNewButtonVisible(false);
      }
      if (deleteHandler == null || !canDelete()) {
        detailsDrawerFooter.setDeleteButtonVisible(false);
      }

      detailsDrawerFooter.addCancelListener(
          e -> {
            currentEditing = null;
            UI.getCurrent().getPage().getHistory().back();
          });
      if (saveHandler != null && canSave()) {
        detailsDrawerFooter.addSaveListener(e -> save(false));
        detailsDrawerFooter.addSaveAndNewListener(e -> save(true));
      }
      if (deleteHandler != null && canDelete()) {
        detailsDrawerFooter.addDeleteListener(e -> delete());
      }

      contentTab.add(detailsDrawerFooter);
    }
    return contentTab;
  }

  protected boolean canSave() {
    return true;
  }

  protected boolean canDelete() {
    return true;
  }

  protected void showDetails(T entity) {
    if (entity == null) {
      return;
    }

    this.binder = new BeanValidationBinder<>(entityType);
    currentEditing = entity;
    detailsDrawer.setContent(createDetails(entity));
  }

  protected Component createAudit(T entity) {
    var idField = new TextField();
    idField.setWidthFull();

    var clientNameField = new TextField();
    clientNameField.setWidthFull();

    var isActiveField = new Checkbox();

    var createdField = new TextField();
    createdField.setWidthFull();

    var updatedField = new TextField();
    updatedField.setWidthFull();

    var createdByField = new TextField();
    createdByField.setWidthFull();

    var updatedByField = new TextField();
    updatedByField.setWidthFull();

    // Form layout
    var auditForm = new FormLayout();
    auditForm.addClassNames(
        LumoStyles.Padding.Bottom.L, LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    auditForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));
    auditForm.setWidthFull();

    auditForm.addFormItem(idField, getTranslation("element.baseEntity.id"));
    auditForm.addFormItem(clientNameField, getTranslation("element.baseEntity.clientName"));
    auditForm.addFormItem(isActiveField, getTranslation("element.baseEntity.isActive"));
    auditForm.add(new EmptyFormLayoutItem());
    auditForm.addFormItem(createdField, getTranslation("element.baseEntity.created"));
    auditForm.addFormItem(updatedField, getTranslation("element.baseEntity.updated"));
    auditForm.addFormItem(createdByField, getTranslation("element.baseEntity.createdBy"));
    auditForm.addFormItem(updatedByField, getTranslation("element.baseEntity.updatedBy"));

    binder.bind(
        idField, entity1 -> entity1.getId() == null ? null : entity1.getId().toString(), null);
    binder.bind(clientNameField, BaseEntity::getClientName, null);
    binder.bind(isActiveField, BaseEntity::isActive, BaseEntity::setActive);
    binder.bind(
        createdField,
        entity1 ->
            entity1.getCreated() == null
                ? ""
                : DateTimeFormatter.format(entity1.getCreated(), getLocale()),
        null);
    binder.bind(createdByField, activityDisplay -> entity.getCreatedBy(), null);
    binder.bind(
        updatedField,
        entity1 ->
            entity1.getModified() == null
                ? ""
                : DateTimeFormatter.format(entity1.getModified(), getLocale()),
        null);
    binder.bind(updatedByField, activityDisplay -> entity.getModifiedBy(), null);

    return auditForm;
  }

  protected boolean beforeSave(T entity) {
    return true;
  }

  protected void afterSave(T entity) {}

  protected boolean beforeDelete(T entity) {
    return true;
  }

  protected void afterDelete() {}

  private void save(boolean saveAndNew) {
    if (binder.writeBeanIfValid(currentEditing)) {
      boolean isNew = currentEditing.getId() == null;

      if (beforeSave(currentEditing)) {
        ServiceResult<T> result = saveHandler.apply(currentEditing);
        if (result != null && result.getIsSuccess() && result.getData() != null) {
          currentEditing = result.getData();
        } else {
          JHapyMainView3.get()
              .displayErrorMessage(
                  getTranslation("message.global.unknownError", result.getMessage()));
          return;
        }
      }
      afterSave(currentEditing);

      JHapyMainView3.get().displayInfoMessage(getTranslation("message.global.recordSavedMessage"));

      if (saveAndNew) {
        try {
          showDetails(entityType.getDeclaredConstructor().newInstance());

        } catch (InstantiationException
            | IllegalAccessException
            | InvocationTargetException
            | NoSuchMethodException ignored) {
        }
        return;
      }

      if (isNew) {
        redirectAfterNewRecordSave(currentEditing);
      } else {
        showDetails(currentEditing);
      }
    } else {
      BinderValidationStatus<T> validate = binder.validate();
      String errorText =
          validate.getFieldValidationStatuses().stream()
              .filter(BindingValidationStatus::isError)
              .map(BindingValidationStatus::getMessage)
              .map(Optional::get)
              .distinct()
              .collect(Collectors.joining(", "));

      String errorText2 =
          validate.getBeanValidationErrors().stream()
              .map(ValidationResult::getErrorMessage)
              .collect(Collectors.joining(", "));

      Notification.show(
          getTranslation("message.global.validationErrorMessage", errorText + errorText2),
          3000,
          Notification.Position.BOTTOM_CENTER);
    }
  }

  protected void redirectAfterNewRecordSave(T entity) {
    // UI.getCurrent().navigate(getClass(), entity.getId().toString());
  }

  public void delete() {
    ConfirmDialog dialog =
        new ConfirmDialog(
            getTranslation("message.global.confirmDelete.title"),
            getTranslation("message.global.confirmDelete.message"),
            getTranslation("action.global.deleteButton"),
            event -> deleteConfirmed(),
            getTranslation("action.global.cancelButton"),
            event -> {});
    dialog.setConfirmButtonTheme("error primary");

    dialog.setOpened(true);
  }

  private void deleteConfirmed() {
    if (beforeDelete(currentEditing)) {
      deleteHandler.accept(currentEditing);
    }

    afterDelete();

    goBack();
  }

  protected void goBack() {
    UI.getCurrent().navigate(parentViewClassname);
  }

  protected Label getLabel(String element) {
    Label label = new Label(getTranslation(element));
    TooltipConfiguration ttconfig = new TooltipConfiguration(myI18NProvider.getTooltip(element));
    ttconfig.setDelay(1000);
    ttconfig.setHideOnClick(TC_HIDE_ON_CLICK.TRUE);
    ttconfig.setShowOnCreate(false);

    Tooltips.getCurrent().setTooltip(label, ttconfig);
    return label;
  }

  protected Button getButton(String action) {
    return getButton(null, action, false, true);
  }

  protected Button getButton(String action, boolean isSmall) {
    return getButton(null, action, isSmall, true);
  }

  protected Button getButton(VaadinIcon icon, String action) {
    return getButton(icon, action, false, false);
  }

  protected Button getButton(VaadinIcon icon, String action, boolean isSmall, boolean displayText) {
    Button button;
    if (isSmall) {
      if (displayText) {
        if (icon == null) {
          button = UIUtils.createSmallButton(getTranslation(action));
        } else {
          button = UIUtils.createSmallButton(getTranslation(action), icon);
        }
      } else {
        button = UIUtils.createSmallButton(icon);
      }
    } else if (displayText) {
      if (icon == null) {
        button = UIUtils.createButton(getTranslation(action));
      } else {
        button = UIUtils.createButton(getTranslation(action), icon);
      }
    } else {
      button = UIUtils.createButton(icon);
    }
    TooltipConfiguration ttconfig = new TooltipConfiguration(myI18NProvider.getTooltip(action));
    ttconfig.setDelay(1000);
    ttconfig.setHideOnClick(TC_HIDE_ON_CLICK.TRUE);
    ttconfig.setShowOnCreate(false);

    Tooltips.getCurrent().setTooltip(button, ttconfig);

    return button;
  }
}
