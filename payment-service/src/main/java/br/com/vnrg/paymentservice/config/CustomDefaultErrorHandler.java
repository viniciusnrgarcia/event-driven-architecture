package br.com.vnrg.paymentservice.config;

import br.com.vnrg.paymentservice.exceptions.RetryErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomDefaultErrorHandler {

    private final KafkaTemplate<String, String> kafkaTemplate;

//    @Value("${environment.kafka.consumer.retry-payment-validated.topics}")
//    public String topic;


    @Bean
    @Qualifier("customErrorHandler")
    public DefaultErrorHandler errorHandler() {
        FixedBackOff fixedBackOff = new FixedBackOff(5_000, 3);
        // recover after 3 failures, with no back off - e.g. send to a dead-letter topic
        DefaultErrorHandler eh = new DefaultErrorHandler((record, exception) -> {
            log.error("Error handler: {}", exception.getMessage());
            var value = record.value();
            log.error("Record Error handler: {}", value);

            // this.kafkaTemplate.send(topic, value);

        }, fixedBackOff);
        eh.addRetryableExceptions(RetryErrorException.class);
        eh.addNotRetryableExceptions(Exception.class);
        return eh;
    }

}
