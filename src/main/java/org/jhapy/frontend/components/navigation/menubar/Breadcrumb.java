package org.jhapy.frontend.components.navigation.menubar;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import org.jhapy.frontend.utils.TextColor;
import org.jhapy.frontend.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

@CssImport("./menubar/breadcrumbs.css")
public class Breadcrumb extends Div {
  private List<Component> breadcrumbs = new ArrayList<>();

  public Breadcrumb() {
    addClassName("breadcrumbs");
    setVisible(false);
  }

  public void init() {
    removeAll();
    for (var i = 0; i < breadcrumbs.size(); i++) {
      add(breadcrumbs.get(i));
      if ((i + 1) < breadcrumbs.size()) {
        add(new Spacer());
        add(UIUtils.createLabel(TextColor.PRIMARY, ">"));
        add(new Spacer());
      }
    }
    setVisible(!breadcrumbs.isEmpty());
  }

  public void setBreadcrumbs(List<Component> breadcrumbs) {
    this.breadcrumbs = breadcrumbs;
    UI.getCurrent().access(this::init);
  }

  public List<Component> getBreadcrumbs() {
    return breadcrumbs;
  }

  public void push(Component breadcrumb) {
    breadcrumbs.add(breadcrumb);
    UI.getCurrent().access(this::init);
  }

  public void pull() {
    breadcrumbs.remove(breadcrumbs.size() - 1);
    UI.getCurrent().access(this::init);
  }
}