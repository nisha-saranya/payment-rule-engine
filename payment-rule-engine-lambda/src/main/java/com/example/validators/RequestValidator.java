package com.example.validators;

import java.util.*;

public class RequestValidator {

    private static final Set<String> VALID_COUNTRIES = new HashSet<>();
    private static final Set<String> VALID_PAYMENT_METHODS = Set.of("MasterCard", "Visa", "CreditCard", "Vipps");
    private static final Set<String> VALID_CUSTOMER_TYPES = Set.of("Employee", "Debtor", "Creditor", "Manager");

    static {
        String[] countries = Locale.getISOCountries();
        for (String country : countries) {
            Locale locale = new Locale("", country);
            VALID_COUNTRIES.add(locale.getDisplayCountry(Locale.ENGLISH));
        }
    }

    public boolean validate(Map<String, Object> input, StringBuilder stringBuilder) {
        stringBuilder.append("[Validation Errors: ");
        int errorCount = 1;
        boolean requestValid = true;
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "Country":
                    if (!(value instanceof String) || !VALID_COUNTRIES.contains(value)) {
                        stringBuilder.append(errorCount++).append(":- Invalid country name. ");
                        requestValid = false;
                    }
                    break;
                case "PaymentMethod":
                    if (!(value instanceof String) || !VALID_PAYMENT_METHODS.contains(value)) {
                        stringBuilder.append(errorCount++).append(":- Invalid payment method type. ");
                        requestValid = false;
                    }
                    break;
                case "Previous3DS":
                    if (!(value instanceof Boolean)) {
                        stringBuilder.append(errorCount++).append(":- Previous3DS should be boolean. ");
                        requestValid = false;
                    }
                    break;
                case "DaysSinceLast3DS":
                    if (!(value instanceof Integer)) {
                        stringBuilder.append(errorCount++).append(":- DaysSinceLast3DS should be a valid integer. ");
                        requestValid = false;
                    }
                    break;
                case "Currency":
                    if (!(value instanceof String) || !isValidCurrencyCode((String) value)) {
                        stringBuilder.append(errorCount++).append(":- Invalid currency code, should be a valid 3 character code. ");
                        requestValid = false;
                    }
                    break;
                case "CustomerType":
                    if (!(value instanceof String) || !VALID_CUSTOMER_TYPES.contains(value)) {
                        stringBuilder.append(errorCount++).append(":- Invalid customer type. ");
                        requestValid = false;
                    }
                    break;
                case "TransactionAmount":
                    if (!(value instanceof Double) || ((Double) value) <= 0) {
                        stringBuilder.append(errorCount++).append(":- TransactionAmount should be a valid double value > 0. ");
                        requestValid = false;
                    }
                    break;
                default:
                    break;
            }
        }
        return requestValid;
    }

    private static boolean isValidCurrencyCode(String code) {
        try {
            Currency currency = Currency.getInstance(code);
            return currency.getCurrencyCode().equals(code);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}