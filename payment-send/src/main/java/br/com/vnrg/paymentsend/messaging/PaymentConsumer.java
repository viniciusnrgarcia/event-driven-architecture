package br.com.vnrg.paymentsend.messaging;

import br.com.vnrg.paymentsend.domain.Log;
import br.com.vnrg.paymentsend.domain.Payment;
import br.com.vnrg.paymentsend.enums.PaymentStatus;
import br.com.vnrg.paymentsend.repository.LogRepository;
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
    private final LogRepository logRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(id = "payment-send-id",
            topics = "payment-send", groupId = "payment-send-group", concurrency = "${listen.concurrency:1}",
            autoStartup = "${listen.auto.start:true}"
            // errorHandler = "validationErrorHandler"
    )
//    @Transactional("paymentTransactionManager")
    public void listen(String message, Acknowledgment ack, @Headers Map<String, Object> headers) {
        try {
            var payment = this.mapper.readValue(message, Payment.class);
            var messageKey = (String) headers.get(KafkaHeaders.KEY);
            this.logRepository.save(new Log(payment.id(), "payment-send", mapper.writeValueAsString(payment)));

            log.info("Consumed message key: {} message content: {} ", messageKey, message);

            // Atualiza para em processamento
            // exactly once commit
            var rowsAffected = this.paymentRepository.updateStatus(payment, PaymentStatus.PROCESSING);

            if (rowsAffected == 1) {
                // processamento do evento e controle de status
                this.sendPayment(payment);

            } else {
                // se evento já processado, ou com status indisponível para pagamento ignora o mesmo
                log.error("Transaction ID: {}, Status: {}", payment.transactionId(), payment.status());
                this.paymentErrorRepository.save(payment);
                this.logRepository.save(new Log(payment.id(), "payment-send", mapper.writeValueAsString(payment)));
            }

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);

        } finally {
            ack.acknowledge();
        }
    }

    private void sendPayment(Payment payment) {
        try {
            // TODO send payment
            // business rules

            // teste error
            if (payment.id() >= 999999) {
                log.error("payment integration error: {}", payment.id());
                throw new RuntimeException("payment error");
            }

            // Atualiza para pagamento enviado
            this.paymentRepository.updateStatus(payment, PaymentStatus.SENT);

            var paymentSend = new Payment(payment.id(), payment.amount(), payment.customerId(), payment.transactionId(), PaymentStatus.SENT.getValue());
            this.logRepository.save(new Log(payment.id(), "payment-send", mapper.writeValueAsString(paymentSend)));
            log.info("Transaction ID: {}, Status: {}", payment.transactionId(), payment.status());

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
