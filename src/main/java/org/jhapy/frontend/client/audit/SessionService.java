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

package org.jhapy.frontend.client.audit;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.jhapy.dto.domain.audit.Session;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.utils.Page;
import org.jhapy.frontend.client.RemoteServiceHandler;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-16
 */
@FeignClient(
    name = "${jhapy.remote-services.audit-server.name:null}",
    path = "/api/sessionService")
@Primary
public interface SessionService extends RemoteServiceHandler {

  @PostMapping(value = "/findAnyMatching")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "findAnyMatchingFallback")
  ServiceResult<Page<Session>> findAnyMatching(@RequestBody FindAnyMatchingQuery query);

  default ServiceResult<Page<Session>> findAnyMatchingFallback(
      FindAnyMatchingQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("findAnyMatchingFallback"), e, new Page<>());
  }

  @PostMapping(value = "/countAnyMatching")
  @CircuitBreaker(
      name = "defaultServiceCircuitBreaker",
      fallbackMethod = "countAnyMatchingFallback")
  ServiceResult<Long> countAnyMatching(@RequestBody CountAnyMatchingQuery query);

  default ServiceResult<Long> countAnyMatchingFallback(CountAnyMatchingQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("countAnyMatchingFallback"), e, 0L);
  }

  @PostMapping(value = "/getById")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getByIdFallback")
  ServiceResult<Session> getById(@RequestBody GetByStrIdQuery query);

  default ServiceResult<Session> getByIdFallback(GetByStrIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByIdFallback"), e, null);
  }
}