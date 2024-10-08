package br.com.vnrg.paymentfraudprocess.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${environment.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<Long, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        configMap.put(ProducerConfig.ACKS_CONFIG, "all"); // See https://kafka.apache.org/documentation/#producerconfigs_acks
        configMap.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
        configMap.put(ProducerConfig.RETRIES_CONFIG, "60000");
        // configMap.put(ProducerConfig.LINGER_MS_CONFIG, "1");
        // configMap.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        // configMap.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432");
        configMap.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // Ensure don't push duplicates messages
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // See https://kafka.apache.org/documentation/#producerconfigs for more properties
        return configMap;
    }

    @Bean
    public KafkaTemplate<Long, String> kafkaTemplate() {
        return new KafkaTemplate<Long, String>(producerFactory());
    }


}
