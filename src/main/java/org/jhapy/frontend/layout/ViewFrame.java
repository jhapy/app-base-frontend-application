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

package org.jhapy.frontend.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.jhapy.frontend.components.navigation.menubar.View;

/**
 * A view frame that establishes app design guidelines. It consists of three parts:
 *
 * <ul>
 *   <li>Topmost {@link #setViewHeader(Component...) header}
 *   <li>Center {@link #setViewContent(Component...) content}
 *   <li>Bottom {@link #setViewFooter(Component...) footer}
 * </ul>
 */
@CssImport("./styles/components/view-frame.css")
@SpringComponent
public abstract class ViewFrame extends View implements HasStyle {

  private final String CLASS_NAME = "view-frame";
  private final Div header;
  private final Div content;
  private final Div footer;

  public ViewFrame() {
    setClassName(CLASS_NAME);

    header = new Div();
    header.setClassName(CLASS_NAME + "__header");
    content = new Div();
    content.setClassName(CLASS_NAME + "__content");
    footer = new Div();
    footer.setClassName(CLASS_NAME + "__footer");

    add(header, content, footer);
  }

  /** Sets the header slot's components. */
  public void setViewHeader(Component... components) {
    header.removeAll();
    header.add(components);
  }

  /** Sets the content slot's components. */
  public void setViewContent(Component... components) {
    content.removeAll();
    content.add(components);
  }

  /** Sets the footer slot's components. */
  public void setViewFooter(Component... components) {
    footer.removeAll();
    footer.add(components);
  }
}
