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

package org.jhapy.frontend.client.i18n;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.jhapy.dto.domain.i18n.ActionDTO;
import org.jhapy.dto.domain.i18n.ActionTrlDTO;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.*;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.actionTrl.GetActionTrlQuery;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.frontend.client.AuthorizedFeignClient;
import org.jhapy.frontend.client.RemoteServiceHandler;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.List;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
@AuthorizedFeignClient(
    name = "${jhapy.remote-services.i18n-server.name:null}",
    path = "/api/actionService")
@Primary
public interface ActionService extends RemoteServiceHandler {
  @PostMapping(value = "/getActionTrls")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getActionTrlsFallback")
  ServiceResult<List<ActionTrlDTO>> getActionTrls(@RequestBody GetActionTrlQuery query);

  default ServiceResult<List<ActionDTO>> getActionTrlsFallback(
      GetActionTrlQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getActionTrlsFallback"), e, Collections.emptyList());
  }

  @PostMapping(value = "/findByIso3")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "findByIso3Fallback")
  ServiceResult<List<ActionTrlDTO>> findByIso3(@RequestBody FindByIso3Query query);

  default ServiceResult<List<ActionDTO>> findByIso3Fallback(FindByIso3Query query, Exception e) {
    return defaultFallback(getLoggerPrefix("findByIso3Fallback"), e, Collections.emptyList());
  }

  @PostMapping(value = "/getActionTrlByNameAndIso3")
  @CircuitBreaker(
      name = "defaultServiceCircuitBreaker",
      fallbackMethod = "getActionTrlByNameAndIso3Fallback")
  ServiceResult<ActionTrlDTO> getActionTrlByNameAndIso3(@RequestBody GetByNameAndIso3Query query);

  default ServiceResult<ActionTrlDTO> getActionTrlByNameAndIso3Fallback(
      GetByNameAndIso3Query query, Exception e) {
    return defaultFallback(getLoggerPrefix("getActionTrlByNameAndIso3Fallback"), e, null);
  }

  @PostMapping(value = "/findAnyMatching")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "findAnyMatchingFallback")
  ServiceResult<PageDTO<ActionDTO>> findAnyMatching(@RequestBody FindAnyMatchingQuery query);

  default ServiceResult<PageDTO<ActionDTO>> findAnyMatchingFallback(
      FindAnyMatchingQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("findAnyMatchingFallback"), e, new PageDTO<>());
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
  ServiceResult<ActionDTO> getById(@RequestBody GetByIdQuery query);

  default ServiceResult<ActionDTO> getByIdFallback(GetByIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByIdFallback"), e, null);
  }

  @PostMapping(value = "/save")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "saveFallback")
  ServiceResult<ActionDTO> save(@RequestBody SaveQuery<ActionDTO> query);

  default ServiceResult<ActionDTO> saveFallback(SaveQuery<ActionDTO> query, Exception e) {
    return defaultFallback(getLoggerPrefix("saveFallback"), e, null);
  }

  @PostMapping(value = "/delete")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "deleteFallback")
  ServiceResult<Void> delete(@RequestBody DeleteByIdQuery query);

  default ServiceResult<Void> deleteFallback(DeleteByIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("deleteFallback"), e, null);
  }
}