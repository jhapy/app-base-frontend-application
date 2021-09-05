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

package org.jhapy.frontend.endpoint;

import org.jhapy.commons.utils.HasLogger;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-08-06
 */
@Controller
public class ErrorEndpoint implements ErrorController, HasLogger {

  @RequestMapping("/error")
  public ResponseEntity<?> handleError(HttpServletRequest httpRequest) {
    var loggerPrefix = getLoggerPrefix("handleError");
    String errorMsg = "";
    int httpErrorCode = getErrorCode(httpRequest);
    Exception exception = (Exception) httpRequest.getAttribute("javax.servlet.error.exception");
    String errorMessage = (String) httpRequest.getAttribute("javax.servlet.error.message");
    switch (httpErrorCode) {
      case -1 -> {
        errorMsg = errorMessage;
        break;
      }
      case 400 -> {
        errorMsg = "Http Error Code: 400. Bad Request";
        break;
      }
      case 401 -> {
        errorMsg = "Http Error Code: 401. Unauthorized";
        break;
      }
      case 404 -> {
        errorMsg = "Http Error Code: 404. Resource not found " + httpRequest
            .getAttribute("javax.servlet.error.request_uri");
        break;
      }
      case 500 -> {
        errorMsg = "Http Error Code: 500. Internal Server Error";
        break;
      }
    }
    logger()
        .error(
            loggerPrefix + "Error (" + httpErrorCode + ") : " + errorMessage + " / " + errorMsg,
            exception);

    return ResponseEntity.ok(errorMessage);
  }

  public String getErrorPath() {
    return "/error";
  }

  private int getErrorCode(HttpServletRequest httpRequest) {
    if (httpRequest.getAttribute("javax.servlet.error.status_code") != null) {
      return (Integer) httpRequest
          .getAttribute("javax.servlet.error.status_code");
    } else {
      return -1;
    }
  }
}