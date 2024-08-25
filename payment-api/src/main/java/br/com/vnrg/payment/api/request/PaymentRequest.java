package br.com.vnrg.payment.api.request;

import java.math.BigDecimal;

public record PaymentRequest(Long id,
                             BigDecimal amount,
                             long customerId,
                             long transactionId,
                             Integer status,
                             String statusDescription,
                             String uuid) {

    public PaymentRequest(Long id, BigDecimal amount, long customerId, long transactionId, Integer status, String statusDescription, String uuid) {
        this.id = id;
        this.amount = amount;
        this.customerId = customerId;
        this.transactionId = transactionId;
        this.status = status;
        this.statusDescription = statusDescription;
        this.uuid = uuid;
    }

}