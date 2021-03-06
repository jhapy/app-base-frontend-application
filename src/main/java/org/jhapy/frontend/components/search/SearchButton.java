package org.jhapy.frontend.components.search;

import com.github.appreciated.app.layout.component.appbar.IconButton;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import java.io.Serial;

public class SearchButton extends IconButton {

  @Serial
  private static final long serialVersionUID = 1L;

  private final SearchView searchView;

  public SearchButton() {
    this(VaadinIcon.SEARCH);
  }

  public SearchButton(VaadinIcon icon) {
    this(icon.create());
  }

  public SearchButton(Component icon) {
    super(icon);
    searchView = new SearchView();
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

  public SearchButton withValueChangeListener(
      HasValue.ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<TextField, String>> listener) {
    addValueChangeListener(listener);
    return this;
  }

  public void addValueChangeListener(
      HasValue.ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<TextField, String>> listener) {
    searchView.addValueChangeListener(listener);
  }
}
