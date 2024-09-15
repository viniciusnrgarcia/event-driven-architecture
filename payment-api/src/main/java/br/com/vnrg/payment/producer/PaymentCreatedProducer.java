package br.com.vnrg.payment.producer;

import br.com.vnrg.payment.domain.EventStore;
import br.com.vnrg.payment.repository.EventStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
@Slf4j
public class PaymentCreatedProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventStoreRepository eventStoreRepository;

    public void sendMessage(String key, String message) {
        try {
            this.eventStoreRepository.save(new EventStore(key, "payment-api", message));
            this.kafkaTemplate.executeInTransaction(t -> t.send("payment-validated", key, message)).get();

            /*CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("payment-validated", key, message);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("sent message='{}' with offset={}", message, result.getRecordMetadata().offset());
                } else {
                    log.error("failed to send message='{}'", message, ex);
                    // TODO send to retry topic (error)
                }
            });*/
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

//    public void send(String key, String message) {
//        try {
//            kafkaTemplate.executeInTransaction(t -> t.send("payment-validated", message)).get();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

}
