package org.jhapy.frontend.security;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * There's no actual HTTP request for the original target view because that's just how navigation in
 * Vaadin works - or rather doesn't work.
 *
 * <p>Instead, the original target is {@link #saveOriginalTargetUrl(VaadinSession, String) saved} in
 * the HTTP session and this custom {@link RequestCache} can make it available in a fake {@link
 * SavedRequest}. That way, the default {@link SavedRequestAwareAuthenticationSuccessHandler} can
 * redirect to the original target.
 */
public class VaadinOAuth2RequestCache implements RequestCache {

  private static final String ATTR_ORIGINAL_TARGET = "VaadinOAuth2OriginalTarget";

  public static void saveOriginalTargetUrl(VaadinSession session, String originalTargetUrl) {
    session
        .getSession()
        .setAttribute(ATTR_ORIGINAL_TARGET, new SimpleSavedRequest(originalTargetUrl));
  }

  @Override
  public void saveRequest(HttpServletRequest request, HttpServletResponse response) {}

  @Override
  public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return null;
    }

    SavedRequest originalTargetRequest = (SavedRequest) session.getAttribute(ATTR_ORIGINAL_TARGET);

    return originalTargetRequest;
  }

  @Override
  public HttpServletRequest getMatchingRequest(
      HttpServletRequest request, HttpServletResponse response) {
    // this part (replacing the request after redirecting with the original request) is not
    // necessary
    return null;
  }

  @Override
  public void removeRequest(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return;
    }

    session.removeAttribute(ATTR_ORIGINAL_TARGET);
  }
}
