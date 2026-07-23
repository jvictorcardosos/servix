package br.com.servix.billing.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialTransactionRequest(
        UUID serviceOrderId,
        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        @DecimalMin(value = "0.01", message = ValidationMessages.MUST_BE_GREATER_THAN_ZERO)
        BigDecimal amount,
        @DecimalMin(value = "0.00", message = ValidationMessages.MUST_BE_GREATER_OR_EQUAL_ZERO)
        BigDecimal discount,
        @DecimalMin(value = "0.00", message = ValidationMessages.MUST_BE_GREATER_OR_EQUAL_ZERO)
        BigDecimal surcharge,
        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        LocalDate dueDate,
        UUID paymentMethodId,
        @Size(max = 4000, message = ValidationMessages.MAX_LENGTH)
        String description,
        @Size(max = 120, message = ValidationMessages.MAX_LENGTH)
        String externalReference) {
}
