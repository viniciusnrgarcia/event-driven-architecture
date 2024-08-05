package br.com.vnrg.paymentconnectormq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConnectorConsumer {

    @JmsListener(destination = "${ibm.mq.queue.payment.mq}",
            concurrency = "${ibm.mq.queue.payment.concurrency}"
    )
    public void receiveMessage(String message) {
        try {
            log.info("Received message: {}", message);
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}
