package org.jhapy.frontend.security;

import de.codecamp.vaadin.security.spring.authentication.AuthenticationResult;
import de.codecamp.vaadin.security.spring.authentication.AuthenticationResultHandler;
import de.codecamp.vaadin.security.spring.authentication.VaadinAuthenticationService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * You can register multiple {@link AuthenticationResultHandler AuthenticationResultHandlers}. Once
 * {@code true} is returned, no further handlers are consulted. The handlers are ordered using the
 * standard Spring machanism of {@link Order} and/or {@link Ordered}. The default built-in handler
 * (redirecting to main view after successful authentication etc.) has the lowest precedence.
 * Handlers registered like this are always considered after the handler passed to {@link
 * VaadinAuthenticationService#login(com.vaadin.flow.component.Component, String, String, boolean,
 * AuthenticationResultHandler)}.
 */
@Component
public class CustomAuthenticationResultHandler implements AuthenticationResultHandler {

  @Override
  public boolean handleAuthenticationResult(AuthenticationResult result) {
    // Return true if you want no further handling.

    return false;
  }
}