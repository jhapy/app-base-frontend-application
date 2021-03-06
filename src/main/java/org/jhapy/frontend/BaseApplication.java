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

package org.jhapy.frontend;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.SpringVaadinServletService;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.config.AppProperties;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.commons.utils.SpringProfileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;


/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
public abstract class BaseApplication implements InitializingBean, HasLogger {

  private static final Logger logger = LoggerFactory.getLogger(BaseApplication.class);

  private final Environment env;

  private final AppProperties appProperties;

  public BaseApplication(Environment env, AppProperties appProperties) {
    this.env = env;
    this.appProperties = appProperties;
  }

  protected static void logApplicationStartup(Environment env) {
    String protocol = "http";
    if (env.getProperty("server.ssl.key-store") != null) {
      protocol = "https";
    }
    String serverPort = env.getProperty("server.port");
    String contextPath = env.getProperty("server.servlet.context-path");
    if (StringUtils.isBlank(contextPath)) {
      contextPath = "/";
    }
    String hostAddress = "localhost";
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      logger.warn("The host name could not be determined, using `localhost` as fallback");
    }
    logger.info("""

            ----------------------------------------------------------
            \tApplication '{}' is running! Access URLs:
            \tLocal: \t\t{}://localhost:{}{}
            \tExternal: \t{}://{}:{}{}
            \tProfile(s): \t{}
            ----------------------------------------------------------""",
        env.getProperty("spring.application.name"),
        protocol,
        serverPort,
        contextPath,
        protocol,
        hostAddress,
        serverPort,
        contextPath,
        env.getActiveProfiles());

    String configServerStatus = env.getProperty("configserver.status");
    if (configServerStatus == null) {
      configServerStatus = "Not found or not setup for this application";
    }
    logger.info("""

            ----------------------------------------------------------
            \tConfig Server: \t{}
            ----------------------------------------------------------""",
        configServerStatus);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
    if (activeProfiles.contains(SpringProfileConstants.SPRING_PROFILE_DEVELOPMENT)
        && activeProfiles
        .contains(SpringProfileConstants.SPRING_PROFILE_PRODUCTION)) {
      logger.error("You have misconfigured your application! It should not run " +
          "with both the 'dev' and 'prod' profiles at the same time.");
    }
  }

  /*
    @RequestMapping("/")
    public String forward(@Value("${vaadin.url}") String vaadinUrl) {
      return "redirect:" + vaadinUrl;
    }

    @RequestMapping("/sw.js")
    public String forwardSW(@Value("${vaadin.url}") String vaadinUrl) {
      return "forward:" + vaadinUrl + "sw.js";
    }

    @Bean
    public ServletRegistrationBean<SpringServlet> springServlet(
        ApplicationContext applicationContext,
        @Value("${vaadin.urlMapping}") String vaadinUrlMapping) {

      SpringServlet servlet = buildSpringServlet(applicationContext);
      ServletRegistrationBean<SpringServlet> registrationBean =
          new ServletRegistrationBean<>(servlet, vaadinUrlMapping, "/frontend/*");
      registrationBean.setLoadOnStartup(1);
      //registrationBean.addInitParameter(Constants.SERVLET_PARAMETER_SYNC_ID_CHECK, "false");

      return registrationBean;
    }
  */
  private SpringServlet buildSpringServlet(ApplicationContext applicationContext) {
    return new SpringServlet(applicationContext, true) {
      @Override
      protected VaadinServletService createServletService(
          DeploymentConfiguration deploymentConfiguration) throws
          ServiceException {
        SpringVaadinServletService service =
            buildSpringVaadinServletService(this, deploymentConfiguration,
                applicationContext);
        service.init();
        return service;
      }
    };
  }

  private SpringVaadinServletService buildSpringVaadinServletService(SpringServlet servlet,
      DeploymentConfiguration deploymentConfiguration,
      ApplicationContext applicationContext) {
    return new SpringVaadinServletService(servlet, deploymentConfiguration,
        applicationContext) {

      @Override
      public void requestStart(VaadinRequest request, VaadinResponse response) {
        super.requestStart(request, response);

        /*
        //logger().debug("Current Locale = " + request.getLocale().getLanguage());
        AppContext.setCurrentIso3Language(request.getLocale().getLanguage());
        AppContext.setCurrentSessionId(request.getWrappedSession().getId());

        Object principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof SecurityUser) {
          AppContext.setCurrentUsername(((SecurityUser) principal).getUsername());
        } else if (principal instanceof UsernamePasswordAuthenticationToken) {
          AppContext
              .setCurrentUsername(((UsernamePasswordAuthenticationToken) principal).getName());
        }
         */
      }

      @Override
      public void requestEnd(
          VaadinRequest request, VaadinResponse response, VaadinSession session) {
        if (session != null) {
          try {
            session.lock();
            writeToHttpSession(request.getWrappedSession(), session);
          } finally {
            session.unlock();
          }
        }
        super.requestEnd(request, response, session);
      }
    };
  }

  @PostConstruct
  void postConstruct() {
    if (StringUtils
        .isNotBlank(appProperties.getSecurity().getTrustStore().getTrustStorePath())) {
      File trustStoreFilePath = new File(
          appProperties.getSecurity().getTrustStore().getTrustStorePath());
      String tsp = trustStoreFilePath.getAbsolutePath();
      logger.info("Use trustStore " + tsp + ", with password : " + appProperties.getSecurity()
          .getTrustStore().getTrustStorePassword() + ", with type : " + appProperties
          .getSecurity()
          .getTrustStore()
          .getTrustStoreType());

      System.setProperty("javax.net.ssl.trustStore", tsp);
      System.setProperty("javax.net.ssl.trustStorePassword",
          appProperties.getSecurity().getTrustStore().getTrustStorePassword());
      if (StringUtils
          .isNotBlank(appProperties.getSecurity().getTrustStore().getTrustStoreType())) {
        System.setProperty("javax.net.ssl.trustStoreType",
            appProperties.getSecurity().getTrustStore().getTrustStoreType());
      }
    }
    if (StringUtils.isNotBlank(appProperties.getSecurity().getKeyStore().getKeyStorePath())) {
      File keyStoreFilePath = new File(
          appProperties.getSecurity().getKeyStore().getKeyStorePath());
      String ksp = keyStoreFilePath.getAbsolutePath();
      logger.info(
          "Use keyStore " + ksp + ", with password : " + appProperties.getSecurity()
              .getKeyStore()
              .getKeyStorePassword() + ", with type : " + appProperties.getSecurity()
              .getKeyStore()
              .getKeyStoreType());

      System.setProperty("javax.net.ssl.keyStore", ksp);
      System.setProperty("javax.net.ssl.keyStorePassword",
          appProperties.getSecurity().getKeyStore().getKeyStorePassword());
      if (StringUtils
          .isNotBlank(appProperties.getSecurity().getKeyStore().getKeyStoreType())) {
        System.setProperty("javax.net.ssl.keyStoreType",
            appProperties.getSecurity().getKeyStore().getKeyStoreType());
      }
    }
    if (appProperties.getSecurity().getTrustStore().getDebug() != null
        || appProperties.getSecurity().getKeyStore().getDebug() != null) {
      System.setProperty("javax.net.debug",
          Boolean.toString(appProperties.getSecurity().getTrustStore().getDebug() != null
              || appProperties.getSecurity().getKeyStore().getDebug() != null));
    }
  }
}
