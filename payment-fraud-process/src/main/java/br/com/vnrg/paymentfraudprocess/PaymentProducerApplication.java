package br.com.vnrg.paymentfraudprocess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = "br.com.vnrg.paymentproducer")
public class PaymentFraudProcessApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentFraudProcessApplication.class, args);
	}

}
