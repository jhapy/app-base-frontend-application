package org.jhapy.frontend.dataproviders;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import org.jhapy.frontend.views.JHapyMainView3;
import org.jhapy.frontend.views.menu.MenuData;
import org.jhapy.frontend.views.menu.MenuEntry;

import java.util.function.Consumer;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 19/08/2020
 */
public abstract class MenuHierarchicalDataProvider
    extends AbstractBackEndHierarchicalDataProvider<MenuEntry, Void> {

  protected MenuData rootMenu;
  protected JHapyMainView3 mainView;

  protected Consumer<MenuData> dataObserver;

  public MenuHierarchicalDataProvider() {}

  public JHapyMainView3 getMainView() {
    return mainView;
  }

  public void setMainView(JHapyMainView3 mainView) {
    this.mainView = mainView;
  }

  public MenuHierarchicalDataProvider(MenuData rootMenu) {
    this.rootMenu = rootMenu;
  }

  public MenuData getRootMenu() {
    return rootMenu;
  }

  public void setRootMenu(MenuData rootMenu) {
    this.rootMenu = rootMenu;
  }

  public Consumer<MenuData> getDataObserver() {
    return dataObserver;
  }

  public void setDataObserver(Consumer<MenuData> dataObserver) {
    this.dataObserver = dataObserver;
  }
}