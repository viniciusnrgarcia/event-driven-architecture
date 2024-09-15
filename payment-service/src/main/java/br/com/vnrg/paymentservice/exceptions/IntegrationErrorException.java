package br.com.vnrg.paymentservice.exceptions;

public class IntegrationErrorException extends Exception {

    public IntegrationErrorException(String message) {
        super(message);
    }

    public IntegrationErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
