package br.com.vnrg.paymentsend.repository;

import br.com.vnrg.paymentsend.domain.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Slf4j
@Repository
public class PaymentRepository {

    private final JdbcClient jdbcClient;

    public PaymentRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Long save(Payment payment) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            var result = this.jdbcClient.sql("""
                              INSERT INTO payment (id, amount, customer_id, transaction_id, status)
                              VALUES (
                              (SELECT nextval('payment_id_seq')),
                              :amount, :customerId, :transactionId, :status)
                            """
                    )
                    //.param("id", null)
                    .param("amount", payment.amount())
                    .param("customerId", payment.customerId())
                    .param("transactionId", payment.transactionId())
                    .param("status", payment.status())
                    .update(keyHolder, "id");

            return Objects.requireNonNull(keyHolder.getKey()).longValue();

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

    /**
     * Atualização status
     * 0-novo
     * 1-em processamento
     * 2-pagamento enviado
     * 3-erro pagamento
     *
     * @param payment
     * @return
     */
    public int updateStatus(Payment payment, int status) {
        try {
            return this.jdbcClient.sql("UPDATE payment SET status = :status WHERE id = :id and status <> :payment_status")
                    .param("status", status)
                    .param("payment_status", 2)
                    .param("id", payment.id())
                    .update();

        } catch (Exception e) {
            log.error("Error updating status: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
