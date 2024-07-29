package br.com.vnrg.paymentfraudprocess.messaging;

import br.com.vnrg.paymentfraudprocess.domain.EventStore;
import br.com.vnrg.paymentfraudprocess.domain.Payment;
import br.com.vnrg.paymentfraudprocess.enums.PaymentStatus;
import br.com.vnrg.paymentfraudprocess.repository.EventStoreRepository;
import br.com.vnrg.paymentfraudprocess.repository.PaymentErrorRepository;
import br.com.vnrg.paymentfraudprocess.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class FraudProcessConsumer {

    private final PaymentValidatedProducer paymentValidatedProducer;
    private final PaymentRepository paymentRepository;
    private final PaymentErrorRepository paymentErrorRepository;
    private final EventStoreRepository eventStoreRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(id = "payment-fraud-process-id",
            topics = "payment-created", groupId = "payment-fraud-process-group", concurrency = "${listen.concurrency:1}",
            autoStartup = "${listen.auto.start:true}"
    )
    public void listen(String message, Acknowledgment ack, @Headers Map<String, Object> headers) throws JsonProcessingException {
        Payment payment = null;
        try {
            payment = this.mapper.readValue(message, Payment.class);
            var messageKey = (String) headers.get(KafkaHeaders.KEY);
            this.eventStoreRepository.save(new EventStore(payment.getId(), "payment-fraud-process", mapper.writeValueAsString(payment)));

            log.info("Consumed message key: {} message content: {} ", messageKey, message);

            // Atualiza para em processamento
            // exactly once commit
            this.paymentRepository.updateStatus(payment, PaymentStatus.FRAUD_PROCESSING);
            // processamento do evento e controle de status
            this.fraudAnalysis(payment);


        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            this.paymentErrorRepository.save(payment);
            if (payment != null) {
                this.eventStoreRepository.save(new EventStore(payment.getId(), "payment-fraud-process", mapper.writeValueAsString(payment)));
            }

        } finally {
            ack.acknowledge();
        }
    }

    private void fraudAnalysis(Payment payment) {
        try {
            // TODO fraud analysis
            // business rules
            // Atualiza para pagamento enviado
            var paymentFraudProcessing = new Payment(payment.getId(), payment.getAmount(), payment.getCustomerId(), payment.getTransactionId(), PaymentStatus.FRAUD_PROCESSING.getCode(), PaymentStatus.FRAUD_PROCESSING);
            this.eventStoreRepository.save(new EventStore(payment.getId(), "payment-fraud-process", mapper.writeValueAsString(paymentFraudProcessing)));

            this.paymentRepository.updateStatus(payment, PaymentStatus.FRAUD_PROCESS_COMPLETED);

            var paymentSend = new Payment(payment.getId(),
                    payment.getAmount(),
                    payment.getCustomerId(),
                    payment.getTransactionId(),
                    PaymentStatus.FRAUD_PROCESS_COMPLETED.getCode(),
                    PaymentStatus.FRAUD_PROCESS_COMPLETED);

            this.eventStoreRepository.save(new EventStore(payment.getId(), "payment-fraud-process", mapper.writeValueAsString(paymentSend)));

            // se não identificado fraude, envia pagamento
            var paymentFraudProcessCompleted = new Payment(payment.getId(), payment.getAmount(), payment.getCustomerId(),
                    payment.getTransactionId(), PaymentStatus.FRAUD_PROCESS_COMPLETED.getCode(),
                    PaymentStatus.FRAUD_PROCESS_COMPLETED);
            this.paymentValidatedProducer.sendMessage(payment.getId(), mapper.writeValueAsString(paymentFraudProcessCompleted));

            log.info("Fraud analysis ID: {}, Status: {}", payment.getTransactionId(), payment.getStatus());

        } catch (Exception e) {
            log.error("Error send Payment: {}", e.getMessage());
            this.paymentRepository.updateStatus(payment, PaymentStatus.ERROR);
            throw new RuntimeException(e);
        }
    }

}
