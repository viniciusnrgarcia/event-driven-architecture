package br.com.vnrg.paymentservice.domain;

public record EventRetry(String id,
                         String topicName,
                         String createdBy,
                         String status,
                         String json
) {

}