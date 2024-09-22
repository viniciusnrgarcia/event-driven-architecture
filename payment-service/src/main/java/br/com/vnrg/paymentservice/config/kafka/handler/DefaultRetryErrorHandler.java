package br.com.vnrg.paymentservice.config.kafka.handler;

import br.com.vnrg.paymentservice.exceptions.RetryErrorException;
import br.com.vnrg.paymentservice.repository.EventStoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;


/**
 * Retry error handler
 * <p>
 * {@link org.springframework.kafka.listener.DefaultErrorHandler}
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultRetryErrorHandler {

    /**
     * Intervalo entre tentativas, de cada exception esperada ocorrida.
     */
    @Value("${environment.kafka.retry.interval:10000}")
    public Long interval;

    /**
     * Número de retentativas antes de enviar para fila de RETRY.
     */
    @Value("${environment.kafka.retry.max-attempts:10}")
    public Long maxAttempts;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private final EventStoreRepository eventStoreRepository;

    private final JdbcClient jdbcClient;


    @Bean
    @Qualifier("retryErrorHandler")
    public DefaultErrorHandler retryErrorHandler() {
        ConsumerRecordRecoverer recover = (record, exception) -> {
            log.error("Error handler: {}, record: {}", exception.getMessage(), record);
            if (exception.getCause() instanceof RetryErrorException) {
                log.error("Record Error handler topic: {}, value: {} ", record.topic() + "-retry", record.value());
                this.saveEventToRetry(record);
            }
        };
        FixedBackOff fixedBackOff = new FixedBackOff(this.interval, this.maxAttempts);
        var exceptionHandlers = new DefaultErrorHandler(recover, fixedBackOff);
        exceptionHandlers.addRetryableExceptions(RetryErrorException.class);
        exceptionHandlers.addNotRetryableExceptions(Exception.class, RuntimeException.class,
                NullPointerException.class);
        return exceptionHandlers;
    }

    private void saveEventToRetry(ConsumerRecord<?, ?> record) {
        try {
            MapSqlParameterSource param = new MapSqlParameterSource();
            param.addValue("id", record.key());
            param.addValue("topicName", record.topic());
            param.addValue("status", "A");
            param.addValue("createdBy", "payment-service");
            param.addValue("json", record.value());

            jdbcClient.sql("INSERT INTO kafka_event_retry (id, topic_name, created_by, status, json) VALUES (:id, :topicName, :createdBy, :status, :json::jsonb)")
                    .paramSource(param)
                    .update();

        } catch (Exception e) {
            log.error("Error save retry data: {}, exception: {} ", record.value(), e.getMessage());
            throw new RuntimeException(e);
        }

//        try {
//            this.eventStoreRepository.save(
//                    new EventRetry(record.key().toString(),
//                            record.topic(),
//                            "payment-serve",
//                            "A",
//                            record.value().toString())
//            );
//        } catch (Exception e) {
//            log.error("Error in retry error handler {}", e.getMessage());
//        }

    }


    /**
     * Envio da mensagem para tópico RETRY
     *
     * @param record {@link org.apache.kafka.clients.consumer.ConsumerRecord}
     */
    public void sentTo(ConsumerRecord<?, ?> record) {
        log.info("Error handler sentTo topic: {}, value: {} ", record.topic() + "-retry", record.value());
        try {
            var topic = this.getRetryTopic(record.topic());
            var json = record.value().toString();
            this.kafkaTemplate.executeInTransaction(t -> t.send(topic, json));

        } catch (Exception e) {
            log.error("Error in retry error handler {}", e.getMessage());
            // can use database for more complex retry mechanism
        }
    }


    private String getRetryTopic(String topic) {
        var suffixValue = "-retry";
        if (topic.contentEquals(suffixValue)) {
            return topic;
        }
        return topic + suffixValue;
    }

}
