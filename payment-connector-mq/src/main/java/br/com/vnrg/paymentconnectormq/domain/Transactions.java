package br.com.vnrg.paymentconnectormq.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transactions(
        BigDecimal amount,
        Integer customerId,
        Long transactionId,
        String paymentArrangementCode,
        LocalDate expectedSettlementDate,
        Integer status,
        String statusDescription,
        String createdBy
) {
}
