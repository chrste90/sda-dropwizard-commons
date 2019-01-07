package org.sdase.commons.server.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.sdase.commons.server.dropwizard.bundles.ConfigurationSubstitutionBundle;
import org.sdase.commons.server.kafka.builder.MessageHandlerRegistration;
import org.sdase.commons.server.kafka.consumer.IgnoreAndProceedErrorHandler;
import org.sdase.commons.server.kafka.model.Key;
import org.sdase.commons.server.kafka.model.Value;
import org.sdase.commons.server.kafka.serializers.KafkaJsonDeserializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class KafkaExampleConsumerApplication extends Application<KafkaExampleConfiguration> {

   private final KafkaBundle<KafkaExampleConfiguration> kafka = KafkaBundle.builder().withConfigurationProvider(KafkaExampleConfiguration::getKafka).build();

   private List<Value> receivedMessages = new ArrayList<>();
   private  List<Long> receivedLongs = new ArrayList<>();

   @Override
   public void initialize(Bootstrap<KafkaExampleConfiguration> bootstrap) {
      // Add ConfigurationSubstitutionBundle to allow correct substitution of bootstrap servers in Test case
      bootstrap.addBundle(ConfigurationSubstitutionBundle.builder().build());
      bootstrap.addBundle(kafka);
   }

   @Override
   public void run(KafkaExampleConfiguration configuration, Environment environment)  {
      ObjectMapper configuredObjectMapper = environment.getObjectMapper();

      createExampleMessageListener(configuredObjectMapper);
      createExampleMessageListenerWithConfiguration();

   }

   /**
    * Example1: create a new simple message listener with a simple handler that just stores
    * the record values within a list
    * It uses
    *     * <ul>
    *     *  <li>the default listener config</li>
    *     *  <li>the default consumer</li>
    *     *  <li>deserializers for JSON </li>
    *     *  <li>the default error handler</li>
    *     * </ul>
    *
    * @param configuredObjectMapper Object mapper for deserializing JSON
    */
   private void createExampleMessageListener(ObjectMapper configuredObjectMapper) {
      kafka.registerMessageHandler(
            MessageHandlerRegistration.<Key, Value>builder()
            .withDefaultListenerConfig()
            .forTopicConfigs(Collections.singletonList(kafka.getTopicConfiguration("example0")))
            .withDefaultConsumer()
            .withKeyDeserializer(new KafkaJsonDeserializer<>(configuredObjectMapper, Key.class))
            .withValueDeserializer(new KafkaJsonDeserializer<>(configuredObjectMapper, Value.class))
            .withHandler(record -> receivedMessages.add(record.value()))
            .withErrorHandler(new IgnoreAndProceedErrorHandler<>())
            .build()
      );
   }

   /**
    * Example2: create a new message listener with a simple handler that just stores the record values within a list
    * It uses
    * <ul>
    *  <li>a customized listener that polls every second configured within the yaml</li>
    *  <li>a customized consumer configured within the yaml</li>
    *  <li>deserializers for Long values</li>
    *  <li>the default error handler</li>
    * </ul>
    *
    */
   private void createExampleMessageListenerWithConfiguration() {
      kafka.registerMessageHandler(
            MessageHandlerRegistration.<Long, Long>builder()
                  .withListenerConfig("example1")
                  .forTopicConfigs(Collections.singletonList(kafka.getTopicConfiguration("example1")))
                  .withConsumerConfig("consumerConfigExample")
                  .withKeyDeserializer(new LongDeserializer())
                  .withValueDeserializer(new LongDeserializer())
                  .withHandler(record -> receivedLongs.add(record.value()))
                  .withErrorHandler(new IgnoreAndProceedErrorHandler<>())
                  .build()
      );
   }

   /**
    * Method just for testing and should not be implemented in real applications.
    */
   List<Value> getReceivedMessages() {
      return receivedMessages;
   }

   /**
    * Method just for testing and should not be implemented in real applications.
    */
   List<Long> getReceivedLongs() {
      return receivedLongs;
   }
}