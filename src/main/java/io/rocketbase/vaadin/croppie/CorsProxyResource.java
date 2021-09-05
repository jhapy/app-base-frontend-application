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

package io.rocketbase.vaadin.croppie;

import com.vaadin.flow.server.StreamResource;
import org.jhapy.commons.utils.HasLoggerStatic;

import java.io.IOException;
import java.net.URL;

public class CorsProxyResource extends StreamResource {

  public CorsProxyResource(String filename, String url) {
    super(
        filename,
        () -> {
          try {
            return new URL(url).openStream();
          } catch (IOException e) {
            HasLoggerStatic.error(
                CorsProxyResource.class,
                HasLoggerStatic.getLoggerPrefix("InputStreamFactory()"),
                e,
                "Unexpected error {0}",
                e.getLocalizedMessage());
            return null;
          }
        });
  }
}