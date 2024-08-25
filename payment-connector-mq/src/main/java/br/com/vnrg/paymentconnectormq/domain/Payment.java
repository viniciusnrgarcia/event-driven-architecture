package br.com.vnrg.paymentconnectormq.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment implements Serializable {

    private String uniqueId;
    private BigDecimal amount;
    private long customerId;
    private long transactionId;
    private String paymentArrangementCode;
    private LocalDate expectedSettlementDate;
    private Integer status;
    private String statusDescription;

}