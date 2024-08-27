package br.com.vnrg.paymentservice.config.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@EnableScheduling
@Component
@Slf4j
public class KafkaAdminListenerControl {

    private final KafkaListenerEndpointRegistry registry;
    private final JdbcClient jdbcClient;


    public KafkaAdminListenerControl(KafkaListenerEndpointRegistry registry, JdbcClient jdbcClient) {
        this.registry = registry;
        this.jdbcClient = jdbcClient;
    }

    @Scheduled(fixedDelayString = "${environment.kafka.admin-listeners-control:30000}")
    public void pauseResume() {
        var containersStatus = this.listenerContainerStatus(this.registry.getListenerContainerIds());
        this.registry.getListenerContainers().forEach(l -> {
                    containersStatus
                            .stream()
                            .filter(c -> c.getListenerId().equals(l.getListenerId()))
                            .findFirst()
                            .ifPresent(containerStatus -> {
                                if (containerStatus.getStatus() == 0 && l.isRunning()) {
                                    l.pause();
                                    log.warn("Listener {} paused", l.getListenerId());
                                }
                                if (containerStatus.getStatus() == 1 && !l.isRunning()) {
                                    l.start();
                                    log.warn("Listener {} resumed", l.getListenerId());
                                }
                                if (containerStatus.getStatus() == 1 && l.isContainerPaused()) {
                                    l.resume();
                                    log.warn("Listener {} resumed", l.getListenerId());
                                }
                            });

                }
        );
    }

    private List<KafkaListenersControl> listenerContainerStatus(Set<String> listenerContainerIds) {
        return this.jdbcClient.sql("""
                        select listener_id, status from kafka_listeners_control where listener_id in (:listenerContainerIds)
                        """)
                .param("listenerContainerIds", listenerContainerIds)
                .query((rs, rowNum) -> new KafkaListenersControl(
                        rs.getString("listener_id"),
                        rs.getInt("status")
                )).list();
    }

}
