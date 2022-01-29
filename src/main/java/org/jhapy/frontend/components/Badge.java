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

package org.jhapy.frontend.components;

import com.vaadin.flow.component.html.Span;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.lumo.BadgeColor;
import org.jhapy.frontend.utils.css.lumo.BadgeShape;
import org.jhapy.frontend.utils.css.lumo.BadgeSize;

import java.util.StringJoiner;

public class Badge extends Span {

  public Badge(String text) {
    this(text, BadgeColor.NORMAL);
  }

  public Badge(String text, BadgeColor color) {
    super(text);
    UIUtils.setTheme(color.getThemeName(), this);
  }

  public Badge(String text, BadgeColor color, BadgeSize size, BadgeShape shape) {
    super(text);
    StringJoiner joiner = new StringJoiner(" ");
    joiner.add(color.getThemeName());

    if (shape.equals(BadgeShape.PILL)) {
      joiner.add(shape.getThemeName());
    }

    if (size.equals(BadgeSize.S)) {
      joiner.add(size.getThemeName());
    }
    UIUtils.setTheme(joiner.toString(), this);
  }
}
