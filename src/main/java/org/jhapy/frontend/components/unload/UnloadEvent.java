package org.jhapy.frontend.components.unload;

import com.vaadin.flow.component.ComponentEvent;

public class UnloadEvent extends ComponentEvent<UnloadObserver> {
  private final boolean becauseOfQuerying;

  public UnloadEvent(UnloadObserver source, boolean attempted) {
    super(source, true);
    this.becauseOfQuerying = attempted;
  }

  public boolean isBecauseOfQuerying() {
    return this.becauseOfQuerying;
  }
}