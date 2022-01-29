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

package org.jhapy.frontend.client.reference;

import org.jhapy.dto.domain.reference.IntermediateRegionDTO;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.*;
import org.jhapy.dto.utils.PageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-27
 */
@FeignClient(
    name = "${jhapy.remote-services.backend-server.name:null}",
    path = "/api/intermediateRegionService",
    fallback = IntermediateRegionServiceFallback.class)
@Primary
public interface IntermediateRegionService {

  @PostMapping(value = "/findAll")
  ServiceResult<List<IntermediateRegionDTO>> findAll(@RequestBody BaseRemoteQuery query);

  @PostMapping(value = "/findAnyMatching")
  ServiceResult<PageDTO<IntermediateRegionDTO>> findAnyMatching(
      @RequestBody FindAnyMatchingQuery query);

  @PostMapping(value = "/countAnyMatching")
  ServiceResult<Long> countAnyMatching(@RequestBody CountAnyMatchingQuery query);

  @PostMapping(value = "/getById")
  ServiceResult<IntermediateRegionDTO> getById(@RequestBody GetByIdQuery query);

  @PostMapping(value = "/save")
  ServiceResult<IntermediateRegionDTO> save(@RequestBody SaveQuery<IntermediateRegionDTO> query);

  @PostMapping(value = "/delete")
  ServiceResult<Void> delete(@RequestBody DeleteByIdQuery query);
}
