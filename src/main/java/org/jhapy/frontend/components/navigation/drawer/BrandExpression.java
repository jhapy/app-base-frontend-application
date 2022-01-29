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

package org.jhapy.frontend.components.navigation.drawer;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import org.jhapy.frontend.utils.UIUtils;

@CssImport("./styles/components/brand-expression.css")
public class BrandExpression extends Div {

  private final String CLASS_NAME = "brand-expression";

  private final Image logo;
  private final Label title;

  public BrandExpression(String text) {
    setClassName(CLASS_NAME);

    logo = new Image(UIUtils.IMG_PATH + "logo.png", "");
    logo.setAlt(text + " logo");
    logo.setClassName(CLASS_NAME + "__logo");

    title = UIUtils.createH3Label(text);
    title.addClassName(CLASS_NAME + "__title");

    add(logo, title);
  }
}
