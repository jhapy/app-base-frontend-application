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

package org.jhapy.frontend.security;

import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.ApplicationConstants;
import de.codecamp.vaadin.security.spring.access.VaadinSecurity;
import de.codecamp.vaadin.security.spring.access.rules.PermitAll;
import de.codecamp.vaadin.security.spring.access.rules.RequiresRole;
import org.jhapy.dto.domain.security.SecurityUser;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;

import static org.jhapy.frontend.utils.AppConst.SECURITY_USER_ATTRIBUTE;
import static org.jhapy.frontend.utils.AppConst.SECURITY_USER_ID_ATTRIBUTE;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

/**
 * SecurityUtils takes care of all such static operations that have to do with security and querying
 * rights from different beans of the UI.
 *
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
public final class SecurityUtils {

  private SecurityUtils() {
    // Util methods only
  }
  /**
   * Tests if the request is an internal framework request. The test consists of checking if the
   * request parameter is present and if its value is consistent with any of the request types know.
   *
   * @param request {@link HttpServletRequest}
   * @return true if is an internal framework request. False otherwise.
   */
  public static boolean isFrameworkInternalRequest(HttpServletRequest request) {
    final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
    return parameterValue != null
        && Stream.of(HandlerHelper.RequestType.values())
            .anyMatch(r -> r.getIdentifier().equals(parameterValue));
  }

  public static Optional<String> getCurrentUserLogin() {
    if (VaadinSecurity.check().isAuthenticated()) {
      return Optional.of(VaadinSecurity.getAuthentication().getName());
    } else {
      return Optional.empty();
    }
  }

  public static SecurityUser getSecurityUser() {
    SecurityContext context = SecurityContextHolder.getContext();
    if (!isUserLoggedIn()) {
      return null;
    }
    if (VaadinSession.getCurrent() != null
        && VaadinSession.getCurrent().getAttribute(SECURITY_USER_ATTRIBUTE) != null) {
      return (SecurityUser) VaadinSession.getCurrent().getAttribute(SECURITY_USER_ATTRIBUTE);
    }
    Object principal = context.getAuthentication().getPrincipal();
    if (principal instanceof DefaultOidcUser) {
      Map<String, Object> attributes = ((DefaultOidcUser) principal).getAttributes();
      SecurityUser securityUser = new SecurityUser();
      securityUser.setEmail(attributes.get("email").toString());
      securityUser.setFirstName(attributes.get("given_name").toString());
      securityUser.setLastName(attributes.get("family_name").toString());
      securityUser.setUsername(attributes.get("preferred_username").toString());
      securityUser.setId(attributes.get("sub"));
      if (VaadinSession.getCurrent() != null) {
        VaadinSession.getCurrent().setAttribute(SECURITY_USER_ATTRIBUTE, securityUser);
        VaadinSession.getCurrent().setAttribute(SECURITY_USER_ID_ATTRIBUTE, securityUser.getId());
      }
      return securityUser;
    } else if (principal instanceof SecurityUser) {
      return (SecurityUser) principal;
    }
    // Anonymous or no authentication.
    return null;
  }

  /**
   * Checks if access is granted for the current user for the given secured view, defined by the
   * view class.
   *
   * @param viewClass View class
   * @return true if access is granted, false otherwise.
   */
  public static boolean isAccessGranted(Class<?> viewClass) {
    // Always allow access to public views
    PermitAll permitAll = AnnotationUtils.findAnnotation(viewClass, PermitAll.class);
    if (permitAll != null) {
      return true;
    }

    // All other views require authentication
    if (!VaadinSecurity.check().isAuthenticated()) {
      return false;
    }

    // Allow if no roles are required.
    Secured secured = AnnotationUtils.findAnnotation(viewClass, Secured.class);
    RequiresRole requiresRole = AnnotationUtils.findAnnotation(viewClass, RequiresRole.class);
    if (secured == null && requiresRole == null) {
      return true;
    }

    List<String> allowedRoles = new ArrayList<>();
    if (secured != null) {
      allowedRoles.addAll(Arrays.asList(secured.value()));
    }
    if (requiresRole != null) {
      allowedRoles.addAll(Arrays.asList(requiresRole.value()));
    }

    return VaadinSecurity.check().getAuthentication().getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(allowedRoles::contains);
  }

  /**
   * Checks if the user is logged in.
   *
   * @return true if the user is logged in. False otherwise.
   */
  public static boolean isUserLoggedIn() {
    return VaadinSecurity.check().isAuthenticated();
  }

  public static void newSession(SecurityUser securityUser) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                securityUser, null, securityUser.getAuthorities()));

    VaadinSession.getCurrent()
        .getSession()
        .setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    VaadinSession.getCurrent().setAttribute(SECURITY_USER_ATTRIBUTE, securityUser);

    // TODO: Fix that
    /*

    AuditServices.getAuditServiceQueue()
        .newSession(
            new NewSession(
                VaadinRequest.getCurrent().getWrappedSession().getId(),
                securityUser.getUsername(),
                VaadinRequest.getCurrent().getRemoteAddr(),
                Instant.now(),
                true,
                null));

     */
  }

  public static boolean hasRole(String role) {
    return VaadinSecurity.hasAccess(MessageFormat.format("hasRole(''{0}'')", role));
  }
}
