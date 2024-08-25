package br.com.vnrg.paymentservice.consumer;

import br.com.vnrg.paymentservice.domain.EventStore;
import br.com.vnrg.paymentservice.domain.Payment;
import br.com.vnrg.paymentservice.enums.PaymentStatus;
import br.com.vnrg.paymentservice.repository.EventStoreRepository;
import br.com.vnrg.paymentservice.repository.PaymentErrorRepository;
import br.com.vnrg.paymentservice.repository.PaymentRepository;
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
public class PaymentConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentErrorRepository paymentErrorRepository;
    private final EventStoreRepository eventStoreRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(id = "${environment.kafka.consumer.payment-validated.id}",
            topics = "${environment.kafka.consumer.payment-validated.topics}",
            groupId = "${environment.kafka.consumer.payment-validated.group-id}",
            concurrency = "${environment.kafka.consumer.payment-validated.concurrency}",
            autoStartup = "${environment.kafka.consumer.payment-validated.auto-startup}"
            // errorHandler = "validationErrorHandler"
    )
//    @Transactional("paymentTransactionManager")
    public void listen(
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey,
            String message, Acknowledgment ack) throws JsonProcessingException {
        Payment payment = null;
        try {
            payment = this.mapper.readValue(message, Payment.class);
            this.eventStoreRepository.save(new EventStore(payment.getUuid(), "payment-service", mapper.writeValueAsString(payment)));

            log.info("Consumed message key: {} message content: {} ", messageKey, message);

            // Atualiza para em processamento
            // exactly once commit
            this.paymentRepository.updateStatus(payment, PaymentStatus.PROCESSING);
            // processamento do evento e controle de status
            this.sendPayment(payment);


        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());

            if (payment != null) {
                // se evento já processado, ou com status indisponível para pagamento ignora o mesmo
                log.error("Transaction ID: {}, Status: {}", payment.getTransactionId(), payment.getStatus());
                this.paymentErrorRepository.save(payment);
                this.eventStoreRepository.save(new EventStore(payment.getUuid(), "payment-service",
                        mapper.writeValueAsString(payment)));
            }

        } finally {
            ack.acknowledge();
        }
    }

    private void sendPayment(Payment payment) {
        try {
            // TODO send payment
            Thread.sleep(500); // API external
            // business rules API External

            // teste error
            if (payment.getId() >= 999999) {
                log.error("payment integration error: {}", payment.getId());
                throw new RuntimeException("payment error");
            }

            // Atualiza para pagamento enviado
            this.paymentRepository.updateStatus(payment, PaymentStatus.SENT);

            var paymentSend = new Payment(payment.getId(), payment.getAmount(), payment.getCustomerId(),
                    payment.getTransactionId(), PaymentStatus.SENT.getCode(), PaymentStatus.SENT, payment.getUuid());
            this.eventStoreRepository.save(new EventStore(payment.getUuid(), "payment-service", mapper.writeValueAsString(paymentSend)));
            log.info("Transaction ID: {}, Status: {}", payment.getTransactionId(), payment.getStatus());

        } catch (Exception e) {
            log.error("Error send Payment: {}", e.getMessage());
            this.paymentRepository.updateStatus(payment, PaymentStatus.ERROR);
            throw new RuntimeException(e);
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