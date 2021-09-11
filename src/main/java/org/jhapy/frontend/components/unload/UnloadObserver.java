package org.jhapy.frontend.components.unload;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.vaadin.miki.markers.WithIdMixin;

import java.io.Serializable;

@JsModule("./unload-observer.js")
@Tag("unload-observer")
public final class UnloadObserver extends PolymerTemplate<TemplateModel>
    implements WithIdMixin<UnloadObserver> {
  private boolean queryingOnUnload;
  private boolean clientInitialised;

  public static UnloadObserver get() {
    UI ui = UI.getCurrent();
    if (ui == null) {
      throw new IllegalStateException("there is no UI available to create UnloadObserver for");
    } else {
      return get(ui);
    }
  }

  public static UnloadObserver get(UI ui) {
    UnloadObserver instance = (UnloadObserver) ComponentUtil.getData(ui, UnloadObserver.class);
    if (instance == null) {
      instance = new UnloadObserver();
      ComponentUtil.setData(ui, UnloadObserver.class, instance);
    }

    return instance;
  }

  public static UnloadObserver getAttached() {
    return getAttached(UI.getCurrent());
  }

  public static <C extends Component & HasComponents> UnloadObserver getAttached(C parent) {
    if (parent == null) {
      throw new NullPointerException(
          "parent component to attach UnloadObserver to must not be null");
    } else if (!parent.getUI().isPresent()) {
      throw new IllegalArgumentException(
          "parent component is not attached to any UI, hence UnloadObserver cannot be added to it");
    } else {
      UnloadObserver observer = get((UI) parent.getUI().get());
      if (observer.getParent().isPresent()) {
        Component currentParent = (Component) observer.getParent().get();
        if (currentParent != parent) {
          if (!(currentParent instanceof HasComponents)) {
            throw new IllegalStateException(
                "UnloadObserver is currently attached to "
                    + currentParent.getClass().getName()
                    + " which is not HasComponents and cannot be automatically removed");
          }

          ((HasComponents) currentParent).remove(new Component[] {observer});
          ((HasComponents) parent).add(new Component[] {observer});
        }
      } else {
        ((HasComponents) parent).add(new Component[] {observer});
      }

      return observer;
    }
  }

  private UnloadObserver() {
    this(true);
  }

  private UnloadObserver(boolean queryOnUnload) {
    this.setQueryingOnUnload(queryOnUnload);
  }

  public void setQueryingOnUnload(boolean queryingOnUnload) {
    if (queryingOnUnload != this.queryingOnUnload) {
      this.queryingOnUnload = queryingOnUnload;
      this.getElement()
          .getNode()
          .runWhenAttached(
              (ui) -> {
                ui.beforeClientResponse(
                    this,
                    (context) -> {
                      this.getElement()
                          .callJsFunction(
                              "queryOnUnload", new Serializable[] {this.queryingOnUnload});
                    });
              });
    }
  }

  public boolean isQueryingOnUnload() {
    return this.queryingOnUnload;
  }

  public UnloadObserver withQueryingOnUnload(boolean value) {
    this.setQueryingOnUnload(value);
    return this;
  }

  public UnloadObserver withQueryingOnUnload() {
    return this.withQueryingOnUnload(true);
  }

  public UnloadObserver withoutQueryingOnUnload() {
    return this.withQueryingOnUnload(false);
  }

  protected void onAttach(AttachEvent attachEvent) {
    if (!this.clientInitialised) {
      this.getElement().callJsFunction("initObserver", new Serializable[0]);
      this.clientInitialised = true;
    }

    super.onAttach(attachEvent);
  }

  protected void onDetach(DetachEvent detachEvent) {
    this.clientInitialised = false;
    detachEvent
        .getUI()
        .getPage()
        .executeJs(
            "if (window.Vaadin.unloadObserver.attemptHandler !== undefined) {    window.removeEventListener('beforeunload', window.Vaadin.unloadObserver.attemptHandler);}",
            new Serializable[0]);
    super.onDetach(detachEvent);
  }

  @ClientCallable
  private void unloadHappened() {
    this.fireUnloadEvent(new UnloadEvent(this, false));
  }

  @ClientCallable
  private void unloadAttempted() {
    this.fireUnloadEvent(new UnloadEvent(this, true));
  }

  protected void fireUnloadEvent(UnloadEvent event) {
    this.getEventBus().fireEvent(event);
  }

  public Registration addUnloadListener(UnloadListener listener) {
    return this.getEventBus().addListener(UnloadEvent.class, listener);
  }
}