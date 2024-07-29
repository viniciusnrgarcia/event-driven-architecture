package br.com.vnrg.paymentfraudprocess.statemachine;

import lombok.Getter;

@Getter
public enum PaymentEvents {

    FRAUD,
    CUSTOMER,
    SEND_PAYMENT;


}
