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

package org.jhapy.frontend.config.metric;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import io.micrometer.core.instrument.MeterRegistry;
import org.jhapy.commons.utils.HasLogger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 30/05/2020
 */
@SpringComponent
public class SessionCountGaugeServiceInitListener implements VaadinServiceInitListener, HasLogger {

  private final MeterRegistry meterRegistry;

  public SessionCountGaugeServiceInitListener(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public void serviceInit(ServiceInitEvent event) {

    final AtomicInteger sessionsCount =
        meterRegistry.gauge("vaadin.sessions", new AtomicInteger(0));

    final VaadinService vaadinService = event.getSource();
    vaadinService.addSessionInitListener(
        e -> {
          var loggerPrefix = getLoggerPrefix("sessionInit");
          if (!e.getRequest().getPathInfo().startsWith("/management")
              && !e.getRequest().getPathInfo().startsWith("/sw.js")) {
            trace(
                loggerPrefix,
                "New Vaadin session created, path {0}, current count is: {1}",
                e.getRequest().getPathInfo(),
                sessionsCount.incrementAndGet());
          }
        });
    vaadinService.addSessionDestroyListener(
        e -> {
          var loggerPrefix = getLoggerPrefix("sessionDestroy");
          trace(
              loggerPrefix,
              "Vaadin session destroyed. Current count is: {0}",
              sessionsCount.decrementAndGet());
        });
  }
}