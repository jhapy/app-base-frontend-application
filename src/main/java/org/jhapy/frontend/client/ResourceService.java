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

package org.jhapy.frontend.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.utils.StoredFile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-15
 */
@FeignClient(
    name = "${jhapy.remote-services.resource-server.name:null}",
    path = "/api/resourceService")
@Primary
public interface ResourceService extends RemoteServiceHandler {

  @PostMapping(value = "/save")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "saveFallback")
  ServiceResult<StoredFile> save(@RequestBody SaveQuery<StoredFile> query);

  default ServiceResult<StoredFile> saveFallback(SaveQuery<StoredFile> query, Exception e) {
    return defaultFallback(getLoggerPrefix("saveFallback"), e, null);
  }

  @PostMapping(value = "/getById")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getByIdFallback")
  ServiceResult<StoredFile> getById(@RequestBody GetByStrIdQuery query);

  default ServiceResult<StoredFile> getByIdFallback(GetByStrIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByIdFallback"), e, null);
  }

  @PostMapping(value = "/getByIdNoContent")
  @CircuitBreaker(
      name = "defaultServiceCircuitBreaker",
      fallbackMethod = "getByIdNoContentFallback")
  ServiceResult<StoredFile> getByIdNoContent(@RequestBody GetByStrIdQuery query);

  default ServiceResult<StoredFile> getByIdNoContentFallback(GetByStrIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByIdNoContentFallback"), e, null);
  }

  @PostMapping(value = "/getByIdPdfContent")
  @CircuitBreaker(
      name = "defaultServiceCircuitBreaker",
      fallbackMethod = "getByIdPdfContentFallback")
  ServiceResult<StoredFile> getByIdPdfContent(@RequestBody GetByStrIdQuery query);

  default ServiceResult<StoredFile> getByIdPdfContentFallback(GetByStrIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByIdPdfContentFallback"), e, null);
  }

  @PostMapping(value = "/delete")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "deleteFallback")
  ServiceResult<Void> delete(@RequestBody DeleteByStrIdQuery query);

  default ServiceResult<Void> deleteFallback(DeleteByStrIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("deleteFallback"), e, null);
  }
}