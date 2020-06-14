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

import java.util.List;
import org.jhapy.dto.domain.reference.Region;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.reference.region.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.reference.region.FindAnyMatchingQuery;
import org.jhapy.dto.utils.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-27
 */
@FeignClient(name = "${jhapy.remote-services.backend-server.name:null}", url = "${jhapy.remote-services.backend-server.url:}", path = "/regionService", fallback = RegionServiceFallback.class)
@Primary
public interface RegionService {

  @PostMapping(value = "/findAll")
  ServiceResult<List<Region>> findAll();

  @PostMapping(value = "/findAnyMatching")
  ServiceResult<Page<Region>> findAnyMatching(@RequestBody FindAnyMatchingQuery query);

  @PostMapping(value = "/countAnyMatching")
  ServiceResult<Long> countAnyMatching(@RequestBody CountAnyMatchingQuery query);

  @PostMapping(value = "/getById")
  ServiceResult<Region> getById(@RequestBody GetByIdQuery query);

  @PostMapping(value = "/save")
  ServiceResult<Region> save(@RequestBody SaveQuery<Region> query);

  @PostMapping(value = "/delete")
  ServiceResult<Void> delete(@RequestBody DeleteByIdQuery query);
}
