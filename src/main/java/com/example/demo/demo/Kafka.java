package com.example.demo.demo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentBatchErrorHandler;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Kafka {
    private static final String TOPIC = "TOPIC";
    private static final String GROUP = "group";
    private static final String BROKER = "localhost:9092";

    @Bean
    public StringJsonMessageConverter converter() {
        return new StringJsonMessageConverter();
    }

    @Bean
    public KafkaMessageDrivenChannelAdapter<String, String> adapter(KafkaMessageListenerContainer<String, String> container) {
        KafkaMessageDrivenChannelAdapter<String, String> kafkaMessageDrivenChannelAdapter =
                new KafkaMessageDrivenChannelAdapter<>(container, KafkaMessageDrivenChannelAdapter.ListenerMode.batch);
        kafkaMessageDrivenChannelAdapter.setBatchMessageConverter(new BatchMessagingMessageConverter(converter()));
        kafkaMessageDrivenChannelAdapter.setOutputChannelName("converterChannel");

        return kafkaMessageDrivenChannelAdapter;
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> container() {
        ContainerProperties properties = new ContainerProperties(TOPIC);
        properties.setPollTimeout(1000);
        properties.setAckMode(ContainerProperties.AckMode.BATCH);
        KafkaMessageListenerContainer<String, String> kafkaContainer = new KafkaMessageListenerContainer<>(consumerFactory(), properties);
        SeekToCurrentBatchErrorHandler ha = new SeekToCurrentBatchErrorHandler();
        ha.setBackOff(new FixedBackOff(500, 3));
        kafkaContainer.setBatchErrorHandler(ha);
        return kafkaContainer;
    }


    @Bean
    @ServiceActivator(inputChannel = "converterChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                System.out.println(message.getPayload());
            }
        };
    }

    @Bean
    public PublishSubscribeChannel converterChannel() {
        return new PublishSubscribeChannel ();
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);


        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }



}
