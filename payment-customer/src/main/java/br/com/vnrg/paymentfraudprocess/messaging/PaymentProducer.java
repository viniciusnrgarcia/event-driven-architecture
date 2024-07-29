package br.com.vnrg.paymentfraudprocess.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
@Slf4j
public class PaymentProducer {

    private final KafkaTemplate<Long, String> kafkaTemplate;

    public void sendMessage(Long key, String message) {
        try {
            CompletableFuture<SendResult<Long, String>> future = kafkaTemplate.send("payment-send", key, message);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("sent message='{}' with offset={}", message, result.getRecordMetadata().offset());
                } else {
                    log.error("failed to send message='{}'", message, ex);
                }
            });
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

//    public void sendMessageInTransaction(String message) {
//        try {
//            CompletableFuture<SendResult<Integer, String>> future = kafkaTemplate.executeInTransaction(t -> t.send("payment-created", message));
//            future.whenComplete((result, ex) -> {
//                if (ex == null) {
//                    log.info("sent message='{}' with offset={}", message, result.getRecordMetadata().offset());
//                } else {
//                    // handleFailure(data, record, ex);
//                    log.error("failed to send message='{}'", message, ex);
//                }
//            });
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

}
