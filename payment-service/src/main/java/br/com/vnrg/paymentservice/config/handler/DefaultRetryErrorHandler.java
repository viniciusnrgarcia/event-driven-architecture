package br.com.vnrg.paymentservice.config.handler;

import br.com.vnrg.paymentservice.exceptions.RetryErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
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
    @Value("${environment.kafka.retry.interval:1000}")
    private Long interval;

    /**
     * Número de retentativas antes de enviar para fila de RETRY.
     */
    @Value("${environment.kafka.retry.max-attempts:0}")
    private Long maxAttempts;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper = new ObjectMapper();


    @Bean
    @Qualifier("retryErrorHandler")
    public DefaultErrorHandler retryErrorHandler() {
        var fixedBackOff = new FixedBackOff(this.interval, this.maxAttempts);
        var exceptionHandler = new DefaultErrorHandler((record, exception) -> {
            log.error("Error handler: {}, record: {}", exception.getMessage(), record);
            log.error("Record Error handler topic: {}, value: {} ", record.topic() + "-retry", record.value());
            this.sentTo(record);
        }, fixedBackOff);
        exceptionHandler.addRetryableExceptions(RetryErrorException.class);
        exceptionHandler.addNotRetryableExceptions(Exception.class);
        return exceptionHandler;
    }


    /**
     * Envio da mensagem para tópico RETRY
     *
     * @param record {@link org.apache.kafka.clients.consumer.ConsumerRecord}
     */
    private void sentTo(ConsumerRecord<?, ?> record) {
        log.info("Error handler sentTo topic: {}, value: {} ", record.topic() + "-retry", record.value());
        try {
            var json = mapper.writeValueAsString(record.value());
            this.kafkaTemplate.executeInTransaction(
                    t -> t.send(record.topic() + "-retry", json));

        } catch (Exception e) {
            log.error("Error in retry error handler {}", e.getMessage());
            // can use database for more complex retry mechanism
        }
    }

}
