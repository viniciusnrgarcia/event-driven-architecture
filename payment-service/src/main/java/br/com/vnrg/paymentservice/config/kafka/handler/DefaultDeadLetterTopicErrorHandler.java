package br.com.vnrg.paymentservice.config.kafka.handler;

import br.com.vnrg.paymentservice.exceptions.DeadLetterTopicException;
import br.com.vnrg.paymentservice.repository.EventStoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;


/**
 * Retry error handler
 * <p>
 * {@link DefaultErrorHandler}
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultDeadLetterTopicErrorHandler {

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
    @Qualifier("deadLetterHandler")
    public DefaultErrorHandler deadLetterHandler() {
        DeadLetterPublishingRecoverer deadLetterRecoverer =
                new DeadLetterPublishingRecoverer(this.kafkaTemplate, (consumerRecord, ex) -> {
                    log.info("DeadLetterPublishingRecoverer ");
                    return new TopicPartition("DLT-TOPIC", -1);
                });

//        ConsumerRecordRecoverer recover = (record, exception) -> {
//            log.error("DefaultDeadLetterTopicErrorHandler Error handler: {}, record: {}", exception.getMessage(), record);
//        };

        FixedBackOff fixedBackOff = new FixedBackOff(this.interval, this.maxAttempts);
        var exceptionHandlers = new DefaultErrorHandler(deadLetterRecoverer, fixedBackOff);
        exceptionHandlers.addRetryableExceptions(DeadLetterTopicException.class);
        return exceptionHandlers;
    }


}
