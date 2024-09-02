package br.com.vnrg.paymentbatchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = "br.com.vnrg.paymentbatchservice")
public class PaymentBatchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentBatchServiceApplication.class, args);
    }

}
