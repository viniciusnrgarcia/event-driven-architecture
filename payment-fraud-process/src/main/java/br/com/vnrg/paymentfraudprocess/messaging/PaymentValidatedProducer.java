package br.com.vnrg.paymentfraudprocess.messaging;

import br.com.vnrg.paymentfraudprocess.domain.EventStore;
import br.com.vnrg.paymentfraudprocess.repository.EventStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
@Slf4j
public class PaymentValidatedProducer {

    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final EventStoreRepository eventStoreRepository;

    public void sendMessage(Long key, String message) {
        try {
            this.eventStoreRepository.save(new EventStore(key, "payment-fraud-process", message));

            CompletableFuture<SendResult<Long, String>> future = kafkaTemplate.send("payment-validated", key, message);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("sent message='{}' with offset={}", message, result.getRecordMetadata().offset());
                } else {
                    log.error("failed to send message='{}'", message, ex);
                    // TODO send to retry topic (error)
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
