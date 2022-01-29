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
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.menubar.View;

/**
 * A view frame that establishes app design guidelines. It consists of four parts:
 *
 * <ul>
 *   <li>Topmost {@link #setViewHeader(Component...) header}
 *   <li>Center {@link #setViewContent(Component...) content}
 *   <li>Center {@link #setViewDetails(Component...) details}
 *   <li>Bottom {@link #setViewFooter(Component...) footer}
 * </ul>
 */
@CssImport("./styles/components/view-frame.css")
public abstract class SplitViewFrame extends View implements HasStyle {

  private final String CLASS_NAME = "view-frame";

  private final Div header;

  private final FlexBoxLayout wrapper;
  private final Div content;
  private final Div details;

  private final Div footer;

  public SplitViewFrame() {
    setClassName(CLASS_NAME);

    header = new Div();
    header.setClassName(CLASS_NAME + "__header");
    wrapper = new FlexBoxLayout();
    wrapper.setClassName(CLASS_NAME + "__wrapper");
    content = new Div();
    content.setClassName(CLASS_NAME + "__content");
    details = new Div();
    details.setClassName(CLASS_NAME + "__details");
    footer = new Div();
    footer.setClassName(CLASS_NAME + "__footer");

    wrapper.add(content, details);
    add(header, wrapper, footer);
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

  /** Sets the detail slot's components. */
  public void setViewDetails(Component... components) {
    details.removeAll();
    details.add(components);
  }

  public void setViewDetailsPosition(Position position) {
    if (position.equals(Position.RIGHT)) {
      wrapper.setFlexDirection(FlexDirection.ROW);

    } else if (position.equals(Position.BOTTOM)) {
      wrapper.setFlexDirection(FlexDirection.COLUMN);
    }
  }

  /** Sets the footer slot's components. */
  public void setViewFooter(Component... components) {
    footer.removeAll();
    footer.add(components);
  }

  public enum Position {
    RIGHT,
    BOTTOM
  }
}
