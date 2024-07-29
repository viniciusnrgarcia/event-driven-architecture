package br.com.vnrg.paymenteventdomain.domain;

import br.com.vnrg.paymenteventdomain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent implements Serializable {

    private Long id;
    private BigDecimal amount;
    private long customerId;
    private long transactionId;
    private Integer status;
    private PaymentStatus statusDescription;

}