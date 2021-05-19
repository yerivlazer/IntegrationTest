package com.example.demo;

import com.example.demo.demo.Kafka;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;


@SpringBootTest(classes = {Kafka.class})
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class DemoApplicationTests {
    @Autowired
    KafkaMessageListenerContainer<String, String> container;
    private static final String TOPIC = "TOPIC";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private static Consumer<Integer, String> consumer;

    private BlockingQueue<ConsumerRecord<String, String>> records;

    @Test
    void testit() throws InterruptedException {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);


        ContainerTestUtils.waitForAssignment(container, 1);

        Producer<String, String> producer = new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new StringSerializer()).createProducer();

        producer.send(new ProducerRecord<>(TOPIC, "key", "{\"name\":\"Test Event\"}"));
        producer.flush();
        Thread.sleep(10000);


    }


}
