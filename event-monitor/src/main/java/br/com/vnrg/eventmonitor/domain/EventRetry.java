package br.com.vnrg.eventmonitor.domain;

public record EventRetry(String id,
                         String topicName,
                         String createdBy,
                         String status,
                         String json
) {

}