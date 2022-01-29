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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Right;
import org.jhapy.frontend.layout.size.Vertical;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;

public class DetailsDrawerFooter extends FlexBoxLayout {

  private final Button save;
  private final Button saveAndNew;
  private final Button cancel;
  private final Button delete;

  public DetailsDrawerFooter() {
    setBackgroundColor(LumoStyles.Color.Contrast._5);
    setPadding(Horizontal.RESPONSIVE_L, Vertical.S);
    setSpacing(Right.S);
    setWidthFull();

    save = UIUtils.createPrimaryButton(getTranslation("action.global.save"));
    save.setId("save");
    saveAndNew = UIUtils.createPrimaryButton(getTranslation("action.global.saveAndNew"));
    saveAndNew.setVisible(false);
    cancel = UIUtils.createTertiaryButton(getTranslation("action.global.cancel"));
    delete = UIUtils.createTertiaryButton(getTranslation("action.global.delete"));
    delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

    HorizontalLayout leftButton = new HorizontalLayout();
    leftButton.add(delete);
    leftButton.setDefaultVerticalComponentAlignment(Alignment.START);

    HorizontalLayout rightButtons = new HorizontalLayout();
    rightButtons.add(cancel, save, saveAndNew);
    rightButtons.setDefaultVerticalComponentAlignment(Alignment.END);
    setJustifyContent("space-between");
    add(leftButton, rightButtons);
  }

  public void setSaveButtonTitle(String title) {
    save.setText(title);
  }

  public void setSaveAndNewButtonTitle(String title) {
    saveAndNew.setText(title);
  }

  public void setCancelButtonTitle(String title) {
    cancel.setText(title);
  }

  public void setDeleteButtonTitle(String title) {
    delete.setText(title);
  }

  public Registration addSaveListener(ComponentEventListener<ClickEvent<Button>> listener) {
    return save.addClickListener(listener);
  }

  public Registration addSaveAndNewListener(ComponentEventListener<ClickEvent<Button>> listener) {
    return saveAndNew.addClickListener(listener);
  }

  public Registration addCancelListener(ComponentEventListener<ClickEvent<Button>> listener) {
    return cancel.addClickListener(listener);
  }

  public Registration addDeleteListener(ComponentEventListener<ClickEvent<Button>> listener) {
    return delete.addClickListener(listener);
  }

  public void setSaveButtonVisible(boolean isVisible) {
    save.setVisible(isVisible);
  }

  public void setSaveAndNewButtonVisible(boolean isVisible) {
    saveAndNew.setVisible(isVisible);
  }

  public void setCancelButtonVisible(boolean isVisible) {
    cancel.setVisible(isVisible);
  }

  public void setDeleteButtonVisible(boolean isVisible) {
    delete.setVisible(isVisible);
  }

  public void setSaveButtonEnabled(boolean isEnabled) {
    save.setEnabled(isEnabled);
  }

  public void setSaveAndNewButtonEnabled(boolean isEnabled) {
    saveAndNew.setEnabled(isEnabled);
  }

  public void setCancelButtonEnabled(boolean isEnabled) {
    cancel.setEnabled(isEnabled);
  }

  public void setDeleteButtonEnabled(boolean isEnabled) {
    delete.setEnabled(isEnabled);
  }
}
