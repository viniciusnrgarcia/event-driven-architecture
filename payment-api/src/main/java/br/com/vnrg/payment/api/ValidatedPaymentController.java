package br.com.vnrg.payment.api;

import br.com.vnrg.payment.api.request.PaymentRequest;
import br.com.vnrg.payment.producer.PaymentCreatedProducer;
import br.com.vnrg.payment.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@Slf4j
public class ValidatedPaymentController {

    private final PaymentCreatedProducer paymentCreatedProducer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PaymentRepository paymentRepository;

    @PostMapping(path = "/validate")
    public ResponseEntity<Void> createPayment(@RequestBody PaymentRequest paymentRequest) throws JsonProcessingException {
        try {
            if (paymentRequest.amount().compareTo(BigDecimal.ZERO) > 0) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }

        } catch (Exception e) {
            log.error("Transaction ID: {}, Error: {}", paymentRequest.transactionId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}