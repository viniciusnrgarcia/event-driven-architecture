package com.example.demo;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"test-topic"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class KafkaIntegrationTest {

    private static final String TOPIC = "test-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final CountDownLatch latch = new CountDownLatch(2);
    private String receivedMessage = null;
    private List<String> messages = new ArrayList<>();

    @KafkaListener(topics = TOPIC, groupId = "test-group")
    public void listen(ConsumerRecord<String, String> record) {
        receivedMessage = record.value();
        messages.add(record.value());
        latch.countDown();
    }

    @Test
    void testKafkaProducerAndConsumer() throws InterruptedException {
        String message = "Hello, Kafka!";
        // kafkaTemplate.send(TOPIC, message);
        this.serviceProducer.send(message);
        latch.await(5, TimeUnit.SECONDS);
        // assertThat(receivedMessage).isEqualTo(message);
        assertThat(messages).contains(message);

    }

    @Autowired
    private ServiceProducer serviceProducer;

}