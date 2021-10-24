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

import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.security.SecurityRole;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.*;
import org.jhapy.dto.serviceQuery.security.securityRole.GetSecurityRoleByNameQuery;
import org.jhapy.dto.utils.PageDTO;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-04
 */
@Component
public class SecurityRoleServiceFallback
    implements SecurityRoleService, HasLogger, FallbackFactory<SecurityRoleServiceFallback> {

  final Throwable cause;

  public SecurityRoleServiceFallback() {
    this(null);
  }

  SecurityRoleServiceFallback(Throwable cause) {
    this.cause = cause;
  }

  @Override
  public SecurityRoleServiceFallback create(Throwable cause) {
    if (cause != null) {
      String errMessage =
          StringUtils.isNotBlank(cause.getMessage())
              ? cause.getMessage()
              : "Unknown error occurred : " + cause;
      // I don't see this log statement
      logger().debug("Client fallback called for the cause : {}", errMessage);
    }
    return new SecurityRoleServiceFallback(cause);
  }

  @Override
  public ServiceResult<List<SecurityRole>> getAllowedLoginRoles() {
    logger().error(getLoggerPrefix("getAllowedLoginRoles") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", Collections.emptyList());
  }

  @Override
  public ServiceResult<SecurityRole> getSecurityRoleByName(GetSecurityRoleByNameQuery query) {
    logger().error(getLoggerPrefix("getSecurityRoleByName") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<List<SecurityRole>> findAllActive() {
    logger().error(getLoggerPrefix("findAllActive") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", Collections.emptyList());
  }

  @Override
  public ServiceResult<PageDTO<SecurityRole>> findAnyMatching(FindAnyMatchingQuery query) {
    logger().error(getLoggerPrefix("findAnyMatching") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", new PageDTO<>());
  }

  @Override
  public ServiceResult<Long> countAnyMatching(CountAnyMatchingQuery query) {
    logger().error(getLoggerPrefix("countAnyMatching") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", 0L);
  }

  @Override
  public ServiceResult<SecurityRole> getById(GetByIdQuery query) {
    logger().error(getLoggerPrefix("getById") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<SecurityRole> save(SaveQuery<SecurityRole> query) {
    logger().error(getLoggerPrefix("save") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<Void> delete(DeleteByIdQuery query) {
    logger().error(getLoggerPrefix("delete") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }
}