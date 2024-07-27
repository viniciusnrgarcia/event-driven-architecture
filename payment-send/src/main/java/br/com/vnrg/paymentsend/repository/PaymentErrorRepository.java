package br.com.vnrg.paymentsend.repository;

import br.com.vnrg.paymentsend.domain.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
public class PaymentErrorRepository {

    private final JdbcClient jdbcClient;

    public PaymentErrorRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Transactional
    public void save(Payment payment) {
        try {
            this.jdbcClient.sql("""
                              INSERT INTO payment_error (id, amount, customer_id, transaction_id, status)
                              VALUES (:id, :amount, :customerId, :transactionId, :status)
                            """
                    )
                    .param("id", null)
                    .param("amount", payment.amount())
                    .param("customerId", payment.customerId())
                    .param("transactionId", payment.transactionId())
                    .param("status", payment.status())
                    .update();

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

}