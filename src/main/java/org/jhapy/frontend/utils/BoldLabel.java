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

package org.jhapy.frontend.utils;

import com.vaadin.flow.component.html.Label;

public class BoldLabel extends Label {

  public BoldLabel(String text) {
    super.setText(text);
    super.getStyle().set("font-weight", "bold");
  }

  public BoldLabel(String text, String padding) {
    super.setText(text);
    super.getStyle().set("font-weight", "bold");
    super.getStyle().set("padding-left", padding);
  }
}