package org.jhapy.frontend.filter;

import com.hazelcast.core.HazelcastInstance;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.utils.SessionInfo;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 17/09/2020
 */
@Component
@Order(2)
public class SessionFilter implements Filter, HasLogger {

  private final HazelcastInstance hazelcastInstance;

  public SessionFilter(HazelcastInstance hazelcastInstance) {
    this.hazelcastInstance = hazelcastInstance;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String sessionId = ((HttpServletRequest) request).getSession().getId();
    if (sessionId != null) {
      SessionInfo sessionInfo = retrieveMap().getOrDefault(sessionId, null);
      if (sessionInfo != null) {
        sessionInfo.setLastContact(LocalDateTime.now());
        retrieveMap().replace(sessionId, sessionInfo);
      }
    }
    chain.doFilter(request, response);
  }

  private ConcurrentMap<String, SessionInfo> retrieveMap() {
    return hazelcastInstance.getMap("userSessions");
  }
}