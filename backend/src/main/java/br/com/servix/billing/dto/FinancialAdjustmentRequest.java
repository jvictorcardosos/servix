package br.com.servix.billing.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FinancialAdjustmentRequest(
        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        @DecimalMin(value = "0.00", message = ValidationMessages.MUST_BE_GREATER_OR_EQUAL_ZERO)
        BigDecimal amount,
        String description) {
}
