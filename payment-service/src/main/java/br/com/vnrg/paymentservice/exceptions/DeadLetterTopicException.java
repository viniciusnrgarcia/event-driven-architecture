package br.com.vnrg.paymentservice.exceptions;

import org.apache.kafka.common.KafkaException;

public class DeadLetterTopicException extends KafkaException {

    public DeadLetterTopicException(String message) {
        super(message);
    }

    public DeadLetterTopicException(String message, Throwable cause) {
        super(message, cause);
    }
}
