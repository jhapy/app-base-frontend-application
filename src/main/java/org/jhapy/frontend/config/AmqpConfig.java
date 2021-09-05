package org.jhapy.frontend.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 27/03/2021
 */
@Configuration
public class AmqpConfig {

  @Bean
  public FanoutExchange elementUpdate() {
    return new FanoutExchange("i18n.elementUpdate");
  }

  @Bean
  public FanoutExchange elementTrlUpdate() {
    return new FanoutExchange("i18n.elementTrlUpdate");
  }

  @Bean
  public FanoutExchange actionUpdate() {
    return new FanoutExchange("i18n.actionUpdate");
  }

  @Bean
  public FanoutExchange actionTrlUpdate() {
    return new FanoutExchange("i18n.actionTrlUpdate");
  }

  @Bean
  public FanoutExchange messageUpdate() {
    return new FanoutExchange("i18n.messageUpdate");
  }

  @Bean
  public FanoutExchange messageTrlUpdate() {
    return new FanoutExchange("i18n.messageTrlUpdate");
  }

  @Bean
  public Queue elementUpdateQueue() {
    return new AnonymousQueue();
  }

  @Bean
  public Queue elementTrlUpdateQueue() {
    return new AnonymousQueue();
  }

  @Bean
  public Queue actionUpdateQueue() {
    return new AnonymousQueue();
  }

  @Bean
  public Queue actionTrlUpdateQueue() {
    return new AnonymousQueue();
  }

  @Bean
  public Queue messageUpdateQueue() {
    return new AnonymousQueue();
  }

  @Bean
  public Queue messageTrlUpdateQueue() {
    return new AnonymousQueue();
  }

  @Bean
  public Binding bindingActionUpdate(
      @Qualifier("actionUpdate") FanoutExchange fanout,
      @Qualifier("actionUpdateQueue") Queue queue) {
    return BindingBuilder.bind(queue).to(fanout);
  }

  @Bean
  public Binding bindingActionTrlUpdate(
      @Qualifier("actionTrlUpdate") FanoutExchange fanout,
      @Qualifier("actionTrlUpdateQueue") Queue queue) {
    return BindingBuilder.bind(queue).to(fanout);
  }

  @Bean
  public Binding bindingElementUpdate(
      @Qualifier("elementUpdate") FanoutExchange fanout,
      @Qualifier("elementUpdateQueue") Queue queue) {
    return BindingBuilder.bind(queue).to(fanout);
  }

  @Bean
  public Binding bindingElementTrlUpdate(
      @Qualifier("elementTrlUpdate") FanoutExchange fanout,
      @Qualifier("elementTrlUpdateQueue") Queue queue) {
    return BindingBuilder.bind(queue).to(fanout);
  }

  @Bean
  public Binding bindingMessageUpdate(
      @Qualifier("messageUpdate") FanoutExchange fanout,
      @Qualifier("messageUpdateQueue") Queue queue) {
    return BindingBuilder.bind(queue).to(fanout);
  }

  @Bean
  public Binding bindingMessageTrlUpdate(
      @Qualifier("messageTrlUpdate") FanoutExchange fanout,
      @Qualifier("messageTrlUpdateQueue") Queue queue) {
    return BindingBuilder.bind(queue).to(fanout);
  }
}