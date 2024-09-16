package br.com.vnrg.paymentservice.consumer;

import br.com.vnrg.paymentservice.domain.Payment;
import br.com.vnrg.paymentservice.exceptions.RetryErrorException;
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

    @KafkaListener(id = "${environment.kafka.consumer.retry-payment-validated.id}",
            topics = "${environment.kafka.consumer.retry-payment-validated.topics}",
            groupId = "${environment.kafka.consumer.retry-payment-validated.group-id}",
            concurrency = "${environment.kafka.consumer.retry-payment-validated.concurrency}",
            autoStartup = "${environment.kafka.consumer.retry-payment-validated.auto-startup}",
            containerFactory = "retryKafkaListenerContainerFactory"
    )
    public void listen(
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey,
            String message, Acknowledgment ack) {
        Payment payment = null;
        try {
            payment = this.mapper.readValue(message, Payment.class);
            log.info("Consumed message retry with message content: {} ", message);

            this.sendTest();
            ack.acknowledge();

        } catch (RetryErrorException retryErrorException) {
            log.error("Retry Error: {}", retryErrorException.getMessage());
            throw retryErrorException;

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }

    }

    private void sendTest() {
        throw new RetryErrorException("Error");
    }

}
