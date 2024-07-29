package br.com.vnrg.paymentfraudprocess.messaging;

import br.com.vnrg.paymentfraudprocess.domain.Log;
import br.com.vnrg.paymentfraudprocess.domain.Payment;
import br.com.vnrg.paymentfraudprocess.enums.PaymentStatus;
import br.com.vnrg.paymentfraudprocess.repository.LogRepository;
import br.com.vnrg.paymentfraudprocess.repository.PaymentErrorRepository;
import br.com.vnrg.paymentfraudprocess.repository.PaymentRepository;
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

    private final PaymentProducer paymentProducer;
    private final PaymentRepository paymentRepository;
    private final PaymentErrorRepository paymentErrorRepository;
    private final LogRepository logRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(id = "payment-fraud-process-id",
            topics = "payment-fraud-process", groupId = "payment-fraud-process-group", concurrency = "${listen.concurrency:1}",
            autoStartup = "${listen.auto.start:true}"
    )
    public void listen(String message, Acknowledgment ack, @Headers Map<String, Object> headers) {
        try {
            var payment = this.mapper.readValue(message, Payment.class);
            var messageKey = (String) headers.get(KafkaHeaders.KEY);
            this.logRepository.save(new Log(payment.id(), "payment-fraud-process", mapper.writeValueAsString(payment)));

            log.info("Consumed message key: {} message content: {} ", messageKey, message);

            // Atualiza para em processamento
            // exactly once commit
            var rowsAffected = this.paymentRepository.updateStatus(payment, PaymentStatus.FRAUD_PROCESSING);

            if (rowsAffected == 1) {
                var paymentFraudProcessing = new Payment(payment.id(), payment.amount(), payment.customerId(), payment.transactionId(), PaymentStatus.FRAUD_PROCESSING.getValue());
                this.logRepository.save(new Log(payment.id(), "payment-fraud-process", mapper.writeValueAsString(paymentFraudProcessing)));

                // processamento do evento e controle de status
                this.fraudAnalysis(paymentFraudProcessing);

                // se não identificado fraude, envia pagamento
                var paymentFraudProcessCompleted = new Payment(payment.id(), payment.amount(), payment.customerId(), payment.transactionId(), PaymentStatus.FRAUD_PROCESS_COMPLETED.getValue());
                this.paymentProducer.sendMessage(payment.id(), mapper.writeValueAsString(paymentFraudProcessCompleted));

            } else {
                // se evento já processado, ou com status indisponível para pagamento ignora o mesmo
                log.error("Transaction ID: {}, Status: {}", payment.transactionId(), payment.status());
                this.paymentErrorRepository.save(payment);
                this.logRepository.save(new Log(payment.id(), "payment-fraud-process", mapper.writeValueAsString(payment)));
            }

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);

        } finally {
            ack.acknowledge();
        }
    }

    private void fraudAnalysis(Payment payment) {
        try {
            // TODO fraud analysis
            // business rules
            // Atualiza para pagamento enviado
            this.paymentRepository.updateStatus(payment, PaymentStatus.FRAUD_PROCESS_COMPLETED);

            var paymentSend = new Payment(payment.id(), payment.amount(), payment.customerId(), payment.transactionId(), PaymentStatus.FRAUD_PROCESS_COMPLETED.getValue());
            this.logRepository.save(new Log(payment.id(), "payment-fraud-process", mapper.writeValueAsString(paymentSend)));
            log.info("Transaction ID: {}, Status: {}", payment.transactionId(), payment.status());

        } catch (Exception e) {
            log.error("Error send Payment: {}", e.getMessage());
            this.paymentRepository.updateStatus(payment, PaymentStatus.ERROR);
            throw new RuntimeException(e);
        }
    }

}
