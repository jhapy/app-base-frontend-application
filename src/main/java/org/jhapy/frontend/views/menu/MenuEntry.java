package org.jhapy.frontend.views.menu;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouteConfiguration;
import lombok.Data;
import org.jhapy.commons.utils.HasLogger;

import java.io.Serializable;
import java.util.List;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 28/07/2020
 */
@Data
public class MenuEntry implements Serializable, HasLogger {

  private String id;
  private VaadinIcon vaadinIcon;
  private String title;
  private Class targetClass;
  private Object targetParams;
  private String targetId;
  private Object relatedObject;
  private MenuEntry parentMenuEntry;
  private Boolean canCreateSubEntries = Boolean.FALSE;
  private List<MenuAction> contextMenu;
  private Boolean hasChildNodes = Boolean.TRUE;
  public MenuEntry(String id) {
    this.id = id;
  }

  public void setTargetClass(Class targetClass, Class routerLayout) {
    var loggerPrefix = getLoggerPrefix("setTargetClass");
    this.targetClass = targetClass;
    // UpNavigationHelper.registerNavigationRoute(targetClass);
    RouteConfiguration configuration = RouteConfiguration.forSessionScope();
    /*
       String route = id;
       if ( ! configuration.isPathRegistered(route) )  {
         logger().debug(loggerPrefix+"Register Route : Path = " + route + ", Target = " + targetClass.getSimpleName());
         configuration.setRoute(route, targetClass, routerLayout);
       } else {
         //logger().warn(loggerPrefix+"Route already registered : Path = " + route + ", Target = " + targetClass.getSimpleName());
       }
    */
  }
}