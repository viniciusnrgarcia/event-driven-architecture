package br.com.vnrg.paymentfraudprocess.api;

import br.com.vnrg.paymentproducer.domain.Payment;
import br.com.vnrg.paymentproducer.messaging.PaymentProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PaymentController {

    private final PaymentProducer paymentProducer;

    @PostMapping("/payment")
    public ResponseEntity<Void> createPayment(@RequestBody Payment payment) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        var json = mapper.writeValueAsString(payment);
        this.paymentProducer.sendMessage(json);
        // this.paymentProducer.sendMessageInTransaction(json);
        return ResponseEntity.ok().build();
    }


}

