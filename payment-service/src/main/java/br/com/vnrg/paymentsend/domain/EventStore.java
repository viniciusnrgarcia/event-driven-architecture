package br.com.vnrg.paymentservice.domain;

public record EventStore(Long id,
                         String createdBy,
                         String json) {

    public EventStore(String createdBy, String json) {
        this(null, createdBy, json);
    }

}