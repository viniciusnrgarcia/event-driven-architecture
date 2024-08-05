package br.com.vnrg.connector_mq;

import org.springframework.boot.SpringApplication;

public class TestConnectorMqApplication {

	public static void main(String[] args) {
		SpringApplication.from(ConnectorMqApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
