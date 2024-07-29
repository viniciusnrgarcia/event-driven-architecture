package br.com.vnrg.paymentfraudprocess.statemachine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class PaymentStateMachineConfig extends EnumStateMachineConfigurerAdapter<PaymentState, PaymentEvents> {


    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvents> states) throws Exception {
        states
                .withStates()
                .initial(PaymentState.CREATED)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.PAYMENT_COMPLETED)
                .end(PaymentState.PAYMENT_REJECTED)

        ;
    }


}
