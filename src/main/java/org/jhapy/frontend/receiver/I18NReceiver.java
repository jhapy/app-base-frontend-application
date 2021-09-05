package org.jhapy.frontend.receiver;

import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.messageQueue.*;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-03
 */
@Component
public class I18NReceiver implements HasLogger {

  private final MyI18NProvider i18NService;

  public I18NReceiver(MyI18NProvider i18NService) {
    this.i18NService = i18NService;
  }

  @RabbitListener(queues = "#{elementUpdateQueue.name}")
  public void onElementUpdate(final I18NElementUpdate update) {
    var loggerPrefix = getLoggerPrefix("onElementUpdate", update);

    logger().info(loggerPrefix + "Message received");

    i18NService.elementUpdate(update.getUpdateType(), update.getElement());
  }

  @RabbitListener(queues = "#{elementTrlUpdateQueue.name}")
  public void onElementTrlUpdate(final I18NElementTrlUpdate update) {
    var loggerPrefix = getLoggerPrefix("onElementTrlUpdate", update);

    logger().info(loggerPrefix + "Message received");

    i18NService.elementTrlUpdate(update.getUpdateType(), update.getElementTrl());
  }

  @RabbitListener(queues = "#{actionUpdateQueue.name}")
  public void onActionUpdate(final I18NActionUpdate update) {
    var loggerPrefix = getLoggerPrefix("onActionUpdate", update);

    logger().info(loggerPrefix + "Message received");

    i18NService.actionUpdate(update.getUpdateType(), update.getAction());
  }

  @RabbitListener(queues = "#{actionTrlUpdateQueue.name}")
  public void onActionTrlUpdate(final I18NActionTrlUpdate update) {
    var loggerPrefix = getLoggerPrefix("onActionTrlUpdate", update);

    logger().info(loggerPrefix + "Message received");

    i18NService.actionTrlUpdate(update.getUpdateType(), update.getActionTrl());
  }

  @RabbitListener(queues = "#{messageUpdateQueue.name}")
  public void onMessageUpdate(final I18NMessageUpdate update) {
    var loggerPrefix = getLoggerPrefix("onMessageUpdate", update);

    logger().info(loggerPrefix + "Message received");

    i18NService.messageUpdate(update.getUpdateType(), update.getMessage());
  }

  @RabbitListener(queues = "#{messageTrlUpdateQueue.name}")
  public void onMessageTrlUpdate(final I18NMessageTrlUpdate update) {
    var loggerPrefix = getLoggerPrefix("onMessageTrlUpdate", update);

    logger().info(loggerPrefix + "Message received");

    i18NService.messageTrlUpdate(update.getUpdateType(), update.getMessageTrl());
  }
}