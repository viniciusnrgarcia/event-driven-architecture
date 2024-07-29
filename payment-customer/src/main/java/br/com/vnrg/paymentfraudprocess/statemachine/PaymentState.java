package br.com.vnrg.paymentfraudprocess.statemachine;

import lombok.Getter;

@Getter
public enum PaymentState {

    CREATED(0),
    CUSTOMER_PROCESSING(1),
    FRAUD_PROCESSING(1),
    FRAUD_PROCESS_COMPLETED(2),
    PROCESSING(3),
    SENT(4),
    ERROR(5),
    PAYMENT_COMPLETED(6),
    PAYMENT_REJECTED(7);

    private final int value;

    PaymentState(int value) {
        this.value = value;
    }

}
