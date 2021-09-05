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

package org.jhapy.frontend.renderer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;
import org.jhapy.frontend.utils.AppConst;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
public class BooleanOkRenderer<SOURCE> extends ComponentRenderer<Component, SOURCE> {

  protected final ValueProvider<SOURCE, Boolean> valueProvider;
  protected ValueProvider<SOURCE, String> textProvider;

  public BooleanOkRenderer(ValueProvider<SOURCE, Boolean> valueProvider) {
    this.valueProvider = valueProvider;
  }

  public BooleanOkRenderer(
      ValueProvider<SOURCE, Boolean> valueProvider, ValueProvider<SOURCE, String> textProvider) {
    this.valueProvider = valueProvider;
    this.textProvider = textProvider;
  }

  public Component createComponent(SOURCE item) {
    Image image = new Image();

    Boolean val = valueProvider.apply(item);

    if (val != null && val) {
      image.setSrc(AppConst.ICON_OK);
    } else {
      image.setSrc(AppConst.ICON_BLANK);
    }

    if (textProvider != null) {
      HorizontalLayout horizontalLayout = new HorizontalLayout();
      horizontalLayout.add(image);
      horizontalLayout.add(new Span(textProvider.apply(item)));
      return horizontalLayout;
    } else {
      return image;
    }
  }
}