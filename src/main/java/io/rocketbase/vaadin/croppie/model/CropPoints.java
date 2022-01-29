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
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
public class CropPoints {

  private int topLeftX, topLeftY, bottomRightX, bottomRightY;

  public static CropPoints parseArray(String arrayString) {
    if (arrayString != null) {
      Pattern pattern =
          Pattern.compile(
              ".*\\[[ \"]*([0-9]+)[ \"]*,[ \"]*([0-9]+)[ \"]*,[ \"]*([0-9]+)[ \"]*,[ \"]*([0-9]+)[ \"]*\\].*");
      Matcher matcher = pattern.matcher(arrayString);
      if (matcher.matches()) {
        return new CropPoints(
            Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)),
            Integer.parseInt(matcher.group(3)),
            Integer.parseInt(matcher.group(4)));
      }
    }
    return null;
  }

  public String getJsonString() {
    return String.format("[%d, %d, %d, %d]", topLeftX, topLeftY, bottomRightX, bottomRightY);
  }
}
