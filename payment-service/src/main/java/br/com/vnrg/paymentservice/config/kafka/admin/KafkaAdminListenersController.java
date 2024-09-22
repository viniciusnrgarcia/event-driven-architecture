package br.com.vnrg.paymentservice.config.kafka.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/kafka/admin")
@Slf4j
public class KafkaAdminListenersController {

    private final KafkaListenerEndpointRegistry registry;

    public KafkaAdminListenersController(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    @PostMapping(path = "/pause/{listener-id}")
    public ResponseEntity<String> pause(@PathVariable("listener-id") String listenerId) {
        var listener = registry.getListenerContainer(listenerId);
        if (listener != null) {
            listener.pause();
            log.warn("Listener {} paused", listenerId);
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/resume/{listener-id}")
    public ResponseEntity<String> resume(@PathVariable("listener-id") String listenerId) {
        var listener = registry.getListenerContainer(listenerId);
        if (listener != null) {
            listener.resume();
            log.warn("Listener {} resumed", listenerId);
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.notFound().build();
    }

}