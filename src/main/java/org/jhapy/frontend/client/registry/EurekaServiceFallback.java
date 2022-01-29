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

package org.jhapy.frontend.client.registry;

import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.dto.registry.EurekaStatus;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-02
 */
@Component
public class EurekaServiceFallback
    implements EurekaService, HasLogger, FallbackFactory<EurekaServiceFallback> {

  final Throwable cause;

  public EurekaServiceFallback() {
    this(null);
  }

  EurekaServiceFallback(Throwable cause) {
    this.cause = cause;
  }

  @Override
  public EurekaServiceFallback create(Throwable cause) {
    if (cause != null) {
      String errMessage =
          StringUtils.isNotBlank(cause.getMessage())
              ? cause.getMessage()
              : "Unknown error occurred : " + cause;
      // I don't see this log statement
      logger().debug("Client fallback called for the cause : {}", errMessage);
    }
    return new EurekaServiceFallback(cause);
  }

  @Override
  public ServiceResult<EurekaInfo> getApplications(BaseRemoteQuery query) {
    logger().error(getLoggerPrefix("getApplications") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<Map<String, List<String[]>>> lastn(BaseRemoteQuery query) {
    logger().error(getLoggerPrefix("lastn") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", Collections.emptyMap());
  }

  @Override
  public ServiceResult<List<String>> replicas(BaseRemoteQuery query) {
    logger().error(getLoggerPrefix("replicas") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", Collections.emptyList());
  }

  @Override
  public ServiceResult<EurekaStatus> status(BaseRemoteQuery query) {
    logger().error(getLoggerPrefix("status") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }
}
