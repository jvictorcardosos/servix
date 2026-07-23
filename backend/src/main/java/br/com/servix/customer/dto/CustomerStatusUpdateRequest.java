package br.com.servix.customer.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.constraints.NotNull;

public record CustomerStatusUpdateRequest(
        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        Boolean ativo) {
}
