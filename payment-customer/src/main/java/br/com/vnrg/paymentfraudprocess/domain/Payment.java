package br.com.vnrg.paymentfraudprocess.domain;

import java.math.BigDecimal;

public record Payment(Long id,
                      BigDecimal amount,
                      long customerId,
                      long transactionId,
                      Integer status,
                      String statusDescription) {

    public Payment(Long id, BigDecimal amount, long customerId, long transactionId, Integer status, String statusDescription) {
        this.id = id;
        this.amount = amount;
        this.customerId = customerId;
        this.transactionId = transactionId;
        this.status = status;
        this.statusDescription = statusDescription;
    }

}