package br.com.vnrg.paymentsend.messaging;

import br.com.vnrg.paymentsend.domain.Payment;
import br.com.vnrg.paymentsend.repository.PaymentErrorRepository;
import br.com.vnrg.paymentsend.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentErrorRepository paymentErrorRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(id = "payment-send-id",
            topics = "payment-send", groupId = "payment-send-group", concurrency = "${listen.concurrency:1}",
            autoStartup = "${listen.auto.start:true}"
            // errorHandler = "validationErrorHandler"
    )
//    @Transactional("paymentTransactionManager")
    public void listen(String message, Acknowledgment ack, @Headers Map<String, Object> headers) {
        try {
            var messageKey = (String) headers.get(KafkaHeaders.KEY);
            var payment = this.mapper.readValue(message, Payment.class);

            log.info("Consumed message key: {} message content: {} ", messageKey, message);

            var rowsAffected = this.paymentRepository.updateStatus(payment, 2);
            if (rowsAffected == 1) {
                log.info("Transaction ID: {}, Status: {}", payment.transactionId(), payment.status());
                ack.acknowledge();

            } else {
                log.error("Transaction ID: {}, Status: {}", payment.transactionId(), payment.status());
                this.paymentErrorRepository.save(payment);
                this.paymentRepository.updateStatus(payment, 3); // erro
            }

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);

        } finally {
            ack.acknowledge();
        }
    }

//    @Bean
//    public KafkaListenerErrorHandler validationErrorHandler() {
//        return (m, e) -> {
//            System.out.println("handle error: " + e.getMessage());
//            // todo: send to DLQ topic
//            return null;
//        };
//    }
}
