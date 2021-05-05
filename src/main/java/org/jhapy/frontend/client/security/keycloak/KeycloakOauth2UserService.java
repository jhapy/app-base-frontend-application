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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.CollectionUtils;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/05/2020
 */

@RequiredArgsConstructor
public class KeycloakOauth2UserService extends OidcUserService {

  private final OAuth2Error INVALID_REQUEST = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);

  private final JwtDecoder jwtDecoder;

  private final GrantedAuthoritiesMapper authoritiesMapper;

  /**
   * Augments {@link OidcUserService#loadUser(OidcUserRequest)} to add authorities provided by
   * Keycloak.
   *
   * Needed because {@link OidcUserService#loadUser(OidcUserRequest)} (currently) does not provide a
   * hook for adding custom authorities from a {@link OidcUserRequest}.
   */
  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

    OidcUser user = super.loadUser(userRequest);

    Set<GrantedAuthority> authorities = new LinkedHashSet<>();
    authorities.addAll(user.getAuthorities());
    authorities.addAll(extractKeycloakAuthorities(userRequest));

    return new DefaultOidcUser(authorities, userRequest.getIdToken(), user.getUserInfo(),
        "preferred_username");
  }

  /**
   * Extracts {@link GrantedAuthority GrantedAuthorities} from the AccessToken in the {@link
   * OidcUserRequest}.
   */
  private Collection<? extends GrantedAuthority> extractKeycloakAuthorities(
      OidcUserRequest userRequest) {

    Jwt token = parseJwt(userRequest.getAccessToken().getTokenValue());

    // Would be great if Spring Security would provide something like a plugable
    // OidcUserRequestAuthoritiesExtractor interface to hide the junk below...

    Map<String, Object> claims = token.getClaims();
    Map<String, Object> resourceMap = (Map<String, Object>) claims.get("resource_access");
    final Map<String, Object> realmAccess = (Map<String, Object>) token.getClaims()
        .get("realm_access");
    String clientId = userRequest.getClientRegistration().getClientId();

    @SuppressWarnings("unchecked")
    Map<String, Map<String, Object>> clientResource = (Map<String, Map<String, Object>>) resourceMap
        .get(clientId);
    if (!CollectionUtils.isEmpty(clientResource)) {
      List<String> clientRoles = (List<String>) clientResource.get("roles");
      if (CollectionUtils.isEmpty(clientRoles)) {
        return Collections.emptyList();
      }
      Collection<? extends GrantedAuthority> authorities = AuthorityUtils
          .createAuthorityList(clientRoles.toArray(new String[0]));
      if (authoritiesMapper == null) {
        return authorities;
      }

      return authoritiesMapper.mapAuthorities(authorities);
    } else {
      Collection<String> roles = (Collection<String>) claims.getOrDefault("groups",
          claims.getOrDefault("roles", realmAccess.getOrDefault("roles", new ArrayList<>())));

      return roles.stream()
          .filter(role -> role.startsWith("ROLE_"))
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList());
    }
  }

  private Jwt parseJwt(String accessTokenValue) {
    try {
      // Token is already verified by spring security infrastructure
      return jwtDecoder.decode(accessTokenValue);
    } catch (JwtException e) {
      throw new OAuth2AuthenticationException(INVALID_REQUEST, e);
    }
  }
}
