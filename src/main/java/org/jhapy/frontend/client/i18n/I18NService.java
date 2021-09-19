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
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.ImportI18NFileQuery;
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
    path = "/api/i18NService")
@Primary
public interface I18NService extends RemoteServiceHandler {

  @PostMapping(value = "/getI18NFile")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getI18NFileFallback")
  ServiceResult<Byte[]> getI18NFile(@RequestBody BaseRemoteQuery query);

  default ServiceResult<Byte[]> getI18NFileFallback(BaseRemoteQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getI18NFileFallback"), e, null);
  }

  @PostMapping(value = "/importI18NFile")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "importI18NFileFallback")
  ServiceResult<Void> importI18NFile(@RequestBody ImportI18NFileQuery query);

  default ServiceResult<Void> findAnyMatchingFallback(ImportI18NFileQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("findAnyMatchingFallback"), e, null);
  }

  @PostMapping(value = "/getExistingLanguages")
  @CircuitBreaker(
      name = "defaultServiceCircuitBreaker",
      fallbackMethod = "getExistingLanguagesFallback")
  ServiceResult<List<String>> getExistingLanguages(@RequestBody BaseRemoteQuery query);

  default ServiceResult<List<String>> getExistingLanguagesFallback(
      BaseRemoteQuery query, Exception e) {
    return defaultFallback(
        getLoggerPrefix("getExistingLanguagesFallback"), e, Collections.emptyList());
  }
}