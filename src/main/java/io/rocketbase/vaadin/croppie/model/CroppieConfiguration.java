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

package io.rocketbase.vaadin.croppie.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CroppieConfiguration {

  private CropPoints points;

  private Float zoom;

  /**
   * The outer container of the cropper
   *
   * <p>Default will default to the size of the container
   */
  private SizeConfig boundary;
  /**
   * A class of your choosing to add to the container to add custom styles to your croppie
   *
   * <p>Default ""
   */
  @Builder.Default private String customClass = "";

  /**
   * The inner container of the coppie. The visible part of the image
   *
   * <p>Default { width: 100, height: 100, type: 'square' }
   */
  @Builder.Default private ViewPortConfig viewport = ViewPortConfig.DEFAULT_VALUE;

  /**
   * Enable or disable support for resizing the viewport area.
   *
   * <p>Default false
   */
  private boolean enableResize;

  /**
   * Enable zooming functionality. If set to false - scrolling and pinching would not zoom.
   *
   * <p>Default true
   */
  @Builder.Default private boolean enableZoom = true;

  @Builder.Default private boolean enableOrientation = true;

  /**
   * Enable or disable the ability to use the mouse wheel to zoom in and out on a croppie instance.
   *
   * <p>Default true
   */
  @Builder.Default private boolean mouseWheelZoom = true;

  /**
   * Hide or Show the zoom slider
   *
   * <p>Default true
   */
  @Builder.Default private boolean showZoomer = true;

  public String getJsonString() {
    List<String> parameters = new ArrayList<>();
    if (points != null) {
      parameters.add(String.format("\"points\": %s", points.getJsonString()));
    }
    if (boundary != null) {
      parameters.add(String.format("\"boundary\": %s", boundary.getJsonString()));
    }
    if (customClass != null && customClass.isEmpty()) {
      parameters.add(String.format("\"customClass\": \"%s\"", customClass));
    }
    if (viewport != null) {
      parameters.add(String.format("\"viewport\": %s", viewport.getJsonString()));
    }
    if (zoom != null) {
      parameters.add(String.format("\"zoom\": %s", zoom));
    }
    parameters.add(String.format("\"enableResize\": %s", enableResize));
    parameters.add(String.format("\"enableOrientation\": %s", enableOrientation));
    parameters.add(String.format("\"enableZoom\": %s", enableZoom));
    parameters.add(String.format("\"mouseWheelZoom\": %s", mouseWheelZoom));
    parameters.add(String.format("\"showZoomer\": %s", showZoomer));

    StringBuilder result = new StringBuilder("{");
    int paramSize = parameters.size();
    for (int x = 0; x < paramSize; x++) {
      result.append(parameters.get(x));
      if (x != paramSize - 1) {
        result.append(", ");
      }
    }
    result.append("}");
    return result.toString();
  }
}
