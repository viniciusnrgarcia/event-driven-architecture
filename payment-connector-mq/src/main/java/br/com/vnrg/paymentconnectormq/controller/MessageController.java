package br.com.vnrg.paymentconnectormq.controller;


import jakarta.websocket.server.PathParam;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "message")
public class MessageController {

    private final JmsTemplate jmsTemplate;

    public MessageController(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @GetMapping
    public ResponseEntity<Object> put(@PathParam("message") String message) {
        this.jmsTemplate.convertAndSend("DEV.QUEUE.1", message);
        return ResponseEntity.ok().build();
    }
}
