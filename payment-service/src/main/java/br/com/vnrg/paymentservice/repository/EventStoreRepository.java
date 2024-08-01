package br.com.vnrg.paymentservice.repository;

import br.com.vnrg.paymentservice.domain.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventStoreRepository {

    private final JdbcClient jdbcClient;

    @Transactional
    public void save(EventStore data) {
        try {
            String sql = "INSERT INTO log (id, json) VALUES (?, ?::jsonb)";

            MapSqlParameterSource param = new MapSqlParameterSource();
            param.addValue("id", data.id());
            param.addValue("createdBy", data.createdBy());
            param.addValue("json", data.json());

            jdbcClient.sql("INSERT INTO event_store (id, created_by, json) VALUES (:id, :createdBy, :json::jsonb)")
                    .paramSource(param)
                    .update();

//            this.jdbcClient.sql(sql)
//                    .param(1, data.id())
//                    .param(2, data.json())
//                    .update();

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }


    private final JdbcTemplate jdbcTemplate;

    public void saveTest(String jsonData) {
        String sql = "INSERT INTO json_data (data) VALUES (?::jsonb)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setObject(1, jsonData);
            return ps;
        });
    }
}
