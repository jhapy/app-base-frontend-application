package org.jhapy.frontend.exceptions;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import de.codecamp.vaadin.security.spring.access.rules.PermitAll;
import org.jhapy.frontend.layout.ViewFrame;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 08/05/2020
 */
@PermitAll
// @ParentLayout(JHapyMainView3.class)
public class AccessDeniedExceptionHandler extends ViewFrame
    implements HasErrorParameter<AccessDeniedException> {

  @Override
  public int setErrorParameter(
      BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
    setViewContent(new Label("Tried to navigate to a view without " + "correct access rights"));
    return HttpServletResponse.SC_FORBIDDEN;
  }
}
