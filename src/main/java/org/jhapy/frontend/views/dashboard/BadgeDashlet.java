package org.jhapy.frontend.views.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import org.jhapy.frontend.components.navigation.menubar.View;

public abstract class BadgeDashlet extends Dashlet {
  private Span descriptionSpan;
  private Span titleSpan;
  private H2 h2;

  public BadgeDashlet(View parentView) {
    super(parentView);
    initContent();
  }

  @Override
  protected Component[] getComponents() {
    return new Component[] {titleSpan, h2, descriptionSpan};
  }

  @Override
  protected String getDashletClassName() {
    return "wrapper";
  }

  @Override
  protected String[] getClasses() {
    return new String[] {"card", "p-m"};
  }

  protected abstract String getDashletTitle();

  protected abstract String getDashletContent();

  protected abstract String getDashletDescription();

  protected abstract BadgeDashletStatusEnum getDashletStatus();

  protected void initContent() {

    titleSpan = new Span(getDashletTitle());
    h2 = new H2();
    if (getDashletContent() != null) h2.setText(getDashletContent());
    if (getDashletStatus().equals(BadgeDashletStatusEnum.REGULAR)) {
      titleSpan.getElement().setAttribute("theme", "badge");
      h2.addClassName("primary-text");
    } else if (getDashletStatus().equals(BadgeDashletStatusEnum.SUCCESS)) {
      titleSpan.getElement().setAttribute("theme", "badge success");
      h2.addClassName("success-text");
    } else if (getDashletStatus().equals(BadgeDashletStatusEnum.ERROR)) {
      titleSpan.getElement().setAttribute("theme", "badge error");
      h2.addClassName("error-text");
    }

    descriptionSpan = new Span();
    if (getDashletDescription() != null) descriptionSpan.setText(getDashletDescription());
    descriptionSpan.addClassName("secondary-text");
  }
}
