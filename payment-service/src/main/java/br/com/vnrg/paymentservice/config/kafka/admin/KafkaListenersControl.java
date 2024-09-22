package br.com.vnrg.paymentservice.config.kafka.admin;

import lombok.Data;

@Data
public class KafkaListenersControl {
    private String listenerId;
    private Integer status;

    public KafkaListenersControl(String listenerId, Integer status) {
        this.listenerId = listenerId;
        this.status = status;
    }

    public KafkaListenersControl() {
        // default
    }
}
