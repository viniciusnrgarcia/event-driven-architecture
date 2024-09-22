package br.com.vnrg.paymentservice.http;

import br.com.vnrg.paymentservice.http.request.PaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "paymentApi",
        url = "${environment.api.validate.url}")
@Validated
public interface PaymentApi {

    @PostMapping(path = "/validate")
    ResponseEntity<String> validate(PaymentRequest request);

}
