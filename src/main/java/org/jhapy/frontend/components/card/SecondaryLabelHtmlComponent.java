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

package org.jhapy.frontend.components.card;

import com.github.appreciated.card.label.WhiteSpaceLabel;
import com.vaadin.flow.component.Html;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-13
 */
public class SecondaryLabelHtmlComponent extends Html implements WhiteSpaceLabel {

  public SecondaryLabelHtmlComponent(String text) {
    super(text);
    init();
  }

  private void init() {
    getElement()
        .getStyle()
        .set("font-size", "var(--lumo-font-size-s)")
        .set("text-overflow", "ellipsis")
        .set("overflow", "scroll")
        .set("color", "var(--lumo-secondary-text-color)")
        .set("width", "100%")
        .set("min-height", "100px")
        .set("max-height", "200px");
  }
}
