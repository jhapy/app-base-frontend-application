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

import com.vaadin.flow.server.VaadinRequest;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.jhapy.cqrs.command.audit.LoginCommand;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.security.securityUser.GetSecurityUserByUsernameQuery;
import org.jhapy.frontend.client.security.SecurityUserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-01
 */
public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  private final SecurityUserService securityUserService;
  private final CommandGateway commandGateway;

  public AuthenticationFailureHandler(
      String failureUrl, SecurityUserService securityUserService, CommandGateway commandGateway) {
    super(failureUrl);
    this.commandGateway = commandGateway;
    setUseForward(true);
    this.securityUserService = securityUserService;
  }

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {
    String username = request.getParameter("username");

    if (exception instanceof BadCredentialsException) {
      SecurityUser securityUser =
          securityUserService
              .getSecurityUserByUsername(new GetSecurityUserByUsernameQuery(username))
              .getData();
      if (securityUser != null) {
        securityUser.setFailedLoginAttempts(securityUser.getFailedLoginAttempts() + 1);
        if (securityUser.getFailedLoginAttempts() > 4) {
          securityUser.setIsAccountLocked(true);
        }
        securityUserService.save(new SaveQuery<>(securityUser));
      }
    }
    LoginCommand loginCommand = new LoginCommand();
    loginCommand.setJsessionId(VaadinRequest.getCurrent().getWrappedSession().getId());
    loginCommand.setUsername(username);
    loginCommand.setSourceIp(null);
    loginCommand.setSuccess(false);
    loginCommand.setError(exception.getLocalizedMessage());
    commandGateway.send(loginCommand);

    if (request.getSession() != null) {
      request.changeSessionId();
    }
    super.onAuthenticationFailure(request, response, exception);
  }
}
