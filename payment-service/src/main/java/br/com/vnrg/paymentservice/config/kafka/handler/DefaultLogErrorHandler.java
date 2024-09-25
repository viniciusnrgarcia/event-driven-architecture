package br.com.vnrg.paymentservice.config.kafka.handler;

import br.com.vnrg.paymentservice.repository.EventStoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
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
public class DefaultLogErrorHandler {

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
    @Qualifier("logErrorHandler")
    public DefaultErrorHandler logErrorHandler() {
        ConsumerRecordRecoverer recover = (record, exception) -> {
            log.error("Error handler: {}, record: {}", exception.getMessage(), record);
        };
        FixedBackOff fixedBackOff = new FixedBackOff(this.interval, this.maxAttempts);
        var exceptionHandlers = new DefaultErrorHandler(recover, fixedBackOff);
        exceptionHandlers.addRetryableExceptions(Exception.class);
        return exceptionHandlers;
    }


}
