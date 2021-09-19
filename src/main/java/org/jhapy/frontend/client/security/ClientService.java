/*
 *
 *  E-FLIGHT MGT CONFIDENTIAL
 *   __________________
 *
 *   [2018] - [2019] E-Flight Mgt
 *   All Rights Reserved.
 *
 *   NOTICE:  All information contained herein is, and remains the property of "E-Flight Mgt"
 *   and its suppliers, if any. The intellectual and technical concepts contained herein are
 *   proprietary to "E-Flight Mgt" and its suppliers and may be covered by Morocco. and Foreign
 *   Patents, patents in process, and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material is strictly forbidden unless
 *    prior written permission is obtained from "E-Flight Mgt".
 */

package org.jhapy.frontend.client.security;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.jhapy.dto.domain.ClientDTO;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.security.GetByExternalIdQuery;
import org.jhapy.dto.serviceQuery.security.GetByExternalIdsQuery;
import org.jhapy.frontend.client.RemoteServiceHandlerV2;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.List;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 2019-03-07
 */
@FeignClient(
    name = "${jhapy.remote-services.mgt-server.name:null}",
    path = "/api/clientService")
@Primary
public interface ClientService extends RemoteServiceHandlerV2<ClientDTO> {
  @PostMapping(value = "/getByExternalId")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getByExternalIdFallback")
  ServiceResult<ClientDTO> getByExternalId(@RequestBody GetByExternalIdQuery query);

  default ServiceResult<ClientDTO> getByExternalIdFallback(
      GetByExternalIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByExternalIdFallback"), e, null);
  }

  @PostMapping(value = "/getByExternalIds")
  @CircuitBreaker(
      name = "defaultServiceCircuitBreaker",
      fallbackMethod = "getByExternalIdsFallback")
  ServiceResult<List<ClientDTO>> getByExternalIds(@RequestBody GetByExternalIdsQuery query);

  default ServiceResult<List<ClientDTO>> getByExternalIdsFallback(
      GetByExternalIdsQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByExternalIdFallback"), e, Collections.emptyList());
  }
}