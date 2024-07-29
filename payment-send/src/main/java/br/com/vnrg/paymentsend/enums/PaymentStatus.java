package br.com.vnrg.paymentsend.enums;

import lombok.Getter;

import java.io.Serializable;
import java.util.Optional;

@Getter
public enum PaymentStatus implements Serializable {

    NEW(0),
    FRAUD_PROCESSING(1),
    FRAUD_PROCESS_COMPLETED(2),
    PROCESSING(3),
    SENT(4),
    ERROR(5),
    PAYMENT_COMPLETED(6);

    private final int code;

    PaymentStatus(int value) {
        this.code = value;
    }

    public static PaymentStatus fromValue(int value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getCode() == value) {
                return status;
            }
        }
        return null;
    }

    public static PaymentStatus ofNullableFromValue(int value) {
        return Optional.ofNullable(fromValue(value)).orElse(NEW);
    }
}
