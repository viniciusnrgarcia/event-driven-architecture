package br.com.vnrg.paymentfraudprocess.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    @KafkaListener(id="payment-consumer-id",
            topics = "payment-created", groupId = "payment-consumer", concurrency = "${listen.concurrency:1}",
            autoStartup = "${listen.auto.start:true}"
            // errorHandler = "validationErrorHandler"
    )
//    @Transactional("paymentTransactionManager")
    public void listen(String message, Acknowledgment ack) {
        System.out.println("Consumed message: " + message);
        ack.acknowledge();
    }

//    @Bean
//    public KafkaListenerErrorHandler validationErrorHandler() {
//        return (m, e) -> {
//            System.out.println("handle error: " + e.getMessage());
//            // todo: send to DLQ topic
//            return null;
//        };
//    }
}
