package br.com.vnrg.eventmonitor.batch;

import br.com.vnrg.eventmonitor.domain.EventRetry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventMonitorJob {

    private final JdbcClient jdbcClient;
    private final KafkaTemplate<String, String> kafkaTemplate;


    @Scheduled(cron = "* * * * * *")
    public void process() {
        try {
            List<EventRetry> result = this.jdbcClient.sql("SELECT * FROM kafka_event_retry where status = :status LIMIT 1000")
                    .param("status", "A")
                    .query((rs, rowNum) -> new EventRetry(
                            rs.getString("id"),
                            rs.getString("topic_name"),
                            rs.getString("created_by"),
                            rs.getString("status"),
                            rs.getString("json")
                    )).list();

            result.forEach(this::retry);

        } catch (Exception e) {
            log.error("Error save retry exception: {} ", e.getMessage());
        }

    }

    @Transactional
    private void retry(EventRetry i) {
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("id", i.id());
        param.addValue("status", "I");

        jdbcClient.sql("update kafka_event_retry set status = :status where id = :id")
                .paramSource(param)
                .update();

        this.kafkaTemplate.executeInTransaction(t -> t.send(i.topicName(), i.id(), i.json()));
    }

}
