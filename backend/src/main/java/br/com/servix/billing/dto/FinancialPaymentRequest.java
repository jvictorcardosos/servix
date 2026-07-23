package br.com.servix.billing.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialPaymentRequest(
        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        @DecimalMin(value = "0.01", message = ValidationMessages.MUST_BE_GREATER_THAN_ZERO)
        BigDecimal amount,
        UUID paymentMethodId,
        LocalDate paymentDate,
        String externalReference,
        String description) {
}
