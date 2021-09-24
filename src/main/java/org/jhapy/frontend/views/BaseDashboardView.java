/*
 *   E-FLIGHT MGT CONFIDENTIAL
 *    __________________
 *
 *    [2019] E-Flight Mgt
 *    All Rights Reserved.
 *
 *    NOTICE:  All information contained herein is, and remains the property of "E-Flight Mgt"
 *    and its suppliers, if any. The intellectual and technical concepts contained herein are
 *    proprietary to "E-Flight Mgt" and its suppliers and may be covered by Morocco, Switzerland and Foreign
 *    Patents, patents in process, and are protected by trade secret or copyright law.
 *    Dissemination of this information or reproduction of this material is strictly forbidden unless
 *    prior written permission is obtained from "E-Flight Mgt".
 */

package org.jhapy.frontend.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import org.jhapy.frontend.layout.ViewFrame;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 2/11/20
 */
@CssImport("./styles/dashboard.css")
public abstract class BaseDashboardView extends ViewFrame {
  protected Long menuParentId;

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    setViewContent(createContent());
  }

  protected abstract Component createContent();

  public void setMenuParentId(long menuParentId) {
    this.menuParentId = menuParentId;
  }
}