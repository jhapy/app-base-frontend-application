package org.jhapy.frontend.config;

import org.jhapy.commons.utils.HasLogger;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 03/11/2020
 */
public class CustomOAuth2AuthorizationRequestResolver
    implements OAuth2AuthorizationRequestResolver, HasLogger {

  private final OAuth2AuthorizationRequestResolver defaultResolver;
  private final boolean forceHttps;

  public CustomOAuth2AuthorizationRequestResolver(
      ClientRegistrationRepository repo, String authorizationRequestBaseUri, boolean forceHttps) {
    this.defaultResolver =
        new DefaultOAuth2AuthorizationRequestResolver(repo, authorizationRequestBaseUri);
    this.forceHttps = forceHttps;
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    var req = defaultResolver.resolve(request);
    if (req != null) {
      req = customizeAuthorizationRequest(req);
    }
    return req;
  }

  @Override
  public OAuth2AuthorizationRequest resolve(
      HttpServletRequest request, String clientRegistrationId) {
    var req = defaultResolver.resolve(request, clientRegistrationId);
    if (req != null) {
      req = customizeAuthorizationRequest(req);
    }
    return req;
  }

  private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest req) {
    var loggerPrefix = getLoggerPrefix("customizeAuthorizationRequest", forceHttps);
    debug(loggerPrefix, "Initial Redirect URI = {0}", req.getRedirectUri());
    if (forceHttps) {
      URI uri = null;
      try {
        uri = new URI(req.getRedirectUri());
        String newRedirectUri = "https://" + uri.getAuthority() + uri.getPath();
        debug(loggerPrefix, "New redirect URI = {0}", newRedirectUri);
        return OAuth2AuthorizationRequest.from(req).redirectUri(newRedirectUri).build();
      } catch (URISyntaxException e) {
        error(loggerPrefix, e, "Unexpected error : {0}", e.getMessage());
      }
    }
    return OAuth2AuthorizationRequest.from(req).build();
  }
}
