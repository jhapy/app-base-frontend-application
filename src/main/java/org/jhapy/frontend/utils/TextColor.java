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

package org.jhapy.frontend.utils;

public enum TextColor {
  HEADER("var(--lumo-header-text-color)"),
  BODY("var(--lumo-body-text-color)"),
  SECONDARY("var(--lumo-secondary-text-color)"),
  TERTIARY("var(--lumo-tertiary-text-color)"),
  DISABLED("var(--lumo-disabled-text-color)"),
  PRIMARY("var(--lumo-primary-text-color)"),
  PRIMARY_CONTRAST("var(--lumo-primary-contrast-color)"),
  ERROR("var(--lumo-error-text-color)"),
  ERROR_CONTRAST("var(--lumo-error-contrast-color)"),
  SUCCESS("var(--lumo-success-text-color)"),
  SUCCESS_CONTRAST("var(--lumo-success-contrast-color)");

  private final String value;

  TextColor(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
