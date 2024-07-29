package br.com.vnrg.paymentfraudprocess.enums;

public enum PaymentStatus {

    NEW(0),
    FRAUD_PROCESSING(1),
    FRAUD_PROCESS_COMPLETED(2),
    PROCESSING(3),
    SENT(4),
    ERROR(5),
    PAYMENT_COMPLETED(6);

    private final int value;

    PaymentStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
