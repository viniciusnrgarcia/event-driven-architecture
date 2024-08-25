package br.com.vnrg.paymentconnectormq.repository;

import br.com.vnrg.paymentconnectormq.domain.Transactions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
public class TransactionRepository {

    private final JdbcClient jdbcClient;

    public TransactionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Transactional
    public void save(Transactions t) {
        try {
            this.jdbcClient.sql("""
                            insert into transaction (amount, customer_id, transaction_id, payment_arrangement_code, expected_settlement_date, status, status_description, created_by) 
                            values (:amount, :customerId, :transactionId, :paymentArrangementCode, :expectedSettlementDate, :status, :statusDescription, :createdBy)
                            """
                    )
                    .param("amount", t.amount())
                    .param("customerId", t.customerId())
                    .param("transactionId", t.transactionId())
                    .param("paymentArrangementCode", t.paymentArrangementCode())
                    .param("expectedSettlementDate", t.expectedSettlementDate())
                    .param("status", t.status())
                    .param("statusDescription", t.statusDescription())
                    .param("createdBy", t.createdBy())
                    .update();

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @Transactional
    public void saveError(Transactions t) {
        try {
            this.jdbcClient.sql("""
                            insert into transaction_error (amount, customer_id, transaction_id, payment_arrangement_code, expected_settlement_date, status, status_description, created_by) 
                            values (:amount, :customerId, :transactionId, :paymentArrangementCode, :expectedSettlementDate, :status, :statusDescription, :createdBy)
                            """
                    )
                    .param("amount", t.amount())
                    .param("customerId", t.customerId())
                    .param("transactionId", t.transactionId())
                    .param("paymentArrangementCode", t.paymentArrangementCode())
                    .param("expectedSettlementDate", t.expectedSettlementDate())
                    .param("status", t.status())
                    .param("statusDescription", t.statusDescription())
                    .param("createdBy", t.createdBy())
                    .update();

        } catch (Exception e) {
            log.error("Error transactions_error: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

}
