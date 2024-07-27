package br.com.vnrg.paymentfraudprocess.repository;

import br.com.vnrg.paymentfraudprocess.domain.Payment;
import br.com.vnrg.paymentfraudprocess.enums.PaymentStatus;
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
     * 1-em análise de fraude
     * 2-pagamento em processamento
     * 3-pagamento enviado
     *
     * @param payment
     * @return
     */
    @Transactional
    public int updateStatus(Payment payment, PaymentStatus status) {
        try {
            return this.jdbcClient.sql("UPDATE payment SET status = :status WHERE id = :id and status not in(4, 6)") // enviado pagamento, pago
                    .param("status", status.getValue())
                    .param("id", payment.id())
                    .update();

        } catch (Exception e) {
            log.error("Error updating status: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
