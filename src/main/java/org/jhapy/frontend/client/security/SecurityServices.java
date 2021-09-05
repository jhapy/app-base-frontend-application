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

package org.jhapy.frontend.client.security;

import com.vaadin.flow.spring.SpringServlet;
import org.jhapy.frontend.client.security.keycloak.KeycloakClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-02
 */
@Service
public class SecurityServices {

  public static SecurityUserService getSecurityUserService() {
    return getApplicationContext().getBean(SecurityUserService.class);
  }

  public static SecurityRoleService getSecurityRoleService() {
    return getApplicationContext().getBean(SecurityRoleService.class);
  }

  public static ClientService getClientService() {
    return getApplicationContext().getBean(ClientService.class);
  }

  public static DbTableService getTableService() {
    return getApplicationContext().getBean(DbTableService.class);
  }

  public static KeycloakClient getKeycloakClient() {
    return getApplicationContext().getBean(KeycloakClient.class);
  }

  public static ApplicationContext getApplicationContext() {
    ServletContext servletContext = SpringServlet.getCurrent().getServletContext();
    return WebApplicationContextUtils.getWebApplicationContext(servletContext);
  }
}