package org.jhapy.frontend.dataproviders;

import java.io.Serializable;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 12/12/2020
 */
public class DefaultFilter implements Serializable {

  private String filter;
  private Boolean showInactive;

  public DefaultFilter() {}

  public DefaultFilter(String filter) {
    this(filter, null);
  }

  public DefaultFilter(String filter, Boolean showInactive) {
    this.filter = filter;
    this.showInactive = showInactive;
  }

  public static DefaultFilter getEmptyFilter() {
    return new DefaultFilter(null, null);
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public Boolean isShowInactive() {
    return showInactive;
  }

  public void setShowInactive(Boolean showInactive) {
    this.showInactive = showInactive;
  }
}