package br.com.vnrg.paymentproducer;

import org.springframework.boot.SpringApplication;

public class TestPaymentProducerApplication {

	public static void main(String[] args) {
		SpringApplication.from(PaymentProducerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
