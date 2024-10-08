package br.com.vnrg.fraud.domain;

import java.math.BigDecimal;

public record Payment(Long id,
                      BigDecimal amount,
                      long customerId,
                      long transactionId,
                      Integer status) {

    public Payment(Long id, BigDecimal amount, long customerId, long transactionId, Integer status) {
        this.id = id;
        this.amount = amount;
        this.customerId = customerId;
        this.transactionId = transactionId;
        this.status = status;
    }

}