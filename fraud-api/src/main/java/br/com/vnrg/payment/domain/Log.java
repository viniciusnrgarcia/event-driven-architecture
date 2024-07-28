package br.com.vnrg.fraud.domain;

public record Log(Long id,
                  String createdBy,
                  String json) {

    public Log(String createdBy, String json) {
        this(null, createdBy, json);
    }

}