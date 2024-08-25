package com.example.demo;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    public String bootstrapServers;

    @Value("${spring.kafka.protocol:}")
    public String protocol;
    // "SASL_PLAINTEXT"

    @Value("${spring.kafka.sasl:}")
    public String sasl;
    // "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"admin-secret\";"

    @Value("${spring.kafka.jaas:}")
    public String jaas;
//    "PLAIN"



    public Map<String, Object> producerConfigs() {
        Map<String, Object> map = new HashMap<>();
        map.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        map.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        map.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        map.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        map.put(ProducerConfig.ACKS_CONFIG, "all");
        map.put(ProducerConfig.RETRIES_CONFIG, 1);
        map.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        map.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        map.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        if (sasl != null && !sasl.isBlank()) {
            map.put("sasl.mechanism", sasl);
            map.put("security.protocol", protocol );
            map.put("sasl.jaas.config", jaas);
        }
        return map;
    }

    @Bean
    public DefaultKafkaProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
