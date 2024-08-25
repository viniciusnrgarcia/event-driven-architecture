package com.example.demo;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ServiceProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ServiceProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String message) {
        try {
            System.out.println("Sending message: " + message);
            this.kafkaTemplate.send("test-topic", message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
