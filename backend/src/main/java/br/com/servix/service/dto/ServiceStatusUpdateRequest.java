package br.com.servix.service.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.constraints.NotNull;

public record ServiceStatusUpdateRequest(
        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        Boolean active) {
}
