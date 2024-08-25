package br.com.vnrg.paymentservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    // TODO configure properties + handle errors + retries + backoff

    @Value("${environment.kafka.bootstrap-servers}")
    private String bootstrapServers;


    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setIdleBetweenPolls(1); // The sleep interval in milliseconds used in the main
        // loop between org. apache. kafka. clients. consumer. Consumer. poll(Duration) calls. Defaults to 0 - no idling.
        factory.setConcurrency(1);
//        factory.setCommonErrorHandler(errorHandler()); todo: handle errors
        return factory;
    }

//    public DefaultErrorHandler errorHandler(){
//        return new DefaultErrorHandler((record, exception) -> {
//                    // recover after 3 failures, with no back off - e.g. send to a dead-letter topic
//                }, new FixedBackOff(0L, 2L));
//    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        // props.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "120000"); //  determina o intervalo limite que o consumer irá ficar processando as mensagens do último poll(). Se o consumer não realizar uma outra chamada dentro desse tempo, o coordinator vai considerar o consumer em falha e vai provocar um rebalance. Se sua aplicação demora muito tempo para processar os eventos você pode aumentar esse parâmetro para esperar mais tempo e conseguir processar os eventos no tempo adequado. Só tenha em mente que se aumentar muito esse valor pode fazer com que o coordinator demore mais para descobrir que sua aplicação falhou.
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1"); // O numero máximo de eventos retornado em uma simples chamada de poll(). O valor default é 500.

        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "300000"); // specifica o máximo de tempo em milissegundos que um consumer dentro de um consumer group pode ficar sem enviar heartbeat antes de ser considerado inativo e provocar um rebalance.
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "60000");

        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "300000");

        /**
         * Se você aumentar o fetch.min.bytes, por exemplo, com um valor maior que o padrão o consumer vai realizar
         * menos requests para o broker e irá trazer mais dados em um único batch, consequentemente irá reduzir a
         * sobrecarga de CPU no consumer e no broker.
         */
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "60000");
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        return props;
    }
}