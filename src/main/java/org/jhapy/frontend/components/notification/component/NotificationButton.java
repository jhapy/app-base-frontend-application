package org.jhapy.frontend.components.notification.component;

import com.github.appreciated.app.layout.component.appbar.ComponentBadgeWrapper;
import com.github.appreciated.app.layout.component.appbar.IconButton;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.jhapy.frontend.components.notification.NotificationComponent;
import org.jhapy.frontend.components.notification.NotificationHolder;
import org.jhapy.frontend.components.notification.interfaces.Notification;
import org.jhapy.frontend.components.notification.interfaces.NotificationsChangeListener;

import java.io.Serial;

/**
 * A component which opens a window containing the notifications on click closely to this component.
 * Also showing an indicator how many new notifications are available
 */
public class NotificationButton<T extends Notification> extends ComponentBadgeWrapper<Button>
    implements NotificationComponent {

  @Serial private static final long serialVersionUID = 1L;

  private final NotificationsOverlayView<T> notificationOverlay;
  private NotificationHolder<T> holder;

  public NotificationButton() {
    this(VaadinIcon.SEARCH);
  }

  public NotificationButton(VaadinIcon icon) {
    this(icon.create());
  }

  public NotificationButton(Component icon) {
    super(new IconButton(icon));
    notificationOverlay = new NotificationsOverlayView<>();
    addClickListener(event -> notificationOverlay.open());
  }

  public NotificationButton(VaadinIcon icon, NotificationHolder<T> holder) {
    this(icon.create());
    this.holder = holder;
    notificationOverlay.setHolder(holder);
    setClassName("app-bar-notification-button");
    holder.addNotificationsChangeListener(
        new NotificationsChangeListener<>() {
          @Override
          public void onNotificationChanges(NotificationHolder<T> holder) {
            refreshNotifications();
          }
        });
    holder.bind(getBadge());
    holder.registerNotificationComponent(this);
    refreshNotifications();
  }

  public void refreshNotifications() {
    notificationOverlay.refreshNotificationViews();
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    notificationOverlay.getElement().removeFromParent();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    attachEvent.getUI().add(notificationOverlay);
  }

  public NotificationsOverlayView<T> getNotificationOverlay() {
    return notificationOverlay;
  }

  @Override
  public boolean isDisplayingNotifications() {
    return notificationOverlay.getOpened();
  }
}
