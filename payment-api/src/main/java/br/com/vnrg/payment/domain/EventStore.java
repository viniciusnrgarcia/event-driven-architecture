package br.com.vnrg.payment.domain;

public record EventStore(String id,
                         String createdBy,
                         String json) {

    public EventStore(String createdBy, String json) {
        this(null, createdBy, json);
    }

}