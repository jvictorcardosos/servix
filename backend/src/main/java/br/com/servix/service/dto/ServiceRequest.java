package br.com.servix.service.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ServiceRequest(
        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Size(max = 150, message = ValidationMessages.MAX_LENGTH)
        String name,

        @Size(max = 4000, message = ValidationMessages.MAX_LENGTH)
        String description,

        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        @Positive(message = ValidationMessages.MUST_BE_GREATER_THAN_ZERO)
        Integer durationMinutes,

        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        @DecimalMin(value = "0.01", message = ValidationMessages.MUST_BE_GREATER_THAN_ZERO)
        BigDecimal price) {
}
