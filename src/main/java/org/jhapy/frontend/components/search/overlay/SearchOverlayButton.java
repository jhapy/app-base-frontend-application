package org.jhapy.frontend.components.search.overlay;

import com.github.appreciated.app.layout.component.appbar.IconButton;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.Query;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.frontend.components.search.overlay.SearchOverlayView.SearchFilter;
import org.jhapy.frontend.dataproviders.DefaultSearchDataProvider;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.io.Serial;
import java.util.function.Consumer;
import java.util.function.Function;

public class SearchOverlayButton<T extends SearchQueryResult, F extends SearchQuery>
    extends IconButton {

  @Serial private static final long serialVersionUID = 1L;

  private final SearchOverlayView<T, F> searchView;

  public SearchOverlayButton() {
    this(VaadinIcon.SEARCH);
  }

  public SearchOverlayButton(VaadinIcon icon) {
    this(icon.create());
  }

  public SearchOverlayButton(Component icon) {
    super(icon);
    searchView = new SearchOverlayView<>();
    addClickListener(event -> searchView.open());
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    searchView.getElement().removeFromParent();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    attachEvent.getUI().add(searchView);
  }

  public SearchOverlayView<T, F> getSearchView() {
    return searchView;
  }

  public void setDataViewProvider(Function<T, ClickNotifier> provider) {
    this.searchView.setDataViewProvider(provider);
  }

  public void setDataProvider(DefaultSearchDataProvider<T, F> dataProvider) {
    this.searchView.setDataProvider(dataProvider);
  }

  public void setQueryProvider(Function<SearchOverlayView.SearchQuery, Query<T, F>> query) {
    this.searchView.setQueryProvider(query);
  }

  public void setQueryResultListener(Consumer<T> queryResultListener) {
    this.searchView.setQueryResultListener(queryResultListener);
  }

  public void setCloseOnQueryResult(boolean closeOnQueryResult) {
    this.searchView.setCloseOnQueryResult(closeOnQueryResult);
  }

  public <T> MultiselectComboBox<SearchFilter> getSearchFilter() {
    return this.searchView.getFilter();
  }
}