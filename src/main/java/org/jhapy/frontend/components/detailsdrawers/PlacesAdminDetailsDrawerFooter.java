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
import com.vaadin.flow.shared.Registration;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Right;
import org.jhapy.frontend.layout.size.Vertical;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;

public class PlacesAdminDetailsDrawerFooter extends FlexBoxLayout {

  private final Button create;
  private final Button cancel;

  public PlacesAdminDetailsDrawerFooter() {
    setBackgroundColor(LumoStyles.Color.Contrast._5);
    setPadding(Horizontal.RESPONSIVE_L, Vertical.S);
    setSpacing(Right.S);
    setWidthFull();

    create = UIUtils.createPrimaryButton(getTranslation("action.global.create"));
    cancel = UIUtils.createTertiaryButton(getTranslation("action.global.cancel"));

    add(create, cancel);
  }

  public Registration addCreateListener(ComponentEventListener<ClickEvent<Button>> listener) {
    return create.addClickListener(listener);
  }

  public Registration addCancelListener(ComponentEventListener<ClickEvent<Button>> listener) {
    return cancel.addClickListener(listener);
  }

  public void setCreateButtonVisible(boolean isVisible) {
    create.setVisible(isVisible);
  }

  public void setCancelButtonVisible(boolean isVisible) {
    cancel.setVisible(isVisible);
  }
}
