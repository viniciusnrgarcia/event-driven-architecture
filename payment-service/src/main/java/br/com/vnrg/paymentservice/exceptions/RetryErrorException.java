package br.com.vnrg.paymentservice.exceptions;

import org.apache.kafka.common.KafkaException;

public class RetryErrorException extends KafkaException {

    public RetryErrorException(String message) {
        super(message);
    }

    public RetryErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
