package org.jhapy.frontend.components.notification.component;

import com.github.appreciated.card.RippleClickableCard;
import org.jhapy.frontend.components.notification.NotificationHolder;
import org.jhapy.frontend.components.notification.interfaces.Notification;
import org.jhapy.frontend.components.notification.interfaces.NotificationListener;

import java.io.Serial;

public class NotificationCardView<T extends Notification> extends RippleClickableCard {

  @Serial private static final long serialVersionUID = 1L;

  private final NotificationView<T> notfication;

  public NotificationCardView(T info, NotificationHolder<T> holder, NotificationListener listener) {
    notfication = new NotificationView<>(info, holder, listener, false);
    notfication.setPadding(true);
    setBackground("var(--lumo-base-color)");
    add(notfication);
    // setWidthFull();
    setMinWidth("40%");
  }

  public NotificationView<T> getNotfication() {
    return notfication;
  }
}
