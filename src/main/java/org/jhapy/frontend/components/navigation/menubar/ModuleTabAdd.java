/*
 * Copyright (c) 2021. Sasak UI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.jhapy.frontend.components.navigation.menubar;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ModuleTabAdd extends Div {

  public ModuleTabAdd() {

    Icon newTabIcon = VaadinIcon.PLUS.create();
    newTabIcon.setSize("20px");
    newTabIcon.setColor("white");
    newTabIcon.addClassName("tab-new-button");

    add(newTabIcon);
    getStyle().set("height", "30px");
    getStyle().set("align-self", "flex-end");
  }
}