package org.jhapy.frontend.views.menu;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 28/07/2020
 */
public class MenuData implements Serializable {

  private final List<MenuEntry> menuList = new ArrayList<>();

  public void addMenuEntry(MenuEntry... menuEntries) {
    menuList.addAll(Arrays.asList(menuEntries));
  }

  public List<MenuEntry> getMenuList() {
    return menuList;
  }

  public List<MenuEntry> getRootItems() {
    return menuList.stream()
        .filter(department -> department.getParentMenuEntry() == null)
        .collect(Collectors.toList());
  }

  public List<MenuEntry> getChildItems(MenuEntry parent) {
    return menuList.stream()
        .filter(menuEntry -> Objects.equals(menuEntry.getParentMenuEntry(), parent))
        .collect(Collectors.toList());
  }

  public Optional<MenuEntry> getFirstMenuByTargetId(String targetId) {
    return menuList.stream()
        .filter(
            menuEntry ->
                menuEntry.getTargetId() != null
                    && menuEntry.getTargetId().equalsIgnoreCase(targetId))
        .findFirst();
  }
}
