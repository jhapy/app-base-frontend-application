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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.tabs.Tabs;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Right;
import org.jhapy.frontend.layout.size.Vertical;
import org.jhapy.frontend.utils.BoxShadowBorders;
import org.jhapy.frontend.utils.UIUtils;

public class DetailsDrawerHeader extends FlexBoxLayout {

  private Button close;
  private Label title;

  public DetailsDrawerHeader(String title, boolean showClose, boolean showTitle) {
    addClassName(BoxShadowBorders.BOTTOM);
    setFlexDirection(FlexDirection.COLUMN);
    setWidthFull();

    if (showClose) {
      this.close = UIUtils.createTertiaryInlineButton(VaadinIcon.CLOSE);
      UIUtils.setLineHeight("1", this.close);
    }

    if (showTitle) {
      this.title = UIUtils.createH4Label(title);
    }

    FlexBoxLayout wrapper;
    if (showClose & showTitle) {
      wrapper = new FlexBoxLayout(this.close, this.title);
    } else if (showTitle) {
      wrapper = new FlexBoxLayout(this.title);
    } else if (showClose) {
      wrapper = new FlexBoxLayout(this.close);
    } else {
      return;
    }

    wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
    wrapper.setPadding(Horizontal.RESPONSIVE_L, Vertical.M);
    wrapper.setSpacing(Right.L);
    add(wrapper);
  }

  public DetailsDrawerHeader(String title) {
    this(title, true, true);
  }

  public DetailsDrawerHeader(String title, Tabs tabs) {
    this(title, true, true);
    add(tabs);
  }

  public DetailsDrawerHeader(String title, Tabs tabs, boolean showClose, boolean showTitle) {
    this(title, showClose, showTitle);
    add(tabs);
  }

  public void setTitle(String title) {
    this.title.setText(title);
  }

  public void addCloseListener(ComponentEventListener<ClickEvent<Button>> listener) {
    this.close.addClickListener(listener);
  }
}