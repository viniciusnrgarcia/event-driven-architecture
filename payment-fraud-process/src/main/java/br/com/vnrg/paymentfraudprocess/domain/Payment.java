package br.com.vnrg.paymentfraudprocess.domain;

public record Payment(String id, String status) {
    public Payment(String id, String status) {
        this.id = id;
        this.status = status;
    }
}