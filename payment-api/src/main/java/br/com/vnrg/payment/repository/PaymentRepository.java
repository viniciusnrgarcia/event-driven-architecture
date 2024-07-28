package br.com.vnrg.payment.repository;

import br.com.vnrg.payment.domain.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Repository
public class PaymentRepository {

    private final JdbcClient jdbcClient;

    public PaymentRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Transactional
    public Long save(Payment payment) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            var result = this.jdbcClient.sql("""
                              INSERT INTO payment (id, amount, customer_id, transaction_id, status, status_description)
                              VALUES (
                              (SELECT nextval('payment_id_seq')),
                              :amount, :customerId, :transactionId, :status, :statusDescription)
                            """
                    )
                    //.param("id", null)
                    .param("amount", payment.amount())
                    .param("customerId", payment.customerId())
                    .param("transactionId", payment.transactionId())
                    .param("status", payment.status())
                    .param("statusDescription", payment.statusDescription())
                    .update(keyHolder, "id");

            return Objects.requireNonNull(keyHolder.getKey()).longValue();

        } catch (Exception e) {
            log.error("Error save payment: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }


    @Transactional
    public void save(Payment payment, long id) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            var result = this.jdbcClient.sql("""
                              INSERT INTO payment (id, amount, customer_id, transaction_id, status, status_description)
                              VALUES (
                              :id, :amount, :customerId, :transactionId, :status, :statusDescription)
                            """
                    )
                    .param("id", id)
                    .param("amount", payment.amount())
                    .param("customerId", payment.customerId())
                    .param("transactionId", payment.transactionId())
                    .param("status", payment.status())
                    .param("statusDescription", payment.statusDescription())
                    .update();

        } catch (Exception e) {
            log.error("Error save payment with id: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

}
