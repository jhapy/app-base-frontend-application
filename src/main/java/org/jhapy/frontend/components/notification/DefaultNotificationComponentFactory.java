package org.jhapy.frontend.components.notification;

import com.github.appreciated.app.layout.component.builder.interfaces.PairComponentFactory;
import com.vaadin.flow.component.Component;
import org.jhapy.frontend.components.notification.component.NotificationView;
import org.jhapy.frontend.components.notification.interfaces.Notification;
import org.jhapy.frontend.components.notification.interfaces.NotificationListener;

public class DefaultNotificationComponentFactory<T extends Notification>
    implements PairComponentFactory<NotificationHolder<T>, T> {

  @Override
  public Component getComponent(NotificationHolder<T> holder, T info) {
    return new NotificationView<>(
        info,
        holder,
        new NotificationListener() {
          @Override
          public void onClick() {
            holder.onNotificationClicked(info);
          }

          @Override
          public void onDismiss() {
            holder.onNotificationDismissed(info);
          }
        },
        true);
  }
}
