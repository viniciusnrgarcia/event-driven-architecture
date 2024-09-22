package br.com.vnrg.paymentservice.consumer;

import br.com.vnrg.paymentservice.domain.EventStore;
import br.com.vnrg.paymentservice.domain.Payment;
import br.com.vnrg.paymentservice.enums.PaymentStatus;
import br.com.vnrg.paymentservice.exceptions.RetryErrorException;
import br.com.vnrg.paymentservice.http.PaymentApi;
import br.com.vnrg.paymentservice.http.request.PaymentRequest;
import br.com.vnrg.paymentservice.repository.EventStoreRepository;
import br.com.vnrg.paymentservice.repository.PaymentErrorRepository;
import br.com.vnrg.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.ConnectException;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentErrorRepository paymentErrorRepository;
    private final EventStoreRepository eventStoreRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PaymentApi paymentApi;


    @KafkaListener(id = "${environment.kafka.consumer.payment-validated.id}",
            topics = "${environment.kafka.consumer.payment-validated.topics}",
            groupId = "${environment.kafka.consumer.payment-validated.group-id}",
            concurrency = "${environment.kafka.consumer.payment-validated.concurrency}",
            autoStartup = "${environment.kafka.consumer.payment-validated.auto-startup}"
    )
//    @Transactional("paymentTransactionManager")
    public void listen(
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey,
            String message, Acknowledgment ack) throws JsonProcessingException, RetryErrorException {
        Payment payment = null;
        try {
            payment = this.mapper.readValue(message, Payment.class);
            this.eventStoreRepository.save(new EventStore(payment.getUuid(), "payment-service", mapper.writeValueAsString(payment)));

            log.info("Consumed message key: {} message content: {} ", messageKey, message);

            // Atualiza para em processamento
            // exactly once commit
            this.paymentRepository.updateStatus(payment, PaymentStatus.PROCESSING);
            payment.setStatus(PaymentStatus.PROCESSING.getCode());
            this.paymentRepository.savePaymentEvent(payment);
            this.validatePayment(payment);

        } catch (RetryErrorException retryErrorException) {
            log.error("Retry Error: {}", retryErrorException.getMessage());
            throw retryErrorException;

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());

            if (payment != null) {
                // se evento já processado, ou com status indisponível para pagamento ignora o mesmo
                log.error("Transaction ID: {}, Status: {}", payment.getTransactionId(), payment.getStatus());
                this.paymentErrorRepository.save(payment);
                this.eventStoreRepository.save(new EventStore(payment.getUuid(), "payment-service",
                        mapper.writeValueAsString(payment)));
            }

            // throw new RetryErrorException("Retry Error");
            // throw new IntegrationErrorException("Retry Error");
            // throw new RuntimeException("error");

            throw e;

        } finally {
            ack.acknowledge();
        }
    }

    private void validatePayment(Payment payment) {
        try {
            Thread.sleep(500); // API external
            // business rules API External

            ResponseEntity<String> response = this.paymentApi.validate(new PaymentRequest(
                    payment.getId(),
                    payment.getAmount(),
                    payment.getCustomerId(),
                    payment.getTransactionId(),
                    payment.getStatus(),
                    payment.getStatusDescription().name(),
                    payment.getUuid()
            ));

            if (response.getStatusCode().is2xxSuccessful()) {
                payment.setStatus(PaymentStatus.SENT.getCode());
                this.paymentRepository.updateStatus(payment, PaymentStatus.SENT);
                this.paymentRepository.savePaymentEvent(payment);

                var paymentSend = new Payment(payment.getId(), payment.getAmount(), payment.getCustomerId(),
                        payment.getTransactionId(), PaymentStatus.SENT.getCode(), PaymentStatus.SENT, payment.getUuid());
                this.eventStoreRepository.save(new EventStore(payment.getUuid(), "payment-service", mapper.writeValueAsString(paymentSend)));
                log.info("Transaction ID: {}, Status: {}", payment.getTransactionId(), payment.getStatus());

            } else {
                log.error("payment integration error: {}", payment.getId());
                payment.setStatus(PaymentStatus.ERROR.getCode());
                this.paymentRepository.updateStatus(payment, PaymentStatus.ERROR);
                this.paymentRepository.savePaymentEvent(payment);
            }

        } catch (RetryableException refused) {
            log.error("Error connection Payment: {}", refused.getMessage());
            throw new RetryErrorException(refused.getMessage());

        } catch (Exception e) {
            log.error("Error send Payment: {}", e.getMessage());
            this.paymentRepository.updateStatus(payment, PaymentStatus.ERROR);
            throw new RuntimeException(e);
        }
    }

}
