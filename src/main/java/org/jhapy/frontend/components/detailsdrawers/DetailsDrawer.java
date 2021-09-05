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

package org.jhapy.frontend.components.detailsdrawers;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import org.jhapy.frontend.components.FlexBoxLayout;

@CssImport("./styles/components/details-drawer.css")
public class DetailsDrawer extends FlexBoxLayout {

  private final String CLASS_NAME = "details-drawer";

  private final FlexBoxLayout header;
  private final FlexBoxLayout content;
  private final FlexBoxLayout footer;

  public DetailsDrawer(Component... components) {
    this(null, components);
  }

  public DetailsDrawer(Position position, Component... components) {
    setClassName(CLASS_NAME);
    if (position != null) {
      setPosition(position);
    }

    header = new FlexBoxLayout();
    header.setClassName(CLASS_NAME + "__header");

    content = new FlexBoxLayout(components);
    content.setClassName(CLASS_NAME + "__content");
    content.setFlexDirection(FlexDirection.COLUMN);

    footer = new FlexBoxLayout();
    footer.setClassName(CLASS_NAME + "__footer");

    add(header, content, footer);
  }

  public FlexBoxLayout getHeader() {
    return this.header;
  }

  public void setHeader(Component... components) {
    this.header.removeAll();
    this.header.add(components);
  }

  public void setContent(Component... components) {
    this.content.removeAll();
    this.content.add(components);
  }

  public void setFooter(Component... components) {
    this.footer.removeAll();
    this.footer.add(components);
  }

  public void setPosition(Position position) {
    getElement().setAttribute("position", position.name().toLowerCase());
  }

  public void hide() {
    getElement().setAttribute("open", false);
  }

  public void show() {
    getElement().setAttribute("open", true);
  }

  public enum Position {
    BOTTOM,
    RIGHT
  }
}