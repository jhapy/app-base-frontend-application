package org.jhapy.frontend.config;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.WildcardTypePermission;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

  @Bean
  public LoggingInterceptor<Message<?>> loggingInterceptor() {
    return new LoggingInterceptor<>();
  }

  @Autowired
  public void configureLoggingInterceptorFor(
      CommandBus commandBus, LoggingInterceptor<Message<?>> loggingInterceptor) {
    commandBus.registerDispatchInterceptor(loggingInterceptor);
    commandBus.registerHandlerInterceptor(loggingInterceptor);
  }

  @Autowired
  public void configureLoggingInterceptorFor(
      EventBus eventBus, LoggingInterceptor<Message<?>> loggingInterceptor) {
    eventBus.registerDispatchInterceptor(loggingInterceptor);
  }

  @Autowired
  public void configureLoggingInterceptorFor(
      EventProcessingConfigurer eventProcessingConfigurer,
      LoggingInterceptor<Message<?>> loggingInterceptor) {
    eventProcessingConfigurer.registerDefaultHandlerInterceptor(
        (config, processorName) -> loggingInterceptor);
  }

  @Autowired
  public void configureLoggingInterceptorFor(
      QueryBus queryBus, LoggingInterceptor<Message<?>> loggingInterceptor) {
    queryBus.registerDispatchInterceptor(loggingInterceptor);
    queryBus.registerHandlerInterceptor(loggingInterceptor);
  }

  @Bean
  @Qualifier("messageSerializer")
  public Serializer messageSerializer() {
    XStream xStream = new XStream();
    xStream.addPermission(new WildcardTypePermission(new String[] {"org.jhapy.dto.**"}));
    xStream.addPermission(new WildcardTypePermission(new String[] {"org.jhapy.cqrs.**"}));

    return XStreamSerializer.builder().xStream(xStream).build();
  }
}
