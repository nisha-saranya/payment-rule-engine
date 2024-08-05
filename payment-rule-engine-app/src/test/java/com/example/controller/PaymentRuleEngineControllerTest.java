package com.example.controller;


import com.example.handlers.PaymentRuleEngineHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentRuleEngineControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentRuleEngineHandler paymentRuleEngineHandler;

    @InjectMocks
    private PaymentRuleEngineController paymentRuleEngineController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentRuleEngineController).build();
        ReflectionTestUtils.setField(paymentRuleEngineController, "paymentRuleEngineHandler", paymentRuleEngineHandler);
    }

    @Test
    void testEvaluateApi() throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("Country", "Norway");

        Map<String, Object> response = new HashMap<>();
        response.put("PaymentMethod", "Vipps");

        when(paymentRuleEngineHandler.handleRequest(any(Map.class), any())).thenReturn(response);

        mockMvc.perform(post("/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"Country\":\"Norway\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"PaymentMethod\":\"Vipps\"}"));
    }

    @Test
    void testEvaluateApiError() throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("Country", "Norway");

        Map<String, Object> response = new HashMap<>();
        response.put("Error", "Exception in the application.");

        when(paymentRuleEngineHandler.handleRequest(any(Map.class), any())).thenReturn(response);

        mockMvc.perform(post("/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"Country\":\"Norway\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"Error\":\"Exception in the application.\"}"));
    }
}