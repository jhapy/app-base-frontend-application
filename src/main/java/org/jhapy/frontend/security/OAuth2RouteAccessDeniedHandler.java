package org.jhapy.frontend.security;

import com.vaadin.flow.router.BeforeEnterEvent;
import de.codecamp.vaadin.security.spring.access.VaadinSecurity;
import de.codecamp.vaadin.security.spring.access.route.RouteAccessDeniedHandler;
import de.codecamp.vaadin.security.spring.autoconfigure.VaadinSecurityProperties;
import de.codecamp.vaadin.security.spring.util.RedirectView;
import org.jhapy.frontend.config.SecurityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OAuth2RouteAccessDeniedHandler implements RouteAccessDeniedHandler {

  private static final Logger LOG = LoggerFactory.getLogger(OAuth2RouteAccessDeniedHandler.class);

  private final String uiRootUrl;

  public OAuth2RouteAccessDeniedHandler(VaadinSecurityProperties vsp) {
    uiRootUrl = vsp.getUiRootUrl();
  }

  @Override
  public void handleAccessDenied(BeforeEnterEvent event) {
    if (!VaadinSecurity.check().isFullyAuthenticated()) {
      String originalTarget = event.getLocation().getPathWithQueryParameters();
      if (originalTarget.equals(".")) {
        originalTarget = "";
      }

      VaadinOAuth2RequestCache.saveOriginalTargetUrl(
          event.getUI().getSession(), uiRootUrl + "/" + originalTarget);

      LOG.debug("Redirecting to OAuth2 login at '{}'.", SecurityConfiguration.LOGIN_URL);

      RedirectView.redirectToUrl(event, SecurityConfiguration.LOGIN_URL);
      // RedirectView.redirectToUrl(event, WebSecurityConfig.LOGIN_URL);

      return;
    }
  }
}