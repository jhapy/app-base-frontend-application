package org.jhapy.frontend.views.admin.swagger;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.shared.Registration;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Vertical;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 17/09/2020
 */
@I18NPageTitle(messageKey = AppConst.TITLE_SWAGGER_ADMIN)
@RequiresRole(SecurityConst.ROLE_SWAGGER)
public class SwaggerAdminView extends ViewFrame implements HasUrlParameter<String> {

  private final Registration contextIconRegistration = null;
  private IFrame swaggerView;
  private String appName;
  private String swaggerUrl;

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // JHapyMainView3.get().getAppBar().setTitle(appName);
    setViewContent(createContent());
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    if (contextIconRegistration != null) {
      contextIconRegistration.remove();
    }
  }

  private Component createContent() {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
    content.setSizeFull();

    swaggerView = new IFrame(swaggerUrl);
    swaggerView.setSizeFull();
    content.add(swaggerView);

    return content;
  }

  @Override
  public void setParameter(BeforeEvent event, String viewParameters) {
    super.setParameter(event, viewParameters);
    if (StringUtils.isNoneBlank(viewParameters)) {
      String url =
          ((VaadinServletRequest) VaadinService.getCurrentRequest()).getRequestURL().toString();

      swaggerUrl = url + "swagger/" + viewParameters + "/swagger-ui.html";
      this.appName = viewParameters;
    }
  }
}