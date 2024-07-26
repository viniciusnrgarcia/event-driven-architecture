package br.com.vnrg.paymentsend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = "br.com.vnrg.paymentsend")
public class PaymentSendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentSendApplication.class, args);
    }

}
