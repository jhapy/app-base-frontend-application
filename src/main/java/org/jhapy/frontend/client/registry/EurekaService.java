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

import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.dto.registry.EurekaStatus;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
@FeignClient(
    name = "${jhapy.remote-services.registry-server.name:null}",
    path = "/api/eureka",
    fallbackFactory = EurekaServiceFallback.class)
@Primary
public interface EurekaService {

  @PostMapping(value = "/applications")
  ServiceResult<EurekaInfo> getApplications(@RequestBody BaseRemoteQuery query);

  @PostMapping(value = "/lastn")
  ServiceResult<Map<String, List<String[]>>> lastn(@RequestBody BaseRemoteQuery query);

  @PostMapping(value = "/replicas")
  ServiceResult<List<String>> replicas(@RequestBody BaseRemoteQuery query);

  @PostMapping(value = "/status")
  ServiceResult<EurekaStatus> status(@RequestBody BaseRemoteQuery query);
}
