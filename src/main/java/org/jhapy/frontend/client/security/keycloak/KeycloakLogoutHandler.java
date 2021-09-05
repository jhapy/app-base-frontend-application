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

package org.jhapy.frontend.client.security.keycloak;

import lombok.RequiredArgsConstructor;
import org.jhapy.commons.utils.HasLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/05/2020
 */
@Component
@RequiredArgsConstructor
public class KeycloakLogoutHandler extends SecurityContextLogoutHandler implements HasLogger {

  private final RestTemplate restTemplate;

  @Override
  public void logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    super.logout(request, response, authentication);

    propagateLogoutToKeycloak((OidcUser) authentication.getPrincipal());
  }

  private void propagateLogoutToKeycloak(OidcUser user) {
    var loggerPrefix = getLoggerPrefix("propagateLogoutToKeycloak");
    String endSessionEndpoint = user.getIssuer() + "/protocol/openid-connect/logout";

    UriComponentsBuilder builder =
        UriComponentsBuilder //
            .fromUriString(endSessionEndpoint) //
            .queryParam("id_token_hint", user.getIdToken().getTokenValue());

    ResponseEntity<String> logoutResponse =
        restTemplate.getForEntity(builder.toUriString(), String.class);
    if (logoutResponse.getStatusCode().is2xxSuccessful()) {
      logger().info(loggerPrefix + "Successfully logged out in Keycloak");
    } else {
      logger().info(loggerPrefix + "Could not propagate logout to Keycloak");
    }
  }
}