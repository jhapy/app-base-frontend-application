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

package org.jhapy.frontend.utils.css;

public enum Shadow {
  XS("var(--lumo-box-shadow-xs)"),
  S("var(--lumo-box-shadow-s)"),
  M("var(--lumo-box-shadow-m)"),
  L("var(--lumo-box-shadow-l)"),
  XL("var(--lumo-box-shadow-xl)");

  private final String value;

  Shadow(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
