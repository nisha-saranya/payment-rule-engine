package com.example.validators;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestValidatorTest {

    private final RequestValidator validator = new RequestValidator();

    @Test
    void testValidInput() {
        Map<String, Object> input = new HashMap<>();
        input.put("Country", "United States");
        input.put("PaymentMethod", "Visa");
        input.put("Previous3DS", true);
        input.put("DaysSinceLast3DS", 5);
        input.put("Currency", "USD");
        input.put("CustomerType", "Manager");
        input.put("TransactionAmount", 100.0);

        StringBuilder sb = new StringBuilder();
        boolean isValid = validator.validate(input, sb);

        assertTrue(isValid);
    }

    @Test
    void testInvalidCountry() {
        Map<String, Object> input = new HashMap<>();
        input.put("Country", "InvalidCountry");

        StringBuilder sb = new StringBuilder();
        boolean isValid = validator.validate(input, sb);

        assertFalse(isValid);
        assertEquals("[Validation Errors: 1:- Invalid country name. ]", sb.toString());
    }

    @Test
    void testInvalidPaymentMethod() {
        Map<String, Object> input = new HashMap<>();
        input.put("PaymentMethod", "Unknown");

        StringBuilder sb = new StringBuilder();
        boolean isValid = validator.validate(input, sb);

        assertFalse(isValid);
        assertEquals("[Validation Errors: 1:- Invalid payment method type. ]", sb.toString());
    }

    @Test
    void testInvalidPrevious3DS() {
        Map<String, Object> input = new HashMap<>();
        input.put("Previous3DS", "NotABoolean");

        StringBuilder sb = new StringBuilder();
        boolean isValid = validator.validate(input, sb);

        assertFalse(isValid);
        assertEquals("[Validation Errors: 1:- Previous3DS should be boolean. ]", sb.toString());
    }

    @Test
    void testInvalidDaysSinceLast3DS() {
        Map<String, Object> input = new HashMap<>();
        input.put("DaysSinceLast3DS", "NotAnInteger");

        StringBuilder sb = new StringBuilder();
        boolean isValid = validator.validate(input, sb);

        assertFalse(isValid);
        assertEquals("[Validation Errors: 1:- DaysSinceLast3DS should be a valid integer. ]", sb.toString());
    }

    @Test
    void testInvalidCurrency() {
        Map<String, Object> input = new HashMap<>();
        input.put("Currency", "Invalid");

        StringBuilder sb = new StringBuilder();
        boolean isValid = validator.validate(input, sb);

        assertFalse(isValid);
        assertEquals("[Validation Errors: 1:- Invalid currency code, should be a valid 3 character code. ]", sb.toString());
    }

    @Test
    void testInvalidCustomerType() {
        Map<String, Object> input = new HashMap<>();
        input.put("CustomerType", "UnknownType");

        StringBuilder sb = new StringBuilder();
        boolean isValid = validator.validate(input, sb);

        assertFalse(isValid);
        assertEquals("[Validation Errors: 1:- Invalid customer type. ]", sb.toString());
    }

    @Test
    void testInvalidTransactionAmount() {
        Map<String, Object> input = new HashMap<>();
        input.put("TransactionAmount", -10.0);

        StringBuilder sb = new StringBuilder();
        boolean isValid = validator.validate(input, sb);

        assertFalse(isValid);
        assertEquals("[Validation Errors: 1:- TransactionAmount should be a valid double value > 0. ]", sb.toString());
    }

    @Test
    void testEmptyMap() {
        Map<String, Object> input = new HashMap<>();

        StringBuilder sb = new StringBuilder();
        boolean isValid = validator.validate(input, sb);

        assertTrue(isValid);
    }

}