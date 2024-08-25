package br.com.vnrg.paymentconnectormq.producer;

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

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String key, String message) {
        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("payment-new", key, message);
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


    public SendResult<String, String> sendSyncMessage(String key, String message) {
        try {
            return kafkaTemplate.send("payment-new", key, message).get();
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
