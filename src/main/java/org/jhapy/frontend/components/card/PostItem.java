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

import com.github.appreciated.card.content.HorizontalCardComponentContainer;
import com.github.appreciated.card.label.PrimaryLabelComponent;
import com.vaadin.flow.component.Component;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-13
 */
public class PostItem
    extends HorizontalCardComponentContainer<com.github.appreciated.card.content.Item> {

  private final Component component;

  public PostItem(String title, String description) {
    component = new PostItemBody(title, description);
    ((PostItemBody) component).setPadding(false);
    add(component);
  }

  public PostItem withWhiteSpaceNoWrap() {
    if (component instanceof PrimaryLabelComponent) {
      ((PrimaryLabelComponent) component).setWhiteSpaceNoWrap();
    } else if (component instanceof PostItemBody) {
      ((PostItemBody) component).withWhiteSpaceNoWrap();
    }
    return this;
  }
}
