package com.example.controller;

import com.example.handlers.PaymentRuleEngineHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/evaluate")
public class PaymentRuleEngineController {

    private final PaymentRuleEngineHandler paymentRuleEngineHandler;

    public PaymentRuleEngineController() {
        this.paymentRuleEngineHandler = new PaymentRuleEngineHandler(Boolean.TRUE);;
    }

    @PostMapping
    public Map<String, Object> evaluateApi(@RequestBody Map<String, Object> input) {
        return (paymentRuleEngineHandler.handleRequest(input, null));
    }
}
