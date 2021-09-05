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

package org.jhapy.frontend.layout.size;

public enum Wide implements Size {
  XS("var(--lumo-space-wide-xs)", "spacing-wide-xs"),
  S("var(--lumo-space-wide-s)", "spacing-wide-s"),
  M("var(--lumo-space-wide-m)", "spacing-wide-m"),
  L("var(--lumo-space-wide-l)", "spacing-wide-l"),
  XL("var(--lumo-space-wide-xl)", "spacing-wide-xl"),

  RESPONSIVE_M("var(--lumo-space-wide-r-m)", null),
  RESPONSIVE_L("var(--lumo-space-wide-r-l)", null);

  private final String variable;
  private final String spacingClassName;

  Wide(String variable, String spacingClassName) {
    this.variable = variable;
    this.spacingClassName = spacingClassName;
  }

  @Override
  public String[] getMarginAttributes() {
    return new String[] {"margin"};
  }

  @Override
  public String[] getPaddingAttributes() {
    return new String[] {"padding"};
  }

  @Override
  public String getSpacingClassName() {
    return this.spacingClassName;
  }

  @Override
  public String getVariable() {
    return this.variable;
  }
}