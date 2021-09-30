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
import com.github.appreciated.card.ClickableCard;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveEvent.ContinueNavigationAction;
import com.vaadin.flow.router.BeforeLeaveObserver;
import dev.mett.vaadin.tooltip.Tooltips;
import dev.mett.vaadin.tooltip.config.TC_HIDE_ON_CLICK;
import dev.mett.vaadin.tooltip.config.TooltipConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawer;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerFooter;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerHeader;
import org.jhapy.frontend.components.navigation.menubar.ModuleToolbar;
import org.jhapy.frontend.dataproviders.DefaultDataProvider;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.DefaultSearchDataProvider;
import org.jhapy.frontend.layout.SplitViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BoxSizing;
import org.jhapy.frontend.utils.i18n.DateTimeFormatter;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 8/27/19
 */
public abstract class DefaultMasterDetailsView<
        T extends BaseEntity,
        F extends DefaultFilter,
        Q extends SearchQuery,
        S extends SearchQueryResult>
    extends SplitViewFrame implements BeforeLeaveObserver {

  protected final String I18N_PREFIX;
  protected final DefaultDataProvider<T, F> dataProvider;
  protected final MyI18NProvider myI18NProvider;
  private final Class<T> entityType;
  private final Function<T, ServiceResult<T>> saveHandler;
  private final Function<T, ServiceResult<Void>> deleteHandler;
  protected Grid<T> grid;
  protected DetailsDrawer detailsDrawer;
  protected DetailsDrawerHeader detailsDrawerHeader;
  protected DetailsDrawerFooter detailsDrawerFooter;
  protected Binder<T> binder;
  protected T currentEditing;
  protected ModuleToolbar moduleToolbar;
  private Tabs tabs;
  private Boolean initialFetch = Boolean.TRUE;
  private FlexBoxLayout content;

  public DefaultMasterDetailsView(
      String I18N_PREFIX, Class<T> entityType, MyI18NProvider myI18NProvider) {
    this(I18N_PREFIX, entityType, null, null, null, myI18NProvider);
  }

  public DefaultMasterDetailsView(
      String I18N_PREFIX,
      Class<T> entityType,
      DefaultDataProvider<T, F> dataProvider,
      MyI18NProvider myI18NProvider) {
    this(I18N_PREFIX, entityType, dataProvider, null, null, myI18NProvider);
  }

  public DefaultMasterDetailsView(
      String I18N_PREFIX,
      Class<T> entityType,
      Function<T, ServiceResult<T>> saveHandler,
      Function<T, ServiceResult<Void>> deleteHandler,
      MyI18NProvider myI18NProvider) {
    this(I18N_PREFIX, entityType, null, true, saveHandler, deleteHandler, myI18NProvider);
  }

  public DefaultMasterDetailsView(
      String I18N_PREFIX,
      Class<T> entityType,
      DefaultDataProvider<T, F> dataProvider,
      Function<T, ServiceResult<T>> saveHandler,
      Function<T, ServiceResult<Void>> deleteHandler,
      MyI18NProvider myI18NProvider) {
    this(I18N_PREFIX, entityType, dataProvider, true, saveHandler, deleteHandler, myI18NProvider);
  }

  public DefaultMasterDetailsView(
      String I18N_PREFIX,
      Class<T> entityType,
      DefaultDataProvider<T, F> dataProvider,
      Boolean initialFetch,
      Function<T, ServiceResult<T>> saveHandler,
      Function<T, ServiceResult<Void>> deleteHandler,
      MyI18NProvider myI18NProvider) {
    this.I18N_PREFIX = I18N_PREFIX;
    this.entityType = entityType;
    this.binder = new BeanValidationBinder<>(entityType);
    this.dataProvider = dataProvider;
    this.initialFetch = initialFetch;
    this.saveHandler = saveHandler;
    this.deleteHandler = deleteHandler;
    this.myI18NProvider = myI18NProvider;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    initHeader();
    setViewContent(createContent());
    setViewDetails(createDetailsDrawer());

    if (initialFetch) {
      filter(null, false);
    }

    if (currentEditing != null) {
      showDetails(currentEditing);
    }
  }

  @Override
  public boolean displayInANewTab() {
    return false;
  }

  @Override
  public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {
    ContinueNavigationAction action = beforeLeaveEvent.postpone();
    checkForDetailsChanges(action::proceed);
  }

  protected void checkForDetailsChanges(Runnable action) {
    if (currentEditing != null && this.binder.hasChanges()) {
      ConfirmDialog.createQuestion()
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
          if ( displayInANewTab() ) {
            getParentTab().closeTab();
            return;
          }
          if (super.getGoBackListener() != null) super.getGoBackListener().goBack();
          else super.getMenuBackListener().goBack();
        });
    moduleToolbar.setSearchTextFieldVisible(isShowSearchTextField());
    moduleToolbar.addSearchTextChangedListener(
        searchText -> filter(searchText, moduleToolbar.getActiveFilterValue()));

    moduleToolbar.setShowInactiveButtonVisible(isShowInactiveFilter());
    moduleToolbar.addShowInactiveChangedListener(
        showInactive -> filter(moduleToolbar.getSearchText(), showInactive));

    moduleToolbar.setFilterButtonVisible(false);

    if (canCreateRecord() && saveHandler != null) {
      moduleToolbar.setAddButtonVisible(true);
      moduleToolbar.addNewRecordListener(this::showDetails);
    }
    if (dataProvider == null)
      moduleToolbar.addRefreshListener(
          () -> {
            grid.getLazyDataView().refreshAll();
          });
    else moduleToolbar.addRefreshListener(dataProvider::refreshAll);

    setViewHeader(moduleToolbar);
  }

  protected void disableCreateRecord() {
    moduleToolbar.setAddButtonVisible( false );
  }

  protected void enableCreateRecord() {
    moduleToolbar.setAddButtonVisible( true );
  }

  protected void showDetails() {
    try {
      showDetails(entityType.getDeclaredConstructor().newInstance());
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException ignored) {
    }
  }

  protected boolean canCreateRecord() {
    return true;
  }

  protected boolean canSave() {
    return true;
  }

  protected boolean canDelete() {
    return true;
  }

  protected boolean isGlobalSearchEnabled() {
    return false;
  }

  protected boolean isShowInactiveFilter() {
    return true;
  }

  protected boolean isShowSearchTextField() {
    return true;
  }

  protected Function<S, ClickNotifier> getSearchResultDataProvider() {
    return s -> new ClickableCard();
  }

  protected DefaultSearchDataProvider<S, Q> getSearchDataProvider() {
    return null;
  }

  protected Function<String, Query<S, Q>> getSearchQueryProvider() {
    return s -> new Query(DefaultFilter.getEmptyFilter());
  }

  public void enableSaveButton() {
    detailsDrawerFooter.setSaveButtonVisible(true);
  }

  public void disableSaveButton() {
    detailsDrawerFooter.setSaveButtonVisible(false);
  }

  public void enableDeleteButton() {
    detailsDrawerFooter.setDeleteButtonVisible(true);
  }

  public void disableDeleteButton() {
    detailsDrawerFooter.setDeleteButtonVisible(false);
  }

  private Component createContent() {
    content = new FlexBoxLayout(createGrid());
    content.setBoxSizing(BoxSizing.BORDER_BOX);
    content.setHeightFull();
    content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

    Label nbRows = UIUtils.createH4Label(getTranslation("element.global.nbRows", 0));
    if (dataProvider == null)
      grid.getLazyDataView()
          .addItemCountChangeListener(
              event ->
                  nbRows.setText(getTranslation("element.global.nbRows", event.getItemCount())));
    else
      dataProvider.setPageObserver(
          tPage ->
              nbRows.setText(getTranslation("element.global.nbRows", tPage.getTotalElements())));

    FooterRow footerRow = grid.appendFooterRow();
    footerRow.getCell(grid.getColumns().get(0)).setComponent(nbRows);
    return content;
  }

  protected void addToContent(Component component) {
    content.add(component);
  }

  protected abstract Grid createGrid();

  protected DetailsDrawer.Position getDetailsDrawerPosition() {
    return DetailsDrawer.Position.RIGHT;
  }

  protected SplitViewFrame.Position getSplitViewFramePosition() {
    return SplitViewFrame.Position.RIGHT;
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

  private DetailsDrawer createDetailsDrawer() {
    detailsDrawer = new DetailsDrawer(getDetailsDrawerPosition());
    setViewDetailsPosition(getSplitViewFramePosition());
    // Header

    tabs = buildTabs();

    detailsDrawerHeader =
        new DetailsDrawerHeader(getTranslation("element." + I18N_PREFIX + "className"), tabs);
    detailsDrawerHeader.addCloseListener(
        e ->
            checkForDetailsChanges(
                () -> {
                  detailsDrawer.hide();
                  currentEditing = null;
                }));
    detailsDrawer.setHeader(detailsDrawerHeader);

    // Footer
    detailsDrawerFooter = new DetailsDrawerFooter();
    if (saveHandler == null || !canSave()) {
      detailsDrawerFooter.setSaveButtonVisible(false);
      detailsDrawerFooter.setSaveAndNewButtonVisible(false);
    }
    if (deleteHandler == null || !canDelete()) {
      detailsDrawerFooter.setDeleteButtonVisible(false);
    }

    detailsDrawerFooter.addCancelListener(
        e -> {
          detailsDrawer.hide();
          currentEditing = null;
        });
    if (saveHandler != null && canSave()) {
      detailsDrawerFooter.addSaveListener(e -> save(false));
      detailsDrawerFooter.addSaveAndNewListener(e -> save(true));
    }
    if (deleteHandler != null && canDelete()) {
      detailsDrawerFooter.addDeleteListener(e -> delete());
    }
    detailsDrawer.setFooter(detailsDrawerFooter);

    return detailsDrawer;
  }

  protected void showDetails(T entity) {
    if (detailsDrawerFooter != null) {
      checkForDetailsChanges(
          () -> {
            if (entity.getId() != null) {
              detailsDrawerFooter.setSaveAndNewButtonVisible(false);
            } else {
              if (saveHandler != null && canSave()) {
                detailsDrawerFooter.setSaveAndNewButtonVisible(true);
              }
            }
            this.binder = new BeanValidationBinder<>(entityType);

            currentEditing = entity;
            detailsDrawer.setContent(createDetails(entity));
            detailsDrawer.show();
            tabs.setSelectedIndex(0);
          });
    }
  }

  protected abstract Component createDetails(T entity);

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
    binder.bind(isActiveField, BaseEntity::getIsActive, BaseEntity::setIsActive);
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

    binder.readBean(entity);

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
        if (result.getIsSuccess() && result.getData() != null) {
          currentEditing = result.getData();
        } else {
          JHapyMainView3.get().displayErrorMessage(result);
          return;
        }
      }
      afterSave(currentEditing);

      JHapyMainView3.get().displayInfoMessage(getTranslation("message.global.recordSavedMessage"));

      if (!isNew) {
        dataProvider.refreshItem(currentEditing);
      } else {
        dataProvider.refreshAll();
      }

      if (saveAndNew) {
        showDetails();
        return;
      }

      showDetails(currentEditing);
    } else {
      BinderValidationStatus<T> validate = binder.validate();
      String errorText =
          validate.getFieldValidationStatuses().stream()
              .filter(BindingValidationStatus::isError)
              .map(BindingValidationStatus::getMessage)
              .map(Optional::get)
              .distinct()
              .collect(Collectors.joining(", "));

      Notification.show(
          getTranslation("message.global.validationErrorMessage", errorText),
          3000,
          Notification.Position.BOTTOM_CENTER);
    }
  }

  public void delete() {
    ConfirmDialog.create()
        .withCaption(getTranslation("message.global.confirmDelete.title"))
        .withMessage(getTranslation("message.global.confirmDelete.message"))
        .withOkButton(this::deleteConfirmed, ButtonOption.focus(), ButtonOption.caption("YES"))
        .withCancelButton(ButtonOption.caption("NO"))
        .open();
  }

  private void deleteConfirmed() {
    if (beforeDelete(currentEditing)) {
      ServiceResult<Void> result = deleteHandler.apply(currentEditing);
      if (!result.getIsSuccess()) {
        JHapyMainView3.get().displayErrorMessage(result);
      }
    }

    afterDelete();

    detailsDrawer.hide();

    if (dataProvider == null)
      moduleToolbar.addRefreshListener(
          () -> {
            grid.getLazyDataView().refreshAll();
          });
    else moduleToolbar.addRefreshListener(dataProvider::refreshAll);

    currentEditing = null;
  }

  protected void filter(String filter, Boolean showInactive) {
    if (dataProvider != null)
      dataProvider.setFilter(
          (F) new DefaultFilter(StringUtils.isBlank(filter) ? null : filter, showInactive));
    else {
      throw new RuntimeException(
          "A dataprovider is need or you need to provide custom filter query method");
    }
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