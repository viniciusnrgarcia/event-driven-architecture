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
                              INSERT INTO payment (id, amount, customer_id, transaction_id, status, status_description)
                              VALUES (
                              (SELECT nextval('payment_id_seq')),
                              :amount, :customerId, :transactionId, :status, :statusDescription)
                            """
                    )
                    //.param("id", null)
                    .param("amount", payment.getAmount())
                    .param("customerId", payment.getCustomerId())
                    .param("transactionId", payment.getTransactionId())
                    .param("status", payment.getStatus())
                    .param("statusDescription", payment.getStatusDescription())
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
    public void updateStatus(Payment payment, PaymentStatus status) {
        try {
            var rowsAffected = this.jdbcClient.sql("UPDATE payment SET status = :status, status_description = :statusDescription WHERE id = :id and status not in(4, 6)") // enviado pagamento, pago
                    .param("status", status.getCode())
                    .param("statusDescription", status.name())
                    .param("id", payment.getId())
                    .update();

            // se evento já processado, ou com status indisponível para pagamento ignora o mesmo
            if (rowsAffected == 0) {
                throw new RuntimeException("Error updating status");
            }

        } catch (Exception e) {
            log.error("Error updating status: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
