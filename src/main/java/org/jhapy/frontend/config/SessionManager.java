package org.jhapy.frontend.config;

import com.hazelcast.core.HazelcastInstance;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.jhapy.commons.security.SecurityUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.messageQueue.EndSession;
import org.jhapy.frontend.client.audit.AuditServices;
import org.jhapy.frontend.utils.SessionInfo;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 17/09/2020
 */
@Configuration
@EnableScheduling
public class SessionManager implements VaadinServiceInitListener, HasLogger {

  private final HazelcastInstance hazelcastInstance;

  public SessionManager(HazelcastInstance hazelcastInstance) {
    this.hazelcastInstance = hazelcastInstance;
  }

  private ConcurrentMap<String, SessionInfo> retrieveMap() {
    return hazelcastInstance.getMap("userSessions");
  }

  @Override
  public void serviceInit(ServiceInitEvent event) {
    var vaadinService = event.getSource();

    vaadinService.addSessionDestroyListener(
        e -> {
          var loggerPrefix = getLoggerPrefix("sessionDestroy");

          var currentSecurityUser = SecurityUtils.getCurrentUserLogin();

          trace(
              loggerPrefix,
              "Vaadin session destroyed. Current user is : {0}",
              (currentSecurityUser.orElse("Not set")));

          if (e.getSession() != null && e.getSession().getSession() != null) {
            trace(loggerPrefix, "End remote session");

            var sessionId = e.getSession().getSession().getId();
            retrieveMap().remove(sessionId);

            AuditServices.getAuditServiceQueue()
                .endSession(new EndSession(sessionId, Instant.now()));
          }
        });
  }

  // @Scheduled(fixedDelay = 60 * 1000)
  public void removeDeadSessions() {
    var loggerPrefix = getLoggerPrefix("removedDeadSessions");
    retrieveMap()
        .forEach(
            (s, sessionInfo) -> {
              if (sessionInfo.getLastContact().plusMinutes(2).isBefore(LocalDateTime.now())) {
                info(loggerPrefix, "Remove dead session : {0}", sessionInfo);
                retrieveMap().remove(s);
              }
            });
  }
}