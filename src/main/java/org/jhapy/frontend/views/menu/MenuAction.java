package org.jhapy.frontend.views.menu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;
import lombok.Data;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 31/07/2020
 */
@Data
public class MenuAction implements Serializable {

  private String title;
  private Component component;
  private Consumer<GridContextMenuItemClickEvent<MenuEntry>> clickListener;

  public MenuAction(String title) {
    this.title = title;
  }

  public MenuAction(Component component) {
    this.component = component;
  }

  public MenuAction(
      String title, Consumer<GridContextMenuItemClickEvent<MenuEntry>> clickListener) {
    this.title = title;
    this.clickListener = clickListener;
  }

  public MenuAction(
      Component component, Consumer<GridContextMenuItemClickEvent<MenuEntry>> clickListener) {
    this.component = component;
    this.clickListener = clickListener;
  }
}