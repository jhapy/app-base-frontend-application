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

import org.jhapy.dto.domain.DbTableDTO;
import org.jhapy.frontend.client.RemoteServiceHandlerV2;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 2019-03-07
 */
@FeignClient(name = "${jhapy.remote-services.mgt-server.name:null}", path = "api/dbTableService")
@Primary
public interface DbTableService extends RemoteServiceHandlerV2<DbTableDTO> {}
