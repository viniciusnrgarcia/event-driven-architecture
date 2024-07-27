package br.com.vnrg.paymentfraudprocess.domain;

public record Log(Long id,
                  String createdBy,
                  String json) {

    public Log(String createdBy, String json) {
        this(null, createdBy, json);
    }

}