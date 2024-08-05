package br.com.vnrg.paymentconnectormq;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.core.JmsTemplate;

@RequiredArgsConstructor
@SpringBootApplication
public class ConnectorMqApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ConnectorMqApplication.class, args);
    }

    final JmsTemplate jmsTemplate;

    @Value("${ibm.mq.queue.payment.mq}")
    private String queue;

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 10; i++) {
            this.jmsTemplate.convertAndSend(queue, "Message " + i);
        }
    }
}
