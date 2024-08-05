package com.example.handler;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.example.handlers.PaymentRuleEngineHandler;
import com.example.validators.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PaymentRuleEngineHandlerTest {

    @InjectMocks
    private PaymentRuleEngineHandler handler;

    @Mock
    private DynamoDB dynamoDB;

    @Mock
    private Table table;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private Context context;

    @Mock
    private ItemCollection<ScanOutcome> itemCollection;

    @Mock
    private IteratorSupport<Item, ScanOutcome> iterator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new PaymentRuleEngineHandler(dynamoDB);
        ReflectionTestUtils.setField(handler, "table", table);
    }

    @Test
    public void testHandleRequest_ValidRequest() {
        // Given
        Map<String, Object> input = new HashMap<>();
        input.put("Country", "Norway");

        when(requestValidator.validate(eq(input), any())).thenReturn(true);

        // Create mock items
        Item item1 = new Item()
                .withInt("RuleID", 1)
                .withMap("Criteria", Map.of("Country", "Norway"))
                .withMap("Action", Map.of("PaymentMethod", "Vipps"));

        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(item1);
        when(itemCollection.iterator()).thenReturn(iterator);
        doReturn(itemCollection).when(table).scan(any(ScanSpec.class));


        // When
        Map<String, Object> result = handler.handleRequest(input, context);

        // Then
        assertEquals("Vipps", result.get("PaymentMethod"));
    }

    @Test
    public void testHandleRequest_InvalidRequest() {
        // Given
        Map<String, Object> input = new HashMap<>();
        input.put("TransactionAmount", 50);

        when(requestValidator.validate(eq(input), any())).thenReturn(false);

        // When
        Map<String, Object> result = handler.handleRequest(input, context);

        // Then
        assertEquals("[Validation Errors: 1:- TransactionAmount should be a valid double value > 0. ]", result.get("Error"));
    }

    @Test
    public void testHandleRequest_ExceptionInFunction() {
        // Given
        Map<String, Object> input = new HashMap<>();
        input.put("TransactionAmount", 200.0);

        when(requestValidator.validate(eq(input), any())).thenReturn(true);
        doThrow(new RuntimeException("Scan exception")).when(table).scan(any(ScanSpec.class));

        // When
        Map<String, Object> result = handler.handleRequest(input, context);

        // Then
        assertEquals("Exception in the application.", result.get("Error"));
    }


}