package br.com.vnrg.paymentservice.config.kafka.handler;

import br.com.vnrg.paymentservice.repository.EventStoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.ManualAckListenerErrorHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


/**
 * Retry error handler
 * <p>
 * {@link DefaultErrorHandler}
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultCustomLogErrorHandler implements ManualAckListenerErrorHandler {

    /**
     * Intervalo entre tentativas, de cada exception esperada ocorrida.
     */
    @Value("${environment.kafka.retry.interval:5000}")
    public Long interval;

    /**
     * Número de retentativas antes de enviar para fila de RETRY.
     */
    @Value("${environment.kafka.retry.max-attempts:5}")
    public Long maxAttempts;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private final EventStoreRepository eventStoreRepository;

    private final JdbcClient jdbcClient;

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer, Acknowledgment ack) {
        log.info("HandleErrorCustom: {}", exception.getMessage());
        return consumer;
    }

//    @Override
//    public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
//        log.info("Handler error");
//        return null;
//    }
//
//    @Override
//    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
//        log.info("hahaha");
//        return KafkaListenerErrorHandler.super.handleError(message, exception, consumer);
//    }
//
//    @Override
//    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer, Acknowledgment ack) {
//        log.info("hahaha");
//        return KafkaListenerErrorHandler.super.handleError(message, exception, consumer, ack);
//    }


//    @Bean
//    @Qualifier("logErrorHandler")
//    public DefaultErrorHandler logErrorHandler() {
//        ConsumerRecordRecoverer recover = (record, exception) -> {
//            log.error("Error handler: {}, record: {}", exception.getMessage(), record);
//            if (exception.getCause() instanceof RetryErrorException) {
//                log.error("Record Error handler topic: {}, value: {} ", record.topic() + "-retry", record.value());
//            }
//        };
//        FixedBackOff fixedBackOff = new FixedBackOff(this.interval, this.maxAttempts);
//        var exceptionHandlers = new DefaultErrorHandler(recover, fixedBackOff);
//        exceptionHandlers.addRetryableExceptions(Exception.class);
//        return exceptionHandlers;
//    }


//    @Bean
//    public KafkaListenerErrorHandler logErrorHandler(DeadLetterPublishingRecoverer recoverer) {
//        return (msg, ex) -> {
//            if (msg.getHeaders().get(KafkaHeaders.DELIVERY_ATTEMPT, Integer.class) > 9) {
//                recoverer.accept(msg.getHeaders().get(KafkaHeaders.RAW_DATA, ConsumerRecord.class), ex);
//
//                log.info("ERror handler: {}, record: {}", ex.getMessage(), msg.getHeaders().get(KafkaHeaders.RAW_DATA, ConsumerRecord.class));
//
//                return "FAILED";
//            }
//            throw ex;
//        };
//    }

}
