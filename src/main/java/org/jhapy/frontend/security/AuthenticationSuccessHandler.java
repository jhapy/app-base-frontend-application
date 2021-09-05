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

import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.messageQueue.NewSession;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.frontend.client.audit.AuditServiceQueue;
import org.jhapy.frontend.client.security.SecurityUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-01
 */
public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  private final SecurityUserService securityUserService;
  private final AuditServiceQueue auditServiceQueue;

  public AuthenticationSuccessHandler(
      SecurityUserService securityUserService, AuditServiceQueue auditServiceQueue) {
    this.securityUserService = securityUserService;
    this.auditServiceQueue = auditServiceQueue;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws ServletException, IOException {
    SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
    securityUser.setLastSuccessfulLogin(Instant.now());
    securityUser.setIsAccountLocked(false);
    securityUser.setFailedLoginAttempts(0);
    securityUserService.save(new SaveQuery<>(securityUser));

    auditServiceQueue.newSession(
        new NewSession(
            request.getRequestedSessionId(),
            securityUser.getUsername(),
            null,
            Instant.now(),
            true,
            null));
    super.onAuthenticationSuccess(request, response, authentication);
  }
}