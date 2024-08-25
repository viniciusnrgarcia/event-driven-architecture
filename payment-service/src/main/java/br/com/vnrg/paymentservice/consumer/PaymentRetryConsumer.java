package br.com.vnrg.paymentservice.consumer;

import br.com.vnrg.paymentservice.domain.Payment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentRetryConsumer {

    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(id = "payment-service-retry-id",
            topics = "payment-validated-retry", groupId = "payment-service-retry-group", concurrency = "${listen.concurrency:1}",
            autoStartup = "${listen.auto.start:false}"
    )
    public void listen(
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey,
            String message, Acknowledgment ack) throws JsonProcessingException {
        Payment payment = null;
        try {
            payment = this.mapper.readValue(message, Payment.class);
            log.info("Consumed message retry with key: {} message content: {} ", messageKey, message);
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        } finally {
            ack.acknowledge();
        }
    }

}
