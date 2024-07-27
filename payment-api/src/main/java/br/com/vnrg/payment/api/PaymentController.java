package br.com.vnrg.payment.api;

import br.com.vnrg.payment.domain.Log;
import br.com.vnrg.payment.domain.Payment;
import br.com.vnrg.payment.messaging.FraudProcessProducer;
import br.com.vnrg.payment.repository.LogRepository;
import br.com.vnrg.payment.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Slf4j
public class PaymentController {

    private final FraudProcessProducer fraudProcessProducer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PaymentRepository paymentRepository;
    private final LogRepository logRepository;

    @PostMapping(path = "/payment")
    public ResponseEntity<Void> createPayment(@RequestBody Payment payment) throws JsonProcessingException {
        try {
            var id = this.paymentRepository.save(payment);
            var paymentCreated = new Payment(id, payment.amount(), payment.customerId(), payment.transactionId(), payment.status());
            this.logRepository.save(new Log(id, "payment-api", mapper.writeValueAsString(paymentCreated)));
            var json = mapper.writeValueAsString(paymentCreated);
            this.fraudProcessProducer.sendMessage(id, json);
            log.info("Transaction ID: {}, Message: {}", payment.transactionId(), json);

        } catch (Exception e) {
            log.error("Transaction ID: {}, Error: {}", payment.transactionId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();

    }

    @PostMapping(path = "/manual-payment")
    public ResponseEntity<Void> createManualPayment(@RequestBody Payment payment) throws JsonProcessingException {
        try {
            var json = mapper.writeValueAsString(payment);
            this.logRepository.save(new Log(payment.id(), "payment-api", mapper.writeValueAsString(payment)));
            this.fraudProcessProducer.sendMessage(payment.id(), json);
            log.info("Transaction ID: {}, Message: {}", payment.transactionId(), json);

        } catch (Exception e) {
            log.error("Transaction ID: {}, Error: {}", payment.transactionId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();

    }


    @PostMapping(path = "/payment/{events}")
    public ResponseEntity<Void> createPayment(@RequestBody Payment payment, @PathVariable Integer events) {
        try {
            for (int i = 0; i < events; i++) {
                var id = this.paymentRepository.save(payment);
                this.logRepository.save(new Log(id, "payment-api", mapper.writeValueAsString(payment)));
                var paymentCreated = new Payment(id, payment.amount(), payment.customerId(), payment.transactionId(), payment.status());
                var json = mapper.writeValueAsString(paymentCreated);
                this.fraudProcessProducer.sendMessage(id, json);
                log.info("Transaction ID: {}, Message: {}", payment.transactionId(), json);
            }

        } catch (Exception e) {
            log.error("Transaction ID: {}, Error: {}", payment.transactionId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();

    }

    @PostMapping(path = "/payment-id")
    public ResponseEntity<Void> createPaymentId(@RequestBody Payment payment) {

        try {
            this.paymentRepository.save(payment, payment.id());
        } catch (Exception e) {
            log.error("Transaction ID: {}, Error: {}", payment.transactionId(), e.getMessage());
            // ignore exception to duplicate events in topic
        }

        try {
            var json = mapper.writeValueAsString(payment);
            this.logRepository.save(new Log(payment.id(), "payment-api", mapper.writeValueAsString(payment)));
            this.fraudProcessProducer.sendMessage(payment.id(), json);
            log.info("Transaction ID: {}, Message: {}", payment.transactionId(), json);

        } catch (Exception e) {
            log.error("Transaction ID: {}, Error: {}", payment.transactionId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();

    }
}