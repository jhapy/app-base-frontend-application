package org.jhapy.frontend.components.search.overlay;

import com.github.appreciated.app.layout.addons.search.overlay.QueryPair;
import com.github.appreciated.app.layout.component.appbar.IconButton;
import com.github.appreciated.ironoverlay.HorizontalOrientation;
import com.github.appreciated.ironoverlay.IronOverlay;
import com.github.appreciated.ironoverlay.VerticalOrientation;
import com.vaadin.componentfactory.MultipleSelect;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.dataproviders.DefaultSearchDataProvider;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Right;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.layout.size.Vertical;
import org.jhapy.frontend.utils.IconSize;
import org.jhapy.frontend.utils.TextColor;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BorderRadius;
import org.jhapy.frontend.utils.css.Overflow;

import java.io.Serial;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@CssImport("./styles/components/search.css")
public class SearchOverlayView<T extends SearchQueryResult, F extends SearchQuery>
    extends IronOverlay {

  @Serial private static final long serialVersionUID = 1L;
  private final TextField searchField = new TextField();
  private final IconButton closeButton = new IconButton(VaadinIcon.ARROW_LEFT.create());
  private final VerticalLayout results = new VerticalLayout();
  private final MultipleSelect<SearchFilter> filter;
  private final FlexBoxLayout searchResult = new FlexBoxLayout();
  private Function<T, ClickNotifier> dataViewProvider;
  private DefaultSearchDataProvider<T, F> dataProvider;
  private Function<SearchQuery, Query<T, F>> queryProvider;
  private Consumer<T> queryResultListener;
  private boolean closeOnQueryResult = true;

  public SearchOverlayView() {
    getElement().getStyle().set("width", "100%");
    setVerticalAlign(VerticalOrientation.TOP);
    setHorizontalAlign(HorizontalOrientation.CENTER);

    VaadinIcon search = VaadinIcon.SEARCH;
    search.create().addClassNames("size-l");

    Button searchIcon =
        new Button(UIUtils.createIcon(IconSize.M, TextColor.PRIMARY, VaadinIcon.SEARCH));
    searchIcon.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

    filter = new MultipleSelect<>();
    filter.setPlaceholder(getTranslation("element.search.filterPlaceholder"));
    filter.setItemLabelGenerator(SearchFilter::getTitle);
    filter.addValueChangeListener(event -> doSearch(searchField.getValue(), event.getValue()));

    searchField.setPlaceholder(getTranslation("element.search.placeholder"));
    searchField.setValueChangeMode(ValueChangeMode.LAZY);
    Button closeButton =
        new Button(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CLOSE));
    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

    FlexBoxLayout searchFieldWrapper =
        new FlexBoxLayout(searchIcon, filter, searchField, closeButton);
    searchFieldWrapper.setClassName("search-layout");
    searchFieldWrapper.setFlex("1", searchField);
    searchFieldWrapper.setPadding(Horizontal.RESPONSIVE_L, Vertical.S);
    searchFieldWrapper.setSpacing(Right.M);
    searchFieldWrapper.setFlexWrap(FlexWrap.WRAP);
    searchFieldWrapper.setJustifyContentMode(JustifyContentMode.EVENLY);
    searchFieldWrapper.setMargin(Top.M);
    searchFieldWrapper.setBorderRadius(BorderRadius.M);
    searchFieldWrapper.setBackgroundColor("var(--lumo-base-color)");

    searchResult.setClassName("search-result");
    searchResult.setFlexDirection(FlexDirection.COLUMN);
    searchResult.setOverflow(Overflow.AUTO);

    searchField.addValueChangeListener(event -> doSearch(event.getValue(), filter.getValue()));
    searchField.setMinWidth("50%");

    closeButton.addClickListener(
        event -> {
          searchField.clear();
          close();
        });

    VerticalLayout wrapper = new VerticalLayout(searchFieldWrapper, searchResult);

    wrapper.setSizeFull();
    wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
    wrapper.setMargin(false);
    wrapper.setPadding(false);
    wrapper.setSpacing(false);
    wrapper.getStyle().set("max-width", "100vw").set("height", "100vh");

    /*
    results.setSizeFull();
    results.setMargin(false);
    results.getStyle().set("overflow", "auto");
    results.getStyle()
        .set("overflow-y", "auto")
        .set("max-width", "100%")
        .set("min-width", "40%")
        .set("--lumo-size-m", "var(--lumo-size-xl)")
        .set("--lumo-contrast-10pct", "transparent");
    results.setHeightFull();
    //results.setWidth("unset");

     */
    add(wrapper);
  }

  private void doSearch(String filter, Set<SearchFilter> searchFilters) {
    searchResult.removeAll();
    if (StringUtils.isNotBlank(filter)) {
      List<T> result =
          dataProvider
              .fetch(queryProvider.apply(new SearchQuery(filter, searchFilters)))
              .collect(Collectors.toList());
      result.stream()
          .map(t -> new QueryPair<>(t, dataViewProvider.apply(t)))
          .forEach(
              clickNotifier -> {
                searchResult.add((Component) clickNotifier.getNotifier());
                clickNotifier
                    .getNotifier()
                    .addClickListener(
                        clickEvent -> {
                          if (closeOnQueryResult) {
                            this.close();
                          }
                          if (queryResultListener != null) {
                            queryResultListener.accept(clickNotifier.getQuery());
                          }
                        });
              });
    }
  }

  @Override
  public void open() {
    super.open();
    searchField.focus();
  }

  public Function<T, ClickNotifier> getDataViewProvider() {
    return dataViewProvider;
  }

  public void setDataViewProvider(Function<T, ClickNotifier> dataViewProvider) {
    this.dataViewProvider = dataViewProvider;
  }

  public DataProvider<T, F> getDataProvider() {
    return dataProvider;
  }

  public void setDataProvider(DefaultSearchDataProvider<T, F> dataProvider) {
    this.dataProvider = dataProvider;
  }

  public VerticalLayout getResults() {
    return results;
  }

  public TextField getSearchField() {
    return searchField;
  }

  public void setQueryProvider(Function<SearchQuery, Query<T, F>> queryProvider) {
    this.queryProvider = queryProvider;
  }

  public void setQueryResultListener(Consumer<T> queryResultListener) {
    this.queryResultListener = queryResultListener;
  }

  public void setCloseOnQueryResult(boolean closeOnQueryResult) {
    this.closeOnQueryResult = closeOnQueryResult;
  }

  public Button getCloseButton() {
    return closeButton;
  }

  public void setTargetComponent(Div content) {}

  public MultipleSelect<SearchFilter> getFilter() {
    return filter;
  }

  @Data
  public static class SearchFilter {

    private String title;

    public SearchFilter(String title) {
      this.title = title;
    }
  }

  @Data
  public static class SearchQuery {

    private String filter;
    private Set<SearchFilter> searchFilters;

    public SearchQuery(String filter, Set<SearchFilter> searchFilters) {
      this.filter = filter;
      this.searchFilters = searchFilters;
    }
  }
}
