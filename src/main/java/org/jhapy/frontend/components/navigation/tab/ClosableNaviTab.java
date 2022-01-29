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

package org.jhapy.frontend.components.navigation.tab;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.jhapy.frontend.utils.FontSize;
import org.jhapy.frontend.utils.UIUtils;

public class ClosableNaviTab extends NaviTab {

  private final Button close;

  public ClosableNaviTab(String label, Class<? extends Component> navigationTarget) {
    super(label, navigationTarget);
    getElement().setAttribute("closable", true);

    close = UIUtils.createButton(VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY_INLINE);
    // ButtonVariant.LUMO_SMALL isn't small enough.
    UIUtils.setFontSize(FontSize.XXS, close);
    add(close);
  }

  public Button getCloseButton() {
    return close;
  }
}
