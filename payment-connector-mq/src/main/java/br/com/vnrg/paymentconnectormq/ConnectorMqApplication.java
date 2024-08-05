package br.com.vnrg.paymentconnectormq;

import lombok.RequiredArgsConstructor;
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

    @Override
    public void run(String... args) throws Exception {
//		this.jmsTemplate.send("DEV.QUEUE.1",
//				session -> session.createTextMessage("Hello World!")
//		);
        this.jmsTemplate.convertAndSend(
                "DEV.QUEUE.1",
                "Message 1!"
        );
    }
}
