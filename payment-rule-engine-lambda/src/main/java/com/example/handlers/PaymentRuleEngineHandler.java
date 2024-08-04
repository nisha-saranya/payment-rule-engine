package com.example.handlers;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.validators.RequestValidator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class PaymentRuleEngineHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final DynamoDB dynamoDB;
    private final Table table;
    private final RequestValidator requestValidator;

    private static final Logger LOGGER = Logger.getLogger(PaymentRuleEngineHandler.class.getName());

    public PaymentRuleEngineHandler() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        this.dynamoDB = new DynamoDB(client);
        this.table = dynamoDB.getTable("PaymentRules");
        this.requestValidator = new RequestValidator();
    }

    public PaymentRuleEngineHandler(Boolean isLocal) {
        AmazonDynamoDB client;
        if (isLocal) {
            client = AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://dynamodb-local:8000", "us-west-2"))
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummyAccessKey", "dummySecretKey")))
                    .build();
        } else {
            client = AmazonDynamoDBClientBuilder.defaultClient();
        }

        this.dynamoDB = new DynamoDB(client);
        this.table = dynamoDB.getTable("PaymentRules");
        this.requestValidator = new RequestValidator();
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        LOGGER.info("Received transaction request: " + input.toString());
        Map<String, Object> result = new HashMap<>();
        try {
            StringBuilder errorBuilder = new StringBuilder();
            if (!requestValidator.validate(input, errorBuilder)) {
                LOGGER.warning("Request contains validation errors: " + errorBuilder);
                return Map.of("Error", errorBuilder.toString());
            }
            table.scan(new ScanSpec()).forEach(item -> {
                Map<String, Object> criteria = item.getMap("Criteria");
                Map<String, Object> action = item.getMap("Action");

                if (matchesCriteria(input, criteria)) {
                    applyAction(input, action, result);
                    LOGGER.info("Rule matched: " + item.get("RuleID"));
                }
            });
        } catch (Exception ignored) {
            LOGGER.warning("Exception in the function: " + ignored.getMessage());
            return Map.of("Error", "Exception in the application.");
        }
        LOGGER.info("Sending response: " + result);
        return result;
    }

    private boolean matchesCriteria(Map<String, Object> input, Map<String, Object> criteria) {
        return criteria.entrySet().stream()
                .allMatch(entry ->
                        containsRequiredInput(entry.getKey(), input)
                                && metRequiredCondition(entry.getKey(), entry.getValue(), input));
    }

    private boolean containsRequiredInput(String criteriaType, Map<String, Object> input) {
        return switch (criteriaType) {
            case "Last3DSTransactionWithinDays" -> input.containsKey("DaysSinceLast3DS");
            case "TransactionAmountGreaterThan", "TransactionAmountLessThan" -> input.containsKey("TransactionAmount");
            default -> input.containsKey(criteriaType);
        };
    }

    private boolean metRequiredCondition(String criteriaType, Object criteriaValue, Map<String, Object> input) {
        int compareTo = 0;
        if (Arrays.asList("Last3DSTransactionWithinDays",
                        "TransactionAmountLessThan",
                        "TransactionAmountGreaterThan")
                .contains(criteriaType)) {
            var value = Double.valueOf(String.valueOf(input.get(criteriaType.equals("Last3DSTransactionWithinDays") ? "DaysSinceLast3DS" : "TransactionAmount")));
            compareTo = value.compareTo(Double.valueOf(String.valueOf(criteriaValue)));
        }

        return switch (criteriaType) {
            case "Last3DSTransactionWithinDays" -> compareTo < 0;
            case "TransactionAmountLessThan" -> compareTo < 0;
            case "TransactionAmountGreaterThan" -> compareTo > 0;
            default -> input.get(criteriaType).equals(criteriaValue);
        };
    }

    private void applyAction(Map<String, Object> input, Map<String, Object> action, Map<String, Object> result) {
        action.forEach((key, value) -> {
            switch (key) {
                case "PaymentMethod", "Waive3DS", "EnableFeature", "RequireAdditionalVerification" ->
                        result.put(key, value);
                case "Routing" -> result.put("Routing", selectRouteBasedOnConfiguration((Map<String, Object>) value));
                case "ApplyFee" -> result.put("Fee", calculateFee(input, (BigDecimal) value));
                default -> {
                }
            }
        });
    }

    private String selectRouteBasedOnConfiguration(Map<String, Object> routingConfig) {
        AtomicInteger totalWeight = new AtomicInteger();
        routingConfig.forEach((key, value) -> totalWeight.addAndGet(((BigDecimal) value).intValue()));

        int randomWeight = new Random().nextInt(totalWeight.get());
        AtomicInteger currentWeight = new AtomicInteger();
        return routingConfig.keySet().stream()
                .filter(key -> currentWeight.addAndGet(((BigDecimal) routingConfig.get(key)).intValue()) > randomWeight)
                .findFirst()
                .orElse("");
    }

    private double calculateFee(Map<String, Object> input, BigDecimal feePercentage) {
        double transactionAmount = ((Number) input.get("TransactionAmount")).doubleValue();
        return transactionAmount * feePercentage.doubleValue() / 100;
    }
}