package br.com.vnrg.payment.api;

import java.math.BigDecimal;

public record PaymentRequest(Long id,
                             BigDecimal amount,
                             long customerId,
                             long transactionId,
                             Integer status,
                             String statusDescription) {

    public PaymentRequest(Long id, BigDecimal amount, long customerId, long transactionId, Integer status, String statusDescription) {
        this.id = id;
        this.amount = amount;
        this.customerId = customerId;
        this.transactionId = transactionId;
        this.status = status;
        this.statusDescription = statusDescription;
    }

}