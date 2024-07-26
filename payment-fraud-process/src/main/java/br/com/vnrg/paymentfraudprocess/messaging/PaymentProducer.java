package br.com.vnrg.paymentfraudprocess.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class PaymentProducer {

    private final KafkaTemplate<Integer, String> kafkaTemplate;

    public void sendMessage(String message) {
        try {
            CompletableFuture<SendResult<Integer, String>> future = kafkaTemplate.send("payment-created", message);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    //handleSuccess(data);
                    System.out.println("sucesso");
                } else {
                    // handleFailure(data, record, ex);
                    System.out.println("erro " + ex.getMessage());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessageInTransaction(String message) {
        try {
            CompletableFuture<SendResult<Integer, String>> future = kafkaTemplate.executeInTransaction(t -> t.send("payment-created", message));
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    //handleSuccess(data);
                    System.out.println("sucesso");
                } else {
                    // handleFailure(data, record, ex);
                    System.out.println("erro " + ex.getMessage());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
