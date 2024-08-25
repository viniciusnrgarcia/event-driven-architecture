package br.com.vnrg.paymentservice.config;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/admin")
public class KafkaAdminListenersController {

    private final KafkaListenerEndpointRegistry registry;

    public KafkaAdminListenersController(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    // @Scheduled(fixedDelay = 10000)
    @PostMapping(path = "/pause")
    public ResponseEntity<String> pause(@RequestBody AdminMessage message) {
        registry.getListenerContainer("pause.resume").pause();
        return ResponseEntity.ok("OK");
    }

    @PostMapping(path = "/resume")
    public ResponseEntity<String> resume(@RequestBody AdminMessage message) {
        registry.getListenerContainer("pause.resume").resume();
        return ResponseEntity.ok("OK");
    }

}


record AdminMessage(String message) {
}
