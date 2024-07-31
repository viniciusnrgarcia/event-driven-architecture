package br.com.vnrg.payment.api;

import br.com.vnrg.payment.domain.Payment;
import br.com.vnrg.payment.enums.PaymentStatus;
import br.com.vnrg.payment.messaging.PaymentCreatedProducer;
import br.com.vnrg.payment.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Slf4j
public class PaymentController {

    private final PaymentCreatedProducer paymentCreatedProducer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PaymentRepository paymentRepository;

    @PostMapping(path = "/payment")
    public ResponseEntity<Void> createPayment(@RequestBody PaymentRequest paymentRequest) throws JsonProcessingException {
        try {
            var paymentCreated = this.savePayment(paymentRequest);
            this.sendMessage(paymentCreated);

        } catch (Exception e) {
            log.error("Transaction ID: {}, Error: {}", paymentRequest.transactionId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();

    }

    /**
     * API para injestão de eventos para teste de integridade
     *
     * @param paymentRequest PaymentRequest
     * @return ResponseEntity Void - 200
     */
    @PostMapping(path = "/payment-id")
    public ResponseEntity<Void> createPaymentId(@RequestBody PaymentRequest paymentRequest) {
        var id = this.paymentRepository.getPaymentId();
        var paymentCreated = new Payment(
                id,
                paymentRequest.amount(),
                paymentRequest.customerId(),
                paymentRequest.transactionId(),
                PaymentStatus.ofNullableFromValue(paymentRequest.status()).getCode(),
                PaymentStatus.ofNullableFromValue(paymentRequest.status()));
//        try {
//            this.savePayment(paymentRequest);
//        } catch (Exception e) {
//            log.error("Transaction created with ID: {}, Error: {}", paymentCreated.getTransactionId(), e.getMessage());
//            // ignore exception to duplicate events in topic
//        }

        try {
            this.sendMessage(paymentCreated);

        } catch (Exception e) {
            log.error("Transaction error ID: {}, Error: {}", paymentCreated.getTransactionId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();

    }

    private void sendMessage(Payment paymentCreated) throws JsonProcessingException {
        var json = mapper.writeValueAsString(paymentCreated);
        this.paymentCreatedProducer.sendMessage(paymentCreated.getId(), json);
        log.info("Send Message  ID: {}, Message: {}", paymentCreated.getTransactionId(), json);
    }


    private Payment savePayment(PaymentRequest paymentRequest) {
        var payment = new Payment(paymentRequest.id(),
                paymentRequest.amount(),
                paymentRequest.customerId(),
                paymentRequest.transactionId(),
                PaymentStatus.ofNullableFromValue(paymentRequest.status()).getCode(),
                PaymentStatus.ofNullableFromValue(paymentRequest.status()));

        var id = this.paymentRepository.save(payment);
        payment.setId(id);
        return payment;
    }

}