package br.com.vnrg.payment.api;

import br.com.vnrg.payment.domain.Payment;
import br.com.vnrg.payment.messaging.PaymentProducer;
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

    private final PaymentProducer paymentProducer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PaymentRepository paymentRepository;

    @PostMapping(path = "/payment")
    public ResponseEntity<Void> createPayment(@RequestBody Payment payment) throws JsonProcessingException {
        try {
            var id = this.paymentRepository.save(payment);
            var paymentCreated = new Payment(id, payment.amount(), payment.customerId(), payment.transactionId(), payment.status());
            var json = mapper.writeValueAsString(paymentCreated);
            this.paymentProducer.sendMessage(id, json);
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
                var paymentCreated = new Payment(id, payment.amount(), payment.customerId(), payment.transactionId(), payment.status());
                var json = mapper.writeValueAsString(paymentCreated);
                this.paymentProducer.sendMessage(id, json);
                log.info("Transaction ID: {}, Message: {}", payment.transactionId(), json);
            }

        } catch (Exception e) {
            log.error("Transaction ID: {}, Error: {}", payment.transactionId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();

    }
}